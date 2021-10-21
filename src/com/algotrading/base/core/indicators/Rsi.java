package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.Series;
import com.algotrading.base.core.window.WindowOfDouble;

/**
 * Вычисление индикатора RSI.
 */
public final class Rsi {

    private Rsi() {
    }

    /**
     * Вычислить индикатор RSI с экспоненциальным усреднением.
     *
     * @param series          временной ряд
     * @param priceColumnName название колонки временного ряда, содержащей цены, по которым вычисляется индикатор
     * @param period          период
     * @param rsiColumnName   имя колонки, куда будет записано значение индикатора
     * @return колонка со значениями индикатора.
     */
    public static DoubleColumn rsiExp(final Series series,
                                      final String priceColumnName,
                                      final int period,
                                      final String rsiColumnName) {
        return rsiExp(series, series.getDoubleColumn(priceColumnName), period, rsiColumnName);
    }

    /**
     * Вычислить индикатор RSI с экспоненциальным усреднением.
     *
     * @param series        временной ряд
     * @param priceColumn   колонка временного ряда, содержащая цены, по которым вычисляется индикатор
     * @param period        период
     * @param rsiColumnName имя колонки, куда будет записано значение индикатора
     * @return колонка со значениями индикатора.
     */
    public static DoubleColumn rsiExp(final Series series,
                                      final DoubleColumn priceColumn,
                                      final int period,
                                      final String rsiColumnName) {
        final int len = series.length();
        final DoubleColumn rsiColumn = series.acquireDoubleColumn(rsiColumnName);
        if (len == 0) {
            return rsiColumn;
        }
        rsiColumn.set(0, 50.0);
        final double k = (double) (period - 1) / period;
        int n = 0;
        double u = 0;
        double d = 0;
        for (int i = 1; i < len; i++) {
            final double delta = priceColumn.get(i) - priceColumn.get(i - 1);
            n++;
            if (n <= period) {
                if (delta >= 0) {
                    u += delta;
                } else {
                    d -= delta;
                }
                if (n == period) {
                    u /= period;
                    d /= period;
                }
            } else {
                u *= k;
                d *= k;
                if (delta >= 0) {
                    u += delta / period;
                } else {
                    d += -delta / period;
                }
            }
            rsiColumn.set(i, (u + d <= 0) ? 50.0 : (100.0 * u / (u + d)));
        }
        return rsiColumn;
    }

    /**
     * Вычислить индикатор RSI с простым усреднением.
     *
     * @param series          временной ряд
     * @param priceColumnName название колонки временного ряда, содержащей цены, по которым вычисляется индикатор
     * @param period          период
     * @param rsiColumnName   имя колонки, куда будет записано значение индикатора
     * @return колонка со значениями индикатора.
     */
    public static DoubleColumn rsiSimple(final Series series,
                                         final String priceColumnName,
                                         final int period,
                                         final String rsiColumnName) {
        return rsiSimple(series, series.getDoubleColumn(priceColumnName), period, rsiColumnName);
    }

    /**
     * Вычислить индикатор RSI с простым усреднением.
     *
     * @param series        временной ряд
     * @param priceColumn   колонка временного ряда, содержащая цены, по которым вычисляется индикатор
     * @param period        период
     * @param rsiColumnName имя колонки, куда будет записано значение индикатора
     * @return колонка со значениями индикатора.
     */
    public static DoubleColumn rsiSimple(final Series series,
                                         final DoubleColumn priceColumn,
                                         final int period,
                                         final String rsiColumnName) {
        final int len = series.length();
        final DoubleColumn rsiColumn = series.acquireDoubleColumn(rsiColumnName);
        if (len == 0) {
            return rsiColumn;
        }
        rsiColumn.set(0, 50.0);
        final WindowOfDouble w = new WindowOfDouble(period);
        double u = 0;
        double d = 0;
        for (int i = 1; i < len; i++) {
            if (w.isFull()) {
                final double delta = w.get(-period + 1);
                if (delta > 0) {
                    u -= delta;
                } else if (delta < 0) {
                    d -= -delta;
                }
            }
            final double delta = priceColumn.get(i) - priceColumn.get(i - 1);
            w.add(delta);
            if (delta > 0) {
                u += delta;
            } else if (delta < 0) {
                d -= delta;
            }
            if (w.isFull()) {
                rsiColumn.set(i, (u + d <= 0) ? 50.0 : (100.0 * u / (u + d)));
            } else {
                rsiColumn.set(i, 50.0);
            }
        }
        return rsiColumn;
    }
}
