package com.algotrading.base.core.tester;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.commission.Commission;
import com.algotrading.base.core.series.FinSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Позиция по инструменту и несколько связанных с ней заявок.
 * Дополнительно имеется возможность вычисления эквити позиции.
 */
public class Position {
    /**
     * Код инструмента.
     */
    public final String secCode;
    /**
     * Набор заявок, куда будут добавляться заявки при изменении размера позиции.
     */
    public final FinSeries orders;
    /**
     * Размер позиции.
     */
    public long position = 0;
    /**
     * Набор активных заявок.
     */
    private final List<TestOrder> activeOrders = new ArrayList<>();
    /**
     * Текущая цена.
     */
    private double currentPrice = Double.NaN;
    /**
     * Шаг цены.
     */
    private double priceStep = 0;
    /**
     * Временной ряд эквити.
     */
    private FinSeries equity = null;
    /**
     * Кэш для вычисления эквити.
     */
    private double cash = 0;

    /**
     * Конструктор.
     *
     * @param secCode код инструмента
     * @param orders  набор, куда будут добавляться сработавшие заявки
     */
    public Position(final String secCode, final FinSeries orders) {
        this.secCode = secCode;
        this.orders = orders;
    }

    /**
     * Инициализировать временной ряд, где будет вычисляться эквити по мере поступления
     * новых свечей и изменения размера позиции.
     *
     * @return временной ряд эквити
     */
    public FinSeries initEquity() {
        cash = 0;
        equity = new FinSeries().withLongColumn(FinSeries.T).withDoubleColumn(Tester.EQUITY);
        return equity;
    }

    /**
     * Снять все активные заявки.
     */
    public void cancelAllOrders() {
        activeOrders.clear();
    }

    /**
     * Снять активную заявку.
     *
     * @param order заявка
     * @return {@code true}, если заявка была активной.
     */
    public boolean cancelOrder(final TestOrder order) {
        return activeOrders.remove(order);
    }

    /**
     * @return {@code true}, если имеются активные заявки.
     */
    public boolean hasActiveOrders() {
        return !activeOrders.isEmpty();
    }

    /**
     * @return список, содержащий активные заявки.
     */
    public List<TestOrder> activeOrders() {
        return new ArrayList<>(activeOrders);
    }

    /**
     * @return текущая цена.
     */
    public double currentPrice() {
        return currentPrice;
    }

    /**
     * Добавить заявку в список сработавших.
     *
     * @param t          метка времени
     * @param volume     объём
     * @param price      цена
     * @param commission комиссия
     * @param comment    комментарий
     */
    public void appendOrder(final long t, final long volume, final double price, final Commission commission, final String comment) {
        final double c = commission.getCommission(volume, secCode, price);
        Tester.appendOrder(orders, t, secCode, volume, price, c, comment);
        cash -= price * volume;
        cash -= c;
        position += volume;
        updateEquity(t, price);
    }

    private void updateEquity(final long t, final double price) {
        if (equity != null) {
            final LongColumn equityTimeCode = equity.timeCode();
            if (equityTimeCode.length() == 0 || equityTimeCode.getLast() != t) {
                equityTimeCode.append(t);
                equity.getDoubleColumn(Tester.EQUITY).append(cash + position * price);
            } else {
                final DoubleColumn equityColumn = equity.getDoubleColumn(Tester.EQUITY);
                equityColumn.set(equityColumn.length() - 1, cash + position * price);
            }
        }
    }

    /**
     * Установить лимитную заявку на покупку по цене не выше текущей цены.
     *
     * @param volume     объем заявки (положительный)
     * @param price      цена
     * @param commission комиссия
     * @param comment    комментарий
     * @return лимитная заявка на покупку
     */
    public TestLimitOrder buyLimit(final long volume, final double price, final Commission commission, final String comment) {
        if (volume <= 0) {
            throw new IllegalArgumentException("volume=" + volume);
        }
        if (price <= currentPrice) {
            final TestLimitOrder testLimitOrder = new TestLimitOrder(volume, price, commission, comment);
            activeOrders.add(testLimitOrder);
            return testLimitOrder;
        } else {
            throw new IllegalArgumentException("price=" + price + ", currentPrice=" + currentPrice);
        }
    }

