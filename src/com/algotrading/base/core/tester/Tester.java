package com.algotrading.base.core.tester;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.columns.StringColumn;
import com.algotrading.base.core.commission.Commission;
import com.algotrading.base.core.csv.CsvWriter;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.series.Series;
import com.algotrading.base.core.sync.Synchronizer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

/**
 * Реализация тестов на истории.
 * <p>
 * Задаются временные ряды свечных данных для набора инструментов
 * и список заявок на покупку/продажу с указанием времени, инструмента, цены и количества.
 * После этого тестер вычисляет результаты торговли (временной ряд equity, количество сделок,
 * доход, профит-фактор и пр.).
 */
public class Tester {
    /**
     * Название колонки эквити.
     */
    public static final String EQUITY = "Equity";
    /**
     * Название колонки использованного капитала.
     */
    public static final String CAPITAL_USED = "CapitalUsed";
    /**
     * Временные ряды инструментов.
     */
    private final Map<String, FinSeries> securitiesMap = new HashMap<>();
    /**
     * Синхронизатор временных рядов.
     */
    private final Synchronizer synchronizer = new Synchronizer();
    /**
     * Список заявок на покупку/продажу.
     */
    private FinSeries orders = newOrders();
    private LongColumn ordersTimeCode = orders.timeCode();
    private StringColumn ordersSecurity = orders.getStringColumn("Security");
    private LongColumn ordersVolume = orders.volume();
    private DoubleColumn ordersPrice = orders.getDoubleColumn("Price");
    private DoubleColumn ordersCommission = orders.getDoubleColumn("Commission");
    private StringColumn ordersComment = orders.getStringColumn("Comment");
    /**
     * Завершённые трейды.
     */
    private final List<TestTrade> doneTrades = new ArrayList<>();
    /**
     * Активные трейды.
     */
    private final List<TestTrade> activeTrades = new ArrayList<>();
    /**
     * Equity, используемый объём капитала.
     */
    private final FinSeries equityAndCapitalUsed = new FinSeries()
            .withLongColumn(FinSeries.T)
            .withDoubleColumn(EQUITY)
            .withDoubleColumn(CAPITAL_USED);
    private final LongColumn equityTimeCode = equityAndCapitalUsed.timeCode();
    private final DoubleColumn equity = equityAndCapitalUsed.getDoubleColumn(EQUITY);
    private final DoubleColumn capitalUsed = equityAndCapitalUsed.getDoubleColumn(CAPITAL_USED);
    /**
     * Режим определения просадки.
     */
    public DrawdownMode drawdownMode = DrawdownMode.FixedCapital;
    /**
     * Начальный капитал.
     */
    private double initialCapital = Double.NaN;
    /**
     * Значение profit для завершённых трейдов.
     */
    private double doneTradesProfit = 0;
    /**
     * Индекс очередного элемента заявок.
     */
    private int ordersIndex = 0;
    /**
     * Максимальное значение equity.
     */
    private double maxEquity = 0;
    /**
     * Максимальная просадка системы.
     */
    private double maxSysDrawDownPercent = 0;
    /**
     * Торговый оборот.
     */
    private double turnover = 0;
    /**
     * Суммарная комиссия.
     */
    private double totalCommission = 0;

    /**
     * @return заголовок для строк с базовой статистикой.
     */
    public static String getStatsHeader() {
        return "InitialCapital; NetProfit(%); MaxSysDD(%); ProfitFactor; AvgProfit(%); TradesCount";
    }

    /**
     * Инициализировать ряд заявок.
     *
     * @return ряд заявок.
     */
    public static FinSeries newOrders() {
        return new FinSeries()
                .withLongColumn(FinSeries.T)
                .withStringColumn("Security")
                .withLongColumn(FinSeries.V)
                .withDoubleColumn("Price")
                .withDoubleColumn("Commission")
                .withStringColumn("Comment");
    }

