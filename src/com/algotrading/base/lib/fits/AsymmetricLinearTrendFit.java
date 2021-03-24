package com.algotrading.base.lib.fits;

import com.algotrading.base.lib.optim.NelderMeadMinimizer;
import com.algotrading.base.lib.optim.PointValue;

/**
 * Вычислитель линейного тренда y = a + b * x методом наименьших квадратов в случае,
 * когда штрафы за уклонение выше/ниже линии тренда асимметричны.
 * <p>
 * Оптимизация производится с помощью метода Нелдера-Мида, начальное приближение выбирается методом
 * наименьших квадратов для симметричного штрафа.
 */
public class AsymmetricLinearTrendFit {
    /**
     * Вес штрафа за уклонение выше линии тренда.
     */
    public final double u;
    /**
     * Вес штрафа за уклонение ниже линии тренда.
     */
    public final double v;
    /**
     * Значение свободного члена (intercept).
     */
    public final double a;
    /**
     * Значение наклона (slope).
     */
    public final double b;

    private AsymmetricLinearTrendFit(final double u, final double v, final double a, final double b) {
        this.u = u;
        this.v = v;
        this.a = a;
        this.b = b;
    }

    /**
     * Вычислить коэффициенты линейного тренда по заданным данным (x_i, y_i).
     *
     * @param x массив, содержащий абсциссы точек
     * @param y массив, содержащий ординаты точек
     * @param u вес для учёта штрафа выше линии регрессии
     * @param v вес для учёта штрафа ниже линии регрессии
     * @return объект типа AsymmetricLinearTrendFit, содержащий свободный член и наклон линейного тренда
     */
    public static AsymmetricLinearTrendFit fit(final double[] x, final double[] y,
                                               final double u, final double v) {
        final double n = x.length;
        if (n != y.length) {
            throw new IllegalArgumentException("Data length mismatch: " + x.length + "!=" + y.length);
        }

        final LinearTrendFit initialFit = LinearTrendFit.fit(x, y);

        final PointValue pointValue = new NelderMeadMinimizer()
                .withInitialPoints(new double[][]{
                        {initialFit.a, initialFit.b},
                        {initialFit.a == 0 ? 1 : (0.95 * initialFit.a), initialFit.b},
                        {initialFit.a, initialFit.b == 0 ? 1 : (0.95 * initialFit.b)}
                })
                .minimize(point -> {
                    final double a = point[0];
                    final double b = point[1];
                    double sum = 0;
                    for (int i = 0; i < x.length; i++) {
                        final double diff = y[i] - a - b * x[i];
                        if (diff > 0) {
                            sum += u * diff * diff;
                        } else if (diff < 0) {
                            sum += v * diff * diff;
                        }
                    }
                    return sum;
                });
        return new AsymmetricLinearTrendFit(u, v, pointValue.x(0), pointValue.x(1));
    }

    /**
     * Вычислить коэффициенты линейного тренда по заданным данным (x_i, y_i) с весами w_i.
     *
     * @param x массив, содержащий абсциссы точек
     * @param y массив, содержащий ординаты точек
     * @param w массив, содержащий веса точек
     * @param u вес для учёта штрафа выше линии регрессии
     * @param v вес для учёта штрафа ниже линии регрессии
     * @return объект типа AsymmetricLinearTrendFit, содержащий свободный член и наклон линейного тренда
     */
    public static AsymmetricLinearTrendFit fit(final double[] x, final double[] y, final double[] w,
                                               final double u, final double v) {
        final double n = x.length;
        if (n != y.length) {
            throw new IllegalArgumentException("Data length mismatch: " + x.length + "!=" + y.length);
        }

        final LinearTrendFit initialFit = LinearTrendFit.fit(x, y, w);

        final PointValue pointValue = new NelderMeadMinimizer()
                .withInitialPoints(new double[][]{
                        {initialFit.a, initialFit.b},
                        {initialFit.a == 0 ? 1 : (0.95 * initialFit.a), initialFit.b},
                        {initialFit.a, initialFit.b == 0 ? 1 : (0.95 * initialFit.b)}
                })
                .minimize(point -> {
                    final double a = point[0];
                    final double b = point[1];
                    double sum = 0;
                    for (int i = 0; i < x.length; i++) {
                        final double diff = y[i] - a - b * x[i];
                        if (diff > 0) {
                            sum += u * w[i] * diff * diff;
                        } else if (diff < 0) {
                            sum += v * w[i] * diff * diff;
                        }
                    }
                    return sum;
                });
        return new AsymmetricLinearTrendFit(u, v, pointValue.x(0), pointValue.x(1));
    }

    @Override
    public String toString() {
        if (b < 0) {
            return "AsymetricLinearTrendFit{y=" + a + b + "*x, u=" + u + ", v=" + v + "}";
        } else {
            return "AsymmetricLinearTrendFit{y=" + a + "+" + b + "*x, u=" + u + ", v=" + v + "}";
        }
    }
}
