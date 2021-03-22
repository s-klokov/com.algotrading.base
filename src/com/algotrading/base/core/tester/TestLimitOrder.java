package com.algotrading.base.core.tester;

import com.algotrading.base.core.commission.Commission;

/**
 * Лимитная заявка для тестера.
 * <p>
 * Заявка на покупку/продажу выставляется по цене не выше/не ниже текущей и срабатывает, если текущая цена становится
 * ниже/выше указанной в заявке. Цена исполнения при отсутствии гэпов равна указанной в заявке.
 */
public class TestLimitOrder extends TestOrder {

    public final double price;
    public final Commission commission;
    public final String comment;

    public TestLimitOrder(final long volume, final double price, final Commission commission, final String comment) {
        super(volume);
        this.price = price;
        this.commission = commission;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "LimitOrder " + volume + " @ " + price + " (" + executionPrice + "), comment=" + comment;
    }
}
