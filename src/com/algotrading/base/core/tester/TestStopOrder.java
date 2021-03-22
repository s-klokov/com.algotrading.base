package com.algotrading.base.core.tester;

import com.algotrading.base.core.commission.Commission;

/**
 * Стоп-заявка для тестера.
 * <p>
 * Заявка на покупку/продажу устанавливается выше/ниже текущей цены и срабатывает, если текущая цена становится
 * не ниже/не выше указанной. Цена исполнения при отсутствии гэпов равна указанной в заявке.
 */
public class TestStopOrder extends TestOrder {

    public final double price;
    public final Commission commission;
    public final String comment;

    public TestStopOrder(final long volume, final double price, final Commission commission, final String comment) {
        super(volume);
        this.price = price;
        this.commission = commission;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "StopOrder " + volume + " @ " + price + " (" + executionPrice + "), comment=" + comment;
    }
}
