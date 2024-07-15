package com.algotrading.base.core.marketdata.futures;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FuturesMapBuilder {

    private final String prefix;
    private final Map<String, Futures> map = new LinkedHashMap<>();

    public FuturesMapBuilder(final String prefix) {
        this.prefix = prefix;
    }

    public FuturesMapBuilder put(final String longCode, final String shortCode,
                                 final int expiry, final int rolling, final int previousExpiry) {
        map.put(shortCode, new Futures(prefix, longCode, shortCode, expiry, rolling, previousExpiry));
        return this;
    }

    public Map<String, Futures> build() {
        return map;
    }
}
