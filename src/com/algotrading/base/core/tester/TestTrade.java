package com.algotrading.base.core.tester;

import com.algotrading.base.core.PriceToString;
import com.algotrading.base.core.TimeCodes;
import com.algotrading.base.core.columns.DoubleColumn;
import com.algotrading.base.core.columns.LongColumn;

import java.util.Locale;

/**
 * Трейд.
 */
public class TestTrade {
    /**
     * Константа точности для сравнения с нулём.
     */
    static final double EPS = 1e-4;
    /**
     * Код инструмента.
     */
    public final String security;
    /**
     * Время входа в трейд.
     */
    public final long enterTimeCode;
    /**
     * Время выхода из трейда (0L - трейд ещё активен).
     */
    public long exitTimeCode = 0L;
    /**
     * Объём со знаком, затраченный на увеличение позиции в трейде.
     */
    public double scaleInVolume;
    /**
     * Деньги, использованные на увеличение позиции в трейде.
     */
    public double scaleInValue;
    /**
     * Объём со знаком, затраченный на уменьшение позиции в трейде.
     */
    public double scaleOutVolume;
    /**
     * Деньги, использованные на уменьшение позиции в трейде.
     */
    public double scaleOutValue;
    /**
     * Количество баров в трейде.
     */
    public int barsInTrade;
    /**
     * Цена последней сделки.
     */
    public double last;
    /**
     * Время последней сделки.
     */
    public long tLast;
    /**
     * Размер комиссии (неотрицательная величина).
     */
    public double commission;
    /**
     * Колонка с временными метками.
     */
    final LongColumn timeCodeColumn;
    /**
     * Колонка с ценами закрытия (для вычисления эквити и просадок).
     */
    final DoubleColumn closeColumn;

    /**
     * Конструктор.
     *
     * @param t          время открытия трейда
     * @param size       размер позиции
     * @param price      цена входа
     * @param commission комиссия
     */
    TestTrade(final long t, final String security, final double size, final double price, final double commission,
              final LongColumn timeCodeColumn, final DoubleColumn closeColumn) {
        this.security = security;
        if (Math.abs(size) <= EPS) {
            throw new IllegalArgumentException("Zero size");
        }
        enterTimeCode = t;
        scaleInVolume = size;
        scaleInValue = size * price;
        scaleOutVolume = 0;
        scaleOutValue = 0;
        barsInTrade = 0;
        last = price;
        tLast = t;
        this.commission = commission;
        this.timeCodeColumn = timeCodeColumn;
        this.closeColumn = closeColumn;
    }

    public static String header() {
        return "Security;Trade;Date;Price;Ex.Date;Ex.Price;Profit;%Profit;Volume;Value;#bars;commission";
    }

    /**
     * Изменить трейд.
     *
     * @param t   время изменения трейда
     * @param delta      приращение позиции
     * @param price      цена исполнения
     * @param commission комиссия
     */
    void update(final long t, final double delta, final double price, final double commission) {
        if (Math.abs(delta) <= EPS) {
            throw new IllegalArgumentException("Zero delta");
        }
        final double volume = getVolume();
        if (Math.abs(volume) <= EPS) {
            throw new IllegalStateException("Cannot update trade done");
        }
        tLast = t;
        double newVolume = volume + delta;
        if (Math.abs(newVolume) <= EPS) {
            newVolume = 0;
            exitTimeCode = t;
        }
        if (volume * newVolume < 0) {
            throw new IllegalArgumentException("Current volume = " + volume + ", delta = " + delta);
        }
        if (isLong() && delta > 0 || isShort() && delta < 0) {
            scaleInVolume += delta;
            scaleInValue += delta * price;
        } else {
            scaleOutVolume -= delta;
            scaleOutValue -= delta * price;
        }
        this.commission += commission;
    }

    /**
     * @return true, если это длинный трейд.
     */
    public boolean isLong() {
        return scaleInVolume > EPS;
    }

    /**
     * @return true, если это короткий трейд.
     */
    public boolean isShort() {
        return scaleInVolume < -EPS;
    }

    /**
     * @return текущий размер трейда с учётом знака.
     */
    public double getVolume() {
        return scaleInVolume - scaleOutVolume;
    }

    /**
     * @return true, если трейд завершён, false иначе.
     */
    public boolean isDone() {
        return Math.abs(getVolume()) <= EPS;
    }

    /**
     * Оценить equity для трейда.
     *
     * @param price текущая цена.
     * @return значение equity для трейда.
     */
    public double getEquity(final double price) {
        return (scaleOutValue - scaleInValue) + (scaleInVolume - scaleOutVolume) * price - commission;
    }

    /**
     * Оценить используемый капитал в трейде.
     *
     * @return значение используемого капитала в трейде.
     */
    public double getUsedCapital() {
        return scaleInValue - scaleOutValue;
    }

    /**
     * @return средняя цена входа в трейд.
     */
    public double getAvgInPrice() {
        return scaleInValue / scaleInVolume;
    }

    /**
     * @return средняя цена выхода из трейда.
     */
    public double getAvgOutPrice() {
        return (scaleOutValue + (scaleInVolume - scaleOutVolume) * last) / scaleInVolume;
    }

    /**
     * @return прибыль/убыток трейда.
     */
    public double getProfit() {
        return getEquity(last);
    }

    /**
     * @return прибыль/убыток трейда в процентах.
     */
    public double getProfitPercent() {
        final double pp = getEquity(last) / scaleInValue * 100.0;
        return isLong() ? pp : isShort() ? -pp : Double.NaN;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Security=%s;Trade=%s%s;Date=%s;Price=%s;Ex.Date=%s;Ex.Price=%s;Profit=%.2f;%%Profit=%.2f%%;Volume=%.1f;Value=%.2f;#bars=%d;commission=%.2f",
                security,
                isDone() ? "" : "Open ",
                isLong() ? "Long" : isShort() ? "Short" : "???",
                TimeCodes.timeCodeString(enterTimeCode),
                PriceToString.priceToString(getAvgInPrice(), Locale.US),
                TimeCodes.timeCodeString(exitTimeCode == 0L ? tLast : exitTimeCode),
                PriceToString.priceToString(getAvgOutPrice(), Locale.US),
                getProfit(),
                getProfitPercent(),
                scaleInVolume,
                scaleInValue,
                barsInTrade,
                commission
        );
    }

    public String toCsvString() {
        return String.format("%s;%s%s;%s;%s;%s;%s;%.2f;%.2f;%.1f;%.2f;%d;%.2f",
                security,
                isDone() ? "" : "Open ",
                isLong() ? "Long" : isShort() ? "Short" : "???",
                TimeCodes.timeCodeString(enterTimeCode),
                PriceToString.priceToString(getAvgInPrice()),
                TimeCodes.timeCodeString(exitTimeCode == 0L ? tLast : exitTimeCode),
                PriceToString.priceToString(getAvgOutPrice()),
                getProfit(),
                getProfitPercent(),
                scaleInVolume,
                scaleInValue,
                barsInTrade,
                commission
        );
    }
}
