package com.algotrading.base.lib.fits;

import java.util.Locale;

/**
 * Вычислитель квадратичного тренда y = a + b * x + c * x * x методом наименьших квадратов.
 */
public class QuadraticTrendFit {
    /**
     * Свободный член.
     */
    public final double a;
    /**
     * Коэффициент при x.
     */
    public final double b;
    /**
     * Коэффициент при x * x.
     */
    public final double c;
    /**
     * Средний квадрат ошибки.
     */
    public final double mse;

    private QuadraticTrendFit(final double a, final double b, final double c, final double mse) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.mse = mse;
    }

    /**
     * Вычислить коэффициенты квадратичного тренда по заданным данным (x_i, y_i).
     *
     * @param x массив, содержащий абсциссы точек
     * @param y массив, содержащий ординаты точек
     * @return объект типа QuadraticTrendFit, содержащий коэффициенты при 1, x и x * x, а также средний квадрат ошибки.
     */
    public static QuadraticTrendFit fit(final double[] x, final double[] y) {
        final double n = x.length;
        if (n != y.length) {
            throw new IllegalArgumentException("Data length mismatch: " + x.length + "!=" + y.length);
        }
        double sx = 0;
        double sy = 0;
        for (int i = 0; i < n; i++) {
            sx += x[i];
            sy += y[i];
        }
        final double ax = sx / n;
        final double ay = sy / n;
        double sx2 = 0;
        double sx3 = 0;
        double sx4 = 0;
        double sxy = 0;
        double sx2y = 0;
        for (int i = 0; i < n; i++) {
            final double cx = x[i] - ax;
            final double cx2 = cx * cx;
            sx2 += cx2;
            sx3 += cx * cx2;
            sx4 += cx2 * cx2;
            final double cy = y[i] - ay;
            sxy += cx * cy;
            sx2y += cx2 * cy;
        }
        final double d = -sx2 * sx2 * sx2 - n * sx3 * sx3 + sx4 * n * sx2;
        final double c = (-sxy * n * sx3 + sx2y * n * sx2) / d;
        double b = (-sx2 * sx2 * sxy - sx3 * n * sx2y + sx4 * n * sxy) / d;
        b -= 2 * c * ax;
        double a = -c * sx2 / n;
        a += -b * ax - c * ax * ax + ay;
        double mse = 0;
        for (int i = 0; i < n; i++) {
            final double xi = x[i];
            final double e = y[i] - a - b * xi - c * xi * xi;
            mse += e * e;
        }
        mse /= n;
        return new QuadraticTrendFit(a, b, c, mse);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "QuadraticTrendFit{y=%f%+f*x%+f*x*x, mse=%f}", a, b, c, mse);
    }

    /**
     * Вычислить значение функции по значению аргумента.
     *
     * @param x значение аргумента
     * @return значение функции
     */
    public double value(final double x) {
        return a + b * x + c * x * x;
    }
}
