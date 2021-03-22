package com.algotrading.base.core.columns;

import static java.util.Objects.requireNonNull;

public abstract class AbstractColumn {
    protected static final int DEFAULT_SIZE = 100;
    private final String name;
    protected int length;

    public AbstractColumn(final String name) {
        this.name = requireNonNull(name);
        length = 0;
    }

    public AbstractColumn(final String name, final int length) {
        this.name = requireNonNull(name);
        this.length = length;
    }

    public final String name() {
        return name;
    }

    public final int length() {
        return length;
    }

    public void setLength(final int newLength) {
        if (length < newLength) {
            ensureCapacity(newLength);
        }
        length = newLength;
    }

    protected void rangeCheck(final int index) {
        if (index >= length()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public abstract void ensureCapacity(final int capacity);

    public abstract boolean hasSameTypeAs(final AbstractColumn column);

    public AbstractColumn copy() {
        return copy(0, length());
    }

    public abstract AbstractColumn copy(final int from, final int to);

    public abstract AbstractColumn subColumn(final int[] indices);

    public abstract AbstractColumn append(final AbstractColumn column);

    public abstract void move(final int offset);

    /**
     * Изменить порядок данных в колонке. На i-е место поставить элемент, который ранее стоял на месте indices[i].
     *
     * @param indices массив длины length с указанием исходных мест данных в колонке.
     */
    public void reorder(final int[] indices) {
        if (length != indices.length) {
            throw new IndexOutOfBoundsException("length=" + length + ", indices.length=" + indices.length);
        }
    }
}
