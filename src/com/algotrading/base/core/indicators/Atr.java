package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;

/**
 * Вычисление индикатора ATR.
 */
public final class Atr {

    private Atr() {
    }

    /**
     * Вычислить индикатор ATR.
     *
     * @param series        временной ряд с колонками {@link FinSeries#H}, {@link FinSeries#L}, {@link FinSeries#C}
     * @param period        период
     * @param atrColumnName название колонки, куда будет записано значение индикатора ATR
     * @return колонка со значениями индикатора ATR.
     */
    public static DoubleColumn atr(final FinSeries series, final int period, final String atrColumnName) {
        final int len = series.length();
        final DoubleColumn atrColumn = series.acquireDoubleColumn(atrColumnName);
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        double a = high.get(0) - low.get(0);
        atrColumn.set(0, a);
        for (int i = 1; i < len; i++) {
            final double cPrev = close.get(i - 1);
            final double trueRange = Math.max(high.get(i), cPrev) - Math.min(low.get(i), cPrev);
            if (i < period) {
                a += trueRange;
                atrColumn.set(i, a / (i + 1));
            } else {
                if (i == period) {
                    a /= period;
                }
                a = ((period - 1) * a + trueRange) / period;
                atrColumn.set(i, a);
            }
        }
        return atrColumn;
    }
}
