package com.algotrading.base.lib.stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Обычная статистика.
 */
public class Statistic implements StatisticBase {
    /**
     * Количество выборочных значений.
     */
    protected int num;
    /**
     * Сумма выборочных значений.
     */
    protected double sum;
    /**
     * Сумма квадратов выборочных значений.
     */
    protected double sum2;

    /**
     * Конструктор, создающий пустую статистику.
     */
    public Statistic() {
        num = 0;
        sum = 0;
        sum2 = 0;
    }

    /**
     * Сконструировать статистику, которая имела бы указанные количество испытаний,
     * оценку математического ожидания и оценку стандартного отклонения.
     *
     * @param num количество испытаний.
     * @param ev  оценка математического ожидания.
     * @param sd  оценка стандартного отклонения.
     */
    public Statistic(final int num, final double ev, final double sd) {
        if (num <= 0) {
            this.num = 0;
            sum = 0;
            sum2 = 0;
        } else {
            this.num = num;
            sum = ev * num;
            sum2 = (num - 1) * sd * sd + num * ev * ev;
        }
    }

    /**
     * Сконструировать статистику, которая имела бы указанные количество испытаний,
     * оценку математического ожидания и половину ширины доверительного интервала
     * с указанной квантилью нормального распределения.
     *
     * @param num   количество испытаний.
     * @param ev    оценка математического ожидания.
     * @param delta половина ширины доверительного интервала.
     * @param u     квантиль нормального распределения.
     */
    public Statistic(final int num, final double ev, final double delta, final double u) {
        this(num, ev, delta * Math.sqrt(num) / u);
    }

    /**
     * Конструктор копирования.
     *
     * @param s статистика.
     */
    public Statistic(final Statistic s) {
        num = s.num;
        sum = s.sum;
        sum2 = s.sum2;
    }

    /**
     * Конструктор, загружающий статистику из двоичного потока.
     *
     * @param dis поток.
     * @throws IOException если произошла ошибка чтения.
     */
    public Statistic(final DataInputStream dis) throws IOException {
        num = dis.readInt();
        sum = dis.readDouble();
        sum2 = dis.readDouble();
    }

    @Override
    public Statistic getCopy() {
        return new Statistic(this);
    }

    @Override
    public void set(final StatisticBase s) throws ClassCastException {
        final Statistic ss = (Statistic) s;
        num = ss.num;
        sum = ss.sum;
        sum2 = ss.sum2;
    }

    @Override
    public void clear() {
        num = 0;
        sum = 0;
        sum2 = 0;
    }

    @Override
    public void add(final double x) {
        num++;
        sum += x;
        sum2 += x * x;
    }

    /**
     * Добавить данный элемент с данной кратностью.
     *
     * @param x элемент.
     * @param n кратность.
     */
    public void add(final double x, final int n) {
        num += n;
        sum += n * x;
        sum2 += n * x * x;
    }

    @Override
    public void add(final StatisticBase s) throws ClassCastException {
        final Statistic ss = (Statistic) s;
        num += ss.num;
        sum += ss.sum;
        sum2 += ss.sum2;
    }

    @Override
    public void shift(final double a) {
        sum2 += a * (a * num + 2 * sum);
        sum += a * num;
    }

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public double getEv() {
        return (num == 0) ? 0 : sum / num;
    }

    @Override
    public double getVar() {
        return (num <= 1) ? 0 : (sum2 - sum * sum / num) / (num - 1);
    }

    public double getSum() {
        return sum;
    }

    @Override
    public double getCi(final double u) {
        return u * Math.sqrt(getVar() / getNum());
    }

    @Override
    public void read(final DataInputStream dis) throws IOException {
        num = dis.readInt();
        sum = dis.readDouble();
        sum2 = dis.readDouble();
    }

    @Override
    public void write(final DataOutputStream dos) throws IOException {
        dos.writeInt(num);
        dos.writeDouble(sum);
        dos.writeDouble(sum2);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "n = %d, sum = %.4f, sum2 = %.4f, Ev = %.4f+-%.4f (95%%)",
                num, sum, sum2, getEv(), getCi(1.96));
    }
}
