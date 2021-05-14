package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.WindowOfDouble;

import static com.algotrading.base.core.indicators.HvLv.hv;
import static com.algotrading.base.core.indicators.HvLv.lv;

/**
 * Вычисление индикатора CandlesArea, являющегося аппроксимацией площади свечного графика в окне терминала.
 */
public final class CandlesArea {

    private CandlesArea() {
    }

    /**
     * Вычислить индикатор, пропорциональный площади, занимаемой свечами на экране.
     * <p>
     * Формула для вычисления:<br>
     * sum(H - L + |O - C|, period) / (HHV(period) - LLV(period))
     * <p>
     * При вычислении дополнительно создаются колонки HHV(H, period) и LLV(L, period),
     * заполненными соответствующими значениями индикаторов HHV и LLV.
     *
     * @param series        временной ряд с колонками O, H, L, C
     * @param period        период
     * @param columnName    имя колонки, куда будут записаны значения индикатора candlesArea
     * @param hhvColumnName имя колонки, куда будут записаны значения индикатора HHV(H, period)
     * @param llvColumnName имя колонки, куда будут записаны значения индикатора LLV(L, period)
     * @return колонка со значениями индикатора candlesArea
     */
    public static DoubleColumn candlesArea(final FinSeries series, final int period,
                                           final String columnName,
                                           final String hhvColumnName, final String llvColumnName) {
        final DoubleColumn candlesAreaColumn = series.acquireDoubleColumn(columnName);
        final DoubleColumn hhvColumn = hv(series, series.high(), period, hhvColumnName);
        final DoubleColumn llvColumn = lv(series, series.low(), period, llvColumnName);
        final DoubleColumn open = series.open();
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        final WindowOfDouble window = new WindowOfDouble(period);
        double sum = 0;
        final int len = close.length();
        for (int i = 0; i < len; i++) {
            if (window.isFull()) {
                sum -= window.get(-period + 1);
            }
            final double v = (high.get(i) - low.get(i) + Math.abs(open.get(i) - close.get(i))) / 2.0;
            sum += v;
            window.add(v);
            final double width = hhvColumn.get(i) - llvColumn.get(i);
            if (width == 0) {
                candlesAreaColumn.set(i, 0);
            } else {
                candlesAreaColumn.set(i, sum / (window.size() * width));
            }
        }
        return candlesAreaColumn;
    }
}