    /**
     * Установить лимитную заявку на продажу по цене не ниже текущей цены.
     *
     * @param volume     объем заявки (отрицательный)
     * @param price      цена
     * @param commission комиссия
     * @param comment    комментарий
     * @return лимитная заявка на продажу
     */
    public TestLimitOrder sellLimit(final long volume, final double price, final Commission commission, final String comment) {
        if (volume >= 0) {
            throw new IllegalArgumentException("volume=" + volume);
        }
        if (price >= currentPrice) {
            final TestLimitOrder testLimitOrder = new TestLimitOrder(volume, price, commission, comment);
            activeOrders.add(testLimitOrder);
            return testLimitOrder;
        } else {
            throw new IllegalArgumentException("price=" + price + ", currentPrice=" + currentPrice);
        }
    }

    /**
     * Установить стоп-заявку на покупку по цене выше текущей цены.
     *
     * @param volume     объём заявки (положительный)
     * @param price      цена, при достижении которой происходит срабатывание заявки
     * @param commission комиссия
     * @param comment    комментарий
     * @return стоп-заявка на покупку
     */
    public TestStopOrder buyStop(final long volume, final double price, final Commission commission, final String comment) {
        if (volume <= 0) {
            throw new IllegalArgumentException("volume=" + volume);
        }
        if (price > currentPrice) {
            final TestStopOrder testStopOrder = new TestStopOrder(volume, price, commission, comment);
            activeOrders.add(testStopOrder);
            return testStopOrder;
        } else {
            throw new IllegalArgumentException("price=" + price + ", currentPrice=" + currentPrice);
        }
    }

    /**
     * Установить стоп-заявку на продажу по цене ниже текущей цены.
     *
     * @param volume     объём заявки (отрицательный)
     * @param price      цена, при достижении которой происходит срабатывание заявки
     * @param commission комиссия
     * @param comment    комментарий
     * @return стоп-заявка на продажу
     */
    public TestStopOrder sellStop(final long volume, final double price, final Commission commission, final String comment) {
        if (volume >= 0) {
            throw new IllegalArgumentException("volume=" + volume);
        }
        if (price < currentPrice) {
            final TestStopOrder testStopOrder = new TestStopOrder(volume, price, commission, comment);
            activeOrders.add(testStopOrder);
            return testStopOrder;
        } else {
            throw new IllegalArgumentException("price=" + price + ", currentPrice=" + currentPrice);
        }
    }

    /**
     * Задать пару связанных заявок для взятия тейк-профита или стоп-лосса.
     *
     * @param volume         объём заявки
     * @param takePrice      цена тейк-профита
     * @param takeCommission комиссия при тейк-профите
     * @param takeComment    комментарий при тейк-профите
     * @param stopPrice      цена стоп-лосса
     * @param stopCommission комиссия при стоп-лоссе
     * @param stopComment    комментарий при стоп-лоссе
     * @return пара связанных заявок
     */
    public TestTakeStopOrder setTakeProfitStopLoss(final long volume,
                                                   final double takePrice,
                                                   final Commission takeCommission,
                                                   final String takeComment,
                                                   final double stopPrice,
                                                   final Commission stopCommission,
                                                   final String stopComment) {
        if (volume == 0) {
            throw new IllegalArgumentException("Zero volume");
        }
        if (volume > 0 && takePrice > currentPrice || volume < 0 && takePrice < currentPrice) {
            throw new IllegalArgumentException("takePrice=" + takePrice);
        }
        if (volume > 0 && stopPrice <= currentPrice || volume < 0 && stopPrice >= currentPrice) {
            throw new IllegalArgumentException("stopPrice=" + stopPrice);
        }
        final TestTakeStopOrder takeStopOrder = new TestTakeStopOrder(volume,
                takePrice, takeCommission, takeComment,
                stopPrice, stopCommission, stopComment);
        activeOrders.add(takeStopOrder);
        return takeStopOrder;
    }

