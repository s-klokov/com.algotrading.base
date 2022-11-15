package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.WindowOfDouble;

/**
 * Индикатор ExitFlow, вычисляющий интенсивность закрытия позиций.
 * См. идею http://redmine.octan.ru/boards/2/topics/1230
 */
public class ExitFlow {

    private ExitFlow() {
        throw new UnsupportedOperationException();
    }

    /**
     * Вычислить значения индикатора ExitFlow и записать их в колонку временного ряда.
     *
     * @param series             временной ряд
     * @param atrPeriod          период ATR
     * @param windowSize         размер окна, в котором учитываются веса длинных и коротких позиций
     * @param drawdownBound      граница размера просадки
     * @param preBoundInertia    склонность к сохранению позиции, когда просадка меньше граничного значения
     * @param postBoundInertia   склонность к сохранению позиции, когда просадка больше граничного значения
     * @param exitFlowColumnName название колонки, куда будет записаны значения индикатора
     */
    public static DoubleColumn exitFlow(final FinSeries series,
                                        final int atrPeriod,
                                        final int windowSize,
                                        final double drawdownBound,
                                        final double preBoundInertia,
                                        final double postBoundInertia,
                                        final String exitFlowColumnName) {
        final DoubleColumn exitFlowColumn = series.acquireDoubleColumn(exitFlowColumnName);
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();

        double atrAccumulator = 0;
        double prevClose = Double.NaN;

        final WindowOfDouble longWeights = new WindowOfDouble(windowSize);
        final WindowOfDouble shortWeights = new WindowOfDouble(windowSize);
        final WindowOfDouble maxPrices = new WindowOfDouble(windowSize);
        final WindowOfDouble minPrices = new WindowOfDouble(windowSize);

        final int len = close.length();
        for (int i = 0; i < len; i++) {
            final double h = high.get(i);
            final double l = low.get(i);
            final double c = close.get(i);

            final double trueRange = (i == 0) ? (h - l) : (Math.max(h, prevClose) - Math.min(l, prevClose));

            final double atr;
            final int numCandles = i + 1;
            if (numCandles < atrPeriod) {
                atrAccumulator += trueRange;
                atr = atrAccumulator / numCandles;
            } else if (numCandles == atrPeriod) {
                atrAccumulator += trueRange;
                atrAccumulator /= numCandles;
                atr = atrAccumulator;
            } else {
                atrAccumulator = (atrAccumulator * (atrPeriod - 1) + trueRange) / atrPeriod;
                atr = atrAccumulator;
            }

            final double typicalPrice = (h + l + c) / 3.0;

            maxPrices.add(typicalPrice);
            minPrices.add(typicalPrice);

            longWeights.add(trueRange / atr);
            shortWeights.add(trueRange / atr);

            double longExitWeight = 0;
            double shortExitWeight = 0;
            double totalWeight = 0;
            for (int offset = -maxPrices.size() + 1; offset < 0; offset++) {
                maxPrices.set(offset, Math.max(maxPrices.get(offset), typicalPrice));
                minPrices.set(offset, Math.min(minPrices.get(offset), typicalPrice));

                final double longDrawdown = maxPrices.get(offset) - typicalPrice;
                final double shortDrawdown = typicalPrice - minPrices.get(offset);

                final double longInertia = (longDrawdown < drawdownBound * atr) ? preBoundInertia : postBoundInertia;
                final double shortInertia = (shortDrawdown < drawdownBound * atr) ? preBoundInertia : postBoundInertia;

                final double longWeight = longWeights.get(offset);
                final double shortWeight = shortWeights.get(offset);

                longWeights.set(offset, longWeight * longInertia);
                shortWeights.set(offset, shortWeight * shortInertia);

                longExitWeight += longWeight * (1.0 - longInertia);
                shortExitWeight += shortWeight * (1.0 - shortInertia);
                totalWeight += longWeight + shortWeight;
            }

            exitFlowColumn.set(i, (totalWeight == 0) ? 0 : (longExitWeight - shortExitWeight) / totalWeight);

            prevClose = c;
        }

        return exitFlowColumn;
    }
}
