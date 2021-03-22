package com.algotrading.base.core.csv;

import com.algotrading.base.core.columns.*;
import com.algotrading.base.core.values.DoubleValue;
import com.algotrading.base.core.values.IntValue;
import com.algotrading.base.core.values.LongValue;
import com.algotrading.base.core.values.StringValue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;

/**
 * Класс, реализующий чтение данных из csv-файлов в колонки.
 */
public class CsvReader {

    private final List<File> files = new ArrayList<>();
    private int linesToSkip = 0;
    private Pattern splitPattern = null;
    private String splitSeparator = ";";
    private final List<String> nanList = new ArrayList<>();
    private Pattern nanPattern = null;
    private Predicate<String> lineFilter = s -> true;
    private BooleanSupplier rowFilter = () -> true;
    private final List<Consumer<String>> columnActions = new ArrayList<>();
    private final List<Runnable> computations = new ArrayList<>();
    private final List<Runnable> undo = new ArrayList<>();

    public CsvReader file(final String fileName) {
        files.add(new File(fileName));
        return this;
    }

    public CsvReader file(final File file) {
        files.add(file);
        return this;
    }

    public CsvReader splitPattern(final String pattern) {
        splitPattern = Pattern.compile(pattern);
        splitSeparator = null;
        return this;
    }

    public CsvReader splitSeparator(final String separator) {
        splitPattern = null;
        splitSeparator = requireNonNull(separator);
        return this;
    }

    public CsvReader linesToSkip(final int linesToSkip) {
        this.linesToSkip = Math.max(0, linesToSkip);
        return this;
    }

    public CsvReader lineFilter(final Predicate<String> lineFilter) {
        this.lineFilter = requireNonNull(lineFilter);
        return this;
    }

    public CsvReader rowFilter(final BooleanSupplier rowFilter) {
        this.rowFilter = requireNonNull(rowFilter);
        return this;
    }

    public CsvReader asNaN(final String nan) {
        nanList.add(requireNonNull(nan));
        nanPattern = null;
        return this;
    }

    public CsvReader asNaN(final Pattern nanPattern) {
        this.nanPattern = nanPattern;
        nanList.clear();
        return this;
    }

    public CsvReader skipColumn() {
        columnActions.add(s -> {
        });
        return this;
    }

    private double parseDouble(final String s) {
        for (final String nan : nanList) {
            if (s.contains(nan)) {
                return Double.NaN;
            }
        }
        if (nanPattern != null && nanPattern.matcher(s).matches()) {
            return Double.NaN;
        }
        return Double.parseDouble(s.trim().replace(',', '.'));
    }

    private int parseInt(final String s) {
        return Math.toIntExact(parseLong(s));
    }

    private long parseLong(final String s) {
        if (s.indexOf(',') >= 0 || s.indexOf('.') >= 0) {
            final double doubleValue = parseDouble(s);
            final long longValue = Math.round(doubleValue);
            if (doubleValue == longValue) {
                return longValue;
            } else {
                throw new NumberFormatException(s);
            }
        } else {
            return Long.parseLong(s.trim());
        }
    }

    public CsvReader value(final DoubleValue value) {
        columnActions.add(s -> value.set(parseDouble(s)));
        return this;
    }

    public CsvReader value(final LongValue value) {
        columnActions.add(s -> value.set(parseLong(s)));
        return this;
    }

    public CsvReader value(final IntValue value) {
        columnActions.add(s -> value.set(parseInt(s)));
        return this;
    }

    public CsvReader value(final StringValue value) {
        columnActions.add(value::set);
        return this;
    }

    public CsvReader column(final DoubleColumn doubleColumn) {
        columnActions.add(s -> doubleColumn.append(parseDouble(s)));
        undo.add(() -> doubleColumn.setLength(doubleColumn.length() - 1));
        return this;
    }

    public CsvReader column(final LongColumn longColumn) {
        columnActions.add(s -> longColumn.append(parseLong(s)));
        undo.add(() -> longColumn.setLength(longColumn.length() - 1));
        return this;
    }

