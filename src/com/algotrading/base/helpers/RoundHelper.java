package com.algotrading.base.helpers;

import java.util.HashMap;
import java.util.Map;

public class RoundHelper {

    private RoundHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * Округлить значения в соответствии: k -> doubleValue и записать в соответствие k -> longValue.
     *
     * @param doubleMap соответствие со значениями типа {@code double}
     * @param longMap   {@code null} или соответствие со значениями типа {@code long}, куда будут помещены результаты
     *                  округления
     * @param <T>       тип ключа
     * @return соответствие со значениями типа {@code long}
     */
    public static <T> Map<T, Long> round(final Map<T, Double> doubleMap, Map<T, Long> longMap) {
        if (longMap == null) {
            longMap = new HashMap<>();
        }
        for (final Map.Entry<T, Double> entry : doubleMap.entrySet()) {
            longMap.put(entry.getKey(), Math.round(entry.getValue()));
        }
        return longMap;
    }
}
