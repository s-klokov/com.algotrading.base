package com.algotrading.base.util;

/**
 * Реализация индексированного диапазона на базе массива.
 * <p>
 * Индексация в диапазоне производится с помощью индексов от -нижняя граница до +верхняя граница так,
 * что индекс базовой линии равен 0.
 * <p>
 * Диапазон можно расширять, добавляя элементы снизу и сверху, и сужать, удаляя элементы снизу и сверху,
 * поэтому он похож на дек.
 * <p>
 * Возможно смещение базовой линии вверх/вниз, при этом индексы всех элементов диапазона смещаются на одну
 * и ту же величину.
 */
public class IndexedRange<E> {
    /**
     * Индекс массива, где лежит нижний элемент.
     */
    private int lowIndex;
    /**
     * Индекс массива, где лежит верхний элемент.
     */
    private int highIndex;
    /**
     * Индекс массива, где лежит элемент базовой линии.
     */
    private int baselineIndex;
    /**
     * Массив, на базе которого реализована структура.
     */
    private E[] elements;

    /**
     * Конструктор.
     */
    @SuppressWarnings("unchecked")
    public IndexedRange() {
        lowIndex = -1;
        highIndex = -1;
        baselineIndex = -1;
        elements = (E[]) new Object[16];
    }

    /**
     * @return размер диапазона.
     */
    public int size() {
        if (baselineIndex < 0) {
            return 0;
        } else if (lowIndex <= highIndex) {
            return highIndex - lowIndex + 1;
        } else {
            return highIndex + elements.length - lowIndex + 1;
        }
    }

    /**
     * @return {@code true}, если диапазон пуст.
     */
    public boolean isEmpty() {
        return baselineIndex < 0;
    }

    private void rangeCheck(final int i) {
        if (isEmpty()) {
            throw new ArrayIndexOutOfBoundsException(i);
        } else if (i > 0) {
            if (baselineIndex <= highIndex) {
                if (i > highIndex - baselineIndex) {
                    throw new ArrayIndexOutOfBoundsException(i);
                }
            } else {
                if (i > highIndex + elements.length - baselineIndex) {
                    throw new ArrayIndexOutOfBoundsException(i);
                }
            }
        } else if (i < 0) {
            if (lowIndex <= baselineIndex) {
                if (i < lowIndex - baselineIndex) {
                    throw new ArrayIndexOutOfBoundsException(i);
                }
            } else {
                if (i < lowIndex - elements.length - baselineIndex) {
                    throw new ArrayIndexOutOfBoundsException(i);
                }
            }
        }
    }

    /**
     * Перевести внешний индекс элемента в индекс внутри массива, на базе которого реализуется диапазон.
     *
     * @param i внешний индекс
     * @return внутренний индекс
     */
    private int index(final int i) {
        final int index = baselineIndex + i;
        if (index > elements.length - 1) {
            return index - elements.length;
        } else if (index < 0) {
            return index + elements.length;
        } else {
            return index;
        }
    }

    /**
     * Увеличить размер массива, на базе которого реализуется диапазон.
     */
    @SuppressWarnings("unchecked")
    private void expand() {
        final int len = elements.length;
        final E[] newElements = (E[]) new Object[2 * len];
        if (lowIndex <= highIndex) {
            System.arraycopy(elements, lowIndex, newElements, lowIndex, highIndex - lowIndex + 1);
        } else {
            System.arraycopy(elements, lowIndex, newElements, lowIndex, len - lowIndex);
            System.arraycopy(elements, 0, newElements, len, highIndex + 1);
            highIndex += len;
        }
        elements = newElements;
    }

    /**
     * Добавить элемент снизу диапазона.
     *
     * @param element элемент
     */
    public void addLow(final E element) {
        final int size = size();
        if (size == 0) {
            elements[0] = element;
            lowIndex = 0;
            highIndex = 0;
            baselineIndex = 0;
        } else {
            if (size == elements.length) {
                expand();
            }
            lowIndex--;
            if (lowIndex < 0) {
                lowIndex = elements.length - 1;
            }
            elements[lowIndex] = element;
        }
    }

    /**
     * Добавить элемент сверху диапазона.
     *
     * @param element элемент
     */
    public void addHigh(final E element) {
        final int size = size();
        if (size == 0) {
            elements[0] = element;
            lowIndex = 0;
            highIndex = 0;
            baselineIndex = 0;
        } else {
            if (size == elements.length) {
                expand();
            }
            highIndex++;
            if (highIndex > elements.length - 1) {
                highIndex = 0;
            }
            elements[highIndex] = element;
        }
    }

    /**
     * Удалить элемент снизу диапазона.
     *
     * @return удалённый элемент
     */
    public E removeLow() {
        final int size = size();
        if (size == 0) {
            return null;
        }
        final E element = elements[lowIndex];
        elements[lowIndex] = null;
        if (size == 1) {
            lowIndex = -1;
            highIndex = -1;
            baselineIndex = -1;
        } else {
            lowIndex++;
            if (lowIndex > elements.length - 1) {
                lowIndex = 0;
            }
        }
        return element;
    }

    /**
     * Удалить элемент сверху диапазона.
     *
     * @return удалённый элемент
     */
    public E removeHigh() {
        final int size = size();
        if (size == 0) {
            return null;
        }
        final E element = elements[highIndex];
        elements[highIndex] = null;
        if (size == 1) {
            lowIndex = -1;
            highIndex = -1;
            baselineIndex = -1;
        } else {
            highIndex--;
            if (highIndex < 0) {
                highIndex = elements.length - 1;
            }
        }
        return element;
    }

    /**
     * @return внешний индекс нижнего элемента диапазона.
     */
    public int getLowId() {
        if (isEmpty()) {
            return -1;
        } else if (lowIndex <= baselineIndex){
             return lowIndex - baselineIndex;
        } else {
            return lowIndex - baselineIndex - elements.length;
        }
    }

    /**
     * @return внешний индекс верхнего элемента диапазона.
     */
    public int getHighId() {
        if (isEmpty()) {
            return -1;
        } else if (highIndex >= baselineIndex){
            return highIndex - baselineIndex;
        } else {
            return highIndex - baselineIndex + elements.length;
        }
    }

    /**
     * @return нижний элемент диапазона.
     */
    public E getLow() {
        return isEmpty() ? null : elements[lowIndex];
    }

    /**
     * @return верхний элемент диапазона.
     */
    public E getHigh() {
        return isEmpty() ? null : elements[highIndex];
    }

    /**
     * @return элемент диапазона на базовой линии.
     */
    public E getBaseline() {
        return isEmpty() ? null : elements[baselineIndex];
    }

    /**
     * Получить элемент диапазона с заданным индексом.
     *
     * @param i индекс
     * @return элемент с заданным индексом
     */
    public E get(final int i) {
        rangeCheck(i);
        return elements[index(i)];
    }

    /**
     * Сместить базовую линию на указанную величину.
     *
     * @param move величина сдвига
     */
    public void moveBaseline(final int move) {
        rangeCheck(move);
        baselineIndex += move;
        if (baselineIndex < 0) {
            baselineIndex += elements.length;
        } else if (baselineIndex > elements.length - 1) {
            baselineIndex -= elements.length;
        }
    }
}
