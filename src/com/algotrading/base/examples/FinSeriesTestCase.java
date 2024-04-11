package com.algotrading.base.examples;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.csv.CsvReader;
import com.algotrading.base.core.csv.CsvWriter;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.values.DoubleValue;
import com.algotrading.base.core.values.IntValue;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Пример чтения и записи csv-файлов с помощью {@link CsvReader} и {@link CsvWriter}.
 */
class FinSeriesTestCase {

    public static void main(final String[] args) {
        final FinSeries series = FinSeries.newCandles()
                .withStringColumn("Comment")
                .withColumn("Info", String.class);
        final IntValue yyyymmdd = new IntValue();
        final IntValue hhmmss = new IntValue();
        try {
            new CsvReader()
                    .file("SBER_160101_160401_1m.csv")
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
                    .computation(series.timeCode(), () -> TimeCodes.t(yyyymmdd, hhmmss))
                    .computation(series.getStringColumn("Comment"), () -> "No comment")
                    .computation(series.getColumn("Info"), () -> "Some info")
                    .read();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        System.out.println(series.hasEqualColumnLength());

        try (final PrintStream ps = new PrintStream("series_test.csv", StandardCharsets.UTF_8)) {
            new CsvWriter()
                    .header("<TICKER>;<PER>;<DATE>;<TIME>;<OPEN>;<HIGH>;<LOW>;<CLOSE>;<VOL>")
                    .separator(";")
                    .column("SBER;1")
                    .column(series.timeCode(), timeCode -> String.valueOf(TimeCodes.yyyymmdd(timeCode)))
                    .column(series.timeCode(), timeCode -> String.valueOf(TimeCodes.hhmmss(timeCode)))
                    .column(series.open(), "%.7f")
                    .column(series.high(), "%.7f")
                    .column(series.low(), "%.7f")
                    .column(series.close(), "%.7f")
                    .column(series.volume())
                    .write(ps, 0, series.length());
        } catch (final IOException e) {
            e.printStackTrace();
        }

        System.out.println(series);
        series.columns().forEach(System.out::println);
        System.out.println(series.hasEqualColumnLength());
        System.out.println(series.timeCode().isIncreasing());

        final FinSeries ticks = FinSeries.newLastVol()
//                .withIntColumn("BuySell")
                .ensureCapacity(1000);
        final IntValue date = new IntValue();
        final DoubleValue time = new DoubleValue();
        try {
            new CsvReader()
                    .file("SiU5_150616_150914_ticks.zip")
                    .splitSeparator(";")
                    .linesToSkip(1)
                    .value(date)
                    .value(time)
                    .column(ticks.last())
                    .column(ticks.volume())
//                    .column(ticks.getIntColumn("BuySell"))
                    .computation(ticks.timeCode(), () -> TimeCodes.t(date, time))
                    .read();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        System.out.println(ticks);

        final FinSeries candles = ticks.compressedCandles(timeCode -> TimeCodes.hhmm(timeCode) < 1845, 15, TimeUnit.MINUTES);
//        final FinSeries candles = ticks.compressedDailyCandles(null);
        System.out.println(candles);

        try (final PrintStream ps = new PrintStream("series_test2.csv", StandardCharsets.UTF_8)) {
            new CsvWriter()
                    .header("<TICKER>;<PER>;<DATE>;<TIME>;<OPEN>;<HIGH>;<LOW>;<CLOSE>;<VOL>")
                    .separator(";")
                    .column("SiU5;15")
//                    .column("SiU5;D")
                    .column(candles.timeCode(), timeCode -> String.valueOf(TimeCodes.yyyymmdd(timeCode)))
                    .column(candles.timeCode(), timeCode -> String.valueOf(TimeCodes.hhmmss(timeCode)))
                    .column(candles.open(), "%s")
                    .column(candles.high(), "%s")
                    .column(candles.low(), "%s")
                    .column(candles.close(), "%s")
                    .column(candles.volume())
                    .write(ps, 0, candles.length());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
