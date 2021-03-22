package com.algotrading.base.core.level2;

import java.util.Objects;

/**
 * Абстрактный стакан котировок.
 * <p>
 * Реализация включает в себя 4 массива: цены и количества для бидов и цены и количества для оферов.
 * Цены в массивах упорядочены: для бидов по убыванию, для оферов по возрастанию.
 *
 * @param <P> тип, задающий массив цен; обычно double[] или BigDecimal[]
 * @param <S> тип, задающий массив количеств; обычно long[] или BigDecimal[]
 */
abstract class AbstractLevel2<P, S> {
    public final long timestamp;
    public final long timeCode;
    public final long sequence;
    public final P bidPrices;
    public final S bidSizes;
    public final P askPrices;
    public final S askSizes;

    /**
     * Конструктор.
     *
     * @param timestamp unix-время или 0, если неактуально
     * @param timeCode  метка времени или 0, если неактуально
     * @param sequence  номер или 0, если неактуально
     * @param bidPrices массив цен для бидов
     * @param bidSizes  масив количеств для бидов
     * @param askPrices массив цен для оферов
     * @param askSizes  массив количеств для оферов
     */
    AbstractLevel2(final long timestamp, final long timeCode, final long sequence,
                   final P bidPrices, final S bidSizes,
                   final P askPrices, final S askSizes) {
        Objects.requireNonNull(bidPrices);
        Objects.requireNonNull(bidSizes);
        Objects.requireNonNull(askPrices);
        Objects.requireNonNull(askSizes);
        this.timestamp = timestamp;
        this.timeCode = timeCode;
        this.sequence = sequence;
        this.bidPrices = bidPrices;
        this.bidSizes = bidSizes;
        this.askPrices = askPrices;
        this.askSizes = askSizes;
    }

    /**
     * @return количество элементов бидов
     */
    public abstract int bidsCount();

    /**
     * @return количество элементов оферов
     */
    public abstract int asksCount();
}
