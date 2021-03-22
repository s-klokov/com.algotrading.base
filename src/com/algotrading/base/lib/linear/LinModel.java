package com.algotrading.base.lib.linear;

import java.util.Locale;

/**
 * Класс для расчёта линейных моделей.
 */
public class LinModel {
    /**
     * Признак наличия свободного члена.
     */
    public final boolean hasIntercept;
    /**
     * Степень аппроксимации:
     * 1 - линейные члены;
     * 2 - линейные и квадратические члены;
     * 3 - линейные, квадратические и кубические члены.
     */
    public final int degree;
    /**
     * Размерность пространства предикторов (от 1 до 3).
     */
    public final int dim;
    /**
     * Коэффициенты модели.
     * Нулевой индекс используется для свободного члена; если в модели его нет, то там записан нуль.
     * Количество и смысл остальных коэффициентов модели зависит от степени аппроксимации
     * и количества предикторов.
     */
    public final double[] cf;
    /**
     * Средний квадрат ошибки.
     */
    private double mse = 0;

    private LinModel(final boolean hasIntercept, final int degree, final int dim, final double[] cf) {
        this.hasIntercept = hasIntercept;
        this.degree = degree;
        this.dim = dim;
        this.cf = cf;
    }

    /**
     * @return средний квадрат ошибки модели
     */
    public double mse() {
        return mse;
    }

