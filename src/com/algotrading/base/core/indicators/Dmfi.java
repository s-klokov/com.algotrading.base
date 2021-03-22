package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.WindowOfDouble;

/**
 * Вычисление индикатора DMFI (double money flow index).
 */
public final class Dmfi {

    private Dmfi() {
    }

    /**
     * Вычислить значение индикатора DMFI.
     *
     * @param series         временной ряд с колонками {@link FinSeries#H}, {@link FinSeries#L}, {@link FinSeries#C},
     *                       {@link FinSeries#V}
     * @param period         период индикатора (не менее 2)
     * @param weight         вес при повторном использовании приращения
     * @param dmfiColumnName имя колонки, куда будут записаны значения индикатора DMFI
     * @return колонка со значениями индикатора DMFI
     */
    public static DoubleColumn dmfi(final FinSeries series, final int period, final double weight, final String dmfiColumnName) {
        final DoubleColumn dmfi = series.acquireDoubleColumn(dmfiColumnName);
        if (dmfi.length() == 0) {
            return dmfi;
        }
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        final LongColumn volume = series.volume();
        final WindowOfDouble moneyFlowWindow = new WindowOfDouble(period);
        double pFlow = 0;
        double nFlow = 0;
        double ppFlow = 0;
        double pnFlow = 0;
        double npFlow = 0;
        double nnFlow = 0;
        dmfi.set(0, 50);
        double prevTypicalPrice = (high.get(0) + low.get(0) + close.get(0)) / 3;
        for (int i = 1; i < dmfi.length(); i++) {
            if (moneyFlowWindow.isFull()) {
                final double flow1 = moneyFlowWindow.get(-period + 1);
                final double flow2 = moneyFlowWindow.get(-period + 2);
                if (flow1 > 0) {
                    pFlow -= flow1;
                    if (flow2 > 0) {
                        ppFlow -= flow1;
                    } else if (flow2 < 0) {
                        pnFlow -= flow1;
                    }
                } else if (flow1 < 0) {
                    nFlow -= flow1;
                    if (flow2 > 0) {
                        npFlow -= flow1;
                    } else if (flow2 < 0) {
                        nnFlow -= flow1;
                    }
                }
            }
            final double currTypicalPrice = (high.get(i) + low.get(i) + close.get(i)) / 3;
            final double flow1 = (moneyFlowWindow.size() == 0) ? 0 : moneyFlowWindow.get(0);
            final double flow2;
            if (currTypicalPrice > prevTypicalPrice * 1.0000001) {
                flow2 = currTypicalPrice * volume.get(i);
            } else if (currTypicalPrice < prevTypicalPrice * 0.9999999) {
                flow2 = -currTypicalPrice * volume.get(i);
            } else {
                flow2 = 0;
            }
            moneyFlowWindow.add(flow2);
            if (flow2 > 0) {
                pFlow += flow2;
                if (flow1 > 0) {
                    ppFlow += flow1;
                } else if (flow1 < 0) {
                    npFlow += flow1;
                }
            } else if (flow2 < 0) {
                nFlow += flow2;
                if (flow1 > 0) {
                    pnFlow += flow1;
                } else if (flow1 < 0) {
                    nnFlow += flow1;
                }
            }
            if (moneyFlowWindow.isFull()) {
                final double positive = pFlow + weight * (ppFlow - pnFlow);
                final double denumerator = pFlow - nFlow + weight * Math.abs(ppFlow - pnFlow - nnFlow + npFlow);
                if (denumerator > 0) {
                    dmfi.set(i, positive / denumerator * 100);
                } else {
                    dmfi.set(i, 50);
                }
            } else {
                dmfi.set(i, 50);
            }
            prevTypicalPrice = currTypicalPrice;
        }
        return dmfi;
    }
}
