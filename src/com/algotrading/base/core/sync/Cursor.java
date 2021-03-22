package com.algotrading.base.core.sync;

import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;

import java.util.NoSuchElementException;

/**
 * Курсор для класса {@link MapSynchronizer}.
 */
public final class Cursor<K> {
    public final K key;
    public final FinSeries series;
    public final LongColumn timeCode;
    private int id;

    public Cursor(final K key, final FinSeries series) {
        this.key = key;
        this.series = series;
        timeCode = series.timeCode();
        id = -1;
    }

    public boolean hasNext() {
        return id < timeCode.length() - 1;
    }

    public void next() {
        if (hasNext()) {
            id++;
        } else {
            throw new NoSuchElementException("End of series " + series);
        }
    }

    public int id() {
        return id;
    }

    public long t() {
        return timeCode.get(id);
    }
}
