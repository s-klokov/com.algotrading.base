package com.algotrading.base.lib.linear.test;

import com.algotrading.base.lib.linear.LinAlg;

@SuppressWarnings({"MethodParameterNamingConvention", "LocalVariableNamingConvention"})
public class LinAlgTestCase {

    /**
     * Напечатать матрицу.
     *
     * @param A матрица
     */
    @SuppressWarnings("ImplicitArrayToString")
    private static void dump(final double[][] A) {
        System.out.println(A.length + "*" + A[0].length + "-matrix " + A.toString());
        for (final double[] ai : A) {
            for (final double aij : ai) {
                System.out.print(aij + "  ");
            }
            System.out.println();
        }
    }

    public static void main(final String[] args) {
        double[][] A = LinAlg.scalar(1, 5);
        dump(A);
        LinAlg.invert(A);
        dump(A);
        System.out.println();

        A = LinAlg.diag(new double[]{2, 5});
        dump(A);
        LinAlg.invert(A);
        dump(A);
        System.out.println();

        A = new double[][]{{1, 2}, {3, 4}};
        dump(A);
        LinAlg.invert(A);
        dump(A);
        System.out.println();

        A = new double[][]{{0, 1}, {1, 0}};
        dump(A);
        LinAlg.invert(A);
        dump(A);
        System.out.println();

        A = new double[][]{{5, 2, 3}, {4, 5, 6}, {7, 6, 6}};
        dump(A);
        LinAlg.invert(A);
        dump(A);
        System.out.println();

        A = new double[][]{{1, -1, 1}, {1, 1, 5}, {0, -1, 1}};
        final double[] b = new double[]{0, -2, -5};
        LinAlg.solve(A, b);
        for (int i = 0; i < b.length; i++) {
            System.out.println("x[" + i + "] = " + b[i]);
        }
        System.out.println();

        A = new double[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        dump(A);
        A = LinAlg.insertColumn(A, 1);
        dump(A);
        A = LinAlg.insertRow(A, 1);
        dump(A);
        A = LinAlg.deleteRow(A, 2);
        dump(A);
        A = LinAlg.deleteColumn(A, 2);
        dump(A);

        A[1][1] += 0.01;
        LinAlg.invert(A);
        dump(A);

        A = new double[][]{{1, 2, 3}, {4, 5, 6}};
        final double[][] C = LinAlg.ATA(A);
        dump(C);

        System.out.print("Constructing a big random matrix...");
        final int n = 1000;
        final double[][] B = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                B[i][j] = Math.random();
            }
        }
        System.out.println("  OK.");

        System.out.print("Copying...");
        final double[][] B1 = LinAlg.copy(B);
        System.out.println("  OK.");

        System.out.print("Inverting...");
        LinAlg.invert(B1);
        System.out.println("  OK.");

        System.out.print("Multiplying...");
        A = LinAlg.times(B, B1);
        System.out.println("  OK.");

        System.out.println("product trace = " + LinAlg.trace(A));
        double s = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                s += Math.abs(A[i][j]);
            }
        }
        System.out.println("product sum abs = " + s);
    }
}
