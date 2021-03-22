package com.algotrading.base.lib.stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Обычная статистика.
 */
public final class EvStatistic implements StatisticBase {

    private int num;
    private double sum;

    /**
     * Конструктор, создающий пустую статистику.
     */
    public EvStatistic() {
        num = 0;
        sum = 0;
    }

    /**
     * Конструктор копирования.
     *
     * @param s статистика.
     */
    public EvStatistic(final EvStatistic s) {
        num = s.num;
        sum = s.sum;
    }

    /**
     * Конструктор, загружающий статистику из двоичного потока.
     *
     * @param dis поток.
     * @throws IOException если произошла ошибка чтения.
     */
    public EvStatistic(final DataInputStream dis) throws IOException {
        num = dis.readInt();
        sum = dis.readDouble();
    }

    @Override
    public EvStatistic getCopy() {
        return new EvStatistic(this);
    }

    @Override
    public void set(final StatisticBase s) throws ClassCastException {
        final EvStatistic ss = (EvStatistic) s;
        num = ss.num;
        sum = ss.sum;
    }

    public void set(final int num, final double sum) {
        this.num = num;
        this.sum = sum;
    }

    @Override
    public void clear() {
        num = 0;
        sum = 0;
    }

    @Override
    public void add(final double x) {
        num++;
        sum += x;
    }

    @Override
    public void add(final StatisticBase s) throws ClassCastException {
        final EvStatistic ss = (EvStatistic) s;
        num += ss.num;
        sum += ss.sum;
    }

    @Override
    public void shift(final double a) {
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
        return 0;
    }

    @Override
    public double getCi(final double u) {
        return 0;
    }

    @Override
    public void read(final DataInputStream dis) throws IOException {
        num = dis.readInt();
        sum = dis.readDouble();
    }

    @Override
    public void write(final DataOutputStream dos) throws IOException {
        dos.writeInt(num);
        dos.writeDouble(sum);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "n = %d, sum = %.4f, Ev = %.4f", num, sum, getEv());
    }
}