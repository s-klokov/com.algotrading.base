package com.algotrading.base.core.marketdata.futures;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FuturesMapBuilder {

    private final String prefix;
    private final Map<String, Fut> map = new LinkedHashMap<>();

    public FuturesMapBuilder(final String prefix) {
        this.prefix = prefix;
    }

    public FuturesMapBuilder put(final String longCode, final String shortCode,
                                 final int expiry, final int oneDayBeforeExpiry, final int previousExpiry) {
        map.put(prefix, new Fut(prefix, longCode, shortCode, expiry, oneDayBeforeExpiry, previousExpiry));
        return this;
    }

    public Map<String, Fut> build() {
        return map;
    }
}
