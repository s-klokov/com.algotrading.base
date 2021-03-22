package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.WindowOfDouble;

/**
 * Вычисление индикатора AO (Awesome Oscillator).
 */
public final class AwesomeOscillator {

    public static final int SHORT_PERIOD = 5;
    public static final int LONG_PERIOD = 34;

    public enum Smoothing {
        Simple,
        Exponential,
    }

    private AwesomeOscillator() {
    }

    /**
     * Вычислить индикатор AO (Awesome Oscillator).
     *
     * @param series       временной ряд, содержащий колонки {@link FinSeries#H} и {@link FinSeries#L}
     * @param shortPeriod  длительность короткого периода
     * @param longPeriod   длительность длинного периода
     * @param smoothing    тип скользящей средней
     * @param aoColumnName имя колонки, куда будет записано значение индикатора
     * @return колонка со значениями индикатора.
     */
    public static DoubleColumn ao(final FinSeries series,
                                  final int shortPeriod,
                                  final int longPeriod,
                                  final Smoothing smoothing,
                                  final String aoColumnName) {
        if (shortPeriod < 1 || longPeriod < 1 || shortPeriod >= longPeriod) {
            throw new IllegalArgumentException("shortPeriod:" + shortPeriod + ", longPeriod:" + longPeriod);
        }
        final int len = series.length();
        final DoubleColumn aoColumn = series.acquireDoubleColumn(aoColumnName);
        if (len == 0) {
            return aoColumn;
        }
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        if (smoothing == Smoothing.Simple) {
            final WindowOfDouble window = new WindowOfDouble(longPeriod);
            double shortSum = 0;
            double longSum = 0;
            for (int i = 0; i < len; i++) {
                final int size = window.size();
                if (size >= shortPeriod) {
                    shortSum -= window.get(-shortPeriod + 1);
                }
                if (size >= longPeriod) {
                    longSum -= window.get(-longPeriod + 1);
                }
                final double medianPrice = (high.get(i) + low.get(i)) / 2;
                shortSum += medianPrice;
                longSum += medianPrice;
                window.add(medianPrice);
                if (window.isFull()) {
                    aoColumn.set(i, shortSum / shortPeriod - longSum / longPeriod);
                } else {
                    aoColumn.set(i, 0);
                }
            }
        } else if (smoothing == Smoothing.Exponential) {
            final double medianPrice = (high.get(0) + low.get(0)) / 2;
            double shortEma = medianPrice;
            double longEma = medianPrice;
            aoColumn.set(0, 0);
            final double sm = shortPeriod - 1;
            final double sp = shortPeriod + 1;
            final double lm = longPeriod - 1;
            final double lp = longPeriod + 1;
            for (int i = 1; i < len; i++) {
                final double highPlusLow = high.get(i) + low.get(i);
                shortEma = (shortEma * sm + highPlusLow) / sp;
                longEma = (longEma * lm + highPlusLow) / lp;
            }
        } else {
            throw new IllegalArgumentException("Smoothing:" + smoothing);
        }
        return aoColumn;
    }
}