    /**
     * Рассчитать модель с одним предиктором.
     *
     * @param t            массив целевых значений
     * @param hasIntercept надо ли включить в модель свободный член
     * @param x            массив значений предиктора
     * @param degree       степень аппроксимации
     * @return модель
     */
    public static LinModel fit(final double[] t, final boolean hasIntercept,
                               final double[] x, final int degree) {
        if (degree < 1 || degree > 3) {
            throw new IllegalArgumentException("Invalid degree: " + degree);
        }
        if (t.length != x.length) {
            throw new IllegalArgumentException("Data size mismatch");
        }
        final LinModel linModel;
        switch (degree) {
            case 1:
                linModel = new LinModel(hasIntercept, 1, 1, fit1(hasIntercept, t, x));
                break;
            case 2:
                linModel = new LinModel(hasIntercept, 2, 1, fit2(hasIntercept, t, x));
                break;
            case 3:
                linModel = new LinModel(hasIntercept, 3, 1, fit3(hasIntercept, t, x));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        linModel.computeMse(t, x);
        return linModel;
    }

    /**
     * Рассчитать модель с двумя предикторами.
     *
     * @param t            массив целевых значений
     * @param hasIntercept надо ли включить в модель свободный член
     * @param x            массив значений первого предиктора
     * @param y            массив значений второго предиктора
     * @param degree       степень аппроксимации
     * @return модель
     */
    public static LinModel fit(final double[] t, final boolean hasIntercept,
                               final double[] x, final double[] y, final int degree) {
        if (degree < 1 || degree > 3) {
            throw new IllegalArgumentException("Invalid degree: " + degree);
        }
        if (t.length != x.length || t.length != y.length) {
            throw new IllegalArgumentException("Data size mismatch");
        }
        final LinModel linModel;
        switch (degree) {
            case 1:
                linModel = new LinModel(hasIntercept, 1, 2, fit1(hasIntercept, t, x, y));
                break;
            case 2:
                linModel = new LinModel(hasIntercept, 2, 2, fit2(hasIntercept, t, x, y));
                break;
            case 3:
                linModel = new LinModel(hasIntercept, 3, 2, fit3(hasIntercept, t, x, y));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        linModel.computeMse(t, x, y);
        return linModel;
    }

    /**
     * Рассчитать модель с тремя предикторами.
     *
     * @param t            массив целевых значений
     * @param hasIntercept надо ли включить в модель свободный член
     * @param x            массив значений первого предиктора
     * @param y            массив значений второго предиктора
     * @param z            массив значений третьего предиктора
     * @param degree       степень аппроксимации
     * @return модель
     */
    public static LinModel fit(final double[] t, final boolean hasIntercept,
                               final double[] x, final double[] y, final double[] z, final int degree) {
        if (degree < 1 || degree > 3) {
            throw new IllegalArgumentException("Invalid degree: " + degree);
        }
        if (t.length != x.length || t.length != y.length) {
            throw new IllegalArgumentException("Data size mismatch");
        }
        final LinModel linModel;
        switch (degree) {
            case 1:
                linModel = new LinModel(hasIntercept, 1, 3, fit1(hasIntercept, t, x, y, z));
                break;
            case 2:
                linModel = new LinModel(hasIntercept, 2, 3, fit2(hasIntercept, t, x, y, z));
                break;
            case 3:
                // TODO:
            default:
                throw new UnsupportedOperationException();
        }
        linModel.computeMse(t, x, y, z);
        return linModel;
    }

    /**
     * Получить аппроксимацию целевого значения с помощью модели.
     *
     * @param x значение предиктора
     * @return предсказываемое моделью значение
     */
    public double predict(final double x) {
        if (dim != 1) {
            throw new IllegalArgumentException("Dimension mismatch. Model dimension = " + dim);
        }
        double sum = cf[0] + cf[1] * x;
        if (degree == 1) {
            return sum;
        }
        final double xx = x * x;
        sum += cf[2] * xx;
        if (degree == 2) {
            return sum;
        }
        sum += cf[3] * xx * x;
        return sum;
    }

    /**
     * Получить аппроксимацию целевого значения с помощью модели.
     *
     * @param x значение первого предиктора
     * @param y значение второго предиктора
     * @return предсказываемое моделью значение
     */
    public double predict(final double x, final double y) {
        if (dim != 2) {
            throw new IllegalArgumentException("Dimension mismatch. Model dimension = " + dim);
        }
        double sum = cf[0] + cf[1] * x + cf[2] * y;
        if (degree == 1) {
            return sum;
        }
        final double xx = x * x;
        final double yy = y * y;
        sum += cf[3] * xx + cf[4] * yy + cf[5] * x * y;
        if (degree == 2) {
            return sum;
        }
        sum += cf[6] * xx * x + cf[7] * yy * y + cf[8] * xx * y + cf[9] * x * yy;
        return sum;
    }

    /**
     * Получить аппроксимацию целевого значения с помощью модели.
     *
     * @param x значение первого предиктора
     * @param y значение второго предиктора
     * @param z значение третьего предиктора
     * @return предсказываемое моделью значение
     */
    public double predict(final double x, final double y, final double z) {
        if (dim != 3) {
            throw new IllegalArgumentException("Dimension mismatch. Model dimension = " + dim);
        }
        double sum = cf[0] + cf[1] * x + cf[2] * y + cf[3] * z;
        if (degree == 1) {
            return sum;
        }
        final double xx = x * x;
        final double yy = y * y;
        final double zz = z * z;
        sum += cf[4] * xx + cf[5] * yy + cf[6] * zz;
        sum += cf[7] * x * y + cf[8] * x * z + cf[9] * y * z;
        if (degree == 2) {
            return sum;
        }
        sum += cf[10] * xx * x + cf[11] * yy * y + cf[12] * zz * z;
        sum += cf[13] * xx * y + cf[14] * x * yy;
        sum += cf[15] * xx * z + cf[16] * x * zz;
        sum += cf[17] * yy * z + cf[18] * y * zz;
        sum += cf[19] * x * y * z;
        return sum;
    }

    private void computeMse(final double[] t, final double[] x) {
        double sum = 0;
        for (int i = 0; i < t.length; i++) {
            final double d = t[i] - predict(x[i]);
            sum += d * d;
        }
        mse = sum / t.length;
    }

    private void computeMse(final double[] t, final double[] x, final double[] y) {
        double sum = 0;
        for (int i = 0; i < t.length; i++) {
            final double d = t[i] - predict(x[i], y[i]);
            sum += d * d;
        }
        mse = sum / t.length;
    }

    private void computeMse(final double[] t, final double[] x, final double[] y, final double[] z) {
        double sum = 0;
        for (int i = 0; i < t.length; i++) {
            final double d = t[i] - predict(x[i], y[i], z[i]);
            sum += d * d;
        }
        mse = sum / t.length;
    }

    private static void ensureNoInterceptCase(final boolean hasIntercept, final double[][] a, final double[] b) {
        if (hasIntercept) {
            return;
        }
        a[0][0] = 1.0;
        for (int i = 1; i < a.length; i++) {
            a[0][i] = 0;
            a[i][0] = 0;
        }
        b[0] = 0;
    }

    private static double[] fit1(final boolean hasIntercept, final double[] t, final double[] x) {
        if (hasIntercept) {
            double sx = 0;
            double st = 0;
            double sxx = 0;
            double stx = 0;
            for (int i = 0; i < t.length; i++) {
                final double ti = t[i];
                final double xi = x[i];
                sx += xi;
                st += ti;
                sxx += xi * xi;
                stx += ti * xi;
            }
            final double[][] a = new double[][]{
                    {t.length, sx},
                    {sx, sxx}
            };
            final double[] b = new double[]{st, stx};
            LinAlg.solve(a, b);
            return b;
        } else {
            double sxx = 0;
            double stx = 0;
            for (int i = 0; i < t.length; i++) {
                final double ti = t[i];
                final double xi = x[i];
                sxx += xi * xi;
                stx += ti * xi;
            }
            return new double[]{0, stx / sxx};
        }
    }

    private static double[] fit2(final boolean hasIntercept, final double[] t, final double[] x) {
        double sx = 0;
        double sx2 = 0;
        double sx3 = 0;
        double sx4 = 0;
        double st = 0;
        double stx = 0;
        double stx2 = 0;
        for (int i = 0; i < t.length; i++) {
            final double xi = x[i];
            sx += xi;
            final double xxi = xi * xi;
            sx2 += xxi;
            sx3 += xxi * xi;
            sx4 += xxi * xxi;
            final double ti = t[i];
            st += ti;
            stx += ti * xi;
            stx2 += ti * xxi;
        }
        final double[][] a = new double[][]{
                {t.length, sx, sx2},
                {sx, sx2, sx3},
                {sx2, sx3, sx4}
        };
        final double[] b = new double[]{st, stx, stx2};
        ensureNoInterceptCase(hasIntercept, a, b);
        LinAlg.solve(a, b);
        return b;
    }

    private static double[] fit3(final boolean hasIntercept, final double[] t, final double[] x) {
        double sx = 0;
        double sx2 = 0;
        double sx3 = 0;
        double sx4 = 0;
        double sx5 = 0;
        double sx6 = 0;
        double st = 0;
        double stx = 0;
        double stx2 = 0;
        double stx3 = 0;
        for (int i = 0; i < t.length; i++) {
            final double xi = x[i];
            sx += xi;
            final double xxi = xi * xi;
            sx2 += xxi;
            final double xxxi = xxi * xi;
            sx3 += xxxi;
            final double xxxxi = xxi * xxi;
            sx4 += xxxxi;
            sx5 += xxxxi * xi;
            sx6 += xxxxi * xxi;
            final double ti = t[i];
            st += ti;
            stx += ti * xi;
            stx2 += ti * xxi;
            stx3 += ti * xxxi;
        }
        final double[][] a = new double[][]{
                {t.length, sx, sx2, sx3},
                {sx, sx2, sx3, sx4},
                {sx2, sx3, sx4, sx5},
                {sx3, sx4, sx5, sx6}
        };
        final double[] b = new double[]{st, stx, stx2, stx3};
        ensureNoInterceptCase(hasIntercept, a, b);
        LinAlg.solve(a, b);
        return b;
    }

    private static double[] fit1(final boolean hasIntercept, final double[] t, final double[] x, final double[] y) {
        double sx = 0;
        double sy = 0;
        double st = 0;
        double sxx = 0;
        double syy = 0;
        double sxy = 0;
        double stx = 0;
        double sty = 0;
        for (int i = 0; i < t.length; i++) {
            final double ti = t[i];
            final double xi = x[i];
            final double yi = y[i];
            sx += xi;
            sy += yi;
            st += ti;
            sxx += xi * xi;
            syy += yi * yi;
            sxy += xi * yi;
            stx += ti * xi;
            sty += ti * yi;
        }
        final double[][] a = new double[][]{
                {t.length, sx, sy},
                {sx, sxx, sxy},
                {sy, sxy, syy}
        };
        final double[] b = new double[]{st, stx, sty};
        ensureNoInterceptCase(hasIntercept, a, b);
        LinAlg.solve(a, b);
        return b;
    }

    private static double[] fit2(final boolean hasIntercept, final double[] t, final double[] x, final double[] y) {
        double sx = 0;
        double sy = 0;

        double sxx = 0;
        double sxy = 0;
        double syy = 0;

        double sxxx = 0;
        double syyy = 0;
        double sxxy = 0;
        double sxyy = 0;

        double sxxxx = 0;
        double sxxxy = 0;
        double sxxyy = 0;
        double sxyyy = 0;
        double syyyy = 0;

        double st = 0;
        double stx = 0;
        double sty = 0;
        double stxx = 0;
        double styy = 0;
        double stxy = 0;
        for (int i = 0; i < t.length; i++) {
            final double ti = t[i];
            final double xi = x[i];
            final double xxi = xi * xi;
            final double yi = y[i];
            final double yyi = yi * yi;
            final double xyi = xi * yi;
            sx += xi;
            sy += yi;
            st += ti;
            sxx += xxi;
            syy += yyi;
            sxy += xyi;
            sxxx += xxi * xi;
            syyy += yyi * yi;
            sxxy += xxi * yi;
            sxyy += xi * yyi;
            sxxxx += xxi * xxi;
            syyyy += yyi * yyi;
            sxxxy += xxi * xyi;
            sxxyy += xxi * yyi;
            sxyyy += xyi * yyi;
            stx += ti * xi;
            sty += ti * yi;
            stxx += ti * xxi;
            styy += ti * yyi;
            stxy += ti * xyi;
        }
        final double[][] a = new double[][]{
                {t.length, sx, sy, sxx, syy, sxy},
                {sx, sxx, sxy, sxxx, sxyy, sxxy},
                {sy, sxy, syy, sxxy, syyy, sxyy},
                {sxx, sxxx, sxxy, sxxxx, sxxyy, sxxxy},
                {syy, sxyy, syyy, sxxyy, syyyy, sxyyy},
                {sxy, sxxy, sxyy, sxxxy, sxyyy, sxxyy}
        };
        final double[] b = new double[]{st, stx, sty, stxx, styy, stxy};
        ensureNoInterceptCase(hasIntercept, a, b);
        LinAlg.solve(a, b);
        return b;
    }

    private static double[] fit3(final boolean hasIntercept, final double[] t, final double[] x, final double[] y) {
        double sx = 0;
        double sy = 0;

        double sxx = 0;
        double syy = 0;
        double sxy = 0;

        double sxxx = 0;
        double syyy = 0;
        double sxxy = 0;
        double sxyy = 0;

        double sxxxx = 0;
        double sxxxy = 0;
        double sxxyy = 0;
        double sxyyy = 0;
        double syyyy = 0;

        double sxxxxx = 0;
        double sxxxxy = 0;
        double sxxxyy = 0;
        double sxxyyy = 0;
        double sxyyyy = 0;
        double syyyyy = 0;

        double sxxxxxx = 0;
        double sxxxxxy = 0;
        double sxxxxyy = 0;
        double sxxxyyy = 0;
        double sxxyyyy = 0;
        double sxyyyyy = 0;
        double syyyyyy = 0;

        double st = 0;
        double stx = 0;
        double sty = 0;
        double stxx = 0;
        double styy = 0;
        double stxy = 0;
        double stxxx = 0;
        double styyy = 0;
        double stxxy = 0;
        double stxyy = 0;

        for (int i = 0; i < t.length; i++) {
            final double ti = t[i];
            final double xi = x[i];
            final double xxi = xi * xi;
            final double xxxi = xxi * xi;
            final double yi = y[i];
            final double yyi = yi * yi;
            final double yyyi = yyi * yi;
            final double xyi = xi * yi;

            sx += xi;
            sy += yi;

            sxx += xxi;
            syy += yyi;
            sxy += xyi;

            sxxx += xxi * xi;
            syyy += yyi * yi;
            sxxy += xxi * yi;
            sxyy += xi * yyi;

            sxxxx += xxi * xxi;
            syyyy += yyi * yyi;
            sxxxy += xxi * xyi;
            sxxyy += xxi * yyi;
            sxyyy += xyi * yyi;

            sxxxxx += xxxi * xxi;
            sxxxxy += xxi * xxi * yi;
            sxxxyy += xxxi * yyi;
            sxxyyy += xxi * yyyi;
            sxyyyy += xi * yyi * yyi;
            syyyyy += yyyi * yyi;

            sxxxxxx += xxxi * xxxi;
            sxxxxxy += xxxi * xxi * yi;
            sxxxxyy += xxi * xxi * yyi;
            sxxxyyy += xxxi * yyyi;
            sxxyyyy += xxi * yyi * yyi;
            sxyyyyy += xi * yyyi * yyi;
            syyyyyy += yyyi * yyyi;

            st += ti;
            stx += ti * xi;
            sty += ti * yi;
            stxx += ti * xxi;
            styy += ti * yyi;
            stxy += ti * xyi;
            stxxx += ti * xxxi;
            styyy += ti * yyyi;
            stxxy += ti * xxi * yi;
            stxyy += ti * xi * yyi;
        }
        final double[][] a = new double[][]{
                {t.length, sx, sy, sxx, syy, sxy, sxxx, syyy, sxxy, sxyy},
                {sx, sxx, sxy, sxxx, sxyy, sxxy, sxxxx, sxyyy, sxxxy, sxxyy},
                {sy, sxy, syy, sxxy, syyy, sxyy, sxxxy, syyyy, sxxyy, sxyyy},
                {sxx, sxxx, sxxy, sxxxx, sxxyy, sxxxy, sxxxxx, sxxyyy, sxxxxy, sxxxyy},
                {syy, sxyy, syyy, sxxyy, syyyy, sxyyy, sxxxyy, syyyyy, sxxyyy, sxyyyy},
                {sxy, sxxy, sxyy, sxxxy, sxyyy, sxxyy, sxxxxy, sxyyyy, sxxxyy, sxxyyy},
                {sxxx, sxxxx, sxxxy, sxxxxx, sxxxyy, sxxxxy, sxxxxxx, sxxxyyy, sxxxxxy, sxxxxyy},
                {syyy, sxyyy, syyyy, sxxyyy, syyyyy, sxyyyy, sxxxyyy, syyyyyy, sxxyyyy, sxyyyyy},
                {sxxy, sxxxy, sxxyy, sxxxxy, sxxyyy, sxxxyy, sxxxxxy, sxxyyyy, sxxxxyy, sxxxyyy},
                {sxyy, sxxyy, sxyyy, sxxxyy, sxyyyy, sxxyyy, sxxxxyy, sxyyyyy, sxxxyyy, sxxyyyy}
        };
        final double[] b = new double[]{st, stx, sty, stxx, styy, stxy, stxxx, styyy, stxxy, stxyy};
        ensureNoInterceptCase(hasIntercept, a, b);
        LinAlg.solve(a, b);
        return b;
    }

    private static double[] fit1(final boolean hasIntercept, final double[] t,
                                 final double[] x, final double[] y, final double[] z) {
        double sx = 0;
        double sy = 0;
        double sz = 0;

        double sxx = 0;
        double syy = 0;
        double szz = 0;
        double sxy = 0;
        double sxz = 0;
        double syz = 0;

        double st = 0;
        double stx = 0;
        double sty = 0;
        double stz = 0;

        for (int i = 0; i < t.length; i++) {
            final double ti = t[i];
            final double xi = x[i];
            final double yi = y[i];
            final double zi = z[i];

            sx += xi;
            sy += yi;
            sz += zi;

            sxx += xi * xi;
            syy += yi * yi;
            szz += zi * zi;
            sxy += xi * yi;
            sxz += xi * zi;
            syz += yi * zi;

            st += ti;
            stx += ti * xi;
            sty += ti * yi;
            stz += ti * zi;
        }
        final double[][] a = new double[][]{
                {t.length, sx, sy, sz},
                {sx, sxx, sxy, sxz},
                {sy, sxy, syy, syz},
                {sz, sxz, syz, szz}
        };
        final double[] b = new double[]{st, stx, sty, stz};
        ensureNoInterceptCase(hasIntercept, a, b);
        LinAlg.solve(a, b);
        return b;
    }

    private static double[] fit2(final boolean hasIntercept, final double[] t,
                                 final double[] x, final double[] y, final double[] z) {
        double sx = 0;
        double sy = 0;
        double sz = 0;

        double sxx = 0;
        double syy = 0;
        double szz = 0;
        double sxy = 0;
        double sxz = 0;
        double syz = 0;

        double sxxx = 0;
        double syyy = 0;
        double szzz = 0;
        double sxxy = 0;
        double sxyy = 0;
        double sxxz = 0;
        double sxzz = 0;
        double syyz = 0;
        double syzz = 0;
        double sxyz = 0;

        double sxxxx = 0;
        double syyyy = 0;
        double szzzz = 0;
        double sxxxy = 0;
        double sxxyy = 0;
        double sxyyy = 0;
        double sxxxz = 0;
        double sxxzz = 0;
        double sxzzz = 0;
        double syyyz = 0;
        double syyzz = 0;
        double syzzz = 0;
        double sxxyz = 0;
        double sxyyz = 0;
        double sxyzz = 0;

        double st = 0;
        double stx = 0;
        double sty = 0;
        double stz = 0;
        double stxx = 0;
        double styy = 0;
        double stzz = 0;
        double stxy = 0;
        double stxz = 0;
        double styz = 0;

        for (int i = 0; i < t.length; i++) {
            final double ti = t[i];
            final double xi = x[i];
            final double xxi = xi * xi;
            final double yi = y[i];
            final double yyi = yi * yi;
            final double zi = z[i];
            final double zzi = zi * zi;
            final double xyi = xi * yi;
            final double xzi = xi * zi;
            final double yzi = yi * zi;

            sx += xi;
            sy += yi;
            sz += zi;

            sxx += xxi;
            syy += yyi;
            szz += zzi;
            sxy += xyi;
            sxz += xzi;
            syz += yzi;

            sxxx += xxi * xi;
            syyy += yyi * yi;
            szzz += zzi * zi;
            sxxy += xxi * yi;
            sxyy += xi * yyi;
            sxxz += xxi * zi;
            sxzz += xi * zzi;
            syyz += yyi * zi;
            syzz += yi * zzi;
            sxyz += xyi * zi;

            sxxxx += xxi * xxi;
            syyyy += yyi * yyi;
            szzzz += zzi * zzi;
            sxxxy += xxi * xyi;
            sxxyy += xxi * yyi;
            sxyyy += xyi * yyi;
            sxxxz += xxi * xzi;
            sxxzz += xxi * zzi;
            sxzzz += xzi * zzi;
            syyyz += yyi * yzi;
            syyzz += yyi * zzi;
            syzzz += yzi * zzi;
            sxxyz += xxi * yzi;
            sxyyz += xyi * yzi;
            sxyzz += xyi * zzi;

            st += ti;
            stx += ti * xi;
            sty += ti * yi;
            stz += ti * zi;
            stxx += ti * xxi;
            styy += ti * yyi;
            stzz += ti * zzi;
            stxy += ti * xyi;
            stxz += ti * xzi;
            styz += ti * yzi;
        }
        final double[][] a = new double[][]{
                {t.length, sx, sy, sz, sxx, syy, szz, sxy, sxz, syz},
                {sx, sxx, sxy, sxz, sxxx, sxyy, sxzz, sxxy, sxxz, sxyz},
                {sy, sxy, syy, syz, sxxy, syyy, syzz, sxyy, sxyz, syyz},
                {sz, sxz, syz, szz, sxxz, syyz, szzz, sxyz, sxzz, syzz},
                {sxx, sxxx, sxxy, sxxz, sxxxx, sxxyy, sxxzz, sxxxy, sxxxz, sxxyz},
                {syy, sxyy, syyy, syyz, sxxyy, syyyy, syyzz, sxyyy, sxyyz, syyyz},
                {szz, sxzz, syzz, szzz, sxxzz, syyzz, szzzz, sxyzz, sxzzz, syzzz},
                {sxy, sxxy, sxyy, sxyz, sxxxy, sxyyy, sxyzz, sxxyy, sxxyz, sxyyz},
                {sxz, sxxz, sxyz, sxzz, sxxxz, sxyyz, sxzzz, sxxyz, sxxzz, sxyzz},
                {syz, sxyz, syyz, syzz, sxxyz, syyyz, syzzz, sxyyz, sxyzz, syyzz}
        };
        final double[] b = new double[]{st, stx, sty, stz, stxx, styy, stzz, stxy, stxz, styz};
        ensureNoInterceptCase(hasIntercept, a, b);
        LinAlg.solve(a, b);
        return b;
    }

    @Override
    public String toString() {
        if (dim == 1) {
            if (degree == 1) {
                return String.format(Locale.US,
                                     "t~%f%+f*x; mse=%f",
                                     cf[0], cf[1], mse);
            } else if (degree == 2) {
                return String.format(Locale.US,
                                     "t~%f%+f*x%+f*x^2; mse=%f",
                                     cf[0], cf[1], cf[2], mse);
            } else if (degree == 3) {
                return String.format(Locale.US,
                                     "t~%f%+f*x%+f*x^2%+f*x^3; mse=%f",
                                     cf[0], cf[1], cf[2], cf[3], mse);
            }
        } else if (dim == 2) {
            if (degree == 1) {
                return String.format(Locale.US,
                                     "t~%f%+f*x%+f*y; mse=%f",
                                     cf[0], cf[1], cf[2], mse);
            } else if (degree == 2) {
                return String.format(Locale.US,
                                     "t~%f%+f*x%+f*y%+f*x^2%+f*y^2%+f*x*y; mse=%f",
                                     cf[0], cf[1], cf[2], cf[3], cf[4], cf[5], mse);
            } else if (degree == 3) {
                return String.format(Locale.US,
                                     "t~%f%+f*x%+f*y%+f*x^2%+f*y^2%+f*x*y%+f*x^3%+f*y^3%+f*x^2*y%+f*x*y^2; mse=%f",
                                     cf[0], cf[1], cf[2], cf[3], cf[4], cf[5], cf[6], cf[7], cf[8], cf[9], mse);
            }
        } else if (dim == 3) {
            if (degree == 1) {
                return String.format(Locale.US,
                                     "t~%f%+f*x%+f*y%+f*z; mse=%f",
                                     cf[0], cf[1], cf[2], cf[3], mse);
            } else if (degree == 2) {
                return String.format(Locale.US,
                                     "t~%f%+f*x%+f*y%+f*z%+f*x^2%+f*y^2%+f*z^2%+f*x*y%+f*x*z%+f*y*z; mse=%f",
                                     cf[0], cf[1], cf[2], cf[3], cf[4], cf[5], cf[6], cf[7], cf[8], cf[9], mse);
            } else if (degree == 3) {
                return String.format(Locale.US,
                                     "t~%f%+f*x%+f*y%+f*z%+f*x^2%+f*y^2%+f*z^2%+f*x*y%+f*x*z%+f*y*z" +
                                     "%+f*x^3%+f*y^3%+f*z^3%+f*x^2*y%+f*x*y^2%+f*x^2*z%+f*x*z^2%+f*y^2*z%+f*y*z^2" +
                                     "%+f*x*y*z; mse=%f",
                                     cf[0], cf[1], cf[2], cf[3], cf[4], cf[5], cf[6], cf[7], cf[8], cf[9],
                                     cf[10], cf[11], cf[12], cf[13], cf[14], cf[15], cf[16], cf[17], cf[18],
                                     cf[19], mse);
            }
        }
        return super.toString();
    }
}
