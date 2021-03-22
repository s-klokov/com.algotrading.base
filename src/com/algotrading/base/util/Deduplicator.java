package com.algotrading.base.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Устранение дубликатов объектов.
 */
public class Deduplicator<T> {

    private final Map<T, T> map = new ConcurrentHashMap<>();

    public T deduplicate(final T t) {
        final T exist = map.putIfAbsent(t, t);
        return (exist == null) ? t : exist;
    }

    public int size() {
        return map.size();
    }

    public static final Deduplicator<String> STRING_DEDUPLICATOR = new Deduplicator<>();
}
