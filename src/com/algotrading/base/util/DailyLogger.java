package com.algotrading.base.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Логгер, осуществляющий запись в ежедневные файлы лога и дублирующий сообщения об ошибках в отдельные файлы.
 * Ежедневные файлы в своём имени содержат дату в формате YYYYMMDD.
 */
public class DailyLogger extends AbstractLogger {

    /**
     * Объект для синхронизации.
     */
    protected final Object mutex = new Object();
    /**
     * Шаблон имени файла лога, содержащий фрагмент "%d" для вставки даты в формате YYYYMMDD.
     */
    protected final String logFileNameFormat;
    /**
     * Шаблон имени файла с ошибками, содержащий фрагмент "%d" для вставки даты в формате YYYYMMDD.
     */
    protected final String errFileNameFormat;
    /**
     * Время, после которого нужно произвести смену потока вывода лога.
     */
    protected ZonedDateTime deadline = ZonedDateTime.now();
    /**
     * Поток для вывода лога.
     */
    protected PrintStream logStream = System.out;

    /**
     * Конструктор.
     *
     * @param logFileNameFormat шаблон имени файла лога, например: "example.%d.log"
     * @param errFileNameFormat шаблон имени файла с ошибками, например: "example_error.%d.log"
     */
    public DailyLogger(final String logFileNameFormat, final String errFileNameFormat) {
        this.logFileNameFormat = Objects.requireNonNull(logFileNameFormat);
        this.errFileNameFormat = Objects.requireNonNull(errFileNameFormat);
    }

    @Override
    public DailyLogger withLogLevel(final int logLevel) {
        super.withLogLevel(logLevel);
        return this;
    }

    @Override
    public DailyLogger withErrLevel(final int errLevel) {
        super.withErrLevel(errLevel);
        return this;
    }

    @Override
    public DailyLogger withThreadNameEnabled(final boolean isThreadNameEnabled) {
        super.withThreadNameEnabled(isThreadNameEnabled);
        return this;
    }

    @Override
    public void close() {
        synchronized (mutex) {
            if (logStream != null && logStream != System.out) {
                logStream.close();
                logStream = System.out;
            }
        }
    }

    @Override
    protected void print(final int level, final String s) {
        synchronized (mutex) {
            ensureLogStreamRotation();
            logStream.println(s);
            if (level >= errLevel) {
                final String errorFileName = getFileName(errFileNameFormat, ZonedDateTime.now());
                try (final PrintStream errStream = new PrintStream(new FileOutputStream(errorFileName, true), true, StandardCharsets.UTF_8)) {
                    errStream.println(s);
                } catch (final FileNotFoundException ignored) {
                }
            }
        }
    }

    @Override
    protected void print(final int level, final String s, final Throwable thrown) {
        synchronized (mutex) {
            ensureLogStreamRotation();
            logStream.println(s);
            thrown.printStackTrace(logStream);
            if (level >= errLevel) {
                final String errorFileName = getFileName(errFileNameFormat, ZonedDateTime.now());
                try (final PrintStream errStream = new PrintStream(new FileOutputStream(errorFileName, true), true, StandardCharsets.UTF_8)) {
                    errStream.println(s);
                    thrown.printStackTrace(errStream);
                } catch (final FileNotFoundException ignored) {
                }
            }
        }
    }

    private void ensureLogStreamRotation() {
        final ZonedDateTime now = ZonedDateTime.now();
        if (!now.isBefore(deadline)) {
            deadline = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            final String newFileName = getFileName(logFileNameFormat, now);
            try {
                final PrintStream ps = new PrintStream(new FileOutputStream(newFileName, true), true, StandardCharsets.UTF_8);
                close();
                logStream = ps;
            } catch (final IOException e) {
                log(AbstractLogger.ERROR, "Cannot change log file", e);
            }
        }
    }

    private static String getFileName(final String formatString, final ZonedDateTime zdt) {
        final int yyyymmdd = zdt.getYear() * 10000 + zdt.getMonthValue() * 100 + zdt.getDayOfMonth();
        return formatString.formatted(yyyymmdd);
    }
}
