package com.algotrading.base.core.marketdata.locators;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public abstract class CandleDataLocator {

    protected int from = 1900_01_01;
    protected int till = 2099_12_31;

    public CandleDataLocator from(final int yyyymmdd) {
        from = yyyymmdd;
        return this;
    }

    public CandleDataLocator from(final LocalDate localDate) {
        from = yyyymmdd(localDate);
        return this;
    }

    public CandleDataLocator till(final int yyyymmdd) {
        till = yyyymmdd;
        return this;
    }

    public CandleDataLocator till(final LocalDate localDate) {
        till = yyyymmdd(localDate);
        return this;
    }

    /**
     * Получить для указанного кода инструмента набор файлов, откуда нужно прочитать свечные данные.
     *
     * @param secCode код инструмента
     * @return список файлов с данными
     */
    public abstract List<File> getFiles(String secCode);

    private static int yyyymmdd(final LocalDate localDate) {
        return localDate.getYear() * 10000 + localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
    }
}
