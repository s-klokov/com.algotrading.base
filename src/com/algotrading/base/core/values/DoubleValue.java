package com.algotrading.base.core.values;

public final class DoubleValue implements AbstractValue {
    private double value;

    public DoubleValue() {
        this(Double.NaN);
    }

    public DoubleValue(final double value) {
        this.value = value;
    }

    public void set(final double value) {
        this.value = value;
    }

    public double get() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
