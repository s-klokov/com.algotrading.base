package com.algotrading.base.lib.stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Интерфейс для классов, накапливающих статистику.
 */
public interface StatisticBase extends Serializable {

    /**
     * Создать копию.
     *
     * @return полная копия этого объекта.
     */
    StatisticBase getCopy();

    /**
     * Заменить эту статистику на указанную.
     *
     * @param s статистика.
     * @throws ClassCastException если указанная статистика не приводится к типу текущего класса.
     */
    void set(StatisticBase s) throws ClassCastException;

    /**
     * Очистить выборку.
     */
    void clear();

    /**
     * Добавить новый элемент в выборку.
     *
     * @param x новый элемент выборки.
     */
    void add(final double x);

    /**
     * Добавить данную статистику к этой статистике.
     * При этом считается, что эта статистика предшествовала статистике {@code s}.
     *
     * @param s статистика.
     * @throws ClassCastException если указанная статистика, которая не совместима с текущей статистикой.
     */
    void add(StatisticBase s) throws ClassCastException;

    /**
     * Сдвинуть статистику: ко всем элементам статистики добавить данное число.
     *
     * @param a число, прибавляемое ко всем элементам статистики.
     */
    void shift(final double a);

    /**
     * Получить количество элементов статистики.
     *
     * @return количество элементов статистики.
     */
    int getNum();

    /**
     * Получить выборочное среднее.
     *
     * @return выборочное среднее.
     */
    double getEv();

    /**
     * Получить выборочную дисперсию.
     *
     * @return выборочная дисперсия.
     */
    double getVar();

    /**
     * Половина ширины доверительного интервала, соответствующая квантили
     * нормального распределения u.
     * u = 1 соответствует уровню доверия 68.26%
     * u = 2 соответствует уровню доверия 95.45%
     * u = 3 соответствует уровню доверия 99.73%
     *
     * @param u квантиль нормального распределения.
     * @return половина ширины доверительного интервала.
     */
    double getCi(final double u);

    /**
     * Загрузить статистику из двоичного потока.
     *
     * @param dis поток.
     * @throws IOException если произошла ошибка записи.
     */
    void read(DataInputStream dis) throws IOException;

    /**
     * Сохранить статистику в двоичный поток.
     *
     * @param dos поток.
     * @throws IOException если произошла ошибка записи.
     */
    void write(DataOutputStream dos) throws IOException;
}
