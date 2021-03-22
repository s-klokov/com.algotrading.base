package com.algotrading.base.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

/**
 * Простой логгер с записью в ежедневные файлы лога, дублируя сообщения об ошибках в отдельные файлы
 * с фрагментом _error в названии файла.
 */
public class DailyLogger {
    /**
     * Префикс файла, который будет открыт в режиме "append", по умолчанию, путь и имя файла.
     */
    private final String fileNamePrefix;
    /**
     * Суффикс файла, который будет открыт в режиме "append".
     */
    private final String fileNameSuffix;
    /**
     * Время, после которого нужно произвести смену файла лога таймера.
     */
    private ZonedDateTime deadline = ZonedDateTime.now();
    /**
     * Идентификатор режима вывода отладочных сообщений.
     */
    private volatile boolean debugEnabled = true;
    /**
     * Файл логгера.
     */
    private PrintStream ps = null;

    /**
     * Конструктор
     *
     * @param fileNamePrefix префикс файла, который будет открыт в режиме "append", по умолчанию, путь и имя файла
     * @param fileNameSuffix суффикс файла, который будет открыт в режиме "append"
     */
    public DailyLogger(final String fileNamePrefix, final String fileNameSuffix) {
        this.fileNamePrefix = fileNamePrefix;
        this.fileNameSuffix = fileNameSuffix;
    }

    /**
     * Вывести сообщение в лог.
     *
     * @param messageType тип сообщения
     * @param message     текст сообщения
     */
    private synchronized void log(final String messageType, final String message) {
        changeLogFileAndResetDeadline();
        if (ps != null) {
            ps.printf("%tF %<tT.%<tL %s <%s> %s%n",
                      System.currentTimeMillis(), messageType, Thread.currentThread().getName(), message);
        }
    }

    /**
     * Получить имя файла для логгера.
     *
     * @param preSuffix фрагмент названия файла перед суффиксом.
     * @return имя файла
     */
    private String getFileName(final String preSuffix) {
        return String.format("%s%tY%<tm%<td%s%s", fileNamePrefix, System.currentTimeMillis(), preSuffix, fileNameSuffix);
    }

    /**
     * Обновить имя файла для логера.
     */
    private void changeLogFileAndResetDeadline() {
        final ZonedDateTime now = ZonedDateTime.now();
        if (!now.isBefore(deadline)) {
            deadline = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            final String newFileName = getFileName("");
            try {
                final PrintStream psNew = new PrintStream(new FileOutputStream(newFileName, true), true, StandardCharsets.UTF_8);
                close();
                ps = psNew;
            } catch (final IOException e) {
                error("Cannot change log file");
                error(e.getMessage());
            }
        }
    }

    /**
     * Установить режим вывода отладочных сообщений.
     *
     * @param debugEnabled true/false
     */
    public void setDebugEnabled(final boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    /**
     * Вывести отладочное сообщение, если включён режим вывода отладочных сообщений.
     *
     * @param message текст сообщения
     */
    public void debug(final String message) {
        if (debugEnabled) {
            log("[DEBUG]", message);
        }
    }

    /**
     * Вывести информационное сообщение.
     *
     * @param message текст сообщения
     */
    public void info(final String message) {
        log("[INFO ]", message);
    }

    /**
     * Вывести предупреждение.
     *
     * @param message текст сообщения
     */
    public void warning(final String message) {
        log("[WARN ]", message);
    }

    /**
     * Вывести сообщение об ошибке.
     *
     * @param message текст сообщения
     */
    public void error(final String message) {
        log("[ERROR]", message);
        final String errorFileName = getFileName("_error");
        try (final PrintStream psError = new PrintStream(new FileOutputStream(errorFileName, true), true, StandardCharsets.UTF_8)) {
            psError.printf("%tF %<tT.%<tL %s <%s> %s%n",
                           System.currentTimeMillis(), "[ERROR]", Thread.currentThread().getName(), message);
        } catch (final FileNotFoundException ignored) {
        }
    }

    /**
     * Вывести сообщение об исключении.
     *
     * @param e исключение.
     */
    public void error(final Exception e) {
        final StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getCanonicalName()).append(": ").append(e.getMessage()).append("\r\n");
        final StackTraceElement[] stackTrace = e.getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.toString()).append("\r\n");
        }
        error(sb.toString());
    }

    /**
     * Закрыть файл, куда логгер выводил сообщения.
     */
    public synchronized void close() {
        if (ps != null) {
            ps.close();
            ps = null;
        }
    }
}
