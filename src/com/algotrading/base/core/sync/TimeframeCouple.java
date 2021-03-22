package com.algotrading.base.core.sync;

import com.algotrading.base.core.columns.LongColumn;

/**
 * Класс, реализующий выделение моментов времени, когда свеча с короткого таймфрейма является последней свечой,
 * составляющей свечу с длинного таймфрейма. При этом предполагается, что свечи длинного таймфрейма получены
 * с помошью сжатия свечей короткого таймфрейма.
 */
public class TimeframeCouple {
    /**
     * Колонка timeCode короткого таймфрейма.
     */
    private final LongColumn shortTimeCode;
    /**
     * Колонка timeCode длинного таймфрейма.
     */
    private final LongColumn longTimeCode;

    /**
     * Конструктор.
     * <p>
     * Предполагается, что свечи длинного таймфрейма получены с помошью сжатия свечей короткого таймфрейма
     * и обе колонки отсортированы по возрастанию.
     *
     * @param shortTimeCode колонка timeCode короткого таймфрейма
     * @param longTimeCode  колонка timeCode длинного таймфрейма
     */
    public TimeframeCouple(final LongColumn shortTimeCode, final LongColumn longTimeCode) {
        this.shortTimeCode = shortTimeCode;
        this.longTimeCode = longTimeCode;
    }

    /**
     * Получить индекс свечи длинного таймфрейма, которая содержит свечу короткого таймфрейма.
     *
     * @param shortId индекс свечи короткого таймфрейма
     * @return индекс свечи длинного таймфрейма или (-1), если обнаружено несоответствие в данных
     */
    public int getLongId(final int shortId) {
        final long tShort = shortTimeCode.get(shortId);
        if (tShort < longTimeCode.get(0)) {
            return -1;
        }
        if (tShort > longTimeCode.getLast()) {
            return longTimeCode.length() - 1;
        }
        int a = 0;
        int b = longTimeCode.length();
        do {
            final int c = (a + b) / 2;
            final long tLong = longTimeCode.get(c);
            if (tLong < tShort) {
                a = c;
            } else if (tLong > tShort) {
                b = c;
            } else {
                return c;
            }
        } while (b - a > 1);
        return a;
    }

    /**
     * Проверить, является ли свеча короткого таймфрейма последней свечой, составляющей свечу длинного таймфрейма
     * с данным индексом. Этот метод более эффективен, чем метод {@link #isLastCandle(int)}, если индекс свечи
     * длинного таймфрейма известен.
     *
     * @param shortId индекс свечи короткого таймфрейма
     * @param longId  индекс свечи длинного таймфрейма
     * @return {@code true} или {@code false}
     */
    public boolean isLastCandle(final int shortId, final int longId) {
        return longId >= 0 && (shortId == shortTimeCode.length() - 1 || getLongId(shortId + 1) > longId);
    }

    /**
     * Проверить, является ли свеча короткого таймфрейма последней свечой, составляющей свечу длинного таймфрейма
     * с данным индексом.
     *
     * @param shortId индекс свечи короткого таймфрейма
     * @return {@code true} или {@code false}
     */
    public boolean isLastCandle(final int shortId) {
        return isLastCandle(shortId, getLongId(shortId));
    }
}
