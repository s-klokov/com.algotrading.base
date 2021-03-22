package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.Window;

/**
 * Вычисление индикатора баланса открытых лонгов/шортов.
 */
public final class LongShortBalanceIndex {

    private LongShortBalanceIndex() {
    }

    /**
     * Вычислить значение индикатора.
     *
     * @param series           временной ряд с колонками {@link FinSeries#H}, {@link FinSeries#L}, {@link FinSeries#C},
     *                         {@link FinSeries#V}
     * @param takeProfitPeriod количество баров, после которых закрываются прибыльные позиции
     * @param stopLossPeriod   количество баров, после которых закрываются убыточные позиции
     * @param lsbiColumnName   имя колонки, куда будут записаны значения индикатора
     * @return колонка со значениями индикатора
     */
    public static DoubleColumn longShortBalanceIndex(final FinSeries series,
                                                     final int takeProfitPeriod,
                                                     final int stopLossPeriod,
                                                     final String lsbiColumnName) {
        if (takeProfitPeriod <= 0 || takeProfitPeriod >= stopLossPeriod) {
            throw new IllegalArgumentException("takeProfitPeriod=" + takeProfitPeriod
                                               + ", stopLossPeriod=" + stopLossPeriod);
        }
        final DoubleColumn iColumn = series.acquireDoubleColumn(lsbiColumnName);
        if (iColumn.length() == 0) {
            return iColumn;
        }
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        final LongColumn volume = series.volume();
        final int len = close.length();
        final Window<TradeInfo> tradeInfoWindow = new Window<>(stopLossPeriod);
        for (int i = 0; i < len; i++) {
            final double currPrice = (high.get(i) + low.get(i) + close.get(i)) / 3;
            // Выход из трейдов и вычисление объёмов в оставшихся трейдах
            double vLong = 0;
            double vShort = 0;
            for (final TradeInfo tradeInfo : tradeInfoWindow) {
                final int barsInTrade = i - tradeInfo.id;
                if (barsInTrade >= stopLossPeriod) {
                    tradeInfo.volumeLong = 0;
                    tradeInfo.volumeShort = 0;
                } else if (barsInTrade >= takeProfitPeriod) {
                    final double enterPrice = tradeInfo.price;
                    if (enterPrice < currPrice) {
                        tradeInfo.volumeLong = 0;
                    } else if (enterPrice > currPrice) {
                        tradeInfo.volumeShort = 0;
                    }
                }
                vLong += tradeInfo.volumeLong;
                vShort += tradeInfo.volumeShort;
            }
            double vSum = vLong + vShort;
            // Вход в новые трейды
            final double v = volume.get(i);
            if (vSum <= 0) {
                final double vHalf = v / 2;
                tradeInfoWindow.add(new TradeInfo(i, currPrice, vHalf, vHalf));
                vLong = vHalf;
                vShort = vHalf;
                vSum = v;
            } else {
                final double vL = vShort / vSum * v;
                final double vS = vLong / vSum * v;
                tradeInfoWindow.add(new TradeInfo(i, currPrice, vL, vS));
                vLong += vL;
                vShort += vS;
                vSum += (vL + vS);
            }
            iColumn.set(i, vSum <= 0 ? 0 : (vLong - vShort) / vSum);
        }
        return iColumn;
    }

    private static class TradeInfo {
        final int id;
        final double price;
        double volumeLong;
        double volumeShort;

        TradeInfo(final int id, final double price, final double volumeLong, final double volumeShort) {
            this.id = id;
            this.price = price;
            this.volumeLong = volumeLong;
            this.volumeShort = volumeShort;
        }
    }
}
