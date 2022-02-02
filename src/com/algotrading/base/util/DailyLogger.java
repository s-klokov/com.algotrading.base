package com.algotrading.base.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Простой логгер с записью в ежедневные файлы лога, дублируя сообщения об ошибках в отдельные файлы
 * с фрагментом _error в названии файла.
 */
public class DailyLogger extends AbstractLogger {

    /**
     * Объект для синхронизации.
     */
    protected final Object mutex = new Object();
    /**
     * Префикс файла, который будет открыт в режиме "append", по умолчанию, путь и имя файла.
     */
    protected final String fileNamePrefix;
    /**
     * Суффикс файла, который будет открыт в режиме "append".
     */
    protected final String fileNameSuffix;
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
     * @param fileNamePrefix префикс файлов лога
     * @param fileNameSuffix суффикс файлов лога
     */
    public DailyLogger(final String fileNamePrefix, final String fileNameSuffix) {
        this.fileNamePrefix = Objects.requireNonNull(fileNamePrefix);
        this.fileNameSuffix = Objects.requireNonNull(fileNameSuffix);
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
                final String errorFileName = getFileName("_error");
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
                final String errorFileName = getFileName("_error");
                try (final PrintStream errStream = new PrintStream(new FileOutputStream(errorFileName, true), true, StandardCharsets.UTF_8)) {
                    errStream.println(s);
                    thrown.printStackTrace(errStream);
                } catch (final FileNotFoundException ignored) {
                }
            }
        }
    }

    private String getFileName(final String preSuffix) {
        return String.format("%s%tY%<tm%<td%s%s", fileNamePrefix, System.currentTimeMillis(), preSuffix, fileNameSuffix);
    }

    private void ensureLogStreamRotation() {
        final ZonedDateTime now = ZonedDateTime.now();
        if (!now.isBefore(deadline)) {
            deadline = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            final String newFileName = getFileName("");
            try {
                final PrintStream ps = new PrintStream(new FileOutputStream(newFileName, true), true, StandardCharsets.UTF_8);
                close();
                logStream = ps;
            } catch (final IOException e) {
                log(AbstractLogger.ERROR, "Cannot change log file", e);
            }
        }
    }
}
