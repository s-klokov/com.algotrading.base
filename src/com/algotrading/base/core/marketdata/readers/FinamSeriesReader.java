package com.algotrading.base.core.marketdata.readers;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.csv.CsvReader;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.values.DoubleValue;
import com.algotrading.base.core.values.IntValue;
import com.algotrading.base.core.values.LongValue;
import com.algotrading.base.core.values.StringValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.LongPredicate;

/**
 * Чтение рыночных данных в формате "Финам" из файла.
 */
public class FinamSeriesReader extends SeriesReader {
    /**
     * Название колонки с информацией о направлении сделки для тиковых данных.
     */
    public static final String BUYSELL_COLUMN_NAME = "BUYSELL";

    /**
     * Признак наличия колонки с информацией о направлении сделки для тиковых данных.
     */
    protected boolean hasBuySell = false;
    /**
     * Количестро начальных колонок с данными о тикере, периоде и пр.,
     * которые надо пропустить при чтении свечных данных.
     */
    protected int numColumnsToSkip = 2;

    @Override
    public FinamSeriesReader file(final File file) {
        super.file(file);
        return this;
    }

    @Override
    public FinamSeriesReader path(final Path path) {
        super.path(path);
        return this;
    }

    @Override
    public FinamSeriesReader from(final int yyyymmdd) {
        super.from(yyyymmdd);
        return this;
    }

    @Override
    public FinamSeriesReader from(final LocalDate localDate) {
        super.from(localDate);
        return this;
    }

    @Override
    public FinamSeriesReader till(final int yyyymmdd) {
        super.till(yyyymmdd);
        return this;
    }

    @Override
    public FinamSeriesReader till(final LocalDate localDate) {
        super.till(localDate);
        return this;
    }

    @Override
    public FinamSeriesReader timeFilter(final LongPredicate timeFilter) {
        super.timeFilter(timeFilter);
        return this;
    }

    public FinamSeriesReader hasBuySell(final boolean hasBuySell) {
        this.hasBuySell = hasBuySell;
        return this;
    }

    public FinamSeriesReader numColumnsToSkip(final int numColumnsToSkip) {
        this.numColumnsToSkip = numColumnsToSkip;
        return this;
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
        final CsvReader csvReader = new CsvReader()
                .file(file)
                .splitSeparator(";")
                .lineFilter(line -> !line.startsWith("<TICKER>"));
        for (int i = 0; i < numColumnsToSkip; i++) {
            csvReader.skipColumn(); // TICKER, PERIOD, ...
        }
        csvReader
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
