package com.algotrading.base.core.tester;

import com.algotrading.base.core.columns.AbstractColumn;
import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;

import java.util.*;

/**
 * Выбор эквити по критерию максимума доходности.
 */
public class NetProfitEquitySelector implements EquitySelector {
    /**
     * Количество выбираемых фрагментов эквити.
     */
    private final int count;

    public NetProfitEquitySelector(final int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count=" + count);
        }
        this.count = count;
    }

    @Override
    public Map<String, Double> selectEquities(final FinSeries equities, final int from, final int to) {
        return (count == 1) ? selectBestEquity(equities, from, to) : selectTopEquities(equities, from, to);
    }

    private Map<String, Double> selectBestEquity(final FinSeries equities, final int from, final int to) {
        String bestName = null;
        double bestNetProfit = 0;
        for (final AbstractColumn column : equities.columns()) {
            if (column instanceof final DoubleColumn equity) {
                final double offset = (from == 0) ? 0 : equity.get(from - 1);
                final double netProfit = equity.get(to - 1) - offset;
                if (bestNetProfit < netProfit) {
                    bestNetProfit = netProfit;
                    bestName = column.name();
                }
            }
        }
        return (bestName == null) ? Map.of() : Map.of(bestName, 1.0);
    }

    private Map<String, Double> selectTopEquities(final FinSeries equities, final int from, final int to) {
        List<StringDouble> list = new ArrayList<>();
        for (final AbstractColumn column : equities.columns()) {
            if (column instanceof final DoubleColumn equity) {
                final double offset = (from == 0) ? 0 : equity.get(from - 1);
                final double netProfit = equity.get(to - 1) - offset;
                if (netProfit > 0) {
                    list.add(new StringDouble(column.name(), netProfit));
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

    private static class StringDouble {
        final String s;
        final double d;

        StringDouble(final String s, final double d) {
            this.s = s;
            this.d = d;
        }
    }
}
