package com.algotrading.base.core.marketdata;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.csv.CsvReader;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.values.DoubleValue;
import com.algotrading.base.core.values.IntValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

/**
 * Источник тиковых данных.
 */
public class TickDataProvider {

    private TickDataProvider(){
    }

    /**
     * Загрузить тиковые данные.
     *
     * @param file       файл для загрузки
     * @param timeFilter фильтр по времени
     * @return временной ряд тиков с колонками timeCode, last, volume, buySell
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static FinSeries getTickSeries(final File file, final LongPredicate timeFilter) throws IOException {
        final IntValue yyyymmdd = new IntValue();
        final DoubleValue hhmmssms = new DoubleValue();
        final FinSeries series = FinSeries.newLastVol().withIntColumn("BUYSELL");
        final LongColumn timeCode = series.timeCode();
        new CsvReader()
                .file(file)
                .splitSeparator(";")
                .linesToSkip(1)
                .value(yyyymmdd)
                .value(hhmmssms)
                .column(series.last())
                .column(series.volume())
                .column(series.getIntColumn("BUYSELL"))
                .computation(series.timeCode(), () -> TimeCodes.timeCode(yyyymmdd, hhmmssms))
                .rowFilter(() -> timeFilter.test(timeCode.get(timeCode.length() - 1)))
                .read();
        return series;
    }

    /**
     * Загрузить тиковые данные.
     *
     * @param files      массив файлов для загрузки
     * @param timeFilter фильтр по времени
     * @return список временных рядов тиков с колонками timeCode, last, volume, buySell
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static List<FinSeries> getTickSeries(final File[] files, final LongPredicate timeFilter) throws IOException {
        final List<FinSeries> list = new ArrayList<>(files.length);
        for (final File file : files) {
            list.add(getTickSeries(file, timeFilter));
        }
        return list;
    }
}
