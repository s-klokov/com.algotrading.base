package com.algotrading.base.core;

import java.util.Locale;

public final class PriceToString {

    private PriceToString() {
    }

    /**
     * @param price цена
     * @return строковое представление цены
     */
    public static String priceToString(final double price) {
        return priceToString(price, Locale.getDefault());
    }

    /**
     * @param price цена
     * @return строковое представление цены
     */
    public static String priceToString(final double price, final Locale locale) {
        if (price == Math.round(price)) {
            return Long.toString(Math.round(price));
        } else {
            final String priceString = String.format(locale, "%.8f", price);
            int i = priceString.length() - 1;
            while (i >= 0 && priceString.charAt(i) == '0') {
                i--;
            }
            if (i == 0) {
                return priceString;
            } else if (priceString.charAt(i) == '.') {
                return priceString.substring(0, i);
            } else {
                return priceString.substring(0, i + 1);
            }
        }
    }
}
