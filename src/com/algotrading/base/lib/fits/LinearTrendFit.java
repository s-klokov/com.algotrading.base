package com.algotrading.base.lib.fits;

/**
 * Вычислитель линейного тренда y = a + b * x методом наименьших квадратов.
 */
public class LinearTrendFit {
    /**
     * Значение свободного члена (intercept).
     */
    public final double a;
    /**
     * Значение наклона (slope).
     */
    public final double b;
    /**
     * Средний квадрат ошибки.
     */
    public final double mse;

    private LinearTrendFit(final double a, final double b, final double mse) {
        this.a = a;
        this.b = b;
        this.mse = mse;
    }

    /**
     * Вычислить коэффициенты линейного тренда по заданным данным (x_i, y_i).
     *
     * @param x массив, содержащий абсциссы точек
     * @param y массив, содержащий ординаты точек
     * @return объект типа LinearTrendFit, содержащий свободный член, наклон линейного тренда и
     * значение среднего квадрата ошибки
     */
    public static LinearTrendFit fit(final double[] x, final double[] y) {
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
        double sy2 = 0;
        double sxy = 0;
        for (int i = 0; i < n; i++) {
            final double cx = x[i] - ax;
            sx2 += cx * cx;
            final double cy = y[i] - ay;
            sy2 += cy * cy;
            sxy += cx * cy;
        }
        final double b = sxy / sx2;
        final double a = ay - b * ax;
        final double mse = (sy2 - sxy * sxy / sx2) / n;
        return new LinearTrendFit(a, b, mse);
    }

    /**
     * Вычислить коэффициенты линейного тренда по заданным данным (x_i, y_i) с положительными весами w_i.
     *
     * @param x массив, содержащий абсциссы точек
     * @param y массив, содержащий ординаты точек
     * @param w массив, содержащий положительные веса точек
     * @return объект типа LinearTrendFit, содержащий свободный член, наклон линейного тренда и
     * значение среднего квадрата ошибки
     */
    public static LinearTrendFit fit(final double[] x, final double[] y, final double[] w) {
        final double n = x.length;
        if (n != y.length) {
            throw new IllegalArgumentException("Data length mismatch: " + x.length + "!=" + y.length);
        }
        if (n != w.length) {
            throw new IllegalArgumentException("Data length mismatch: " + x.length + "!=" + w.length);
        }
        double sw = 0;
        double swx = 0;
        double swy = 0;
        for (int i = 0; i < n; i++) {
            final double wi = w[i];
            sw += wi;
            swx += wi * x[i];
            swy += wi * y[i];
        }
        final double ax = swx / sw;
        final double ay = swy / sw;
        double swx2 = 0;
        double swy2 = 0;
        double swxy = 0;
        for (int i = 0; i < n; i++) {
            final double cx = x[i] - ax;
            final double wi = w[i];
            swx2 += wi * cx * cx;
            final double cy = y[i] - ay;
            swy2 += wi * cy * cy;
            swxy += wi * cx * cy;
        }
        final double b = swxy / swx2;
        final double a = ay - b * ax;
        final double mse = (swy2 - swxy * swxy / swx2) / sw;
        return new LinearTrendFit(a, b, mse);
    }

    @Override
    public String toString() {
        if (b < 0) {
            return "LinearTrendFit{y=" + a + b + "*x, mse=" + mse + "}";
        } else {
            return "LinearTrendFit{y=" + a + "+" + b + "*x, mse=" + mse + "}";
        }

    }

    /**
     * Вычислить значение функции по значению аргумента.
     *
     * @param x значение аргумента
     * @return значение функции
     */
    public double value(final double x) {
        return a + b * x;
    }
}
