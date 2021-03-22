package com.algotrading.base.core.columns;

public class StringColumn extends AbstractColumn {

    private static final String[] EMPTY = new String[0];
    protected String[] data;

    public StringColumn(final String name) {
        super(name);
        data = EMPTY;
    }

    public StringColumn(final String name, final int length) {
        super(name, length);
        data = new String[length];
    }

    public void set(final int index, final String value) {
        rangeCheck(index);
        data[index] = value;
    }

    public String get(final int index) {
        rangeCheck(index);
        return data[index];
    }

    public String getLast() {
        return get(length() - 1);
    }

    public void append(final String value) {
        ensureCapacity(length + 1);
        data[length] = value;
        length++;
    }

    @Override
    public void ensureCapacity(final int capacity) {
        if (data.length < capacity) {
            final String[] newData = new String[Math.max(DEFAULT_SIZE, Math.max(data.length + data.length / 2, capacity))];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    @Override
    public boolean hasSameTypeAs(final AbstractColumn column) {
        return column instanceof StringColumn;
    }

    @Override
    public StringColumn copy() {
        return copy(0, length());
    }

    @Override
    public StringColumn copy(final int from, final int to) {
        final StringColumn copy = new StringColumn(name(), to - from);
        System.arraycopy(data, from, copy.data, 0, to - from);
        return copy;
    }

    @Override
    public StringColumn subColumn(final int[] indices) {
        final StringColumn subColumn = new StringColumn(name(), indices.length);
        for (int i = 0; i < indices.length; i++) {
            subColumn.set(i, data[indices[i]]);
        }
        return subColumn;
    }

    @Override
    public StringColumn append(final AbstractColumn column) {
        final StringColumn stringColumn = (StringColumn) column;
        ensureCapacity(length + stringColumn.length);
        System.arraycopy(stringColumn.data, 0, data, length, stringColumn.length);
        length += stringColumn.length;
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
        return "String[" + length() + "] " + name();
    }

    @Override
    public void reorder(final int[] indices) {
        super.reorder(indices);
        final String[] newData = new String[data.length];
        for (int i = 0; i < length; i++) {
            newData[i] = data[indices[i]];
        }
        data = newData;
    }
}
