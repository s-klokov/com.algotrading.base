package com.algotrading.base.lib.optim;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

/**
 * Точка n-мерного пространства и значение функции.
 */
public final class PointValue {
    /**
     * Координаты точки в n-мерном пространстве.
     */
    public final double[] x;
    /**
     * Значение функции.
     */
    public double value;

    public PointValue(final double[] x, final double value) {
        this.x = x;
        this.value = value;
    }

    public PointValue(final double[] x) {
        this(x, Double.NaN);
    }

    public PointValue(final double[] x, final ToDoubleFunction<double[]> f) {
        this(x, f.applyAsDouble(x));
    }

    public final double evaluate(final ToDoubleFunction<double[]> f) {
        return value = f.applyAsDouble(x);
    }

    /**
     * @return координаты точки
     */
    public final double[] x() {
        return Arrays.copyOf(x, x.length);
    }

    /**
     * @param i номер координаты
     * @return значение координаты
     */
    public final double x(final int i) {
        return x[i];
    }

    /**
     * @return значение функции в точке
     */
    public final double value() {
        return value;
    }

    @Override
    public String toString() {
        return Arrays.toString(x) + ", value=" + value;
    }
}
