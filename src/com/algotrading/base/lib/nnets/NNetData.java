package com.algotrading.base.lib.nnets;

import com.algotrading.base.lib.DataFormatException;
import com.algotrading.base.lib.Rnd;
import com.algotrading.base.lib.stat.WStatistic;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Класс, содержащий тренировочные и тестовые образы для обучения нейросетей.
 * <p/>
 * Один образ, читаемый из потока, состоит из входов in, выходов out и, может быть,
 * весового коэффициента w, и представляется массивом типа double[].
 * <p/>
 * Образы можно нормализовать, перенеся константные входные столбцы в конец массива
 * и линейно преобразовав остальные входные столбцы так, чтобы каждый столбец имел
 * нулевое среднее и единичную дисперсию.
 * <p/>
 * Денормализация -- это приведение данных в исходное состояние.
 * <p/>
 * Имеется возможность добавлять новые образы при условии, что данные не являются
 * нормализованными.
 */
public class NNetData {
    /**
     * Минимальное число тренировочных образов.
     */
    public static final int MIN_TRAIN_SIZE = 250;
    /**
     * Датчик случайных чисел.
     */
    private final Rnd rnd;
    /**
     * Циклический массив обучающих образов.
     */
    private final double[][] samples;
    /**
     * Общее количество обучающих образов.
     */
    private int size = 0;
    /**
     * Индекс для нового обучающего образа в массиве {@link #samples}.
     */
    private int current = 0;
    /**
     * Массив тренировочных образов.
     */
    private final double[][] trainSamples;
    /**
     * Число тренировочных обучающих образов.
     */
    private int trainSize = 0;
    /**
     * Массив тестовых образов.
     */
    private final double[][] testSamples;
    /**
     * Число тренировочных обучающих образов.
     */
    private int testSize = 0;
    /**
     * Количество входов в исходных данных, количество неконстантных входов в нормализованных данных.
     */
    private int nI = 0;
    /**
     * Количество константных входов в нормализованных данных или 0 в ненормализованных данных.
     */
    private int nIc = 0;
    /**
     * Количество выходов.
     */
    private int nO = 0;
    /**
     * Количество константных выходов в нормализованных данных или 0 в ненормализованных данных.
     */
    private int nOc = 0;
    /**
     * Сдвиг линейного преобразования неконстантных входов, проиндексированный как ненормализованные данные.
     */
    private double[] a = null;
    /**
     * Множитель линейного преобразования неконстантных входов, проиндексированный как ненормализованные данные.
     * В нормализованных данных используются только те входы, для которых b[i] > 0.
     */
    private double[] b = null;
    /**
     * Дисперсия выходов тренировочных образов; если -1, то еще не вычислялась.
     */
    private double trainVar = -1.0;
    /**
     * Дисперсия выходов тестовых образов; если -1, то еще не вычислялась.
     */
    private double testVar = -1.0;
    /**
     * Дисперсия выходов для всех образов; если -1, то еще не вычислялась.
     */
    private double overallVar = -1.0;
    /**
     * Вспомогательный массив.
     */
    private double[] tmpG = null;

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Конструктор.
     *
     * @param rnd           датчик случайных чисел.
     * @param trainCapacity ёмкость таблицы тренировочных образов.
     * @param testCapacity  ёмкость таблицы тестовых образов.
     */
    public NNetData(final Rnd rnd, final int trainCapacity, final int testCapacity) {
        this.rnd = rnd;
        samples = new double[trainCapacity + testCapacity][];
        trainSamples = new double[trainCapacity][];
        testSamples = new double[testCapacity][];
    }

    /**
     * Удалить все обучающие образы и очистить накопленную статистику по переменным, если она есть.
     */
    public void clear() {
        for (int i = 0; i < size; i++) samples[i] = null;
        size = 0;
        current = 0;
        for (int i = 0; i < trainSize; i++) trainSamples[i] = null;
        trainSize = 0;
        for (int i = 0; i < testSize; i++) testSamples[i] = null;
        testSize = 0;
        nI = 0;
        nIc = 0;
        nO = 0;
        a = null;
        b = null;
        trainVar = -1.0;
        testVar = -1.0;
        overallVar = -1.0;
    }

