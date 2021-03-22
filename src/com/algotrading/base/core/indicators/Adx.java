package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;

/**
 * Вычисление индикатора ADX.
 */
public final class Adx {

    private Adx() {
    }

    /**
     * Вычислить индикатор ADX.
     *
     * @param series        временной ряд с колонками {@link FinSeries#H}, {@link FinSeries#L}, {@link FinSeries#C}
     * @param period        период
     * @param pdiColumnName название колонки, куда будет записано значение +DX
     * @param ndiColumnName название колонки, куда будет записано значение -DX
     * @param adxColumnName название колонки, куда будет записано значение ADX
     * @return колонка со значениями ADX
     */
    public static DoubleColumn adx(final FinSeries series, final int period,
                                   final String pdiColumnName,
                                   final String ndiColumnName,
                                   final String adxColumnName) {
        final int len = series.length();
        final DoubleColumn pdiColumn = series.acquireDoubleColumn(pdiColumnName);
        final DoubleColumn ndiColumn = series.acquireDoubleColumn(ndiColumnName);
        final DoubleColumn adxColumn = series.acquireDoubleColumn(adxColumnName);
        if (len == 0) {
            return adxColumn;
        }

        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();

        final double alpha = 2.0 / (period + 1);
        final double beta = 1.0 - alpha;
        double pdi = 0;
        double ndi = 0;
        double adx = 0;

        double hPrev = high.get(0);
        double lPrev = low.get(0);
        double cPrev = close.get(0);

        pdiColumn.set(0, 0);
        ndiColumn.set(0, 0);
        adxColumn.set(0, 0);

        for (int i = 1; i < len; i++) {
            final double h = high.get(i);
            final double l = low.get(i);
            final double c = close.get(i);
            final double trueRange = Math.max(h, cPrev) - Math.min(l, cPrev);
            double pdm = Math.max(0, h - hPrev);
            double ndm = Math.max(0, lPrev - l);
            if (pdm > ndm) {
                ndm = 0;
            } else if (pdm < ndm) {
                pdm = 0;
            } else {
                pdm = ndm = 0;
            }
            final double psdi = (trueRange <= 0) ? 0 : (pdm / trueRange * 100);
            final double nsdi = (trueRange <= 0) ? 0 : (ndm / trueRange * 100);

            pdi = beta * pdi + alpha * psdi;
            ndi = beta * ndi + alpha * nsdi;

            pdiColumn.set(i, pdi);
            ndiColumn.set(i, ndi);

            final double dxi = (pdi + ndi <= 0) ? 0 : (Math.abs(pdi - ndi) / (pdi + ndi) * 100);

            adx = beta * adx + alpha * dxi;
            adxColumn.set(i, adx);

            hPrev = h;
            lPrev = l;
            cPrev = c;
        }

        return adxColumn;
    }
}
