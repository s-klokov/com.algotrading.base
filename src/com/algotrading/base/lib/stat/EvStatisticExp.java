package com.algotrading.base.lib.stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Класс для оценки математического ожидания по выборке с использованием
 * экспоненциального сглаживания.
 */
public class EvStatisticExp implements StatisticBase {

    protected double q;
    protected double qn = 1;
    protected int num = 0;
    protected double sum = 0;

    /**
     * @param q скорость забывания прошлого - число от 0 до 1
     *          Чем ближе q к 1, тем медленнее забывается прошлое.<br>
     *          Назовём периодом полураспада статистики такой номер N,
     *          что элемент выборки номер N учитывается в общей сумме с
     *          коэффициентом 0.5. Тогда справедлива формула
     *          q = 0.5^(1/(N-1)).
     */
    public EvStatisticExp(final double q) {
        this.q = q;
    }

    /**
     * Конструктор, загружающий статистику из двоичного потока.
     *
     * @param q   скорость забывания прошлого - число от 0 до 1
     *            Чем ближе q к 1, тем медленнее забывается прошлое.<br>
     *            Назовём периодом полураспада статистики такой номер N,
     *            что элемент выборки номер N учитывается в общей сумме с
     *            коэффициентом 0.5. Тогда справедлива формула
     *            q = 0.5^(1/(N-1)).
     * @param dis поток.
     * @throws IOException если произошла ошибка чтения.
     */
    public EvStatisticExp(final double q, final DataInputStream dis) throws IOException {
        this.q = q;
        loadPrivate(dis);
    }

    /**
     * Конструктор копирования.
     *
     * @param s статистика.
     */
    public EvStatisticExp(final EvStatisticExp s) {
        q = s.q;
        qn = s.qn;
        num = s.num;
        sum = s.sum;
    }

    @Override
    public EvStatisticExp getCopy() {
        return new EvStatisticExp(this);
    }

    @Override
    public void set(final StatisticBase s) throws ClassCastException {
        final EvStatisticExp es = (EvStatisticExp) s;
        q = es.q;
        qn = es.qn;
        num = es.num;
        sum = es.sum;
    }

    /**
     * Установить данные значения МО и числа наблюдений.
     *
     * @param ev  МО.
     * @param num число наблюдений.
     */
    public void set(final double ev, final int num) {
        this.num = num;
        qn = Math.pow(q, num);
        sum = (1 - qn) * ev / (1 - q);
    }

    /**
     * Добавить данную статистику к этой статистике.
     * При этом считается, что эта статистика предшествовала статистике {@code s}.
     * <p/>
     * ВНИМАНИЕ! Этот метод приближённый и страдает ошибками округления.
     * Вычисления тем точнее, чем объёмнее обе статистики.
     *
     * @param s статистика.
     * @throws ClassCastException если указанная статистика, которая не совместима с текущей статистикой.
     */
    @Override
    public void add(final StatisticBase s) throws ClassCastException {
        final EvStatisticExp ss = (EvStatisticExp) s;
        if (q == ss.q) {
            sum = ss.sum + ss.qn * sum;
            num = ss.num + num;
            qn *= ss.qn;
        } else {
            sum = ss.sum + ss.qn * (1.0 - q) / (1.0 - ss.q) * sum;
            num = ss.num + (int) ((num * Math.log(q)) / Math.log(ss.q));
            q = ss.q;
            qn *= ss.qn;
        }
    }

    /**
     * Смешать статистику {@code s} с этой статистикой.
     * Этот метод может работать лишь со статистиками, у которых одинаковый параметр {@link #q}.
     * В результате смешения оценка мат. ожидания новой статистики будет равна взвешенной сумме
     * оценок мат. ожиданий статистик до смешения, а количество наблюдений будет равно сумме наблюдений
     * смешиваемых статистик.
     *
     * @param w  вес этой статистики в смеси.
     * @param s  статистика, которую подмешивают.
     * @param sw вес статистики {@code s}.
     */
    public void merge(final double w, final EvStatisticExp s, final double sw) {
        if (num == 0) {
            qn = s.qn;
            num = s.num;
            sum = s.sum;
        } else if (s.num > 0) {
            sum = (w * sum / (1 - qn) + sw * s.sum / (1 - s.qn)) / (w + sw);
            qn *= s.qn;
            sum *= (1 - qn);
            num += s.num;
        }
    }

    /**
     * Добавить указанную статистику в конец к текущей статистике.
     * <p/>
     * Этот метод точный, но может работать лишь со статистиками, у которых одинаковый параметр {@link #q}.
     *
     * @param s статистика.
     */
    public void update(final EvStatisticExp s) {
        num += s.num;
        qn *= s.qn;
        sum = s.qn * sum + s.sum;
    }

    @Override
    public void clear() {
        qn = 1;
        num = 0;
        sum = 0;
    }

    @Override
    public void add(final double x) {
        num++;
        qn *= q;
        sum = q * sum + x;
    }

    @Override
    public void shift(final double a) {
        sum += a * (1 - qn) / (1 - q);
    }

    @Override
    public final int getNum() {
        return num;
    }

    public final double getQn() {
        return qn;
    }

    @Override
    public final double getEv() {
        return (num > 0) ? (1 - q) * sum / (1 - qn) : 0;
    }

    @Override
    public double getVar() {
        return 0;
    }

    @Override
    public double getCi(final double u) {
        return u * Math.sqrt(getVar() / getNum());
    }

    @Override
    public void read(final DataInputStream dis) throws IOException {
        loadPrivate(dis);
    }

    @Override
    public void write(final DataOutputStream dos) throws IOException {
        dos.writeInt(num);
        dos.writeDouble(sum);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "q = %.4f, n = %d, sum = %.4f, Ev = %.4f", q, num, sum, getEv());
    }

    //------------------------------------------------------------------------------------------------------------------

    private void loadPrivate(final DataInputStream dis) throws IOException {
        num = dis.readInt();
        sum = dis.readDouble();
        qn = Math.pow(q, num);
    }

    public void scale(final double multiplier) {
        sum *= multiplier;
    }
}
