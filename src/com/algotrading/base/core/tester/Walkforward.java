package com.algotrading.base.core.tester;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Реализация walkforward-теста:<br>
 * 1) создать объект;<br>
 * 2) добавить эквити, полученные в результате оптимизации (переборе параметров) по общему временному окну,
 * используя метод {@link #addEquity(String, LongColumn, DoubleColumn)};<br>
 * 3) вызвать метод {@link #walkForward(List, EquitySelector)};<br>
 * 4) получить эквити walkforward-теста методом {@link #getWalkforwardEquity()} и описание процесса
 * методом {@link #getDescriptions()}.
 */
public class Walkforward {

    public final FinSeries allEquities = new FinSeries();
    private final FinSeries walkforwardEquity = new FinSeries().withLongColumn(FinSeries.T).withDoubleColumn(Tester.EQUITY);
    private final List<String> descriptions = new ArrayList<>();

    /**
     * Добавить эквити в список всех эквити для walkforward-теста.
     *
     * @param equityId идентификатор эквити
     * @param timeCode колонка с метками времени
     * @param equity   колонка с эквити
     */
    public void addEquity(final String equityId, final LongColumn timeCode, final DoubleColumn equity) {
        if (timeCode.length() != equity.length()) {
            throw new IllegalArgumentException("TimeCode length = " + timeCode.length()
                                               + " != "
                                               + equity.length() + " = Equity length");
        }
        if (allEquities.getDoubleColumn(equityId) != null) {
            throw new IllegalArgumentException("Duplicate equity " + equityId);
        }
        if (allEquities.timeCode() == null) {
            final LongColumn tColumn = allEquities.acquireLongColumn(FinSeries.T);
            final DoubleColumn eColumn = allEquities.acquireDoubleColumn(equityId);
            tColumn.append(timeCode);
            eColumn.append(equity);
        } else {
            final LongColumn tColumn = allEquities.timeCode();
            final DoubleColumn eColumn = allEquities.acquireDoubleColumn(equityId);
            if (eColumn.length() != equity.length()) {
                throw new IllegalArgumentException("Incorrect equity length = " + equity.length() + ", expected " + eColumn.length());
            }
            for (int i = 0; i < eColumn.length(); i++) {
                if (tColumn.get(i) != timeCode.get(i)) {
                    throw new IllegalArgumentException("TimeCode mismatch: index = " + i);
                }
                eColumn.set(i, equity.get(i));
            }
        }
    }

    /**
     * Провести walkforward-тест
     *
     * @param walkforwardIndices описание промежутков времени для оптимизации и тестирования
     * @param equitySelector     способ выбора лучших эквити
     */
    public void walkForward(final List<WalkforwardIndices> walkforwardIndices,
                            final EquitySelector equitySelector) {
        walkforwardEquity.setLength(0);
        descriptions.clear();

        final LongColumn timeCode = allEquities.timeCode();
        double lastEquity = 0;

        final LongColumn totalTimeCode = walkforwardEquity.timeCode();
        final DoubleColumn totalEquityColumn = walkforwardEquity.getDoubleColumn(Tester.EQUITY);

        for (final WalkforwardIndices wfi : walkforwardIndices) {
            descriptions.add("Optimization period: "
                             + TimeCodes.timeCodeStringHHMMSS(timeCode.get(wfi.optFrom))
                             + " -- "
                             + TimeCodes.timeCodeStringHHMMSS(timeCode.get(wfi.optTo - 1)));
            final Map<String, Double> selected = equitySelector.selectEquities(allEquities, wfi.optFrom, wfi.optTo);
            descriptions.add("Equities selected: " + selected.size());
            double totalWeight = 0;
            for (final Map.Entry<String, Double> e : selected.entrySet()) {
                descriptions.add(e.getKey() + ", weight=" + e.getValue());
                totalWeight += e.getValue();
            }
            descriptions.add("Trading period: "
                             + TimeCodes.timeCodeStringHHMMSS(timeCode.get(wfi.tradeFrom))
                             + " -- "
                             + TimeCodes.timeCodeStringHHMMSS(timeCode.get(wfi.tradeTo - 1)));
            if (totalWeight == 0) {
                for (int i = wfi.tradeFrom; i < wfi.tradeTo; i++) {
                    totalTimeCode.append(timeCode.get(i));
                    totalEquityColumn.append(lastEquity);
                }
            } else {
                for (int i = wfi.tradeFrom; i < wfi.tradeTo; i++) {
                    totalTimeCode.append(timeCode.get(i));
                    double totalEquity = 0;
                    for (final Map.Entry<String, Double> e : selected.entrySet()) {
                        final DoubleColumn equityColumn = allEquities.getDoubleColumn(e.getKey());
                        final double deltaEquity;
                        if (wfi.tradeFrom == 0) {
                            deltaEquity = equityColumn.get(i);
                        } else {
                            deltaEquity = equityColumn.get(i) - equityColumn.get(wfi.tradeFrom - 1);
                        }
                        totalEquity += deltaEquity * e.getValue();
                    }
                    totalEquity /= totalWeight;
                    totalEquityColumn.append(lastEquity + totalEquity);
                }
                lastEquity = totalEquityColumn.getLast();
            }
            descriptions.add("Cumulative equity: " + lastEquity);
            descriptions.add("");
        }
    }

    /**
     * @return колонка с метками времени;
     * доступна после вызова методов {{@link #addEquity(String, LongColumn, DoubleColumn)}}
     */
    public LongColumn timeCode() {
        return allEquities.timeCode();
    }

    /**
     * @return временной ряд с эквити walkforward-теста;
     * доступен после вызова метода {@link #walkForward(List, EquitySelector)}
     */
    public FinSeries getWalkforwardEquity() {
        return walkforwardEquity.copy();
    }

    /**
     * @return список строк с описанием выбранных эквити для каждого промежутка тестирования;
     * доступен после вызова метода {@link #walkForward(List, EquitySelector)}
     */
    public List<String> getDescriptions() {
        return Collections.unmodifiableList(descriptions);
    }
}
