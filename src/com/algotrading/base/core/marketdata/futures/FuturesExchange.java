package com.algotrading.base.core.marketdata.futures;

import java.util.HashMap;
import java.util.Map;

/**
 * Информация о фьючерсах на бирже.
 */
public abstract class FuturesExchange {

    /**
     * Соответствие: префикс фьючерса -> соответствие: короткий код -> объект, описывающий фьючерс.
     */
    public final Map<String, Map<String, Futures>> prefixMap = new HashMap<>();

    /**
     * Соответствие: короткий код -> объект, описывающий фьючерс.
     */
    public final Map<String, Futures> futuresMap = new HashMap<>();

    public final void addFuturesMap(final Map<String, Futures> futuresMap) {
        if (!futuresMap.isEmpty()) {
            for (final Futures f : futuresMap.values()) {
                prefixMap.put(f.prefix, futuresMap);
                break;
            }
            this.futuresMap.putAll(futuresMap);
        }
    }

    /**
     * Получить соответствие: короткий код -> объект, описывающий фьючерс.
     *
     * @param prefix префикс
     * @return соответствие
     */
    public Map<String, Futures> byPrefix(final String prefix) {
        return prefixMap.get(prefix);
    }

    /**
     * Получить объект, описывающий фьючерс.
     *
     * @param shortCode короткий код
     * @return объект, описывающий фьючерс
     */
    public Futures byShortCode(final String shortCode) {
        return futuresMap.get(shortCode);
    }
}
