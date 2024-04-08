package com.algotrading.base.core.sync;

import com.algotrading.base.core.columns.LongColumn;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Синхронизатор для нескольких колонок типа {@link LongColumn}.
 * <p>
 * Для синхронизации нескольких финансовых временных рядов используются их колонки timeCode().
 * Комментариии даны в соответствующей интерпретации.
 */
public class Synchronizer {
    /**
     * Колонки для синхронизации и их курсоры.
     */
    private final Map<LongColumn, Cursor> columns = new HashMap<>();
    /**
     * Значение t, по которому прошла последняя синхронизация.
     */
    private long t = 0L;

    /**
     * Добавить колонку в синхронизатор.
     *
     * @param longColumn колонка
     */
    public void put(final LongColumn longColumn) {
        columns.putIfAbsent(requireNonNull(longColumn), new Cursor());
    }

    /**
     * Установить синхронизатор в начальное состояние.
     */
    public void reset() {
        for (final Cursor cursor : columns.values()) {
            cursor.currIndex = -1;
            cursor.prevIndex = -1;
        }
        t = 0L;
    }

    /**
     * Провести очередную синхронизацию.
     *
     * @return значение timeCode по которому прошла синхронизация или {@link Long#MAX_VALUE}, если достигнут конец.
     */
    public long synchronize() {
        t = Long.MAX_VALUE;
        for (final Map.Entry<LongColumn, Cursor> e : columns.entrySet()) {
            final LongColumn longColumn = e.getKey();
            final Cursor cursor = e.getValue();
            final int nextIndex = cursor.currIndex + 1;
            if (nextIndex < longColumn.length()) {
                t = Math.min(t, longColumn.get(nextIndex));
            }
        }
        for (final Map.Entry<LongColumn, Cursor> entry : columns.entrySet()) {
            final LongColumn longColumn = entry.getKey();
            final Cursor cursor = entry.getValue();
            cursor.prevIndex = cursor.currIndex;
            final int nextIndex = cursor.currIndex + 1;
            if (nextIndex < longColumn.length() && longColumn.get(nextIndex) == t) {
                cursor.currIndex = nextIndex;
            }
        }
        return t;
    }

    /**
     * Проверить, что все колонки одновременно синхронизированы.
     *
     * @return {@code true} или {@code false}.
     */
    public boolean isFullySynchronized() {
        for (final Map.Entry<LongColumn, Cursor> e : columns.entrySet()) {
            final int currIndex = e.getValue().currIndex;
            if (currIndex == -1 || e.getKey().get(currIndex) != t) {
                return false;
            }
        }
        return true;
    }

    /**
     * Получить индекс, по которому прошла последняя синхронизация данной колонки.
     *
     * @param longColumn колонка
     * @return индекс, по которому прошла последняя синхронизация данной колонки,  или -1, если синхронизация
     * ещё не выполнялась или такая колонка отсутствует
     */
    public int getLastIndex(final LongColumn longColumn) {
        final Cursor cursor = columns.get(longColumn);
        return (cursor == null) ? -1 : cursor.currIndex;
    }

    /**
     * Получить индекс текущей синхронизации набора колонок применительно к данной колонке.
     *
     * @param longColumn колонка
     * @return индекс текущей синхронизации применительно к данной колонке или -1, если синхронизация ещё
     * не выполнялась, колонка не содержит текущее значение timeCode или колонка отсутствует
     */
    public int getCurrIndex(final LongColumn longColumn) {
        final Cursor cursor = columns.get(longColumn);
        return (cursor == null || cursor.currIndex < 0 || longColumn.get(cursor.currIndex) != t) ? -1 : cursor.currIndex;
    }

    /**
     * Получить индекс элемента, следующего за тем, по которому прошла последняя синхронизация.
     *
     * @param longColumn колонка
     * @return индекс элемента, следующего за тем, по которому прошла последняя синхронизация
     */
    public int getNextIndex(final LongColumn longColumn) {
        return columns.get(longColumn).currIndex + 1;
    }

    /**
     * Проверить, изменился ли индекс элемента, по которому прошла синхронизация, по сравнению с его предыдущим
     * значением.
     *
     * @param longColumn колонка
     * @return индекс текущего элемента, если последняя синхронизация поменяла его, иначе -1
     */
    public int getUpdatedIndex(final LongColumn longColumn) {
        final Cursor cursor = columns.get(longColumn);
        return (cursor != null) && (cursor.currIndex != cursor.prevIndex) ? cursor.currIndex : -1;
    }

    /**
     * @return текущее значение {@link #t}.
     */
    public long t() {
        return t;
    }

    private static class Cursor {
        int currIndex;
        int prevIndex;

        private Cursor() {
            currIndex = -1;
            prevIndex = -1;
        }
    }
}