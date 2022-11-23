package com.algotrading.base.core.marketdata;

import com.algotrading.base.core.series.FinSeries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.LongPredicate;

public abstract class SeriesReader {

    protected File file = null;
    protected int from = 1900_01_01;
    protected int till = 2099_12_31;
    protected LongPredicate timeFilter = t -> true;

    public SeriesReader file(final File file) {
        this.file = file;
        return this;
    }

    public SeriesReader path(final Path path) {
        return file(path.toFile());
    }

    public SeriesReader from(final int yyyymmdd) {
        from = yyyymmdd;
        return this;
    }

    public SeriesReader from(final LocalDate localDate) {
        return from(yyyymmdd(localDate));
    }

    public SeriesReader till(final int yyyymmdd) {
        till = yyyymmdd;
        return this;
    }

    public SeriesReader till(final LocalDate localDate) {
        return till(yyyymmdd(localDate));
    }

    public SeriesReader timeFilter(final LongPredicate timeFilter) {
        this.timeFilter = timeFilter;
        return this;
    }

    public abstract FinSeries read() throws IOException;

    public abstract FinSeries readDaily() throws IOException;

    public abstract FinSeries readTicks() throws IOException;

    private static int yyyymmdd(final LocalDate localDate) {
        return localDate.getYear() * 10000 + localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
    }
}
