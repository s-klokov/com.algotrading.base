package com.algotrading.base.core.commission;

/**
 * Реализация комиссии как доли оборота.
 */
public final class SimpleCommission implements Commission {
    /**
     * Доля комиссии в обороте.
     */
    private final double c;

    /**
     * @param percent процент оборота
     * @return объект, реализующий комиссию
     */
    public static SimpleCommission ofPercent(final double percent) {
        return new SimpleCommission(percent);
    }

    /**
     * Конструктор.
     *
     * @param percent значение комиссии как доли оборота, выраженной в процентах
     */
    public SimpleCommission(final double percent) {
        c = percent / 100.0;
    }

    @Override
    public double getCommission(final double volume, final String secCode, final double price) {
        return Math.abs(price * volume) * c;
    }
}
