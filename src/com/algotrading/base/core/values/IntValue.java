package com.algotrading.base.core.values;

public final class IntValue implements AbstractValue {
    private int value;

    public IntValue() {
        this(0);
    }

    public IntValue(final int value) {
        this.value = value;
    }

    public void set(final int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
