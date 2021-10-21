package com.algotrading.base.core.tester;

import com.algotrading.base.core.columns.AbstractColumn;
import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.lib.fits.LinearTrendFit;

import java.util.*;

/**
 * Выбор эквити по критерию лучшего нормированного наклона.
 */
public class SlopeEquitySelector implements EquitySelector {
    /**
     * Количество выбираемых фрагментов эквити.
     */
    private final int count;

    public SlopeEquitySelector(final int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count=" + count);
        }
        this.count = count;
    }

    @Override
    public Map<String, Double> selectEquities(final FinSeries equities, final int from, final int to) {
        final int len = to - from;
        final double[] x = new double[len];
        final double[] y = new double[len];
        for (int i = 0; i < len; i++) {
            x[i] = i;
        }
        List<StringDouble> list = new ArrayList<>();
        for (final AbstractColumn column : equities.columns()) {
            if (column instanceof final DoubleColumn equity) {
                for (int i = 0; i < len; i++) {
                    y[i] = equity.get(from + i);
                }
                final LinearTrendFit fit = LinearTrendFit.fit(x, y);
                final double a = fit.a;
                final double b = fit.b;
                double sum = 0;
                for (int i = 0; i < len; i++) {
                    sum += Math.abs(y[i] - a - b * x[i]);
                }
                final double w = sum / len;
                final double slope = len * b / w;
                if (slope > 0) {
                    list.add(new StringDouble(column.name(), slope));
                }
            }
        }
        if (list.size() > count) {
            list.sort(Comparator.comparingDouble(o -> -o.d));
            list = list.subList(0, count);
        }
        final Map<String, Double> map = new HashMap<>(list.size());
        list.forEach(stringDouble -> map.put(stringDouble.s, 1.0));
        return map;
    }

    private record StringDouble(String s, double d) {
    }
}
