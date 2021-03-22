package com.algotrading.base.lib.nnets;

import java.util.Locale;

/**
 * Итеративный обучатель нейросети методом сопряжённых градиентов.
 */
public class ConjugateGradientNNetTrainer extends GoldenSectNNetTrainer {

    /**
     * Квадрат градиента на предыдущем шаге.
     */
    private double g2prev = 0.0;
    /**
     * Градиент на предыдущем шаге.
     */
    private double[] gprev = null;
    /**
     * Количество найденных сопряжённых векторов.
     */
    private int numCG = 0;

    /**
     * Конструктор.
     */
    public ConjugateGradientNNetTrainer() {
        super(600);
    }

    @Override
    protected void initialize(final int n) {
        super.initialize(n);
        g2prev = 0.0;
        if (gprev == null || gprev.length < n) {
            gprev = new double[n];
        }
        numCG = 0;
    }

    @Override
    protected double step(final int n, final double g2) {
        final double[] g = getGradient();
        double beta;
        if (g2prev == 0.0 || numCG % n == 0) {
            beta = 0.0;
            numCG = 0;
        } else {
            double ggprev = 0.0;
            for (int i = 0; i < n; i++) ggprev += g[i] * gprev[i];
            beta = (g2 - ggprev) / g2prev;
            if (beta <= 0) {
                beta = 0.0;
                numCG = 0;
            }
        }
        g2prev = g2;
        System.arraycopy(g, 0, gprev, 0, n);
        double p2 = 0.0;
        for (int i = 0; i < n; i++) {
            final double pi = -g[i] + beta * p[i];
            p[i] = pi;
            p2 += pi * pi;
        }
        numCG++;
        return stepByGoldenSect(p2) / Math.sqrt(n);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "ConjugateGradientNNetTrainer: defaultCycles = %d, mu = %.4f", maxCycles, mu);
    }
}
