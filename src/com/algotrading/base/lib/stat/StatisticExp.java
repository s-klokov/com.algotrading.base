package com.algotrading.base.lib.stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Базовый класс для статистической обработки с помощью экспоненциального сглаживания.
 */
public final class StatisticExp extends EvStatisticExp {

    private double sum2 = 0;

    /**
     * @param q скорость забывания прошлого - число от 0 до 1
     *          Чем ближе q к 1, тем медленнее забывается прошлое.<br>
     *          Назовём периодом полураспада статистики такой номер N,
     *          что элемент выборки номер N учитывается в общей сумме с
     *          коэффициентом 0.5. Тогда справедлива формула
     *          q = 0.5^(1/(N-1)).
     */
    public StatisticExp(final double q) {
        super(q);
    }

    /**
     * Конструктор копирования.
     *
     * @param s статистика.
     */
    public StatisticExp(final StatisticExp s) {
        super(s);
        sum2 = s.sum2;
    }

    @Override
    public void add(final StatisticBase s) throws ClassCastException {
        final StatisticExp ss = (StatisticExp) s;
        sum2 = ss.sum2 + ss.qn * (1 - q) / (1 - ss.q) * sum2;
        super.add(s);
    }

    @Override
    public StatisticExp getCopy() {
        return new StatisticExp(this);
    }

    @Override
    public void set(final StatisticBase s) throws ClassCastException {
        final StatisticExp es = (StatisticExp) s;
        super.set(s);
        sum2 = es.sum2;
    }

    @Override
    public void clear() {
        super.clear();
        sum2 = 0;
    }

    @Override
    public void add(final double x) {
        super.add(x);
        sum2 = q * sum2 + x * x;
    }

    @Override
    public void shift(final double a) {
        final double sumq = (1 - qn) / (1 - q);
        sum2 += a * (a * sumq + 2 * sum);
        sum += a * sumq;
    }

    @Override
    public double getVar() {
        return (num > 0) ? 0.5 * (1 - q * q) * (sum2 - (1 - q) * sum * sum / (1 - qn)) / (q - qn) : 0;
    }

    @Override
    public void read(final DataInputStream dis) throws IOException {
        super.read(dis);
        sum2 = dis.readDouble();
    }

    @Override
    public void write(final DataOutputStream dos) throws IOException {
        super.write(dos);
        dos.writeDouble(sum2);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "q = %.4f, n = %d, sum = %.4f, sum2 = %.4f, Ev = %.4f+-%.4f (95%%)",
                q, num, sum, sum2, getEv(), getCi(1.96));
    }
}
