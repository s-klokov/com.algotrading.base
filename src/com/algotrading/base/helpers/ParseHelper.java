package com.algotrading.base.helpers;

public class ParseHelper {

    private ParseHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * Из объекта получить число типа {@code double}.
     *
     * @param o объект
     * @return число или {@link Double#NaN}, если не удалось
     */
    public static double asDouble(final Object o) {
        if (o == null) {
            return Double.NaN;
        }
        if (o instanceof Double) {
            return (double) o;
        }
        if (o instanceof Long) {
            return (long) o;
        }
        try {
            return Double.parseDouble(o.toString());
        } catch (final NumberFormatException e) {
            return Double.NaN;
        }
    }

    /**
     * Из объекта получить число типа {@code long}.
     *
     * @param o объект
     * @return число или 0, если не удалось
     */
    public static long asLong(final Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Long) {
            return (long) o;
        }
        try {
            return Long.parseLong(o.toString());
        } catch (final NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Из объекта получить булево значение.
     *
     * @param o объект
     * @return {@code true} или {@code false}
     */
    public static boolean asBoolean(final Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return (boolean) o;
        }
        return Boolean.parseBoolean(o.toString());
    }
}
