package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.Series;

/**
 * Вычисление индикатора ROC (rate of change).
 */
public final class Roc {

    private Roc() {
    }

    /**
     * Вычислить индикатор ROC.
     *
     * @param series          временной ряд
     * @param priceColumnName название колонки временного ряда, содержащей цены, по которым вычисляется индикатор
     * @param period          период
     * @param rocColumnName   имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn roc(final Series series,
                                   final String priceColumnName,
                                   final int period,
                                   final String rocColumnName) {
        return roc(series, series.getDoubleColumn(priceColumnName), period, rocColumnName);
    }

    /**
     * Вычислить индикатор ROC.
     *
     * @param series        временной ряд
     * @param priceColumn   колонка временного ряда, содержащая цены, по которым вычисляется индикатор
     * @param period        период
     * @param rocColumnName название колонки, куда будет записано значение индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn roc(final Series series,
                                   final DoubleColumn priceColumn,
                                   final int period,
                                   final String rocColumnName) {
        final int len = series.length();
        final DoubleColumn rocColumn = series.acquireDoubleColumn(rocColumnName);
        for (int i = 0; i < period; i++) {
            rocColumn.set(i, Double.NaN);
        }
        for (int i = period; i < len; i++) {
            rocColumn.set(i, (priceColumn.get(i) / priceColumn.get(i - period) - 1) * 100);
        }
        return rocColumn;
    }
}
