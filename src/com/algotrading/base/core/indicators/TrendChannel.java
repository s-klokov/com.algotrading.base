package com.algotrading.base.core.indicators;

import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;
import com.algotrading.base.core.series.FinSeries;
import com.algotrading.base.core.window.WindowOfDouble;
import com.algotrading.base.lib.fits.LinearTrendFit;

/**
 * Вычисление индикаторов TrendChannel.
 * <p>
 * В окно заданной длины вписывается канал линейной регрессии и вычисляются его параметры:<br>
 * 1) наклон канала (channelSlope);<br>
 * 2) ширина канала (channelWidth);<br>
 * 3) отклонение цены закрытия от линии регрессии (deviation);<br>
 * 4) рейтинг силы тренда (trendStrength);<br>
 * 5) рейтинг уклонения цены закрытия от линии регрессии (contraStrength).
 * <p>
 * Для показателя trendStrength знак +/- соответствует растущему/падающему тренду.
 * Для показателя contraStrength знак +/- соответствует уклонению выше/ниже линии регрессии.
 */
public final class TrendChannel {

    private TrendChannel() {
    }

    /**
     * Оценить параметры линейного канала и записать их в колонки с указанными именами.
     * Используются параметры свечей O, H, L, C и, возможно, объёмы V.
     * Если в качестве имени рассчитываемой колонки указать {@code null}, колонка не будет создаваться.
     *
     * @param series              временной ряд с колонками O, H, L, C и, возможно, V
     * @param period              длина окна для построения канала
     * @param useVolumes          {@code true}, если нужно использовать объёмы в качестве весовых коэффициентов
     * @param aColumnName         название колонки, куда будут записаны значения свободного члена канала;
     *                            для свечей с индексом меньше period - 1 используется {@link Double#NaN}
     * @param bColumnName         название колонки, куда будут записаны значения наклона канала;
     *                            для свечей с индексом меньше period - 1 используется {@link Double#NaN}
     * @param widthColumnName     название колонки, куда будут записаны значения ширины канала;
     *                            для свечей с индексом меньше period - 1 используется {@link Double#NaN}
     * @param deviationColumnName название колонки, куда будут записаны значения разности между ценой закрытия и
     *                            средней линией канала;
     *                            для свечей с индексом меньше period - 1 используется {@link Double#NaN}
     */
    public static void estimateChannel(final FinSeries series,
                                       final int period,
                                       final boolean useVolumes,
                                       final String aColumnName,
                                       final String bColumnName,
                                       final String widthColumnName,
                                       final String deviationColumnName) {
        final DoubleColumn aColumn = (aColumnName == null) ? null : series.acquireDoubleColumn(aColumnName);
        final DoubleColumn bColumn = (bColumnName == null) ? null : series.acquireDoubleColumn(bColumnName);
        final DoubleColumn widthColumn = (widthColumnName == null) ? null : series.acquireDoubleColumn(widthColumnName);
        final DoubleColumn deviationColumn = (deviationColumnName == null) ? null : series.acquireDoubleColumn(deviationColumnName);

        final double[] x = new double[3 * period];
        final double[] y = new double[3 * period];
        final double[] vol = useVolumes ? new double[3 * period] : null;

        for (int i = 0, k = -x.length + 1; i < x.length; i++, k++) {
            x[i] = k;
        }

        final DoubleColumn high = series.high();
        final DoubleColumn low = series.low();
        final DoubleColumn close = series.close();
        final LongColumn volume = useVolumes ? series.volume() : null;
        final int len = close.length();

        for (int i = 0; i < len; i++) {
            if (i < period - 1) {
                if (aColumn != null) {
                    aColumn.set(i, Double.NaN);
                }
                if (bColumn != null) {
                    bColumn.set(i, Double.NaN);
                }
                if (widthColumn != null) {
                    widthColumn.set(i, Double.NaN);
                }
                if (deviationColumn != null) {
                    deviationColumn.set(i, Double.NaN);
                }
                continue;
            }
            for (int j = i - period + 1, k = 0; j <= i; j++) {
                final double cPrev = (j == 0) ? series.open().get(0) : close.get(j - 1);
                final double h = high.get(j);
                final double l = low.get(j);
                final double c = close.get(j);
                if (c >= cPrev) {
                    y[k++] = l;
                    y[k++] = h;
                } else {
                    y[k++] = h;
                    y[k++] = l;
                }
                y[k++] = c;
                if (useVolumes) {
                    vol[k - 1] = vol[k - 2] = vol[k - 3] = volume.get(j) / 3.0;
                }
            }
            final LinearTrendFit fit = useVolumes ? LinearTrendFit.fit(x, y, vol) : LinearTrendFit.fit(x, y);
            if (aColumn != null) {
                aColumn.set(i, fit.a);
            }
            if (bColumn != null) {
                bColumn.set(i, fit.b);
            }
            if (widthColumn != null) {
                double sd = 0;
                double sw = 0;
                for (int k = 0; k < y.length; k++) {
                    final double w = useVolumes ? vol[k] : 1;
                    sw += w;
                    sd += w * Math.abs(y[k] - fit.a - fit.b * x[k]);
                }
                widthColumn.set(i, sd / sw);
            }
            if (deviationColumn != null) {
                deviationColumn.set(i, y[y.length - 1] - fit.a);
            }
        }
    }

