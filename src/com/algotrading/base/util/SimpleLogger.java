package com.algotrading.base.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.function.Supplier;

/**
 * Реализация простого логгирования.
 */
public class SimpleLogger {

    public static final int FATAL = 6;
    public static final int ERROR = 5;
    public static final int WARN = 4;
    public static final int INFO = 3;
    public static final int DEBUG = 2;
    public static final int TRACE = 1;

    protected final Object mutex = new Object();

    /**
     * Поток для вывода сообщений.
     */
    protected PrintStream logStream = System.out;
    /**
     * Поток для дублирования сообщений об ошибках, уровня {@link #errLevel} и выше.
     */
    protected PrintStream errStream = null;
    /**
     * Уровень логгирования.
     */
    protected volatile int logLevel = INFO;
    /**
     * Уровень логгирования для ошибок.
     */
    protected volatile int errLevel = ERROR;
    /**
     * Нужно ли выводить имя вычислительного потока.
     */
    protected volatile boolean isThreadNameEnabled = true;

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

    public SimpleLogger withLogLevel(final int logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public SimpleLogger withErrLevel(final int errLevel) {
        this.errLevel = errLevel;
        return this;
    }

    public SimpleLogger withThreadNameEnabled(final boolean isThreadNameEnabled) {
        this.isThreadNameEnabled = isThreadNameEnabled;
        return this;
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

    public synchronized void close() {
        synchronized (mutex) {
            closeLogStream();
            closeErrStream();
        }
    }

    private static String messageType(final int level) {
        if (level >= FATAL) {
            return "[FATAL]";
        } else if (level >= ERROR) {
            return "[ERROR]";
        } else if (level >= WARN) {
            return "[WARN ]";
        } else if (level >= INFO) {
            return "[INFO ]";
        } else if (level >= DEBUG) {
            return "[DEBUG]";
        } else {
            return "[TRACE]";
        }
    }

    protected static String getMessage(final Supplier<String> messageSupplier) {
        try {
            return messageSupplier.get();
        } catch (final Throwable t) {
            return t.toString();
        }
    }

    protected String getLogString(final int level, final String message) {
        final StringBuilder sb = new StringBuilder(message.length() + (isThreadNameEnabled ? 50 : 32));
        try (final Formatter f = new Formatter(sb)) {
            f.format("%tF %<tT.%<tL", System.currentTimeMillis());
        }
        sb.append(' ').append(messageType(level)).append(' ');
        if (isThreadNameEnabled) {
            sb.append('<').append(Thread.currentThread().getName()).append("> ");
        }
        sb.append(message);
        return sb.toString();
    }

    protected void print(final int level, final String s) {
        synchronized (mutex) {
            logStream.println(s);
            if (level >= errLevel && errStream != null) {
                errStream.println(s);
            }
        }
    }

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

    public void log(final int level, final String message) {
        if (level >= logLevel) {
            print(level, getLogString(level, message));
        }
    }

    public void log(final int level, final Supplier<String> messageSupplier) {
        if (level >= logLevel) {
            print(level, getLogString(level, getMessage(messageSupplier)));
        }
    }

    public void log(final int level, final String message, final Throwable thrown) {
        if (level >= logLevel) {
            print(level, getLogString(level, message), thrown);
        }
    }

    public void log(final int level, final Throwable thrown, final Supplier<String> messageSupplier) {
        if (level >= logLevel) {
            print(level, getLogString(level, getMessage(messageSupplier)), thrown);
        }
    }

    public void fatal(final String message) {
        log(FATAL, message);
    }

    public void fatal(final Supplier<String> messageSupplier) {
        log(FATAL, messageSupplier);
    }

    public void error(final String message) {
        log(ERROR, message);
    }

    public void error(final Supplier<String> messageSupplier) {
        log(ERROR, messageSupplier);
    }

    public void warn(final String message) {
        log(WARN, message);
    }

    public void warn(final Supplier<String> messageSupplier) {
        log(WARN, messageSupplier);
    }

    public void info(final String message) {
        log(INFO, message);
    }

    public void info(final Supplier<String> messageSupplier) {
        log(INFO, messageSupplier);
    }

    public void debug(final String message) {
        log(DEBUG, message);
    }

    public void debug(final Supplier<String> messageSupplier) {
        log(DEBUG, messageSupplier);
    }

    public void trace(final String message) {
        log(TRACE, message);
    }

    public void trace(final Supplier<String> messageSupplier) {
        log(TRACE, messageSupplier);
    }
}
