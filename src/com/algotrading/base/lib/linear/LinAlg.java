package com.algotrading.base.lib.linear;

import java.util.Arrays;

/**
 * Пакет для работы с векторами и прямоугольными матрицами.
 * <p/>
 * Векторы реализуются на базе типа double[] и считаются векторами-столбцами,
 * если только специально не указано, что вектор является вектором-строкой.
 * <p/>
 * Матрицы реализуются на базе типа double[][], где первый индекс задаёт
 * номер строки, а второй -- номер столбца. Если имеется матрица
 * <code>double[][] A</code>, то её размеры определяются следующим
 * образом: A.length - число строк, A[0].length - число столбцов.
 * Контроль того, что матрица реально прямоугольная, не производится.
 */
@SuppressWarnings({"MethodParameterNamingConvention", "LocalVariableNamingConvention", "NewMethodNamingConvention"})
public class LinAlg {

    /**
     * Получить копию данного вектора.
     *
     * @param u вектор
     * @return копия вектора
     */
    public static double[] copy(final double[] u) {
        return Arrays.copyOf(u, u.length);
    }

    /**
     * Получить копию данной матрицы.
     *
     * @param A матрица
     * @return копия матрицы
     */
    public static double[][] copy(final double[][] A) {
        final double[][] B = new double[A.length][];
        for (int i = 0; i < B.length; i++) {
            B[i] = copy(A[i]);
        }
        return B;
    }

