package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.WindowOfDouble;

/**
 * Вычисление индикатора FI.
 */
@SuppressWarnings("ClassNamingConvention")
public final class Fi {

    private Fi() {
    }

    /**
     * Вычислить индикатор FI.
     *
     * @param series       временной ряд с колонками {@link FinSeries#O}, {@link FinSeries#H}, {@link FinSeries#L},
     *                     {@link FinSeries#C}, {@link FinSeries#V}
     * @param period       период индикатора
     * @param fiColumnName имя колонки, куда будут записаны значения индикатора FI
     * @return колонка со значениями индикатора FI
     */
    public static DoubleColumn fi(final FinSeries series, final int period, final String fiColumnName) {
        final DoubleColumn fi = series.acquireDoubleColumn(fiColumnName);
        if (fi.length() == 0) {
            return fi;
        }
        final DoubleColumn open = series.high();
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        final LongColumn volume = series.volume();

        double prevClose = Double.NaN;
        double sum = 0;
        double sumAbs = 0;
        final WindowOfDouble w = new WindowOfDouble(period);
        final int len = volume.length();
        for (int i = 0; i < len; i++) {
            if (w.isFull()) {
                final double f = w.get(-period + 1);
                sum -= f;
                sumAbs -= Math.abs(f);
            }
            final double v = volume.get(i);
            final double o, h, l, c;
            if (Double.isNaN(prevClose)) {
                o = open.get(i);
                h = high.get(i);
                l = low.get(i);
            } else {
                o = prevClose;
                h = Math.max(o, high.get(i));
                l = Math.min(o, low.get(i));
            }
            c = close.get(i);
            final double s = 2.0 * (h - l) - Math.abs(c - o);
            final double f = (s <= 0) ? 0 : ((c * c - o * o) / 2.0 / s * v);
            w.add(f);
            sum += f;
            sumAbs += Math.abs(f);
            fi.set(i, (sumAbs <= 0) ? 0 : (sum / sumAbs));
            prevClose = c;
        }
        return fi;
    }
}
