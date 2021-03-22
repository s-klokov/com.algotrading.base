package com.algotrading.base.lib.nnets;

import java.util.Locale;

/**
 * Обучатель нейросети методом обратного распространения ошибки (back propagate).
 */
public class BpNNetTrainer extends NNetTrainer {

    /**
     * Скорость забывания статистики.
     * По умолчанию скорость забывания выставляется так, что период полураспада равен 1000.
     */
    public double q = Math.pow(0.5, 1.0 / 1000.0);

    /**
     * Потоковый обучатель.
     */
    private BpOnlineNNetTrainer onlineTrainer = null;

    /**
     * Конструктор.
     */
    public BpNNetTrainer() {
        super(200);
    }

    @Override
    protected void initialize(final int n) {
        onlineTrainer = new BpOnlineNNetTrainer(net, q);
    }

    @Override
    protected double step(final int n, final double g2) {
        for (int i = 0; i < data.getTrainSize(); i++) {
            onlineTrainer.addSample(data.getTrainSample(i));
            if (onlineTrainer.getNumSamples() >= 1000) {
                onlineTrainer.train();
            }
        }
        resetCaches();
        return 1.0;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "BpNNetTrainer: defaultCycles = %d, q = %g", maxCycles, q);
    }
}
