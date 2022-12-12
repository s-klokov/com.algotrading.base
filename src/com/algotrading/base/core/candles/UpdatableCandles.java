package com.algotrading.base.core.candles;

import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.ColumnUpdater;
import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;

/**
 * Реализация обновляемых свечей.
 * <p>
 * Объект содержит временной ряд свечей с дополнительными колонками, список объектов типа
 * {@link ColumnUpdater} для обновления этих колонок и параметры накопления свечей
 * (см. поля объекта для детального описания).
 * <p>
 * Методы хранилища гарантируют, что ссылки на исходные колонки временного ряда
 * неизменны на протяжении всего срока жизни элемента хранилища. Поэтому рекомендуется
 * добавлять сразу все нужные дополнительные колонки в первоначальный временной ряд
 * и привязывать к ним обновляющие их объекты, реализующие интерфейс {@link ColumnUpdater}.
 * <p>
 * При добавлении в конец временного ряда новых свечных данных происходит расчёт
 * значений в дополнительных колонках для добавленных новых данных.
 * <p>
 * При урезании временного ряда с удалением самых ранних свечей происходит полный
 * перерасчёт значений в дополнительных колонках.
 */
public class UpdatableCandles {
    /**
     * Функция для реализации временного сдвига.
     */
    public final LongUnaryOperator timeShift;
    /**
     * Функция для фильтрации свечей по времени.
     */
    public final LongPredicate timeFilter;
    /**
     * Таймфрейм временного ряда.
     */
    public final int period;
    /**
     * Единица измерения таймфрейма.
     */
    public final TimeUnit unit;
    /**
     * Количество свечей, достижение или превышение которого приводит к удалению
     * самых старых свечей временного ряда.
     */
    public final int truncationSize;
    /**
     * Количество свечей, которое останется во временном ряде после удаления
     * самых старых свечей временного ряда.
     */
    public final int targetSize;
    /**
     * Временной ряд свечей, возможно, с дополнительными колонками
     */
    public final FinSeries series = FinSeries.newCandles();
    /**
     * Список объектов типа {@link ColumnUpdater} для обновления
     * дополнительных колонок во временном ряде.
     */
    public final List<ColumnUpdater> updaters = new ArrayList<>();

    /**
     * Конструктор.
     *
     * @param timeShift      временной сдвиг
     * @param timeFilter     временной фильтр
     * @param period         период (таймфрейм)
     * @param unit           единица изменения
     * @param truncationSize граница количества свечей для урезания временного ряда
     * @param targetSize     количество свечей после урезания временного ряда
     */
    public UpdatableCandles(final LongUnaryOperator timeShift,
                            final LongPredicate timeFilter,
                            final int period, final TimeUnit unit,
                            final int truncationSize,
                            final int targetSize) {
        this.timeShift = (timeShift == null) ? FinSeries.NO_TIME_SHIFT : timeShift;
        this.timeFilter = (timeFilter == null) ? FinSeries.ALL : timeFilter;
        this.period = period;
        this.unit = Objects.requireNonNull(unit);
        if (targetSize < 1 || truncationSize <= targetSize) {
            throw new IllegalArgumentException(
                    "targetSize = " + targetSize
                    + ", truncationSize = " + truncationSize);
        }
        this.truncationSize = truncationSize;
        this.targetSize = targetSize;
    }

    /**
     * @return длина временного ряда
     */
    public int length() {
        return series.timeCode().length();
    }

