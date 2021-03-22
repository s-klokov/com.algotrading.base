package com.algotrading.base.core;

import com.algotrading.base.core.columns.LongColumn;

public final class DayTradingSession {

    private DayTradingSession() {
    }

    /**
     * Узнать, является ли момент времени первым в торговой сессии своего торгового дня.
     *
     * @param timeCode колонка с моментами времени
     * @param index    индекс
     * @return {@code true} если момент времени является первым в торговой сессии своего торгового дня
     */
    public static boolean isFirstSessionCandle(final LongColumn timeCode, final int index) {
        return index == 0
               || TimeCodes.yyyymmdd(timeCode.get(index - 1)) != TimeCodes.yyyymmdd(timeCode.get(index));
    }

    /**
     * Узнать, является ли момент времени последним в торговой сессии своего торгового дня.
     *
     * @param timeCode колонка с моментами времени
     * @param index    индекс
     * @return {@code true} если момент времени является последним в торговой сессии своего торгового дня
     */
    public static boolean isLastSessionCandle(final LongColumn timeCode, final int index) {
        return index == timeCode.length() - 1
               || TimeCodes.yyyymmdd(timeCode.get(index)) != TimeCodes.yyyymmdd(timeCode.get(index + 1));
    }

    /**
     * Получить индекс первой свечи в торговой сессии.
     *
     * @param timeCode колонка с моментами времени
     * @param index    индекс
     * @return индекс первой свечи в торговой сессии, в которой принадлежит момент времени с указанным индексом
     */
    public static int getFirstSessionCandleIndex(final LongColumn timeCode, final int index) {
        final int yyyymmdd = TimeCodes.yyyymmdd(timeCode.get(index));
        for (int j = index - 1; j >= 0; j--) {
            if (TimeCodes.yyyymmdd(timeCode.get(j)) != yyyymmdd) {
                return j + 1;
            }
        }
        return 0;
    }

    /**
     * Получить индекс последней свечи в торговой сессии.
     *
     * @param timeCode колонка с моментами времени
     * @param index    индекс
     * @return индекс последней свечи в торговой сессии, в которой принадлежит момент времени с указанным индексом
     */
    public static int getLastSessionCandleIndex(final LongColumn timeCode, final int index) {
        final int yyyymmdd = TimeCodes.yyyymmdd(timeCode.get(index));
        for (int j = index + 1; j < timeCode.length(); j++) {
            if (TimeCodes.yyyymmdd(timeCode.get(j)) != yyyymmdd) {
                return j - 1;
            }
        }
        return timeCode.length() - 1;
    }
}
