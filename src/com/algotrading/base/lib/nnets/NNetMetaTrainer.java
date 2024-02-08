package com.algotrading.base.lib.nnets;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

/**
 * Составной тренер нейросетей.
 */
public class NNetMetaTrainer {
    /**
     * Первоначальное число нейросетей.
     */
    public int initNetCount = 51;
    /**
     * Количество итераций обучения группы нейросетей при первоначальном обучении нейросети.
     */
    public int initCycles = 10;
    /**
     * Коэффициент, задающий максимально возможное соотношение между n1 и n0.
     */
    public double maxN1N0Ratio = 2.0;
    /**
     * Минимальное число обучающих образов в расчёте на один весовой коэффициент нейросети.
     * Если указано нулевое значение, ограничение не применяется.
     */
    public int minDataSizePerWeight = 20;
    /**
     * Модификаторы для числа нейронов на внутреннем слое: n1 = initInNeuronsMods * n0,
     * где n1 - число нейронов на внутреннем слое, n0 - число входов нейросети.
     */
    public double[] initN1Mods = {0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.8, 1.0, 1.5, 2.0};
    /**
     * Число клонов нейросети при дообучении.
     */
    public int imprNetCount = 21;
    /**
     * Количество итераций обучения группы нейросетей при дообучении нейросети.
     */
    public int imprCycles = 10;
    /**
     * Модификаторы для числа нейронов на внутреннем слое,
     * где n1 - число нейронов на внутреннем слое, n0 - число входов нейросети.
     */
    public double[] imprN1Mods = {0.0, 0.05, 0.1, 0.2};
    /**
     * Финишный тренер с хорошей разрешающей способностью.
     */
    public NNetTrainer finishTrainer = new BfgsGoldenSectNNetTrainer();

    /**
     * Вычислить возможное количество нейронов на внутреннем слое.
     *
     * @param n0        число входов.
     * @param n1        текущее количество нейронов на внутреннем слое.
     * @param n2        число выходов.
     * @param mod       модификатор.
     * @param trainSize количество тренировочных обучающих образов.
     * @return возможное количество нейронов на внутреннем слое.
     */
    private int n1i(final int n0, final int n1, final int n2, final double mod, final int trainSize) {
        // Сколько может быть нейронов на внутреннем слое?
        int m = n1 + (int) Math.floor(mod * n0);
        // Но не более числа входов, умноженного на заданный коэффициент
        m = Math.min(m, (int) Math.round(maxN1N0Ratio * n0));
        if (minDataSizePerWeight > 0) {
            // И на каждый вес нейросети должно приходиться как минимум заданное количество обучающих образов
            final int u = (trainSize / minDataSizePerWeight - n2) / (n0 + 1 + n2);
            if (m > u) {
                m = u;
            }
        }
        // Но не менее одного нейрона
        if (m < 1) {
            m = 1;
        }
        // И не менее, чем уже есть сейчас
        return Math.max(n1, m);
    }

