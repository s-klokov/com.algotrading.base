package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.lib.fits.QuadraticTrendFit;

/**
 * Вычисление двумерного индикатора квадратичной регрессии по OHLC-данным.
 * <p>
 * В окно заданной длины вписывается парабола и вычисляются соответствующие значения:<br>
 * 1) коэффициент скорости = угловой коэффициент касательной в середине окна, умноженный на длину окна
 * и делённый на среднеквадратическое уклонение;<br>
 * 2) коэффициент ускорения = разность угловых коэффициентов касательных в конце и начале окна,
 * умноженная на длину окна и делённая на среднеквадратическое уклонение.
 * <p>
 * Коэффициенты являются безразмерными величинами.
 */
public class QuadraticRegressionIndicator {

    private QuadraticRegressionIndicator() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param series                 временной ряд с колонками O, H, L, C
     * @param period                 период индикатора
     * @param velocityColumnName     имя колонки со значениями коэффициента скорости
     * @param accelerationColumnName имя колонки со значениями коэффициента ускорения
     */
    public static void estimate(final FinSeries series,
                                final int period,
                                final String velocityColumnName,
                                final String accelerationColumnName) {
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();

        final DoubleColumn velocityColumn = series.acquireDoubleColumn(velocityColumnName);
        final DoubleColumn accelerationColumn = series.acquireDoubleColumn(accelerationColumnName);

        final double[] x = new double[3 * period];
        final double[] y = new double[3 * period];
        for (int i = 0, k = -x.length + 1; i < x.length; i++, k++) {
            x[i] = k;
        }
        final double length = x.length - 1;
        final double middle = 0.5 * (-x.length + 1);

        for (int i = 0; i < close.length(); i++) {
            if (i < period - 1) {
                velocityColumn.set(i, Double.NaN);
                accelerationColumn.set(i, Double.NaN);
                continue;
            }
            for (int j = i - period + 1, k = 0; j <= i; j++) {
                final double cPrev = (j == 0) ? series.open().get(0) : close.get(j - 1);
                final double h = high.get(j);
                final double l = low.get(j);
                final double c = close.get(j);
                if (c >= cPrev) {
                    y[k++] = l;
                    y[k++] = h;
                } else {
                    y[k++] = h;
                    y[k++] = l;
                }
                y[k++] = c;
            }
            final QuadraticTrendFit fit = QuadraticTrendFit.fit(x, y);
            final double velocity = (fit.b + 2 * fit.c * middle) * length / Math.sqrt(fit.mse);
            final double acceleration = 2 * fit.c * length * length / Math.sqrt(fit.mse);
            velocityColumn.set(i, velocity);
            accelerationColumn.set(i, acceleration);
        }
    }
}
