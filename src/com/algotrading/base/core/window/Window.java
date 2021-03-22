package com.algotrading.base.core.window;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * Окно фиксированного размера.
 */
public class Window<E> implements Iterable<E> {
    private final E[] entries;
    private int size;
    private int pointer;
    private int modCount;

    /**
     * Конструктор.
     *
     * @param capacity размер окна, когда оно полностью запоняется
     */
    @SuppressWarnings("unchecked")
    public Window(final int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Illegal window capacity:" + capacity);
        }
        entries = (E[]) new Object[capacity];
        size = 0;
        pointer = -1;
        modCount = 0;
    }

    /**
     * Добавить элемент в окно. Новые элементы вытесняют старые, когда окно полностью заполнено.
     *
     * @param entry добавляемый элемент
     */
    public void add(final E entry) {
        modCount++;
        pointer++;
        if (pointer == entries.length) {
            pointer = 0;
        }
        entries[pointer] = entry;
        if (size < entries.length) {
            size++;
        }
    }

    /**
     * Получить элемент с указанным сдвигом.
     * Последний элемент имеет сдвиг 0, предпоследний элемент -- сдвиг (-1),
     * самый старый элемент окна имеет сдвиг {@code -size + 1}.
     *
     * @param offset сдвиг (0 или отрицательное число)
     * @return элемент с указанным сдвигом
     */
    public E get(final int offset) {
        if (offset > 0 || offset <= -size) {
            throw new ArrayIndexOutOfBoundsException(offset);
        }
        int i = pointer + offset;
        if (i < 0) {
            i += entries.length;
        }
        return entries[i];
    }

    /**
     * Задать элемент окна.
     * Последний элемент имеет сдвиг 0, предпоследний элемент -- сдвиг (-1),
     * самый старый элемент окна имеет сдвиг {@code -size + 1}.
     *
     * @param offset сдвиг (0 или отрицательное число)
     * @param e      элемент
     */
    public void set(final int offset, final E e) {
        if (offset > 0 || offset <= -size) {
            throw new ArrayIndexOutOfBoundsException(offset);
        }
        int i = pointer + offset;
        if (i < 0) {
            i += entries.length;
        }
        entries[i] = e;
    }

    /**
     * Очистить окно.
     */
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++) {
            entries[i] = null;
        }
        size = 0;
        pointer = -1;
    }

    /**
     * @return максимальный размер окна
     */
    public int capacity() {
        return entries.length;
    }

    /**
     * @return текущий размер окна
     */
    public int size() {
        return size;
    }

    /**
     * @return {@code true}, если окно заполнено
     */
    public boolean isFull() {
        return size == entries.length;
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
            private final int expectedModCount = modCount;
            private int offset = -size + 1;

            @Override
            public boolean hasNext() {
                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
                return offset <= 0;
            }

            @Override
            public E next() {
                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
                return get(offset++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
