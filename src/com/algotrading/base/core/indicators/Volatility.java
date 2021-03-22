package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.WindowOfDouble;

/**
 * Вычисление волатильности.
 */
public final class Volatility {

    private Volatility() {
    }

    /**
     * Вычислить волатильность (стандартное отклонение приращений Close / Open - 1).
     *
     * @param series               временной ряд, содержащий колонки {@link FinSeries#O} и {@link FinSeries#C}
     * @param period               период
     * @param k                    коэффициент для приведения к годовому масштабу, если это требуется
     * @param volatilityColumnName имя колонки, куда будет записано значение волатильности
     * @return колонка со значениями волатильности
     */
    public static DoubleColumn volatility(final FinSeries series, final int period, final double k,
                                          final String volatilityColumnName) {
        final WindowOfDouble window = new WindowOfDouble(period);
        double sum = 0;
        double sum2 = 0;
        final DoubleColumn open = series.open();
        final DoubleColumn close = series.close();
        final DoubleColumn volatility = series.acquireDoubleColumn(volatilityColumnName);
        final int len = series.length();
        for (int i = 0; i < len; i++) {
            if (window.isFull()) {
                final double x = window.get(-period + 1);
                sum -= x;
                sum2 -= x * x;
            }
            final double x = close.get(i) / open.get(i) - 1;
            sum += x;
            sum2 += x * x;
            window.add(x);
            final int n = window.size();
            double var = (n <= 1) ? 0 : (sum2 - sum * sum / n) / (n - 1);
            if (var < 0) {
                var = 0;
            }
            volatility.set(i, k * Math.sqrt(var));
        }
        return volatility;
    }
}
