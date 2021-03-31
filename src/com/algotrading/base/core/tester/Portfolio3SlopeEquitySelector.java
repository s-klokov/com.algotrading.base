package com.algotrading.base.core.tester;

import com.algotrading.base.core.columns.AbstractColumn;
import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.lib.fits.LinearTrendFit;

import java.util.*;

/**
 * Выбор трёх эквити из общей совокупности по критерию лучшего нормированного наклона портфеля.
 */
public class Portfolio3SlopeEquitySelector implements EquitySelector {

    @Override
    public Map<String, Double> selectEquities(final FinSeries equities, final int from, final int to) {
        final int len = to - from;
        final double[] x = new double[len];
        final double[] y = new double[len];
        for (int i = 0; i < len; i++) {
            x[i] = i;
        }
        final List<DoubleColumn> equitiesList = new ArrayList<>();
        for (final AbstractColumn column : equities.columns()) {
            if (column instanceof DoubleColumn) {
                equitiesList.add((DoubleColumn) column);
            }
        }
        double bestSlope = Double.NEGATIVE_INFINITY;
        int best1 = -1;
        int best2 = -1;
        int best3 = -1;
        final int size = equitiesList.size();
        for (int k1 = 0; k1 < size - 2; k1++) {
            final DoubleColumn equity1 = equitiesList.get(k1);
            for (int k2 = k1 + 1; k2 < size - 1; k2++) {
                final DoubleColumn equity2 = equitiesList.get(k2);
                for (int k3 = k2 + 1; k3 < size; k3++) {
                    final DoubleColumn equity3 = equitiesList.get(k3);
                    for (int i = 0; i < len; i++) {
                        final int id = from + i;
                        y[i] = equity1.get(id) + equity2.get(id) + equity3.get(id);
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
                    if (bestSlope < slope) {
                        bestSlope = slope;
                        best1 = k1;
                        best2 = k2;
                        best3 = k3;
                    }
                }
            }
        }
        return (bestSlope <= 0) ? Map.of() : Map.of(
                equitiesList.get(best1).name(), 1.0,
                equitiesList.get(best2).name(), 1.0,
                equitiesList.get(best3).name(), 1.0
        );
    }
}
