package com.algotrading.base.core.marketdata;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.csv.CsvReader;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.sync.Synchronizer;
import com.algotrading.base.core.values.IntValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Источник свечных данных.
 * <p>
 * Реализовано получение минутных данных из папок вида "1" (1-минутные данные),
 * секундных данных из папок вида "5s" (5-секундные данные)
 * и дневных данных из папок вида "D" (дневные данные).
 */
public class CandleDataProvider {

    private static final Pattern TWO_DATE_PATTERN = Pattern.compile("_2?0?(\\d{6})_2?0?(\\d{6}).*");
    private static final Pattern ONE_DATE_PATTERN = Pattern.compile("_2?0?(\\d{6}).*");

    private final String[] paths;

    /**
     * Конструктор.
     *
     * @param paths набор путей, где будет происходить поиск данных.
     */
    public CandleDataProvider(final String... paths) {
        this.paths = paths;
    }

    private List<File> getFiles(final String secCode,
                                final int timeFrame,
                                final TimeUnit timeUnit,
                                final long timeCodeFrom,
                                final long timeCodeTill) {
        final String secPrefix = Futures.getPrefix(secCode);
        final int yyyymmddFrom = TimeCodes.yyyymmdd(timeCodeFrom);
        final int yyyymmddTill = TimeCodes.yyyymmdd(timeCodeTill);
        final List<File> files = new ArrayList<>();
        for (final String path : paths) {
            File[] folderFiles = getFiles(path, secPrefix, timeFrame, timeUnit, secCode);
            if (folderFiles != null) {
                filterFiles(folderFiles, secCode, yyyymmddFrom, yyyymmddTill, files);
            }
            folderFiles = getFiles(path, secCode, timeFrame, timeUnit, secCode);
            if (folderFiles != null) {
                filterFiles(folderFiles, secCode, yyyymmddFrom, yyyymmddTill, files);
            }
        }
        return files;
    }

    private File[] getFiles(final String path, final String secPrefix,
                            final int timeFrame, final TimeUnit timeUnit,
                            final String secCode) {
        File folder = new File(path, secPrefix);
        if (folder.isDirectory()) {
            if (timeUnit == TimeUnit.MINUTES) {
                folder = new File(folder, String.valueOf(timeFrame));
                if (folder.isDirectory()) {
                    return folder.listFiles((dir, name) -> name.startsWith(secCode + '_'));
                }
            } else if (timeUnit == TimeUnit.SECONDS) {
                folder = new File(folder, timeFrame + "s");
                if (folder.isDirectory()) {
                    return folder.listFiles((dir, name) -> name.startsWith(secCode + '_'));
                }
            } else if (timeFrame == 1 && timeUnit == TimeUnit.DAYS) {
                folder = new File(folder, "D");
                if (folder.isDirectory()) {
                    return folder.listFiles((dir, name) -> name.startsWith(secCode + '_'));
                }
            } else if (timeFrame == 1 && timeUnit == TimeUnit.HOURS) {
                folder = new File(folder, "60");
                if (folder.isDirectory()) {
                    return folder.listFiles((dir, name) -> name.startsWith(secCode + '_'));
                }
            }
        }
        return null;
    }

    private void filterFiles(final File[] folderFiles,
                             final String secCode,
                             final int yyyymmddFrom, final int yyyymmddTill,
                             final List<File> files) {
        for (final File file : folderFiles) {
            final String suffix = file.getName().substring(secCode.length());
            Matcher matcher = TWO_DATE_PATTERN.matcher(suffix);
            if (matcher.matches()) {
                final int from = 20000000 + Integer.parseInt(matcher.group(1));
                final int till = 20000000 + Integer.parseInt(matcher.group(2));
                if (from <= yyyymmddTill && till >= yyyymmddFrom && !files.contains(file)) {
                    files.add(file);
                }
                continue;
            }
            matcher = ONE_DATE_PATTERN.matcher(suffix);
            if (matcher.matches()) {
                final int yyyymmdd = 20000000 + Integer.parseInt(matcher.group(1));
                if (yyyymmddFrom <= yyyymmdd && yyyymmdd <= yyyymmddTill && !files.contains(file)) {
                    files.add(file);
                }
            }
        }
    }

