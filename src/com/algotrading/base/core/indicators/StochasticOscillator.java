package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.Window;

/**
 * Вычисление индикатора Stochastic Oscillator.
 */
public final class StochasticOscillator {

    private StochasticOscillator() {
    }

    /**
     * Вычисление индикатора Stochastic Oscillator.
     *
     * @param series      временной ряд с колонками {@link FinSeries#H}, {@link FinSeries#L}, {@link FinSeries#C}
     * @param periodK     период, на котором вычисляются значения максимумов и минимумов
     * @param maK         период сглаживания для %K
     * @param maD         период сглаживания для %D
     * @param columnNameK имя колонки, куда будут записаны значения индикатора %K
     * @param columnNameD имя колонки, куда будут записаны значения индикатора %D
     */
    public static void stochasticOscillator(final FinSeries series,
                                            final int periodK,
                                            final int maK,
                                            final int maD,
                                            final String columnNameK,
                                            final String columnNameD) {
        if (periodK < 1 || maK < 1 || maD < 1) {
            throw new IllegalArgumentException("Invalid periods:" + periodK + "," + maK + "," + maD);
        }
        final int len = series.length();
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        final DoubleColumn columnK = series.acquireDoubleColumn(columnNameK);
        final Window<HLC> windowPeriodK = new Window<>(periodK);
        final Window<HLC> windowMaD = new Window<>(maK);
        double hhv = Double.NEGATIVE_INFINITY;
        double llv = Double.POSITIVE_INFINITY;
        double sumCL = 0;
        double sumHL = 0;
        for (int i = 0; i < len; i++) {
            if (windowPeriodK.isFull()) {
                HLC hlc = windowPeriodK.get(-periodK + 1);
                if (hlc.h == hhv || hlc.l == llv) {
                    hhv = Double.NEGATIVE_INFINITY;
                    llv = Double.POSITIVE_INFINITY;
                    for (int j = 0; j > -periodK + 1; j--) {
                        hlc = windowPeriodK.get(j);
                        hhv = Math.max(hhv, hlc.h);
                        llv = Math.min(llv, hlc.l);
                    }
                }
            }
            final double h = high.get(i);
            final double l = low.get(i);
            final double c = close.get(i);
            hhv = Math.max(hhv, h);
            llv = Math.min(llv, l);
            windowPeriodK.add(new HLC(h, l, c));
            if (windowMaD.isFull()) {
                final HLC hlc = windowMaD.get(-maD + 1);
                sumCL -= hlc.c - hlc.l;
                sumHL -= hlc.h - hlc.l;
            }
            sumCL += c - llv;
            sumHL += hhv - llv;
            windowMaD.add(new HLC(hhv, llv, c));
            columnK.set(i, (sumHL <= 0) ? 50.0 : (100.0 * sumCL / sumHL));
        }
        Ma.ma(series, columnNameK, maD, columnNameD);
    }

    private static class HLC {
        final double h;
        final double l;
        final double c;

        HLC(final double h, final double l, final double c) {
            this.h = h;
            this.l = l;
            this.c = c;
        }
    }
}
