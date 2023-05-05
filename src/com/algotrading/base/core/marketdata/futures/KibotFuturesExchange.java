package com.algotrading.base.core.marketdata.futures;

public final class KibotFuturesExchange extends FuturesExchange {

    public static final KibotFuturesExchange INSTANCE = new KibotFuturesExchange();

    private KibotFuturesExchange() {

        // TODO:

        addFuturesMap(new FuturesMapBuilder("ES")
                .put("ESH11", "ESH11", 20110318, 20110317, 20101217)
                .put("ESM11", "ESM11", 20110617, 20110616, 20110318)
                .put("ESU11", "ESU11", 20110916, 20110915, 20110617)
                .put("ESZ11", "ESZ11", 20111216, 20111215, 20110916)

                .put("ESH12", "ESH12", 20120316, 20120315, 20111216)
                .put("ESM12", "ESM12", 20120615, 20120614, 20120316)
                .put("ESU12", "ESU12", 20120921, 20120920, 20120615)
                .put("ESZ12", "ESZ12", 20121221, 20121220, 20120921)

                .put("ESH13", "ESH13", 20130315, 20130314, 20121221)
                .put("ESM13", "ESM13", 20130621, 20130620, 20130315)
                .put("ESU13", "ESU13", 20130920, 20130919, 20130621)
                .put("ESZ13", "ESZ13", 20131220, 20131219, 20130920)

                .put("ESH14", "ESH14", 20140321, 20140320, 20131220)
                .put("ESM14", "ESM14", 20140620, 20140619, 20140321)
                .put("ESU14", "ESU14", 20140919, 20140918, 20140620)
                .put("ESZ14", "ESZ14", 20141219, 20141218, 20140919)

                .put("ESH15", "ESH15", 20150320, 20150319, 20141219)
                .put("ESM15", "ESM15", 20150619, 20150618, 20150320)
                .put("ESU15", "ESU15", 20150918, 20150917, 20150619)
                .put("ESZ15", "ESZ15", 20151218, 20151217, 20150918)

                .put("ESH16", "ESH16", 20160318, 20160317, 20151218)
                .put("ESM16", "ESM16", 20160617, 20160616, 20160318)
                .put("ESU16", "ESU16", 20160916, 20160915, 20160617)
                .put("ESZ16", "ESZ16", 20161216, 20161215, 20160916)

                .put("ESH17", "ESH17", 20170317, 20170316, 20161216)
                .put("ESM17", "ESM17", 20170616, 20170615, 20170317)
                .put("ESU17", "ESU17", 20170915, 20170914, 20170616)
                .put("ESZ17", "ESZ17", 20171215, 20171214, 20170915)

                .put("ESH18", "ESH18", 20180316, 20180315, 20171215)
                .put("ESM18", "ESM18", 20180615, 20180614, 20180316)
                .build()
        );

        // TODO:
        addFuturesMap(new FuturesMapBuilder("BZ")
                .build()
        );

        // TODO:
        addFuturesMap(new FuturesMapBuilder("NG")
                .build()
        );
    }
}