    /**
     * Оценить параметры линейного канала и записать их в колонки с указанными именами.
     * Используются данные указанной колонки с ценами и, возможно, объёмы V.
     * Если в качестве имени рассчитываемой колонки указать {@code null}, колонка не будет создаваться.
     *
     * @param series              временной ряд
     * @param priceColumnName     имя колонки с ценой
     * @param period              длина окна для построения канала
     * @param useVolumes          {@code true}, если нужно использовать объёмы в качестве весовых коэффициентов
     * @param aColumnName         название колонки, куда будут записаны значения свободного члена канала;
     *                            для свечей с индексом меньше period - 1 используется {@link Double#NaN}
     * @param bColumnName         название колонки, куда будут записаны значения наклона канала;
     *                            для свечей с индексом меньше period - 1 используется {@link Double#NaN}
     * @param widthColumnName     название колонки, куда будут записаны значения ширины канала;
     *                            для свечей с индексом меньше period - 1 используется {@link Double#NaN}
     * @param deviationColumnName название колонки, куда будут записаны значения разности между ценой закрытия и
     *                            средней линией канала;
     *                            для свечей с индексом меньше period - 1 используется {@link Double#NaN}
     */
    public static void estimateChannel(final FinSeries series,
                                       final String priceColumnName,
                                       final int period,
                                       final boolean useVolumes,
                                       final String aColumnName,
                                       final String bColumnName,
                                       final String widthColumnName,
                                       final String deviationColumnName) {
        final DoubleColumn aColumn = (aColumnName == null) ? null : series.acquireDoubleColumn(aColumnName);
        final DoubleColumn bColumn = (bColumnName == null) ? null : series.acquireDoubleColumn(bColumnName);
        final DoubleColumn widthColumn = (widthColumnName == null) ? null : series.acquireDoubleColumn(widthColumnName);
        final DoubleColumn deviationColumn = (deviationColumnName == null) ? null : series.acquireDoubleColumn(deviationColumnName);

        final double[] x = new double[period];
        final double[] y = new double[period];
        final double[] vol = useVolumes ? new double[period] : null;

        for (int i = 0, k = -x.length + 1; i < x.length; i++, k++) {
            x[i] = k;
        }

        final DoubleColumn price = series.getDoubleColumn(priceColumnName);
        final LongColumn volume = useVolumes ? series.volume() : null;
        final int len = price.length();

        for (int i = 0; i < len; i++) {
            if (i < period - 1) {
                if (aColumn != null) {
                    aColumn.set(i, Double.NaN);
                }
                if (bColumn != null) {
                    bColumn.set(i, Double.NaN);
                }
                if (widthColumn != null) {
                    widthColumn.set(i, Double.NaN);
                }
                if (deviationColumn != null) {
                    deviationColumn.set(i, Double.NaN);
                }
                continue;
            }
            for (int j = i - period + 1, k = 0; j <= i; j++, k++) {
                y[k] = price.get(j);
                if (useVolumes) {
                    vol[k] = volume.get(j);
                }
            }
            final LinearTrendFit fit = useVolumes ? LinearTrendFit.fit(x, y, vol) : LinearTrendFit.fit(x, y);
            if (aColumn != null) {
                aColumn.set(i, fit.a);
            }
            if (bColumn != null) {
                bColumn.set(i, fit.b);
            }
            if (widthColumn != null) {
                double sd = 0;
                double sw = 0;
                for (int k = 0; k < y.length; k++) {
                    final double w = useVolumes ? vol[k] : 1;
                    sw += w;
                    sd += w * Math.abs(y[k] - fit.a - fit.b * x[k]);
                }
                widthColumn.set(i, sd / sw);
            }
            if (deviationColumn != null) {
                deviationColumn.set(i, y[y.length - 1] - fit.a);
            }
        }
    }

