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

    public static void main(final String[] args) {
        final double[] h = new double[]{223.00,
                                        224.09,
                                        225.75,
                                        226.89,
                                        225.00,
                                        224.83,
                                        224.00,
                                        224.00,
                                        223.89,
                                        224.00,
                                        224.00,
                                        223.53,
                                        223.20,
                                        223.36,
                                        223.84,
                                        223.67,
                                        224.47,
                                        224.27,
                                        224.35,
                                        224.15,
                                        224.69,
                                        224.75,
                                        224.60,
                                        224.40,
                                        223.92,
                                        223.16,
                                        223.10,
                                        222.89,
                                        223.62,
                                        223.93,
                                        223.91,
                                        223.45,
                                        223.28,
                                        223.42,
                                        223.31,
                                        222.97,
                                        222.89};
        final double[] x = new double[h.length];
        for (int i = 0; i < x.length; i++) {
            x[i] = i;
        }
        final AsymmetricLinearTrendFit fitH = fit(x, h, 10.0, 1.0);
        // AsymetricLinearTrendFit{y=225.8232067158425-0.06838766471810558*x, u=10.0, v=1.0}
        System.out.println(fitH);
        System.out.println(fitH.a + fitH.b * x[0]);
        System.out.println(fitH.a + fitH.b * x[x.length - 1]);

        final double[] l = new double[]{221.90,
                                        222.01,
                                        223.85,
                                        224.70,
                                        224.25,
                                        223.44,
                                        223.10,
                                        223.40,
                                        223.34,
                                        223.66,
                                        223.37,
                                        221.54,
                                        221.59,
                                        222.79,
                                        223.07,
                                        222.64,
                                        223.22,
                                        223.95,
                                        223.42,
                                        223.59,
                                        223.79,
                                        224.27,
                                        224.30,
                                        223.00,
                                        223.11,
                                        222.10,
                                        222.30,
                                        222.32,
                                        222.64,
                                        223.50,
                                        223.14,
                                        222.82,
                                        222.91,
                                        223.05,
                                        222.82,
                                        222.71,
                                        222.80};

        final AsymmetricLinearTrendFit fitL = fit(x, l, 1.0, 10.0);
        // AsymmetricLinearTrendFit{y=222.16610831727775+0.013661091234498844*x, u=1.0, v=10.0}
        System.out.println(fitL);
        System.out.println(fitL.a + fitL.b * x[0]);
        System.out.println(fitL.a + fitL.b * x[x.length - 1]);
    }
}
