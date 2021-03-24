package com.algotrading.base.lib.fits.test;

import com.algotrading.base.lib.fits.LinExpTrendFit;

/**
 * Тестирование {@link LinExpTrendFit}.
 */
public class LinExpTrendFitTestCase {

    public static void main(final String[] args) {
        final double[] x = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final double[] y = new double[]{
                86390,
                86300,
                86300,
                86160,
                86600,
                86710,
                86780,
                86870,
                86720,
                88070};
        System.out.println(LinExpTrendFit.fit(x, y, 1000));
        // LinExpTrendFit{y=32766.272669+1877.701243*(x-x_avg)+53691.754103*exp(-0.032302*(x-x_avg)), xAvg=5.500000, mse=61785.226249}

        final double[] xx = new double[]{1, 2, 3, 4, 5, 6, 7};
        double xSum = 0;
        for (final double v : xx) {
            xSum += v;
        }
        final double xAvg = xSum / xx.length;
        final double[] yy = new double[xx.length];
        for (int i = 0; i < xx.length; i++) {
            yy[i] = 5 + 10 * (xx[i] - xAvg) + 3 * Math.exp(2 * (xx[i] - xAvg));
        }
        System.out.println(LinExpTrendFit.fit(xx, yy, 1000));
        // LinExpTrendFit{y=5.000000+10.000000*(x-x_avg)+3.000000*exp(+2.000000*(x-x_avg)), xAvg=4.000000, mse=0.000000}
    }
}