    /**
     * Получить свечи заданного таймфрейма из файлового хранилища.
     *
     * @param secCode      код инструмента
     * @param timeFrame    тайфрейм
     * @param timeUnit     единица измерения времени
     * @param timeCodeFrom начало запрашиваемого периода
     * @param timeCodeTill конец запрашиваемого периода
     * @return временной ряд свечей типа {@link FinSeries}.
     * @throws IOException если произошла ошибка ввода-вывода.
     */
    public FinSeries getSeries(final String secCode,
                               final int timeFrame,
                               final TimeUnit timeUnit,
                               final long timeCodeFrom,
                               final long timeCodeTill) throws IOException {
        return getSeries(secCode, timeFrame, timeUnit, timeCodeFrom, timeCodeTill, timeCode -> true);
    }

    /**
     * Получить свечи заданного таймфрейма из файлового хранилища.
     *
     * @param secCode      код инструмента
     * @param timeFrame    тайфрейм
     * @param timeUnit     единица измерения времени
     * @param timeCodeFrom начало запрашиваемого периода
     * @param timeCodeTill конец запрашиваемого периода
     * @param timeFilter   временной фильтр свечей
     * @return временной ряд свечей типа {@link FinSeries}.
     * @throws IOException если произошла ошибка ввода-вывода.
     */
    public FinSeries getSeries(final String secCode,
                               final int timeFrame,
                               final TimeUnit timeUnit,
                               final long timeCodeFrom,
                               final long timeCodeTill,
                               final LongPredicate timeFilter) throws IOException {
        final List<File> files = getFiles(secCode, timeFrame, timeUnit, timeCodeFrom, timeCodeTill);
        final List<FinSeries> seriesList = new ArrayList<>(files.size());
        final List<LongColumn> timeColumnList = new ArrayList<>(files.size());
        final Synchronizer synchronizer = new Synchronizer();
        for (final File file : files) {
            final FinSeries series = loadSeries(file, timeFilter);
            seriesList.add(series);
            final LongColumn timeColumn = series.timeCode();
            timeColumnList.add(timeColumn);
            synchronizer.put(timeColumn);
        }
        final FinSeries series = FinSeries.newCandles();
        long timeCode;
        while ((timeCode = synchronizer.synchronize()) < timeCodeTill) {
            if (timeCode >= timeCodeFrom) {
                for (int i = 0; i < timeColumnList.size(); i++) {
                    final int index = synchronizer.getCurrIndex(timeColumnList.get(i));
                    if (index >= 0) {
                        final FinSeries src = seriesList.get(i);
                        series.timeCode().append(timeCode);
                        series.open().append(src.open().get(index));
                        series.high().append(src.high().get(index));
                        series.low().append(src.low().get(index));
                        series.close().append(src.close().get(index));
                        series.volume().append(src.volume().get(index));
                        break;
                    }
                }
            }
        }
        return series;
    }

    /**
     * Загрузить свечные данные в формате Finam.
     *
     * @param file       файл
     * @param timeFilter фильтр по времени
     * @return временной ряд со свечами
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static FinSeries loadSeries(final File file, final LongPredicate timeFilter) throws IOException {
        final IntValue yyyymmdd = new IntValue();
        final IntValue hhmmss = new IntValue();
        final FinSeries series = FinSeries.newCandles();
        final LongColumn timeCode = series.timeCode();
        new CsvReader()
                .file(file)
                .splitSeparator(";")
                .linesToSkip(1)
                .skipColumn()
                .skipColumn()
                .value(yyyymmdd)
                .value(hhmmss)
                .column(series.open())
                .column(series.high())
                .column(series.low())
                .column(series.close())
                .column(series.volume())
                .computation(series.timeCode(), () -> TimeCodes.timeCode(yyyymmdd, hhmmss))
                .rowFilter(() -> timeFilter.test(timeCode.get(timeCode.length() - 1)))
                .read();
        return series;
    }
}
