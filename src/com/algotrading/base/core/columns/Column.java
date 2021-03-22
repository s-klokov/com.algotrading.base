package com.algotrading.base.core.columns;

import static java.util.Objects.requireNonNull;

public class Column<T> extends AbstractColumn {

    private static final Object[] EMPTY = new Object[0];
    protected Object[] data;
    protected final Class<T> type;

    public Column(final String name, final Class<T> type) {
        super(name);
        data = EMPTY;
        this.type = requireNonNull(type);
    }

    public Column(final String name, final Class<T> type, final int length) {
        super(name, length);
        data = new Object[length];
        this.type = requireNonNull(type);
    }

    public Class<T> type() {
        return type;
    }

    public void set(final int index, final T value) {
        if (!type.isInstance(value)) {
            throw new ClassCastException("Cannot cast " + value.getClass().getName() + " to " + type.getName());
        }
        rangeCheck(index);
        data[index] = value;
    }

    @SuppressWarnings("unchecked")
    public T get(final int index) {
        rangeCheck(index);
        return (T) data[index];
    }

    public T getLast() {
        return get(length() - 1);
    }

    public void append(final T value) {
        ensureCapacity(length + 1);
        data[length] = value;
        length++;
    }

    @Override
    public void ensureCapacity(final int capacity) {
        if (data.length < capacity) {
            final Object[] newData = new Object[Math.max(DEFAULT_SIZE, Math.max(data.length + data.length / 2, capacity))];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    @Override
    public boolean hasSameTypeAs(final AbstractColumn column) {
        return column instanceof Column<?> && type().equals(((Column<?>) column).type());
    }

    @Override
    public Column<T> copy() {
        return copy(0, length());
    }

    @Override
    public Column<T> copy(final int from, final int to) {
        final Column<T> copy = new Column<>(name(), type(), to - from);
        System.arraycopy(data, from, copy.data, 0, to - from);
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Column<T> subColumn(final int[] indices) {
        final Column<T> subColumn = new Column<>(name(), type(), indices.length);
        for (int i = 0; i < indices.length; i++) {
            subColumn.set(i, (T) data[indices[i]]);
        }
        return subColumn;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Column<T> append(final AbstractColumn column) {
        final Column<T> tColumn = (Column<T>) column;
        if (!type().equals(tColumn.type())) {
            throw new ClassCastException("Cannot cast type " + tColumn.type() + " to " + type());
        }
        ensureCapacity(length + tColumn.length);
        System.arraycopy(tColumn.data, 0, data, length, tColumn.length);
        length += tColumn.length;
        return this;
    }

    @Override
    public void move(final int offset) {
        if (offset > 0) {
            System.arraycopy(data, 0, data, offset, data.length - offset);
        } else if (offset < 0) {
            System.arraycopy(data, -offset, data, 0, data.length + offset);
        }
    }

    @Override
    public String toString() {
        return type.getName() + '[' + length() + "] " + name();
    }

    @Override
    public void reorder(final int[] indices) {
        super.reorder(indices);
        final Object[] newData = new Object[data.length];
        for (int i = 0; i < length; i++) {
            newData[i] = data[indices[i]];
        }
        data = newData;
    }
}
