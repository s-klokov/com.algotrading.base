package com.algotrading.base.lib.stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Статистика, элементы которой имеют вес.
 */
public final class WStatistic implements StatisticBase {

    private int num;
    private double sum;
    private double sum2;
    private double sumw;
    private double sumw2;

    public WStatistic() {
        num = 0;
        sum = 0;
        sum2 = 0;
        sumw = 0;
        sumw2 = 0;
    }

    public WStatistic(final WStatistic s) {
        num = s.num;
        sum = s.sum;
        sum2 = s.sum2;
        sumw = s.sumw;
        sumw2 = s.sumw2;
    }

    public WStatistic(final DataInputStream dis) throws IOException {
        num = dis.readInt();
        sum = dis.readDouble();
        sum2 = dis.readDouble();
        sumw = dis.readDouble();
        sumw2 = dis.readDouble();
    }

    @Override
    public WStatistic getCopy() {
        return new WStatistic(this);
    }

    @Override
    public void set(final StatisticBase s) throws ClassCastException {
        final WStatistic ss = (WStatistic) s;
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
        sum += w * x;
        sum2 += w * x * x;
        sumw += w;
        sumw2 += w * w;
    }

    @Override
    public void add(final StatisticBase s) throws ClassCastException {
        final WStatistic ss = (WStatistic) s;
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
        num = dis.readInt();
        sum = dis.readDouble();
        sum2 = dis.readDouble();
        sumw = dis.readDouble();
        sumw2 = dis.readDouble();
    }

    @Override
    public void write(final DataOutputStream dos) throws IOException {
        dos.writeInt(num);
        dos.writeDouble(sum);
        dos.writeDouble(sum2);
        dos.writeDouble(sumw);
        dos.writeDouble(sumw2);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "n = %d, sum = %.4f, sum2 = %.4f, sumw = %.4f, sumw2 = %.4f, Ev = %.4f+-%.4f (95%%)",
                num, sum, sum2, sumw, sumw2, getEv(), getCi(1.96));
    }
}