    /**
     * Добавить заявку в ряд заявок.
     *
     * @param orders     ряд заявок
     * @param timeCode   метка времени
     * @param security   код инструмента
     * @param volume     объём заявки
     * @param price      цена
     * @param commission комиссия
     * @param comment    комментарий
     */
    public static void appendOrder(final Series orders,
                                   final long timeCode,
                                   final String security,
                                   final long volume,
                                   final double price,
                                   final double commission,
                                   final String comment) {
        orders.getLongColumn(FinSeries.T).append(timeCode);
        orders.getStringColumn("Security").append(security);
        orders.getLongColumn(FinSeries.V).append(volume);
        orders.getDoubleColumn("Price").append(price);
        orders.getDoubleColumn("Commission").append(commission);
        orders.getStringColumn("Comment").append(comment);
    }

    /**
     * Добавить заявку в ряд заявок.
     *
     * @param orders     ряд заявок
     * @param timeCode   метка времени
     * @param security   код инструмента
     * @param volume     объём заявки
     * @param price      цена
     * @param commission комиссия
     * @param comment    комментарий
     */
    public static void appendOrder(final Series orders,
                                   final long timeCode,
                                   final String security,
                                   final long volume,
                                   final double price,
                                   final Commission commission,
                                   final String comment) {
        appendOrder(orders, timeCode, security, volume, price, commission.getCommission(volume, security, price), comment);
    }

    public static void writeEquity(final Series equity, final String fileName) throws IOException {
        try (final PrintStream ps = new PrintStream(fileName, StandardCharsets.UTF_8)) {
            new CsvWriter()
                    .locale(Locale.getDefault())
                    .header("Date;Time;Equity,%")
                    .separator(";")
                    .column(equity.getLongColumn("T"), t -> String.format("%02d.%02d.%04d",
                                                                          TimeCodes.day(t),
                                                                          TimeCodes.month(t),
                                                                          TimeCodes.year(t)))
                    .column(equity.getLongColumn("T"), t -> String.format("%02d:%02d:%02d",
                                                                          TimeCodes.hour(t),
                                                                          TimeCodes.min(t),
                                                                          TimeCodes.sec(t)))
                    .column(equity.getDoubleColumn(EQUITY), "%.4f")
                    .write(ps, 0, equity.length());
        }
    }

    public static void writeEquityAndCapitalUsed(final Series equity, final String fileName) throws IOException {
        try (final PrintStream ps = new PrintStream(fileName, StandardCharsets.UTF_8)) {
            new CsvWriter()
                    .locale(Locale.getDefault())
                    .header("Date;Time;Equity,%;CapitalUsed,%")
                    .separator(";")
                    .column(equity.getLongColumn("T"), t -> String.format("%02d.%02d.%04d",
                                                                          TimeCodes.day(t),
                                                                          TimeCodes.month(t),
                                                                          TimeCodes.year(t)))
                    .column(equity.getLongColumn("T"), t -> String.format("%02d:%02d:%02d",
                                                                          TimeCodes.hour(t),
                                                                          TimeCodes.min(t),
                                                                          TimeCodes.sec(t)))
                    .column(equity.getDoubleColumn(EQUITY), "%.4f")
                    .column(equity.getDoubleColumn(CAPITAL_USED), "%.4f")
                    .write(ps, 0, equity.length());
        }
    }

    /**
     * Задать начальный капитал.
     *
     * @param initialCapital начальный капитал.
     */
    public void setInitialCapital(final double initialCapital) {
        this.initialCapital = initialCapital;
    }

    /**
     * Получить размер начального капитала.
     *
     * @return размер начального капитала.
     */
    public double getInitialCapital() {
        return initialCapital;
    }

    /**
     * Задать временной ряд.
     *
     * @param key    код инструмента
     * @param series временной ряд
     */
    public void setSeries(final String key, final FinSeries series) {
        securitiesMap.put(key, series);
        synchronizer.put(series.timeCode());
    }

