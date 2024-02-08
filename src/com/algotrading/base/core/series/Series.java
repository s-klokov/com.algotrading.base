package com.algotrading.base.core.series;

import com.algotrading.base.core.columns.*;
import com.algotrading.base.core.values.*;

import java.util.*;
import java.util.function.Supplier;

/**
 * Объект для представления (временных) рядов общего типа.
 * <p>
 * Для обеспечения производительности используется колоночная структура данных.
 * <p>
 * Порядок следования колонок определяется в момент создания.
 * <p>
 * Ответственность за обеспечение одинаковой длины колонок несёт пользователь.
 */
public class Series {
    /**
     * Набор именованных колонок.
     */
    protected final Map<String, AbstractColumn> columnMap = new LinkedHashMap<>();

    public Series ensureCapacity(final int capacity) {
        columnMap.values().forEach(column -> column.ensureCapacity(capacity));
        return this;
    }

    public Series withDoubleColumn(final String name) {
        acquireDoubleColumn(name);
        return this;
    }

    public Series withLongColumn(final String name) {
        acquireLongColumn(name);
        return this;
    }

    public Series withIntColumn(final String name) {
        acquireIntColumn(name);
        return this;
    }

    public Series withStringColumn(final String name) {
        acquireStringColumn(name);
        return this;
    }

    public <T> Series withColumn(final String name, final Class<T> type) {
        acquireColumn(name, type);
        return this;
    }

    public DoubleColumn acquireDoubleColumn(final String name) {
        return (DoubleColumn) columnMap.computeIfAbsent(name, n -> new DoubleColumn(n, length()));
    }

    public DoubleColumn acquireDoubleColumn(final String name, final Supplier<? extends DoubleColumn> supplier) {
        DoubleColumn column = (DoubleColumn) columnMap.get(name);
        if (column == null) {
            column = supplier.get();
            columnMap.put(name, column);
        }
        return column;
    }

    public LongColumn acquireLongColumn(final String name) {
        return (LongColumn) columnMap.computeIfAbsent(name, n -> new LongColumn(n, length()));
    }

    public LongColumn acquireLongColumn(final String name, final Supplier<? extends LongColumn> supplier) {
        LongColumn column = (LongColumn) columnMap.get(name);
        if (column == null) {
            column = supplier.get();
            columnMap.put(name, column);
        }
        return column;
    }

    public IntColumn acquireIntColumn(final String name) {
        return (IntColumn) columnMap.computeIfAbsent(name, n -> new IntColumn(n, length()));
    }

    public IntColumn acquireIntColumn(final String name,
                                      final Supplier<? extends IntColumn> supplier) {
        IntColumn column = (IntColumn) columnMap.get(name);
        if (column == null) {
            column = supplier.get();
            columnMap.put(name, column);
        }
        return column;
    }

    public StringColumn acquireStringColumn(final String name) {
        return (StringColumn) columnMap.computeIfAbsent(name, n -> new StringColumn(n, length()));
    }

    public StringColumn acquireStringColumn(final String name, final Supplier<? extends StringColumn> supplier) {
        StringColumn column = (StringColumn) columnMap.get(name);
        if (column == null) {
            column = supplier.get();
            columnMap.put(name, column);
        }
        return column;
    }

    @SuppressWarnings("unchecked")
    public <T> Column<T> acquireColumn(final String name, final Class<T> type) {
        Column<T> column = (Column<T>) columnMap.get(name);
        if (column == null) {
            column = new Column<>(name, type, length());
            columnMap.put(name, column);
        }
        if (column.type() != type) {
            throw new ClassCastException("Existing column type " + column.type() + " mismatch argument type " + type);
        }
        return column;
    }

    public DoubleColumn getDoubleColumn(final String name) {
        return (DoubleColumn) columnMap.get(name);
    }

    public LongColumn getLongColumn(final String name) {
        return (LongColumn) columnMap.get(name);
    }

    public IntColumn getIntColumn(final String name) {
        return (IntColumn) columnMap.get(name);
    }

