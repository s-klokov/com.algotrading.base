package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.Series;
import com.algotrading.base.core.window.WindowOfDouble;

/**
 * Вычисление индикатора MA (Moving Average).
 */
@SuppressWarnings("ClassNamingConvention")
public final class Ma {

    private Ma() {
        throw new UnsupportedOperationException();
    }

    /**
     * Вычислить индикатор MA.
     *
     * @param series          временной ряд
     * @param priceColumnName название колонки временного ряда, содержащей цены, по которым вычисляется индикатор
     * @param period          период
     * @param maColumnName    имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn ma(final Series series,
                                  final String priceColumnName,
                                  final int period,
                                  final String maColumnName) {
        return ma(series, series.getDoubleColumn(priceColumnName), period, maColumnName);
    }

    /**
     * Вычислить индикатор MA.
     *
     * @param series       временной ряд
     * @param priceColumn  колонка временного ряда, содержащая цены, по которым вычисляется индикатор
     * @param period       период
     * @param maColumnName имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn ma(final Series series,
                                  final DoubleColumn priceColumn,
                                  final int period,
                                  final String maColumnName) {
        final int len = series.length();
        final DoubleColumn maColumn = series.acquireDoubleColumn(maColumnName);
        final WindowOfDouble window = new WindowOfDouble(period);
        double sum = 0;
        for (int i = 0; i < len; i++) {
            if (window.isFull()) {
                sum -= window.get(-period + 1);
            }
            final double price = priceColumn.get(i);
            sum += price;
            window.add(price);
            maColumn.set(i, sum / window.size());
        }
        return maColumn;
    }
}
