package com.algotrading.base.examples;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.TimeFilters;
import com.algotrading.base.core.csv.CsvWriter;
import com.algotrading.base.core.marketdata.CandleDataProvider;
import com.algotrading.base.core.marketdata.locators.TimeframeCandleDataLocator;
import com.algotrading.base.core.marketdata.readers.FinamSeriesReader;
import com.algotrading.base.core.series.FinSeries;
import com.simpleutils.UserProperties;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;

/**
 * Экспорт свечей за данный период в формате Финама.
 */
class CandlesExporter {

    public static void main(final String[] args) {
        final LongPredicate timeFilter = TimeFilters.between(1000, 1850).and(TimeFilters.weekDays());
        new CandlesExporter()
//                .export("SBER", 5, TimeUnit.MINUTES, 20160101, 20170503);
//                .export("GAZP", 5, TimeUnit.MINUTES, 20160101, 20170503);
//                .export("LKOH", 5, TimeUnit.MINUTES, 20160101, 20170503);
//                .export("IMOEX", 1, TimeUnit.DAYS, 20170101, 20241201);
//                .export("CNYRUBF", 1, TimeUnit.DAYS, 20240101, 20250725, timeFilter);
//                .export("CRH24", 1, TimeUnit.DAYS, 20240101, 20250725, timeFilter);
//                .export("CRM24", 1, TimeUnit.DAYS, 20240101, 20250725, timeFilter);
//                .export("CRU24", 1, TimeUnit.DAYS, 20240101, 20250725, timeFilter);
//                .export("CRZ24", 1, TimeUnit.DAYS, 20240101, 20250725, timeFilter);
//                .export("CRH5", 1, TimeUnit.DAYS, 20240101, 20250725, timeFilter);
//                .export("CRM5", 1, TimeUnit.DAYS, 20240101, 20250725, timeFilter);
                .export("CRU5", 1, TimeUnit.DAYS, 20240101, 20250725, timeFilter);
    }

    private void export(final String secCode,
                        final int timeframe, final TimeUnit unit,
                        final int from, final int till,
                        final LongPredicate timeFilter) {
        final CandleDataProvider provider = new CandleDataProvider(
                new TimeframeCandleDataLocator(1, TimeUnit.MINUTES,
                        Path.of(UserProperties.get("marketData"), "Finam").toString(),
                        Path.of(UserProperties.get("marketData"), "Quik/Export").toString(),
                        Path.of(UserProperties.get("marketData"), "Quik/Archive").toString()),
                new FinamSeriesReader());

        System.out.println("Loading " + secCode + " " + from + "-" + till + "...");
        try (final PrintStream ps = new PrintStream(secCode + "_" + from + "_" + till + "_" + timeframe + ".csv", StandardCharsets.UTF_8)) {
            FinSeries series = provider.from(from).till(till).timeFilter(timeFilter).getSeries(secCode);
            if (timeframe != 1 || unit != TimeUnit.MINUTES) {
                series = series.compressedCandles(timeframe, unit);
            }
            final String prefix = secCode + ";" + timeframe;
            new CsvWriter()
                    .header("<TICKER>;<PER>;<DATE>;<TIME>;<OPEN>;<HIGH>;<LOW>;<CLOSE>;<VOL>")
                    .column(prefix)
                    .column(series.timeCode(), t -> String.valueOf(TimeCodes.yyyymmdd(t)))
                    .column(series.timeCode(), t -> String.valueOf(TimeCodes.hhmmss(t)))
                    .column(series.open())
                    .column(series.high())
                    .column(series.low())
                    .column(series.close())
                    .column(series.volume())
                    .write(ps, 0, series.length());
        } catch (final IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
