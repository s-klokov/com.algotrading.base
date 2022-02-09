package com.algotrading.base.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Логгер, осуществляющий запись в ежедневные файлы лога и дублирующий сообщения об ошибках в отдельные файлы.
 * Файлы логов в своём названии содержат номер дня недели в виде числа от 1 (понедельник) до 7 (воскресенье).
 * Старые файлы перезаписываются новыми в процессе работы.
 */
public class SevenDaysLogger extends AbstractLogger {
    /**
     * Объект для синхронизации.
     */
    protected final Object mutex = new Object();
    /**
     * Шаблон имени файла лога, содержащий фрагмент "%d" для вставки номера дня недели.
     */
    protected final String logFileNameFormat;
    /**
     * Шаблон имени файла с ошибками, содержащий фрагмент "%d" для вставки номера дня недели.
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
    public SevenDaysLogger(final String logFileNameFormat, final String errFileNameFormat) {
        this.logFileNameFormat = Objects.requireNonNull(logFileNameFormat);
        this.errFileNameFormat = Objects.requireNonNull(errFileNameFormat);
    }

    @Override
    public SevenDaysLogger withLogLevel(final int logLevel) {
        super.withLogLevel(logLevel);
        return this;
    }

    @Override
    public SevenDaysLogger withErrLevel(final int errLevel) {
        super.withErrLevel(errLevel);
        return this;
    }

    @Override
    public SevenDaysLogger withThreadNameEnabled(final boolean isThreadNameEnabled) {
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
                final String errorFileName = errFileNameFormat.formatted(ZonedDateTime.now().getDayOfWeek().getValue());
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
                final String errorFileName = errFileNameFormat.formatted(ZonedDateTime.now().getDayOfWeek().getValue());
                try (final PrintStream errStream = new PrintStream(new FileOutputStream(errorFileName, true), true, StandardCharsets.UTF_8)) {
                    errStream.println(s);
                    thrown.printStackTrace(errStream);
                } catch (final FileNotFoundException ignored) {
                }
            }
        }
    }

    private static void removeFileIfOld(final String fileName) {
        final File file = new File(fileName);
        if (file.exists() && file.lastModified() < System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000) {
            file.delete();
        }
    }

    private void ensureLogStreamRotation() {
        final ZonedDateTime now = ZonedDateTime.now();
        if (!now.isBefore(deadline)) {
            deadline = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            close();
            final int day = ZonedDateTime.now().getDayOfWeek().getValue();
            final String newLogFileName = logFileNameFormat.formatted(day);
            final String newErrFileName = errFileNameFormat.formatted(day);
            removeFileIfOld(newLogFileName);
            removeFileIfOld(newErrFileName);
            try {
                logStream = new PrintStream(new FileOutputStream(newLogFileName, true), true, StandardCharsets.UTF_8);
            } catch (final IOException e) {
                log(AbstractLogger.ERROR, "Cannot change log file", e);
            }
        }
    }
}
