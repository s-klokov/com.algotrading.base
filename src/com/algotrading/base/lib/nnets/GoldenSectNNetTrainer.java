package com.algotrading.base.lib.nnets;

import java.util.Locale;

/**
 * Обучатель нейросети, в котором направление выбирается противоположно градиенту
 * и вдоль этого направления выполняется поиск минимума методом золотого сечения.
 */
public class GoldenSectNNetTrainer extends NNetTrainer {
    /**
     * Момент инерции: новое направление выбирается по формуле p_{k+1} = (1 - mu) * grad_k + mu * p_k.
     */
    public double mu = 0.2;
    /**
     * Направление шага.
     */
    protected double[] p = null;
    /**
     * Пробный шаг.
     */
    private double dh = 1.0;

    /**
     * Конструктор.
     */
    public GoldenSectNNetTrainer() {
        super(1000);
    }

    /**
     * Конструктор.
     *
     * @param numCycles число итераций обучения.
     */
    public GoldenSectNNetTrainer(final int numCycles) {
        super(numCycles);
    }

    @Override
    protected void initialize(final int n) {
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
        for (int i = 0; i < n; i++) {
            final double pi = mu * p[i] - (1 - mu) * g[i];
            p[i] = pi;
            p2 += pi * pi;
        }
        return stepByGoldenSect(p2) / Math.sqrt(n);
    }

    /**
     * Обратная пропорция золотого сечения.
     */
    private static final double PHI = 2.0 / (1.0 + Math.sqrt(5));

    /**
     * Сделать один шаг методом золотого сечения вдоль направления {@link #p}.
     *
     * @param p2 скалярное произведение p * p.
     * @return длина сделанного шага.
     */
    protected double stepByGoldenSect(final double p2) {
        final double m = 1.0 / Math.sqrt(p2);
        double h = 0.0; // точка, в которой сейчас вычислено значение функции
        double e1 = getTrainError();
        // сначала шагаем до тех пор, пока функция не начнёт возрастать.
        dh *= 0.5;
        while (true) {
            dh *= 2.0;
            h += dh;
            addWeights(m * dh, p);
            final double e2 = getTrainError();
            if (e2 > e1) break;
            e1 = e2;
        }
        // теперь применяем золотое сечение
        double a = h - dh;
        double b = h;
        double h1 = b - (b - a) * PHI;
        double h2 = a + (b - a) * PHI;
        dh *= 0.5;
        addWeights(m * (h1 - h), p);
        e1 = getTrainError();
        h = h1;
        addWeights(m * (h2 - h), p);
        double e2 = getTrainError();
        h = h2;

        while (Math.abs(h1 - h2) > 1e-5) {
            if (e1 <= e2) {
                b = h2;
                h2 = h1;
                h1 = b - (b - a) * PHI;
                e2 = e1;
                addWeights(m * (h1 - h), p);
                e1 = getTrainError();
                h = h1;
            } else {
                a = h1;
                h1 = h2;
                h2 = a + (b - a) * PHI;
                e1 = e2;
                addWeights(m * (h2 - h), p);
                e2 = getTrainError();
                h = h2;
            }
        }
        if (e1 < e2) {
            if (h != h1) {
                addWeights(m * (h1 - h), p);
                h = h1;
            }
        } else {
            if (h != h2) {
                addWeights(m * (h2 - h), p);
                h = h2;
            }
        }
        return h;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "GoldenSectNNetTrainer: defaultCycles = %d, mu = %.4f", maxCycles, mu);
    }
}
