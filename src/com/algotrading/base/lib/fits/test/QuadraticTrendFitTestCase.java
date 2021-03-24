package com.algotrading.base.lib.fits.test;

import com.algotrading.base.lib.fits.QuadraticTrendFit;

/**
 * Тестирование {@link QuadraticTrendFit}.
 */
public class QuadraticTrendFitTestCase {

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
        System.out.println(QuadraticTrendFit.fit(x, y));
        // y=86558.166667-176.750000*x+28.674242*x*x, mse=60892.530303
    }
}
