package com.algotrading.base.core.columns;

/**
 * Интерфейс для объектов, обновляющих значения в одной или нескольких колонках.
 */
public interface ColumnUpdater {

    /**
     * Выполнить обновление значений в указанном диапазоне индексов.
     *
     * @param startIndex индекс, начиная с которого (включая) нужно произвести обновление
     * @param endIndex   индекс, до которого (исключая) нужно произвести обновление
     */
    void update(int startIndex, int endIndex);
}
