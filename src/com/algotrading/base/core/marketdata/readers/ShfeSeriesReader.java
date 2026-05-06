package com.algotrading.base.core.marketdata.readers;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.csv.CsvReader;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.values.IntValue;
import com.algotrading.base.core.values.LongValue;
import com.algotrading.base.core.values.StringValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.LongPredicate;

/**
 * Чтение данных SHFE из файла.
 */
public class ShfeSeriesReader extends SeriesReader {

    @Override
    public ShfeSeriesReader file(final File file) {
        super.file(file);
        return this;
    }

    @Override
    public ShfeSeriesReader path(final Path path) {
        super.path(path);
        return this;
    }

    @Override
    public ShfeSeriesReader from(final int yyyymmdd) {
        super.from(yyyymmdd);
        return this;
    }

    @Override
    public ShfeSeriesReader from(final LocalDate localDate) {
        super.from(localDate);
        return this;
    }

    @Override
    public ShfeSeriesReader till(final int yyyymmdd) {
        super.till(yyyymmdd);
        return this;
    }

    @Override
    public ShfeSeriesReader till(final LocalDate localDate) {
        super.till(localDate);
        return this;
    }

    @Override
    public ShfeSeriesReader timeFilter(final LongPredicate timeFilter) {
        super.timeFilter(timeFilter);
        return this;
    }

    /**
     * Чтение OHLCV-данных SHFE.
     * <p>
     * Первая строка имеет заголовок<br>
     * TradingDay,ActionDay,ContractCode,Session,CandleTime,OpenUpdateTime,CloseUpdateTime,OpenInterest,
     * UpperLimitPrice,LowerLimitPrice,OpenPrice,HighestPrice,LowestPrice,ClosePrice,CandleVolume,CandleTurnover
     * Данные имеют вид:<br>
     * 20250924,20250924,zn2510,day,15:00:00,15:00:00.500,15:00:00.500,42913.0,23480.0,20405.0,21845.0,21845.0,21845.0,21845.0,31290,3427476350.0
     * 20250924,20250923,zn2510,night,00:00:00,00:00:12.500,00:04:47.000,46532.0,23480.0,20405.0,21950.0,21950.0,21940.0,21945.0,10526,1154665125.0
     * ...
     * 20250924,20250923,zn2510,night,23:55:00,23:55:42.500,23:59:54.000,46587.0,23480.0,20405.0,21940.0,21950.0,21940.0,21950.0,10408,1141717175.0
     * <p>
     * Чтобы получить данные в хронологическом порядке, надо сначала взять блок данных ночной сессии с временем 21-23,
     * потом добавить блок данных ночной сессии с временем 00-04 с датой следующего дня, наконец добавить блок данных
     * дневной сессии.
     *
     * @return рыночные данные формата OHLCV
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    public FinSeries read() throws IOException {
        final FinSeries series = FinSeries.newCandles();
        final IntValue yyyymmdd = new IntValue();
        final StringValue dayNight = new StringValue();
        final StringValue hhmmss = new StringValue();
        final LongValue tValue = new LongValue();
        new CsvReader()
                .file(file)
                .splitSeparator(",")
                .linesToSkip(1)
                .skipColumn()
                .value(yyyymmdd)
                .skipColumn()
                .value(dayNight)
                .value(hhmmss)
                .skipColumn() // open update time
                .skipColumn() // close update time
                .skipColumn() // OI
                .skipColumn() // Upper
                .skipColumn() // Lower
                .column(series.open())
                .column(series.high())
                .column(series.low())
                .column(series.close())
                .column(series.volume())
                .skipColumn() // Turnover
                .computation(series.timeCode(), () -> {
                    int ymd = yyyymmdd.get();
                    final int hms = Integer.parseInt(hhmmss.get().replaceAll(":", ""));
                    final long t;
                    if ("night".equals(dayNight.get())) {
                        if (hms < 50000) {
                            final int year = ymd % 10000;
                            final int month = (ymd / 100) % 100;
                            final int day = ymd % 100;
                            final LocalDate date = LocalDate.of(year, month, day).plusDays(1);
                            ymd = date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
                        }
                    }
                    t = TimeCodes.t(ymd, hms);
                    tValue.set(t);
                    return t;
                })
                .rowFilter(() -> {
                    final long t = tValue.get();
                    final int yymmdd = TimeCodes.yyyymmdd(t);
                    return from <= yymmdd && yymmdd <= till && timeFilter.test(t);
                })
                .read();
        // Обеспечение хронологического порядка
        series.sortByTimeCode();
        // Расчёт объёмов отдельных свечей
        final LongColumn volume = series.volume();
        final int len = volume.length();
        for (int i = len - 1; i >= 1; i--) {
            final long currVolume = volume.get(i);
            final long prevVolume = volume.get(i - 1);
            if (currVolume > prevVolume) {
                volume.set(i, currVolume - prevVolume);
            } else {
                volume.set(i, currVolume);
            }
        }
        return series;
    }

    @Override
    public FinSeries readDaily() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public FinSeries readTicks() {
        throw new UnsupportedOperationException("Not implemented!");
    }
}
