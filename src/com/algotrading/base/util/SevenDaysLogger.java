package com.algotrading.base.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Логгер, осуществляющий запись в ежедневные файлы лога и дублирующий сообщения об ошибках
 * в отдельные файлы с фрагментом "_error" в названии файла.
 * Файлы лога в своём названии содержат номер дня недели в виде числа от 1 (понедельник) до 7 (воскресенье).
 * Старые файлы перезаписываются новыми в процессе работы.
 */
public class SevenDaysLogger extends AbstractLogger {
    private static final String ERR_PRE_SUFFIX = "_error";
    /**
     * Объект для синхронизации.
     */
    protected final Object mutex = new Object();
    /**
     * Префикс имени файла лога.
     */
    protected final String fileNamePrefix;
    /**
     * Суффикс имени файла лога.
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
    public SevenDaysLogger(final String fileNamePrefix, final String fileNameSuffix) {
        this.fileNamePrefix = Objects.requireNonNull(fileNamePrefix);
        this.fileNameSuffix = Objects.requireNonNull(fileNameSuffix);
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
                final String errorFileName = getFileName(ERR_PRE_SUFFIX);
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
                final String errorFileName = getFileName(ERR_PRE_SUFFIX);
                try (final PrintStream errStream = new PrintStream(new FileOutputStream(errorFileName, true), true, StandardCharsets.UTF_8)) {
                    errStream.println(s);
                    thrown.printStackTrace(errStream);
                } catch (final FileNotFoundException ignored) {
                }
            }
        }
    }

    private String getFileName(final String preSuffix) {
        return String.format("%s%d%s%s", fileNamePrefix, ZonedDateTime.now().getDayOfWeek().getValue(), preSuffix, fileNameSuffix);
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
            final String newLogFileName = getFileName("");
            final String newErrFileName = getFileName(ERR_PRE_SUFFIX);
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
