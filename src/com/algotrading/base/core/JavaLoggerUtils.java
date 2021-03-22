package com.algotrading.base.core;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.*;

public class JavaLoggerUtils {
    private JavaLoggerUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Удалить все хэндлеры у корневого логгера.
     */
    public static void removeRootLoggerHandlers() {
        final Logger rootLogger = Logger.getLogger("");
        for (final Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
    }

    /**
     * @return стандартный форматтер для логгирования.
     */
    public static Formatter getLogFormatter() {
        return new Formatter() {
            @Override
            public String format(final LogRecord record) {
                final Throwable t = record.getThrown();
                if (t == null) {
                    return String.format(Locale.US, "%tF %<tT.%<tL [%s] <Thread-%d> %s%n",
                                         record.getMillis(),
                                         record.getLevel(),
                                         record.getThreadID(),
                                         record.getMessage());
                } else {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(String.format(Locale.US, "%tF %<tT.%<tL [%s] <Thread-%d> %s%n",
                                            record.getMillis(),
                                            record.getLevel(),
                                            record.getThreadID(),
                                            record.getMessage()));
                    for (final StackTraceElement ste : t.getStackTrace()) {
                        sb.append(ste.toString()).append("\r\n");
                    }
                    sb.append("\r\n");
                    return sb.toString();
                }
            }
        };
    }

    /**
     * Получить файловый хэндлер для логгирования со стандартным форматтером.
     *
     * @param fileName имя файла лога
     * @param append   true/false
     * @return файловый хэндлер лога
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static FileHandler getFileHandler(final String fileName, final boolean append) throws IOException {
        final FileHandler handler = new FileHandler(fileName, append);
        handler.setFormatter(getLogFormatter());
        return handler;
    }

    /**
     * Получить консольный хэндлер для логгирования в System.out со стандартным форматтером.
     *
     * @return консольный хэндлер
     */
    public static ConsoleHandler getConsoleHandler() {
        final ConsoleHandler handler = new ConsoleHandler() {{
            setOutputStream(System.out);
        }};
        handler.setFormatter(getLogFormatter());
        return handler;
    }
}