    /**
     * Добавить заявки на покупку/продажу.
     *
     * @param newOrders объект типа {@link Series} с колонками, заданными в методе {@link #newOrders()}.
     */
    public void addOrders(final FinSeries newOrders) {
        final int len2 = newOrders.length();
        if (len2 == 0) {
            return;
        }
        if (!orders.hasSameColumnsAs(newOrders)) {
            throw new IllegalArgumentException("Columns mismatch in " + this + " and " + newOrders);
        }
        if (!newOrders.timeCode().isNonDecreasing()) {
            throw new IllegalArgumentException("Column " + newOrders.timeCode() + " should be non-decreasing");
        }
        final int len1 = orders.length();
        if (len1 == 0) {
            orders = newOrders;
            ordersTimeCode = newOrders.timeCode();
            ordersSecurity = newOrders.getStringColumn("Security");
            ordersVolume = newOrders.volume();
            ordersPrice = newOrders.getDoubleColumn("Price");
            ordersCommission = newOrders.getDoubleColumn("Commission");
            ordersComment = newOrders.getStringColumn("Comment");
            return;
        }
        final LongColumn timeCode1 = ordersTimeCode;
        final LongColumn timeCode2 = newOrders.timeCode();
        final StringColumn security1 = ordersSecurity;
        final StringColumn security2 = newOrders.getStringColumn("Security");
        final LongColumn volume1 = ordersVolume;
        final LongColumn volume2 = newOrders.volume();
        final DoubleColumn price1 = ordersPrice;
        final DoubleColumn price2 = newOrders.getDoubleColumn("Price");
        final DoubleColumn commission1 = ordersCommission;
        final DoubleColumn commission2 = newOrders.getDoubleColumn("Commission");
        final StringColumn comment1 = ordersComment;
        final StringColumn comment2 = newOrders.getStringColumn("Comment");

        orders = newOrders();
        orders.ensureCapacity(len1 + len2);
        ordersTimeCode = orders.timeCode();
        ordersSecurity = orders.getStringColumn("Security");
        ordersVolume = orders.volume();
        ordersPrice = orders.getDoubleColumn("Price");
        ordersCommission = orders.getDoubleColumn("Commission");
        ordersComment = orders.getStringColumn("Comment");

        final Synchronizer sync = new Synchronizer();
        sync.put(timeCode1);
        sync.put(timeCode2);
        while (sync.synchronize() != Long.MAX_VALUE) {
            int index = sync.getUpdatedIndex(timeCode1);
            if (index >= 0) {
                ordersTimeCode.append(timeCode1.get(index));
                ordersSecurity.append(security1.get(index));
                ordersVolume.append(volume1.get(index));
                ordersPrice.append(price1.get(index));
                ordersCommission.append(commission1.get(index));
                ordersComment.append(comment1.get(index));
            }
            index = sync.getUpdatedIndex(timeCode2);
            if (index >= 0) {
                ordersTimeCode.append(timeCode2.get(index));
                ordersSecurity.append(security2.get(index));
                ordersVolume.append(volume2.get(index));
                ordersPrice.append(price2.get(index));
                ordersCommission.append(commission2.get(index));
                ordersComment.append(comment2.get(index));
            }
        }
    }

    /**
     * Провести тест.
     */
    public void test() {
        doneTrades.clear();
        activeTrades.clear();
        equityAndCapitalUsed.setLength(0);
        synchronizer.reset();
        maxEquity = 0;
        maxSysDrawDownPercent = 0;
        ordersIndex = 0;
        doneTradesProfit = 0;
        turnover = 0;
        totalCommission = 0;
        while (synchronizer.synchronize() != Long.MAX_VALUE) {
            processOrders();
            processSeries();
        }
    }

    /**
     * @return пустой временной ряд с колонками {@link FinSeries#T}, {@link #EQUITY}, {@link #CAPITAL_USED}.
     */
    public static FinSeries newEquityAndCapitalUsed() {
        return new FinSeries().withLongColumn(FinSeries.T).withDoubleColumn(EQUITY).withDoubleColumn(CAPITAL_USED);
    }

    /**
     * @return временной ряд с колонками {@link #EQUITY}, {@link #CAPITAL_USED}.
     */
    public FinSeries getEquityAndCapitalUsed() {
        return equityAndCapitalUsed.copy();
    }

