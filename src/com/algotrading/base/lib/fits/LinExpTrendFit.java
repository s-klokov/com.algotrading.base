package com.algotrading.base.lib.fits;

import com.algotrading.base.lib.optim.NelderMeadMinimizer;
import com.algotrading.base.lib.optim.PointValue;

import java.util.Locale;

/**
 * Вычислитель нелинейного тренда y = a + b * (x - xAvg) + c * exp(d * (x - xAvg)) методом наименьших квадратов.
 */
public class LinExpTrendFit {
    /**
     * Значение xAvg.
     */
    public final double xAvg;
    /**
     * Свободный член.
     */
    public final double a;
    /**
     * Коэффициент при x.
     */
    public final double b;
    /**
     * Коэффициент перед экспонентой.
     */
    public final double c;
    /**
     * Коэффициент в показателе экспоненты.
     */
    public final double d;
    /**
     * Средний квадрат ошибки.
     */
    public final double mse;

    private LinExpTrendFit(final double xAvg,
                           final double a, final double b, final double c, final double d,
                           final double mse) {
        this.xAvg = xAvg;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.mse = mse;
    }

    /**
     * Вычислить коэффициенты квадратичного тренда по заданным данным (x_i, y_i).
     *
     * @param x массив, содержащий абсциссы точек
     * @param y массив, содержащий ординаты точек
     * @return объект типа LinExpTrendFit
     */
    public static LinExpTrendFit fit(final double[] x, final double[] y, final int maxIterations) {
        final double n = x.length;
        if (n != y.length) {
            throw new IllegalArgumentException("Data length mismatch: " + x.length + "!=" + y.length);
        }

        double xMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double xSum = 0;
        for (final double v : x) {
            xMin = Math.min(xMin, v);
            xMax = Math.max(xMax, v);
            xSum += v;
        }
        final double xAvg = xSum / x.length;
        final double xBound = 1.0 + Math.abs(xMin) + Math.abs(xMax);

        double yAvg = 0;
        for (final double v : y) {
            yAvg += v;
        }
        yAvg /= y.length;

        final double yBound = 1.0 + Math.abs(yAvg);
        final PointValue pointValue = new NelderMeadMinimizer()
                .withInitialPoints(new double[][]{
                        new double[]{0, 0, 0, 0},
                        new double[]{yBound, 0, 0, 0},
                        new double[]{0, yBound / xBound, 0, 0},
                        new double[]{0, 0, yBound, 1.0 / (xMax - xAvg)},
                        new double[]{0, 0, yBound, -1.0 / (xAvg - xMin)}
                })
                .withMaxIterations(maxIterations).minimize(point -> {
                    final double a = point[0];
                    final double b = point[1];
                    final double c = point[2];
                    final double d = point[3];
                    double sum = 0;
                    for (int i = 0; i < x.length; i++) {
                        final double cx = x[i] - xAvg;
                        final double deviation = y[i] - a - b * cx - c * Math.exp(d * cx);
                        sum += deviation * deviation;
                    }
                    return sum / x.length;
                });
        final double[] point = pointValue.x;
        return new LinExpTrendFit(xAvg, point[0], point[1], point[2], point[3], pointValue.value);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "LinExpTrendFit{y=%f%+f*(x-x_avg)%+f*exp(%+f*(x-x_avg)), xAvg=%f, mse=%f}",
                a, b, c, d, xAvg, mse);
    }

    /**
     * Вычислить значение функции по значению аргумента.
     *
     * @param x значение аргумента
     * @return значение функции
     */
    public double value(final double x) {
        return a + b * (x - xAvg) + c * Math.exp(d * (x - xAvg));
    }
}
