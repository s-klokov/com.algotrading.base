package com.algotrading.base.core.marketdata;

import com.algotrading.base.core.series.FinSeries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.LongPredicate;

public abstract class SeriesReader<T extends SeriesReader<T>> {

    @SuppressWarnings("unchecked")
    protected final T thisAsT = (T) this;
    protected File file = null;
    protected int from = 1900_01_01;
    protected int till = 2099_12_31;
    protected LongPredicate timeFilter = t -> true;

    public T file(final File file) {
        this.file = file;
        return thisAsT;
    }

    public T path(final Path path) {
        return file(path.toFile());
    }

    public T from(final int yyyymmdd) {
        from = yyyymmdd;
        return thisAsT;
    }

    public T from(final LocalDate localDate) {
        return from(yyyymmdd(localDate));
    }

    public T till(final int yyyymmdd) {
        till = yyyymmdd;
        return thisAsT;
    }

    public T till(final LocalDate localDate) {
        return till(yyyymmdd(localDate));
    }

    public T timeFilter(final LongPredicate timeFilter) {
        this.timeFilter = timeFilter;
        return thisAsT;
    }

    public abstract FinSeries read() throws IOException;

    public abstract FinSeries readDaily() throws IOException;

    public abstract FinSeries readTicks() throws IOException;

    private static int yyyymmdd(final LocalDate localDate) {
        return localDate.getYear() * 10000 + localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
    }
}
