package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.Series;

import java.util.Arrays;

/**
 * Вычисление отображения: окно некоторого периода -> квантиль последнего элемента окна,
 * т.е. отношения номера элемента в отсортированном окне (при нумерации с нуля)
 * к размеру окна, уменьшенному на 1.
 * <p>
 * Таким образом, в массиве попарно различных элементов минимальный элемент будет иметь квантиль 0,
 * а максимальный -- квантиль 1.
 */
public class Quantile {

    private Quantile() {
    }

    /**
     * Вычислить индикатор Quantile.
     *
     * @param series             временной ряд
     * @param valueColumnName    название колонки временного ряда, содержащей значения, по которым вычисляется индикатор
     * @param period             период
     * @param quantileColumnName имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn quantile(final Series series,
                                        final String valueColumnName,
                                        final int period,
                                        final String quantileColumnName) {
        return quantile(series, series.getDoubleColumn(valueColumnName), period, quantileColumnName);
    }

    /**
     * Вычислить индикатор Quantile.
     *
     * @param valueColumn        колонка временного ряда, содержащей значения, по которым вычисляется индикатор
     * @param period             период
     * @param quantileColumnName имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn quantile(final Series series,
                                        final DoubleColumn valueColumn,
                                        final int period,
                                        final String quantileColumnName) {
        final DoubleColumn quantileColumn = series.acquireDoubleColumn(quantileColumnName);
        final int len = valueColumn.length();
        if (len == 0) {
            return quantileColumn;
        }
        quantileColumn.set(0, 0.5);
        final double[] array = new double[period];
        for (int i = 1; i < len; i++) {
            final int size = Math.min(i + 1, period);
            for (int j = 0; j < period && i - j >= 0; j++) {
                array[j] = valueColumn.get(i - j);
            }
            Arrays.sort(array, 0, size);
            final double key = valueColumn.get(i);
            final int id = Arrays.binarySearch(array, 0, size, key);
            int idMin = id;
            for (int j = id - 1; j >= 0; j--) {
                if (array[j] == key) {
                    idMin = j;
                } else {
                    break;
                }
            }
            int idMax = id;
            for (int j = id + 1; j < size; j++) {
                if (array[j] == key) {
                    idMax = j;
                } else {
                    break;
                }
            }
            quantileColumn.set(i, (idMin + idMax) / 2.0 / (size - 1));
        }
        return quantileColumn;
    }
}