    /**
     * @return временной ряд с колонками {@link #EQUITY}, {@link #CAPITAL_USED} на закрытие дня.
     */
    public FinSeries getEquityAndCapitalUsedDaily() {
        final int len = equityAndCapitalUsed.length();
        final LongColumn t = equityAndCapitalUsed.timeCode();
        final DoubleColumn e = equityAndCapitalUsed.getDoubleColumn(EQUITY);
        final DoubleColumn c = equityAndCapitalUsed.getDoubleColumn(CAPITAL_USED);
        final FinSeries equityDaily = equityAndCapitalUsed.copy(0, 0);
        final LongColumn tDaily = equityDaily.timeCode();
        final DoubleColumn eDaily = equityDaily.getDoubleColumn(EQUITY);
        final DoubleColumn cDaily = equityDaily.getDoubleColumn(CAPITAL_USED);
        for (int index = 0; index < len - 1; index++) {
            if (TimeCodes.yyyymmdd(t.get(index)) != TimeCodes.yyyymmdd(t.get(index + 1))) {
                tDaily.append(t.get(index));
                eDaily.append(e.get(index));
                cDaily.append(c.get(index));
            }
        }
        tDaily.append(t.get(t.length() - 1));
        eDaily.append(e.get(t.length() - 1));
        cDaily.append(c.get(t.length() - 1));
        return equityDaily;
    }

    /**
     * @return временной ряд с колонками {@link #EQUITY}, {@link #CAPITAL_USED} по часам.
     */
    public FinSeries getEquityAndCapitalUsedHourly() {
        final int len = equityAndCapitalUsed.length();
        final LongColumn t = equityAndCapitalUsed.timeCode();
        final DoubleColumn e = equityAndCapitalUsed.getDoubleColumn(EQUITY);
        final DoubleColumn c = equityAndCapitalUsed.getDoubleColumn(CAPITAL_USED);
        final FinSeries equityHourly = equityAndCapitalUsed.copy(0, 0);
        final LongColumn tHourly = equityHourly.timeCode();
        final DoubleColumn eHourly = equityHourly.getDoubleColumn(EQUITY);
        final DoubleColumn cHourly = equityHourly.getDoubleColumn(CAPITAL_USED);
        for (int index = 0; index < len - 1; index++) {
            if (TimeCodes.hour(t.get(index)) != TimeCodes.hour(t.get(index + 1))
                || TimeCodes.yyyymmdd(t.get(index)) != TimeCodes.yyyymmdd(t.get(index + 1))) {
                tHourly.append(t.get(index));
                eHourly.append(e.get(index));
                cHourly.append(c.get(index));
            }
        }
        tHourly.append(t.get(t.length() - 1));
        eHourly.append(e.get(t.length() - 1));
        cHourly.append(c.get(t.length() - 1));
        return equityHourly;
    }

    /**
     * Выполнить пересчёт эквити и использованного капитала в проценты к заданному капиталу.
     *
     * @param equitySeries временной ряд с {@link #EQUITY}, {@link #CAPITAL_USED}, выраженными в единицах капитала
     * @param capital      размер капитала, в процентах к которому нужно сделать пересчёт
     */
    private static void makeEquityAndCapitalUsedPercent(final Series equitySeries, final double capital) {
        final int len = equitySeries.length();
        final DoubleColumn equityColumn = equitySeries.getDoubleColumn(EQUITY);
        final DoubleColumn capitalColumn = equitySeries.getDoubleColumn(CAPITAL_USED);
        for (int i = 0; i < len; i++) {
            equityColumn.set(i, equityColumn.get(i) / capital * 100.0);
            capitalColumn.set(i, capitalColumn.get(i) / capital * 100.0);
        }
    }

    /**
     * @param capital капитал
     * @return временной ряд с колонками {@link #EQUITY}, {@link #CAPITAL_USED} в процентах к указанному капиталу.
     */
    public FinSeries getEquityAndCapitalUsedPercent(final double capital) {
        final FinSeries equitySeries = equityAndCapitalUsed.copy();
        makeEquityAndCapitalUsedPercent(equitySeries, capital);
        return equitySeries;
    }

