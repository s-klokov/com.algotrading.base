package com.algotrading.base.core.series;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.values.AbstractValue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;

/**
 * Финансовый временной ряд -- это набор данных, состоящий из нескольких колонок, которые могут содержать
 * время, данные open, high, low, close, volume, open interest data и / или другую дополнительную информацию.
 * <p>
 * В этом классе реализованы методы, которые имеют смысл для свечных временных рядов.
 * Эти методы, в основном, предназначены для компрессии свечей различных временных таймфреймов и не проверяют,
 * в самом ли деле временной ряд является свечным временным рядом.
 */
public class FinSeries extends Series {
    /**
     * Название для колонки "timeCode".
     */
    public static final String T = "T";
    /**
     * Название для колонки "open".
     */
    public static final String O = "O";
    /**
     * Название для колонки "high".
     */
    public static final String H = "H";
    /**
     * Название для колонки "low".
     */
    public static final String L = "L";
    /**
     * Название для колонки "close".
     */
    public static final String C = "C";
    /**
     * Название для колонки "volume".
     */
    public static final String V = "V";
    /**
     * Название для колонки "open interest".
     */
    public static final String OI = "OI";
    /**
     * Название для колонки "last".
     */
    public static final String LAST = "LAST";
    /**
     * Отсутствие сдвига по времени.
     */
    public static final LongUnaryOperator NO_TIME_SHIFT = t -> t;
    /**
     * Отсутствие фильтрации по времени.
     */
    public static final LongPredicate ALL = t -> true;

    public static FinSeries newCandles() {
        return new FinSeries()
                .withLongColumn(T)
                .withDoubleColumn(O)
                .withDoubleColumn(H)
                .withDoubleColumn(L)
                .withDoubleColumn(C)
                .withLongColumn(V);
    }

    public static FinSeries newLastVol() {
        return new FinSeries()
                .withLongColumn(T)
                .withDoubleColumn(LAST)
                .withLongColumn(V);
    }

    @Override
    public FinSeries ensureCapacity(final int capacity) {
        super.ensureCapacity(capacity);
        return this;
    }

    @Override
    public FinSeries withDoubleColumn(final String name) {
        super.withDoubleColumn(name);
        return this;
    }

    @Override
    public FinSeries withLongColumn(final String name) {
        super.withLongColumn(name);
        return this;
    }

    @Override
    public FinSeries withIntColumn(final String name) {
        super.withIntColumn(name);
        return this;
    }

    @Override
    public FinSeries withStringColumn(final String name) {
        super.withStringColumn(name);
        return this;
    }

    @Override
    public <T> FinSeries withColumn(final String name, final Class<T> type) {
        super.withColumn(name, type);
        return this;
    }

    public LongColumn timeCode() {
        return getLongColumn(T);
    }

    public DoubleColumn open() {
        return getDoubleColumn(O);
    }

    public DoubleColumn high() {
        return getDoubleColumn(H);
    }

    public DoubleColumn low() {
        return getDoubleColumn(L);
    }

    public DoubleColumn close() {
        return getDoubleColumn(C);
    }

    public LongColumn volume() {
        return getLongColumn(V);
    }

    public DoubleColumn last() {
        return getDoubleColumn(LAST);
    }

    public LongColumn oi() {
        return getLongColumn(OI);
    }

    public FinSeries compressedCandles(final int period, final TimeUnit unit) {
        return compressedCandles(timeCode -> true, period, unit);
    }

    public FinSeries compressedCandles(final LongPredicate timeFilter, final int period, final TimeUnit unit) {
        return compressedCandles(
                NO_TIME_SHIFT,
                timeFilter,
                timeCode -> TimeCodes.getTimeFrameStart(timeCode, period, unit),
                0);
    }

    public FinSeries compressedDailyCandles(final LongPredicate timeFilter) {
        return compressedCandles(
                NO_TIME_SHIFT,
                timeFilter,
                timeCode -> TimeCodes.t(TimeCodes.yyyymmdd(timeCode), 0),
                0);
    }

