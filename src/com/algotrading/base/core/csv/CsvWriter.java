package com.algotrading.base.core.csv;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.IntColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.columns.StringColumn;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.function.*;

import static java.util.Objects.requireNonNull;

/**
 * Класс, реализующий вывод данных из колонок в csv-файлы.
 */
public class CsvWriter {

    private final StringBuilder sb;
    private final Formatter f;
    private final List<IntConsumer> columnActions;
    private int blockSize = 1000;
    private Locale locale = Locale.US;
    private String header = null;
    private String separator = ";";
    private IntPredicate rowFilter = index -> true;

    public CsvWriter() {
        sb = new StringBuilder();
        f = new Formatter(sb);
        columnActions = new ArrayList<>();
    }

    public CsvWriter blockSize(final int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    public CsvWriter locale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    public CsvWriter header(final String header) {
        this.header = header;
        return this;
    }

    public CsvWriter separator(final String separator) {
        this.separator = separator;
        return this;
    }

    public CsvWriter rowFilter(final IntPredicate rowFilter) {
        this.rowFilter = requireNonNull(rowFilter);
        return this;
    }

    public CsvWriter column(final String s) {
        columnActions.add(index -> sb.append(s).append(separator));
        return this;
    }

    public CsvWriter column(final DoubleColumn doubleColumn) {
        columnActions.add(index -> sb.append(doubleColumn.get(index)).append(separator));
        return this;
    }

    public CsvWriter column(final DoubleColumn doubleColumn, final String format) {
        columnActions.add(index -> {
            f.format(locale, format, doubleColumn.get(index));
            sb.append(separator);
        });
        return this;
    }

    public CsvWriter column(final DoubleColumn doubleColumn, final DoubleFunction<String> f) {
        columnActions.add(index -> sb.append(f.apply(doubleColumn.get(index))).append(separator));
        return this;
    }

    public CsvWriter column(final LongColumn longColumn) {
        columnActions.add(index -> sb.append(longColumn.get(index)).append(separator));
        return this;
    }

    public CsvWriter column(final LongColumn longColumn, final String format) {
        columnActions.add(index -> {
            f.format(locale, format, longColumn.get(index));
            sb.append(separator);
        });
        return this;
    }

    public CsvWriter column(final LongColumn longColumn, final LongFunction<String> f) {
        columnActions.add(index -> sb.append(f.apply(longColumn.get(index))).append(separator));
        return this;
    }

    public CsvWriter column(final IntColumn intColumn) {
        columnActions.add(index -> sb.append(intColumn.get(index)).append(separator));
        return this;
    }

    public CsvWriter column(final IntColumn intColumn, final String format) {
        columnActions.add(index -> {
            f.format(locale, format, intColumn.get(index));
            sb.append(separator);
        });
        return this;
    }

    public CsvWriter column(final IntColumn intColumn, final IntFunction<String> f) {
        columnActions.add(index -> sb.append(f.apply(intColumn.get(index))).append(separator));
        return this;
    }

    public CsvWriter column(final StringColumn stringColumn) {
        columnActions.add(index -> sb.append(stringColumn.get(index)).append(separator));
        return this;
    }

    public CsvWriter column(final StringColumn stringColumn, final String format) {
        columnActions.add(index -> {
            f.format(locale, format, stringColumn.get(index));
            sb.append(separator);
        });
        return this;
    }

    public CsvWriter column(final StringColumn stringColumn, final Function<String, String> f) {
        columnActions.add(index -> sb.append(f.apply(stringColumn.get(index))).append(separator));
        return this;
    }

    public void write(final PrintStream ps, final int indexFrom, final int indexTo) {
        if (header != null) {
            ps.println(header);
        }
        final StringBuilder blockStringBuilder = new StringBuilder();
        int lines = 0;
        for (int index = indexFrom; index < indexTo; index++) {
            if (rowFilter.test(index)) {
                sb.setLength(0);
                final int i = index;
                columnActions.forEach(action -> action.accept(i));
                sb.setLength(sb.length() - separator.length());
                blockStringBuilder.append(sb.toString()).append("\r\n");
                lines++;
                if (lines >= blockSize) {
                    ps.print(blockStringBuilder.toString());
                    blockStringBuilder.setLength(0);
                    lines = 0;
                }
            }
        }
        if (lines > 0) {
            ps.print(blockStringBuilder.toString());
        }
    }
}
