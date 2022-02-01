package com.algotrading.base.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Реализация простого логгирования.
 */
public class SimpleLogger extends AbstractLogger {

    protected final Object mutex = new Object();

    /**
     * Поток для вывода сообщений.
     */
    protected PrintStream logStream = System.out;
    /**
     * Поток для дублирования сообщений об ошибках, уровня {@link #errLevel} и выше.
     */
    protected PrintStream errStream = null;

    @Override
    public SimpleLogger withLogLevel(final int logLevel) {
        super.withLogLevel(logLevel);
        return this;
    }

    @Override
    public SimpleLogger withErrLevel(final int errLevel) {
        super.withErrLevel(errLevel);
        return this;
    }

    @Override
    public SimpleLogger withThreadNameEnabled(final boolean isThreadNameEnabled) {
        super.withThreadNameEnabled(isThreadNameEnabled);
        return this;
    }

    public SimpleLogger withLogStream(final PrintStream logStream) {
        synchronized (mutex) {
            this.logStream = logStream;
        }
        return this;
    }

    public SimpleLogger withErrStream(final PrintStream errStream) {
        synchronized (mutex) {
            this.errStream = errStream;
        }
        return this;
    }

    public SimpleLogger withLogFile(final File logFile) throws FileNotFoundException {
        return withLogStream(new PrintStream(new FileOutputStream(logFile, true), true, StandardCharsets.UTF_8));
    }

    public SimpleLogger withErrFile(final File errFile) throws FileNotFoundException {
        return withErrStream(new PrintStream(new FileOutputStream(errFile, true), true, StandardCharsets.UTF_8));
    }

    public void closeLogStream() {
        synchronized (mutex) {
            if (logStream != null) {
                logStream.close();
            }
        }
    }

    public void closeErrStream() {
        synchronized (mutex) {
            if (errStream != null) {
                errStream.close();
            }
        }
    }

    @Override
    public synchronized void close() {
        synchronized (mutex) {
            closeLogStream();
            closeErrStream();
        }
    }

    @Override
    protected void print(final int level, final String s) {
        synchronized (mutex) {
            logStream.println(s);
            if (level >= errLevel && errStream != null) {
                errStream.println(s);
            }
        }
    }

    @Override
    protected void print(final int level, final String s, final Throwable thrown) {
        synchronized (mutex) {
            logStream.println(s);
            thrown.printStackTrace(logStream);
            if (level >= errLevel && errStream != null) {
                errStream.println(s);
                thrown.printStackTrace(errStream);
            }
        }
    }
}
