package com.algotrading.base.core;

import java.util.function.LongPredicate;

/**
 * Фильтры по времени.
 */
public class TimeFilters {

    private TimeFilters() {
    }

    /**
     * Фильтр для включения утренней сессии и дневной сессии и исключения вечерней сессии.
     */
    public static final LongPredicate FILTER_0900_1850 = t -> {
        final int hhmm = TimeCodes.hhmm(t);
        return 900 <= hhmm && hhmm < 1850;
    };
    /**
     * Фильтр для акций: с 10:00 до 18:40 (дневная сессия без аукционов открытия и закрытия).
     */
    public static final LongPredicate FILTER_1000_1840 = t -> {
        final int hhmm = TimeCodes.hhmm(t);
        return 1000 <= hhmm && hhmm < 1840;
    };
    /**
     * Фильтр для фьючерсов: с 10:00 до 18:45 (только дневная сессия).
     */
    public static final LongPredicate FILTER_1000_1845 = t -> {
        final int hhmm = TimeCodes.hhmm(t);
        return 1000 <= hhmm && hhmm < 1845;
    };
    /**
     * Фильтр для фьючерсов: с 10:00 до 18:50 (только дневная сессия).
     */
    public static final LongPredicate FILTER_1000_1850 = t -> {
        final int hhmm = TimeCodes.hhmm(t);
        return 1000 <= hhmm && hhmm < 1850;
    };
    /**
     * Фильтр для фьючерсов: с 10:00 до 23:50 (дневная и вечерняя сессии).
     */
    public static final LongPredicate FILTER_1000_2350 = t -> {
        final int hhmm = TimeCodes.hhmm(t);
        return 1000 <= hhmm && hhmm < 2350;
    };
    /**
     * Фильтр для американских акций: с 9:30 до 16:00.
     */
    public static final LongPredicate FILTER_0930_1600 = t -> {
        final int hhmm = TimeCodes.hhmm(t);
        return 930 <= hhmm && hhmm < 1600;
    };
}
