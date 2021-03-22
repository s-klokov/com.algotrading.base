package com.algotrading.base.core.columns;

public class IntColumn extends AbstractColumn {

    private static final int[] EMPTY = new int[0];
    protected int[] data;

    public IntColumn(final String name) {
        super(name);
        data = EMPTY;
    }

    public IntColumn(final String name, final int length) {
        super(name, length);
        data = new int[length];
    }

    public void set(final int index, final int value) {
        rangeCheck(index);
        data[index] = value;
    }

    public int get(final int index) {
        rangeCheck(index);
        return data[index];
    }

    public int getLast() {
        return get(length() - 1);
    }

    public void append(final int value) {
        ensureCapacity(length + 1);
        data[length] = value;
        length++;
    }

    @Override
    public void ensureCapacity(final int capacity) {
        if (data.length < capacity) {
            final int[] newData = new int[Math.max(DEFAULT_SIZE, Math.max(data.length + data.length / 2, capacity))];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    @Override
    public boolean hasSameTypeAs(final AbstractColumn column) {
        return column instanceof IntColumn;
    }

    @Override
    public IntColumn copy() {
        return copy(0, length());
    }

    @Override
    public IntColumn copy(final int from, final int to) {
        final IntColumn copy = new IntColumn(name(), to - from);
        System.arraycopy(data, from, copy.data, 0, to - from);
        return copy;
    }

    @Override
    public IntColumn subColumn(final int[] indices) {
        final IntColumn subColumn = new IntColumn(name(), indices.length);
        for (int i = 0; i < indices.length; i++) {
            subColumn.set(i, data[indices[i]]);
        }
        return subColumn;
    }

    @Override
    public IntColumn append(final AbstractColumn column) {
        final IntColumn intColumn = (IntColumn) column;
        ensureCapacity(length + intColumn.length);
        System.arraycopy(intColumn.data, 0, data, length, intColumn.length);
        length += intColumn.length;
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
        return "int[" + length() + "] " + name();
    }

    public boolean isIncreasing() {
        for (int index = 1; index < length; index++) {
            if (get(index) <= get(index - 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean isNonDecreasing() {
        for (int index = 1; index < length; index++) {
            if (get(index) < get(index - 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Предполагая, что колонка отсортирована по возрастанию, найти индекс данного элемента в колонке.
     *
     * @param v искомый элемент
     * @return индекс, если элемент содержится в колонке, иначе <tt>(-(<i>insertion point</i>) - 1)</tt>.
     * @see java.util.Arrays#binarySearch
     */
    public int binarySearch(final int v) {
        int low = 0;
        int high = length() - 1;
        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final int midVal = data[mid];

            if (midVal < v) {
                low = mid + 1;
            } else if (midVal > v) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -(low + 1);
    }

    @Override
    public void reorder(final int[] indices) {
        super.reorder(indices);
        final int[] newData = new int[data.length];
        for (int i = 0; i < length; i++) {
            newData[i] = data[indices[i]];
        }
        data = newData;
    }
}
