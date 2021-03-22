package com.algotrading.base.core.values;

public final class LongValue implements AbstractValue {
    private long value;

    public LongValue() {
        this(0L);
    }

    public LongValue(final long value) {
        this.value = value;
    }

    public void set(final long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