    public CsvReader column(final IntColumn intColumn) {
        columnActions.add(s -> intColumn.append(parseInt(s)));
        undo.add(() -> intColumn.setLength(intColumn.length() - 1));
        return this;
    }

    public CsvReader column(final StringColumn stringColumn) {
        columnActions.add(stringColumn::append);
        undo.add(() -> stringColumn.setLength(stringColumn.length() - 1));
        return this;
    }

    public <T> CsvReader column(final Column<T> column, final Function<String, T> f) {
        columnActions.add(s -> column.append(f.apply(s)));
        undo.add(() -> column.setLength(column.length() - 1));
        return this;
    }

    public CsvReader computation(final DoubleColumn doubleColumn, final DoubleSupplier supplier) {
        computations.add(() -> doubleColumn.append(supplier.getAsDouble()));
        undo.add(() -> doubleColumn.setLength(doubleColumn.length() - 1));
        return this;
    }

    public CsvReader computation(final LongColumn longColumn, final LongSupplier supplier) {
        computations.add(() -> longColumn.append(supplier.getAsLong()));
        undo.add(() -> longColumn.setLength(longColumn.length() - 1));
        return this;
    }

    public CsvReader computation(final IntColumn intColumn, final IntSupplier supplier) {
        computations.add(() -> intColumn.append(supplier.getAsInt()));
        undo.add(() -> intColumn.setLength(intColumn.length() - 1));
        return this;
    }

    public CsvReader computation(final StringColumn stringColumn, final Supplier<String> supplier) {
        computations.add(() -> stringColumn.append(supplier.get()));
        undo.add(() -> stringColumn.setLength(stringColumn.length() - 1));
        return this;
    }

    public <T> CsvReader computation(final Column<T> column, final Supplier<T> supplier) {
        computations.add(() -> column.append(supplier.get()));
        undo.add(() -> column.setLength(column.length() - 1));
        return this;
    }

    public CsvReader computation(final Runnable runnable) {
        computations.add(runnable);
        return this;
    }

    public void read() throws IOException {
        int lineNumber = 0;
        for (final File file : files) {
            if (file.getName().toLowerCase().endsWith("zip")) {
                try (final ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
                     final BufferedReader br = new BufferedReader(new InputStreamReader(zipInputStream, StandardCharsets.UTF_8))) {
                    ZipEntry zipEntry;
                    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                        int toSkip = linesToSkip;
                        lineNumber = 0;
                        String line;
                        while ((line = br.readLine()) != null) {
                            lineNumber++;
                            if (toSkip > 0) {
                                toSkip--;
                            } else {
                                try {
                                    parseLine(line);
                                } catch (final RuntimeException e) {
                                    throw new IllegalArgumentException("Parse exception at line " + lineNumber
                                            + " in zip-entry " + zipEntry
                                            + " in file " + file, e);
                                }
                            }
                        }
                    }
                }
            } else {
                try (final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                    int toSkip = linesToSkip;
                    String line;
                    while ((line = br.readLine()) != null) {
                        lineNumber++;
                        if (toSkip > 0) {
                            toSkip--;
                        } else {
                            try {
                                parseLine(line);
                            } catch (final RuntimeException e) {
                                throw new IllegalArgumentException("Parse exception at line " + lineNumber
                                        + " in file " + file, e);
                            }
                        }
                    }
                }
            }
        }
        files.clear();
    }

    private void parseLine(final String line) {
        if (line.isEmpty() || !lineFilter.test(line)) {
            return;
        }
        final String[] parts;
        if (splitPattern != null) {
            parts = splitPattern.split(line);
        } else {
            parts = line.split(splitSeparator);
        }
        final int size = columnActions.size();
        if (parts.length < size) {
            throw new IllegalArgumentException(line);
        }
        for (int i = 0; i < size; i++) {
            columnActions.get(i).accept(parts[i]);
        }
        computations.forEach(Runnable::run);
        if (!rowFilter.getAsBoolean()) {
            undo.forEach(Runnable::run);
        }
    }
}
