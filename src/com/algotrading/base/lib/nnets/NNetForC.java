package com.algotrading.base.lib.nnets;

import com.algotrading.base.lib.DataFormatException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Класс, реализующий трёхслойную (с одним внутренним слоем) нейросеть.
 * Упрощённый вариант для портирования на Си.
 */
public class NNetForC implements Serializable {

    /**
     * Число входов.
     */
    private int n0;
    /**
     * Число нейронов на внутреннем слое.
     */
    private int n1;
    /**
     * Число выходов (число нейронов на внешнем слое).
     */
    private int n2;
    /**
     * Выходы из нейронов внутреннего слоя.
     */
    private double[] y1;
    /**
     * Выходы из нейронов внешнего слоя.
     */
    private double[] y2;
    /**
     * Массив весов.<br>
     * Вес связи между j-м нейроном внутреннего слоя и k-м нейроном входного слоя имеет индекс j * (n0 + 1) + k.<br>
     * Вес связи между i-м нейроном выходного слоя и j-м нейроном внутреннего слоя имеет индекс (n0 + 1) * n1 + i * (n1 + 1) + j.
     */
    private double[] w;
    /**
     * {@code true}, если в выходам нейросети применяется сигмоид.
     */
    private boolean hasSigmoidOutput;

    /**
     * Конструктор сети по конфигурации.
     *
     * @param n0               число входов.
     * @param n1               число нейронов на внутреннем слое.
     * @param n2               выходов.
     * @param hasSigmoidOutput {@code true}, если в выходам нейросети применяется сигмоид.
     */
    public NNetForC(final int n0, final int n1, final int n2, final boolean hasSigmoidOutput) {
        this.n0 = n0;
        this.n1 = n1;
        this.n2 = n2;
        allocateMemory();
        this.hasSigmoidOutput = hasSigmoidOutput;
    }

    /**
     * Конструктор сети по конфигурации.
     * Слои нейросети нумеруются от 0 (входы) до n.length-1 (выходы)
     * Например, сеть с 10 входами, 4 внутренними нейронами и 2 выходами
     * будет задаваться массивом {10, 4, 2}.
     *
     * @param n                массив, задающий конфигурацию нейросети.
     * @param hasSigmoidOutput {@code true}, если в выходам нейросети применяется сигмоид.
     */
    public NNetForC(final int[] n, final boolean hasSigmoidOutput) {
        this(n[0], n[1], n[2], hasSigmoidOutput);
    }

    /**
     * Конструктор, читающий сеть из двоичного потока.
     *
     * @param dis              поток, откуда будет прочитана структура нейросети и веса;
     *                         (см. формат в методе {@link NNetForC#write}).
     * @param hasSigmoidOutput {@code true}, если в выходам нейросети применяется сигмоид.
     * @throws IOException если не удалось прочитать нейросеть из потока.
     */
    public NNetForC(final DataInputStream dis, final boolean hasSigmoidOutput) throws IOException {
        final int numLayers = dis.readInt();
        if (numLayers != 3) throw new DataFormatException("There must be 3 layers.");
        n0 = dis.readInt();
        n1 = dis.readInt();
        n2 = dis.readInt();
        allocateMemory();
        for (int i = 0; i < w.length; i++) {
            final double wi = dis.readDouble();
            if (Double.isNaN(wi)) throw new DataFormatException("NaN weight.");
            w[i] = wi;
        }
        this.hasSigmoidOutput = hasSigmoidOutput;
    }

    /**
     * Загрузить нейросеть из двоичного потока.
     *
     * @param dis поток, откуда будет прочитана структура нейросети и веса;
     *            см. формат в методе {@link NNetForC#write(DataOutputStream)}.
     * @throws IOException если не удалось прочитать нейросеть из потока.
     */
    public void read(final DataInputStream dis) throws IOException {
        final int numLayers = dis.readInt();
        if (numLayers != 3) throw new DataFormatException("There must be 3 layers.");
        setConfigurationTo(dis.readInt(), dis.readInt(), dis.readInt());
        for (int i = 0; i < w.length; i++) {
            final double wi = dis.readDouble();
            if (Double.isNaN(wi)) throw new DataFormatException("NaN weight.");
            w[i] = wi;
        }
    }