    public StringColumn getStringColumn(final String name) {
        return (StringColumn) columnMap.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> Column<T> getColumn(final String name) {
        return (Column<T>) columnMap.get(name);
    }

    public AbstractColumn removeColumn(final String name) {
        return columnMap.remove(name);
    }

    public String[] columnNames() {
        final String[] columns = new String[columnMap.size()];
        int i = 0;
        for (final String key : columnMap.keySet()) {
            columns[i++] = key;
        }
        return columns;
    }

    public Collection<AbstractColumn> columns() {
        return columnMap.values();
    }

    public int length() {
        int length = -1;
        for (final AbstractColumn column : columnMap.values()) {
            if (length == -1) {
                length = column.length();
            } else if (length != column.length()) {
                throw new IllegalStateException("Column length mismatch for " + column);
            }
        }
        return (length == -1) ? 0 : length;
    }

    public void setLength(final int newLength) {
        columnMap.values().forEach(column -> column.setLength(newLength));
    }

    public boolean hasEqualColumnLength() {
        try {
            length();
            return true;
        } catch (final IllegalStateException e) {
            return false;
        }
    }

    public boolean hasSameColumnsAs(final Series series) {
        for (final AbstractColumn thisColumn : columnMap.values()) {
            final AbstractColumn thatColumn = series.columnMap.get(thisColumn.name());
            if (!thisColumn.hasSameTypeAs(thatColumn)) {
                return false;
            }
        }
        return columnMap.size() == series.columnMap.size();
    }

    public Series append(final Series series) {
        if (!hasSameColumnsAs(series)) {
            throw new IllegalArgumentException("Columns mismatch in " + this + " and " + series);
        }
        for (final AbstractColumn column : columnMap.values()) {
            switch (column) {
                case final DoubleColumn doubleColumn -> doubleColumn.append(series.getDoubleColumn(column.name()));
                case final LongColumn longColumn -> longColumn.append(series.getLongColumn(column.name()));
                case final IntColumn intColumn -> intColumn.append(series.getIntColumn(column.name()));
                case final StringColumn stringColumn -> stringColumn.append(series.getStringColumn(column.name()));
                case final Column<?> objColumn -> objColumn.append(series.getColumn(column.name()));
                case null, default -> throw new ClassCastException("Unknown column type: " + column.getClass());
            }
        }
        return this;
    }

    public Series append(final Series series, final int rowId) {
        for (final AbstractColumn column : columnMap.values()) {
            switch (column) {
                case final DoubleColumn doubleColumn -> doubleColumn.append(series.getDoubleColumn(column.name()).get(rowId));
                case final LongColumn longColumn -> longColumn.append(series.getLongColumn(column.name()).get(rowId));
                case final IntColumn intColumn -> intColumn.append(series.getIntColumn(column.name()).get(rowId));
                case final StringColumn stringColumn -> stringColumn.append(series.getStringColumn(column.name()).get(rowId));
                case final Column<?> objColumn -> objColumn.append((Column<?>) series.getColumn(column.name()).get(rowId));
                case null, default -> throw new ClassCastException("Unknown column type: " + column.getClass());
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public Series appendRow(final AbstractValue... values) {
        if (values.length != columnMap.size()) {
            throw new IllegalArgumentException("Values length " + values.length
                    + " mismatch columns count " + columnMap.size());
        }
        final Iterator<AbstractColumn> i = columnMap.values().iterator();
        for (final AbstractValue value : values) {
            final AbstractColumn column = i.next();
            switch (value) {
                case final DoubleValue doubleValue -> ((DoubleColumn) column).append(doubleValue.get());
                case final LongValue longValue -> ((LongColumn) column).append(longValue.get());
                case final IntValue intValue -> ((IntColumn) column).append(intValue.get());
                case final StringValue stringValue -> ((StringColumn) column).append(stringValue.get());
                case final Value<?> v -> {
                    final Column c = (Column) column;
                    if (c.type().isInstance(v.get())) {
                        c.append(v.get());
                    } else {
                        throw new ClassCastException("Column type " + c.type() + " mismatch value type" + v.get().getClass());
                    }
                }
                case null, default -> throw new ClassCastException("Unknown value type: " + value.getClass());
            }
        }
        return this;
    }

    public void move(final int offset) {
        columnMap.values().forEach(column -> column.move(offset));
    }

    public String getAsString(final int index) {
        return getAsString(index, ";");
    }

    public String getAsString(final int index, final String separator) {
        final StringBuilder sb = new StringBuilder();
        for (final AbstractColumn column : columnMap.values()) {
            switch (column) {
                case final DoubleColumn doubleColumn -> sb.append(doubleColumn.get(index));
                case final LongColumn longColumn -> sb.append(longColumn.get(index));
                case final IntColumn intColumn -> sb.append(intColumn.get(index));
                case final StringColumn stringColumn -> sb.append(stringColumn.get(index));
                case final Column<?> objColumn -> sb.append(objColumn.get(index));
                case null, default -> throw new ClassCastException("Unknown column type: " + column.getClass());
            }
            sb.append(separator);
        }
        sb.setLength(sb.length() - separator.length());
        return sb.toString();
    }

    public List<String> getAsStrings(final int from, final int to) {
        final List<String> strings = new ArrayList<>();
        final int len = length();
        for (int index = from; index < to; index++) {
            if (0 <= index && index < len) {
                strings.add(getAsString(index));
            }
        }
        return strings;
    }

    public Series copy() {
        return copy(0, length());
    }

    public Series copy(final int from, final int to) {
        final Series series = new Series();
        columnMap.forEach((key, value) -> series.columnMap.put(key, value.copy(from, to)));
        return series;
    }

    public Series subSeries(final int[] indices) {
        final Series series = new Series();
        columnMap.forEach((key, value) -> series.columnMap.put(key, value.subColumn(indices)));
        return series;
    }

    public Series sortBy(final LongColumn longColumn) {
        checkColumn(longColumn);
        final int len = longColumn.length();
        final LongInt[] array = new LongInt[len];
        for (int i = 0; i < len; i++) {
            array[i] = new LongInt(longColumn.get(i), i);
        }
        Arrays.sort(array, Comparator.comparingLong(o -> o.value));
        reorderBy(array);
        return this;
    }

    public Series sortBy(final DoubleColumn doubleColumn) {
        checkColumn(doubleColumn);
        final int len = doubleColumn.length();
        final DoubleInt[] array = new DoubleInt[len];
        for (int i = 0; i < len; i++) {
            array[i] = new DoubleInt(doubleColumn.get(i), i);
        }
        Arrays.sort(array, Comparator.comparingDouble(o -> o.value));
        reorderBy(array);
        return this;
    }

    public Series sortBy(final IntColumn intColumn) {
        checkColumn(intColumn);
        final int len = intColumn.length();
        final IntInt[] array = new IntInt[len];
        for (int i = 0; i < len; i++) {
            array[i] = new IntInt(intColumn.get(i), i);
        }
        Arrays.sort(array, Comparator.comparingInt(o -> o.value));
        reorderBy(array);
        return this;
    }

    public Series sortBy(final StringColumn stringColumn) {
        checkColumn(stringColumn);
        final int len = stringColumn.length();
        final StringInt[] array = new StringInt[len];
        for (int i = 0; i < len; i++) {
            array[i] = new StringInt(stringColumn.get(i), i);
        }
        Arrays.sort(array, Comparator.comparing(o -> o.value));
        reorderBy(array);
        return this;
    }

    private void checkColumn(final AbstractColumn column) {
        if (!columnMap.containsValue(column)) {
            throw new IllegalArgumentException("Column " + column + " is absent in series " + this);
        }
    }

    private void reorderBy(final Indexed[] array) {
        final int len = array.length;
        final int[] indices = new int[len];
        for (int i = 0; i < len; i++) {
            indices[i] = array[i].index;
        }
        columnMap.forEach((columnName, column) -> column.reorder(indices));
    }

    private static abstract class Indexed {
        final int index;

        Indexed(final int index) {
            this.index = index;
        }
    }

    private static class LongInt extends Indexed {
        final long value;

        LongInt(final long value, final int index) {
            super(index);
            this.value = value;
        }
    }

    private static class DoubleInt extends Indexed {
        final double value;

        DoubleInt(final double value, final int index) {
            super(index);
            this.value = value;
        }
    }

    private static class IntInt extends Indexed {
        final int value;

        IntInt(final int value, final int index) {
            super(index);
            this.value = value;
        }
    }

    private static class StringInt extends Indexed {
        final String value;

        StringInt(final String value, final int index) {
            super(index);
            this.value = value;
        }
    }

    @Override
    public String toString() {
        return "Series[" + length() + ']' + Arrays.toString(columnNames());
    }
}
