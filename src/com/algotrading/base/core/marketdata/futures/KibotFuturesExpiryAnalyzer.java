package com.algotrading.base.core.marketdata.futures;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.marketdata.CandleDataProvider;
import com.algotrading.base.core.marketdata.locators.FolderCandleDataLocator;
import com.algotrading.base.core.marketdata.readers.KibotSeriesReader;
import com.algotrading.base.core.series.FinSeries;
import com.simpleutils.UserProperties;

import java.io.IOException;

/**
 * Определение даты экспирации и даты последнего дня торговли перед экспирацией для данных Kibot.
 */
class KibotFuturesExpiryAnalyzer {

    private CandleDataProvider candleDataProvider = null;
    private int from = 0;
    private int till = 0;

    public static void main(final String[] args) {
        final KibotFuturesExpiryAnalyzer analyzer = new KibotFuturesExpiryAnalyzer();
        analyzer.candleDataProvider = new CandleDataProvider(
                new FolderCandleDataLocator(sec -> sec + ".txt",
                        UserProperties.get("diskM") + "Kostya/20240711/5min"),
                new KibotSeriesReader());
        analyzer.from = 20140101;
        analyzer.till = 20240425;
        final String[] prefixes = {
                "AD", "BR", "BTC", "BZ", "C", "CD", "CL", "ES", "GF", "HE",
                "HG", "LB", "LE", "NG", "NQ", "PA", "PL", "PX", "RA", "RR",
                "RTY", "S", "SI", "SIR", "SM", "TY", "VX", "W", "XAE", "YC",
                "YK", "YW",
        };

        for (final String prefix : prefixes) {
            System.out.println("addFuturesMap(new FuturesMapBuilder(\"" + prefix + "\")");
            analyzer.analyze(prefix, prefix + "-");
            System.out.println(".build()");
            System.out.println(");");
            System.out.println();
        }
    }

    private void analyze(final String prefix, final String longCode) {
        final String monthSymbols = "*FGHJKMNQUVXZ";
        final int yyFrom = (from / 10000) % 100;
        final int yyTill = (till / 10000) % 100;
        int prevExpiry = 0;
        for (int yy = yyFrom; yy <= yyTill; yy++) {
            for (int month = 1; month <= 12; month++) {
                final char m = monthSymbols.charAt(month);
                final String secCode = prefix + m + yy;
                try {
                    final FinSeries series = candleDataProvider
                            .from(from)
                            .till(till)
                            .timeFilter(t -> true)
                            .getSeries(secCode);
                    if (series.length() > 0) {
                        final int expiry = TimeCodes.yyyymmdd(series.timeCode().getLast());
                        for (int i = series.length() - 1; i >= 0; i--) {
                            final long t = series.timeCode().get(i);
                            final int yyyymmdd = TimeCodes.yyyymmdd(t);
                            if (yyyymmdd != expiry) {
                                System.out.println(".put(\"" + longCode + month + "." + yy + "\", \"" + secCode + "\", " + expiry + ", " + yyyymmdd + ", " + prevExpiry + ")");
                                prevExpiry = expiry;
                                break;
                            }
                        }
                    }
                } catch (final IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }
}