    /**
     * @return датчик случайных чисел.
     */
    public Rnd getRnd() {
        return rnd;
    }

    /**
     * Получить количество неконстантных входов в нормализованных обучающих образах
     * или количество входов в ненормализованных обучающих образах.
     *
     * @return количество входов или 0, если количество входов не определено.
     */
    public final int getNumInputs() {
        return nI;
    }

    /**
     * Получить количество неконстантных выходов в нормализованных обучающих образах
     * или количество выходов в ненормализованных обучающих образах.
     *
     * @return количество выходов в обучающем образе или 0, если количество входов не определено.
     */
    public final int getNumOutputs() {
        return nO;
    }

    /**
     * @return число тренировочных образов.
     */
    public int getTrainSize() {
        return trainSize;
    }

    /**
     * @param i номер тренировочного обучающего образа от 0 до {@link #getTrainSize()} - 1.
     * @return тренировочный обучающий образ.
     */
    public double[] getTrainSample(final int i) {
        return trainSamples[i];
    }

    /**
     * @return число тестовых образов.
     */
    public int getTestSize() {
        return testSize;
    }

    /**
     * @return число всех обучающих образов.
     */
    public int getSize() {
        return size;
    }

    /**
     * @param i номер обучающего образа от 0 до {@link #getSize()} - 1.
     * @return тренировочный обучающий образ.
     */
    public double[] getSample(final int i) {
        return samples[i];
    }

    /**
     * Добавить ненормализованный обучающий образ.
     * <p/>
     * При добавлении все данные из массива sample копируются. Поэтому этот массив можно использовать повторно.
     * <p/>
     * По первому обучающему образу определяется конфигурация (число входов и выходов) всех остальных образов.
     *
     * @param sample ненормализованный обучающий образ: массив длины не менее (nI + nO), первых nI элементов которого -
     *               это входы нейросети, следующие nO элементов - это выходы нейросети, и, наконец,
     *               элемент с индексом nI + nO (если он есть) - это вес данного обучающего образа.
     * @param nI     количество входов.
     * @param nO     количество выходов.
     * @throws NNetException если конфигурация обучающего образа не совпадает с конфигурацией уже добавленных образов.
     */
    public void addSample(final double[] sample, final int nI, final int nO) throws NNetException {
        if (size == 0) {
            this.nI = nI;
            this.nO = nO;
        } else if (this.nI + nIc != nI || this.nO != nO) {
            throw new NNetException("Sample configuration mismatch: legal configuration is ("
                    + (this.nI + nIc) + ", " + this.nO + "), "
                    + "new sample configuration is (" + nI + ", " + nO + ").");
        }
        double[] curSample = samples[current];
        if (curSample != null) {
            // новые образы затирают старые.
            current++;
        } else {
            // еще не завершен первый цикл заполнения.
            curSample = samples[current++] = new double[nI + nO + 1];
            size = current;
            if (trainSize < MIN_TRAIN_SIZE) {
                trainSamples[trainSize++] = curSample;
            } else {
                final int trainLack = trainSamples.length - trainSize;
                final int testLack = testSamples.length - testSize;
                if (rnd.rnd(trainLack + testLack) < trainLack) {
                    trainSamples[trainSize++] = curSample;
                    trainVar = -1.0;
                } else {
                    testSamples[testSize++] = curSample;
                    testVar = -1.0;
                }
            }
        }
        if (current == samples.length) current = 0;
        if (sample.length >= curSample.length) {
            System.arraycopy(sample, 0, curSample, 0, curSample.length);
        } else {
            System.arraycopy(sample, 0, curSample, 0, curSample.length - 1);
            curSample[nI + nO] = 1.0;
        }
        overallVar = -1.0;
        if (a != null) normalize(curSample);
    }

