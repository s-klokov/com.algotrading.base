package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;

/**
 * Вычисление EUDI (exponential up-down index).
 */
public final class Eudi {

    private Eudi() {
    }

    /**
     * Вычислить индикатор EUDI.
     *
     * @param series         временной ряд с колонками {@link FinSeries#O}, {@link FinSeries#H}, {@link FinSeries#L},
     *                       {@link FinSeries#C}
     * @param period         период (экспоненциальной скользящей средней в расчёте на один бар)
     * @param eudiColumnName имя колонки, куда будут записаны значения индикатора EUDI
     * @return колонка со значениями индикатора EUDI
     */
    public static DoubleColumn eudi(final FinSeries series, final int period, final String eudiColumnName) {
        final DoubleColumn eudi = series.acquireDoubleColumn(eudiColumnName);
        if (eudi.length() == 0) {
            return eudi;
        }
        final DoubleColumn open = series.high();
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();

        double prevClose = open.get(0);
        double u = 0;
        double d = 0;
        final int pm = 3 * period + 1;
        final int pp = 3 * period + 3;
        final double r = (double) pm / pp;
        final int len = close.length();
        for (int i = 0; i < len; i++) {
            final double o, h, l, c;
            o = prevClose;
            h = Math.max(o, high.get(i));
            l = Math.min(o, low.get(i));
            c = close.get(i);
            if (o < c) {
                u *= r;
                d = (d * pm + 2 * (o - l)) / pp;
                u = (u * pm + 2 * (h - l)) / pp;
                d *= r;
                u *= r;
                d = (d * pm + 2 * (h - c)) / pp;
            } else if (o > c) {
                u = (u * pm + 2 * (h - o)) / pp;
                d *= r;
                u *= r;
                d = (d * pm + 2 * (h - l)) / pp;
                u = (u * pm + 2 * (c - l)) / pp;
                d *= r;
            } else { // o == c
                // case o < c
                double u1 = u;
                double d1 = d;
                u1 *= r;
                d1 = (d1 * pm + 2 * (o - l)) / pp;
                u1 = (u1 * pm + 2 * (h - l)) / pp;
                d1 *= r;
                u1 *= r;
                d1 = (d1 * pm + 2 * (h - c)) / pp;
                // case o > c
                double u2 = u;
                double d2 = d;
                u2 = (u2 * pm + 2 * (h - o)) / pp;
                d2 *= r;
                u2 *= r;
                d2 = (d2 * pm + 2 * (h - l)) / pp;
                u2 = (u2 * pm + 2 * (c - l)) / pp;
                d2 *= r;
                // average
                u = (u1 + u2) / 2;
                d = (d1 + d2) / 2;
            }
            eudi.set(i, (i < period || u == d || u + d == 0) ? 0 : ((u - d) / (u + d)));
            prevClose = c;
        }
        return eudi;
    }
}
