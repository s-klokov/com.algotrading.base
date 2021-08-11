package com.algotrading.base.core.tester;

import com.algotrading.base.core.DayTradingSession;
import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.commission.Commission;
import com.algotrading.base.core.commission.SimpleCommission;
import com.algotrading.base.core.marketdata.CandleDataProvider;
import com.algotrading.base.core.marketdata.Futures;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.sync.Synchronizer;

import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;

/**
 * Шаблон для написания тестов на истории для одновременной торговли несколькими инструментами.
 */
public abstract class MultiSecurityTest {
    /**
     * Поток вывода результатов.
     */
    protected PrintStream ps = System.out;
    /**
     * Провайдер свечных данных.
     */
    protected CandleDataProvider provider = null;
    /**
     * Таймфрейм, используемый при загрузке данных.
     */
    protected int providerTimeframe = 1;
    /**
     * Единица измерения времени для указания таймфрейма провайдера данных.
     */
    protected TimeUnit providerTimeUnit = TimeUnit.MINUTES;
    /**
     * Нужно ли детектировать фьючерсы по их префиксу.
     */
    protected boolean enableFuturesPrefix = true;
    /**
     * Таймфрейм, используемый в тестах.
     */
    protected int timeframe = 1;
    /**
     * Единица измерения времени для указания таймфрейма.
     */
    protected TimeUnit timeUnit = TimeUnit.MINUTES;
    /**
     * Количество календарных дней для перекрытия данных по фьючерсам перед экспирацией.
     */
    protected int futuresOverlapDays = 0;
    /**
     * Метка для имени файла эквити.
     */
    protected String label = null;
    /**
     * Список префиксов временных рядов.
     */
    protected final List<String> prefixList = new ArrayList<>();
    /**
     * Набор свечных временных рядов.
     */
    protected final Map<String, FinSeries> marketDataMap = new LinkedHashMap<>();
    /**
     * Капитал для расчёта размера позиции.
     */
    protected double capital = 100_000_000;
    /**
     * Комиссия для лимитных сделок.
     */
    protected Commission limitCommission = SimpleCommission.ofPercent(0.01);
    /**
     * Комиссия для рыночных сделок.
     */
    protected Commission marketCommission = SimpleCommission.ofPercent(0.02);
    /**
     * Тестер.
     */
    protected Tester tester = null;

    /**
     * Опции при запуске теста.
     */
    public enum TestOption {
        Summary,      // печать результатов теста
        EquityDaily,  // вывод в csv-файл эквити теста по дням
        EquityHourly, // вывод в csv-файл эквити теста по часам
        EquityAndCapitalDaily,  // вывод в csv-файл эквити теста и используемого капитала по дням
        EquityAndCapitalHourly, // вывод в csv-файл эквити теста и используемого капитала по часам
        Trades,       // печать сделок
        Optimisation, // оптимизация
    }

    public MultiSecurityTest withOutput(final PrintStream ps) {
        this.ps = ps;
        return this;
    }

    public MultiSecurityTest withProviderSettings(final int providerTimeframe, final TimeUnit providerTimeUnit, final String... paths) {
        provider = new CandleDataProvider(paths);
        this.providerTimeframe = providerTimeframe;
        this.providerTimeUnit = providerTimeUnit;
        marketDataMap.clear();
        return this;
    }

    public MultiSecurityTest withTimeframe(final int timeframe, final TimeUnit timeUnit) {
        if (timeUnit == TimeUnit.SECONDS
            || timeUnit == TimeUnit.MINUTES
            || timeUnit == TimeUnit.HOURS
            || timeUnit == TimeUnit.DAYS) {
            this.timeframe = timeframe;
            this.timeUnit = timeUnit;
            marketDataMap.clear();
            return this;
        } else {
            throw new IllegalArgumentException("Illegal timeUnit " + timeUnit);
        }
    }

    public MultiSecurityTest withCapital(final double capital) {
        this.capital = capital;
        return this;
    }

    public MultiSecurityTest withLimitCommission(final Commission limitCommission) {
        this.limitCommission = limitCommission;
        return this;
    }

    public MultiSecurityTest withMarketCommission(final Commission marketCommission) {
        this.marketCommission = marketCommission;
        return this;
    }

    public MultiSecurityTest enableFuturesPrefix(final boolean enableFuturesPrefix) {
        this.enableFuturesPrefix = enableFuturesPrefix;
        return this;
    }

    public MultiSecurityTest withFuturesOverlapDays(final int futuresOverlapDays) {
        this.futuresOverlapDays = futuresOverlapDays;
        return this;
    }

    public MultiSecurityTest withLabel(final String label) {
        this.label = label;
        return this;
    }

