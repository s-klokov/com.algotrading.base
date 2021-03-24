package com.algotrading.base.lib.fits.test;

import com.algotrading.base.lib.fits.LinearTrendFit;

/**
 * Тестирование {@link LinearTrendFit}.
 */
public class LinearTrendFitTestCase {

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
        System.out.println(LinearTrendFit.fit(x, y));
        // y=85927.33333333333+138.66666666666666*x, mse=104305.33333333333

        final double[] xx = new double[]{1, 3, 4};
        final double[] yy = new double[]{10, 11, 12};
        final double[] ww = new double[]{1.0, 4.0, 3.0};
        System.out.println(LinearTrendFit.fit(xx, yy, ww));
        // LinearTrendFit{y=9.09090909090909+0.6909090909090909*x, mse=0.027272727272727282}
        final double[] xxx = new double[]{1, 3, 3, 3, 3, 4, 4, 4};
        final double[] yyy = new double[]{10, 11, 11, 11, 11, 12, 12, 12};
        System.out.println(LinearTrendFit.fit(xxx, yyy));
        // LinearTrendFit{y=9.09090909090909+0.6909090909090909*x, mse=0.027272727272727282}
    }
}
