package com.algotrading.base.lib.nnets;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Класс, реализующий трёхслойную (с одним внутренним слоем) нейросеть.
 * <p/>
 * Объекты этого класса являются неизменяемыми.
 */
public class ImmutableNNet3L {

    /**
     * Число входов.
     */
    private final int n0;
    /**
     * Число нейронов на внутреннем слое.
     */
    private final int n1;
    /**
     * Число выходов (число нейронов на внешнем слое).
     */
    private final int n2;
    /**
     * Выходы из нейронов внутреннего слоя.
     */
    private final double[] y1;
    /**
     * Выходы из нейронов внешнего слоя.
     */
    private final double[] y2;
    /**
     * Массив весов.<br>
     * Вес связи между j-м нейроном внутреннего слоя и k-м нейроном входного слоя имеет индекс j * (n0 + 1) + k.<br>
     * Вес связи между i-м нейроном выходного слоя и j-м нейроном внутреннего слоя имеет индекс (n0 + 1) * n1 + i * (n1 + 1) + j.
     */
    private final double[] w;
    /**
     * {@code true}, если в выходам нейросети применяется сигмоид.
     */
    private final boolean hasSigmoidOutput;

    /**
     * Конструктор, читающий сеть из двоичного потока.
     *
     * @param dis              поток, откуда будет прочитана структура нейросети и веса;
     *                         (см. формат в методе {@link NNet3L#write}).
     * @param hasSigmoidOutput {@code true}, если в выходам нейросети применяется сигмоид.
     * @throws IOException если не удалось прочитать нейросеть из потока.
     */
    public ImmutableNNet3L(final DataInputStream dis, final boolean hasSigmoidOutput) throws IOException {
        final int numLayers = dis.readInt();
        if (numLayers != 3) throw new DataFormatException("There must be 3 layers.");
        n0 = dis.readInt();
        n1 = dis.readInt();
        n2 = dis.readInt();
        y1 = new double[n1 + 1];
        y1[n1] = 1.0;
        y2 = new double[n2];
        w = new double[getNumWeights()];
        for (int i = 0; i < w.length; i++) {
            final double wi = dis.readDouble();
            if (Double.isNaN(wi)) throw new DataFormatException("NaN weight.");
            w[i] = wi;
        }
        this.hasSigmoidOutput = hasSigmoidOutput;
    }

    /**
     * Конструктор копирования.
     *
     * @param net нейросеть.
     */
    public ImmutableNNet3L(final ImmutableNNet3L net) {
        n0 = net.n0;
        n1 = net.n1;
        n2 = net.n2;
        y1 = new double[n1 + 1];
        y1[n1] = 1.0;
        y2 = new double[n2];
        w = net.w;
        hasSigmoidOutput = net.hasSigmoidOutput;
    }

    /**
     * Создать копию.
     *
     * @return копия этого объекта.
     */
    public ImmutableNNet3L getCopy() {
        return new ImmutableNNet3L(this);
    }

    /**
     * @return число входов.
     */
    public final int getNumInputs() {
        return n0;
    }

    /**
     * @return число выходов.
     */
    public final int getNumOutputs() {
        return n2;
    }

    /**
     * @return общее количество весов.
     */
    public final int getNumWeights() {
        return (n0 + 1) * n1 + (n1 + 1) * n2;
    }

    /**
     * По входному массиву данных получить массив значений на выходах нейросети.
     *
     * @param in массив входных данных длины не меньше, чем {@link #getNumInputs()}.
     * @return массив длины {@link #getNumOutputs()} с предсказанными значениями.
     */
    public double[] propagate(final double[] in) {
        int wi = 0;
        for (int j = 0; j < n1; j++) {
            double s = 0;
            for (int k = 0; k < n0; k++) {
                s += w[wi++] * in[k];
            }
            y1[j] = SigmoidFunction.f(s + w[wi++]);
        }
        if (hasSigmoidOutput) {
            for (int i = 0; i < n2; i++) {
                double s = 0;
                for (int j = 0; j < n1; j++) {
                    s += w[wi++] * y1[j];
                }
                y2[i] = SigmoidFunction.f(s + w[wi++]);
            }
        } else {
            for (int i = 0; i < n2; i++) {
                double s = 0;
                for (int j = 0; j < n1; j++) {
                    s += w[wi++] * y1[j];
                }
                y2[i] = s + w[wi++];
            }
        }
        return y2;
    }

    @Override
    public String toString() {
        return String.format("n0 = %d, n1 = %d, n2 = %d, nw = %d", n0, n1, n2, getNumWeights());
    }
}