package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.sync.Synchronizer;

import java.util.Map;

/**
 * Вычисление взвешенного индекса по ценам закрытия набора инструментов.
 * <p>
 * Для каждого инструмента на очередной свече вычисляется относительное изменение цены
 * delta = C[i] / C[i-1] - 1, i > 0, и delta = C[i] / O[i] - 1, i = 0.
 * Полученные изменения усредняются с заданными весами, после чего индекс обновляется
 * с помощью полученного среднего изменения.
 */
public class Index {

    /**
     * Весовая функция для вычисления индекса.
     */
    public interface WeightFunction {
        /**
         * Получить значение веса инструмента в индексе в указанный момент времени.
         *
         * @param t       метка времени
         * @param secCode код инструмента
         * @return вес инструмента в индексе
         */
        double applyAsDouble(final long t, final String secCode);
    }

    /**
     * Весовая функция для вычисления индекса через среднее арифметическое приращений.
     */
    public static final WeightFunction SIMPLE_AVERAGE_WEIGHT_FUNCTION = (t, secCode) -> 1.0;

    private Index() {
        throw new UnsupportedOperationException();
    }

    /**
     * Вычислить значение взвешенного индекса по набору временных рядов инструментов.
     *
     * @param marketDataMap   соответствие secCode -> временной ряд, содержащий значения O и C
     * @param weightFunction  весовая функция, определяющая по коду инструмента его вес в индексе
     * @param indexColumnName название колонки, куда будет записано значение индекса
     * @return временной ряд со значениями индекса
     */
    public static FinSeries indexSeries(final Map<String, FinSeries> marketDataMap,
                                        final WeightFunction weightFunction,
                                        final String indexColumnName) {
        final FinSeries indexSeries = new FinSeries();
        final LongColumn indexTimeCode = indexSeries.acquireLongColumn(FinSeries.T);
        final DoubleColumn indexColumn = indexSeries.acquireDoubleColumn(indexColumnName);

        final Synchronizer synchronizer = new Synchronizer();
        marketDataMap.forEach((secCode, series) -> synchronizer.put(series.timeCode()));

        double sumWeights, sumWeightedDeltas;
        double indexValue = 1.0;
        while (synchronizer.synchronize() != Long.MAX_VALUE) {
            final long t = synchronizer.t();
            sumWeights = 0;
            sumWeightedDeltas = 0;
            for (final Map.Entry<String, FinSeries> entry : marketDataMap.entrySet()) {
                final String secCode = entry.getKey();
                final FinSeries series = entry.getValue();
                final LongColumn timeCode = series.timeCode();
                final int lastId = synchronizer.getLastIndex(timeCode);
                if (lastId < 0) {
                    continue;
                }
                final int currId = synchronizer.getCurrIndex(timeCode);
                final double weight = weightFunction.applyAsDouble(t, secCode);
                if (!Double.isFinite(weight)) {
                    continue;
                }
                sumWeights += weight;
                final double delta;
                if (currId < 0) {
                    delta = 0;
                } else if (currId == 0) {
                    delta = series.close().get(currId) / series.open().get(currId) - 1.0;
                } else {
                    final DoubleColumn close = series.close();
                    delta = close.get(currId) / close.get(currId - 1) - 1.0;
                }
                sumWeightedDeltas += weight * delta;
            }
            final double delta = (sumWeightedDeltas == 0) ? 0 : (sumWeightedDeltas / sumWeights);
            indexValue *= (1.0 + delta);
            indexTimeCode.append(t);
            indexColumn.append(indexValue);
        }

        return indexSeries;
    }
}
