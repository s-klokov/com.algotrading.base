package com.algotrading.base.core.marketdata.futures;

import java.time.LocalDate;

public class Futures {

    public final String prefix;
    public final String longCode;
    public final String shortCode;
    public final int expiry;
    public final int rolling;
    public final int previousExpiry;
    public final LocalDate expiryDate;
    public final LocalDate oneDayBeforeExpiryDate;
    public final LocalDate previousExpiryDate;

    protected Futures(final String prefix,
                      final String longCode,
                      final String shortCode,
                      final int expiry,
                      final int rolling,
                      final int previousExpiry) {
        this.prefix = prefix;
        this.longCode = longCode;
        this.shortCode = shortCode;
        this.expiry = expiry;
        this.rolling = rolling;
        this.previousExpiry = previousExpiry;
        expiryDate = localDate(expiry);
        oneDayBeforeExpiryDate = localDate(rolling);
        previousExpiryDate = localDate(previousExpiry);
    }

    private static LocalDate localDate(final int yyyymmdd) {
        int v = yyyymmdd;
        final int day = v % 100;
        v /= 100;
        final int month = v % 100;
        v /= 100;
        final int year = v;
        return LocalDate.of(year, month, day);
    }

    private static final String FUT_LETTERS = "FGHJKMNQUVXZ";

    /**
     * Узнать, является ли инструмент фьючерсом.
     *
     * @param secCode код инструмента
     * @return true, если это фьючерс
     */
    public static boolean isFutures(final String secCode) {
        if (secCode == null) {
            return false;
        }
        final int len = secCode.length();
        if (len < 4 || len > 6) {
            return false;
        }
        final char ch1 = secCode.charAt(len - 1);
        final char ch2 = secCode.charAt(len - 2);
        return '0' <= ch1 && ch1 <= '9'
                && (FUT_LETTERS.indexOf(ch2) >= 0 || '0' <= ch2 && ch2 <= '9' && FUT_LETTERS.indexOf(secCode.charAt(len - 3)) >= 0);
    }

    /**
     * Получить суффикс фьючерса, если это фьючерс.
     * Предполагается, что суффикс фьючерса имеет вид типа "U7" или "U07".
     *
     * @param secCode код инструмента
     * @return суффикс фьючерса, если это фьючерс,
     * пустая строка, если не фьючерс,
     * и {@code null}, если аргумент {@code null}.
     */
    public static String getSuffix(final String secCode) {
        if (secCode == null) {
            return null;
        }
        final int len = secCode.length();
        if (len < 4 || len > 6) {
            return "";
        }
        final char ch1 = secCode.charAt(len - 1);
        if ('0' <= ch1 && ch1 <= '9') {
            final char ch2 = secCode.charAt(len - 2);
            if (FUT_LETTERS.indexOf(ch2) >= 0) {
                return secCode.substring(len - 2);
            }
            if ('0' <= ch2 && ch2 <= '9') {
                final char ch3 = secCode.charAt(len - 3);
                if (FUT_LETTERS.indexOf(ch3) >= 0) {
                    return secCode.substring(len - 3);
                }
            }
        }
        return "";
    }

    /**
     * @param secCode код инструмента
     * @return префикс фьючерса, если это фьючерс, или исходный код инструмента.
     */
    public static String getPrefix(final String secCode) {
        if (secCode == null) {
            return null;
        }
        final int len = secCode.length();
        if (len < 4 || len > 6) {
            return secCode;
        }
        final char ch1 = secCode.charAt(len - 1);
        if ('0' <= ch1 && ch1 <= '9') {
            final char ch2 = secCode.charAt(len - 2);
            if (FUT_LETTERS.indexOf(ch2) >= 0) {
                return secCode.substring(0, len - 2);
            }
            if ('0' <= ch2 && ch2 <= '9') {
                final char ch3 = secCode.charAt(len - 3);
                if (FUT_LETTERS.indexOf(ch3) >= 0) {
                    return secCode.substring(0, len - 3);
                }
            }
        }
        return secCode;
    }
}