    /**
     * Первоначальное обучение нейросети.
     *
     * @param net     обучаемая нейросеть.
     * @param data    набор обучающих образов.
     * @param zeroOut массив константных выходов в случае, когда данный набор обучающих образов пуст.
     * @param log     поток для логгирования вычислений.
     */
    public void train(final NNet3L net, final NNetData data, final double[] zeroOut, final PrintStream log) {
        long overallTime = -System.currentTimeMillis();
        if (log != null) {
            log.println("Initial training...");
            log.println();
            log.println("PARAMETERS:");
            log.println("  net = [" + net + "]");
            log.println("  trainSize = " + data.getTrainSize());
            log.println("  testSize = " + data.getTestSize());
            log.println("  netCount = " + initNetCount);
            log.println("  cycles = " + initCycles);
            log.println("  n1Mods = " + Arrays.toString(initN1Mods));
            log.println("  fastTrainer = [" + createFastNNetTrainer() + "]");
            log.println("  finishTrainer = [" + finishTrainer + "]");
        }
        if (data.getSize() == 0) {
            net.setConfigurationTo(0, 0, zeroOut.length);
            net.setSingular(zeroOut, 0);
            if (log != null) log.println("Train data is empty. Singular net produced.");
            return;
        }
        data.normalize();
        if (data.getTestSize() == 0 || data.getNumOutputs() == 0) {
            data.degenerateNet(net);
            if (log != null) {
                log.println("No test samples or all outputs are constant. Singular net produced.");
            }
        } else {
            data.normalizeNet(net);

            final int n0 = net.getNumInputs();
            final int n2 = net.getNumOutputs();
            final ArrayList<Integer> n1 = new ArrayList<>();
            for (final double mod : initN1Mods) {
                final Integer n1i = n1i(n0, 0, n2, mod, data.getTrainSize());
                if (!n1.contains(n1i)) n1.add(n1i);
            }
            final ArrayList<NNetTrainer> trainers = new ArrayList<>(initNetCount);
            for (int i = 0; i < initNetCount; i++) {
                final NNetTrainer trainer = createFastNNetTrainer();
                final NNet3L pretender = net.getCopy();
                final int n1i = n1.get(i % n1.size());
                pretender.setConfigurationTo(net.getNumInputs(), n1i, net.getNumOutputs());
                pretender.setRandomWeights(data.getRnd());
                trainer.initialize(pretender, data);
                trainers.add(trainer);
            }

            net.set(train(trainers, initCycles, data, log));
            data.denormalizeNet(net);
        }
        data.denormalize();
        if (log != null) {
            log.println();
            NNetTrainer.printNetState(net, data, log);
            overallTime += System.currentTimeMillis();
            log.printf("TOTAL TIME %d:%02d:%02d.%03d%n",
                       overallTime / (1000L * 60L * 60L),
                       (overallTime / (1000L * 60L)) % 60L,
                       (overallTime / 1000L) % 60L,
                       overallTime % 1000L);
        }
    }

    /**
     * Дообучение уже обученной нейросети.
     *
     * @param net     обучаемая нейросеть.
     * @param data    набор обучающих образов.
     * @param zeroOut массив константных выходов в случае, когда данный набор обучающих образов пуст.
     * @param log     поток для логгирования вычислений.
     */
    public void improve(final NNet3L net, final NNetData data, final double[] zeroOut, final PrintStream log) {
        long overallTime = -System.currentTimeMillis();
        if (log != null) {
            log.println("Improving...");
            log.println();
            NNetTrainer.printNetState(net, data, log);
            log.println();
            log.println("PARAMETERS:");
            log.println("  net = [" + net + "]");
            log.println("  trainSize = " + data.getTrainSize());
            log.println("  testSize = " + data.getTestSize());
            log.println("  netCount = " + imprNetCount);
            log.println("  cycles = " + imprCycles);
            log.println("  n1Mods = " + Arrays.toString(imprN1Mods));
            log.println("  fastTrainer = [" + createFastNNetTrainer() + "]");
            log.println("  finishTrainer = [" + finishTrainer + "]");
        }
        if (data.getSize() == 0) {
            net.setConfigurationTo(0, 0, zeroOut.length);
            net.setSingular(zeroOut, 0);
            if (log != null) log.println("Train data is empty. Singular net produced.");
            return;
        }
        data.normalize();
        if (data.getTestSize() == 0 || data.getNumOutputs() == 0) {
            data.degenerateNet(net);
            if (log != null) log.println("No test samples or all outputs are constant. Net is singular.");
        } else {
            data.normalizeNet(net);

            final int n0 = net.getNumInputs();
            final int n1 = net.getNumInnerNeurons();
            final int n2 = net.getNumOutputs();
            final ArrayList<Integer> n1array = new ArrayList<>();
            for (final double mod : imprN1Mods) {
                final Integer n1i = n1i(n0, n1, n2, mod, data.getTrainSize());
                if (!n1array.contains(n1i)) n1array.add(n1i);
            }
            final ArrayList<NNetTrainer> trainers = new ArrayList<>(imprNetCount);
            for (int i = 0; i < imprNetCount; i++) {
                final NNetTrainer trainer = createFastNNetTrainer();
                final NNet3L pretender = net.getCopy();
                final int n1i = n1array.get(i % n1array.size());
                if (n1 == n1i) {
                    final int step = i / n1array.size();
                    pretender.shakeWeights(data.getRnd(), step * 0.1);
                } else {
                    pretender.addNeuronsToInnerLayer(n1i - n1, data.getRnd());
                }
                trainer.initialize(pretender, data);
                trainers.add(trainer);
            }
            net.set(train(trainers, imprCycles, data, log));
            data.denormalizeNet(net);
        }
        data.denormalize();
        if (log != null) {
            log.println();
            NNetTrainer.printNetState(net, data, log);
            overallTime += System.currentTimeMillis();
            log.printf("TOTAL TIME %d:%02d:%02d.%03d%n",
                       overallTime / (1000L * 60L * 60L),
                       (overallTime / (1000L * 60L)) % 60L,
                       (overallTime / 1000L) % 60L,
                       overallTime % 1000L);
        }
    }

