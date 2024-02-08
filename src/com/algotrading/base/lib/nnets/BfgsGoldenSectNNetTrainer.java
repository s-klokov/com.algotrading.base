package com.algotrading.base.lib.nnets;

import java.util.Locale;

/**
 * Обучатель нейросети методом BFGS, в котором шаг выбирается методом золотого сечения.
 * <p/>
 * <a href="http://en.wikipedia.org/wiki/BFGS_method">Описание в Википедии</a><br>
 * <a href="http://www.nsc.ru/rus/textbooks/akhmerov/mo_unicode/5.html#NotionBFSM">Ахмеров Р.Р. Методы оптимизации гладких функций</a>
 * <p/>
 * <h1>Алгоритм</h1>
 * <h2>Обозначения</h2>
 * k - номер итерации;<br>
 * w_k - вектор весов;<br>
 * grad_k - градиент штрафной функции в точке w_k;<br>
 * B_k - оценка обратной матрицы Гессе;<br>
 * p_k - вектор, задающий направление шага;<br>
 * s_k - вектор, задающий реально выполненный шаг;<br>
 * y_k - разность градиентов.
 * <h2>Инициализация</h2>
 * 1. w_0 = вектор весов обучаемой нейросети;<br>
 * 2. B_0 = E;<br>
 * <h2>Итерация номер k = 0, 1, ...</h2>
 * 1. p_k = -B_k * grad_k;<br>
 * 2. Если p_k * g_k > 0, то p_k = -grad_k, B_k = E;<br>
 * 3. s_k = h_k * p_k, где величина шага h_k выбирается как в методе парабол;<br>
 * 4. w_{k+1} = w_k + s_k;<br>
 * 5. y_k = grad_{k+1} - grad_k;<br>
 * 6. sy = s_k^T * y_k;<br>
 * 7. Если |sy| < 1e-10, то B_{k+1} = E;<br>
 * 8. Иначе B_{k+1} = B_k + gamma_k * s_k * s_k^T - (B_k * y_k * s_k^T + s_k * y_k^T * B_k) / sy,<br>
 * где gamma_k = (sy + y_k^T * B_k * y_k) / (sy * sy).
 */
public class BfgsGoldenSectNNetTrainer extends GoldenSectNNetTrainer {

    /**
     * Оценка обратной матрицы Гессе. Она симметрична. Поэтому запоминается только нижний треугольник.
     * <p/>
     * Если i - номер строки (начиная с 0), j - номер столбца (начиная с 0), j <= i, то<br>
     * B_{i,j} = B_{j,i} = mB[(i + 1) * i / 2 + j].
     */
    private double[] mB = null;
    /**
     * Градиент на предыдущем шаге.
     */
    private double[] g0 = null;
    /**
     * Вспомогательный массив для хранения произведения B_k * y_k.
     */
    private double[] by = null;

    /**
     * Конструктор.
     */
    public BfgsGoldenSectNNetTrainer() {
        super(600);
    }

    @Override
    protected void initialize(final int n) {
        super.initialize(n);
        final int m = (n + 1) * n / 2;
        if (mB == null || mB.length < m) mB = new double[m];
        // Делаем единичную матрицу
        for (int i = 0, r = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                mB[r++] = 0.0;
            }
            mB[r++] = 1.0;
        }
        if (g0 == null || g0.length < n) {
            g0 = new double[n];
            by = new double[n];
        }
    }

    @Override
    protected double step(final int n, final double g2) {
        double[] g = getGradient();
        System.arraycopy(g, 0, g0, 0, n); // g0 = grad_k

        double p2 = 0.0; // = p_k^T * p_k
        double gp = 0.0; // = grad_k^T * p_k
        // p_k = -B_k * grad_k
        for (int i = 0; i < n; i++) {
            final int ioff = (i + 1) * i / 2;
            double pi = 0.0;
            int j = 0;
            for (; j <= i; j++) pi -= mB[ioff + j] * g[j];
            for (; j < n; j++) pi -= mB[i + (j + 1) * j / 2] * g[j];
            p[i] = pi;
            p2 += pi * pi;
            gp += g[i] * pi;
        }
        if (gp >= 0) {
            // Хотим сделать шаг по градиенту. Это не правильно.
            // Сбрасываем матрицу B в единичную и шагаем против градиента.
            initialize(n);
            for (int i = 0; i < n; i++) p[i] = -g[i];
            p2 = g2;
        }

        final double h = stepByGoldenSect(p2);
        final double xh = h / Math.sqrt(p2);
        g = getGradient(); // = grad_{k+1}

        final double[] s = p; // s_k = w_{k+1} - w_{k} = h * p_k
        final double[] y = g0; // y_k = grad_{k+1} - grad_k
        double sy = 0.0; // = s_k^T * y_k
        for (int i = 0; i < n; i++) {
            s[i] = xh * p[i];
            y[i] = g[i] - g0[i];
            sy += s[i] * y[i];
        }

        if (Math.abs(sy) < 1E-10) {
            // Сбросим матрицу B
            initialize(n);
        } else {
            double yBy = 0.0; // = y_k^T * B_k * y_k
            // by = B_k * y_k
            for (int i = 0; i < n; i++) {
                final int ioff = (i + 1) * i / 2;
                double byi = 0.0;
                int j = 0;
                for (; j <= i; j++) byi += mB[ioff + j] * y[j];
                for (; j < n; j++) byi += mB[i + (j + 1) * j / 2] * y[j];
                by[i] = byi;
                yBy += y[i] * byi;
            }

            final double gamma = (sy + yBy) / (sy * sy);
            for (int i = 0, r = 0; i < n; i++) {
                final double si = s[i];
                final double byi = by[i];
                for (int j = 0; j <= i; j++) {
                    final double sj = s[j];
                    mB[r++] += gamma * si * sj - (byi * sj + si * by[j]) / sy;
                }
            }
        }
        return h / Math.sqrt(n);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "BfgsGoldenSectNNetTrainer: defaultCycles = %d, mu = %.4f", maxCycles, mu);
    }
}
