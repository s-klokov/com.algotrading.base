package com.algotrading.base.examples;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.commission.SimpleCommission;
import com.algotrading.base.core.indicators.Mfi;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.tester.*;

import java.io.IOException;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.algotrading.base.core.TimeFilters.FILTER_1000_1845;

/**
 * Пример тестирования торговли по индикатору MFI.
 * <p>
 * Правила торговли:<br>
 * - при значении MFI(period) < bound1 < 50 продаём,
 * - при значении MFI(period) > bound2 > 50 покупаем,
 * - позиция закрывается при переходе MFI(period) через уровень 50.
 * <p>
 * В данном примере реализован перебор параметров period, bound1, bound2,
 * генерация эквити walkforward-теста с периодическим пересмотром
 * используемых параметров.
 * <p>
 * Также можно смоделировать торговлю портфелем из нескольких систем,
 * каждая из которых имеет свои параметры.
 */
class BasicMfiTest extends SingleSecurityTest {
    /**
     * Набор стратегий.
     */
    private final List<Strategy> strategies = new ArrayList<>();
    /**
     * Вспомогательная переменная для склейки фьючерсных контрактов.
     */
    private long tLast = 0L;

    public static void main(final String[] args) {
        final String secPrefix =
//                "SBER";
                "RI";
        try {
            new BasicMfiTest()
                    .withCapital(100_000_000)
                    .withProviderSettings(1, TimeUnit.MINUTES,
                            "D:/MarketData/Finam",
                            "D:/MarketData/Quik/Export",
                            "D:/MarketData/Quik/Archive")
                    .withTimeframe(15, TimeUnit.MINUTES)
                    .withFuturesOverlapDays(10)
                    .withMarketCommission(SimpleCommission.ofPercent(0.01))
                    .loadMarketData(secPrefix,
                            2015_01_01, 2021_03_31,
                            FILTER_1000_1845)
                    .optimize();
//                    .runTest(TestOption.Summary, TestOption.EquityAndCapitalDaily);
        } catch (final IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void runTest(final TestOption... testOptions) {
        strategies.clear();
        strategies.add(new Strategy(14, 40, 60));
        tLast = 0L;
        super.runTest(testOptions);
    }

    @Override
    public void optimize() {
        ps.println("counter; period; bound1; bound2; netProfit,%;maxSysDD,%;trades;netProfit/(-maxSysDD);Commission,%");
        int counterAll = 0;
        int counterPositiveProfit = 0;
        int counterRatio = 0;

        final Walkforward walkforward = new Walkforward();

        // Перебор параметров
        for (int period = 10; period <= 20; period++) {
            for (double bound1 = 20; bound1 <= 48; bound1 += 2) {
                for (double bound2 = 52; bound2 <= 80; bound2 += 2) {
                    ps.printf("%d; %d; %.2f; %.2f; ",
                            ++counterAll,
                            period,
                            bound1,
                            bound2
                    );
                    // Указание стратегии для тестирования
                    strategies.clear();
                    strategies.add(new Strategy(period, bound1, bound2));

                    // Запуск тестирования
                    tLast = 0L;
                    super.runTest(TestOption.Optimisation);

                    // Учёт статистики
                    if (tester.getNetProfitPercent() > 0) {
                        counterPositiveProfit++;
                    }
                    if (tester.getNetProfitPercent() / -tester.getMaxSysDrawDownPercent() > 3.0) {
                        counterRatio++;
                    }
                    ps.printf("%.2f%%; %.2f%%; %d; %.2f; %.2f%%%n",
                            tester.getNetProfitPercent(),
                            tester.getMaxSysDrawDownPercent(),
                            tester.getTradesCount(),
                            tester.getNetProfitPercent() / -tester.getMaxSysDrawDownPercent(),
                            tester.getTotalCommission() / tester.getInitialCapital() * 100
                    );

                    // При достаточном количестве сделок помещаем стратегию в пул
                    // для дальнейшего проведения walkforward-теста
                    if (tester.getTradesCount() >= 200) {
                        final FinSeries equity = tester.getEquityAndCapitalUsedDailyPercent(capital);
                        final String equityId = period + "/" + bound1 + "/" + bound2;
                        walkforward.addEquity(equityId, equity.timeCode(), equity.getDoubleColumn(Tester.EQUITY));
                    }
                }
            }
        }
        ps.printf("%nPositiveProfit: %.1f%%; NetProfit/-MaxSysDD: %.1f%%%n",
                100.0 * counterPositiveProfit / counterAll,
                100.0 * counterRatio / counterAll
        );

        // Запуск walkforward-теста, комбинирующего эквити лучших стратегий за указанный период
        ps.printf("%nWalkforward test started%n");
        final List<WalkforwardIndices> walkforwardIndices = WalkforwardIndices.getRegularWalkforwardIndices(
                walkforward.timeCode(),
                2016_01_01,
                2021_03_31,
                23_59_59,
                Period.ofMonths(3),
                Period.ofYears(1)
        );

        walkforward.walkForward(walkforwardIndices,
                new NetProfitEquitySelector(5) // выбор нескольких лучших стратегий
        );
        walkforward.getDescriptions().forEach(ps::println);

        // Вывод файла с эквити
        try {
            Tester.writeEquity(walkforward.getWalkforwardEquity(),
                    BasicMfiTest.class.getSimpleName() + "_" + secPrefix + "_walkforward.csv");
        } catch (final IOException e) {
            e.printStackTrace(ps);
        }
    }

    @Override
    public FinSeries getOrders(final String secCode, final FinSeries series, final TestOption... testOptions) {
        final LongColumn timeCode = series.timeCode();
        final DoubleColumn close = series.close();

        final FinSeries orders = Tester.newOrders();

        strategies.forEach(strategy -> strategy.init(series));

        long position = 0;
        final int len = timeCode.length();
        for (int i = 0; i < len; i++) {
            final long oldPosition = position;
            final long t = timeCode.get(i);

            double sum = 0;
            for (final Strategy strategy : strategies) {
                strategy.update(series, i);
                sum += strategy.position;
            }
            sum /= strategies.size();
            if (tLast < t) {
                tLast = t;
                position = Math.round(sum);
            }
            if (i == len - 1) {
                position = 0;
            }
            if (position != oldPosition) {
                Tester.appendOrder(orders, timeCode.get(i), secCode, position - oldPosition, close.get(i),
                        marketCommission, null);
            }
        }

        strategies.forEach(Strategy::done);

        return orders;
    }

    /**
     * Реализация стратегии торговли на базе индикатора MFI.
     */
    private class Strategy {
        /**
         * Период индикатора.
         */
        final int period;
        /**
         * Граница для входа в шорт.
         */
        final double bound1;
        /**
         * Граница для входа в лонг.
         */
        final double bound2;

        long position = 0;
        DoubleColumn close = null;
        DoubleColumn mfiColumn = null;

        Strategy(final int period, final double bound1, final double bound2) {
            if (period > 0 && bound1 < 50 && bound2 > 50) {
                this.period = period;
                this.bound1 = bound1;
                this.bound2 = bound2;
            } else {
                throw new IllegalArgumentException(
                        "period = " + period + ", bound1 = " + bound1 + ", bound2 = " + bound2
                );
            }
        }

        void init(final FinSeries series) {
            position = 0;
            close = series.close();
            final String mfiColumnName = "MFI" + period;
            mfiColumn = series.getDoubleColumn(mfiColumnName);
            if (mfiColumn == null) {
                mfiColumn = Mfi.mfi(series, period, mfiColumnName);
            }
        }

        void update(final FinSeries series, final int i) {
            final double mfi = mfiColumn.get(i);
            if (position > 0 && mfi <= 50 || position < 0 && mfi >= 50) {
                position = 0;
            }
            if (position == 0) {
                if (mfi <= bound1) {
                    position = -Math.round(capital / close.get(i));
                    ;
                } else if (mfi >= bound2) {
                    position = Math.round(capital / close.get(i));
                }
            }
        }

        void done() {
            position = 0;
            close = null;
            mfiColumn = null;
        }
    }
}
