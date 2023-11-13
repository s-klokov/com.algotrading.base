package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.IntColumn;
import com.algotrading.base.core.series.FinSeries;

/**
 * Разметка минимумов и максимумов на свечном графике.
 * <p>
 * Параметры:<br>
 * 1) период ATR для оценки средних размеров свечей;<br>
 * 2) минимальный размер "безоткатного" движения цены.
 * <p>
 * В процессе расчёта вычисляется индикатор ATR с указанным периодом
 * и значения индикаторов минимума и максимума, каждый из которых
 * имеет смысл количества свечей тому назад, когда наблюдался ближайший
 * минимум или максимум.
 */
public class MinMaxIndicator {

    private MinMaxIndicator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Вычислить значения индикаторов ATR и MinMax.
     * <p>
     * Индикатор минимума показывает номер свечи, на которой наблюдался предыдущий минимум.
     * Индикатор максимума определяется аналогично.
     *
     * @param series             временной ряд O, H, L, C
     * @param atrPeriod          период индикатора ATR
     * @param atrColumnName      название колонки для индикатора ATR
     * @param priceMovementInAtr размер "безоткатного" движения в долях ATR
     * @param minColumnName      название колонки для индикатора минимума
     * @param maxColumnName      название колонки для индикатора максимума
     */
    public static void minMaxIndicator(final FinSeries series,
                                       final int atrPeriod,
                                       final String atrColumnName,
                                       final double priceMovementInAtr,
                                       final String minColumnName,
                                       final String maxColumnName) {
        final DoubleColumn atrColumn = series.acquireDoubleColumn(atrColumnName, () -> Atr.atr(series, atrPeriod, atrColumnName));

        IntColumn minColumn = series.getIntColumn(minColumnName);
        IntColumn maxColumn = series.getIntColumn(maxColumnName);
        if (minColumn != null && maxColumn != null) {
            return;
        }
        minColumn = series.acquireIntColumn(minColumnName);
        maxColumn = series.acquireIntColumn(maxColumnName);

        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();

        final State state = new State(series.open().get(0));

        final int len = close.length();
        for (int i = 0; i < len; i++) {
            final double h = high.get(i);
            final double l = low.get(i);
            final double c = close.get(i);
            state.priceMovement = priceMovementInAtr * atrColumn.get(i);

            if (c >= state.price) {
                state.update(i, l);
                state.update(i, h);
            } else {
                state.update(i, h);
                state.update(i, l);
            }
            state.update(i, c);
            minColumn.set(i, state.prevMinId);
            maxColumn.set(i, state.prevMaxId);
        }
    }

    private static class State {
        int direction = 0;
        int prevMinId = -1;
        int prevMaxId = -1;
        int currMinId = 0;
        int currMaxId = 0;
        double currMin;
        double currMax;
        double price;
        double priceMovement = Double.NaN;

        State(final double price) {
            currMin = currMax = price;
            this.price = price;
        }

        void update(final int id, final double price) {
            if (direction == 1) {
                if (price > currMax) {
                    currMax = price;
                    currMaxId = id;
                } else if (price <= currMax - priceMovement) {
                    direction = -1;
                    prevMaxId = currMaxId;
                    currMaxId = -1;
                    currMax = Double.NaN;
                    currMin = price;
                    currMinId = id;
                }
            } else if (direction == -1) {
                if (price < currMin) {
                    currMin = price;
                    currMinId = id;
                } else if (price >= currMin + priceMovement) {
                    direction = 1;
                    prevMinId = currMinId;
                    currMinId = -1;
                    currMin = Double.NaN;
                    currMax = price;
                    currMaxId = id;
                }
            } else {
                if (price > currMax) {
                    currMax = price;
                    currMaxId = id;
                } else if (price < currMin) {
                    currMin = price;
                    currMinId = id;
                }
                if (price >= currMin + priceMovement) {
                    direction = 1;
                    prevMinId = currMinId;
                    currMinId = -1;
                    currMin = Double.NaN;
                    currMax = price;
                    currMaxId = id;
                }
                if (price <= currMax - priceMovement) {
                    direction = -1;
                    prevMaxId = currMaxId;
                    currMaxId = -1;
                    currMax = Double.NaN;
                    currMin = price;
                    currMinId = id;
                }
            }
            this.price = price;
        }
    }
}