    /**
     * Выровнять данные по времени, обеспечив одинаковое количество свечей в каждом временном ряде.
     * При этом добавляемые свечи без сделок будут иметь нулевой объём торгов и совпадающие значения
     * open, high, low и close.
     *
     * @return этот объект
     */
    public MultiSecurityTest align() {
        final Map<String, FinSeries> alignedMap = new HashMap<>();
        marketDataMap.forEach((secCode, series) -> alignedMap.put(secCode, FinSeries.newCandles()));
        final Synchronizer synchronizer = newSynchronizer();
        while (synchronizer.synchronize() != Long.MAX_VALUE) {
            marketDataMap.forEach((secCode, series) -> {
                final FinSeries alignedSeries = alignedMap.get(secCode);
                final int i = synchronizer.getCurrIndex(series.timeCode());
                if (i >= 0) {
                    alignedSeries.timeCode().append(synchronizer.timeCode());
                    alignedSeries.open().append(series.open().get(i));
                    alignedSeries.high().append(series.high().get(i));
                    alignedSeries.low().append(series.low().get(i));
                    alignedSeries.close().append(series.close().get(i));
                    alignedSeries.volume().append(series.volume().get(i));
                } else {
                    final double price = (alignedSeries.length() == 0) ?
                                         series.open().get(0) : alignedSeries.close().getLast();
                    alignedSeries.timeCode().append(synchronizer.timeCode());
                    alignedSeries.open().append(price);
                    alignedSeries.high().append(price);
                    alignedSeries.low().append(price);
                    alignedSeries.close().append(price);
                    alignedSeries.volume().append(0);
                }
            });
        }
        marketDataMap.putAll(alignedMap);
        return this;
    }

    /**
     * Загрузить данные.
     *
     * @param secPrefixes набор кодов акций или префиксов фьючерсов
     * @param from        начало периода в формате YYYYMMDD
     * @param till        конец периода (включительно) в формате YYYYMMDD
     * @param timeFilter  фильтр времени
     * @return этот объект
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public MultiSecurityTest loadMarketData(final Iterable<String> secPrefixes, final int from, final int till, final LongPredicate timeFilter) throws IOException {
        for (final String secPrefix : secPrefixes) {
            loadMarketData(secPrefix, from, till, timeFilter);
        }
        return this;
    }

    /**
     * Загрузить данные.
     *
     * @param secPrefixes массив кодов акций или префиксов фьючерсов
     * @param from        начало периода в формате YYYYMMDD
     * @param till        конец периода (включительно) в формате YYYYMMDD
     * @param timeFilter  фильтр времени
     * @return этот объект
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public MultiSecurityTest loadMarketData(final String[] secPrefixes, final int from, final int till, final LongPredicate timeFilter) throws IOException {
        for (final String secPrefix : secPrefixes) {
            loadMarketData(secPrefix, from, till, timeFilter);
        }
        return this;
    }

    /**
     * Загрузить данные.
     *
     * @param secPrefix  код акции или префикс фьючерса
     * @param from       начало периода в формате YYYYMMDD
     * @param till       конец периода (включительно) в формате YYYYMMDD
     * @param timeFilter фильтр времени
     * @return этот объект
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public MultiSecurityTest loadMarketData(final String secPrefix, final int from, final int till, final LongPredicate timeFilter) throws IOException {
        if (prefixList.contains(secPrefix)) {
            throw new IllegalArgumentException("Market data already loaded for " + secPrefix);
        }
        prefixList.add(secPrefix);
        if (!enableFuturesPrefix || Futures.withPrefix(secPrefix).length == 0) {
            loadStockData(secPrefix, from, till, timeFilter);
        } else {
            loadFuturesData(secPrefix, from, till, timeFilter);
        }
        return this;
    }

    /**
     * Запустить тест.
     *
     * @param testOptions опции теста
     */
    public void runTest(final TestOption... testOptions) {
        tester = new Tester();
        tester.setInitialCapital(capital);
        init();
        marketDataMap.forEach(((secCode, series) -> tester.setSeries(secCode, series)));
        tester.addOrders(getOrders(testOptions));
        done();
        tester.test();
        if (contains(TestOption.Summary, testOptions)) {
            ps.println();
            tester.printSummary(ps);
        }
        if (contains(TestOption.EquityDaily, testOptions)) {
            try {
                Tester.writeEquity(tester.getEquityAndCapitalUsedDailyPercent(capital),
                                   getEquityFileName(label, timeframe, timeUnit));
            } catch (final IOException e) {
                e.printStackTrace(ps);
            }
        }
        if (contains(TestOption.EquityHourly, testOptions)) {
            try {
                Tester.writeEquity(tester.getEquityAndCapitalUsedHourlyPercent(capital),
                                   getEquityFileName(label, timeframe, timeUnit));
            } catch (final IOException e) {
                e.printStackTrace(ps);
            }
        }
        if (contains(TestOption.EquityAndCapitalDaily, testOptions)) {
            try {
                Tester.writeEquityAndCapitalUsed(tester.getEquityAndCapitalUsedDailyPercent(capital),
                                                 getEquityFileName(label, timeframe, timeUnit));
            } catch (final IOException e) {
                e.printStackTrace(ps);
            }
        }
        if (contains(TestOption.EquityAndCapitalHourly, testOptions)) {
            try {
                Tester.writeEquityAndCapitalUsed(tester.getEquityAndCapitalUsedHourlyPercent(capital),
                                                 getEquityFileName(label, timeframe, timeUnit));
            } catch (final IOException e) {
                e.printStackTrace(ps);
            }
        }
        if (contains(TestOption.Trades, testOptions)) {
            ps.println();
            ps.println(TestTrade.header());
            for (final TestTrade testTrade : tester.getTrades()) {
                ps.println(testTrade.toCsvString());
            }
        }
    }

