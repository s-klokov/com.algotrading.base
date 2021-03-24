package com.algotrading.base.lib.linear.test;

import com.algotrading.base.lib.linear.LinModel;

public class LinModelTestCase {

    public static void main(final String[] args) {
        System.out.println("dim: 1");
        // t = 10 + 2 * x
        System.out.println(LinModel.fit(
                new double[]{12, 14, 16},
                true,
                new double[]{1, 2, 3},
                1
        ));

        // t = 10 * x
        System.out.println(LinModel.fit(
                new double[]{10, 15, 20},
                false,
                new double[]{1, 1.5, 2},
                1
        ));

        // t = 10 + 2 * x^2
        System.out.println(LinModel.fit(
                new double[]{12, 18, 28, 42},
                true,
                new double[]{1, 2, 3, 4},
                2
        ));

        // t = 1 + x + x^2
        System.out.println(LinModel.fit(
                new double[]{3, 7, 13, 21},
                true,
                new double[]{1, 2, 3, 4},
                2
        ));

        // t = x - x^2
        System.out.println(LinModel.fit(
                new double[]{0, -2, -6, -12},
                false,
                new double[]{1, 2, 3, 4},
                2
        ));

        // t = x - x^2
        System.out.println(LinModel.fit(
                new double[]{0, -2, -6, -12},
                true,
                new double[]{1, 2, 3, 4},
                2
        ));

        // t = -1 + x + x^2 + x^3
        System.out.println(LinModel.fit(
                new double[]{2, 13, 38, 83, 154},
                true,
                new double[]{1, 2, 3, 4, 5},
                3
        ));

        // Known coefficients.
        {
            final double a = 18.15;
            final double b = -3.41;
            final double c = -2.07;
            final double d = 0.87;
            final int n = 1_000_000;
            final double[] x = new double[n];
            final double[] t = new double[n];
            for (int i = 0; i < n; i++) {
                final double xi = Math.random() * 20 - 10;
                x[i] = xi;
                t[i] = a + b * xi + c * xi * xi + d * xi * xi * xi + (Math.random() * 2 - 1);
            }
            System.out.println(LinModel.fit(t, true, x, 3));
            System.out.println(LinModel.fit(t, false, x, 3));
        }

        System.out.println();
        System.out.println("dim: 2");

        {
            final double c0 = 10.32;
            final double cx = 30.53;
            final double cy = -20.67;
            final int n = 1_000_000;
            final double[] x = new double[n];
            final double[] y = new double[n];
            final double[] t = new double[n];
            for (int i = 0; i < n; i++) {
                final double xi = Math.random() * 20 - 10;
                final double yi = Math.random() * 20 - 10;
                x[i] = xi;
                y[i] = yi;
                t[i] = c0 + cx * xi + cy * yi + (Math.random() * 2 - 1);
            }
            System.out.println(LinModel.fit(t, true, x, y, 1));
            System.out.println(LinModel.fit(t, false, x, y, 1));
        }

        {
            final double c0 = 16.55;
            final double cx = 80.03;
            final double cy = -2.15;
            final double cxx = 1.11;
            final double cyy = -2.22;
            final double cxy = -4.44;
            final int n = 1_000_000;
            final double[] x = new double[n];
            final double[] y = new double[n];
            final double[] t = new double[n];
            for (int i = 0; i < n; i++) {
                final double xi = Math.random() * 20 - 10;
                final double yi = Math.random() * 20 - 10;
                x[i] = xi;
                y[i] = yi;
                t[i] = c0 + cx * xi + cy * yi
                       + cxx * xi * xi + cyy * yi * yi + cxy * xi * yi
                       + (Math.random() * 2 - 1);
            }
            System.out.println(LinModel.fit(t, true, x, y, 2));
            System.out.println(LinModel.fit(t, false, x, y, 2));
        }

        {
            final double c0 = 6.02;
            final double cx = 38.15;
            final double cy = -7.96;
            final double cxx = 1.11;
            final double cyy = -2.22;
            final double cxy = -4.44;
            final double cxxx = 5.55;
            final double cyyy = -3.33;
            final double cxxy = 6.66;
            final double cxyy = -7.77;
            final int n = 1_000_000;
            final double[] x = new double[n];
            final double[] y = new double[n];
            final double[] t = new double[n];
            for (int i = 0; i < n; i++) {
                final double xi = Math.random() * 20 - 10;
                final double yi = Math.random() * 20 - 10;
                x[i] = xi;
                y[i] = yi;
                t[i] = c0 + cx * xi + cy * yi
                       + cxx * xi * xi + cyy * yi * yi + cxy * xi * yi
                       + cxxx * xi * xi * xi + cyyy * yi * yi * yi + cxxy * xi * xi * yi + cxyy * xi * yi * yi
                       + (Math.random() * 2 - 1);
            }
            System.out.println(LinModel.fit(t, true, x, y, 3));
            System.out.println(LinModel.fit(t, false, x, y, 3));
        }

        System.out.println();
        System.out.println("dim: 3");

        {
            final double c0 = 40.32;
            final double cx = 30.53;
            final double cy = -20.67;
            final double cz = 0.95;
            final int n = 1_000_000;
            final double[] x = new double[n];
            final double[] y = new double[n];
            final double[] z = new double[n];
            final double[] t = new double[n];
            for (int i = 0; i < n; i++) {
                final double xi = Math.random() * 20 - 10;
                final double yi = Math.random() * 20 - 10;
                final double zi = Math.random() * 20 - 10;
                x[i] = xi;
                y[i] = yi;
                z[i] = zi;
                t[i] = c0 + cx * xi + cy * yi + cz * zi + (Math.random() * 2 - 1);
            }
            System.out.println(LinModel.fit(t, true, x, y, z, 1));
            System.out.println(LinModel.fit(t, false, x, y, z, 1));
        }

        {
            final double c0 = 55.32;
            final double cx = 3.53;
            final double cy = -2.67;
            final double cz = 9.5;
            final double cxx = 2.22;
            final double cyy = 4.44;
            final double czz = -3.33;
            final double cxy = 0.05;
            final double cxz = 0.06;
            final double cyz = 0.07;

            final int n = 1_000_000;
            final double[] x = new double[n];
            final double[] y = new double[n];
            final double[] z = new double[n];
            final double[] t = new double[n];
            for (int i = 0; i < n; i++) {
                final double xi = Math.random() * 20 - 10;
                final double yi = Math.random() * 20 - 10;
                final double zi = Math.random() * 20 - 10;
                x[i] = xi;
                y[i] = yi;
                z[i] = zi;
                t[i] = c0 + cx * xi + cy * yi + cz * zi
                       + cxx * xi * xi + cyy * yi * yi + czz * zi * zi
                       + cxy * xi * yi + cxz * xi * zi + cyz * yi * zi
                       + (Math.random() * 2 - 1);
            }
            System.out.println(LinModel.fit(t, true, x, y, z, 2));
            System.out.println(LinModel.fit(t, false, x, y, z, 2));
        }

    }
}
