package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.Series;
import com.algotrading.base.core.window.WindowOfDouble;

/**
 * Вычисление индикатора StDev (Standard Deviation).
 */
public final class StDev {

    private StDev() {
        throw new UnsupportedOperationException();
    }

    /**
     * Вычислить индикатор StDev.
     *
     * @param series          временной ряд
     * @param priceColumnName название колонки временного ряда, содержащей цену
     * @param avgColumnName   название колонки временного ряда, содержащей среднее
     * @param period          период
     * @param stDevColumnName имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn stDev(final Series series,
                                     final String priceColumnName,
                                     final String avgColumnName,
                                     final int period,
                                     final String stDevColumnName) {
        return stDev(series, series.getDoubleColumn(priceColumnName), series.getDoubleColumn(avgColumnName),
                     period, stDevColumnName);
    }

    /**
     * Вычислить индикатор MA.
     *
     * @param series          временной ряд
     * @param priceColumn     колонка временного ряда, содержащая цену
     * @param avgColumn       колонка временного ряда, содержащая среднее
     * @param period          период
     * @param stDevColumnName имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn stDev(final Series series,
                                     final DoubleColumn priceColumn,
                                     final DoubleColumn avgColumn,
                                     final int period,
                                     final String stDevColumnName) {
        final DoubleColumn stDevColumn = series.acquireDoubleColumn(stDevColumnName);
        final int len = stDevColumn.length();
        final WindowOfDouble window = new WindowOfDouble(period);
        double sum = 0;
        double sum2 = 0;
        for (int i = 0; i < len; i++) {
            final double x = priceColumn.get(i) - avgColumn.get(i);
            if (Double.isFinite(x)) {
                if (window.isFull()) {
                    final double y = window.get(-period + 1);
                    sum -= y;
                    sum2 -= y * y;
                }
                sum += x;
                sum2 += x * x;
                window.add(x);
            }
            if (window.isFull()) {
                double v = (sum2 - sum * sum / period) / (period - 1);
                if (v < 0 || !Double.isFinite(v)) {
                    v = 0;
                }
                stDevColumn.set(i, Math.sqrt(v));
            } else {
                stDevColumn.set(i, Double.NaN);
            }
        }
        return stDevColumn;
    }
}