    /**
     * Прочитать обучающие образы из файла с данным именем.
     * <p/>
     * Образы читаются с конца потока и распределяются между тренировочными и
     * тестовыми пропорционально ёмкостям соответствующих таблиц.
     * <p/>
     * Каждая строка входного потока должна иметь формат:<br>
     * x1 x2 ... xn | y1 ... ym {| v}}.<br>
     * Здесь:<br>
     * {@code x1 x2 ... xn} - входы;<br>
     * {@code y1 y2 ... ym} - выходы;<br>
     * {@code v} - вес данного образа;<br>
     * {@code { }} - все, что указано в фигурных скобках можно опускать.<br>
     * Разделители между столбиками (кроме обязательного '|'): [,;\s]+
     * <p/>
     * Все строки в данном файле должны иметь одинаковую длину.
     * <p/>
     * Если количество входов и выходов еще не определено, то оно определяется автоматически.
     * <p/>
     * Производится проверка на соответствие каждого обучающего образа нужным размерам.
     *
     * @param fileName имя файла.
     * @throws IOException если прочитать образы не удалось.
     */
    public void load(final String fileName) throws IOException {
        load(new File(fileName));
    }

    /**
     * Прочитать обучающие образы из данного файла.
     * <p/>
     * Образы читаются с конца потока и распределяются между тренировочными и
     * тестовыми пропорционально ёмкостям соответствующих таблиц.
     * <p/>
     * Каждая строка входного потока должна иметь формат:<br>
     * x1 x2 ... xn | y1 ... ym {| v}}.<br>
     * Здесь:<br>
     * {@code x1 x2 ... xn} - входы;<br>
     * {@code y1 y2 ... ym} - выходы;<br>
     * {@code v} - вес данного образа;<br>
     * {@code { }} - все, что указано в фигурных скобках можно опускать.<br>
     * Разделители между столбиками (кроме обязательного '|'): [,;\s]+
     * <p/>
     * Все строки в данном файле должны иметь одинаковую длину.
     * <p/>
     * Если количество входов и выходов еще не определено, то оно определяется автоматически.
     * <p/>
     * Производится проверка на соответствие каждого обучающего образа нужным размерам.
     *
     * @param file файл.
     * @throws IOException если прочитать образы не удалось.
     */
    public void load(final File file) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        try {
            br.mark(16384);
            int lineLength = 1;
            for (int c = br.read(); c != -1 && c != '\n'; c = br.read()) {
                lineLength++;
            }
            if (lineLength == 1) return;
            try {
                br.reset();
            } catch (final IOException e) {
                br.close();
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            }
            br.skip(Math.max(0L, file.length() - (long) samples.length * lineLength));
            load(br);
        } finally {
            br.close();
        }
    }

    /**
     * Чтение данных для обучения нейросети.
     * <p/>
     * Образы читаются с конца потока и распределяются между тренировочными и
     * тестовыми пропорционально ёмкостям соответствующих таблиц.
     * <p/>
     * Каждая строка входного потока должна иметь формат:<br>
     * x1 x2 ... xn | y1 ... ym {| v}}.<br>
     * Здесь:<br>
     * {@code x1 x2 ... xn} - входы;<br>
     * {@code y1 y2 ... ym} - выходы;<br>
     * {@code v} - вес данного образа;<br>
     * {@code { }} - все, что указано в фигурных скобках можно опускать.<br>
     * Разделители между столбиками (кроме обязательного '|'): [,;\s]+
     * <p/>
     * Если количество входов и выходов еще не определено, то оно определяется автоматически.
     * <p/>
     * Производится проверка на соответствие каждого обучающего образа нужным размерам.
     *
     * @param br текстовый поток, из которого происходит чтение данных.
     * @throws DataFormatException если формат записанных данных неверный.
     */
    public void load(final BufferedReader br) throws DataFormatException {
        double[] sample = null;
        int ni = 0;
        int no = 0;
        String line = "";
        try {
            while ((line = br.readLine()) != null) {
                if (sample == null) {
                    final String[] parts = line.trim().split("\\s+\\|\\s+");
                    if (parts.length < 2 || parts.length > 3) {
                        throw new DataFormatException("Incorrect data line [" + line + "]");
                    }
                    final String[] in = parts[0].split("[,;\\s]+");
                    final String[] out = parts[1].split("[,;\\s]+");
                    ni = in.length;
                    no = out.length;
                    if (parts.length == 2) {
                        sample = new double[ni + no];
                    } else {
                        sample = new double[ni + no + 1];
                        sample[ni + no] = Double.parseDouble(parts[2]);
                    }
                    for (int i = 0; i < ni; i++) {
                        sample[i] = Double.parseDouble(in[i]);
                    }
                    for (int i = 0; i < no; i++) {
                        sample[ni + i] = Double.parseDouble(out[i]);
                    }
                } else {
                    final String[] parts = line.trim().split("[,;\\s|]+");
                    for (int i = 0; i < sample.length; i++) {
                        sample[i] = Double.parseDouble(parts[i]);
                    }
                }
                boolean isCorrect = true;
                for (final double x : sample) {
                    if (Double.isNaN(x) || Double.isInfinite(x)) {
                        isCorrect = false;
                        break;
                    }
                }
                if (isCorrect) addSample(sample, ni, no);
            }
        } catch (final DataFormatException e) {
            throw e;
        } catch (final Exception e) {
            throw new DataFormatException("Incorrect data line [" + line + "]", e);
        }
    }

    /**
     * Узнать, являются ли данные нормализованными.
     *
     * @return true - данные нормализованы, false - данные ненормализованы.
     */
    protected boolean isNormalized() {
        return (a != null);
    }

    /**
     * Нормализовать данные:<br>
     * 1. Исключить константные входы.<br>
     * 2. Неконстантные входы линейно преобразовать к нулевому математическому ожиданию
     * и единичному стандартному отклонению.
     */
    public void normalize() {
        if (isNormalized() || size < 2) return;
        final int n = nI + nO;
        final WStatistic[] stat = new WStatistic[n];
        final boolean[] isConstant = new boolean[n];
        for (int i = 0; i < n; i++) {
            stat[i] = new WStatistic();
            isConstant[i] = true;
        }
        final double[] sample0 = samples[0];
        for (int k = 0; k < size; k++) {
            final double[] sample = samples[k];
            for (int i = 0; i < n; i++) {
                stat[i].add(sample[i], sample[nI + nO]);
                if (sample[i] != sample0[i]) isConstant[i] = false;
            }
        }
        a = new double[n];
        b = new double[n];
        int i = 0;
        for (; i < nI; i++) { // входы
            final double sd = Math.sqrt(stat[i].getVar());
            if (isConstant[i] || sd <= 0.0) { // константный вход
                a[i] = sample0[i];
                b[i] = 0.0;
                nIc++;
            } else { // неконстантный вход
                a[i] = stat[i].getEv();
                b[i] = sd;
            }
        }
        for (; i < n; i++) { // выходы
            if (isConstant[i]) { // константный выход
                a[i] = sample0[i];
                b[i] = 0.0;
                nOc++;
            } else { // неконстантный выход
                a[i] = stat[i].getEv();
                b[i] = Math.max(0.0001, Math.sqrt(stat[i].getVar()));
            }
        }
        nI -= nIc;
        nO -= nOc;
        for (int k = 0; k < size; k++) {
            normalize(samples[k]);
        }
    }

    /**
     * Нормализовать данный обучающий образ.
     *
     * @param sample обучающий образ.
     */
    private void normalize(final double[] sample) {
        int i = 0;
        int i1 = 0;
        final int ni = nI + nIc;
        for (; i < ni; i++) {
            if (b[i] > 0) {
                sample[i1++] = (sample[i] - a[i]) / b[i];
            }
        }
        final int n = sample.length - 1;
        for (; i < n; i++) {
            if (b[i] > 0) {
                sample[i1++] = sample[i];
            }
        }
        sample[i1] = sample[i]; // вес
    }

    /**
     * Произвести обратное линейное преобразование над неконстантными входами
     * и перенести назад на свои места константные входы.
     */
    public void denormalize() {
        if (isNormalized()) {
            for (int k = 0; k < size; k++) {
                denormalize(samples[k]);
            }
            nI += nIc;
            nIc = 0;
            nO += nOc;
            nOc = 0;
            a = null;
            b = null;
        }
    }

    /**
     * Денормализовать данный обучающий образ.
     *
     * @param sample обучающий образ.
     */
    private void denormalize(final double[] sample) {
        int i = sample.length - 1;
        int i1 = nI + nO;
        sample[i--] = sample[i1--]; // вес
        final int ni = nI + nIc;
        for (; i >= ni; i--) {
            if (b[i] > 0) {
                sample[i] = sample[i1--];
            } else {
                sample[i] = a[i];
            }
        }
        for (; i >= 0; i--) {
            if (b[i] > 0) {
                sample[i] = a[i] + b[i] * sample[i1--];
            } else {
                sample[i] = a[i];
            }
        }
    }

    /**
     * Нормализовать нейросеть.
     * Если обучающие образы нормализованы, то коэффициенты нейросети преобразуются так, чтобы
     * нейросеть работала в пространстве нормализованных переменных.
     *
     * @param net нейросеть.
     */
    public void normalizeNet(final NNet3L net) {
        if (isNormalized()) {
            net.normalize(nI, nO, a, b);
        }
    }

    /**
     * Денормализовать нейросеть.
     * Если обучающие образы нормализованы, то коэффициенты нейросети преобразуются так, чтобы
     * нейросеть работала в пространстве ненормализованных переменных.
     *
     * @param net нейросеть.
     */
    public void denormalizeNet(final NNet3L net) {
        if (isNormalized()) {
            net.denormalize(nI + nIc, nO + nOc, a, b);
        }
    }

    /**
     * Определить оптимальную конфигурацию нейросети.
     *
     * @param neuronsMultiplier поправочный коэффициент на количество нейронов на внутреннем слое.
     * @param maxNeurons        максимальное количество нейронов на внутреннем слое.
     * @param minTrainSize      минимальный объём обучающих образов.
     * @return рекомендуемая конфигурация нейросети или {@code null}, если нет ни одного обучающего
     *         образа или не определены размеры входов и выходов.
     */
    public int[] adviseNetConfiguration(final double neuronsMultiplier, final int maxNeurons, final int minTrainSize) {
        if (size == 0) return null;
        if (trainSize < minTrainSize) return new int[]{nI, 0, nO};
        final double[] p = new double[(int) Math.pow(2, nO)];
        double totalWeight = 0.0;
        for (int i = 0; i < size; i++) {
            final double[] sample = samples[i];
            int id = 0;
            for (int k = 0; k < nO; k++) {
                id *= 2;
                if (sample[nI + k] > 0) id++;
            }
            p[id] += sample[nI + nO];
            totalWeight += sample[nI + nO];
        }

        double entropy = 0.0;
        for (final double aP : p) {
            if (aP > 0) {
                entropy -= aP * Math.log(aP / totalWeight);
            }
        }
        if (entropy <= 0.0) {
            return new int[]{nI, 0, nO};
        } else {
            int n1 = (int) Math.min(2.0 * nI, neuronsMultiplier * Math.log10(1.0 + entropy));
            if (n1 > maxNeurons) n1 = maxNeurons;
            int k = n1 - 2;
            for (int j = 1; j < nO && k > 0; j++, k -= 2) {
                n1 += k;
            }
            return new int[]{nI, n1, nO};
        }
    }

    /**
     * Преобразовать нейросеть так, чтобы она предсказывала усредненный выход на ненормализованных данных.
     * <p/>
     * Этот метод работает только тогда, когда данные нормализованы
     * (при нормализации вычисляются средние значения выходов).
     *
     * @param net нейросеть.
     * @see #normalize
     * @see NNet3L#setSingular
     */
    public void degenerateNet(final NNet3L net) {
        if (isNormalized()) {
            net.setConfigurationTo(nI + nIc, 0, nO + nOc);
            net.setSingular(a, nI + nIc);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Перемешать тренировочные и тестовые образы.
     */
    public void shuffle() {
        if (trainSize < MIN_TRAIN_SIZE) return;
        final int oldTrainSize = trainSize;
        final int oldTestSize = testSize;
        trainSize = 0;
        testSize = 0;
        int i = 0;
        for (; i < MIN_TRAIN_SIZE; i++) {
            final int j = (current + size - i - 1) % size;
            trainSamples[trainSize++] = samples[j];
        }
        for (; i < size; i++) {
            final int j = (current + size - i - 1) % size;
            final int trainLack = trainSamples.length - trainSize;
            final int testLack = testSamples.length - testSize;
            if (rnd.rnd(trainLack + testLack) < trainLack) {
                trainSamples[trainSize++] = samples[j];
            } else {
                testSamples[testSize++] = samples[j];
            }
        }
        for (i = trainSize; i < oldTrainSize; i++) trainSamples[i] = null;
        for (i = testSize; i < oldTestSize; i++) testSamples[i] = null;
        trainVar = -1.0;
        testVar = -1.0;
    }

    /**
     * Вычислить дисперсию тренировочных обучающих образов.
     * Эта дисперсия численно равна среднеквадратической ошибке предсказания
     * константной сети (сети, у которой 0 нейронов на внутреннем слое).
     *
     * @return суммарная дисперсия обучающих выходов.
     */
    public double getTrainVariance() {
        if (trainVar == -1) trainVar = computeVariance(trainSamples, trainSize);
        return trainVar;
    }

    /**
     * Вычислить дисперсию тренировочных обучающих образов.
     * Эта дисперсия численно равна среднеквадратической ошибке предсказания
     * константной сети (сети, у которой 0 нейронов на внутреннем слое).
     *
     * @return суммарная дисперсия обучающих выходов.
     */
    public double getTestVariance() {
        if (testVar == -1) testVar = computeVariance(testSamples, testSize);
        return testVar;
    }

    /**
     * Вычислить дисперсию тренировочных обучающих образов.
     * Эта дисперсия численно равна среднеквадратической ошибке предсказания
     * константной сети (сети, у которой 0 нейронов на внутреннем слое).
     *
     * @return суммарная дисперсия обучающих выходов.
     */
    public double getOverallVariance() {
        if (overallVar == -1) overallVar = computeVariance(samples, size);
        return overallVar;
    }

    /**
     * Вычислить суммарную дисперсию обучающих выходов.
     * Эта дисперсия численно равна среднеквадратической ошибке предсказания
     * константной сети (сети, у которой 0 нейронов на внутреннем слое).
     *
     * @param tSamples массив обучающих образов.
     * @param tSize    количество значащих элементов в массиве.
     * @return суммарная дисперсия обучающих выходов.
     */
    private double computeVariance(final double[][] tSamples, final int tSize) {
        if (tSize < 2) return 0.0;
        double sum2 = 0.0;
        final double[] sum = new double[nO];
        double totalWeight = 0.0;
        for (int k = 0; k < tSize; k++) {
            final double[] sample = tSamples[k];
            final double v = sample[nI + nO];
            totalWeight += v;
            for (int i = 0; i < nO; i++) {
                final double x = sample[nI + i];
                sum2 += v * x * x;
                sum[i] += v * x;
            }
        }
        if (totalWeight <= 0.0) {
            return 0;
        } else {
            double s = 0.0;
            for (int i = 0; i < nO; i++) {
                final double sumi = sum[i];
                s += sumi * sumi;
            }
            return 0.5 * (sum2 - s / totalWeight) / (totalWeight * nO);
        }
    }

    /**
     * Вычислить ошибку по тренировочным данным.
     *
     * @param net нейросеть, число входов которой равно {@link #getNumInputs()}, а выходов - {@link #getNumOutputs()}.
     * @return среднеквадратическая ошибка для тренировочных данных в расчёте на единицу массы и один выход.
     */
    public final double getTrainError(final NNet3L net) {
        return computeError(net, trainSamples, trainSize);
    }

    /**
     * Вычислить ошибку по тестовым данным.
     *
     * @param net нейросеть, число входов которой равно {@link #getNumInputs()}, а выходов - {@link #getNumOutputs()}.
     * @return среднеквадратическая ошибка для тестовых данных в расчёте на единицу массы и один выход.
     */
    public final double getTestError(final NNet3L net) {
        return computeError(net, testSamples, testSize);
    }

    /**
     * Вычислить ошибку по всем обучающим образам.
     *
     * @param net нейросеть, число входов которой равно {@link #getNumInputs()}, а выходов - {@link #getNumOutputs()}.
     * @return среднеквадратическая ошибка для всего набора данных в расчёте на единицу массы и один выход.
     */
    public final double getOverallError(final NNet3L net) {
        return computeError(net, samples, size);
    }

    /**
     * Вычислить ошибку.
     *
     * @param net      нейросеть, число входов которой равно {@link #getNumInputs()}, а выходов - {@link #getNumOutputs()}.
     * @param tSamples массив обучающих образов.
     * @param tSize    количество значащих элементов в массиве.
     * @return среднеквадратическая ошибка для тренировочных данных в расчёте на единицу массы и один выход.
     */
    private double computeError(final NNet3L net, final double[][] tSamples, final int tSize) {
        double e = 0.0;
        double totalWeight = 0.0;
        for (int k = 0; k < tSize; k++) {
            final double[] sample = tSamples[k];
            final double v = sample[nI + nO];
            e += v * net.computeError(sample);
            totalWeight += v;
        }
        return totalWeight > 0 ? 0.5 * e / (totalWeight * nO) : 0.0;
    }

    /**
     * Вычислить градиент штрафной функции по тренировочным данным.
     *
     * @param net нейросеть, число входов которой равно {@link #getNumInputs()}, а выходов - {@link #getNumOutputs()}.
     * @param g   массив длины не менее {@link NNet3L#getNumWeights()}, в который будет записан градиент функции ошибки.
     */
    public final void computeTrainGradient(final NNet3L net, final double[] g) {
        computeGradient(net, trainSamples, trainSize, g);
    }

    /**
     * Вычислить градиент штрафной функции.
     *
     * @param net      нейросеть, число входов которой равно {@link #getNumInputs()}, а выходов - {@link #getNumOutputs()}.
     * @param tSamples массив обучающих образов.
     * @param tSize    количество значащих элементов в массиве.
     * @param g        массив длины не менее {@link NNet3L#getNumWeights()}, в который будет записан градиент функции ошибки.
     */
    private void computeGradient(final NNet3L net, final double[][] tSamples, final int tSize, final double[] g) {
        final int numWeights = net.getNumWeights();
        for (int i = 0; i < numWeights; i++) g[i] = 0.0;
        double totalWeight = 0.0;
        for (int k = 0; k < tSize; k++) {
            final double[] sample = tSamples[k];
            net.computeGradient(sample, g);
            totalWeight += sample[nI + nO];
        }
        final double m = (totalWeight <= 0) ? 0.0 : 1.0 / (totalWeight * nO);
        for (int i = 0; i < numWeights; i++) g[i] *= m;
    }

    /**
     * Вычислить норму градиента штрафной функции по тренировочным данным.
     *
     * @param net нейросеть, число входов которой равно {@link #getNumInputs()}, а выходов - {@link #getNumOutputs()}.
     * @return норма градиента штрафной функции по тренировочным данным.
     */
    public final double getTrainGradientNorm(final NNet3L net) {
        final int numWeights = net.getNumWeights();
        if (tmpG == null || tmpG.length < numWeights) tmpG = new double[numWeights];
        computeGradient(net, trainSamples, trainSize, tmpG);
        return computeNorm(tmpG, numWeights);
    }

    /**
     * Вычислить норму градиента штрафной функции по тестовым данным.
     *
     * @param net нейросеть, число входов которой равно {@link #getNumInputs()}, а выходов - {@link #getNumOutputs()}.
     * @return норма градиента штрафной функции по тестовым данным.
     */
    public final double getTestGradientNorm(final NNet3L net) {
        final int numWeights = net.getNumWeights();
        if (tmpG == null || tmpG.length < numWeights) tmpG = new double[numWeights];
        computeGradient(net, testSamples, testSize, tmpG);
        return computeNorm(tmpG, numWeights);
    }

    /**
     * Вычислить норму градиента штрафной функции по всем обучающим образам.
     *
     * @param net нейросеть, число входов которой равно {@link #getNumInputs()}, а выходов - {@link #getNumOutputs()}.
     * @return норма градиента штрафной функции по всем обучающим образам.
     */
    public final double getOverallGradientNorm(final NNet3L net) {
        final int numWeights = net.getNumWeights();
        if (tmpG == null || tmpG.length < numWeights) tmpG = new double[numWeights];
        computeGradient(net, samples, size, tmpG);
        return computeNorm(tmpG, numWeights);
    }

    /**
     * Вычислить норму данного вектора.
     *
     * @param g   вектор.
     * @param len количество значащих элементов в векторе.
     * @return норма вектора.
     */
    public double computeNorm(final double[] g, final int len) {
        double norm = 0.0;
        for (int i = 0; i < len; i++) {
            final double gi = g[i];
            norm += gi * gi;
        }
        return Math.sqrt(norm / len);
    }

    /**
     * Вспомогательный массив для определения "мертвых" внутренних нейронов.
     */
    private double[] min = null;
    /**
     * Вспомогательный массив для определения "мертвых" внутренних нейронов.
     */
    private double[] max = null;
    /**
     * Вспомогательный массив для определения "мертвых" внутренних нейронов.
     */
    private double[] wijy1j = null;

    /**
     * Встряхнуть "мертвые" внутренние нейроны. Нейрон считается мертвым, если выход из него практически постоянный.
     *
     * @param net нейросеть.
     * @return {@code true} если хотя бы один мертвый нейрон был найден.
     */
    public boolean deleteDeadInnerNeurons(final NNet3L net) {
        if (size <= 100 || !isNormalized()) return false;
        final int n1 = net.getNumInnerNeurons();
        if (min == null || min.length < n1 * nO) {
            min = new double[n1 * nO];
            max = new double[n1 * nO];
        }
        if (wijy1j == null || wijy1j.length < nO) {
            wijy1j = new double[nO];
        }
        for (int j = 0; j < n1; j++) {
            min[j] = Double.MAX_VALUE;
            max[j] = -Double.MAX_VALUE;
        }
        for (int s = 0; s < size; s++) {
            net.computeMinMaxInnerNeuronsValues(samples[s], min, max);
        }
        boolean hasDeadNeurons = false;
        for (int j = n1 - 1; j >= 0; j--) {
            boolean isConstant = true;
            for (int i = 0, mij = j, bi = nI + nIc; i < nO; i++, mij += n1, bi++) {
                while (b[bi] == 0.0) bi++;
                final double d = max[mij] - min[mij];
                wijy1j[i] = 0.5 * (max[mij] + min[mij]);
                if (d > 0.002 * b[bi]) {
                    isConstant = false;
                    break;
                }
            }
            if (isConstant) {
                net.deleteInnerNeuron(j, wijy1j);
                hasDeadNeurons = true;
            }
        }
        return hasDeadNeurons;
    }
}