    /**
     * Инициализация перед запуском теста на наборе исторических данных.
     */
    public void init() {
    }

    /**
     * Вызывается, когда тест на наборе исторических данных завершён.
     */
    public void done() {
    }

    /**
     * Запустить оптимизацию. В потомках метод перегружается, чтобы реализовать перебор множества наборов параметров.
     */
    public void optimize() {
        runTest(TestOption.Optimisation);
    }

    /**
     * Буквенное сокращения для обозначения единицы измерения таймфрейма.
     *
     * @param timeUnit единица измерения таймфрейма
     * @return буквенное сокращение
     */
    protected String getTimeUnitLetter(final TimeUnit timeUnit) {
        switch (timeUnit) {
            case SECONDS:
                return "s";
            case MINUTES:
                return "m";
            case HOURS:
                return "H";
            case DAYS:
                return "D";
            default:
                return "";
        }
    }

    /**
     * @param label     метка имени файла с эквити
     * @param timeframe таймфрейм
     * @param timeUnit  единица измерения времени
     * @return имя csv-файла для вывода эквити
     */
    protected String getEquityFileName(final String label, final int timeframe, final TimeUnit timeUnit) {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        if (label != null && label.length() > 0) {
            sb.append('_').append(label);
        }
        sb.append('_').append(timeframe).append(getTimeUnitLetter(timeUnit)).append(".csv");
        return sb.toString();
    }

    /**
     * Получить список ордеров для тестирования в тестере на истории.
     *
     * @param testOptions опции теста
     * @return список ордеров
     */
    public abstract FinSeries getOrders(final TestOption... testOptions);

    /**
     * @return синхронизатор всех временных рядов.
     */
    protected Synchronizer newSynchronizer() {
        final Synchronizer synchronizer = new Synchronizer();
        marketDataMap.forEach((secCode, series) -> synchronizer.put(series.timeCode()));
        return synchronizer;
    }

    /**
     * Получить актуальные для текущей торговли инструменты. Для фьючерсов даётся соответствие:
     * префикс -> код фьючерса, актуального для торговли в настоящий момент времени.
     * Для остальных инструментов префикс и код инструмента совпадают.
     *
     * @param t          метка времени
     * @param hhmmSwitch время для перехода на новый фьючерс в день, предшествующий дню экспирации старого фьючерса
     * @return соответствие: префикс -> код инструмента
     */
    protected Map<String, String> getSecCodesMap(final long t, final int hhmmSwitch) {
        final Map<String, String> map = new HashMap<>();
        final int yyyymmdd = TimeCodes.yyyymmdd(t);
        final int hhmm = TimeCodes.hhmm(t);
        for (final Map.Entry<String, FinSeries> entry : marketDataMap.entrySet()) {
            final String secCode = entry.getKey();
            if (!Futures.isFutures(secCode)) { // не фьючерс
                map.put(secCode, secCode);
            } else { // фьючерс
                final Futures f = Futures.byShortCode(secCode);
                if (!map.containsKey(f.prefix)) {
                    if (hhmm < hhmmSwitch && yyyymmdd <= f.oneDayBeforeExpiry
                        || hhmm >= hhmmSwitch && yyyymmdd < f.oneDayBeforeExpiry) {
                        map.put(f.prefix, secCode);
                    }
                }
            }
        }
        return map;
    }

    private void loadStockData(final String secPrefix, final int from, final int till, final LongPredicate timeFilter) throws IOException {
        ps.print("Loading " + secPrefix + " " + from + "-" + till + "...");
        FinSeries series = provider.getSeries(secPrefix, providerTimeframe, providerTimeUnit,
                                              TimeCodes.timeCode(from, 0),
                                              TimeCodes.timeCode(till, 235959),
                                              timeFilter);
        if (timeframe != providerTimeframe || timeUnit != providerTimeUnit) {
            ps.print(" " + series.length() + " ->");
            series = series.compressedCandles(timeframe, timeUnit);
        }
        ps.println(" " + series.length() + " candles (" + timeframe + getTimeUnitLetter(timeUnit) + ").");
        marketDataMap.put(secPrefix, series);
    }

