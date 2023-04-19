package com.algotrading.base.lib.linear.test;

import com.algotrading.base.lib.linear.LinRegression;

public class LinRegressionTestCase {

    public static void main(final String[] args) {
        final double[] targets = new double[]{
                -2.54,
                26.50,
                4.44,
                17.12,
                10.19,
                13.88,
                4.55,
                10.28,
                11.76,
                11.89,
                5.14,
                7.70,
                7.17,
                7.57,
                17.46,
        };
        final double[][] predictors = new double[][]{
                {1, -5.31, -2.07},
                {1, 16.84, 19.34},
                {1, 0.07, 3.63},
                {1, 10.03, 13.28},
                {1, 4.98, 7.68},
                {1, 7.52, 10.51},
                {1, 0.23, 3.68},
                {1, 5.53, 8.78},
                {1, 5.94, 8.67},
                {1, 6.09, 8.60},
                {1, 0.93, 3.74},
                {1, 3.22, 6.25},
                {1, 2.08, 5.67},
                {1, 2.81, 5.84},
                {1, 10.73, 13.54},
        };

        final LinRegression linRegression = LinRegression.fit(targets, predictors, true);

        /* Коэффициенты модели */
        // 3.330977582399911
        // 1.0880428512350082
        // 0.21464524721669678

        /* Коэффициент детерминации */
        // R2=0.9965942259753863

        for (final double b : linRegression.cf) {
            System.out.println(b);
        }
        System.out.println("R2=" + linRegression.r2);
    }
}