    /**
     * @param capital капитал
     * @return временной ряд с колонками {@link #EQUITY}, {@link #CAPITAL_USED} в процентах к указанному капиталу
     * на конец дня.
     */
    public FinSeries getEquityAndCapitalUsedDailyPercent(final double capital) {
        final FinSeries equitySeries = getEquityAndCapitalUsedDaily();
        makeEquityAndCapitalUsedPercent(equitySeries, capital);
        return equitySeries;
    }

    /**
     * @param capital капитал
     * @return временной ряд с колонками {@link #EQUITY}, {@link #CAPITAL_USED} в процентах к указанному капиталу
     * по часам.
     */
    public FinSeries getEquityAndCapitalUsedHourlyPercent(final double capital) {
        final FinSeries equitySeries = getEquityAndCapitalUsedHourly();
        makeEquityAndCapitalUsedPercent(equitySeries, capital);
        return equitySeries;
    }

    /**
     * Дописать информацию об эквити и использованном капитале из источника в приёмник.
     * Предполагается, что последняя метка времени в приёмнике предшествует первой метке времени в источнике.
     *
     * @param src источник: временной ряд с колонками {@link FinSeries#T}, {@link #EQUITY}, {@link #CAPITAL_USED}
     * @param dst приёмник: временной ряд с колонками {@link FinSeries#T}, {@link #EQUITY}, {@link #CAPITAL_USED}
     */
    public static void appendEquity(final FinSeries src, final FinSeries dst) {
        final LongColumn dstTimeCode = dst.timeCode();
        final DoubleColumn dstEquity = dst.getDoubleColumn(EQUITY);
        final DoubleColumn dstCapital = dst.getDoubleColumn(CAPITAL_USED);
        final double offsetEquity = (dstEquity.length() == 0) ? 0 : dstEquity.getLast();

        final LongColumn srcTimeCode = src.timeCode();
        final DoubleColumn srcEquity = src.getDoubleColumn(EQUITY);
        final DoubleColumn srcCapital = src.getDoubleColumn(CAPITAL_USED);

        for (int i = 0; i < srcTimeCode.length(); i++) {
            dstTimeCode.append(srcTimeCode.get(i));
            dstEquity.append(offsetEquity + srcEquity.get(i));
            dstCapital.append(srcCapital.get(i));
        }
    }

    /**
     * @return список всех ордеров (копия).
     */
    public FinSeries getOrders() {
        return orders.copy();
    }

    /**
     * @return список сделок.
     */
    public List<TestTrade> getTrades() {
        final List<TestTrade> trades = new ArrayList<>();
        trades.addAll(doneTrades);
        trades.addAll(activeTrades);
        return trades;
    }

    /**
     * @return список незавершенных сделок.
     */
    public List<TestTrade> getActiveTrades() {
        return activeTrades;
    }

    /**
     * @return список завершённых сделок.
     */
    public List<TestTrade> getDoneTrades() {
        return doneTrades;
    }

    private void processOrders() {
        final int len = orders.length();
        while (ordersIndex < len && orders.timeCode().get(ordersIndex) <= synchronizer.t()) {
            processOrder(ordersTimeCode.get(ordersIndex),
                         ordersSecurity.get(ordersIndex),
                         ordersVolume.get(ordersIndex),
                         ordersPrice.get(ordersIndex),
                         ordersCommission.get(ordersIndex),
                         ordersComment.get(ordersIndex));
            ordersIndex++;
        }
    }

