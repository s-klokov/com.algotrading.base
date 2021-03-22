package com.algotrading.base.core.tester;

import com.algotrading.base.core.series.FinSeries;

import java.util.Map;

/**
 * Интерфейс для реализации способов выбора лучших эквити в walkforward-тесте.
 */
public interface EquitySelector {

    /**
     * Выбрать из всех имеющихся эквити оптимальные для торговли в диапазоне индексов [from, to).
     *
     * @param equities временной ряд всех эквити с общей временной шкалой
     * @param from     начальный индекс фрагментов эквити
     * @param to       конечный индекс фрагментов эквити
     * @return соответствие идентификаторов выбранных фрагментов эквити и их весов
     */
    Map<String, Double> selectEquities(final FinSeries equities, final int from, final int to);

}
