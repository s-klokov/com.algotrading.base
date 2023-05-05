package com.algotrading.base.core.marketdata.futures;

import java.util.HashMap;
import java.util.Map;

public class FuturesExchange {

    public final Map<String, Map<String, Fut>> prefixMap = new HashMap<>();
    public final Map<String, Fut> futuresMap = new HashMap<>();

    public final void addFuturesMap(final Map<String, Fut> futuresMap) {
        if (!futuresMap.isEmpty()) {
            for (final Fut f : futuresMap.values()) {
                prefixMap.put(f.prefix, futuresMap);
                break;
            }
            this.futuresMap.putAll(futuresMap);
        }
    }

    public Map<String, Fut> byPrefix(final String prefix) {
        return prefixMap.get(prefix);
    }

    public Fut byShortCode(final String shortCode) {
        return futuresMap.get(shortCode);
    }
}