    private void processOrder(final long timeCode,
                              final String security,
                              final long volume,
                              final double price,
                              final double commission,
                              final String comment) {
        int tradeId = -1;
        for (int i = 0; i < activeTrades.size(); i++) {
            final TestTrade t = activeTrades.get(i);
            if (t.security.equals(security)) {
                tradeId = i;
                break;
            }
        }
        turnover += Math.abs(price * volume);
        totalCommission += commission;
        if (tradeId == -1) {
            final FinSeries s = securitiesMap.get(security);
            activeTrades.add(new TestTrade(timeCode, security, volume, price, commission,
                                                 s.timeCode(), s.close()));
        } else {
            final TestTrade trade = activeTrades.get(tradeId);
            final double oldVolume = trade.getVolume();
            double newVolume = oldVolume + volume;
            if (Math.abs(newVolume) <= TestTrade.EPS) {
                newVolume = 0;
            }
            if (newVolume == 0) {
                trade.update(timeCode, -oldVolume, price, commission);
                activeTrades.remove(tradeId);
                doneTrades.add(trade);
                doneTradesProfit += trade.getEquity(price);
            } else if (oldVolume * newVolume > 0) {
                trade.update(timeCode, volume, price, commission);
            } else {
                final double absVolume = Math.abs(volume);
                final double k1 = Math.abs(oldVolume) / absVolume;
                final double k2 = Math.abs(newVolume) / absVolume;
                final double commission1 = k1 * commission;
                final double commission2 = k2 * commission;
                trade.update(timeCode, -oldVolume, price, commission1);
                activeTrades.remove(tradeId);
                doneTrades.add(trade);
                doneTradesProfit += trade.getEquity(price);
                activeTrades.add(new TestTrade(timeCode, security, newVolume, price, commission2,
                                                     trade.timeCodeColumn, trade.closeColumn));
            }
        }
    }

    private void processSeries() {
        double sumEquity = doneTradesProfit;
        double sumCapitalUsed = 0;
        final long timeCode = synchronizer.t();
        for (final TestTrade trade : activeTrades) {
            final int updatedIndex = synchronizer.getUpdatedIndex(trade.timeCodeColumn);
            if (updatedIndex >= 0) {
                trade.barsInTrade++;
                trade.last = trade.closeColumn.get(updatedIndex);
                trade.lastTimeCode = timeCode;
            }
            sumEquity += trade.getEquity(trade.last);
            sumCapitalUsed += Math.abs(trade.getUsedCapital());
        }
        equityTimeCode.append(timeCode);
        equity.append(sumEquity);
        capitalUsed.append(sumCapitalUsed);
        if (sumEquity > maxEquity) {
            maxEquity = sumEquity;
        } else {
            final double drawDownPercent;
            if (drawdownMode == DrawdownMode.FixedCapital) {
                drawDownPercent = (sumEquity - maxEquity) / initialCapital * 100.0;
            } else if (drawdownMode == DrawdownMode.Reinvestment) {
                drawDownPercent = (sumEquity - maxEquity) / (maxEquity + initialCapital) * 100.0;
            } else {
                drawDownPercent = (sumEquity - maxEquity) / initialCapital * 100.0;
            }
            if (drawDownPercent < maxSysDrawDownPercent) {
                maxSysDrawDownPercent = drawDownPercent;
            }
        }
    }

    /**
     * @return количество сделок.
     */
    public int getTradesCount() {
        return doneTrades.size() + activeTrades.size();
    }

    /**
     * @return доход.
     */
    public double getNetProfit() {
        double netProfit = doneTradesProfit;
        for (final TestTrade trade : activeTrades) {
            netProfit += trade.getProfit();
        }
        return netProfit;
    }

    /**
     * @return доход в процентах на начальный капитал.
     */
    public double getNetProfitPercent() {
        return getNetProfit() / initialCapital * 100.0;
    }

    /**
     * @return средний доход на сделку в процентах.
     */
    public double getAvgProfitPercent() {
        double sum = 0;
        for (final TestTrade trade : doneTrades) {
            sum += trade.getProfitPercent();
        }
        for (final TestTrade trade : activeTrades) {
            sum += trade.getProfitPercent();
        }
        return sum / getTradesCount();
    }

    /**
     * @return профит-фактор.
     */
    public double getProfitFactor() {
        double profit = 0;
        double loss = 0;
        for (final TestTrade trade : doneTrades) {
            final double value = trade.getProfit();
            if (value > 0) {
                profit += value;
            } else {
                loss += value;
            }
        }
        for (final TestTrade trade : activeTrades) {
            final double value = trade.getProfit();
            if (value > 0) {
                profit += value;
            } else {
                loss += value;
            }
        }
        return profit / -loss;
    }