    /**
     * Если это возможно, добавить к уже имеющимся свечам новые свечи, предварительно выполнив
     * временной сдвиг, временную фильтрацию и компрессию.
     * <p>
     * Если свечей ещё нет, их меньше трёх или временной диапазон новых свечей накрывает полностью
     * временной диапазон старых, то результатом будет временной ряд из новых свечей,
     * иначе новый набор должен содержать свечу, идентичную предпоследней свече имеющегося набора,
     * для проведения "склейки" данных.
     *
     * @param newSeries           добавляемые свечи (исходный объект не изменяется при сдвиге, фильтрации и компрессии)
     * @param shiftFilterCompress {@code true}, если надо выполнять сдвиг, фильтрацию и компрессию новых свечей,
     *                            иначе {@code false}
     * @return если операция добавления прошла успешно, то индекс в итоговом наборе,
     * начиная с которого идут новые свечи, иначе -1
     */
    public int update(FinSeries newSeries, final boolean shiftFilterCompress) {
        if (shiftFilterCompress) {
            final LongUnaryOperator timeFrameStartFunction = timeCode -> TimeCodes.getTimeFrameStart(timeCode, period, unit);
            final LongColumn timeCode = series.timeCode();
            final int startIndex;
            if (timeCode.length() < 3) {
                startIndex = 0;
            } else {
                startIndex = getStartIndex(
                        newSeries.timeCode(), timeShift, timeFilter, timeFrameStartFunction,
                        timeCode.get(timeCode.length() - 2));
                if (startIndex < 0) {
                    return -1;
                }
            }
            newSeries = newSeries.compressedCandles(
                    timeShift, timeFilter, timeFrameStartFunction, startIndex
            );
        }
        final FinSeries oldSeries = series;
        final LongColumn oldTimeCode = oldSeries.timeCode();
        final LongColumn newTimeCode = newSeries.timeCode();
        if (shouldReplaceSeries(oldTimeCode, newTimeCode)) {
            int len = newTimeCode.length();
            oldSeries.setLength(len);
            final DoubleColumn oldOpen = oldSeries.open();
            final DoubleColumn oldHigh = oldSeries.high();
            final DoubleColumn oldLow = oldSeries.low();
            final DoubleColumn oldClose = oldSeries.close();
            final LongColumn oldVolume = oldSeries.volume();
            final DoubleColumn newOpen = newSeries.open();
            final DoubleColumn newHigh = newSeries.high();
            final DoubleColumn newLow = newSeries.low();
            final DoubleColumn newClose = newSeries.close();
            final LongColumn newVolume = newSeries.volume();
            for (int i = 0; i < len; i++) {
                oldTimeCode.set(i, newTimeCode.get(i));
                oldOpen.set(i, newOpen.get(i));
                oldHigh.set(i, newHigh.get(i));
                oldLow.set(i, newLow.get(i));
                oldClose.set(i, newClose.get(i));
                oldVolume.set(i, newVolume.get(i));
            }
            if (truncate()) {
                len = oldTimeCode.length();
            }
            for (final ColumnUpdater updater : updaters) {
                updater.update(0, len);
            }
            return 0;
        }
        if (newTimeCode.length() < 2) {
            return -1;
        }
        final int oldIndex = oldTimeCode.length() - 2;
        final long t = oldTimeCode.get(oldIndex);
        final int newIndex = newTimeCode.binarySearch(t);
        if (newIndex < 0) {
            return -1;
        }
        final DoubleColumn oldOpen = oldSeries.open();
        final DoubleColumn oldHigh = oldSeries.high();
        final DoubleColumn oldLow = oldSeries.low();
        final DoubleColumn oldClose = oldSeries.close();
        final LongColumn oldVolume = oldSeries.volume();
        final DoubleColumn newOpen = newSeries.open();
        final DoubleColumn newHigh = newSeries.high();
        final DoubleColumn newLow = newSeries.low();
        final DoubleColumn newClose = newSeries.close();
        final LongColumn newVolume = newSeries.volume();
        if (oldOpen.get(oldIndex) != newOpen.get(newIndex)
            || oldHigh.get(oldIndex) != newHigh.get(newIndex)
            || oldLow.get(oldIndex) != newLow.get(newIndex)
            || oldClose.get(oldIndex) != newClose.get(newIndex)
            || oldVolume.get(oldIndex) != newVolume.get(newIndex)) {
            return -1;
        }
        int len = oldTimeCode.length() - 2 + newTimeCode.length() - newIndex;
        oldSeries.setLength(len);
        for (int i = oldIndex + 1, j = newIndex + 1; j < newTimeCode.length(); i++, j++) {
            oldTimeCode.set(i, newTimeCode.get(j));
            oldOpen.set(i, newOpen.get(j));
            oldHigh.set(i, newHigh.get(j));
            oldLow.set(i, newLow.get(j));
            oldClose.set(i, newClose.get(j));
            oldVolume.set(i, newVolume.get(j));
        }
        final int startIndex;
        if (truncate()) {
            startIndex = 0;
            len = oldTimeCode.length();
        } else {
            startIndex = oldIndex + 1;
        }
        for (final ColumnUpdater updater : updaters) {
            updater.update(startIndex, len);
        }
        return startIndex;
    }

    private static boolean shouldReplaceSeries(final LongColumn oldTimeCode, final LongColumn newTimeCode) {
        return oldTimeCode.length() < 3
               || (newTimeCode.length() > 0
                   && newTimeCode.get(0) <= oldTimeCode.get(0)
                   && newTimeCode.getLast() >= oldTimeCode.getLast());
    }

    private boolean truncate() {
        final int size = series.timeCode().length();
        if (size < truncationSize) {
            return false;
        }
        series.move(targetSize - size);
        series.setLength(targetSize);
        return true;
    }

    private static int getStartIndex(final LongColumn timeCode,
                                     final LongUnaryOperator timeShift,
                                     final LongPredicate timeFilter,
                                     final LongUnaryOperator timeFrameStartFunction,
                                     final long startValue) {
        final int len = timeCode.length();
        for (int i = 0; i < len; i++) {
            long t = timeShift.applyAsLong(timeCode.get(i));
            if (timeFilter.test(t)) {
                t = timeFrameStartFunction.applyAsLong(t);
                if (t == startValue) {
                    return i;
                }
                if (t > startValue) {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Обновить последнюю или начать новую свечу; при этом сдвиг и фильтрация выполняются,
     * а компрессия -- нет.
     *
     * @param t время свечи
     * @param o значение Open
     * @param h значение High
     * @param l значение Low
     * @param c значение Close
     * @param v значение Volume
     * @return если операция обновления последней или добавления новой свечи прошла успешно
     * (в том числе если ничего не изменилось из-за использования фильтрации),
     * то индекс в итоговом наборе, начиная с которого идут новые свечи, иначе -1
     */
    public int update(long t, final double o, final double h, final double l, final double c, final long v) {
        t = timeShift.applyAsLong(t);
        final LongColumn timeCode = series.timeCode();
        int len = timeCode.length();
        if (!timeFilter.test(t)) {
            return len - 1;
        }
        if (len > 0 && t < timeCode.getLast()) {
            return -1;
        }
        final boolean isNewCandle;
        if (len == 0 || t > timeCode.getLast()) {
            isNewCandle = true;
            series.setLength(++len);
        } else {
            isNewCandle = false;
        }
        final DoubleColumn open = series.open();
        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        final LongColumn volume = series.volume();
        if (!isNewCandle) {
            if (high.getLast() > h || low.getLast() < l || volume.getLast() > v) {
                return -1;
            }
        }
        final int i = len - 1;
        timeCode.set(i, t);
        open.set(i, o);
        high.set(i, h);
        low.set(i, l);
        close.set(i, c);
        volume.set(i, v);

        final int startIndex;
        if (truncate()) {
            startIndex = 0;
            len = timeCode.length();
        } else {
            startIndex = i;
        }
        for (final ColumnUpdater updater : updaters) {
            updater.update(startIndex, len);
        }
        return startIndex;
    }
}