    /**
     * Появилась новая свеча.
     *
     * @param t         метка времени
     * @param open      цена открытия
     * @param high      максимальная цена
     * @param low       минимальная цена
     * @param close     цена закрытия
     * @param volume    объём
     * @param priceStep шаг цены
     * @return список сработавших заявок (возможно, пустой)
     */
    public List<TestOrder> onCandle(final long t,
                                    final double open,
                                    final double high,
                                    final double low,
                                    final double close,
                                    final long volume,
                                    final double priceStep) {
        this.priceStep = priceStep;
        if (Double.isNaN(currentPrice)) {
            currentPrice = open;
        }
        final List<TestOrder> ordersExecuted = new ArrayList<>();
        movePrice(t, open, true, ordersExecuted);
        if (open <= close) {
            movePrice(t, low, false, ordersExecuted);
            movePrice(t, high, false, ordersExecuted);
        } else {
            movePrice(t, high, false, ordersExecuted);
            movePrice(t, low, false, ordersExecuted);
        }
        // в методе movePrice(t, close, false, ordersExecuted) не будет новых срабатываний,
        // поэтому можно просто присвоить переменной currentPrice значение close
        currentPrice = close;
        updateEquity(t, close);
        return ordersExecuted;
    }

    /**
     * Смоделировать изменение цены от текущей цены к новой цене.
     *
     * @param t              временная метка
     * @param price          новая цена
     * @param hasGap         {@code true}, если был гэп
     * @param ordersExecuted список, куда будут добавляться сработавшие заявки
     */
    private void movePrice(final long t, final double price, final boolean hasGap, final List<TestOrder> ordersExecuted) {
        for (final Iterator<TestOrder> i = activeOrders.iterator(); i.hasNext(); ) {
            final TestOrder activeOrder = i.next();
            if (activeOrder instanceof final TestLimitOrder o) {
                if (crossed(o.price, currentPrice, price)) {
                    o.executionPrice = o.price;
                    appendOrder(t, o.volume, o.executionPrice, o.commission, o.comment);
                    i.remove();
                    ordersExecuted.add(o);
                }
            } else if (activeOrder instanceof final TestStopOrder o) {
                if (o.volume > 0 && price >= o.price - priceStep / 2
                        || o.volume < 0 && price <= o.price + priceStep / 2) {
                    o.executionPrice = hasGap ? price : o.price;
                    appendOrder(t, o.volume, o.executionPrice, o.commission, o.comment);
                    i.remove();
                    ordersExecuted.add(o);
                }
            } else if (activeOrder instanceof final TestTakeStopOrder o) {
                if (o.volume > 0 && price >= o.stopPrice - priceStep / 2
                        || o.volume < 0 && price <= o.stopPrice + priceStep / 2) {
                    o.executionPrice = hasGap ? price : o.stopPrice;
                    o.type = TestTakeStopOrder.Type.StopLoss;
                    appendOrder(t, o.volume, o.executionPrice, o.stopCommission, o.stopComment);
                    i.remove();
                    ordersExecuted.add(o);
                } else if (crossed(o.takePrice, currentPrice, price)) {
                    o.executionPrice = o.takePrice;
                    o.type = TestTakeStopOrder.Type.TakeProfit;
                    appendOrder(t, o.volume, o.executionPrice, o.takeCommission, o.takeComment);
                    i.remove();
                    ordersExecuted.add(o);
                }
            }
        }
        currentPrice = price;
    }

    /**
     * Узнать, пересекло ли движение цены от a к b ценовой уровень x с учётом шага цены.
     *
     * @param x ценовой уровень
     * @param a начало движения
     * @param b конец движения цены
     * @return {@code true}, если пересечение произошло
     */
    private boolean crossed(final double x, final double a, final double b) {
        return (a < x && x + priceStep / 2 < b) || (a > x && x - priceStep / 2 > b);
    }
}
