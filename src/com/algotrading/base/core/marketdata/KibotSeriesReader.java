package com.algotrading.base.core.marketdata;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.csv.CsvReader;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.values.LongValue;
import com.algotrading.base.core.values.StringValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.LongPredicate;

/**
 * Чтение данных в формате kibot.com из файла.
 */
public class KibotSeriesReader extends SeriesReader {

    @Override
    public KibotSeriesReader file(final File file) {
        super.file(file);
        return this;
    }

    @Override
    public KibotSeriesReader path(final Path path) {
        super.path(path);
        return this;
    }

    @Override
    public KibotSeriesReader from(final int yyyymmdd) {
        super.from(yyyymmdd);
        return this;
    }

    @Override
    public KibotSeriesReader from(final LocalDate localDate) {
        super.from(localDate);
        return this;
    }

    @Override
    public KibotSeriesReader till(final int yyyymmdd) {
        super.till(yyyymmdd);
        return this;
    }

    @Override
    public KibotSeriesReader till(final LocalDate localDate) {
        super.till(localDate);
        return this;
    }

    @Override
    public KibotSeriesReader timeFilter(final LongPredicate timeFilter) {
        super.timeFilter(timeFilter);
        return this;
    }

    /**
     * Чтение OHLCV-данных в формате kibot.com. Заголовка нет. Данные имеют вид:<br>
     * 05/28/2014,15:45,24.07,24.07,24.07,24.07,727,2
     * Содержимое последнего столбца неизвестно и он может отсутствовать.
     *
     * @return рыночные данные формата OHLCV
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    public FinSeries read() throws IOException {
        final FinSeries series = FinSeries.newCandles();
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy;H:mm");
        final StringValue mmddyyyy = new StringValue();
        final StringValue hhmm = new StringValue();
        final LongValue tValue = new LongValue();
        new CsvReader()
                .file(file)
                .splitSeparator(",")
                .linesToSkip(0)
                .value(mmddyyyy)
                .value(hhmm)
                .column(series.open())
                .column(series.high())
                .column(series.low())
                .column(series.close())
                .column(series.volume())
                .computation(series.timeCode(), () -> {
                    final long t = TimeCodes.timeCode(mmddyyyy.get() + ';' + hhmm.get(), dtf);
                    tValue.set(t);
                    return t;
                })
                .rowFilter(() -> {
                    final long t = tValue.get();
                    final int yymmdd = TimeCodes.yyyymmdd(t);
                    return from <= yymmdd && yymmdd <= till && timeFilter.test(t);
                })
                .read();
        return series;
    }

    /**
     * Чтение OHLCV-данных дневного таймфрейма в формате kibot.com. Заголовка нет. Данные имеют вид:<br>
     * 05/24/2022,140.83,141.97,137.33,140.42,92053388
     *
     * @return рыночные данные формата OHLCV
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    public FinSeries readDaily() throws IOException {
        final FinSeries series = FinSeries.newCandles();
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy;HH:mm");
        final StringValue mmddyyyy = new StringValue();
        final LongValue tValue = new LongValue();
        new CsvReader()
                .file(file)
                .splitSeparator(",")
                .linesToSkip(0)
                .value(mmddyyyy)
                .column(series.open())
                .column(series.high())
                .column(series.low())
                .column(series.close())
                .column(series.volume())
                .computation(series.timeCode(), () -> {
                    final long t = TimeCodes.timeCode(mmddyyyy.get() + ";00:00", dtf);
                    tValue.set(t);
                    return t;
                })
                .rowFilter(() -> {
                    final long t = tValue.get();
                    final int yyyymmdd = TimeCodes.yyyymmdd(t);
                    return from <= yyyymmdd && yyyymmdd <= till && timeFilter.test(t);
                })
                .read();
        return series;
    }

    @Override
    public FinSeries readTicks() {
        throw new UnsupportedOperationException("Not implemented!");
    }
}
