package com.algotrading.base.lib;

/**
 * Мультипликативный датчик случайных чисел с множителем {@code 2^40} и
 * модулем {@code 5^17}.
 */
public final class Rnd {

    private static final double DIVISOR = 1.0 / 1099511627776.0; // 1/2^40;

    private long a;

    public Rnd() {
        this(System.nanoTime() * 2 + 1);
    }

    /**
     * Этот конструктор создает генератор с заданным зерном.
     *
     * @param seed зерно генератора.
     */
    public Rnd(final long seed) {
        a = ((seed & 0xFFFFFFFFFFL) | 1L);
    }

    public void setSeed(final long seed) {
        a = ((seed & 0xFFFFFFFFFFL) | 1L);
    }

    public long getSeed() {
        return a;
    }

    public double rnd() {
        a *= 762939453125L; // *5^17
        a &= 0xFFFFFFFFFFL; // mod 2^40
        return DIVISOR * a;
    }

    public int rnd(final int n) {
        return (int) (n * rnd());
    }

    public double rnd(final double a, final double b) {
        return a + (b - a) * rnd();
    }
}
