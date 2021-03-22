package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.IntColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.sync.TimeframeCouple;

import java.util.concurrent.TimeUnit;

/**
 * Вычисление индикатора Drawdown Reverse Indicator.
 * <p>
 * Параметры: таймфрейм для вычисления ATR, период для вычисления ATR, размер просадки в долях ATR.
 * <p>
 * На указанном таймфрейме вычисляем ATR с указанным периодом. По ценам закрытия исходного временного ряда
 * действует агент по принципу: получил просадку в x * ATR -- перевернулся. На той свече, где происходит
 * переворот, индикатор принимает значения:<br>
 * *) +offset, если offset свечей тому назад был момент, с которого отсчитывается просадка длинной позиции
 * от максимума;<br>
 * *) -offset, если offset свечей тому назад был момент, с которого отсчитывается просадка короткой позиции
 * от минимума.
 */
public final class DrawdownReverseIndicator {

    private DrawdownReverseIndicator() {
    }

    /**
     * Вычислить индикатор Drawdown Reverse Indicator.
     *
     * @param series       временной ряд
     * @param atrTimeframe таймфрейм для вычисления ATR
     * @param atrTimeUnit  единица измерения времени для таймфрейма
     * @param atrPeriod    период индикатора ATR
     * @param drawdown     величина просадки в долях ATR; положительное число
     * @param columnName   имя колонки, куда будет записано значение индикатора
     * @return колонка со значениями индикатора
     */
    public static IntColumn drawdownReverseIndicator(final FinSeries series,
                                                     final int atrTimeframe,
                                                     final TimeUnit atrTimeUnit,
                                                     final int atrPeriod,
                                                     final double drawdown,
                                                     final String columnName) {
        final IntColumn column = series.acquireIntColumn(columnName);
        final int len = column.length();
        if (len == 0) {
            return column;
        }
        final LongColumn timeCode = series.timeCode();
        final DoubleColumn close = series.close();

        final FinSeries atrSeries = series.compressedCandles(atrTimeframe, atrTimeUnit);
        final LongColumn atrTimeCode = atrSeries.timeCode();
        final DoubleColumn atrColumn = Atr.atr(atrSeries, atrPeriod, "ATR");

        final TimeframeCouple timeframeCouple = new TimeframeCouple(timeCode, atrTimeCode);

        int dir = 0;
        double price = close.get(0);
        int priceId = 0;
        for (int i = 0; i < len; i++) {
            final int j = timeframeCouple.getLongId(i);
            final double atr = (j == 0) ? atrColumn.get(0) : atrColumn.get(j - 1);
            final double c = close.get(i);
            if (dir > 0) {
                if (price < c) {
                    price = c;
                    priceId = i;
                    column.set(i, 0);
                } else if (price - c >= drawdown * atr) {
                    column.set(i, i - priceId);
                    price = c;
                    priceId = i;
                    dir = -1;
                } else {
                    column.set(i, 0);
                }
            } else if (dir < 0) {
                if (price > c) {
                    price = c;
                    priceId = i;
                    column.set(i, 0);
                } else if (c - price >= drawdown * atr) {
                    column.set(i, -(i - priceId));
                    price = c;
                    priceId = i;
                    dir = 1;
                } else {
                    column.set(i, 0);
                }
            } else {
                column.set(i, 0);
                if (c >= price + drawdown * atr) {
                    price = c;
                    priceId = i;
                    dir = 1;
                } else if (c <= price - drawdown * atr) {
                    price = c;
                    priceId = i;
                    dir = -1;
                }
            }
        }

        return column;
    }
}
