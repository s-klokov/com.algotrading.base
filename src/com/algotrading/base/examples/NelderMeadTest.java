package com.algotrading.base.examples;

import com.algotrading.base.lib.optim.NelderMeadMinimizer;
import com.algotrading.base.lib.optim.PointValue;

import java.util.Arrays;

class NelderMeadTest {

    public static void main(final String[] args) {
        final PointValue minimum =
                new NelderMeadMinimizer()
                        .withInitialPoints(new double[][]{
                                {0, 0},
                                {1, 0},
                                {0, 1}
                        })
                        .minimize(x -> 3 * Math.pow(x[0] - 2, 2) + 2 * Math.pow(x[1] - 5, 2) + 10);
        System.out.printf("MinValue=%f, point=%s", minimum.value, Arrays.toString(minimum.x));
    }
}
