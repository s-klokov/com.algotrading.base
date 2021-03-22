package com.algotrading.base.core.commission;

/**
 * Комиссия и проскальзывание в сделке.
 */
public interface Commission {

    /**
     * Вычислить размер комиссии в сделке.
     *
     * @param volume  объём сделки
     * @param secCode код инструмента
     * @param price   цена сделки
     * @return размер комиссии в сделке
     */
    double getCommission(double volume, String secCode, double price);
}
