package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.series.Series;

/**
 * Вычисление индикатора PPO (price oscillator indicator).
 * См. описание https://www.tradingview.com/wiki/Price_Oscillator_(PPO)
 */
public final class Ppo {

    private Ppo() {
    }

    /**
     * Вычислить PPO-линию и сигнальную линию индикатора PPO.
     *
     * @param series               временной ряд
     * @param shortEmaColumn       колонка данных со скользящими средними цены короткого периода (обычно, EMA(12))
     * @param longEmaColumn        колонка данных со скользящими средними цены длинного периода (обычно, EMA(26))
     * @param signalPeriod         период сглаживания для получения сигнальной линии (обычно это значение равно 9)
     * @param ppoLineColumnName    название колонки, куда будут записаны значения PPO линии
     * @param signalLineColumnName название колонки, куда будут записаны значения сигнальной линии
     */
    public static void ppo(final Series series,
                           final DoubleColumn shortEmaColumn,
                           final DoubleColumn longEmaColumn,
                           final int signalPeriod,
                           final String ppoLineColumnName,
                           final String signalLineColumnName) {
        final DoubleColumn ppoLineColumn = series.acquireDoubleColumn(ppoLineColumnName);
        final int len = ppoLineColumn.length();
        for (int i = 0; i < len; i++) {
            ppoLineColumn.set(i, (shortEmaColumn.get(i) / longEmaColumn.get(i) - 1) * 100);
        }
        Ema.ema(series, ppoLineColumn, signalPeriod, signalLineColumnName);
    }
}
