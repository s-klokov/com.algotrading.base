package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.WindowOfDouble;

/**
 * Вычисление индикатора MFI.
 */
public final class Mfi {

    private Mfi() {
    }

    /**
     * Вычислить значение индикатора MFI.
     *
     * @param series        временной ряд с колонками {@link FinSeries#H}, {@link FinSeries#L}, {@link FinSeries#C},
     *                      {@link FinSeries#V}
     * @param period        период индикатора
     * @param mfiColumnName имя колонки, куда будут записаны значения индикатора MFI
     * @return колонка со значениями индикатора MFI
     */
    public static DoubleColumn mfi(final FinSeries series, final int period, final String mfiColumnName) {
        final DoubleColumn mfi = series.acquireDoubleColumn(mfiColumnName);
        if (mfi.length() == 0) {
            return mfi;
        }
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        final LongColumn volume = series.volume();
        final WindowOfDouble moneyFlowWindow = new WindowOfDouble(period);
        double positiveFlow = 0;
        double negativeFlow = 0;
        mfi.set(0, 50);
        double prevTypicalPrice = (high.get(0) + low.get(0) + close.get(0)) / 3;
        for (int i = 1; i < mfi.length(); i++) {
            if (moneyFlowWindow.isFull()) {
                final double flow = moneyFlowWindow.get(-period + 1);
                if (flow > 0) {
                    positiveFlow -= flow;
                } else {
                    negativeFlow -= flow;
                }
            }
            final double currTypicalPrice = (high.get(i) + low.get(i) + close.get(i)) / 3;
            final double flow;
            if (currTypicalPrice > prevTypicalPrice * 1.0000001) {
                flow = currTypicalPrice * volume.get(i);
            } else if (currTypicalPrice < prevTypicalPrice * 0.9999999) {
                flow = -currTypicalPrice * volume.get(i);
            } else {
                flow = 0;
            }
            moneyFlowWindow.add(flow);
            if (flow > 0) {
                positiveFlow += flow;
            } else if (flow < 0) {
                negativeFlow += flow;
            }
            if (moneyFlowWindow.isFull() && positiveFlow != negativeFlow) {
                mfi.set(i, positiveFlow / (positiveFlow - negativeFlow) * 100);
            } else {
                mfi.set(i, 50);
            }
            prevTypicalPrice = currTypicalPrice;
        }
        return mfi;
    }
}