    /**
     * @return максимальная просадка в процентах.
     */
    public double getMaxSysDrawDownPercent() {
        return maxSysDrawDownPercent;
    }

    /**
     * @return оборот.
     */
    public double getTurnover() {
        return turnover;
    }

    /**
     * @return суммарная комиссия.
     */
    public double getTotalCommission() {
        return totalCommission;
    }

    /**
     * @param condition условие.
     * @return количество сделок, удовлетворяющих условию.
     */
    public int getTradesCount(final Predicate<TestTrade> condition) {
        int count = 0;
        for (final TestTrade trade : doneTrades) {
            if (condition.test(trade)) {
                count++;
            }
        }
        for (final TestTrade trade : activeTrades) {
            if (condition.test(trade)) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return количество длинных сделок.
     */
    public int getLongTradesCount() {
        return getTradesCount(TestTrade::isLong);
    }

    /**
     * @return количество коротких.
     */
    public int getShortTradesCount() {
        return getTradesCount(TestTrade::isShort);
    }

    /**
     * @return количество прибыльных сделок.
     */
    public int getWinningTradesCount() {
        return getTradesCount(trade -> (trade.getProfit() > 0));
    }

    /**
     * @return количество убыточных сделок.
     */
    public int getLosingTradesCount() {
        return getTradesCount(trade -> (trade.getProfit() <= 0));
    }

    /**
     * @return массив строк, содержащих отчёт тестера.
     */
    public String[] getSummary() {
        final int tc = getTradesCount();
        final int winningTradesCount = getWinningTradesCount();
        final int losingTradesCount = getLosingTradesCount();
        final int longTradesCount = getLongTradesCount();
        final int shortTradesCount = getShortTradesCount();
        return new String[]{
                "*SUMMARY*",
                String.format(Locale.US, "Initial Capital: %s", initialCapital),
                String.format(Locale.US, "Net Profit, %%  : %.2f%%", getNetProfitPercent()),
                String.format(Locale.US, "MaxSysDD,   %%  : %.2f%%", getMaxSysDrawDownPercent()),
                String.format(Locale.US, "Profit Factor  : %.2f", getProfitFactor()),
                String.format(Locale.US, "Avg Profit, %%  : %.2f%%", getAvgProfitPercent()),
                String.format(Locale.US, "All     Trades : %d", tc),
                String.format(Locale.US, "Winning Trades : %d (%.2f%%)", winningTradesCount, 100.0 * winningTradesCount / tc),
                String.format(Locale.US, "Losing  Trades : %d (%.2f%%)", losingTradesCount, 100.0 * losingTradesCount / tc),
                String.format(Locale.US, "Long    Trades : %d (%.2f%%)", longTradesCount, 100.0 * longTradesCount / tc),
                String.format(Locale.US, "Short   Trades : %d (%.2f%%)", shortTradesCount, 100.0 * shortTradesCount / tc),
                String.format(Locale.US, "Turnover       : %.2f (%.2f x initial capital)", getTurnover(), getTurnover() / initialCapital),
                String.format(Locale.US, "Commission paid: %.2f (%.2f%% of initial capital)", getTotalCommission(), getTotalCommission() / initialCapital * 100.0)
        };
    }

    /**
     * Напечатать отчёт тестера.
     *
     * @param ps поток вывода
     */
    public void printSummary(final PrintStream ps) {
        for (final String s : getSummary()) {
            ps.println(s);
        }
    }

    /**
     * Напечатать отчёт тестера на консоль.
     */
    public void printSummary() {
        printSummary(System.out);
    }

    /**
     * @return строка с базовой статистикой.
     */
    public String getStatsValues() {
        return String.format(Locale.US, "%s; %.2f; %.2f; %.2f; %.2f; %d",
                             initialCapital,
                             getNetProfitPercent(),
                             getMaxSysDrawDownPercent(),
                             getProfitFactor(),
                             getAvgProfitPercent(),
                             getTradesCount());
    }

    public enum DrawdownMode {
        FixedCapital,
        Reinvestment,
    }
}
