package com.algotrading.base.examples;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.TimeFilters;
import com.algotrading.base.core.csv.CsvWriter;
import com.algotrading.base.core.marketdata.CandleDataProvider;
import com.algotrading.base.core.marketdata.readers.FinamSeriesReader;
import com.algotrading.base.core.marketdata.locators.TimeframeCandleDataLocator;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.helpers.IOHelper;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

/**
 * Экспорт свечей за данный период в формате Финама.
 */
class CandlesExporter {

    public static void main(final String[] args) {
        new CandlesExporter()
//                .export("SBER", 5, TimeUnit.MINUTES, 20160101, 20170503);
//                .export("GAZP", 5, TimeUnit.MINUTES, 20160101, 20170503);
                .export("LKOH", 5, TimeUnit.MINUTES, 20160101, 20170503);
    }

    private void export(final String secCode,
                        final int timeframe, final TimeUnit unit,
                        final int from, final int till) {
        final CandleDataProvider provider = new CandleDataProvider(
                new TimeframeCandleDataLocator(1, TimeUnit.MINUTES,
                        "M:/Finam", "M:/Quik/Export", "M:/Quik/Archive"),
                new FinamSeriesReader());

        System.out.println("Loading " + secCode + " " + from + "-" + till + "...");
        try (final PrintStream ps = IOHelper.getPrintStream(secCode + "_" + from + "_" + till + "_" + timeframe + ".csv")) {
            FinSeries series = provider.from(from).till(till).timeFilter(TimeFilters.FILTER_1000_1840).getSeries(secCode);
            if (timeframe != 1) {
                series = series.compressedCandles(timeframe, TimeUnit.MINUTES);
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
            e.printStackTrace();
        }
    }
}
