package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.FinSeries;

/**
 * Вычисление индикатора SCTR (StockCharts Technical Rank).
 * См. http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:sctr
 */
public final class Sctr {

    public static final double LONG_TERM_WEIGHT = 0.3;
    public static final double MEDIUM_TERM_WEIGHT = 0.15;
    public static final double SHORT_TERM_WEIGHT = 0.05;
    public static final int SHORT_TERM_SLOPE_PERIOD = 3;

    private Sctr() {
    }

    /**
     * Вычислить индикатор SCTR.
     * <p>
     * В исходной методике используются:<br>
     * * долгосрочный процент превышения 200-дневной EMA (30%);<br>
     * * долгосрочный 125-дневный ROC (30%);<br>
     * * среднесрочный процент превышения 50-дневной EMA (15%);<br>
     * * среднесрочный 20-дневный ROC (15%);<br>
     * * краткорочный вклад наклона 3-дневной гистограммы PPO-индикатора с параметрами (12, 26) и 9-дневным EMA (5%);<br>
     * * краткосрочный RSI с 14-дневный периодом (5%).
     *
     * @param series                    временной ряд, содержащий колонку {@link FinSeries#C} и другие колонки,
     *                                  которые заданы в качестве аргументов этого метода
     * @param longTermEmaColumn         колонка, содержащая долгосрочное экспоненциальное среднее цены
     * @param longTermEmaWeight         вес долгосрочного экспоненциального среднего цены в ранге
     * @param longTermRocColumn         колонка, содержащая долгосрочное значение ROC цены
     * @param longTermRocWeight         вес долгосрочного значения ROC цены
     * @param mediumTermEmaColumn       колонка, содержащая среднесрочное экспоненциальное среднее цены
     * @param mediumTermEmaWeight       вес среднесрочного экспоненциального среднего цены
     * @param mediumTermRocColumn       колонка, содержащая среднесрочное значение ROC цены
     * @param mediumTermRocWeight       вес среднесрочного значения ROC цены
     * @param shortTermPpoLineColumn    колонка, содержащая краткосрочное значение PPO-линии цены
     * @param shortTermSignalLineColumn колонка, содержащая краткосрочное значение сигнальной линии цены
     * @param shortTermSlopePeriod      число столбцов краткосрочной PPO-гистограммы для оценки наклона
     * @param shortTermPpoWeight        вес краткосрочного значения PPO цены
     * @param shortTermRsiColumn        колонка, содержащая краткосрочное значение RSI цены
     * @param shortTermRsiWeight        вес краткосрочного значения RSI цены
     * @param sctrColumnName            название колонки, куда будет записано значение индикатора SCTR
     * @return колонка, содержащая значения индикатора SCTR
     */
    public static DoubleColumn sctr(final FinSeries series,
                                    final DoubleColumn longTermEmaColumn,
                                    final double longTermEmaWeight,
                                    final DoubleColumn longTermRocColumn,
                                    final double longTermRocWeight,
                                    final DoubleColumn mediumTermEmaColumn,
                                    final double mediumTermEmaWeight,
                                    final DoubleColumn mediumTermRocColumn,
                                    final double mediumTermRocWeight,
                                    final DoubleColumn shortTermPpoLineColumn,
                                    final DoubleColumn shortTermSignalLineColumn,
                                    final int shortTermSlopePeriod,
                                    final double shortTermPpoWeight,
                                    final DoubleColumn shortTermRsiColumn,
                                    final double shortTermRsiWeight,
                                    final String sctrColumnName) {
        final double totalWeight = longTermEmaWeight + longTermRocWeight
                                   + mediumTermEmaWeight + mediumTermRocWeight
                                   + shortTermPpoWeight + shortTermRsiWeight;
        final DoubleColumn sctrColumn = series.acquireDoubleColumn(sctrColumnName);
        final DoubleColumn close = series.close();
        final int len = close.length();
        for (int i = 0; i < shortTermSlopePeriod; i++) {
            sctrColumn.set(i, Double.NaN);
        }
        for (int i = shortTermSlopePeriod; i < len; i++) {
            final double price = close.get(i);
            final double slope = (shortTermPpoLineColumn.get(i)
                                  - shortTermSignalLineColumn.get(i)
                                  - shortTermPpoLineColumn.get(i - shortTermSlopePeriod)
                                  + shortTermSignalLineColumn.get(i - shortTermSlopePeriod))
                                 / shortTermSlopePeriod;
            double score = longTermEmaWeight * (price / longTermEmaColumn.get(i) - 1) * 100
                           + longTermRocWeight * longTermRocColumn.get(i)
                           + mediumTermEmaWeight * (price / mediumTermEmaColumn.get(i) - 1) * 100
                           + mediumTermRocWeight * mediumTermRocColumn.get(i)
                           + shortTermPpoWeight * slopeScore(slope)
                           + shortTermRsiWeight * shortTermRsiColumn.get(i);
            score /= totalWeight;
            sctrColumn.set(i, score);
        }
        return sctrColumn;
    }

    private static double slopeScore(final double x) {
        return (x >= 1) ? 100 : (x <= -1) ? 0 : (x + 1) * 50;
    }
}
