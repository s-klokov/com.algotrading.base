package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.Series;

/**
 * Вычисление индикаторов HV (highest value), LV (lowest value).
 */
public final class HvLv {

    private HvLv() {
        throw new UnsupportedOperationException();
    }

    /**
     * Вычислить индикатор HV (highest value).
     *
     * @param series          временной ряд
     * @param priceColumnName название колонки временного ряда, содержащей цены, по которым вычисляется индикатор
     * @param period          период
     * @param hvColumnName   имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn hv(final Series series,
                                  final String priceColumnName,
                                  final int period,
                                  final String hvColumnName) {
        return hv(series, series.getDoubleColumn(priceColumnName), period, hvColumnName);
    }

    /**
     * Вычислить индикатор HV (highest value).
     *
     * @param series        временной ряд
     * @param priceColumn   колонка временного ряда, содержащая цены, по которым вычисляется индикатор
     * @param period        период
     * @param hvColumnName имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn hv(final Series series,
                                  final DoubleColumn priceColumn,
                                  final int period,
                                  final String hvColumnName) {
        final DoubleColumn hvColumn = series.acquireDoubleColumn(hvColumnName);
        final int len = hvColumn.length();
        if (len == 0) {
            return hvColumn;
        }
        double hv = priceColumn.get(0);
        int id = 0;
        hvColumn.set(0, hv);
        for (int i = 1; i < len; i++) {
            final double p = priceColumn.get(i);
            if (hv <= p) {
                hv = p;
                id = i;
            } else if (i - id >= period) {
                id = maxId(priceColumn, Math.max(0, i - period + 1), i + 1);
                hv = priceColumn.get(id);
            }
            hvColumn.set(i, hv);
        }
        return hvColumn;
    }

    /**
     * Вычислить индикатор LV (lowest value).
     *
     * @param series          временной ряд
     * @param priceColumnName название колонки временного ряда, содержащей цены, по которым вычисляется индикатор
     * @param period          период
     * @param lvColumnName   имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn lv(final Series series,
                                  final String priceColumnName,
                                  final int period,
                                  final String lvColumnName) {
        return lv(series, series.getDoubleColumn(priceColumnName), period, lvColumnName);
    }

    /**
     * Вычислить индикатор LV (lowest value).
     *
     * @param series        временной ряд
     * @param priceColumn   колонка временного ряда, содержащая цены, по которым вычисляется индикатор
     * @param period        период
     * @param lvColumnName имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn lv(final Series series,
                                  final DoubleColumn priceColumn,
                                  final int period,
                                  final String lvColumnName) {
        final DoubleColumn lvColumn = series.acquireDoubleColumn(lvColumnName);
        final int len = lvColumn.length();
        if (len == 0) {
            return lvColumn;
        }
        double lv = priceColumn.get(0);
        int id = 0;
        lvColumn.set(0, lv);
        for (int i = 1; i < len; i++) {
            final double p = priceColumn.get(i);
            if (lv >= p) {
                lv = p;
                id = i;
            } else if (i - id >= period) {
                id = minId(priceColumn, Math.max(0, i - period + 1), i + 1);
                lv = priceColumn.get(id);
            }
            lvColumn.set(i, lv);
        }
        return lvColumn;
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
