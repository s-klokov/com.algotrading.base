package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.commission.Commission;
import com.algotrading.base.core.series.Series;

/**
 * Вычисление индикатора Equity.
 */
public final class Equity {

    private Equity() {
        throw new UnsupportedOperationException();
    }

    /**
     * Вычислить индикатор Equity.
     *
     * @param secCode            код инструмента (для вычисления комиссии)
     * @param series             временной ряд
     * @param priceColumnName    название колонки, где указаны цены сделок
     * @param positionColumnName название колонки, где указаны целевые размеры позиции (после сделок)
     * @param commission         комиссия
     * @param equityColumnName   название колонки, куда будет записаны значения эквити
     * @return колонка со значениями эквити
     */
    public static DoubleColumn equity(final String secCode,
                                      final Series series,
                                      final String priceColumnName,
                                      final String positionColumnName,
                                      final Commission commission,
                                      final String equityColumnName) {
        final DoubleColumn priceColumn = series.getDoubleColumn(priceColumnName);
        final LongColumn positionColumn = series.getLongColumn(positionColumnName);
        final DoubleColumn equityColumn = series.acquireDoubleColumn(equityColumnName);
        final int len = priceColumn.length();

        double cash = 0;
        long posPrev = 0;
        for (int i = 0; i < len; i++) {
            final long posCurr = positionColumn.get(i);
            final double price = priceColumn.get(i);
            final long volume = posCurr - posPrev;
            if (volume != 0) {
                cash -= price * volume;
                cash -= commission.getCommission(volume, secCode, price);
            }
            equityColumn.set(i, cash + posCurr * price);
            posPrev = posCurr;
        }

        return equityColumn;
    }
}
