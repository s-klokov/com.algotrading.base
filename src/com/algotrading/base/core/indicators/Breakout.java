package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.IntColumn;
import com.algotrading.base.core.series.FinSeries;

/**
 * Вычисление моментов пробоя экстремумов.
 * <p>
 * Пусть заданы натуральные числа 0 < m < n, вещественные числа a > 0 и k > 0, вычислены типичные цены
 * свечей (H + L + C) / 3 и значения индикатора ATR с некоторым периодом period > n.
 * <p>
 * Считается, что на свече i пробивается прошлый максимум j, если выполнены следующие условия:<br>
 * 1) H[j] -- двусторонний максимум:<br>
 * H[j] >= H[k] при j < k < i и j - [a * (i - j)] < k < j;<br>
 * 2) H[i] > H[j];<br>
 * 3) среднее уклонение типичной цены от уровня пробиваемого максимума достаточно велико:<br>
 * average(H[j] - (H[k] + L[k] + C[k]) / 3; j < k < i) &ge; k * ATR[i - 1] * sqrt(i - j - 1),<br>
 * average(H[j] - (H[k] + L[k] + C[k]) / 3; j - [a * (i - j)] < k < j) &ge; k * ATR[i - 1] * sqrt([a * (i - j)] - 1).
 * <p>
 * Если на свече i пробивается прошлый максимум j и при этом m &le; i - j &le; n,
 * то индикатор {@link #breakHigh(FinSeries, int, int, double, double, int, String, String, String, String)}
 * равен индексу пробиваемого прошлого максимума, иначе -1. Если пробивается более одного прошлого
 * максимума, то указывается индекс ближайшего из них.
 * <p>
 * В дополнение может быть вычислено среднее уклонение типичной цены от уровня пробиваемого максимума
 * в диапазоне между пробиваемым максимумом и пробивающей свечой.
 * <p>
 * Случай пробоя минимума реализуется аналогичным образом в методе
 * {@link #breakLow(FinSeries, int, int, double, double, int, String, String, String, String)}
 */
public class Breakout {

    private Breakout() {
        throw new UnsupportedOperationException();
    }

    /**
     * Вычислить пробои максимумов. В процессе вычислений при необходимости создаются колонки со значениями
     * типичной цены и индикатором ATR.
     *
     * @param series              временной ряд с колонками H, L, C
     * @param m                   минимальный горизонт поиска прошлого максимума
     * @param n                   максимальный горизонт поиска прошлого максимума
     * @param a                   относительный размер окрестности слева от максимума
     * @param k                   коэффициент для определения достаточности уклонения типичной цены
     * @param atrPeriod           период индикатора ATR
     * @param atrColumnName       название колонки со значениями индикатора ATR (будет создана при необходимости)
     * @param tpColumnName        название колонки со значениями типичной цены (будет создана при необходимости)
     * @param breakHighColumnName название колонки типа {@link IntColumn} с индексами пробиваемых максимумов
     * @param deviationColumnName название колонки со средним уклонением типичной цены от уровня максимума;
     *                            {@code null} если колонку вычислять не нужно
     */
    public static void breakHigh(final FinSeries series,
                                 final int m, final int n, final double a,
                                 final double k, final int atrPeriod,
                                 final String atrColumnName,
                                 final String tpColumnName,
                                 final String breakHighColumnName,
                                 final String deviationColumnName) {
        if (m >= n || a < 0 || k < 0 || atrPeriod <= 1) {
            throw new IllegalArgumentException("m=" + m + ", n=" + n + ", a=" + a + ", k=" + k
                                               + ", atrPeriod=" + atrPeriod);
        }
        final DoubleColumn tpColumn = ensureTpColumn(series, tpColumnName);
        final DoubleColumn atrColumn = series.acquireDoubleColumn(atrColumnName,
                                                                  () -> Atr.atr(series, atrPeriod, atrColumnName));
        final IntColumn breakHighColumn = series.acquireIntColumn(breakHighColumnName);
        final DoubleColumn deviationColumn = (deviationColumnName == null) ? null : series.acquireDoubleColumn(deviationColumnName);
        final DoubleColumn high = series.high();
        final int len = high.length();
        for (int i = 0; i < len; i++) {
            breakHighColumn.set(i, -1);
            if (deviationColumn != null) {
                deviationColumn.set(i, Double.NaN);
            }
            if (i == 0) {
                continue;
            }
            final double hi = high.get(i);
            final double atr = atrColumn.get(i - 1);
            for (int j = i - n; j <= i - m; j++) {
                final int l = j - (int) (a * (i - j));
                if (l <= -1) {
                    continue;
                }
                final double hj = high.get(j);
                if (hi <= hj || !isMaximum(j, high, l + 1, j) || !isMaximum(j, high, j + 1, i)) {
                    continue;
                }
                final double deviation = getAverageDeviationFromPrice(tpColumn, j + 1, i, hj);
                if (deviation < k * atr * Math.sqrt(i - j - 1)) {
                    continue;
                }
                if (getAverageDeviationFromPrice(tpColumn, l + 1, j, hj) < k * atr * Math.sqrt(j - l - 1)) {
                    continue;
                }
                breakHighColumn.set(i, j);
                if (deviationColumn != null) {
                    deviationColumn.set(i, deviation);
                    break;
                }
            }
        }
    }