    /**
     * Запись конфигурацию и веса нейросети в двоичный поток.
     *
     * @param dos поток.
     * @throws IOException если не удалось записать нейросеть в поток.
     */
    public void write(final DataOutputStream dos) throws IOException {
        dos.writeInt(3);
        dos.writeInt(n0);
        dos.writeInt(n1);
        dos.writeInt(n2);
        for (final double wi : w) dos.writeDouble(wi);
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
     * @return число внутренних нейронов.
     */
    public final int getNumInnerNeurons() {
        return n1;
    }

    /**
     * @return общее количество весов.
     */
    public final int getNumWeights() {
        return (n0 + 1) * n1 + (n1 + 1) * n2;
    }

    /**
     * @return {@code true}, если есть сигмоид на внешнем слое.
     */
    public boolean hasSigmoidOutput() {
        return hasSigmoidOutput;
    }

    /**
     * Изменить конфигурацию сети.
     *
     * @param n0 число входов.
     * @param n1 число нейронов на внутреннем слое.
     * @param n2 выходов.
     */
    public void setConfigurationTo(final int n0, final int n1, final int n2) {
        if (this.n0 != n0 || this.n1 != n1 || this.n2 != n2) {
            this.n0 = n0;
            this.n1 = n1;
            this.n2 = n2;
            allocateMemory();
        }
    }

    /**
     * Сделать сеть вырожденной для предсказания постоянных выходов.
     *
     * @param out    массив, в котором записаны требуемые постоянные выходы.
     * @param offset смещение в данном массиве, начиная с которого записаны требуемые постоянные выходы.
     */
    public void setSingular(final double[] out, final int offset) {
        final int dn1 = n1 + 1;
        if (hasSigmoidOutput) {
            for (int i = 0, si = dn1 * n1; i < n2; i++, si += dn1) {
                for (int j = 0; j < n1; j++) w[si + j] = 0.0;
                w[si + n1] = fInverse(out[offset + i]);
            }
        } else {
            for (int i = 0, si = dn1 * n1; i < n2; i++, si += dn1) {
                for (int j = 0; j < n1; j++) w[si + j] = 0.0;
                w[si + n1] = out[offset + i];
            }
        }
    }

    /**
     * Сигмоид.
     *
     * @param x аргумент.
     * @return значение сигмоида.
     */
    private static double f(final double x) {
        return (x >= 0) ? -0.5 + 1.0 / (1.0 + StrictMath.exp(-x)) : 0.5 - 1.0 / (1.0 + StrictMath.exp(x));
    }

    /**
     * Функция, обратная к f.
     *
     * @param y значение сигмоида.
     * @return аргумент сигмоида, дающий указанное значение сигмоида.
     */
    private double fInverse(final double y) {
        if (y < -0.499999) {
            return -Double.MAX_VALUE;
        } else if (y < 0.0) {
            return Math.log(1.0 / (0.5 - y) - 1.0);
        } else if (y <= 0.499999) {
            return -Math.log(1.0 / (0.5 + y) - 1.0);
        } else {
            return Double.MAX_VALUE;
        }
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
            y1[j] = f(s + w[wi++]);
        }
        if (hasSigmoidOutput) {
            for (int i = 0; i < n2; i++) {
                double s = 0;
                for (int j = 0; j < n1; j++) {
                    s += w[wi++] * y1[j];
                }
                y2[i] = f(s + w[wi++]);
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

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Выделить память под массивы.
     */
    private void allocateMemory() {
        y1 = new double[n1 + 1];
        y1[n1] = 1.0;
        y2 = new double[n2];
        w = new double[getNumWeights()];
    }
}