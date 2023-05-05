package com.algotrading.base.core.tester;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.marketdata.Futures;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Индексы для оптимизации и тестирования в walkforward-тесте.
 */
public class WalkforwardIndices {
    /**
     * Индекс начала периода оптимизации.
     */
    public final int optFrom;
    /**
     * Индекс конца периода оптимизации.
     */
    public final int optTo;
    /**
     * Индекс начала торговли с использованием оптимальных параметров.
     */
    public final int tradeFrom;
    /**
     * Индекс конца торговли с использованием оптимальных параметров.
     */
    public final int tradeTo;

    public WalkforwardIndices(final int optFrom, final int optTo,
                              final int tradeFrom, final int tradeTo) {
        if (0 <= optFrom && optFrom < optTo && optTo <= tradeFrom && tradeFrom < tradeTo) {
            this.optFrom = optFrom;
            this.optTo = optTo;
            this.tradeFrom = tradeFrom;
            this.tradeTo = tradeTo;
        } else {
            throw new IllegalArgumentException("Invalid indices: " + optFrom + ", " + optTo
                                               + ", " + tradeFrom + ", " + tradeTo);
        }
    }

    /**
     * Найти в данной колонке меток времени индекс для указанных даты и времени.
     * Если индекс, где дата и время совпадают точно, не найдены, то ищется индекс, для которого
     * дата не позже указанной даты и время дня не позже указанного времени.
     * Если индекс не удалось найти, возвращается 0.
     *
     * @param timeCode колонка с метками времени
     * @param date     дата
     * @param hhmmss   время в формате HHMMSS
     * @return индекс метки времени
     */
    public static int getIndex(final LongColumn timeCode, final LocalDate date, final int hhmmss) {
        final int yyyymmdd = date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
        long t = TimeCodes.timeCode(yyyymmdd, hhmmss);
        int id = timeCode.binarySearch(t);
        if (id >= 0) {
            return id;
        }
        id = -id - 1;
        if (id == timeCode.length()) {
            return id;
        }
        while (--id >= 0) {
            t = timeCode.get(id);
            if (TimeCodes.yyyymmdd(t) <= yyyymmdd && TimeCodes.hhmmss(t) <= hhmmss) {
                return id;
            }
        }
        return 0;
    }

    /**
     * Получить список индексов для walkforward-теста.
     *
     * @param timeCode            колонка с метками времени из временного ряда эквити
     * @param from                дата начало периода торговли в формате YYYYMMDD
     * @param till                дата конца периода торговли в формате YYYYMMDD
     * @param hhmmss              время в формате HHMMSS, когда происходит смена фрагмента эквити
     * @param walkforwardStep     шаг walkforward-теста
     * @param optimizationHorizon горизонт оптимизации
     * @return список индексов для walkforward-теста
     */
    public static List<WalkforwardIndices> getRegularWalkforwardIndices(final LongColumn timeCode,
                                                                        final int from,
                                                                        final int till,
                                                                        final int hhmmss,
                                                                        final Period walkforwardStep,
                                                                        final Period optimizationHorizon) {
        final List<WalkforwardIndices> list = new ArrayList<>();

        final LocalDate dtFrom = LocalDate.of(from / 10000, (from / 100) % 100, from % 100);
        final LocalDate dtTill = LocalDate.of(till / 10000, (till / 100) % 100, till % 100);

        for (LocalDate d0 = dtFrom, d1 = dtFrom.plus(walkforwardStep); !d0.isAfter(dtTill); d0 = d1, d1 = d1.plus(walkforwardStep)) {
            final int tradeFrom = getIndex(timeCode, d0, hhmmss);
            final int tradeTo = getIndex(timeCode, d1, hhmmss);
            final int optFrom = getIndex(timeCode, d0.minus(optimizationHorizon), hhmmss);
            list.add(new WalkforwardIndices(optFrom, tradeFrom, tradeFrom, tradeTo));
        }

        return list;
    }

    /**
     * Получить список индексов для walkforward-теста на фьючерсах.
     * Смена фрагмента эквити происходит в день, предшествующий дню экспирации.
     *
     * @param timeCode            колонка с метками времени из временного ряда эквити
     * @param from                дата начало периода торговли в формате YYYYMMDD
     * @param till                дата конца периода торговли в формате YYYYMMDD
     * @param futuresPrefix       префикс фьючерса
     * @param optimizationFutures количество прошлых фьючерсов для оптимизации
     * @return список индексов для walkforward-теста
     */
    public static List<WalkforwardIndices> getFuturesWalkforwardIndices(final LongColumn timeCode,
                                                                        final int from,
                                                                        final int till,
                                                                        final String futuresPrefix,
                                                                        final int optimizationFutures) {
        final List<WalkforwardIndices> list = new ArrayList<>();
        final Futures[] futures = Futures.byPrefix(futuresPrefix);
        for (int i = optimizationFutures; i < futures.length; i++) {
            final Futures f = futures[i];
            final int futFrom = Math.max(from, f.previousExpiry);
            final int futTill = Math.min(till, f.oneDayBeforeExpiry);
            if (futFrom < futTill) {
                final int tradeFrom = getFirstDayIndex(timeCode, futFrom);
                final int tradeTo = getLastDayIndex(timeCode, futTill) + 1;
                final int optFrom = getFirstDayIndex(timeCode, futures[i - optimizationFutures].previousExpiry);
                list.add(new WalkforwardIndices(optFrom, tradeFrom, tradeFrom, tradeTo));
            }
        }
        return list;
    }

    private static int getFirstDayIndex(final LongColumn timeCode, final int yyyymmdd) {
        final int len = timeCode.length();
        for (int i = 0; i < len; i++) {
            if (TimeCodes.yyyymmdd(timeCode.get(i)) == yyyymmdd) {
                return i;
            }
        }
        return len;
    }

    private static int getLastDayIndex(final LongColumn timeCode, final int yyyymmdd) {
        final int len = timeCode.length();
        for (int i = len - 1; i >= 0; i--) {
            if (TimeCodes.yyyymmdd(timeCode.get(i)) == yyyymmdd) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return "[" + optFrom + ";" + optTo + ") => [" + tradeFrom + ";" + tradeTo + ")";
    }
}
