package com.algotrading.base.core.marketdata;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.csv.CsvReader;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.values.DoubleValue;
import com.algotrading.base.core.values.IntValue;
import com.algotrading.base.core.values.LongValue;
import com.algotrading.base.core.values.StringValue;

import java.io.IOException;

/**
 * Чтение рыночных данных в формате "Финам" из файла.
 */
public class FinamSeriesReader extends SeriesReader<FinamSeriesReader> {
    /**
     * Название колонки с информацией о направлении сделки для тиковых данных.
     */
    public static final String BUYSELL_COLUMN_NAME = "BUYSELL";

    /**
     * Признак наличия колонки с информацией о направлении сделки для тиковых данных.
     */
    protected boolean hasBuySell = false;

    public FinamSeriesReader hasBuySell(final boolean hasBuySell) {
        this.hasBuySell = hasBuySell;
        return thisAsT;
    }

    /**
     * Чтение OHLCV-данных в формате "Финам". Заголовок имеет вид:<br>
     * &lt;TICKER&gt;;&lt;PER&gt;;&lt;DATE&gt;;&lt;TIME&gt;;&lt;OPEN&gt;;&lt;HIGH&gt;;&lt;LOW&gt;;&lt;CLOSE&gt;;&lt;VOL&gt;
     *
     * @return рыночные данные формата OHLCV
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    public FinSeries read() throws IOException {
        final FinSeries series = FinSeries.newCandles();
        final StringValue yyyymmdd = new StringValue();
        final StringValue hhmmss = new StringValue();
        final LongValue tValue = new LongValue();
        new CsvReader()
                .file(file)
                .splitSeparator(";")
                .lineFilter(line -> !line.startsWith("<TICKER>"))
                .skipColumn() // TICKER
                .skipColumn() // PERIOD
                .value(yyyymmdd)
                .value(hhmmss)
                .column(series.open())
                .column(series.high())
                .column(series.low())
                .column(series.close())
                .column(series.volume())
                .computation(series.timeCode(), () -> {
                    final long t = TimeCodes.timeCode(Integer.parseInt(yyyymmdd.get()), Integer.parseInt(hhmmss.get()), 0);
                    tValue.set(t);
                    return t;
                })
                .rowFilter(() -> {
                    final long t = tValue.get();
                    final int yymmdd = TimeCodes.yyyymmdd(t);
                    return from <= yymmdd && yymmdd <= till && timeFilter.test(t);
                })
                .read();
        return series;
    }

    @Override
    public FinSeries readDaily() throws IOException {
        return read();
    }

    /**
     * Чтение тиковых данных формате "Финам". Заголовок имеет вид:<br>
     * &lt;DATE&gt;;&lt;TIME&gt;;&lt;LAST&gt;;&lt;VOL&gt;<br>
     * или<br>
     * &lt;DATE&gt;;&lt;TIME&gt;;&lt;LAST&gt;;&lt;VOL&gt;;&lt;BUYSELL&gt;
     *
     * @return тиковые данные
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    public FinSeries readTicks() throws IOException {
        final FinSeries series = FinSeries.newLastVol();
        if (hasBuySell) {
            series.withIntColumn(BUYSELL_COLUMN_NAME);
        }
        final IntValue yyyymmdd = new IntValue();
        final DoubleValue hhmmssms = new DoubleValue();
        final LongValue tValue = new LongValue();
        final CsvReader csvReader = new CsvReader()
                .file(file)
                .splitSeparator(";")
                .lineFilter(line -> !line.startsWith("<DATE>"))
                .value(yyyymmdd)
                .value(hhmmssms)
                .column(series.last())
                .column(series.volume());
        if (hasBuySell) {
            csvReader.column(series.getIntColumn(BUYSELL_COLUMN_NAME));
        }
        csvReader
                .computation(series.timeCode(), () -> {
                    final long t = TimeCodes.timeCode(yyyymmdd, hhmmssms);
                    tValue.set(t);
                    return t;
                })
                .rowFilter(() -> {
                    final long t = tValue.get();
                    final int yymmdd = TimeCodes.yyyymmdd(t);
                    return from <= yymmdd && yymmdd <= till && timeFilter.test(t);
                })
                .read();
        return series;
    }
}