    private void loadFuturesData(final String secPrefix, final int from, final int till, final LongPredicate timeFilter) throws IOException {
        for (final Futures f : Futures.withPrefix(secPrefix)) {
            int futFrom = Math.max(from, f.previousExpiry);
            if (futuresOverlapDays > 0) {
                final int dd = futFrom % 100;
                final int mm = (futFrom / 100) % 100;
                final int yyyy = futFrom / 10000;
                final LocalDate date = LocalDate.of(yyyy, mm, dd).minusDays(futuresOverlapDays);
                futFrom = Math.max(from, date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth());
            }
            final int futTill = Math.min(till, f.oneDayBeforeExpiry);
            if (futFrom < futTill) {
                ps.print("Loading " + f.shortCode + " " + futFrom + "-" + futTill + "...");
                FinSeries series = provider.getSeries(f.shortCode, providerTimeframe, providerTimeUnit,
                                                      TimeCodes.timeCode(futFrom, 0),
                                                      TimeCodes.timeCode(futTill, 235959),
                                                      timeFilter);
                if (timeframe != providerTimeframe || timeUnit != providerTimeUnit) {
                    ps.print(" " + series.length() + " ->");
                    series = series.compressedCandles(timeframe, timeUnit);
                }
                ps.println(" " + series.length() + " candles (" + timeframe + getTimeUnitLetter(timeUnit) + ").");
                marketDataMap.put(f.shortCode, series);
            }
        }
    }

    protected static boolean contains(final TestOption option, final TestOption... options) {
        for (final TestOption o : options) {
            if (o == option) {
                return true;
            }
        }
        return false;
    }

    /**
     * Вычислить, какие сделки необходимо провести для того, чтобы перейти от старых позиций к новым.
     *
     * @param oldPositions старые позиции: соответствие код бумаги -> размер позиции
     * @param newPositions новые позиции: соответствие код бумаги -> размер позиции
     * @return соответствие код бумаги -> объём сделки для приведения старых позиций к новым
     */
    public static Map<String, Long> getPositionsDifference(final Map<String, Long> oldPositions,
                                                           final Map<String, Long> newPositions) {
        final Map<String, Long> positionsDifference = new HashMap<>();
        newPositions.forEach((secCode, newPosition) -> {
            final Long oldPosition = oldPositions.get(secCode);
            final long volume = newPosition - ((oldPosition == null) ? 0 : oldPosition);
            if (volume != 0) {
                positionsDifference.put(secCode, volume);
            }
        });
        oldPositions.forEach((secCode, oldPosition) -> {
            if (!newPositions.containsKey(secCode) && oldPosition != 0) {
                positionsDifference.put(secCode, -oldPosition);
            }
        });
        return positionsDifference;
    }

    /**
     * Пополнить список заявок на сделки, которые необходимо провести для того, чтобы перейти от старых позиций к новым.
     * Цены исполнения -- цены закрытия последних баров по инструментам.
     *
     * @param orders       список заявок на сделки
     * @param synchronizer синхронизатор временных рядов
     * @param oldPositions старые позиции
     * @param newPositions новые позиции
     * @param commission   комиссия
     * @param comment      комментарий
     */
    public void appendOrdersAtClosePrices(final FinSeries orders,
                                          final Synchronizer synchronizer,
                                          final Map<String, Long> oldPositions,
                                          final Map<String, Long> newPositions,
                                          final Commission commission,
                                          final String comment) {
        getPositionsDifference(oldPositions, newPositions).forEach((secCode, volume) -> {
            final FinSeries series = marketDataMap.get(secCode);
            final int id = synchronizer.getLastIndex(series.timeCode());
            final double price = series.close().get(id);
            Tester.appendOrder(orders, synchronizer.timeCode(), secCode, volume, price, commission, comment);
        });
    }

    public void appendOrdersAtDayClosePrices(final FinSeries orders,
                                             final Synchronizer synchronizer,
                                             final Map<String, Long> oldPositions,
                                             final Map<String, Long> newPositions,
                                             final Commission commission,
                                             final String comment) {
        getPositionsDifference(oldPositions, newPositions).forEach((secCode, volume) -> {
            final FinSeries series = marketDataMap.get(secCode);
            final int id = synchronizer.getLastIndex(series.timeCode());
            final double price = series.close().get(getDayCloseId(series.timeCode(), id));
            Tester.appendOrder(orders, synchronizer.timeCode(), secCode, volume, price, commission, comment);
        });
    }

    public static int getDayCloseId(final LongColumn timeCode, int id) {
        while (!DayTradingSession.isLastSessionCandle(timeCode, id)) {
            id++;
        }
        return id;
    }
}
