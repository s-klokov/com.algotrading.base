package com.algotrading.base.lib.nnets;

import com.algotrading.base.lib.Rnd;

import java.io.*;
import java.util.Locale;

/**
 * Класс, реализующий трёхслойную (с одним внутренним слоем) нейросеть.
 */
public class NNet3L implements Serializable {

    /**
     * Двусторонняя квантиль нормального распределения на уровне доверия 0.95.
     */
    private static final double U = 1.96;
    /**
     * Максимальное значение сигмоида.
     */
    private static final double SUP_F = 0.5;
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
    public NNet3L(final int n0, final int n1, final int n2, final boolean hasSigmoidOutput) {
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
    public NNet3L(final int[] n, final boolean hasSigmoidOutput) {
        this(n[0], n[1], n[2], hasSigmoidOutput);
    }

    /**
     * Конструктор, читающий сеть из двоичного потока.
     *
     * @param dis              поток, откуда будет прочитана структура нейросети и веса;
     *                         (см. формат в методе {@link NNet3L#write}).
     * @param hasSigmoidOutput {@code true}, если в выходам нейросети применяется сигмоид.
     * @throws IOException если не удалось прочитать нейросеть из потока.
     */
    public NNet3L(final DataInputStream dis, final boolean hasSigmoidOutput) throws IOException {
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
     * Конструктор копирования.
     *
     * @param net нейросеть.
     */
    public NNet3L(final NNet3L net) {
        n0 = net.n0;
        n1 = net.n1;
        n2 = net.n2;
        allocateMemory();
        System.arraycopy(net.w, 0, w, 0, w.length);
        hasSigmoidOutput = net.hasSigmoidOutput;
    }

    /**
     * Сделать объект копией аргумента.
     * При несовпадении конфигураций сетей параметры сети инициализируются заново
     * нужными размерами.
     *
     * @param net нейросеть.
     */
    public void set(final NNet3L net) {
        if (this == net) return;
        setConfigurationTo(net.n0, net.n1, net.n2);
        System.arraycopy(net.w, 0, w, 0, w.length);
        hasSigmoidOutput = net.hasSigmoidOutput;
    }

    /**
     * Создать копию.
     *
     * @return копия этого объекта.
     */
    public NNet3L getCopy() {
        return new NNet3L(this);
    }

    /**
     * Загрузить нейросеть из двоичного потока.
     *
     * @param dis поток, откуда будет прочитана структура нейросети и веса;
     *            см. формат в методе {@link NNet3L#write(DataOutputStream)}.
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
     * Напечатать конфигурацию нейросети и её коэффициенты в текстовом формате.
     *
     * @param ps поток, куда будут выведены коэффициенты нейросети.
     */
    public void print(final PrintStream ps) {
        ps.printf(Locale.US, "Layers: %d%n", 3);
        ps.printf(Locale.US, "n0: %d%n", n0);
        ps.printf(Locale.US, "n1: %d%n", n1);
        ps.printf(Locale.US, "n2: %d%n", n2);
        ps.printf(Locale.US, "Weights count: %d%n", getNumWeights());
        ps.printf(Locale.US, "Weights:%n");
        for (final double wi : w) {
            ps.println(wi);
        }
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
//    private double f(final double x) {
//        return (x >= 0) ? -0.5 + 1.0 / (1.0 + StrictMath.exp(-x)) : 0.5 - 1.0 / (1.0 + StrictMath.exp(x));
//    }

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

    /**
     * Установить случайные веса.
     * <p/>
     * В этом методе предполагается, что обучающие образы нормализованы.
     *
     * @param rnd датчик случайных чисел.
     */
    public void setRandomWeights(final Rnd rnd) {
        final int dn0 = n0 + 1;
        for (int j = 0; j < n1; j++) {
            randomizeL1Weights(rnd, U, j);
        }
        final int dn1 = n1 + 1;
        for (int i = 0, si = n1 * dn0; i < n2; i++, si += dn1) {
            double a1 = 0.0;
            double a2 = 0.0;
            for (int j = 0; j < n1; j++) {
                final double wij = rnd.rnd(-1.0, 1.0);
                a1 += Math.abs(wij);
                a2 += wij * wij;
                w[si + j] = wij;
            }
            a2 = 0.5 * SUP_F * Math.sqrt(a2);
            final double b = w[si + n1] = rnd.rnd(-a2, a2);
            a1 = SUP_F * a1 + Math.abs(b);
            final double x = rnd.rnd(2.0, 7.0) / a1;
            for (int j = 0; j <= n1; j++) {
                w[si + j] *= x;
            }
        }
    }

    /**
     * Рандомизировать все веса входящие в данный нейрон на внутреннем слое.
     * <p/>
     * В этом методе предполагается, что обучающие образы нормализованы.
     *
     * @param rnd    датчик случайных чисел.
     * @param spread "ширина" диапазона входных данных. Для нормализованных
     *               данных выбирается как квантиль нормального распределения
     *               на нужном уровне доверия, например {@link #U}.
     * @param j      номер нейрона на внутреннем слое.
     */
    private void randomizeL1Weights(final Rnd rnd, final double spread, final int j) {
        final int sj = j * (n0 + 1);
        double a1 = 0.0;
        double a2 = 0.0;
        for (int k = 0; k < n0; k++) {
            final double wjk = rnd.rnd(-1.0, 1.0);
            a1 += Math.abs(wjk);
            a2 += wjk * wjk;
            w[sj + k] = wjk;
        }
        a2 = 0.5 * spread * Math.sqrt(a2);
        final double b = w[sj + n0] = rnd.rnd(-a2, a2);
        a1 = spread * a1 + Math.abs(b);
        final double x = rnd.rnd(2.0, 7.0) / a1;
        for (int k = 0; k <= n0; k++) {
            w[sj + k] *= x;
        }
    }

    /**
     * Добавление к весам случайных чисел с равномерным распределением
     * в диапазоне (-spread/2, spread/2).
     * <p/>
     * В этом методе предполагается, что обучающие образы нормализованы.
     *
     * @param rnd    датчик случайных чисел.
     * @param spread ширина диапазона.
     */
    public void shakeWeights(final Rnd rnd, final double spread) {
        if (spread > 0) {
            int i = 0;
            for (final int len = n1 * (n0 + 1); i < len; i++) {
                w[i] += (-0.5 + rnd.rnd()) * spread / U;
            }
            for (final int len = w.length; i < len; i++) {
                w[i] += (-0.5 + rnd.rnd()) * spread / SUP_F;
            }
        }
    }

    /**
     * Прибавить данные веса к весам нейросетей с данным коэффициентом.
     *
     * @param m  множитель, на который будут домножены все элементы прибавляемого массива.
     * @param dw массив прибавляемых весов длины на менее {@link #getNumWeights()}.
     */
    public void addWeights(final double m, final double[] dw) {
        for (int i = w.length - 1; i >= 0; i--) {
            w[i] += m * dw[i];
        }
    }

    /**
     * Вычислить производную сигмоида.
     *
     * @param f значение сигмоида.
     * @return значение производной сигмоида.
     */
    private double df(final double f) {
        return 0.25 - f * f;
    }

    /**
     * По входному массиву данных получить массив значений на выходах нейросети.
     *
     * @param sample обучающий образ: первых {@link #getNumInputs} элементов массива - это вход,
     *               следующих {@link #getNumOutputs} элементов - это выход,
     *               следующий элемент (если есть) - это вес.
     * @return среднеквадратичная ошибка нейросети для данного обучающего образа.
     */
    public double computeError(final double[] sample) {
        int wi = 0;
        for (int j = 0; j < n1; j++) {
            double s = 0;
            for (int k = 0; k < n0; k++) {
                s += w[wi++] * sample[k];
            }
            y1[j] = SigmoidFunction.f(s + w[wi++]);
        }
        double e = 0.0;
        if (hasSigmoidOutput) {
            for (int i = 0; i < n2; i++) {
                double s = 0;
                for (int j = 0; j < n1; j++) {
                    s += w[wi++] * y1[j];
                }
                final double d = SigmoidFunction.f(s + w[wi++]) - sample[n0 + i];
                e += d * d;
            }
        } else {
            for (int i = 0; i < n2; i++) {
                double s = 0;
                for (int j = 0; j < n1; j++) {
                    s += w[wi++] * y1[j];
                }
                final double d = s + w[wi++] - sample[n0 + i];
                e += d * d;
            }
        }
        return e;
    }

    /**
     * Вспомогательный массив для метода {@link #computeGradient}.
     */
    private double[] delta = null;

    /**
     * Вычислить градиент функции ошибки в данной точке и прибавить его к данному вектору.
     *
     * @param sample обучающий образ: первых {@link #getNumInputs} элементов массива - это вход,
     *               следующих {@link #getNumOutputs} элементов - это выход,
     *               следующий элемент (если есть) - это вес.
     * @param dw     массив длины не менее {@link #getNumWeights()}, в котором накапливается градиент.
     * @return вес данного обучающего образа.
     */
    public double computeGradient(final double[] sample, final double[] dw) {
        propagate(sample);
        final double v = sample[n0 + n2]; // вес обучающего образа
        if (delta == null || delta.length < n2) delta = new double[n2];
        int dwId = w.length;
        if (hasSigmoidOutput) {
            for (int i = n2 - 1; i >= 0; i--) {
                double d = y2[i];
                d = v * (d - sample[n0 + i]) * df(d);
                delta[i] = d;
                dw[--dwId] += d;
                for (int j = n1 - 1; j >= 0; j--) {
                    dw[--dwId] += d * y1[j];
                }
            }
        } else {
            for (int i = n2 - 1; i >= 0; i--) {
                final double d = v * (y2[i] - sample[n0 + i]);
                delta[i] = d;
                dw[--dwId] += d;
                for (int j = n1 - 1; j >= 0; j--) {
                    dw[--dwId] += d * y1[j];
                }
            }
        }
        for (int j = n1 - 1; j >= 0; j--) {
            double d = 0.0;
            for (int i = 0, wij = (n0 + 1) * n1 + j; i < n2; i++, wij += (n1 + 1)) {
                d += delta[i] * w[wij];
            }
            d *= df(y1[j]);
            dw[--dwId] += d;
            for (int k = n0 - 1; k >= 0; k--) {
                dw[--dwId] += d * sample[k];
            }
        }
        return v;
    }

    /**
     * Добавить нейроны на внутренний слой. Веса новых связей выбираются наудачу из интервала (-spread/2, spread/2).
     * <p/>
     * В этом методе предполагается, что обучающие образы нормализованы.
     *
     * @param n   положительное число добавляемых нейронов.
     * @param rnd датчик случайных чисел.
     */
    public void addNeuronsToInnerLayer(final int n, final Rnd rnd) {
        final double[] w0 = w;
        final int n10 = n1;
        setConfigurationTo(n0, n1 + n, n2);
        int si0 = (n0 + 1) * n10; // индекс первого веса второго слоя в старом массиве весов.
        int si = (n0 + 1) * n1; // индекс первого веса второго слоя в новом массиве весов.
        System.arraycopy(w0, 0, w, 0, si0);
        for (int j = n10; j < n1; j++) {
            randomizeL1Weights(rnd, 0.196, j);
        }
        final int dn10 = n10 + 1;
        final int dn1 = n1 + 1;
        for (int i = 0; i < n2; i++, si0 += dn10, si += dn1) {
            System.arraycopy(w0, si0, w, si, n10);
            for (int j = n10; j < n1; j++) {
                w[si + j] = 0.01 * (-0.5 + rnd.rnd()) / SUP_F;
            }
            w[si + n1] = w0[si0 + n10];
        }
    }

    /**
     * Нормализация: аффинное преобразование пространства входных векторов,
     * стандартизирующее статистику по каждой входной переменной.
     * <p/>
     * Выходы не стандартизируются, но исключаются постоянные выходы.
     *
     * @param nI число входов в нормализованных данных не превосходящее {@link #getNumInputs}.
     * @param nO число выходов в нормализованных данных не превосходящее {@link #getNumOutputs()}.
     * @param a  массив длины ({@link #getNumInputs} + {@link #getNumOutputs}) - сдвиг линейного преобразования
     *           непостоянных входов, проиндексированный как ненормализованные данные.
     * @param b  массив длины ({@link #getNumInputs} + {@link #getNumOutputs}) - множитель линейного преобразования
     *           непостоянных входов, проиндексированный как ненормализованные данные.
     *           В нормализованных данных используются только те входы, для которых b[i] > 0.
     */
    public void normalize(final int nI, final int nO, final double[] a, final double[] b) {
        final double[] w0 = w;
        final int n00 = n0;
        final int n20 = n2;
        setConfigurationTo(nI, n1, nO);
        final int dn0 = n0 + 1;
        final int dn00 = n00 + 1;
        for (int j = 0, sj = 0, sj0 = 0; j < n1; j++, sj += dn0, sj0 += dn00) {
            double wc = w0[sj0 + n00]; // свободный коэффициент
            int k = 0;
            for (int k0 = 0; k0 < n00; k0++) {
                final double w0jk = w0[sj0 + k0];
                wc += w0jk * a[k0];
                if (b[k0] > 0) {
                    w[sj + k] = w0jk * b[k0];
                    k++;
                }
            }
            w[sj + n0] = wc;
        }
        if (w0 == w) return;
        if (n20 == n2) { // постоянных выходов нет.
            System.arraycopy(w0, dn00 * n1, w, dn0 * n1, (n1 + 1) * n2);
            return;
        }
        // убираем постоянные выходы.
        final int dn1 = n1 + 1;
        for (int i0 = 0, si0 = dn00 * n1, si = dn0 * n1; i0 < n20; i0++, si0 += dn1) {
            if (b[n00 + i0] > 0) {
                System.arraycopy(w0, si0, w, si, dn1);
                si += dn1;
            }
        }
    }

    /**
     * Денормализация &mdash; это преобразование, обратное к {@link #normalize}.
     *
     * @param nI число входов в нормализованных данных не меньшее, чем {@link #getNumInputs}.
     * @param nO число выходов в нормализованных данных не меньшее, чем {@link #getNumOutputs()}.
     * @param a  сдвиг линейного преобразования непостоянных входов,
     *           проиндексированный как ненормализованные данные.
     *           Длины массивов a и b равны между собой и равны числу входов в ненормализованных данных.
     * @param b  множитель линейного преобразования непостоянных входов,
     *           проиндексированный как ненормализованные данные.
     *           В нормализованных данных используются только те входы, для которых b[i] > 0.
     */
    public void denormalize(final int nI, final int nO, final double[] a, final double[] b) {
        final double[] w0 = w;
        final int n00 = n0;
        final int n20 = n2;
        setConfigurationTo(nI, n1, nO);
        final int dn0 = n0 + 1;
        final int dn00 = n00 + 1;
        for (int j = 0, sj = 0, sj0 = 0; j < n1; j++, sj += dn0, sj0 += dn00) {
            double wc = w0[sj0 + n00]; // свободный коэффициент
            int k0 = 0;
            for (int k = 0; k < n0; k++) {
                if (b[k] > 0) {
                    w[sj + k] = w0[sj0 + k0] / b[k];
                    wc -= w[sj + k] * a[k];
                    k0++;
                } else {
                    w[sj + k] = 0.0;
                }
            }
            w[sj + n0] = wc;
        }
        if (w0 == w) return;
        if (n20 == n2) {
            System.arraycopy(w0, dn00 * n1, w, dn0 * n1, (n1 + 1) * n2);
            return;
        }
        // вставляем постоянные выходы.
        final int dn1 = n1 + 1;
        for (int i = 0, si = dn0 * n1, si0 = dn00 * n1; i < n2; i++, si += dn1) {
            if (b[n0 + i] > 0) {
                System.arraycopy(w0, si0, w, si, dn1);
                si0 += dn1;
            } else {
                for (int j = 0; j < n1; j++) {
                    w[si + j] = 0.0;
                }
                if (hasSigmoidOutput) {
                    w[si + n1] = fInverse(a[n0 + i]);
                } else {
                    w[si + n1] = a[n0 + i];
                }
            }
        }
    }

    /**
     * Обновить минимальное и максимальное значения возможных выходов для каждого из внутренних нейронов.
     * Этот метод нужен для обнаружения "мертвых" нейронов.
     *
     * @param in  массив входных данных длины не меньше, чем {@link #getNumInputs()}.
     * @param max массив длины не меньше {@link #getNumInnerNeurons},
     *            в котором накоплены максимальные выходы для каждого внутреннего нейрона.
     * @param min массив длины не меньше {@link #getNumInnerNeurons},
     *            в котором накоплены минимальные выходы для каждого внутреннего нейрона.
     */
    public void computeMinMaxInnerNeuronsValues(final double[] in, final double[] min, final double[] max) {
        final int l1offset = (n0 + 1) * n1;
        final int dn1 = n1 + 1;
        int wi = 0;
        for (int j = 0; j < n1; j++) {
            double s = 0;
            for (int k = 0; k < n0; k++) {
                s += w[wi++] * in[k];
            }
            final double y1j = SigmoidFunction.f(s + w[wi++]);
            for (int i = 0, si = l1offset, mij = j; i < n2; i++, si += dn1, mij += n1) {
                final double wijy1j = w[si + j] * y1j;
                if (wijy1j < min[mij]) min[mij] = wijy1j;
                if (wijy1j > max[mij]) max[mij] = wijy1j;
            }
        }
    }

    /**
     * Удалить внутренний нейрон.
     *
     * @param dj     номер удаляемого нейрона.
     * @param wijy1j массив произведений w^(2)_ij * y^(1)_j.
     */
    public void deleteInnerNeuron(final int dj, final double[] wijy1j) {
        final double[] w0 = w;
        setConfigurationTo(n0, n1 - 1, n2);
        final int dn0 = n0 + 1;
        final int lsize = dj * dn0;
        System.arraycopy(w0, 0, w, 0, lsize);
        System.arraycopy(w0, lsize + dn0, w, lsize, n1 * dn0 - lsize);

        final int dn1 = n1 + 1;
        final int dn10 = n1 + 2;
        for (int i = 0, si = n1 * dn0, si0 = (n1 + 1) * dn0; i < n2; i++, si += dn1, si0 += dn10) {
            for (int j = 0, j0 = 0; j <= n1; j++, j0++) {
                if (j == dj) j0++;
                w[si + j] = w0[si0 + j0];
            }
            w[si + n1] += wijy1j[i];
        }
    }

    /**
     * Удалить вход.
     *
     * @param dk номер удаляемого входа от 0 до {@link #getNumInputs()} - 1.
     */
    public void deleteInput(final int dk) {
        final double[] w0 = w;
        setConfigurationTo(n0 - 1, n1, n2);
        final int dn0 = n0 + 1;
        for (int j = 0, sj = 0; j < n1; j++, sj += dn0) {
            for (int k = 0, k0 = 0; k < n0; k++, k0++) {
                if (k0 == dk) k0++;
                w[sj + k] = w0[sj + j + k0];
            }
        }
        final int l2offset = n1 * (n0 + 1);
        System.arraycopy(w0, n1 * (n0 + 2), w, l2offset, w.length - l2offset);
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