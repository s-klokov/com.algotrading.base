package com.algotrading.base.core.tester;

import com.algotrading.base.core.commission.Commission;
import com.algotrading.base.core.commission.SimpleCommission;
import com.algotrading.base.core.marketdata.CandleDataProvider;
import com.algotrading.base.core.marketdata.Futures;
import com.algotrading.base.core.series.FinSeries;

import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;

/**
 * Шаблон для написания тестов на истории на одной акции или фьючерсе.
 */
public abstract class SingleSecurityTest {
    /**
     * Поток вывода результатов.
     */
    protected PrintStream ps = System.out;
    /**
     * Провайдер свечных данных.
     */
    protected CandleDataProvider candleDataProvider = null;
    /**
     * Код акции или префикс фьючерса.
     */
    protected String secPrefix = null;
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

    protected int from = 1900_01_01;
    protected int till = 2099_12_31;
    protected LongPredicate timeFilter = t -> true;

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
        WalkForward,  // тест Walk-Forward
    }

    public SingleSecurityTest withOutput(final PrintStream ps) {
        this.ps = ps;
        return this;
    }

    public SingleSecurityTest withCandleDataProvider(final CandleDataProvider candleDataProvider) {
        this.candleDataProvider = candleDataProvider;
        marketDataMap.clear();
        return this;
    }

    public SingleSecurityTest withTimeframe(final int timeframe, final TimeUnit timeUnit) {
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

    public SingleSecurityTest withCapital(final double capital) {
        this.capital = capital;
        return this;
    }

    public SingleSecurityTest withLimitCommission(final Commission limitCommission) {
        this.limitCommission = limitCommission;
        return this;
    }

    public SingleSecurityTest withMarketCommission(final Commission marketCommission) {
        this.marketCommission = marketCommission;
        return this;
    }

    public SingleSecurityTest withFuturesOverlapDays(final int futuresOverlapDays) {
        this.futuresOverlapDays = futuresOverlapDays;
        return this;
    }

    public SingleSecurityTest enableFuturesPrefix(final boolean enableFuturesPrefix) {
        this.enableFuturesPrefix = enableFuturesPrefix;
        return this;
    }

    public SingleSecurityTest from(final int yyyymmdd) {
        from = yyyymmdd;
        candleDataProvider.from(yyyymmdd);
        return this;
    }

    public SingleSecurityTest from(final LocalDate localDate) {
        from = yyyymmdd(localDate);
        candleDataProvider.from(localDate);
        return this;
    }

    public SingleSecurityTest till(final int yyyymmdd) {
        till = yyyymmdd;
        candleDataProvider.till(yyyymmdd);
        return this;
    }

    public SingleSecurityTest till(final LocalDate localDate) {
        till = yyyymmdd(localDate);
        candleDataProvider.till(localDate);
        return this;
    }

    public SingleSecurityTest timeFilter(final LongPredicate timeFilter) {
        this.timeFilter = timeFilter;
        return this;
    }

    /**
     * Загрузить рыночные данные.
     *
     * @param secPrefix код акции или префикс фьючерса
     * @return этот объект
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public SingleSecurityTest loadMarketData(final String secPrefix) throws IOException {
        this.secPrefix = secPrefix;
        marketDataMap.clear();
        if (!enableFuturesPrefix || Futures.byPrefix(this.secPrefix).length == 0) {
            loadStockData();
        } else {
            loadFuturesData();
        }
        return this;
    }

    /**
     * Использовать рыночные данные из объекта типа {@link SingleSecurityTest}.
     *
     * @param test объект, рыночные данные которого будут использованы
     * @return этот объект
     */
    public SingleSecurityTest useMarketDataOf(final SingleSecurityTest test) {
        secPrefix = test.secPrefix;
        marketDataMap.clear();
        marketDataMap.putAll(test.marketDataMap);
        return this;
    }

    /**
     * Задать рыночные данные.
     *
     * @param secPrefix код акции или префикс фьючерса
     * @param series    рыночные данные
     * @return этот объект
     */
    public SingleSecurityTest putMarketData(final String secPrefix, final FinSeries series) {
        this.secPrefix = secPrefix;
        marketDataMap.put(secPrefix, series);
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
        marketDataMap.forEach(((secCode, series) -> {
            tester.setSeries(secCode, series);
            tester.addOrders(getOrders(secCode, series, testOptions));
        }));
        done();
        tester.test();
        if (contains(TestOption.Summary, testOptions)) {
            ps.println();
            tester.printSummary(ps);
        }
        if (contains(TestOption.EquityDaily, testOptions)) {
            try {
                Tester.writeEquity(tester.getEquityAndCapitalUsedDailyPercent(capital),
                        getEquityFileName(secPrefix, timeframe, timeUnit));
            } catch (final IOException e) {
                e.printStackTrace(ps);
            }
        }
        if (contains(TestOption.EquityHourly, testOptions)) {
            try {
                Tester.writeEquity(tester.getEquityAndCapitalUsedHourlyPercent(capital),
                        getEquityFileName(secPrefix, timeframe, timeUnit));
            } catch (final IOException e) {
                e.printStackTrace(ps);
            }
        }
        if (contains(TestOption.EquityAndCapitalDaily, testOptions)) {
            try {
                Tester.writeEquityAndCapitalUsed(tester.getEquityAndCapitalUsedDailyPercent(capital),
                        getEquityFileName(secPrefix, timeframe, timeUnit));
            } catch (final IOException e) {
                e.printStackTrace(ps);
            }
        }
        if (contains(TestOption.EquityAndCapitalHourly, testOptions)) {
            try {
                Tester.writeEquityAndCapitalUsed(tester.getEquityAndCapitalUsedHourlyPercent(capital),
                        getEquityFileName(secPrefix, timeframe, timeUnit));
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
     * @param secPrefix код для акции или префикс для фьючерса
     * @param timeframe таймфрейм
     * @param timeUnit  единица измерения времени
     * @return имя csv-файла для вывода эквити
     */
    protected String getEquityFileName(final String secPrefix, final int timeframe, final TimeUnit timeUnit) {
        return getClass().getSimpleName() + "_" + secPrefix + "_" + timeframe + getTimeUnitLetter(timeUnit) + ".csv";
    }

    /**
     * Получить список ордеров для тестирования в тестере на истории.
     *
     * @param secCode     код инструмента
     * @param series      свечной временной ряд
     * @param testOptions опции теста
     * @return список ордеров
     */
    public abstract FinSeries getOrders(final String secCode, final FinSeries series, final TestOption... testOptions);

    private void loadStockData() throws IOException {
        ps.print("Loading " + secPrefix + " " + from + "-" + till + "...");
        FinSeries series = candleDataProvider.from(from).till(till).timeFilter(timeFilter).getSeries(secPrefix);
        series = series.compressedCandles(timeframe, timeUnit);
        ps.println(" " + series.length() + " candles (" + timeframe + getTimeUnitLetter(timeUnit) + ").");
        marketDataMap.put(secPrefix, series);
    }

    private void loadFuturesData() throws IOException {
        for (final Futures f : Futures.byPrefix(secPrefix)) {
            int futFrom = Math.max(from, f.previousExpiry);
            if (futuresOverlapDays > 0) {
                final int dd = futFrom % 100;
                final int mm = (futFrom / 100) % 100;
                final int yyyy = futFrom / 10000;
                final LocalDate date = LocalDate.of(yyyy, mm, dd).minusDays(futuresOverlapDays);
                futFrom = Math.max(from, yyyymmdd(date));
            }
            final int futTill = Math.min(till, f.oneDayBeforeExpiry);
            if (futFrom < futTill) {
                ps.print("Loading " + f.shortCode + " " + futFrom + "-" + futTill + "...");
                candleDataProvider.from(futFrom).till(futTill).timeFilter(timeFilter);
                FinSeries series = candleDataProvider.getSeries(f.shortCode);
                series = series.compressedCandles(timeframe, timeUnit);
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

    private static int yyyymmdd(final LocalDate localDate) {
        return localDate.getYear() * 10000 + localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
    }
}
