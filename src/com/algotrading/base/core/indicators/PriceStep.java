package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.WindowOfDouble;

/**
 * Вычисление индикатора, оценивающего шаг цены по ценам в окне заданной длины.
 */
public final class PriceStep {

    private static final long MULTIPLIER = 100_000_000L;

    private PriceStep() {
    }

    /**
     * Оценить шаг цены.
     *
     * @param series              временной ряд
     * @param period              период (рекомендуется порядка 20-50)
     * @param priceStepColumnName имя колонки, куда будет записано значение шага цены
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn priceStep(final FinSeries series,
                                         final int period,
                                         final String priceStepColumnName) {
        final int len = series.length();
        final DoubleColumn priceStepColumn = series.acquireDoubleColumn(priceStepColumnName);
        if (len == 0) {
            return priceStepColumn;
        }
        final WindowOfDouble window = new WindowOfDouble(period);
        final DoubleColumn open = series.open();
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        final DoubleColumn last = series.last();
        for (int i = 0; i < len; i++) {
            if (open != null) {
                window.add(open.get(i));
            }
            if (high != null) {
                window.add(high.get(i));
            }
            if (low != null) {
                window.add(low.get(i));
            }
            if (close != null) {
                window.add(close.get(i));
            }
            if (last != null) {
                window.add(last.get(i));
            }
            priceStepColumn.set(i, window.isFull() ? priceStep(window) : Double.NaN);
        }
        for (int i = 0; i < len; i++) {
            final double step = priceStepColumn.get(i);
            if (Double.isFinite(step)) {
                for (int j = 0; j < i; j++) {
                    priceStepColumn.set(j, step);
                }
                break;
            }
        }
        return priceStepColumn;
    }

    private static double priceStep(final WindowOfDouble window) {
        long prev = Math.round(window.get(0) * MULTIPLIER);
        long gcd = -1L;
        for (int i = 1; i < window.size(); i++) {
            final long curr = Math.round(window.get(-i) * MULTIPLIER);
            final long diff = Math.abs(curr - prev);
            if (diff == 0L) continue;
            if (gcd == -1L) {
                gcd = diff;
            } else if (diff > gcd) {
                gcd = gcd(diff, gcd);
            } else {
                gcd = gcd(gcd, diff);
            }
            prev = curr;
        }
        if (gcd <= 0) {
            return Double.NaN;
        } else {
            return (double) gcd / MULTIPLIER;
        }
    }

    /**
     * Предполагая, что m >= n > 0, вычислить НОД(m, n).
     *
     * @param m число
     * @param n число
     * @return НОД(m, n)
     */
    private static long gcd(long m, long n) {
        while (n != 0) {
            final long z = m % n;
            m = n;
            n = z;
        }
        return m;
    }
}
