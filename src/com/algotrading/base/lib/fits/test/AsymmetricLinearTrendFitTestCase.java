package com.algotrading.base.lib.fits.test;

import com.algotrading.base.lib.fits.AsymmetricLinearTrendFit;

/**
 * Тестирование {@link AsymmetricLinearTrendFit}.
 */
public class AsymmetricLinearTrendFitTestCase {

    public static void main(final String[] args) {
        final double[] h = new double[]{
                223.00,
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
        final AsymmetricLinearTrendFit fitH = AsymmetricLinearTrendFit.fit(x, h, 10.0, 1.0);
        // AsymetricLinearTrendFit{y=225.8232067158425-0.06838766471810558*x, u=10.0, v=1.0}
        System.out.println(fitH);
        System.out.println(fitH.a + fitH.b * x[0]);
        System.out.println(fitH.a + fitH.b * x[x.length - 1]);

        final double[] l = new double[]{
                221.90,
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

        final AsymmetricLinearTrendFit fitL = AsymmetricLinearTrendFit.fit(x, l, 1.0, 10.0);
        // AsymmetricLinearTrendFit{y=222.16610831727775+0.013661091234498844*x, u=1.0, v=10.0}
        System.out.println(fitL);
        System.out.println(fitL.a + fitL.b * x[0]);
        System.out.println(fitL.a + fitL.b * x[x.length - 1]);
    }
}
