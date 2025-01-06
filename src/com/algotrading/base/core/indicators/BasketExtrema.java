package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.sync.Synchronizer;
import com.algotrading.base.core.values.DoubleValue;
import com.algotrading.base.core.window.WindowOfDouble;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * Вычисление индикатора, отражающего одновременность обновления минимумов/максимумов корзиной инструментов.
 */
public class BasketExtrema {

    private BasketExtrema() {
        throw new UnsupportedOperationException();
    }

    /**
     *Равномерная весова функция.
     */
    public static final ToDoubleFunction<String> UNIFORM_WEIGHT_FUNCTION = s -> 1.0;

    /**
     * Сформировать временной ряд из двух колонок с именами {@link FinSeries#T} и {@code columnName}.
     * Значение индикатора равно суммарному весу инструментов, обновивших максимумы за окно длины {@code longPeriod}
     * в течение последних {@code shortPeriod} моментов времени, за вычетом суммарного веса инструментов,
     * обновивших минимумы то же окно, делённому на общий вес инструментов.
     *
     * @param marketDataMap соответствие код инструмента -> свечной временной ряд инструмента
     * @param weightFunction весовая функция, задающая для каждого инструмента его вес для использования в индикаторе
     * @param longPeriod    длина окна для расчёта максимумов/минимумов
     * @param shortPeriod   длина окна для отслеживания одновременности обновления экстремумов
     * @param columnName    название колонки индикатора
     * @return временной ряд с колонками с именами {@link FinSeries#T} и {@code columnName}
     */
    public static FinSeries basketExtrema(final Map<String, FinSeries> marketDataMap,
                                          final ToDoubleFunction<String> weightFunction,
                                          final int longPeriod,
                                          final int shortPeriod,
                                          final String columnName) {
        final FinSeries indicatorSeries = new FinSeries();
        final LongColumn timeCode = indicatorSeries.acquireLongColumn(FinSeries.T);
        final DoubleColumn column = indicatorSeries.acquireDoubleColumn(columnName);

        final Synchronizer synchronizer = new Synchronizer();
        final Map<String, WindowOfDouble> mapOfHighs = new HashMap<>();
        final Map<String, WindowOfDouble> mapOfLows = new HashMap<>();
        final DoubleValue totalWeightValue = new DoubleValue(0);
        marketDataMap.forEach((secCode, series) -> {
            synchronizer.put(series.timeCode());
            mapOfHighs.put(secCode, new WindowOfDouble(longPeriod + shortPeriod));
            mapOfLows.put(secCode, new WindowOfDouble(longPeriod + shortPeriod));
            totalWeightValue.set(totalWeightValue.get() + weightFunction.applyAsDouble(secCode));
        });

        int counter = 0;
        final DoubleValue doubleValue = new DoubleValue();
        long t;
        while ((t = synchronizer.synchronize()) != Long.MAX_VALUE) {
            marketDataMap.forEach((secCode, series) -> {
                final int id = synchronizer.getLastIndex(series.timeCode());
                if (id < 0) {
                    mapOfHighs.get(secCode).add(Double.NEGATIVE_INFINITY);
                    mapOfLows.get(secCode).add(Double.POSITIVE_INFINITY);
                } else {
                    mapOfHighs.get(secCode).add(series.high().get(id));
                    mapOfLows.get(secCode).add(series.low().get(id));
                }
            });

            doubleValue.set(0.0);
            if (++counter >= longPeriod + shortPeriod) {
                marketDataMap.forEach((secCode, series) -> {
                    double max = Double.NEGATIVE_INFINITY;
                    double min = Double.POSITIVE_INFINITY;
                    final WindowOfDouble windowOfHighs = mapOfHighs.get(secCode);
                    final WindowOfDouble windowOfLows = mapOfLows.get(secCode);
                    for (int i = -longPeriod - shortPeriod + 1; i <= -shortPeriod; i++) {
                        max = Math.max(max, windowOfHighs.get(i));
                        min = Math.min(min, windowOfLows.get(i));
                    }
                    if (Double.isFinite(max)) {
                        for (int i = -shortPeriod + 1; i <= 0; i++) {
                            if (windowOfHighs.get(i) > max) {
                                doubleValue.set(doubleValue.get() + weightFunction.applyAsDouble(secCode));
                                break;
                            }
                        }
                    }
                    if (Double.isFinite(min)) {
                        for (int i = -shortPeriod + 1; i <= 0; i++) {
                            if (windowOfLows.get(i) < min) {
                                doubleValue.set(doubleValue.get() - weightFunction.applyAsDouble(secCode));
                                break;
                            }
                        }
                    }
                });
            }

            timeCode.append(t);
            column.append(doubleValue.get() / totalWeightValue.get());
        }

        return indicatorSeries;
    }
}