    public FinSeries compressedCandles(final LongUnaryOperator timeShift,
                                       final LongPredicate timeFilter,
                                       final LongUnaryOperator timeFrameStartFunction,
                                       final int startIndex) {
        final LongColumn time = timeCode();
        final DoubleColumn open = open();
        final DoubleColumn high = high();
        final DoubleColumn low = low();
        final DoubleColumn close = close();
        final DoubleColumn last = last();
        final LongColumn volume = volume();
        final LongColumn oi = oi();
        final FinSeries series = new FinSeries();
        final LongColumn cTime = series.acquireLongColumn(T);
        final DoubleColumn cOpen = series.acquireDoubleColumn(O);
        final DoubleColumn cHigh = series.acquireDoubleColumn(H);
        final DoubleColumn cLow = series.acquireDoubleColumn(L);
        final DoubleColumn cClose = series.acquireDoubleColumn(C);
        final LongColumn cVolume = (volume == null) ? null : series.acquireLongColumn(V);
        final LongColumn cOi = (oi == null) ? null : series.acquireLongColumn(OI);
        final int length = length();
        long frameTimeCode = 0L;
        for (int index = startIndex, cIndex = -1; index < length; index++) {
            final long timeCode = timeShift.applyAsLong(time.get(index));
            if (timeFilter != null && !timeFilter.test(timeCode)) {
                continue;
            }
            final long currFrameTimeCode = timeFrameStartFunction.applyAsLong(timeCode);
            if (frameTimeCode != currFrameTimeCode) {
                frameTimeCode = currFrameTimeCode;
                cTime.append(frameTimeCode);
                if (open != null) {
                    cOpen.append(open.get(index));
                } else if (last != null) {
                    cOpen.append(last.get(index));
                } else if (close != null) {
                    cOpen.append(close.get(index));
                }
                if (high != null) {
                    cHigh.append(high.get(index));
                } else if (last != null) {
                    cHigh.append(last.get(index));
                } else if (close != null) {
                    cHigh.append(close.get(index));
                }
                if (low != null) {
                    cLow.append(low.get(index));
                } else if (last != null) {
                    cLow.append(last.get(index));
                } else if (close != null) {
                    cLow.append(close.get(index));
                }
                if (close != null) {
                    cClose.append(close.get(index));
                } else if (last != null) {
                    cClose.append(last.get(index));
                }
                if (volume != null) {
                    cVolume.append(volume.get(index));
                }
                if (oi != null) {
                    cOi.append(oi.get(index));
                }
                cIndex++;
            } else {
                if (high != null) {
                    cHigh.set(cIndex, Math.max(cHigh.get(cIndex), high.get(index)));
                } else if (last != null) {
                    cHigh.set(cIndex, Math.max(cHigh.get(cIndex), last.get(index)));
                } else if (close != null) {
                    cHigh.set(cIndex, Math.max(cHigh.get(cIndex), close.get(index)));
                }
                if (low != null) {
                    cLow.set(cIndex, Math.min(cLow.get(cIndex), low.get(index)));
                } else if (last != null) {
                    cLow.set(cIndex, Math.min(cLow.get(cIndex), last.get(index)));
                } else if (close != null) {
                    cLow.set(cIndex, Math.min(cLow.get(cIndex), close.get(index)));
                }
                if (close != null) {
                    cClose.set(cIndex, close.get(index));
                } else if (last != null) {
                    cClose.set(cIndex, last.get(index));
                }
                if (volume != null) {
                    cVolume.set(cIndex, cVolume.get(cIndex) + volume.get(index));
                }
                if (oi != null) {
                    cOi.set(cIndex, oi.get(index));
                }
            }
        }
        series.length();
        return series;
    }

    @Override
    public FinSeries append(final Series series) {
        super.append(series);
        return this;
    }

    @Override
    public FinSeries append(final Series series, final int rowId) {
        super.append(series, rowId);
        return this;
    }

    @Override
    public FinSeries appendRow(final AbstractValue... values) {
        super.appendRow(values);
        return this;
    }

    @Override
    public FinSeries copy() {
        return copy(0, length());
    }

    @Override
    public FinSeries copy(final int from, final int to) {
        final FinSeries series = new FinSeries();
        columnMap.forEach((key, value) -> series.columnMap.put(key, value.copy(from, to)));
        return series;
    }

    @Override
    public FinSeries subSeries(final int[] indices) {
        final FinSeries series = new FinSeries();
        columnMap.forEach((key, value) -> series.columnMap.put(key, value.subColumn(indices)));
        return series;
    }

    public FinSeries subSeries(final long timeCodeFrom, final long timeCodeTill) {
        final LongColumn timeCode = timeCode();
        final int len = timeCode.length();
        int size = 0;
        for (int i = 0; i < len; i++) {
            final long t = timeCode.get(i);
            if (timeCodeFrom <= t && t < timeCodeTill) {
                size++;
            }
        }
        final int[] indices = new int[size];
        for (int i = 0, j = 0; i < len; i++) {
            final long t = timeCode.get(i);
            if (timeCodeFrom <= t && t < timeCodeTill) {
                indices[j] = i;
                j++;
            }
        }
        return subSeries(indices);
    }

    @Override
    public String toString() {
        return "FinSeries[" + length() + ']' + Arrays.toString(columnNames());
    }

    public FinSeries sortByTimeCode() {
        return (FinSeries) sortBy(timeCode());
    }
}
