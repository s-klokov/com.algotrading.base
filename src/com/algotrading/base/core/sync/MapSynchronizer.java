package com.algotrading.base.core.sync;

import com.algotrading.base.core.series.FinSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Класс для получения событий из соответствия key -> FinSeries в хронологическом порядке.
 * Предполагается, что события внутри каждого из объектов типа FinSeries уже идут в хронологическом порядке.
 */
public class MapSynchronizer<K> {
    private final List<Cursor<K>> cursors;
    private Cursor<K> currentCursor = null;
    private long tCurrent = 0L;

    public MapSynchronizer(final Map<? extends K, ? extends FinSeries> map) {
        cursors = new ArrayList<>(map.size());
        map.forEach((key, series) -> cursors.add(new Cursor<>(key, series)));
    }

    public Cursor<K> current() {
        return currentCursor;
    }

    public boolean hasNext() {
        for (final Cursor<K> cursor : cursors) {
            if (cursor.hasNext()) {
                return true;
            }
        }
        return false;
    }

    public Cursor<K> next() {
        long tNext = Long.MAX_VALUE;
        for (final Cursor<K> cursor : cursors) {
            if (cursor.hasNext()) {
                tNext = Math.min(tNext, cursor.timeCode.get(cursor.id() + 1));
                if (tNext <= tCurrent) {
                    tCurrent = tNext;
                    cursor.next();
                    currentCursor = cursor;
                    return currentCursor;
                }
            }
        }
        for (final Cursor<K> cursor : cursors) {
            if (cursor.hasNext() && cursor.timeCode.get(cursor.id() + 1) == tNext) {
                tCurrent = tNext;
                cursor.next();
                currentCursor = cursor;
                return currentCursor;
            }
        }
        throw new NoSuchElementException("End of all series");
    }
}
