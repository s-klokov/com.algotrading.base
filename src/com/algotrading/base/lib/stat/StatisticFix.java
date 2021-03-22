package com.algotrading.base.lib.stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Класс для оценки скользящего среднего с данным размером окна по выборке.
 */
public class StatisticFix extends Statistic {

    private int cur = 0;
    private double[] xx;

    /**
     * @param capacity количество запоминаемых элементов статистики (размер окна).
     */
    public StatisticFix(final int capacity) {
        xx = new double[capacity];
    }

    /**
     * Конструктор, загружающий статистику из двоичного потока.
     *
     * @param dis поток.
     * @throws IOException если произошла ошибка чтения.
     */
    public StatisticFix(final DataInputStream dis) throws IOException {
        super(dis);
        xx = new double[dis.readInt()];
        cur = num % xx.length;
        for (int i = 0; i < xx.length; i++) {
            xx[i] = dis.readDouble();
        }
    }

    /**
     * Конструктор копирования.
     *
     * @param s статистика.
     */
    public StatisticFix(final StatisticFix s) {
        super(s);
        cur = s.cur;
        xx = new double[s.xx.length];
        System.arraycopy(s.xx, 0, xx, 0, xx.length);
    }

    @Override
    public StatisticFix getCopy() {
        return new StatisticFix(this);
    }

    @Override
    public void set(final StatisticBase s) throws ClassCastException {
        super.set(s);
        final StatisticFix sf = (StatisticFix) s;
        cur = sf.cur;
        xx = new double[sf.xx.length];
        System.arraycopy(sf.xx, 0, xx, 0, xx.length);
    }

    @Override
    public void add(final StatisticBase s) throws ClassCastException {
        final StatisticFix sf = (StatisticFix) s;
        final int n = sf.num;
        final int mod = sf.xx.length;
        for (int i = 0; i < n; i++) {
            add(sf.xx[(cur - n + i) % mod]);
        }
    }

    @Override
    public void clear() {
        super.clear();
        cur = 0;
    }

    @Override
    public void add(final double x) {
        if (num < xx.length) {
            super.add(x);
        } else {
            final double y = xx[cur];
            sum += x - y;
            sum2 += x * x - y * y;
        }
        xx[cur++] = x;
        if (cur >= xx.length) cur = 0;
    }

    @Override
    public void add(final double x, final int n) {
        for (int i = 0; i < n; i++) {
            add(x);
        }
    }

    @Override
    public void shift(final double a) {
        super.shift(a);
        for (int i = 0; i < num; i++) {
            xx[i] += a;
        }
    }

    @Override
    public void read(final DataInputStream dis) throws IOException {
        super.read(dis);
        xx = new double[dis.readInt()];
        cur = num % xx.length;
        for (int i = 0; i < xx.length; i++) {
            xx[i] = dis.readDouble();
        }
    }

    @Override
    public void write(final DataOutputStream dos) throws IOException {
        super.write(dos);
        dos.writeInt(xx.length);
        for (int i = 0; i < num; i++) {
            add(xx[(cur - num + i) % xx.length]);
        }
    }

    /**
     * Получить максимальный из всех запомненных элементов статистики.
     *
     * @return максимальный из всех запомненных элементов статистики.
     */
    public double getMax() {
        if (num == 0) return 0;
        double max = xx[0];
        for (int i = 1; i < num; i++) {
            if (max < xx[i]) {
                max = xx[i];
            }
        }
        return max;
    }

    /**
     * @param i номер элемента статистики от 0 до {@link #getNum()} - 1.
     * @return элемент статистики с данным номером.
     */
    public double get(final int i) {
        return xx[i];
    }

    /**
     * @return ёмкость статистики: запоминаемое число наблюдений.
     */
    public int getCapacity() {
        return xx.length;
    }

    /**
     * @return {@code true} если статистика заполнена, то есть число наблюдений равно
     *         или превосходит ёмкость окна выборки.
     */
    public boolean isFull() {
        return xx.length == num;
    }
}