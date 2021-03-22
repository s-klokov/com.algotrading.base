package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.Series;

/**
 * Вычисление индикаторов HHV, LLV.
 */
public final class HhvLlv {

    private HhvLlv() {
    }

    /**
     * Вычислить индикатор HHV.
     *
     * @param series          временной ряд
     * @param priceColumnName название колонки временного ряда, содержащей цены, по которым вычисляется индикатор
     * @param period          период
     * @param hhvColumnName   имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn hhv(final Series series,
                                   final String priceColumnName,
                                   final int period,
                                   final String hhvColumnName) {
        return hhv(series, series.getDoubleColumn(priceColumnName), period, hhvColumnName);
    }

    /**
     * Вычислить индикатор HHV.
     *
     * @param series        временной ряд
     * @param priceColumn   колонка временного ряда, содержащая цены, по которым вычисляется индикатор
     * @param period        период
     * @param hhvColumnName имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn hhv(final Series series,
                                   final DoubleColumn priceColumn,
                                   final int period,
                                   final String hhvColumnName) {
        final DoubleColumn hhvColumn = series.acquireDoubleColumn(hhvColumnName);
        final int len = hhvColumn.length();
        if (len == 0) {
            return hhvColumn;
        }
        double hhv = priceColumn.get(0);
        int id = 0;
        hhvColumn.set(0, hhv);
        for (int i = 1; i < len; i++) {
            final double p = priceColumn.get(i);
            if (hhv <= p) {
                hhv = p;
                id = i;
            } else if (i - id >= period) {
                id = maxId(priceColumn, Math.max(0, i - period + 1), i + 1);
                hhv = priceColumn.get(id);
            }
            hhvColumn.set(i, hhv);
        }
        return hhvColumn;
    }

    /**
     * Вычислить индикатор LLV.
     *
     * @param series          временной ряд
     * @param priceColumnName название колонки временного ряда, содержащей цены, по которым вычисляется индикатор
     * @param period          период
     * @param llvColumnName   имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn llv(final Series series,
                                   final String priceColumnName,
                                   final int period,
                                   final String llvColumnName) {
        return llv(series, series.getDoubleColumn(priceColumnName), period, llvColumnName);
    }

    /**
     * Вычислить индикатор LLV.
     *
     * @param series        временной ряд
     * @param priceColumn   колонка временного ряда, содержащая цены, по которым вычисляется индикатор
     * @param period        период
     * @param llvColumnName имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn llv(final Series series,
                                   final DoubleColumn priceColumn,
                                   final int period,
                                   final String llvColumnName) {
        final DoubleColumn llvColumn = series.acquireDoubleColumn(llvColumnName);
        final int len = llvColumn.length();
        if (len == 0) {
            return llvColumn;
        }
        double llv = priceColumn.get(0);
        int id = 0;
        llvColumn.set(0, llv);
        for (int i = 1; i < len; i++) {
            final double p = priceColumn.get(i);
            if (llv >= p) {
                llv = p;
                id = i;
            } else if (i - id >= period) {
                id = minId(priceColumn, Math.max(0, i - period + 1), i + 1);
                llv = priceColumn.get(id);
            }
            llvColumn.set(i, llv);
        }
        return llvColumn;
    }

    private static int maxId(final DoubleColumn priceColumn, final int from, final int to) {
        double max = Double.NEGATIVE_INFINITY;
        int maxId = -1;
        for (int i = from; i < to; i++) {
            final double p = priceColumn.get(i);
            if (max <= p) {
                max = p;
                maxId = i;
            }
        }
        return maxId;
    }

    private static int minId(final DoubleColumn priceColumn, final int from, final int to) {
        double min = Double.POSITIVE_INFINITY;
        int minId = -1;
        for (int i = from; i < to; i++) {
            final double p = priceColumn.get(i);
            if (min >= p) {
                min = p;
                minId = i;
            }
        }
        return minId;
    }
}
