package com.algotrading.base.core.values;

public class Value<T> implements AbstractValue {
    private T value;

    public Value() {
        this(null);
    }

    public Value(final T value) {
        this.value = value;
    }

    public void set(final T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