    /**
     * Вычислить пробои минимумов. В процессе вычислений при необходимости создаются колонки со значениями
     * типичной цены и индикатором ATR.
     *
     * @param series              временной ряд с колонками H, L, C
     * @param m                   минимальный горизонт поиска прошлого минимума
     * @param n                   максимальный горизонт поиска прошлого минимума
     * @param a                   относительный размер окрестности слева от минимума
     * @param k                   коэффициент для определения достаточности уклонения типичной цены
     * @param atrPeriod           период индикатора ATR
     * @param atrColumnName       название колонки со значениями индикатора ATR (будет создана при необходимости)
     * @param tpColumnName        название колонки со значениями типичной цены (будет создана при необходимости)
     * @param breakLowColumnName  название колонки типа {@link IntColumn} с индексами пробиваемых минимумов
     * @param deviationColumnName название колонки со средним уклонением типичной цены от уровня минимума;
     *                            {@code null} если колонку вычислять не нужно
     */
    public static void breakLow(final FinSeries series,
                                final int m, final int n, final double a,
                                final double k, final int atrPeriod,
                                final String atrColumnName,
                                final String tpColumnName,
                                final String breakLowColumnName,
                                final String deviationColumnName) {
        if (m >= n || a < 0 || k < 0 || atrPeriod <= 1) {
            throw new IllegalArgumentException("m=" + m + ", n=" + n + ", a=" + a + ", k=" + k
                                               + ", atrPeriod=" + atrPeriod);
        }
        final DoubleColumn tpColumn = ensureTpColumn(series, tpColumnName);
        final DoubleColumn atrColumn = series.acquireDoubleColumn(atrColumnName,
                                                                  () -> Atr.atr(series, atrPeriod, atrColumnName));
        final IntColumn breakLowColumn = series.acquireIntColumn(breakLowColumnName);
        final DoubleColumn deviationColumn = (deviationColumnName == null) ? null : series.acquireDoubleColumn(deviationColumnName);
        final DoubleColumn low = series.low();
        final int len = low.length();
        for (int i = 0; i < len; i++) {
            breakLowColumn.set(i, -1);
            if (deviationColumn != null) {
                deviationColumn.set(i, Double.NaN);
            }
            if (i == 0) {
                continue;
            }
            final double li = low.get(i);
            final double atr = atrColumn.get(i - 1);
            for (int j = i - n; j <= i - m; j++) {
                final int l = j - (int) (a * (i - j));
                if (l <= -1) {
                    continue;
                }
                final double lj = low.get(j);
                if (li >= lj || !isMinimum(j, low, l + 1, j) || !isMinimum(j, low, j + 1, i)) {
                    continue;
                }
                final double deviation = getAverageDeviationFromPrice(tpColumn, j + 1, i, lj);
                if (deviation < k * atr * Math.sqrt(i - j - 1)) {
                    continue;
                }
                if (getAverageDeviationFromPrice(tpColumn, l + 1, j, lj) < k * atr * Math.sqrt(j - l - 1)) {
                    continue;
                }
                breakLowColumn.set(i, j);
                if (deviationColumn != null) {
                    deviationColumn.set(i, deviation);
                    break;
                }
            }
        }
    }

    private static DoubleColumn ensureTpColumn(final FinSeries series, final String tpColumnName) {
        DoubleColumn tpColumn = series.getDoubleColumn(tpColumnName);
        if (tpColumn == null) {
            tpColumn = series.acquireDoubleColumn(tpColumnName);
            final DoubleColumn high = series.high();
            final DoubleColumn low = series.low();
            final DoubleColumn close = series.close();
            final int len = tpColumn.length();
            for (int i = 0; i < len; i++) {
                tpColumn.set(i, (high.get(i) + low.get(i) + close.get(i)) / 3.0);
            }
        }
        return tpColumn;
    }

    private static boolean isMaximum(final int id, final DoubleColumn priceColumn, final int from, final int to) {
        final double h = priceColumn.get(id);
        for (int k = from; k < to; k++) {
            if (priceColumn.get(k) > h) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMinimum(final int id, final DoubleColumn priceColumn, final int from, final int to) {
        final double l = priceColumn.get(id);
        for (int k = from; k < to; k++) {
            if (priceColumn.get(k) < l) {
                return false;
            }
        }
        return true;
    }

    private static double getAverageDeviationFromPrice(final DoubleColumn priceColumn,
                                                       final int from, final int to,
                                                       final double price) {
        if (from >= to) {
            return 0;
        }
        double sum = 0;
        for (int k = from; k < to; k++) {
            sum += Math.abs(priceColumn.get(k) - price);
        }
        return sum / (to - from);
    }
}
