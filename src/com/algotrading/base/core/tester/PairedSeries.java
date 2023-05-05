package com.algotrading.base.core.tester;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.marketdata.futures.Futures;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.sync.Synchronizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Пара синхронизированных временных рядов с ценами закрытия свечей.
 */
public class PairedSeries {
    /**
     * Название колонки временного ряда с ценами закрытия инструмента A.
     */
    public static final String CLOSE_A = "closeA";
    /**
     * Название колонки временного ряда с ценами закрытия инструмента B.
     */
    public static final String CLOSE_B = "closeB";
    /**
     * Код инструмента A.
     */
    public final String secCodeA;
    /**
     * Код инструмента B.
     */
    public final String secCodeB;
    /**
     * Временной ряд с ценами закрытия по паре инструментов, в котором имеются колонки с названиями
     * {@link FinSeries#T}, {@link #CLOSE_A}, {@link #CLOSE_B}.
     */
    public final FinSeries series;

    /**
     * Конструктор.
     *
     * @param secCodeA код инструмента A
     * @param secCodeB код инструмента B
     * @param series   Временной ряд с ценами закрытия по паре инструментов
     */
    public PairedSeries(final String secCodeA, final String secCodeB, final FinSeries series) {
        this.secCodeA = secCodeA;
        this.secCodeB = secCodeB;
        this.series = series;
    }

    /**
     * @return колонка с метками времени
     */
    public LongColumn timeCode() {
        return series.timeCode();
    }

    /**
     * @return колонка с ценами закрытия по инструменту A
     */
    public DoubleColumn closeA() {
        return series.getDoubleColumn(CLOSE_A);
    }

    /**
     * @return колонка с ценами закрытия по инструменту B
     */
    public DoubleColumn closeB() {
        return series.getDoubleColumn(CLOSE_B);
    }

    /**
     * Вернуть список, состоящий из временных рядов пар инструментов.
     * Каждый временной ряд имеет колонки: {@link FinSeries#T}, {@link #CLOSE_A}, {@link #CLOSE_B}.
     *
     * @param marketDataMap соответствие: код инструмента -> временной ряд свечных данных
     * @param secA          код инструмента A или префикс фьючерса A
     * @param secB          код инструмента B или префикс фьючерса B
     * @return список временных рядов
     */
    public static List<PairedSeries> getPairedSeriesList(final Map<String, FinSeries> marketDataMap,
                                                         final String secA, final String secB) {
        final boolean isFutA = Futures.isFutures(secA);
        final boolean isFutB = Futures.isFutures(secB);

        final List<PairedSeries> pairedSeriesList = new ArrayList<>();

        final Synchronizer synchronizer = new Synchronizer();
        marketDataMap.forEach((secCode, series) -> synchronizer.put(series.timeCode()));

        String prevSecCodeA = null;
        String prevSecCodeB = null;
        FinSeries series = null;
        while (synchronizer.synchronize() != Long.MAX_VALUE) {
            String currSecCodeA = null;
            String currSecCodeB = null;
            double closeA = Double.NaN;
            double closeB = Double.NaN;
            for (final Map.Entry<String, FinSeries> entry : marketDataMap.entrySet()) {
                final String secCode = entry.getKey();
                final int id = synchronizer.getCurrIndex(entry.getValue().timeCode());
                if (id >= 0) {
                    if (secCode.equals(secA) || isFutA && secCode.startsWith(secA)) {
                        currSecCodeA = secCode;
                        closeA = entry.getValue().close().get(id);
                    } else if (secCode.equals(secB) || isFutB && secCode.startsWith(secB)) {
                        currSecCodeB = secCode;
                        closeB = entry.getValue().close().get(id);
                    }
                }
            }
            if (currSecCodeA == null || currSecCodeB == null) {
                continue;
            }
            if (!currSecCodeA.equals(prevSecCodeA) || !currSecCodeB.equals((prevSecCodeB))) {
                series = new FinSeries()
                        .withLongColumn(FinSeries.T)
                        .withDoubleColumn(CLOSE_A)
                        .withDoubleColumn(CLOSE_B);
                pairedSeriesList.add(new PairedSeries(currSecCodeA, currSecCodeB, series));
            }

            series.timeCode().append(synchronizer.timeCode());
            series.getDoubleColumn(CLOSE_A).append(closeA);
            series.getDoubleColumn(CLOSE_B).append(closeB);

            prevSecCodeA = currSecCodeA;
            prevSecCodeB = currSecCodeB;
        }

        return pairedSeriesList;
    }

    @Override
    public String toString() {
        return "PairedSeries{" + secCodeA + "/" + secCodeB + ", series=" + series + '}';
    }
}