    /**
     * @return быстрый тренер для первоначального обучения нейросети.
     */
    protected NNetTrainer createFastNNetTrainer() {
        return new BpNNetTrainer();
    }

    /**
     * Обучить данный набор нейросетей методом вычеркивания.
     *
     * @param trainers набор проинициализированных тренеров.
     * @param cycles   количество циклов обучения набора.
     * @param data     обучающие образы.
     * @param log      поток для вывода лога; null, если вывод отключен.
     * @return обученная нейросеть.
     */
    public NNet3L train(final ArrayList<NNetTrainer> trainers, final int cycles, final NNetData data, final PrintStream log) {
        int trainCycles = 5;
        final int maxCycles = trainers.getFirst().maxCycles;
        final int trainCyclesInc = (int) (Math.floor(((2.0 * maxCycles) / cycles - 2.0 * trainCycles) / (cycles - 1.0)));

        final int numKills = trainers.size() / cycles;

        int killedVolume = 0;
        final long startTime = System.currentTimeMillis();

        while (trainers.size() > 1) {
            final int size = trainers.size();
            for (int id = 0; id < size; id++) {
                final NNetTrainer trainer = trainers.get(id);
                if (trainer.getStopReason() == null) {
                    if (log != null) log.printf("%nTRAIN %d.%d, %s...%n", size, id, trainer.net);
                    trainer.train(trainCycles, log);
                }
            }
            trainCycles += trainCyclesInc;
            Collections.sort(trainers);
            if (log != null) log.println();
            for (int k = 0; k < numKills && trainers.size() > 1; k++) {
                final NNetTrainer trainer = trainers.removeLast();
                final int n = trainer.net.getNumWeights();
                killedVolume += n * trainer.getIterations();
                if (log != null) {
                    final double trainVar = data.getTrainVariance();
                    final double testVar = data.getTestVariance();
                    final double trainErr = trainer.getTrainError();
                    final double testErr = trainer.getTestError();
                    final double maxErr = Math.max(trainErr, testErr);
                    final double gamma = trainErr > 0.0 ? testErr / trainErr : 1.0;
                    final double trainVF = trainVar > 0.0 ? 100.0 - 100.0 * trainErr / trainVar : 0.0;
                    final double testVF = testVar > 0.0 ? 100.0 - 100.0 * testErr / testVar : 0.0;
                    final double[] g = trainer.getGradient();
                    double trainGN = NNetTrainer.multiply(g, g, n);
                    trainGN = Math.sqrt(trainGN / n);
                    log.printf(Locale.US, "KILL: %s, trnE = %g, tstE = %g, maxErr = %g, gamma = %g, trnV = %g, tstV = %g, trnVarFall = %.2f%%, tstVarFall = %.2f%%, trn|G| = %.2g%n",
                               trainer.net, trainErr, testErr, maxErr, gamma, trainVar, testVar, trainVF, testVF, trainGN);
                }
            }
            if (log != null) {
                int executedVolume = killedVolume;
                int remainingVolume = 0;
                int tc = trainCycles;
                for (int k = trainers.size() - 1, j = 1; k >= 0; k--, j++) {
                    final NNetTrainer trainer = trainers.get(k);
                    final int n = trainer.net.getNumWeights();
                    executedVolume += n * trainer.getIterations();
                    if (trainer.getStopReason() == null) remainingVolume += n * tc;
                    if (j % numKills == 0 && k > 1) tc += trainCyclesInc;
                }
                final long remainingTime = executedVolume > 0
                                           ? startTime + (System.currentTimeMillis() - startTime) * (executedVolume + remainingVolume) / executedVolume
                                           : startTime;
                log.printf("%nEstimated completion time %tF %<tT%n", remainingTime);
            }
        }
        final NNet3L bestNet = trainers.getFirst().net;
        if (log != null) log.printf("%nTRAIN BEST NET %s...%n", bestNet);
        finishTrainer.initialize(bestNet, data);
        finishTrainer.train(0, log);
        return bestNet;
    }
}
