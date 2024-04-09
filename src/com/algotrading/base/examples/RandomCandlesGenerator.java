package com.algotrading.base.examples;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.csv.CsvWriter;
import com.algotrading.base.core.series.FinSeries;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Генерация случайных свечей.
 */
class RandomCandlesGenerator {

    public static void main(final String[] args) {
        final Random rnd = new Random(5L);
        final FinSeries series = FinSeries.newCandles();
        final LongColumn timeCode = series.timeCode();
        final DoubleColumn open = series.open();
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        final LongColumn volume = series.volume();
        double logPrice = 1.0;
        final double k = 1000.0;
        for (LocalDateTime dt = LocalDateTime.of(2011, 1, 1, 10, 0);
                dt.isBefore(LocalDateTime.of(2017, 7, 13, 18, 40));
                dt = dt.plusMinutes(1)) {
            final long t = TimeCodes.timeCode(dt);
            final int hhmm = TimeCodes.hhmm(t);
            if (1000 <= hhmm && hhmm < 1840) {
                final double o = k * Math.exp(logPrice);
                double h = o;
                double l = o;
                for (int i = 0; i < 10; i++) {
                    logPrice += -0.001 + 0.002 * rnd.nextDouble();
                    final double price = k * Math.exp(logPrice);
                    h = Math.max(h, price);
                    l = Math.min(l, price);
                }
                final double c = k * Math.exp(logPrice);
                final long v = Math.round(1 + Math.exp(5 * rnd.nextDouble()));
                timeCode.append(t);
                open.append(o);
                high.append(h);
                low.append(l);
                close.append(c);
                volume.append(v);
            }
        }
        try (final PrintStream ps = new PrintStream("RND.csv", StandardCharsets.UTF_8)) {
            new CsvWriter()
                    .header("<TICKER>;<PERIOD>;<DATE>;<TIME>;<OPEN>;<HIGH>;<LOW>;<CLOSE>;<VOLUME>")
                    .column("RND;1")
                    .column(series.timeCode(), t -> String.valueOf(TimeCodes.yyyymmdd(t)))
                    .column(series.timeCode(), t -> String.valueOf(TimeCodes.hhmmss(t)))
                    .column(series.open())
                    .column(series.high())
                    .column(series.low())
                    .column(series.close())
                    .column(series.volume())
                    .write(ps, 0, series.length());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
