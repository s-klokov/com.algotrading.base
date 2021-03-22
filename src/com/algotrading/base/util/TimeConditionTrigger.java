package com.algotrading.base.util;

import com.algotrading.base.core.values.Value;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Проверка срабатывания условия в зависимости от текущего времени.
 * Предполагается, что метод {@link #isTriggered()} вызывается достаточно часто,
 * чтобы не пропускать моменты срабатывания.
 * Если условие начало выполняться, то триггер срабатывает однократно.
 * Для последующего срабатывания триггера требуется, чтобы условие сначала перестало
 * выполняться, а потом снова выполнилось.
 */
public class TimeConditionTrigger {

    /**
     * Алгоритм получения текущего времени в формате {@link ZonedDateTime}.
     */
    private final Supplier<ZonedDateTime> currentTimeSupplier;
    /**
     * Условие срабатывания как функция от предыдущего момента времени и текущего момента времени.
     */
    private final BiPredicate<ZonedDateTime, ZonedDateTime> condition;
    /**
     * Последнее значение текущего времени. Гарантируется его монотонность.
     */
    private ZonedDateTime dateTime;
    /**
     * Признак срабатывания условия в предыдущий раз.
     */
    private boolean hasTriggered = false;

    /**
     * Конструктор.
     *
     * @param currentTimeSupplier алгоритм получения текущего времени
     * @param condition           условие срабатывания -- булева функция от предыдущего момента времени
     *                            и текущего момента времени
     */
    public TimeConditionTrigger(final Supplier<ZonedDateTime> currentTimeSupplier,
                                final BiPredicate<ZonedDateTime, ZonedDateTime> condition) {
        this.currentTimeSupplier = currentTimeSupplier;
        this.condition = condition;
        dateTime = currentTimeSupplier.get();
    }

    /**
     * Конструктор.
     *
     * @param currentTimeSupplier алгоритм получения текущего времени
     * @param condition           условие срабатывания -- булева функция от текущего момента времени
     */
    public TimeConditionTrigger(final Supplier<ZonedDateTime> currentTimeSupplier,
                                final Predicate<ZonedDateTime> condition) {
        this.currentTimeSupplier = currentTimeSupplier;
        this.condition = (prev, curr) -> condition.test(curr);
        dateTime = currentTimeSupplier.get();
    }

    /**
     * Конструктор.
     *
     * @param condition условие срабатывания -- булева функция от предыдущего момента времени
     *                  и текущего момента времени, определяемого по локальным часам
     */
    public TimeConditionTrigger(final BiPredicate<ZonedDateTime, ZonedDateTime> condition) {
        this(ZonedDateTime::now, condition);
    }

    /**
     * Конструктор.
     *
     * @param condition условие срабатывания -- булева функция от текущего момента времени
     */
    public TimeConditionTrigger(final Predicate<ZonedDateTime> condition) {
        this(ZonedDateTime::now, condition);
    }

    /**
     * @return {@code true}, если условие сработало, иначе {@code false}
     */
    public boolean isTriggered() {
        final ZonedDateTime currDateTime = currentTimeSupplier.get();
        if (!currDateTime.isAfter(dateTime)) {
            return false;
        }
        final ZonedDateTime prevDateTime = dateTime;
        dateTime = currDateTime;
        if (!hasTriggered) {
            if (condition.test(prevDateTime, currDateTime)) {
                hasTriggered = true;
                return true;
            } else {
                return false;
            }
        } else {
            if (!condition.test(prevDateTime, currDateTime)) {
                hasTriggered = false;
            }
            return false;
        }
    }

    /**
     * @return триггер, срабатывающий каждую секунду
     * (предполагается, что метод {@link #isTriggered()} вызывается достаточно часто)
     */
    public static TimeConditionTrigger getNewSecondTrigger() {
        return new TimeConditionTrigger(
                (prev, curr) -> prev.getSecond() != curr.getSecond() || prev.plusSeconds(5).isBefore(curr)
        );
    }

    /**
     * @return триггер, срабатывающий каждую минуту
     * (предполагается, что метод {@link #isTriggered()} вызывается достаточно часто)
     */
    public static TimeConditionTrigger getNewMinuteTrigger() {
        return new TimeConditionTrigger(
                (prev, curr) -> prev.getMinute() != curr.getMinute() || prev.plusMinutes(5).isBefore(curr)
        );
    }

    /**
     * @param sec1 секунда начала диапазона
     * @param sec2 секунда конца диапазона
     * @return триггер, срабатывающий при попадании в диапазон секунд от sec1 до sec2 включительно в рамках минуты
     * (предполагается, что метод {@link #isTriggered()} вызывается достаточно часто)
     */
    public static TimeConditionTrigger getIntraMinuteTrigger(final int sec1, final int sec2) {
        return new TimeConditionTrigger(
                now -> {
                    final int sec = now.getSecond();
                    if (sec1 <= sec2) {
                        return sec1 <= sec && sec <= sec2;
                    } else {
                        return sec >= sec1 || sec <= sec2;
                    }
                }
        );
    }

    /**
     * @param timeout величина задержки
     * @param unit    единица измерения
     * @return триггер, срабатывающий после указанной задержки
     * (предполагается, что метод {@link #isTriggered()} вызывается достаточно часто)
     */
    public static TimeConditionTrigger getDelayTrigger(final long timeout, final ChronoUnit unit) {
        final ZonedDateTime deadline = ZonedDateTime.now().plus(timeout, unit);
        return new TimeConditionTrigger(now -> !now.isBefore(deadline));
    }

    /**
     * @param timeout величина задержки
     * @param unit    единица измерения
     * @return триггер, срабатывающий периодически после указанной задержки
     * (предполагается, что метод {@link #isTriggered()} вызывается достаточно часто)
     */
    public static TimeConditionTrigger getPeriodicTrigger(final long timeout, final ChronoUnit unit) {
        final Value<ZonedDateTime> deadline = new Value<>(ZonedDateTime.now().plus(timeout, unit));
        return new TimeConditionTrigger(now -> {
            if (!now.isBefore(deadline.get())) {
                deadline.set(now.plus(timeout, unit));
                return true;
            } else {
                return false;
            }
        });
    }
}
