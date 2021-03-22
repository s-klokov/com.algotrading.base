package com.algotrading.base.lib.nnets;

import com.algotrading.base.lib.stat.WStatisticExp;

/**
 * Онлайновый обучатель нейросети методом back propagate.
 */
public class BpOnlineNNetTrainer {

    /**
     * Обучаемая нейросеть.
     */
    public final NNet3L net;
    /**
     * Скорость забывания статистики.
     */
    private final double q;
    /**
     * Накопленный градиент штрафной функции.
     */
    private final double[] g;
    /**
     * Ошибка.
     */
    private double e = 0.0;
    /**
     * Количество обучающих образов, добавленное с момента последней тренировки.
     */
    private int numSamples = 0;
    /**
     * Статистика по каждому из выходов.
     */
    private final WStatisticExp[] outStats;

    /**
     * Конструктор.
     *
     * @param net обучаемая нейросеть.
     * @param q   скорость забывания статистики.
     */
    public BpOnlineNNetTrainer(final NNet3L net, final double q) {
        this.net = net;
        this.q = q;
        final int n = net.getNumWeights();
        g = new double[n];
        outStats = new WStatisticExp[net.getNumOutputs()];
        for (int i = 0; i < outStats.length; i++) {
            outStats[i] = new WStatisticExp(q);
        }
    }

    /**
     * Добавить обучающий образ.
     *
     * @param sample ненормализованный обучающий образ: массив длины не менее (nI + nO), первых nI элементов которого -
     *               это входы нейросети, следующие nO элементов - это выходы нейросети, и, наконец,
     *               элемент с индексом nI + nO (если он есть) - это вес данного обучающего образа.
     *               Здесь nI = {@link NNet3L#getNumInputs()}, nO = {@link NNet3L#getNumOutputs()}.
     */
    public void addSample(final double[] sample) {
        for (int i = 0; i < g.length; i++) g[i] *= q;
        net.computeGradient(sample, g);
        final int nI = net.getNumInputs();
        final int nO = net.getNumOutputs();
        final double v = sample[nI + nO];
        e = v * net.computeError(sample) + q * e;
        for (int i = 0; i < nO; i++) {
            outStats[i].add(sample[nI + i], v);
        }
        numSamples++;
    }

    /**
     * @return обучающих образов, добавленное с момента последней тренировки.
     */
    public int getNumSamples() {
        return numSamples;
    }

    /**
     * Дообучить нейросеть.
     */
    public void train() {
        numSamples = 0;
        double var = 0.0;
        for (final WStatisticExp outStat : outStats) {
            final double sumw = outStat.getSumW();
            if (sumw > 0.0) {
                final double sum = outStat.getSum();
                var += outStat.getSum2() + sum * sum / sumw;
            }
        }
        double h;
        if (net.hasSigmoidOutput()) {
            h = 4.0 / var;
        } else {
            h = 0.05 / var;
        }
        double g2 = 0;
        for (int i = net.getNumWeights() - 1; i >= 0; i--) g2 += g[i] * g[i];
        if (g2 > 0.0) {
            final double hmax = 0.1 * e / g2;
            if (h > hmax) h = hmax;
        }
        net.addWeights(-h, g);
    }
}