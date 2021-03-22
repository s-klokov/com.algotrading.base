package com.algotrading.base.core.tester;

/**
 * Заявка для тестера.
 */
public abstract class TestOrder {
    /**
     * Объём заявки.
     */
    public final long volume;
    /**
     * Цена, по которой исполнилась заявка.
     */
    public double executionPrice = Double.NaN;

    public TestOrder(final long volume) {
        if (volume == 0) {
            throw new IllegalArgumentException("volume=" + volume);
        }
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "Order " + volume + "@" + executionPrice;
    }
}
