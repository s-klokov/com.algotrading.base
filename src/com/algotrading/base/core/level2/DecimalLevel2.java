package com.algotrading.base.core.level2;

import java.math.BigDecimal;

/**
 * Реализация стакана, где цены и количества имеют тип {@link BigDecimal}.
 */
public class DecimalLevel2 extends AbstractLevel2<BigDecimal[], BigDecimal[]> {

    private static final BigDecimal[] EMPTY = new BigDecimal[0];

    public DecimalLevel2(final long timestamp, final long timeCode, final long sequence,
                         final BigDecimal[] bidPrices, final BigDecimal[] bidSizes,
                         final BigDecimal[] askPrices, final BigDecimal[] askSizes) {
        super(timestamp, timeCode, sequence, bidPrices, bidSizes, askPrices, askSizes);
        if (bidPrices.length != bidSizes.length) {
            throw new IllegalArgumentException("Bid prices length " + bidPrices.length + "!=" + bidSizes.length + " bid sizes length");
        }
        if (askPrices.length != askSizes.length) {
            throw new IllegalArgumentException("Ask prices length " + askPrices.length + "!=" + askSizes.length + " ask prices length");
        }
    }

    public static DecimalLevel2 empty(final long timestamp, final long timeCode, final long sequence) {
        return new DecimalLevel2(timestamp, timeCode, sequence, EMPTY, EMPTY, EMPTY, EMPTY);
    }

    @Override
    public int bidsCount() {
        return bidPrices.length;
    }

    @Override
    public int asksCount() {
        return askPrices.length;
    }
}
