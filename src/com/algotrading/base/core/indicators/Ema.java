package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.Series;

/**
 * Вычисление индикатора EMA.
 */
public final class Ema {

    private Ema() {
    }

    /**
     * Вычислить индикатор EMA.
     *
     * @param series          временной ряд
     * @param priceColumnName название колонки временного ряда, содержащей цены, по которым вычисляется индикатор
     * @param period          период
     * @param emaColumnName   имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора.
     */
    public static DoubleColumn ema(final Series series,
                                   final String priceColumnName,
                                   final int period,
                                   final String emaColumnName) {
        return ema(series, series.getDoubleColumn(priceColumnName), period, emaColumnName);
    }

    /**
     * Вычислить индикатор EMA.
     *
     * @param series        временной ряд
     * @param priceColumn   колонка временного ряда, содержащая цены, по которым вычисляется индикатор
     * @param period        период
     * @param emaColumnName имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора.
     */
    public static DoubleColumn ema(final Series series,
                                   final DoubleColumn priceColumn,
                                   final int period,
                                   final String emaColumnName) {
        final int len = series.length();
        final DoubleColumn emaColumn = series.acquireDoubleColumn(emaColumnName);
        if (len == 0) {
            return emaColumn;
        }
        double ema = priceColumn.get(0);
        emaColumn.set(0, ema);
        final int pm = period - 1;
        final int pp = period + 1;
        for (int i = 1; i < len; i++) {
            ema = (ema * pm + 2 * priceColumn.get(i)) / pp;
            emaColumn.set(i, ema);
        }
        return emaColumn;
    }
}
