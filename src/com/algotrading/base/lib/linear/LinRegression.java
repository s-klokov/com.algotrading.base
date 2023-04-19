package com.algotrading.base.lib.linear;

/**
 * Расчёт многомерной регрессионной модели.
 * <p>
 * Пусть имеется вектор целевых значений targets[i] и матрица значений предикторов predictors[i][j].
 * Требуется вычислить вектор коэффициентов регрессионной модели cf[j], для которых<br>
 * estimates[i] = sum_j (predictors[i][j] * cf[j])<br>
 * дают оценки targets[i] по методу наименьших квадратов.
 */
public class LinRegression {
    /**
     * Коэффициенты линейной регрессии.
     */
    public final double[] cf;
    /**
     * Коэффициент детерминации R2.
     */
    public final double r2;

    private LinRegression(final double[] cf, final double r2) {
        this.cf = cf;
        this.r2 = r2;
    }

    /**
     * Произвести оценку по значениям предикторов.
     *
     * @param x массив предикторов
     * @return оценка
     */
    public double predict(final double[] x) {
        if (x.length != cf.length) {
            throw new IllegalArgumentException("Size mismatch: #x=" + x.length + "!=" + cf.length);
        }
        double z = 0;
        for (int j = 0; j < x.length; j++) {
            z += x[j] * cf[j];
        }
        return z;
    }

    public static LinRegression fit(final double[] targets, final double[][] predictors, final boolean hasR2) {
        if (targets.length != predictors.length) {
            throw new IllegalArgumentException("Size mismatch: #targets=" + targets.length
                    + "!=" + predictors.length + "=#predictors");
        }
        for (int i = 1; i < predictors.length; i++) {
            if (predictors[i].length != predictors[0].length) {
                throw new IllegalArgumentException("Size mismatch: #targets[" + i + "]="
                        + predictors[i].length + "!=" + predictors.length + "=#targets[0]");
            }
        }
        final int numPredictors = predictors[0].length;
        final double[] cf = new double[numPredictors];
        for (int j = 0; j < numPredictors; j++) {
            double sum = 0;
            for (int i = 0; i < targets.length; i++) {
                sum += predictors[i][j] * targets[i];
            }
            cf[j] = sum;
        }
        LinAlg.solve(LinAlg.ATA(predictors), cf);
        double r2 = Double.NaN;
        if (hasR2) {
            double sum = 0;
            for (final double target : targets) {
                sum += target;
            }
            final double avg = sum / targets.length;
            double tss = 0;
            for (final double target : targets) {
                final double d = target - avg;
                tss += d * d;
            }
            double rss = 0;
            for (int i = 0; i < targets.length; i++) {
                sum = 0;
                for (int j = 0; j < cf.length; j++) {
                    sum += predictors[i][j] * cf[j];
                }
                final double d = targets[i] - sum;
                rss += d * d;
            }
            r2 = 1.0 - rss / tss;
        }
        return new LinRegression(cf, r2);
    }
}
