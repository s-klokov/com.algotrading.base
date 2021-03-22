package com.algotrading.base.lib;

import java.io.IOException;

/**
 * Ошибка формата данных.
 */
public class DataFormatException extends IOException {

    public DataFormatException(final String message) {
        super(message);
    }

    public DataFormatException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
