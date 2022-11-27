package com.algotrading.base.core.marketdata;

import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.marketdata.locators.CandleDataLocator;
import com.algotrading.base.core.marketdata.readers.SeriesReader;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.sync.Synchronizer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

public class CandleDataProvider2 {

    private final CandleDataLocator candleDataLocator;
    private final SeriesReader seriesReader;

    public CandleDataProvider2(final CandleDataLocator candleDataLocator,
                               final SeriesReader seriesReader) {
        this.candleDataLocator = candleDataLocator;
        this.seriesReader = seriesReader;
    }

    public CandleDataLocator candleDataLocator() {
        return candleDataLocator;
    }

    public  SeriesReader seriesReader() {
        return seriesReader;
    }

    public CandleDataProvider2 from(final int yyyymmdd) {
        candleDataLocator().from(yyyymmdd);
        seriesReader().from(yyyymmdd);
        return this;
    }

    public CandleDataProvider2 from(final LocalDate localDate) {
        candleDataLocator().from(localDate);
        seriesReader().from(localDate);
        return this;
    }

    public CandleDataProvider2 till(final int yyyymmdd) {
        candleDataLocator().till(yyyymmdd);
        seriesReader().till(yyyymmdd);
        return this;
    }

    public CandleDataProvider2 till(final LocalDate localDate) {
        candleDataLocator().till(localDate);
        seriesReader().till(localDate);
        return this;
    }

    public CandleDataProvider2 timeFilter(final LongPredicate timeFilter) {
        seriesReader().timeFilter(timeFilter);
        return this;
    }

    public FinSeries getSeries(final String secCode) throws IOException {
        final List<File> files = candleDataLocator().getFiles(secCode);
        final List<FinSeries> seriesList = new ArrayList<>(files.size());
        final List<LongColumn> timeColumnList = new ArrayList<>(files.size());
        final Synchronizer synchronizer = new Synchronizer();
        for (final File file : files) {
            final FinSeries series = seriesReader().file(file).read();
            seriesList.add(series);
            final LongColumn timeColumn = series.timeCode();
            timeColumnList.add(timeColumn);
            synchronizer.put(timeColumn);
        }
        final FinSeries series = FinSeries.newCandles();
        long timeCode;
        while ((timeCode = synchronizer.synchronize()) != Long.MAX_VALUE) {
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
        return series;
    }
}
