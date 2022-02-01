package com.algotrading.base.util;

import java.util.Formatter;
import java.util.function.Supplier;

public abstract class AbstractLogger {
    public static final int FATAL = 6;
    public static final int ERROR = 5;
    public static final int WARN = 4;
    public static final int INFO = 3;
    public static final int DEBUG = 2;
    public static final int TRACE = 1;
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

    protected static String messageType(final int level) {
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

    public AbstractLogger withLogLevel(final int logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public AbstractLogger withErrLevel(final int errLevel) {
        this.errLevel = errLevel;
        return this;
    }

    public AbstractLogger withThreadNameEnabled(final boolean isThreadNameEnabled) {
        this.isThreadNameEnabled = isThreadNameEnabled;
        return this;
    }

    public abstract void close();

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

    protected abstract void print(int level, String s);

    protected abstract void print(int level, String s, Throwable thrown);

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
