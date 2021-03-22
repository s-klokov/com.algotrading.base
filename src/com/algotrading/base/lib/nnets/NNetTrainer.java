package com.algotrading.base.lib.nnets;

import com.algotrading.base.lib.stat.StatisticFix;

import java.io.PrintStream;
import java.util.Locale;

/**
 * Абстрактный класс для итерационных тренеров нейросетей.
 */
public abstract class NNetTrainer implements Comparable<NNetTrainer> {

    /**
     * Минимальная норма градиента штрафной функции.
     */
    private static final double MIN_STEP = 1E-8;
    /**
     * Минимальная норма градиента штрафной функции.
     */
    private static final double MIN_GRAD = 1E-8;

    public enum StopReason {
        MaxCycles("Stop because a maximum of cycles reached."),
        SmallStep("Stop because the step is too small."),
        SmallGrad("Stop because the gradient is small."),
        SmallRE("Stop because relative error fall is small.");

        final String description;

        StopReason(final String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Максимальное число итераций обучения.
     */
    public int maxCycles;
    /**
     * Периодичность вывода в лог состояния обучения.
     */
    public int logPeriod;

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Обучаемая нейросеть.
     */
    protected NNet3L net = null;
    /**
     * Обучающие образы.
     */
    protected NNetData data = null;
    /**
     * Ошибка на тренировочных образах.
     */
    private double trainErr = -1;
    /**
     * Ошибка на тестовых образах.
     */
    private double testErr = -1;

    /**
     * Вектор градиента.
     */
    private double[] grad = null;
    /**
     * {@code true}, если вектор градиента еще не вычислен.
     */
    private boolean isGradInvalid = true;
    /**
     * Число итераций.
     */
    private int endCycles = 0;
    /**
     * Число уже сделанных итераций.
     */
    private int iterations = 0;
    /**
     * Оценка скорости падения относительной ошибки.
     */
    final StatisticFix reSpeed = new StatisticFix(40);
    /**
     * Причина остановки обучения.
     */
    StopReason stopReason = null;

    /**
     * Конструктор.
     *
     * @param maxCycles число итераций обучения по умолчанию.
     */
    protected NNetTrainer(final int maxCycles) {
        this.maxCycles = maxCycles;
        logPeriod = maxCycles / 20;
    }

    /**
     * @return причина остановки обучения или null, если обучение еще не завершено.
     */
    public StopReason getStopReason() {
        return stopReason;
    }

    /**
     * @return число уже сделанных итераций.
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Инициализация перед началом обучения.
     *
     * @param net  обучаемая нейросеть.
     * @param data обучающие образы.
     */
    public final void initialize(final NNet3L net, final NNetData data) {
        this.net = net;
        this.data = data;
        final int n = net.getNumWeights();
        if (grad == null || grad.length != n) grad = new double[n];
        endCycles = maxCycles;
        iterations = 0;
        reSpeed.clear();
        resetCaches();
        initialize(n);
        stopReason = null;
    }

    /**
     * Дообучить нейросеть.
     *
     * @param cycles количество циклов обучения; если cycles <= 0, то используется значение по умолчанию {@link #maxCycles}.
     * @param log    поток для вывода лога; null, если выводить лог не надо.
     */
    public final void train(int cycles, final PrintStream log) {
        if (log != null) {
            log.println("i; time; n1; trainE; testE; maxE; gamma; trainVar; testVar; trainRE; testRE; maxRE; dw; |G|_2; avgErrFall");
        }
        int n = net.getNumWeights();
        if (cycles <= 0) cycles = maxCycles + 1;
        long time = 0L;
        double maxRE = getMaxRelativeError();
        double dw = 1.0; // евклидова длина сделанного шага.
        for (int i = 0; true; i++, iterations++) {
            final double maxREn = getMaxRelativeError();
            reSpeed.add(maxRE - maxREn);
            maxRE = maxREn;
            long iTime = -System.currentTimeMillis(); // время, потраченное на эту итерацию
            final double[] g = getGradient();
            final double g2 = multiply(g, g, n);
            final double gNorm = g2 <= 0 ? 0.0 : Math.sqrt(g2 / n);
            iTime += System.currentTimeMillis();

            if (log != null && (iterations % logPeriod) == 0) {
                printState(log, time, dw, gNorm, reSpeed.getEv());
            }

            if (iterations == endCycles) {
                stopReason = StopReason.MaxCycles;
            } else if (dw < MIN_STEP) {
                stopReason = StopReason.SmallStep;
            } else if (gNorm < MIN_GRAD) {
                stopReason = StopReason.SmallGrad;
            } else if (reSpeed.getNum() >= reSpeed.getCapacity() / 2 && reSpeed.getEv() < 0) {
                stopReason = StopReason.SmallRE;
            }
            if (stopReason != null) {
                if (data.deleteDeadInnerNeurons(net)) {
                    stopReason = null;
                    endCycles += maxCycles / 10;
                    n = net.getNumWeights();
                    initialize(n);
                    reSpeed.clear();
                    i = -1;
                } else {
                    if (log != null) {
                        if ((iterations % logPeriod) != 0) printState(log, time, dw, gNorm, reSpeed.getEv());
                        log.println(stopReason);
                    }
                    break;
                }
            }
            if (i == cycles) {
                if (log != null && (iterations % logPeriod) != 0) printState(log, time, dw, gNorm, reSpeed.getEv());
                return;
            }

            // Сделать итерацию.
            iTime -= System.currentTimeMillis();
            dw = step(n, g2);
            iTime += System.currentTimeMillis();
            time += iTime;
        }
    }

    /**
     * Инициализация перед началом итераций.
     *
     * @param n количество весов.
     */
    protected abstract void initialize(final int n);

    /**
     * Сделать один шаг итерации.
     *
     * @param n  количество весов.
     * @param g2 скалярный квадрат градиента.
     * @return длина шага в евклидовой метрике.
     */
    protected abstract double step(final int n, final double g2);

    /**
     * @return среднеквадратическая ошибка для тренировочных данных в расчёте на единицу массы и один выход.
     */
    protected double getTrainError() {
        if (trainErr == -1) trainErr = data.getTrainError(net);
        return trainErr;
    }

    /**
     * @return среднеквадратическая ошибка для тестовых данных в расчёте на единицу массы и один выход.
     */
    protected double getTestError() {
        if (testErr == -1) testErr = data.getTestError(net);
        return testErr;
    }

    /**
     * @return максимум из двух ошибок {@link #getTrainError()} и {@link #getTestError()}.
     */
    public double getMaxRelativeError() {
        final double trnE = getTrainError();
        final double tstE = getTestError();
        final double trnVar = data.getTrainVariance();
        final double tstVar = data.getTestVariance();
        final double trnRE = trnVar > 0.0 ? trnE / trnVar : 1.0;
        final double tstRE = tstVar > 0.0 ? tstE / tstVar : 1.0;
        return Math.max(trnRE, tstRE);
    }

    /**
     * @return градиент штрафной функции по тренировочным данным, вычисленный в текущей точке.
     */
    protected double[] getGradient() {
        if (isGradInvalid) {
            isGradInvalid = false;
            data.computeTrainGradient(net, grad);
        }
        return grad;
    }

    /**
     * Прибавить данные веса к весам нейросетей с данным коэффициентом и очистить кэш ошибок.
     *
     * @param m  множитель, на который будут домножены все элементы прибавляемого массива.
     * @param dw массив прибавляемых весов длины на менее {@link NNet3L#getNumWeights()}.
     */
    protected void addWeights(final double m, final double[] dw) {
        net.addWeights(m, dw);
        resetCaches();
    }

    /**
     * Сбросить кэши ошибок и градиента.
     */
    protected final void resetCaches() {
        trainErr = -1.0;
        testErr = -1.0;
        isGradInvalid = true;
    }

    /**
     * Напечатать текущее состояние.
     *
     * @param log   поток для вывода лога.
     * @param time  время, потраченное на эти итерации.
     * @param step  длина сделанного шага в евклидовой метрике.
     * @param gNorm евклидова норма градиента.
     * @param dre   оценка падения относительной ошибки.
     */
    private void printState(final PrintStream log, final long time, final double step, final double gNorm, final double dre) {
        final double trnE = getTrainError();
        final double tstE = getTestError();
        final double maxE = Math.max(trnE, tstE);
        final double gamma = trnE > 0 ? tstE / trnE : 1.0;
        final double trnVar = data.getTrainVariance();
        final double tstVar = data.getTestVariance();
        final double trnRE = trnVar > 0.0 ? trnE / trnVar : 1.0;
        final double tstRE = tstVar > 0.0 ? tstE / tstVar : 1.0;
        final double maxRE = Math.max(trnRE, tstRE);
        log.printf(Locale.US, "%d; %d; %d; %g; %g; %g; %g; %g; %g; %g; %g; %g; %g; %g; %g%n",
                iterations, time, net.getNumInnerNeurons(), trnE, tstE, maxE, gamma,
                trnVar, tstVar, trnRE, tstRE, maxRE, step, gNorm, dre);
    }

    @Override
    public int compareTo(final NNetTrainer o) {
        return Double.compare(getMaxRelativeError(), o.getMaxRelativeError());
    }

    /**
     * Умножить скалярно два данных вектора.
     *
     * @param a   первый вектор.
     * @param b   второй вектор.
     * @param len количество значащих элементов в массивах, задающий вектора.
     * @return скалярное произведение вектора a на вектор b.
     */
    public static double multiply(final double[] a, final double[] b, final int len) {
        double d = 0;
        for (int i = 0; i < len; i++) {
            d += a[i] * b[i];
        }
        return d;
    }

    /**
     * Напечатать в лог строку с полным описанием нейросети.
     *
     * @param net  нейросеть.
     * @param data обучающие образы.
     * @param log  поток для вывода лога; null, если выводить лог не надо.
     */
    public static void printNetState(final NNet3L net, final NNetData data, final PrintStream log) {
        if (log == null) return;
        final double trainVar = data.getTrainVariance();
        final double testVar = data.getTestVariance();
        final double overallVar = data.getOverallVariance();
        final double trainErr = data.getTrainError(net);
        final double testErr = data.getTestError(net);
        final double overallErr = data.getOverallError(net);
        final double maxErr = Math.max(trainErr, testErr);
        final double gamma = trainErr > 0 ? testErr / trainErr : 1.0;
        final double trainVF = trainVar > 0.0 ? 100.0 - 100.0 * trainErr / trainVar : 0.0;
        final double testVF = testVar > 0.0 ? 100.0 - 100.0 * testErr / testVar : 0.0;
        final double overallVF = overallVar > 0.0 ? 100.0 - 100.0 * overallErr / overallVar : 0.0;
        final double trainGN = data.getTrainGradientNorm(net);
        final double testGN = data.getTestGradientNorm(net);
        final double overallGN = data.getOverallGradientNorm(net);
        log.printf(Locale.US,
                "%s, Err = (%.3f, %.3f, %.3f), maxErr = %.4f, gamma = %.5f, Var = (%.3f, %.3f, %.3f), VarFall = (%.2f%%, %.2f%%, %.2f%%), |G| = (%.2e, %.2e, %.2e)%n",
                net, trainErr, testErr, overallErr, maxErr, gamma, trainVar, testVar, overallVar,
                trainVF, testVF, overallVF, trainGN, testGN, overallGN);
    }
}
