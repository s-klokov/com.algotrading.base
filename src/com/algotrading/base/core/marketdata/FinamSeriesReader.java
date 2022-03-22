package com.algotrading.base.core.marketdata;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.csv.CsvReader;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.values.LongValue;
import com.algotrading.base.core.values.StringValue;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.function.LongPredicate;

/**
 * Чтение данных в формате "Финам" из файла.
 */
public class FinamSeriesReader {

    private FinamSeriesReader() {
        throw new UnsupportedOperationException();
    }

    /**
     * Прочитать временной OHLCV-ряд формата "Финам" из файла.
     *
     * @param file файл
     * @return временной ряд
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static FinSeries readSeries(final File file) throws IOException {
        return readSeries(file, 1900_01_01, 2099_12_31, t -> true);
    }

    /**
     * Прочитать временной OHLCV-ряд формата "Финам" из файла.
     *
     * @param file       файл
     * @param from       начальная дата в формате yyyymmdd
     * @param till       конечная дата в формате yyyymmdd
     * @param timeFilter временной фильтр
     * @return временной ряд
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static FinSeries readSeries(final File file,
                                       final int from, final int till,
                                       final LongPredicate timeFilter) throws IOException {
        final FinSeries series = FinSeries.newCandles();
        final StringValue yyyymmdd = new StringValue();
        final StringValue hhmmss = new StringValue();
        final LongValue tValue = new LongValue();
        new CsvReader()
                .file(file)
                .splitSeparator(";")
                .linesToSkip(1)
                .skipColumn() // TICKER
                .skipColumn() // PERIOD
                .value(yyyymmdd)
                .value(hhmmss)
                .column(series.open())
                .column(series.high())
                .column(series.low())
                .column(series.close())
                .column(series.volume())
                .computation(series.timeCode(), () -> {
                    final long t = TimeCodes.timeCode(Integer.parseInt(yyyymmdd.get()), Integer.parseInt(hhmmss.get()), 0);
                    tValue.set(t);
                    return t;
                })
                .rowFilter(() -> from <= TimeCodes.yyyymmdd(tValue.get())
                                 && TimeCodes.yyyymmdd(tValue.get()) <= till
                                 && timeFilter.test(tValue.get()))
                .read();
        return series;
    }
}
