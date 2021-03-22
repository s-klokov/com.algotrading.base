package com.algotrading.base.core.level2;

/**
 * Реализация стандартного стакана: цены -- вещественные числа, количества -- целые числа.
 */
public class Level2 extends AbstractLevel2<double[], long[]> {

    private static final double[] EMPTY_DOUBLE = new double[0];
    private static final long[] EMPTY_LONG = new long[0];

    public Level2(final long timestamp, final long timeCode, final long sequence,
                  final double[] bidPrices, final long[] bidSizes,
                  final double[] askPrices, final long[] askSizes) {
        super(timestamp, timeCode, sequence, bidPrices, bidSizes, askPrices, askSizes);
        if (bidPrices.length != bidSizes.length) {
            throw new IllegalArgumentException("Bid prices length " + bidPrices.length + "!=" + bidSizes.length + " bid sizes length");
        }
        if (askPrices.length != askSizes.length) {
            throw new IllegalArgumentException("Ask prices length " + askPrices.length + "!=" + askSizes.length + " ask prices length");
        }
    }

    public static Level2 empty(final long timestamp, final long timeCode, final long sequence) {
        return new Level2(timestamp, timeCode, sequence, EMPTY_DOUBLE, EMPTY_LONG, EMPTY_DOUBLE, EMPTY_LONG);
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
