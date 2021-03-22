package com.algotrading.base.lib.nnets;

import java.util.Locale;

/**
 * Обучатель нейросети методом градиентного спуска с параболическим выбором шага.
 * Для определения коэффициентов параболы используется значение функции ошибки в точке (w - 0.01 * grad).
 */
public class ParabolicNNetTrainer extends NNetTrainer {

    /**
     * Шаг по градиенту, в котором вычисляется пробное значение штрафной функции для оценки коэффициентов параболы.
     */
    private static final double H = 0.0001;

    /**
     * Момент инерции: новое направление выбирается по формуле p_{k+1} = (1 - mu) * grad_k + mu * p_k.
     */
    public double mu = 0.1;
    /**
     * Длина шага для случая, когда вершина параболы расположена по градиенту.
     */
    private double h = 1.0;
    /**
     * Направление шага.
     */
    protected double[] p = null;

    /**
     * Конструктор.
     */
    public ParabolicNNetTrainer() {
        super(700);
    }

    /**
     * Конструктор.
     *
     * @param numCycles число итераций обучения.
     */
    public ParabolicNNetTrainer(final int numCycles) {
        super(numCycles);
    }

    @Override
    protected void initialize(final int n) {
        h = 1.0;
        if (p == null || p.length < n) {
            p = new double[n];
        } else {
            for (int i = 0; i < n; i++) p[i] = 0.0;
        }
    }

    @Override
    protected double step(final int n, final double g2) {
        final double[] g = getGradient();
        double p2 = 0.0;
        double gp = 0.0;
        for (int i = 0; i < n; i++) {
            final double pi = mu * p[i] - (1 - mu) * g[i];
            p[i] = pi;
            p2 += pi * pi;
            gp += g[i] * pi;
        }
        return stepByParabolic(n, p2, gp) * Math.sqrt(p2 / n);
    }

    /**
     * Сделать один шаг методом парабол вдоль направления {@link #p}.
     *
     * @param n  количество весов.
     * @param p2 скалярное произведение p * p.
     * @param gp скалярное произведение grad * p.
     * @return длина шага в евклидовой метрике.
     */
    protected double stepByParabolic(final int n, final double p2, final double gp) {
        final double e0 = getTrainError();
        final double x = 1.0 / Math.sqrt(p2);
        final double h1 = x * H;
        addWeights(h1, p);
        final double e1 = getTrainError();
        final double a = (e1 - e0 - gp * h1) / (h1 * h1);
        final double h0 = a == 0.0 ? 1.0 : -0.5 * gp / a;
        if (h0 <= 0.0 || h0 > x) {
            // вершина параболы расположена по росту градиента либо она слишком далеко от текущей точки.
            // шагаем на максимальный шаг против градиента.
            addWeights(h * x - h1, p);
            h *= 1.5;
            return h * x;
        } else {
            // вершина параболы расположена против роста градиента
            // шагаем в вершину параболы
            addWeights(h0 - h1, p);
            h = Math.max(0.5 * h, 1.0);
            return h0;
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "ParabolicNNetTrainer: defaultCycles = %d, mu = %.4f", maxCycles, mu);
    }
}
