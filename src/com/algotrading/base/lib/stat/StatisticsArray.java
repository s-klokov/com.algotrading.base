package com.algotrading.base.lib.stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

/**
 * Класс для оценки математического ожидания вектора значений по выборке с использованием
 * экспоненциального сглаживания.
 */
public class StatisticsArray {

    /**
     * Массив объемов статистики для каждой компоненты вектора.
     */
    protected final int[] num;
    /**
     * Массив накопленных математических ожиданий для каждой компоненты вектора.
     */
    protected final double[] ev;

    /**
     * @param size длина вектора статистик.
     */
    public StatisticsArray(final int size) {
        num = new int[size];
        ev = new double[size];
    }

    /**
     * Конструктор копирования.
     *
     * @param s статистика.
     */
    public StatisticsArray(final StatisticsArray s) {
        num = new int[s.num.length];
        ev = new double[num.length];
        for (int i = num.length - 1; i >= 0; i--) {
            num[i] = s.num[i];
            ev[i] = s.ev[i];
        }
    }

    /**
     * Смешать статистику {@code s} с этой статистикой.
     * <p/>
     * В результате смешения оценка мат. ожидания новой статистики будет равна взвешенной сумме
     * оценок мат. ожиданий статистик до смешения, а количество наблюдений будет равно сумме наблюдений
     * смешиваемых статистик.
     *
     * @param w  вес этой статистики в смеси.
     * @param s  статистика, которую подмешивают.
     * @param sw вес статистики {@code s}.
     */
    public void merge(final double w, final StatisticsArray s, final double sw) {
        for (int i = num.length - 1; i >= 0; i--) {
            merge(i, w, s, sw);
        }
    }

    /**
     * Аналог метода {@link #merge(double, StatisticsArray, double)}, но только для указанной координаты.
     *
     * @param i  номер координаты.
     * @param w  вес этой статистики в смеси.
     * @param s  статистика, которую подмешивают.
     * @param sw вес статистики {@code s}.
     */
    public void merge(final int i, final double w, final StatisticsArray s, final double sw) {
        num[i] += s.num[i];
        if (sw <= 0) return;
        if (w <= 0) {
            ev[i] = s.ev[i];
        } else if (s.num[i] > 0) {
            ev[i] = (w * ev[i] + sw * s.ev[i]) / (w + sw);
        }
    }

    /**
     * Скопировать все статистики массива {@code s} в этот массив статистик.
     *
     * @param s статистика, которую подмешивают.
     */
    public void set(final StatisticsArray s) {
        for (int i = num.length - 1; i >= 0; i--) {
            set(i, s);
        }
    }

    /**
     * Скопировать указанную статистику из массива {@code s} в данный массив статистик на соответствующее место.
     *
     * @param i номер статистики.
     * @param s статистика, откуда берутся исходные данные.
     */
    public void set(final int i, final StatisticsArray s) {
        num[i] = s.num[i];
        ev[i] = s.ev[i];
    }

    /**
     * Очистить все статистики.
     */
    public void clear() {
        for (int i = num.length - 1; i >= 0; i--) {
            num[i] = 0;
            ev[i] = 0;
        }
    }

    /**
     * Добавить наблюдение к данной статистике.
     *
     * @param i номер статистики.
     * @param x наблюдение.
     * @param q скорость забывания прошлого.<br>
     *          Скорость забывания - это число из интервала (0; 1).
     *          Чем ближе q к 1, тем медленнее забывается прошлое.<br>
     *          Назовём периодом полураспада статистики такой номер N,
     *          что элемент выборки номер N учитывается в общей сумме с
     *          коэффициентом 0.5. Тогда справедлива формула
     *          q = 0.5^(1/(N-1)).
     */
    public void add(final int i, final double x, final double q) {
        if (num[i] == 0) {
            num[i] = 1;
            ev[i] = x;
        } else {
            final int n = ++num[i];
            ev[i] += (x - ev[i]) * (1.0 - q) / (1.0 - StrictMath.pow(q, n));
        }
    }

    /**
     * @return количество элементов в массиве статистик.
     */
    public int length() {
        return num.length;
    }

    /**
     * @param i номер статистики.
     * @return число наблюдений для данной статистики.
     */
    public final int getNum(final int i) {
        return num[i];
    }

    /**
     * Установить число наблюдений для данной статистики.
     *
     * @param i номер статистики.
     * @param n число наблюдений.
     */
    public final void setNum(final int i, final int n) {
        num[i] = n;
    }

    /**
     * @param i номер статистики.
     * @return оценка математического ожидания для статистики с данным номером.
     */
    public final double getEv(final int i) {
        return ev[i];
    }

    /**
     * Прочитать массив статистик из потока.
     *
     * @param dis поток.
     * @throws IOException если произошла ошибка ввода-вывода.
     */
    public void read(final DataInputStream dis) throws IOException {
        for (int i = 0; i < num.length; i++) {
            num[i] = dis.readInt();
            ev[i] = dis.readDouble();
        }
    }

    /**
     * Записать массив статистик в поток.
     *
     * @param dos поток.
     * @throws IOException если произошла ошибка ввода-вывода.
     */
    public void write(final DataOutputStream dos) throws IOException {
        for (int i = 0; i < num.length; i++) {
            dos.writeInt(num[i]);
            dos.writeDouble(ev[i]);
        }
    }

    @Override
    public String toString() {
        try (final Formatter f = new Formatter(Locale.US)) {
            for (int i = 0; i < num.length; i++) {
                f.format("; %d: n = %d, ev = %.4f", i, num[i], ev[i]);
            }
            return f.toString().substring(2);
        }
    }
}
