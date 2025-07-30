package com.algotrading.base.core.marketdata.futures;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Справочник данных о фандинге вечных фьючерсов. Данные загружаются из файла csv-формата вида:
 * <pre>
 * 20250718185000000;10.93600000;0.00732000
 * 20250721185000000;10.85900000;0.00693000
 * 20250722185000000;10.93600000;0.00866000
 * </pre>
 * (первая колонка -- время фандинга, вторая -- settlement price, третья -- размер фандинга).
 * <p>
 * Возвращается временной ряд, где в первой колонке стоит время фандинга, а во второй -- значение фандинга.
 */
public class PerpetualFuturesFunding {

    private LongColumn timeCode = null;
    private DoubleColumn fundingRates = null;

    /**
     * Прочитать данные о фандинге из файла.
     *
     * @param file файл
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public void readFundingRates(final File file) throws IOException {
        final FinSeries series = new FinSeries();
        timeCode = series.acquireLongColumn(FinSeries.T);
        fundingRates = series.acquireDoubleColumn("fundingRate");
        for (final String line : Files.readAllLines(file.toPath())) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            final String[] parts = line.split(";");
            final long t = Long.parseLong(parts[0]);
            double fundingRate = 0.0;
            if (parts.length == 3) {
                fundingRate = Double.parseDouble(parts[2]);
            }
            timeCode.append(t);
            fundingRates.append(fundingRate);
        }
    }

    /***
     * Получить значение фандинга в расчёте на один контракт.
     *
     * @param t момент времени
     * @return значение фандинга
     */
    public double getFundingPerUnit(final long t) {
        if (timeCode == null || timeCode.length() == 0 || t < timeCode.get(0)) {
            return Double.NaN;
        }
        int id = timeCode.binarySearch(t);
        if (id >= 0) {
            return fundingRates.get(id);
        }
        id = -id - 1;
        return fundingRates.get(id - 1);
    }
}
