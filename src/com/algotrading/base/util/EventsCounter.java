package com.algotrading.base.util;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Подсчёт количества событий, произошедших за последние {@link #duration} миллисекунд.
 */
public class EventsCounter {
    /**
     * Длительность промежутка времени в миллисекундах.
     */
    private final long duration;
    /**
     * Очередь зарегистрированных моментов времени.
     */
    private final Queue<Long> queue = new ArrayDeque<>();
    /**
     * Время (System.currentTimeMillis()) с гарантией монотонности.
     */
    private long time;

    /**
     * Конструктор.
     *
     * @param duration длительность промежутка времени в миллисекундах.
     */
    public EventsCounter(final long duration) {
        this.duration = duration;
        time = System.currentTimeMillis();
    }

    /**
     * Зарегистрировать событие.
     */
    public void registerEvent() {
        updateTime();
        queue.add(time);
    }

    /**
     * @return количество событий за последние {@link #duration} миллисекунд
     */
    public int getCount() {
        if (queue.isEmpty()) {
            return 0;
        }
        updateTime();
        final long bound = time - duration;
        while (true) {
            final Long t = queue.peek();
            if (t == null || t > bound) {
                return queue.size();
            }
            queue.remove();
        }
    }

    private void updateTime() {
        final long now = System.currentTimeMillis();
        if (time < now) {
            time = now;
        }
    }
}