    /**
     * Скопировать данные из первого вектора во второй.
     *
     * @param src  вектор
     * @param dest вектор
     */
    public static void vectorcopy(final double[] src, final double[] dest) {
        if (src.length != dest.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        System.arraycopy(src, 0, dest, 0, src.length);
    }

    /**
     * Скопировать данные из первой матрицы во вторую.
     *
     * @param src  матрица
     * @param dest матрица
     */
    public static void matrixcopy(final double[][] src, final double[][] dest) {
        if (src.length != dest.length) {
            throw new IllegalArgumentException("Size mismath!");
        }
        for (int i = 0; i < src.length; i++) {
            vectorcopy(src[i], dest[i]);
        }
    }

    /**
     * Преобразовать вектор в матрицу-столбец.
     *
     * @param v вектор
     * @return матрица-столбец
     */
    public static double[][] toMatrix(final double[] v) {
        final double[][] V = new double[v.length][1];
        for (int i = 0; i < v.length; i++) {
            V[i][0] = v[i];
        }
        return V;
    }

    /**
     * Удалить из вектора элемент.
     *
     * @param u вектор
     * @param i номер удаляемого элемента
     * @return вектор меньшего размера
     */
    public static double[] deleteElement(final double[] u, final int i) {
        if (i < 0 || i >= u.length) {
            throw new IllegalArgumentException("No such an element!");
        }
        final double[] v = new double[u.length - 1];
        System.arraycopy(u, 0, v, 0, i);
        System.arraycopy(u, i + 1, v, i, u.length - i - 1);
        return v;
    }

    /**
     * Вставить нулевой элемент перед заданным элементов вектора.
     *
     * @param u вектор
     * @param i номер элемента, перед которым будет сделана вставка
     * @return вектор большего размера
     */
    public static double[] insertElement(final double[] u, int i) {
        if (i < 0) {
            i = 0;
        }
        if (i > u.length) {
            i = u.length;
        }
        final double[] v = new double[u.length + 1];
        System.arraycopy(u, 0, v, 0, i);
        System.arraycopy(u, i, v, i + 1, u.length - i);
        return v;
    }

    /**
     * Удалить строку из матрицы. Возвращаемая матрица
     * состоит из тех же строк, что и исходная.
     *
     * @param A матрица
     * @param i номер удаляемой строки
     * @return матрица меньшего размера
     */
    public static double[][] deleteRow(final double[][] A, final int i) {
        if (i < 0 || i >= A.length) {
            throw new IllegalArgumentException("No such a row!");
        }
        final double[][] B = new double[A.length - 1][];
        System.arraycopy(A, 0, B, 0, i);
        System.arraycopy(A, i + 1, B, i, A.length - i - 1);
        return B;
    }

    /**
     * Вставить нулевую строку в матрицу. Возращаемая матрица
     * состоит из тех же строк, что и исходная.
     *
     * @param A матрица
     * @param i номер строки, перед которой будет добавлена нулевая строка
     * @return матрица большего размера
     */
    public static double[][] insertRow(final double[][] A, int i) {
        if (i < 0) {
            i = 0;
        }
        if (i > A.length) {
            i = A.length;
        }
        final double[][] B = new double[A.length + 1][];
        System.arraycopy(A, 0, B, 0, i);
        B[i] = new double[A[0].length];
        System.arraycopy(A, i, B, i + 1, A.length - i);
        return B;
    }

    /**
     * Удалить столбец из матрицы.
     *
     * @param A матрица
     * @param j номер удаляемого столбца
     * @return матрица меньшего размера
     */
    public static double[][] deleteColumn(final double[][] A, final int j) {
        for (int i = 0; i < A.length; i++) {
            A[i] = deleteElement(A[i], j);
        }
        return A;
    }

    /**
     * Вставить столбец в матрицу.
     *
     * @param A матрица
     * @param j номер столбца, перед которым будет добавлен нулевой столбец
     * @return матрица большего размера
     */
    public static double[][] insertColumn(final double[][] A, final int j) {
        for (int i = 0; i < A.length; i++) {
            A[i] = insertElement(A[i], j);
        }
        return A;
    }

    /**
     * Получить единичную матрицу.
     *
     * @param size размер
     * @return единичная матрица
     */
    public static double[][] I(final int size) {
        return scalar(size, 1.0);
    }

    /**
     * Получить скалярную матрицу.
     *
     * @param size размер
     * @param s    число по диагонали
     * @return скалярная матрица
     */
    public static double[][] scalar(final int size, final double s) {
        final double[][] A = new double[size][size];
        for (int i = 0; i < size; i++) {
            A[i][i] = s;
        }
        return A;
    }

    /**
     * Получить диагональную матрицу.
     *
     * @param v вектор, задающий диагональ матрицы
     * @return диагональная матрица
     */
    public static double[][] diag(final double[] v) {
        final double[][] A = new double[v.length][v.length];
        int i = 0;
        while (i < v.length) {
            A[i][i] = v[i];
            i++;
        }
        return A;
    }

    /**
     * Вычислить сумму векторов.
     *
     * @param u вектор
     * @param v вектор
     * @return сумма
     */
    public static double[] plus(final double[] u, final double[] v) {
        if (u.length != v.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        final double[] w = new double[u.length];
        for (int i = 0; i < w.length; i++) {
            w[i] = u[i] + v[i];
        }
        return w;
    }

    /**
     * Вычислить сумму матриц.
     *
     * @param A матрица
     * @param B матрица
     * @return сумма
     */
    public static double[][] plus(final double[][] A, final double[][] B) {
        if (A.length != B.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        final double[][] C = new double[A.length][];
        for (int i = 0; i < A.length; i++) {
            C[i] = plus(A[i], B[i]);
        }
        return C;
    }

    /**
     * Добавить второй вектор к первому.
     *
     * @param u первый вектор
     * @param v второй вектор
     */
    public static void plusEquals(final double[] u, final double[] v) {
        if (u.length != v.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        for (int i = 0; i < u.length; i++) {
            u[i] += v[i];
        }
    }

    /**
     * Добавить вторую матрицу к первой.
     *
     * @param A первая матрица
     * @param B вторая матрица
     */
    public static void plusEquals(final double[][] A, final double[][] B) {
        if (A.length != B.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        for (int i = 0; i < A.length; i++) {
            plusEquals(A[i], B[i]);
        }
    }

    /**
     * Вычислить разность векторов.
     *
     * @param u вектор
     * @param v вектор
     * @return разность
     */
    public static double[] minus(final double[] u, final double[] v) {
        if (u.length != v.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        final double[] w = new double[u.length];
        for (int i = 0; i < w.length; i++) {
            w[i] = u[i] - v[i];
        }
        return w;
    }

    /**
     * Вычислить разность матриц.
     *
     * @param A матрица
     * @param B матрица
     * @return разность
     */
    public static double[][] minus(final double[][] A, final double[][] B) {
        if (A.length != B.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        final double[][] C = new double[A.length][];
        for (int i = 0; i < A.length; i++) {
            C[i] = minus(A[i], B[i]);
        }
        return C;
    }

    /**
     * Вычесть второй вектор из первого.
     *
     * @param u первый вектор
     * @param v второй вектор
     */
    public static void minusEquals(final double[] u, final double[] v) {
        if (u.length != v.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        for (int i = 0; i < u.length; i++) {
            u[i] -= v[i];
        }
    }

    /**
     * Вычесть вторую матрицу из первой.
     *
     * @param A первая матрица
     * @param B вторая матрица
     */
    public static void minusEquals(final double[][] A, final double[][] B) {
        if (A.length != B.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        for (int i = 0; i < A.length; i++) {
            minusEquals(A[i], B[i]);
        }
    }

    /**
     * Вычислить скалярное произведение векторов.
     *
     * @param u вектор
     * @param v вектор
     * @return скалярное произведение
     */
    public static double times(final double[] u, final double[] v) {
        if (u.length != v.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        double s = 0;
        for (int i = 0; i < u.length; i++) {
            s += u[i] * v[i];
        }
        return s;
    }

    /**
     * Перемножить две матрицы.
     *
     * @param A первая матрица
     * @param B вторая матрица
     * @return произведение
     */
    public static double[][] times(final double[][] A, final double[][] B) {
        if (A[0].length != B.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        final double[][] C = new double[A.length][B[0].length];
        for (int i = 0; i < C.length; i++) {
            final double[] Ai = A[i];
            final double[] Ci = C[i];
            for (int j = 0; j < Ci.length; j++) {
                for (int k = 0; k < Ai.length; k++) {
                    Ci[j] += Ai[k] * B[k][j];
                }
            }
        }
        return C;
    }

    /**
     * Умножить матрицу на вектор.
     *
     * @param A матрица
     * @param u вектор
     * @return вектор
     */
    public static double[] times(final double[][] A, final double[] u) {
        if (A[0].length != u.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        final double[] v = new double[A.length];
        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < u.length; j++) {
                v[i] += A[i][j] * u[j];
            }
        }
        return v;
    }

    /**
     * Умножить вектор-строку на матрицу.
     *
     * @param u вектор-строка
     * @param A матрица
     * @return вектор-строка
     */
    public static double[] times(final double[] u, final double[][] A) {
        if (u.length != A.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        final double[] v = new double[A[0].length];
        for (int j = 0; j < A[0].length; j++) {
            for (int i = 0; i < u.length; i++) {
                v[j] += u[i] * A[i][j];
            }
        }
        return v;
    }

    /**
     * Умножить вектор на константу.
     *
     * @param c константа
     * @param u вектор
     * @return вектор
     */
    public static double[] times(final double c, final double[] u) {
        final double[] v = new double[u.length];
        for (int i = 0; i < u.length; i++) {
            v[i] = c * u[i];
        }
        return v;
    }

    /**
     * Умножить вектор на константу.
     *
     * @param u вектор
     * @param c константа
     */
    public static void timesEquals(final double[] u, final double c) {
        for (int i = 0; i < u.length; i++) {
            u[i] *= c;
        }
    }

    /**
     * Умножить матрицу на константу.
     *
     * @param c константа
     * @param A матрица
     * @return матрица
     */
    public static double[][] times(final double c, final double[][] A) {
        final double[][] B = new double[A.length][];
        for (int i = 0; i < A.length; i++) {
            B[i] = times(c, A[i]);
        }
        return B;
    }

    /**
     * Умножить матрицу на константу.
     *
     * @param A матрица
     * @param c константа
     */
    public static void timesEquals(final double[][] A, final double c) {
        for (final double[] Ai : A) {
            timesEquals(Ai, c);
        }
    }

    /**
     * Вычислить произведение вектора-строки на матрицу и на вектор-столбец.
     * @param u вектор-строка
     * @param A матрица
     * @param v вектор-столбец
     * @return значение произведения
     */
    public static double uTAv(final double[] u, final double[][] A, final double[] v) {
        if (u.length != A.length || A[0].length != v.length) {
            throw new IllegalArgumentException("Size mismatch!");
        }
        double s = 0;
        for (int i = 0; i < u.length; i++) {
            final double[] Ai = A[i];
            for (int j = 0; j < v.length; j++) {
                s += Ai[j] * u[i] * v[j];
            }
        }
        return s;
    }

    /**
     * Для данной марицы вычислить произведение транспонированной матрицы на данную.
     * @param A матрица
     * @return произведение транспонированной матрицы на данную
     */
    public static double[][] ATA(final double[][] A) {
        final int size = A[0].length;
        final double[][] B = new double[size][size];
        for (int i = 0; i < size; i++) {
            final double[] Bi = B[i];
            for (int j = i; j < size; j++) {
                for (final double[] Ak : A) {
                    Bi[j] += Ak[i] * Ak[j];
                }
                B[j][i] = Bi[j];
            }
        }
        return B;
    }

    /**
     * Транспонировать матрицу.
     *
     * @param A матрица
     * @return транспонированная матрица
     */
    public static double[][] transpose(final double[][] A) {
        final double[][] B = new double[A[0].length][A.length];
        for (int i = 0; i < A.length; i++) {
            final double[] Ai = A[i];
            for (int j = 0; j < Ai.length; j++) {
                B[j][i] = Ai[j];
            }
        }
        return B;
    }

    /**
     * Транспонировать квадратную матрицу.
     * Результат записать в исходную матрицу.
     *
     * @param A матрица
     */
    public static void transposeEquals(final double[][] A) {
        if (A.length != A[0].length) {
            throw new IllegalArgumentException("Matrix is not square!");
        }
        for (int i = 0; i < A.length - 1; i++) {
            final double[] Ai = A[i];
            for (int j = i + 1; j < A.length; j++) {
                final double t = Ai[j];
                Ai[j] = A[j][i];
                A[j][i] = t;
            }
        }
    }

    /**
     * Вычислить евклидову норму вектора.
     *
     * @param u вектор
     * @return евклидова норма
     */
    public static double norm2(final double[] u) {
        double sum = 0;
        for (final double x : u) {
            sum += x * x;
        }
        return Math.sqrt(sum);
    }

    /**
     * Вычислить след матрицы.
     *
     * @param A матрица
     * @return след
     */
    public static double trace(final double[][] A) {
        double t = 0;
        for (int i = 0; i < Math.min(A.length, A[0].length); i++) {
            t += A[i][i];
        }
        return t;
    }

    /**
     * Привести квадратную матрицу к верхне-треугольному виду методом отражений,
     * параллельно осуществляя те же самые преобразования с дополнительной матрицей.
     * В случае вырожденной матрицы выдаётся исключение IllegalStateException.
     * В результате верхний треугольник матрицы и возвращаемый массив содержат
     * верхне-треугольную мартицу, а диагональ и нижний треугольник задают вектора,
     * использованные при отражениях.
     * TODO: разобраться с сингулярностью
     *
     * @param A преобразуемая матрица
     * @param B дополнительная матрица (может быть null)
     * @return вектор из диагональных элементов матрицы, приведённой к верхне-треугольному виду
     */
    private static double[] qr(final double[][] A, final double[][] B) {
        final int nRow = A.length;
        final int nCol = A[0].length;
        if (nRow != nCol) {
            throw new IllegalArgumentException("Matrix is not square!");
        }
        if (B != null && B.length != nRow) {
            throw new IllegalArgumentException("Number of rows in supplementary matrix mismatch!");
        }
        final double[] d = new double[nRow];
        for (int j = 0; j < nCol; j++) {
            // Вычисляем вектор, задающий отражение
            double s = 0;
            for (int i = j + 1; i < nRow; i++) {
                s += A[i][j] * A[i][j];
            }
            final double na = Math.sqrt(A[j][j] * A[j][j] + s);
            if (na == 0) {
                throw new IllegalStateException("Matrix singularity encountered!");
            }
            d[j] = na;
            A[j][j] -= na;
            final double nx = Math.sqrt(A[j][j] * A[j][j] + s);
            if (nx > 0) {
                for (int i = j; i < nRow; i++) {
                    A[i][j] /= nx;
                }
            }
            // Производим отражение для матрицы
            for (int k = j + 1; k < nCol; k++) {
                double xy = 0;
                for (int i = j; i < nRow; i++) {
                    xy += A[i][j] * A[i][k];
                }
                xy *= 2;
                for (int i = j; i < nRow; i++) {
                    A[i][k] -= A[i][j] * xy;
                }
            }
            // Производим отражение для дополнительной матрицы
            if (B != null) {
                for (int k = 0; k < B[0].length; k++) {
                    double xy = 0;
                    for (int i = j; i < nRow; i++) {
                        xy += A[i][j] * B[i][k];
                    }
                    xy *= 2;
                    for (int i = j; i < nRow; i++) {
                        B[i][k] -= A[i][j] * xy;
                    }
                }
            }
        }
        return d;
    }

    /**
     * Выполнить обратный ход метода Гаусса для матрицы, получившейся после приведения
     * к верхне-треугольному виду, и дополнительной матрицы.
     *
     * @param qr массив, полученный в результате исполнения метода qr
     * @param A  преобразуемая матрица, полученая в результате исполнения метода qr
     * @param B  дополнительная матрица
     */
    private static void reverseGauss(final double[] qr, final double[][] A, final double[][] B) {
        final int nRow = A.length;
        final int nCol = A[0].length;
        if (nRow != nCol) {
            throw new IllegalStateException("Matrix is not square!");
        }
        if (B.length != nRow) {
            throw new IllegalArgumentException("Number of rows in supplementary matrix mismatch!");
        }
        if (qr.length != nRow) {
            throw new IllegalArgumentException("Auxiliary qr-vector is invalid!");
        }
        for (int j = 0; j < B[0].length; j++) {
            for (int i = nRow - 1; i >= 0; i--) {
                for (int k = i + 1; k < nCol; k++) {
                    B[i][j] -= A[i][k] * B[k][j];
                }
                B[i][j] /= qr[i];
            }
        }
    }

    /**
     * Решить систему уравнений Ax = b. При этом матрица A
     * изменяется, а результаты решения записываются в вектор b.
     * В случае вычислительных проблем выдаётся исключение.
     *
     * @param A матрица коэффициентов
     * @param b вектор свободных членов, в который будет записано решение системы
     */
    public static void solve(final double[][] A, final double[] b) {
        final double[][] B = toMatrix(b);
        solve(A, B);
        for (int i = 0; i < b.length; i++) {
            b[i] = B[i][0];
        }
    }

    /**
     * Решить совокупность систем уравнений Ax = B. При этом матрица A
     * изменяется, а результаты решения записываются в матрицу B.
     * В случае вычислительных проблем выдаётся исключение.
     *
     * @param A матрица коэффициентов
     * @param B матрица свободных членов, в которую будут записаны решения систем
     */
    public static void solve(final double[][] A, final double[][] B) {
        if (A.length != A[0].length) {
            throw new IllegalStateException("Matrix is not square!");
        }
        if (A.length != B.length) {
            throw new IllegalArgumentException("Number of rows in supplementary matrix mismatch!");
        }
        final double[] qr = qr(A, B);
        reverseGauss(qr, A, B);
    }

    /**
     * Обратить данную матрицу.
     *
     * @param A обращаемая матрица
     */
    public static void invert(final double[][] A) {
        final double[][] B = I(A.length);
        solve(A, B);
        matrixcopy(B, A);
    }

    /**
     * Вычислить модуль определителя квадратной матрицы.
     * Матрица при этом будет изменена!
     *
     * @param A матрица (изменится после вычислений)
     * @return модуль определителя
     */
    public static double absDet(final double[][] A) {
        final double[] qr = qr(A, null);
        double det = 1.0;
        for (final double a : qr) {
            det *= Math.abs(a);
        }
        return det;
    }

    /**
     * Вычислить логарифм модуля определителя квадратной матрицы.
     * Матрица при этом будет изменена!
     *
     * @param A матрица (изменится после вычислений)
     * @return модуль определителя
     */
    public static double logAbsDet(final double[][] A) {
        final double[] qr = qr(A, null);
        double logDet = 0;
        for (final double a : qr) {
            logDet += Math.log(Math.abs(a));
        }
        return logDet;
    }
}
