package com.algotrading.base.core.marketdata;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Информация о датах экспирации опционов.
 */
public class OptionExpiryInfo {
    /**
     * Дата экспирации в формате yyyymmdd.
     */
    public final int expiry;
    /**
     * Дата торгового дня, предшествующего дате экспирации, в формате yyyymmdd.
     */
    public final int oneDayBeforeExpiry;
    /**
     * Дата экспирации.
     */
    public final LocalDate expiryDate;
    /**
     * Дата торгового дня, предшествующего дате экспирации.
     */
    public final LocalDate oneDayBeforeExpiryDate;
    /**
     * Тип опциона: 'W' - недельный, 'M' - месячный, 'Q' - квартальный.
     */
    public final char optionType;
    /**
     * Суффикс опциона колл.
     */
    public final String callSuffix;
    /**
     * Суффикс опциона пут.
     */
    public final String putSuffix;
    /**
     * Суффикс базового актива.
     */
    public final String futSuffix;
    /**
     * Список, задающий календарь экспирации опционов на RI.
     */
    public static final List<OptionExpiryInfo> RI_LIST;

    static {
        final List<OptionExpiryInfo> ri = new ArrayList<>();
        def(ri, 20170209, 20170208, 'W', "BB7B", "BN7B", "H7");
        def(ri, 20170216, 20170215, 'M', "BB7", "BN7", "H7");
        def(ri, 20170222, 20170221, 'W', "BB7D", "BN7D", "H7");
        def(ri, 20170302, 20170301, 'W', "BC7A", "BO7A", "H7");
        def(ri, 20170309, 20170307, 'W', "BC7B", "BO7B", "H7");
        def(ri, 20170316, 20170315, 'Q', "BC7", "BO7", "H7");
        def(ri, 20170323, 20170322, 'W', "BC7D", "BO7D", "M7");
        def(ri, 20170330, 20170329, 'W', "BC7E", "BO7E", "M7");
        def(ri, 20170406, 20170405, 'W', "BD7A", "BP7A", "M7");
        def(ri, 20170413, 20170412, 'W', "BD7B", "BP7B", "M7");
        def(ri, 20170420, 20170419, 'M', "BD7", "BP7", "M7");
        def(ri, 20170427, 20170426, 'W', "BD7D", "BP7D", "M7");
        def(ri, 20170504, 20170503, 'W', "BE7A", "BQ7A", "M7");
        def(ri, 20170511, 20170510, 'W', "BE7B", "BQ7B", "M7");
        def(ri, 20170518, 20170517, 'M', "BE7", "BQ7", "M7");
        def(ri, 20170525, 20170524, 'W', "BE7D", "BQ7D", "M7");
        def(ri, 20170601, 20170531, 'W', "BF7A", "BR7A", "M7");
        def(ri, 20170608, 20170607, 'W', "BF7B", "BR7B", "M7");
        def(ri, 20170615, 20170614, 'Q', "BF7", "BR7", "M7");
        def(ri, 20170622, 20170621, 'W', "BF7D", "BR7D", "U7");
        def(ri, 20170629, 20170628, 'W', "BF7E", "BR7E", "U7");
        def(ri, 20170706, 20170705, 'W', "BG7A", "BS7A", "U7");
        def(ri, 20170713, 20170712, 'W', "BG7B", "BS7B", "U7");
        def(ri, 20170720, 20170719, 'M', "BG7", "BS7", "U7");
        def(ri, 20170727, 20170726, 'W', "BG7D", "BS7D", "U7");
        def(ri, 20170803, 20170802, 'W', "BH7A", "BT7A", "U7");
        def(ri, 20170810, 20170809, 'W', "BH7B", "BT7B", "U7");
        def(ri, 20170817, 20170816, 'M', "BH7", "BT7", "U7");
        def(ri, 20170824, 20170823, 'W', "BH7D", "BT7D", "U7");
        def(ri, 20170831, 20170830, 'W', "BH7E", "BT7E", "U7");
        def(ri, 20170907, 20170906, 'W', "BI7A", "BU7A", "U7");
        def(ri, 20170914, 20170913, 'W', "BI7B", "BU7B", "U7");
        def(ri, 20170921, 20170920, 'Q', "BI7", "BU7", "U7");
        def(ri, 20170928, 20170927, 'W', "BI7D", "BU7D", "Z7");
        RI_LIST = Collections.unmodifiableList(ri);
    }

    private static void def(final List<OptionExpiryInfo> list,
                            final int exp, final int dayBeforeExp, final char type,
                            final String callSuffix, final String putSuffix, final String futSuffix) {
        list.add(new OptionExpiryInfo(exp, dayBeforeExp, type, callSuffix, putSuffix, futSuffix));
    }

    private OptionExpiryInfo(final int expiry, final int oneDayBeforeExpiry, final char optionType,
                             final String callSuffix, final String putSuffix, final String futSuffix) {
        this.expiry = expiry;
        this.oneDayBeforeExpiry = oneDayBeforeExpiry;
        expiryDate = localDate(expiry);
        oneDayBeforeExpiryDate = localDate(oneDayBeforeExpiry);
        this.callSuffix = callSuffix;
        this.putSuffix = putSuffix;
        this.optionType = optionType;
        this.futSuffix = futSuffix;
    }

    private static LocalDate localDate(final int yyyymmdd) {
        int v = yyyymmdd;
        final int day = v % 100;
        v /= 100;
        final int month = v % 100;
        v /= 100;
        final int year = v;
        return LocalDate.of(year, month, day);
    }

    @Override
    public String toString() {
        return "OptionExpiryInfo{"
               + expiry + '(' + oneDayBeforeExpiry + "), "
               + optionType + ' ' + callSuffix + '/' + putSuffix + '/' + futSuffix + '}';
    }
}
