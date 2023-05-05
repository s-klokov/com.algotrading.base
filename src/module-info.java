module com.algotrading.base {
    requires java.logging;
    requires json.simple;
    exports com.algotrading.base.core;
    exports com.algotrading.base.core.candles;
    exports com.algotrading.base.core.columns;
    exports com.algotrading.base.core.commission;
    exports com.algotrading.base.core.csv;
    exports com.algotrading.base.core.indicators;
    exports com.algotrading.base.core.level2;
    exports com.algotrading.base.core.marketdata;
    exports com.algotrading.base.core.series;
    exports com.algotrading.base.core.sync;
    exports com.algotrading.base.core.tester;
    exports com.algotrading.base.core.values;
    exports com.algotrading.base.core.window;
    exports com.algotrading.base.helpers;
    exports com.algotrading.base.lib;
    exports com.algotrading.base.lib.fits;
    exports com.algotrading.base.lib.linear;
    exports com.algotrading.base.lib.nnets;
    exports com.algotrading.base.lib.optim;
    exports com.algotrading.base.lib.stat;
    exports com.algotrading.base.util;
    exports com.algotrading.base.core.marketdata.futures;
    exports com.algotrading.base.core.marketdata.locators;
    exports com.algotrading.base.core.marketdata.readers;
}