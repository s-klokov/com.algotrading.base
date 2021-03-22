package com.algotrading.base.lib.optim;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Минимизация функции нескольких переменных методом Нелдера-Мида.
 * <p>
 * См. https://en.wikipedia.org/wiki/Nelder%E2%80%93Mead_method
 */
public class NelderMeadMinimizer {
    /**
     * Значение параметра alpha (отражение).
     */
    private double alpha = 1.0;
    /**
     * Значение параметра gamma (растяжение).
     */
    private double gamma = 2.0;
    /**
     * Значение параметра rho (сжатие).
     */
    private double rho = 0.5;
    /**
     * Значение параметра sigma (сокращение).
     */
    private double sigma = 0.5;
    /**
     * Оптимизируемая функция. Вычисляет значение функции.
     */
    private ToDoubleFunction<double[]> f = null;
    /**
     * Точки симплекса в количестве n + 1 для n-мерного пространства.
     */
    private PointValue[] points = null;
    /**
     * Максимально допустимое количество итераций.
     */
    private int iterations = 100;
    /**
     * Условие прекращения расчётов в зависимости от текущих точек симплекса.
     */
    private Predicate<PointValue[]> termination = points -> false;

    private PointValue centroid = null;
    private PointValue reflected = null;
    private PointValue expanded = null;
    private PointValue contracted = null;

    public NelderMeadMinimizer withAlpha(final double alpha) {
        this.alpha = alpha;
        return this;
    }

    public NelderMeadMinimizer withGamma(final double gamma) {
        this.gamma = gamma;
        return this;
    }

    public NelderMeadMinimizer withRho(final double rho) {
        this.rho = rho;
        return this;
    }

    public NelderMeadMinimizer withSigma(final double sigma) {
        this.sigma = sigma;
        return this;
    }

    public NelderMeadMinimizer withMaxIterations(final int maxIterations) {
        iterations = maxIterations;
        return this;
    }

    public NelderMeadMinimizer withTermination(final Predicate<PointValue[]> termination) {
        this.termination = termination;
        return this;
    }

    /**
     * Задать начальные вершины симплекса (начальные приближения).
     *
     * @param points двумерный массив из (n + 1) точки в случае n-мерного пространства
     * @return this
     */
    public NelderMeadMinimizer withInitialPoints(final double[][] points) {
        this.points = new PointValue[points.length];
        for (int i = 0; i < points.length; i++) {
            final double[] point = points[i];
            if (point.length + 1 == points.length) {
                this.points[i] = new PointValue(point);
            } else {
                throw new IllegalArgumentException("Dimensions mismatch: "
                                                   + points.length + " points and point = " + Arrays.toString(point));
            }
        }
        return this;
    }

    /**
     * Минимизировать функцию.
     *
     * @param f функция
     * @return точка минимума и значение функции в этой точке
     */
    public PointValue minimize(final ToDoubleFunction<double[]> f) {
        this.f = f;
        for (final PointValue point : points) {
            point.evaluate(f);
        }
        while (true) {
            Arrays.sort(points, Comparator.comparingDouble(point -> point.value));
            if (iterations <= 0 || termination.test(points)) {
                return points[0];
            }
            updateCentroid();
            reflect();
            if (points[0].value <= reflected.value && reflected.value < points[points.length - 2].value) {
                final PointValue temp = points[points.length - 1];
                points[points.length - 1] = reflected;
                reflected = temp;
                iterations--;
                continue;
            }
            if (reflected.value < points[0].value) {
                expand();
                final PointValue temp = points[points.length - 1];
                if (expanded.value < reflected.value) {
                    points[points.length - 1] = expanded;
                    expanded = temp;
                } else {
                    points[points.length - 1] = reflected;
                    reflected = temp;
                }
                iterations--;
                continue;
            }
            contract();
            if (contracted.value < points[points.length - 1].value) {
                final PointValue temp = points[points.length - 1];
                points[points.length - 1] = contracted;
                contracted = temp;
                iterations--;
                continue;
            }
            shrink();
            iterations--;
        }
    }

    /**
     * Когда минимизируемая функция определена, вычисляет значение функции в данной точке.
     *
     * @param point координаты точки
     * @return значение функции
     */
    public double evaluate(final double[] point) {
        return f.applyAsDouble(point);
    }

    private void updateCentroid() {
        final int n = points.length - 1;
        if (centroid == null) {
            centroid = new PointValue(new double[n]);
        }
        final double[] ox = centroid.x;
        Arrays.fill(ox, 0);
        for (int i = 0; i < points.length - 1; i++) {
            final double[] px = points[i].x;
            for (int j = 0; j < n; j++) {
                ox[j] += px[j];
            }
        }
        for (int j = 0; j < n; j++) {
            ox[j] /= n;
        }
        centroid.value = Double.NaN;
    }

    private void reflect() {
        final int n = points.length - 1;
        if (reflected == null) {
            reflected = new PointValue(new double[n]);
        }
        final double[] ox = centroid.x;
        final double[] rx = reflected.x;
        final double[] wx = points[points.length - 1].x;
        for (int i = 0; i < n; i++) {
            rx[i] = ox[i] + alpha * (ox[i] - wx[i]);
        }
        reflected.evaluate(f);
    }

    private void expand() {
        final int n = points.length - 1;
        if (expanded == null) {
            expanded = new PointValue(new double[n]);
        }
        final double[] ox = centroid.x;
        final double[] rx = reflected.x;
        final double[] ex = expanded.x;
        for (int i = 0; i < n; i++) {
            ex[i] = ox[i] + gamma * (rx[i] - ox[i]);
        }
        expanded.evaluate(f);
    }

    private void contract() {
        final int n = points.length - 1;
        if (contracted == null) {
            contracted = new PointValue(new double[n]);
        }
        final double[] ox = centroid.x;
        final double[] cx = contracted.x;
        final double[] wx = points[points.length - 1].x;
        for (int i = 0; i < n; i++) {
            cx[i] = ox[i] + rho * (wx[i] - ox[i]);
        }
        contracted.evaluate(f);
    }

    private void shrink() {
        final int n = points.length - 1;
        final double[] bx = points[0].x;
        for (int i = 1; i < points.length; i++) {
            final PointValue point = points[i];
            final double[] px = point.x;
            for (int j = 0; j < n; j++) {
                px[j] = bx[j] + sigma * (px[j] - bx[j]);
            }
            point.evaluate(f);
        }
    }
}
