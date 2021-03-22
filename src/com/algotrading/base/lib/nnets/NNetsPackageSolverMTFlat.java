package com.algotrading.base.lib.nnets;

import com.algotrading.base.lib.Rnd;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Класс для многопоточного расчёта пакета нейросетей.
 * <p/>
 * Для запуска этого класса необходимо подготовить папку с файлами данных для расчёта нейросетей.
 * Расчитанные нейросети складываются в подпапку {@link #outDir}. Если в подпапке {@link #outDir}
 * уже присутствует файл с рассчитанной нейросетью, то эта нейросеть повторно не рассчитывается.
 */
public abstract class NNetsPackageSolverMTFlat {

    /**
     * Количество потоков.
     */
    protected int numThreads = Runtime.getRuntime().availableProcessors();
    /**
     * Максимальное количество тренировочных образов.
     */
    protected int trainCapacity = 250 * 1000;
    /**
     * Максимальное количество тестовых образов.
     */
    protected int testCapacity = 250 * 1000;
    /**
     * {@code true}, если в выходам нейросети применяется сигмоид.
     */
    private final boolean hasSigmoidOutput;
    /**
     * Папка, в которой лежат обучающие образы.
     */
    private final File inDir;
    /**
     * Папка, в которую будут выведены результаты расчетов.
     */
    private final File outDir;
    /**
     * Папка, в которую складываются обработанные файлы с обучающими образами.
     */
    public final File preparedDir;

    /**
     * Конструктор.
     *
     * @param solverId         идентификатор вычислительного процесса.
     * @param hasSigmoidOutput {@code true}, если в выходам нейросети применяется сигмоид.
     * @param inDir            папка, в которой лежат обучающие образы.
     * @param outDir           папка, в которую будут выведены результаты расчетов.
     */
    public NNetsPackageSolverMTFlat(final String solverId, final boolean hasSigmoidOutput, final File inDir, final File outDir) {
        this.hasSigmoidOutput = hasSigmoidOutput;
        this.inDir = inDir;
        this.outDir = new File(outDir, solverId);
        preparedDir = new File(outDir, "prepared");
        this.outDir.mkdirs();
        preparedDir.mkdirs();
    }

    /**
     * @param netName название нейросети.
     * @return выходы нейросети, если она оказалась пустая.
     */
    public abstract double[] getZeroOut(final String netName);

    /**
     * Запустить потоки расчёта нейросетей.<br>
     *
     * @throws InterruptedException  если один из потоков прерывается внешним потоком.
     * @throws FileNotFoundException если не удалось открыть файл outDir/_summary.log.
     */
    public void solve() throws InterruptedException, FileNotFoundException {
        try (final PrintStream summaryPs = new PrintStream(new FileOutputStream(new File(outDir, "_summary.log"), true), true, StandardCharsets.UTF_8)) {
            summaryPs.println(" n0  n1 n2 trnSize tstSize    trnE     tstE     ovaE     maxE    gamma     trnV     tstV     ovaV    trnVF    tstVF    ovaVF netName");
            final Thread[] threads = new Thread[numThreads];
            for (int id = 0; id < threads.length; id++) {
                threads[id] = new ComputingThread(id, summaryPs);
                threads[id].start();
                Thread.sleep(1000);
            }
            for (final Thread thread : threads) {
                thread.join();
            }
            summaryPs.println();
        }
    }

    private final class ComputingThread extends Thread {
        private final File workDir;
        private final PrintStream summaryPs;

        private ComputingThread(final int id, final PrintStream summaryPs) {
            super("Computing thread - " + id);
            workDir = new File(outDir, "W" + id);
            workDir.mkdirs();
            this.summaryPs = summaryPs;
        }

        @Override
        public void run() {
            try {
                final NNetMetaTrainer metaTrainer = createMetaTrainer();
                final Rnd rnd = new Rnd();
                final NNetData data = new NNetData(rnd, trainCapacity, testCapacity);
                while (true) {
                    final File[] files = inDir.listFiles();
                    if (files == null) break;

                    File inDataFile = null;
                    String netName = null;
                    for (final File file : files) {
                        netName = getNetName(file);
                        if (netName != null) {
                            inDataFile = file;
                            break;
                        }
                    }
                    if (inDataFile == null) break;

                    final File dataFile = new File(workDir, inDataFile.getName());
                    dataFile.delete();
                    inDataFile.renameTo(dataFile);
                    if (!dataFile.exists()) continue;

                    System.out.println(getName() + ": net " + netName + " solving started.");

                    data.clear();
                    data.load(dataFile);

                    try (final PrintStream log = new PrintStream(new File(outDir.getParent(), netName + ".net.log"), StandardCharsets.UTF_8)) {
                        NNet3L net = loadNet(netName);
                        if (net == null) {
                            net = new NNet3L(data.getNumInputs(), 0, data.getNumOutputs(), hasSigmoidOutput);
                            metaTrainer.train(net, data, getZeroOut(netName), log);
                            System.out.println(getName() + ": net " + netName + " computed.");
                        } else if (net.getNumInnerNeurons() == 0) {
                            metaTrainer.train(net, data, getZeroOut(netName), log);
                            System.out.println(getName() + ": net " + netName + " computed.");
                        } else {
                            metaTrainer.improve(net, data, getZeroOut(netName), log);
                            System.out.println(getName() + ": net " + netName + " updated.");
                        }
                        saveNet(netName, net);
                        synchronized (summaryPs) {
                            final double trnV = data.getTrainVariance();
                            final double tstV = data.getTestVariance();
                            final double ovaV = data.getOverallVariance();
                            final double trnE = data.getTrainError(net);
                            final double tstE = data.getTestError(net);
                            final double ovaE = data.getOverallError(net);
                            final double maxE = Math.max(trnE, tstE);
                            final double gamma = trnE > 0 ? tstE / trnE : 1.0;
                            final double trnVF = trnV > 0.0 ? 100.0 - 100.0 * trnE / trnV : 0.0;
                            final double tstVF = tstV > 0.0 ? 100.0 - 100.0 * tstE / tstV : 0.0;
                            final double ovaVF = ovaV > 0.0 ? 100.0 - 100.0 * ovaE / ovaV : 0.0;
                            summaryPs.printf(Locale.US, "%3d %3d %2d %7d %7d %7.4f  %7.4f  %7.4f  %7.4f  %7.4f  %7.4f  %7.4f  %7.4f %7.2f%% %7.2f%% %7.2f%% %s%n",
                                             net.getNumInputs(), net.getNumInnerNeurons(), net.getNumOutputs(),
                                             data.getTrainSize(), data.getTestSize(),
                                             trnE, tstE, ovaE, maxE, gamma, trnV, tstV, ovaV, trnVF, tstVF, ovaVF, netName);

                        }
                        final File preparedFile = new File(preparedDir, dataFile.getName());
                        preparedFile.delete();
                        dataFile.renameTo(preparedFile);
                    }
                }
            } catch (final IOException e) {
                System.out.println("Thread " + getName() + " STOPPED!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Создать составной тренер нейросетей для вычислительного потока.
     *
     * @return составной тренер нейросетей.
     */
    protected NNetMetaTrainer createMetaTrainer() {
        return new NNetMetaTrainer();
    }

    /**
     * @param dataFile файл с данными.
     * @return имя нейросети или null, если данный файл не содержит данных.
     */
    protected String getNetName(final File dataFile) {
        final String name = dataFile.getName();
        return name.endsWith(".txt")
               ? name.substring(0, name.length() - 4)
               : name;
    }

    /**
     * Попытаться загрузить нейросеть из файла.
     *
     * @param netName имя нейросети.
     * @return загруженная нейросеть или null, если загрузить нейросеть не удалось.
     */
    private NNet3L loadNet(final String netName) {
        final File file = getBinaryNetFile(netName);
        try {
            try (final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                return new NNet3L(dis, hasSigmoidOutput);
            }
        } catch (final FileNotFoundException ignored) {
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Сохранить нейросеть с данным именем.
     *
     * @param netName имя нейросети.
     * @param net     рассчитанная нейросеть.
     * @throws IOException если произошла ошибка записи.
     */
    private void saveNet(final String netName, final NNet3L net) throws IOException {
        final File file = getBinaryNetFile(netName);
        try (final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            net.write(dos);
        }
//        final PrintStream ps = new PrintStream(getTextNetFile(netName), "UTF-8");
//        try {
//            net.print(ps);
//        } finally {
//            ps.close();
//        }
    }

    /**
     * @param netName имя нейросети.
     * @return файл, в который будет записана или уже записана нейросеть.
     */
    protected File getBinaryNetFile(final String netName) {
        return new File(outDir.getParent(), netName + ".net");
    }

    /**
     * @param netName имя нейросети.
     * @return файл, в который будут напечатаны коэффициенты нейросети.
     */
//    protected File getTextNetFile(final String netName) {
//        return new File(outDir.getParent(), netName + ".net.txt");
//    }
}