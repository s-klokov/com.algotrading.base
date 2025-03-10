package com.algotrading.base.core;

import java.time.LocalDate;
import java.util.function.LongPredicate;

/**
 * Фильтры по времени.
 */
public class TimeFilters {

    private TimeFilters() {
        throw new UnsupportedOperationException();
    }

    /**
     * Получить фильтр по времени между указанными границами.
     *
     * @param hhmmFrom начало
     * @param hhmmTill конец
     * @return фильтр
     */
    public static LongPredicate between(final int hhmmFrom, final int hhmmTill) {
        if (hhmmFrom <= hhmmTill) {
            return t -> {
                final int hhmm = TimeCodes.hhmm(t);
                return hhmmFrom <= hhmm && hhmm < hhmmTill;
            };
        } else {
            return t -> {
                final int hhmm = TimeCodes.hhmm(t);
                return hhmm >= hhmmFrom || hhmm < hhmmTill;
            };
        }
    }

    /**
     * Получить фильтр по времени, где выбираются только дни с понедельника по пятницу.
     *
     * @return фильтр
     */
    public static LongPredicate weekDays() {
        return t -> switch (LocalDate.of(TimeCodes.year(t), TimeCodes.month(t), TimeCodes.day(t)).getDayOfWeek()) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> true;
            case SATURDAY, SUNDAY -> false;
        };
    }

    /**
     * Фильтр для включения утренней сессии и дневной сессии и исключения вечерней сессии.
     */
    public static final LongPredicate FILTER_0900_1850 = between(900, 1850);
    /**
     * Фильтр для акций: с 10:00 до 18:40 (дневная сессия без аукционов открытия и закрытия).
     */
    public static final LongPredicate FILTER_1000_1840 = between(1000, 1840);
    /**
     * Фильтр для фьючерсов: с 10:00 до 18:45 (только дневная сессия).
     */
    public static final LongPredicate FILTER_1000_1845 = between(1000, 1845);
    /**
     * Фильтр для фьючерсов: с 10:00 до 18:50 (только дневная сессия).
     */
    public static final LongPredicate FILTER_1000_1850 = between(1000, 1850);
    /**
     * Фильтр для фьючерсов: с 10:00 до 23:50 (дневная и вечерняя сессии).
     */
    public static final LongPredicate FILTER_1000_2350 = between(1000, 2350);
    /**
     * Фильтр для американских акций: с 9:30 до 16:00.
     */
    public static final LongPredicate FILTER_0930_1600 = between(930, 1600);
}
