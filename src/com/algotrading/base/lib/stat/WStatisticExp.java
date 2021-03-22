package com.algotrading.base.lib.stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Статистика с экспоненциальным сглаживанием, элементы которой имеют вес.
 */
public final class WStatisticExp implements StatisticBase {

    /**
     * Скорость забывания прошлого - число от 0 до 1
     * Чем ближе q к 1, тем медленнее забывается прошлое.<br>
     * Назовём периодом полураспада статистики такой номер N,
     * что элемент выборки номер N учитывается в общей сумме с
     * коэффициентом 0.5. Тогда справедлива формула
     * q = 0.5^(1/(N-1)).
     */
    private double q;
    /**
     * Число наблюдений.
     */
    private int num;
    /**
     * Взвешенная сумма элементов статистики.
     */
    private double sum;
    /**
     * Взвешенная сумма квадратов элементов статистики.
     */
    private double sum2;
    /**
     * Сумма весов.
     */
    private double sumw;
    /**
     * Сумма квадратов весов.
     */
    private double sumw2;

    /**
     * @param q скорость забывания прошлого - число от 0 до 1
     *          Чем ближе q к 1, тем медленнее забывается прошлое.<br>
     *          Назовём периодом полураспада статистики такой номер N,
     *          что элемент выборки номер N учитывается в общей сумме с
     *          коэффициентом 0.5. Тогда справедлива формула
     *          q = 0.5^(1/(N-1)).
     */
    public WStatisticExp(final double q) {
        this.q = q;
        num = 0;
        sum = 0.0;
        sum2 = 0.0;
        sumw = 0.0;
        sumw2 = 0.0;
    }

    /**
     * Конструктор копирования.
     *
     * @param s оригинал.
     */
    public WStatisticExp(final WStatisticExp s) {
        q = s.q;
        num = s.num;
        sum = s.sum;
        sum2 = s.sum2;
        sumw = s.sumw;
        sumw2 = s.sumw2;
    }

    /**
     * @param dis бинарный поток, из которого следует прочитать статистику.
     * @throws IOException если произошла ошибка чтения из потока.
     */
    public WStatisticExp(final DataInputStream dis) throws IOException {
        read(dis);
    }

    @Override
    public WStatisticExp getCopy() {
        return new WStatisticExp(this);
    }

    @Override
    public void set(final StatisticBase s) throws ClassCastException {
        final WStatisticExp ss = (WStatisticExp) s;
        q = ss.q;
        num = ss.num;
        sum = ss.sum;
        sum2 = ss.sum2;
        sumw = ss.sum;
        sumw2 = ss.sum2;
    }

    @Override
    public void clear() {
        num = 0;
        sum = 0;
        sum2 = 0;
        sumw = 0;
        sumw2 = 0;
    }

    @Override
    public void add(final double x) {
        add(x, 1.0);
    }

    /**
     * Добавить выборочное значение с указанным весом.
     *
     * @param x выборочное значение.
     * @param w вес.
     */
    public void add(final double x, final double w) {
        num++;
        sum = w * x + q * sum;
        sum2 = w * x * x + q * sum2;
        sumw = w + q * sumw;
        sumw2 = w * w + q * sumw2;
    }

    @Override
    public void add(final StatisticBase s) throws ClassCastException {
        final WStatisticExp ss = (WStatisticExp) s;
        num += ss.num;
        sum += ss.sum;
        sum2 += ss.sum2;
        sumw += ss.sumw;
        sumw2 += ss.sumw2;
    }

    @Override
    public void shift(final double a) {
        sum2 += a * (a * sumw + 2 * sum);
        sum += a * sumw;
    }

    @Override
    public int getNum() {
        return num;
    }

    /**
     * @return взвешенная сумма элементов статистики.
     */
    public double getSum() {
        return sum;
    }

    /**
     * @return взвешенная сумма квадратов элементов статистики.
     */
    public double getSum2() {
        return sum2;
    }

    /**
     * @return сумма весов.
     */
    public double getSumW() {
        return sumw;
    }

    /**
     * @return общий вес накопленных значений.
     */
    public double getWeight() {
        return sumw;
    }

    @Override
    public double getEv() {
        return sumw > 0 ? sum / sumw : 0;
    }

    @Override
    public double getVar() {
        return sumw > 0 ? sumw * (sum2 - sum * sum / sumw) / (sumw * sumw - sumw2) : 0;
    }

    @Override
    public double getCi(final double u) {
        return sumw > 0 ? u * Math.sqrt(sumw2 * getVar()) / sumw : 0;
    }

    @Override
    public final void read(final DataInputStream dis) throws IOException {
        q = dis.readDouble();
        num = dis.readInt();
        sum = dis.readDouble();
        sum2 = dis.readDouble();
        sumw = dis.readDouble();
        sumw2 = dis.readDouble();
    }

    @Override
    public void write(final DataOutputStream dos) throws IOException {
        dos.writeDouble(q);
        dos.writeInt(num);
        dos.writeDouble(sum);
        dos.writeDouble(sum2);
        dos.writeDouble(sumw);
        dos.writeDouble(sumw2);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "q = %g, n = %d, sum = %g, sum2 = %g, sumw = %g, sumw2 = %g, Ev = %g+-%g (95%%)",
                q, num, sum, sum2, sumw, sumw2, getEv(), getCi(1.96));
    }
}
