package com.algotrading.base.lib.nnets;

/**
 * Сигмоидная функция.
 */
public class SigmoidFunction {

    private static final double B1 = 10.0;
    private static final int N1 = 100000;
    private static final double[] F1 = new double[N1 + 1];
    private static final double B2 = 110.0;
    private static final int N2 = 100000;
    private static final double[] F2 = new double[N2 + 1];
    private static final double M1 = N1 / B1;
    private static final double M2 = N2 / (B2 - B1);

    static {
        for (int i = 0; i <= N1; i++) {
            F1[i] = computeSigmoid(i * B1 / N1);
        }
        for (int i = 0; i <= N2; i++) {
            F2[i] = computeSigmoid(B1 + i * (B2 - B1) / N2);
        }
    }

    /**
     * Вычислить значение сигмоидной функции в данной точке.
     *
     * @param x аргумент.
     * @return значение сигмоида.
     */
    private static double computeSigmoid(final double x) {
        return (x >= 0) ? -0.5 + 1.0 / (1.0 + StrictMath.exp(-x)) : 0.5 - 1.0 / (1.0 + StrictMath.exp(x));
    }

    /**
     * Вычислить значение сигмоидной функции в данной точке, воспользовавшись таблицей.
     *
     * @param x аргумент.
     * @return значение сигмоида.
     */
    public static double f(double x) {
        if (x <= -B2) {
            return -0.5;
        } else if (x <= -B1) {
            x = (-x - B1) * M2;
            final int i = (int) x;
            x -= i;
            return (x - 1.0) * F2[i] - x * F2[i + 1];
        } else if (x < 0) {
            x *= -M1;
            final int i = (int) x;
            x -= i;
            return (x - 1.0) * F1[i] - x * F1[i + 1];
        } else if (x < B1) {
            x *= M1;
            final int i = (int) x;
            x -= i;
            return (1.0 - x) * F1[i] + x * F1[i + 1];
        } else if (x < B2) {
            x = (x - B1) * M2;
            final int i = (int) x;
            x -= i;
            return (1.0 - x) * F2[i] + x * F2[i + 1];
        } else {
            return 0.5;
        }
    }
}
