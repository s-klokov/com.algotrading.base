package com.algotrading.base.core.window;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Окно, хранящее элементы в течение заданного промежутка времени.
 */
public class TimeWindow<E> implements Iterable<E> {

    private final Deque<E> eDeque = new ArrayDeque<>();
    private final Deque<Long> tDeque = new ArrayDeque<>();
    private final long durationNanos;
    private int modCount = 0;

    /**
     * Конструктор.
     *
     * @param duration длительность нахождения объекта в окне
     * @param unit     единица измерения длительности
     */
    public TimeWindow(final long duration, final TimeUnit unit) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Non-positive duration: " + duration);
        }
        durationNanos = unit.toNanos(duration);
    }

    /**
     * Удалить старые элементы из окна.
     */
    private void removeOldElements() {
        final long now = System.nanoTime();
        for (final long t : tDeque) {
            if (now - t >= durationNanos) {
                eDeque.removeFirst();
                tDeque.removeFirst();
                modCount++;
            } else {
                break;
            }
        }
    }

    /**
     * @return количество элементов, находящихся в окне в данный момент.
     */
    public int getCount() {
        removeOldElements();
        return eDeque.size();
    }

    /**
     * Очистить окно.
     */
    public void clear() {
        modCount++;
        eDeque.clear();
        tDeque.clear();
    }

    /**
     * Добавить элемент в окно.
     *
     * @param e элемент
     */
    public void add(final E e) {
        removeOldElements();
        modCount++;
        eDeque.addLast(e);
        tDeque.addLast(System.nanoTime());
    }

    /**
     * Узнать, имеется ли данный элемент в окне.
     *
     * @param e элемент
     * @return {@code true}, если данный элемент содержится в окне
     */
    public boolean contains(final E e) {
        removeOldElements();
        return eDeque.contains(e);
    }

    /**
     * Узнать, сколько наносекунд данный объект находится в окне.
     *
     * @param e элемент
     * @return -1, если объект отсутствует в окне, иначе количество наносекунд, которое объек присутствует в окне
     */
    public long getNanosInWindow(final E e) {
        removeOldElements();
        final Iterator<E> eIterator = eDeque.iterator();
        final Iterator<Long> tIterator = tDeque.iterator();
        while (eIterator.hasNext() && tIterator.hasNext()) {
            final long t = tIterator.next();
            if (Objects.equals(e, eIterator.next())) {
                return System.nanoTime() - t;
            }
        }
        return -1;
    }

    /**
     * Получить итератор по окну. Удаление элементов не поддерживается.
     * Порядок итерирования: от старых элементов к новым.
     *
     * @return итератор
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private final Iterator<E> eIterator;
            private final int expectedModCount;

            {
                removeOldElements();
                eIterator = eDeque.iterator();
                expectedModCount = modCount;
            }

            @Override
            public boolean hasNext() {
                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
                return eIterator.hasNext();
            }

            @Override
            public E next() {
                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
                return eIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