    /**
     * Вычисление силы тренда на основании рейтинга нормированных наклонов.
     *
     * @param series                  временной ряд
     * @param ratingSize              количество значений для подсчёта рейтинга
     * @param ratingDirection         0 -- симметричный вариант, +1/-1 -- по положительным/отрицательным наклонам
     * @param bColumn                 колонка, где записан наклон канала
     * @param wColumn                 колонка, где записана ширина канала
     * @param trendStrengthColumnName название колонки, куда будет записана сила тренда
     * @return колонка, куда будет записана сила тренда
     */
    public static DoubleColumn trendStrength(final FinSeries series,
                                             final int ratingSize,
                                             final int ratingDirection,
                                             final DoubleColumn bColumn,
                                             final DoubleColumn wColumn,
                                             final String trendStrengthColumnName) {
        final DoubleColumn trendStrengthColumn = series.acquireDoubleColumn(trendStrengthColumnName);
        final WindowOfDouble window = new WindowOfDouble(ratingSize);
        final int len = bColumn.length();
        for (int i = 0; i < len; i++) {
            final double ratio = bColumn.get(i) / wColumn.get(i);
            if (!Double.isFinite(ratio)) {
                trendStrengthColumn.set(i, Double.NaN);
                continue;
            }
            if (ratingDirection == 0
                || ratingDirection > 0 && ratio > 0
                || ratingDirection < 0 && ratio < 0) {
                window.add(ratio);
            }
            if (!window.isFull()) {
                trendStrengthColumn.set(i, Double.NaN);
                continue;
            }
            final double absRatio = Math.abs(ratio);
            int ts = 0;
            for (int j = -window.size() + 1; j <= 0; j++) {
                if (absRatio >= Math.abs(window.get(j))) {
                    ts++;
                }
            }
            trendStrengthColumn.set(i, Math.signum(ratio) * ts / window.size());
        }
        return trendStrengthColumn;
    }

    /**
     * Вычисление силы контртренда (рейтинг уклонения цены закрытия от средней линии канала).
     *
     * @param series                   временной ряд
     * @param ratingSize               количество значений для подсчёта рейтинга
     * @param deviationColumn          колонка, где записано уклонение цены закрытия от средней линии канала
     * @param contraStrengthColumnName название колонки, куда будет записана сила контртренда
     * @return колонка, куда будет записана сила контртренда
     */
    public static DoubleColumn contraStrength(final FinSeries series,
                                              final int ratingSize,
                                              final DoubleColumn deviationColumn,
                                              final String contraStrengthColumnName) {
        final DoubleColumn contraStrengthColumn = series.acquireDoubleColumn(contraStrengthColumnName);
        final WindowOfDouble window = new WindowOfDouble(ratingSize);
        final int len = deviationColumn.length();
        for (int i = 0; i < len; i++) {
            final double deviation = deviationColumn.get(i);
            if (!Double.isFinite(deviation)) {
                contraStrengthColumn.set(i, Double.NaN);
                continue;
            }
            window.add(deviation);
            if (!window.isFull()) {
                contraStrengthColumn.set(i, Double.NaN);
                continue;
            }
            final double absDevition = Math.abs(deviation);
            int cs = 0;
            for (int j = -window.size() + 1; j <= 0; j++) {
                if (absDevition >= Math.abs(window.get(j))) {
                    cs++;
                }
            }
            contraStrengthColumn.set(i, Math.signum(deviation) * cs / window.size());
        }
        return contraStrengthColumn;
    }
}
