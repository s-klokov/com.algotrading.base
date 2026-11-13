package com.algotrading.base.examples;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.TimeFilters;
import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.csv.CsvWriter;
import com.algotrading.base.core.marketdata.CandleDataProvider;
import com.algotrading.base.core.marketdata.futures.Futures;
import com.algotrading.base.core.marketdata.futures.MoexFuturesExchange;
import com.algotrading.base.core.marketdata.locators.TimeframeCandleDataLocator;
import com.algotrading.base.core.marketdata.readers.FinamSeriesReader;
import com.algotrading.base.core.series.FinSeries;
import com.simpleutils.UserProperties;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;

/**
 * Экспорт свечей за данный период в формате Финама.
 */
class BrentCandlesExporter {

    public static void main(final String[] args) {
        new BrentCandlesExporter().export(
                20160201, 20261031,
                TimeFilters.between(700, 2350).and(TimeFilters.weekDays())
        );
    }

    private void export(final int from, final int till,
                        final LongPredicate timeFilter) {
        final CandleDataProvider provider = new CandleDataProvider(
                new TimeframeCandleDataLocator(1, TimeUnit.MINUTES,
                        Path.of(UserProperties.get("marketData"), "Finam").toString(),
                        Path.of(UserProperties.get("marketData"), "Quik/Export").toString(),
                        Path.of(UserProperties.get("marketData"), "Quik/Archive").toString()),
                new FinamSeriesReader());

        try (final PrintStream ps = new PrintStream("BR_" + from + "_" + till + ".csv", StandardCharsets.UTF_8)) {
            ps.println("SecCode;YYYYMM;O;H;L;C");
            final Map<String, Futures> map = MoexFuturesExchange.INSTANCE.byPrefix("BR");
            for (final Map.Entry<String, Futures> entry : map.entrySet()) {
                final Futures futures = entry.getValue();
                final int yyyymmddTill = futures.rolling;
                final int yyyymmddFrom = yyyymmddTill - (yyyymmddTill % 100) + 1;
                final LongPredicate dateFilter = t -> {
                    final int yyyymmdd = TimeCodes.yyyymmdd(t);
                    return yyyymmddFrom <= yyyymmdd && yyyymmdd <= yyyymmddTill;
                };
                FinSeries series = provider.from(from).till(till).timeFilter(dateFilter.and(timeFilter)).getSeries(entry.getKey());
                series = series.compressedCandles(1, TimeUnit.DAYS);
                if (series.length() < 5) {
                    continue;
                }
                System.out.println(entry.getKey() + ": " + series.length()
                        + " (" + TimeCodes.yyyymmdd(series.timeCode().get(0))
                        + "--" + TimeCodes.yyyymmdd(series.timeCode().getLast()) + ")");
                final double o = series.open().get(0);
                final DoubleColumn high = series.high();
                final DoubleColumn low = series.low();
                double h = o;
                double l = o;
                for (int i = 0; i < series.length(); i++) {
                    h = Math.max(h, high.get(i));
                    l = Math.min(l, low.get(i));
                }
                final double c = series.close().getLast();
                ps.printf("%s;%6d;%f;%f;%f;%f%n",
                        entry.getKey(),
                        yyyymmddFrom / 100,
                        o,
                        h,
                        l,
                        c
                );
            }
        } catch (final IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
