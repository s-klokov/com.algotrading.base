package com.algotrading.base.core.commission;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Комиссия в зависимости от инструмента.
 */
public final class SecurityCommission implements Commission {

    private final Map<String, Commission> commissionMap = new HashMap<>();
    private Commission defaultCommission = SimpleCommission.ofPercent(0);

    /**
     * Задать комиссию по умолчанию.
     *
     * @param percent размер комиссии по умолчанию в процентах
     * @return этот объект
     */
    public SecurityCommission setPercent(final double percent) {
        setDefault(SimpleCommission.ofPercent(percent));
        return this;
    }

    /**
     * Задать комиссию.
     *
     * @param secCode код инструмента
     * @param percent размер комиссии в процентах
     * @return этот объект
     */
    public SecurityCommission setPercent(final String secCode, final double percent) {
        set(secCode, SimpleCommission.ofPercent(percent));
        return this;
    }

    /**
     * Задать комиссию по умолчанию.
     *
     * @param commission комиссия по умолчанию
     * @return этот объект
     */
    public SecurityCommission setDefault(final Commission commission) {
        defaultCommission = Objects.requireNonNull(commission);
        return this;
    }

    /**
     * Задать комиссию.
     *
     * @param secCode    код инструмента
     * @param commission комиссия
     * @return этот объект
     */
    public SecurityCommission set(final String secCode, final Commission commission) {
        commissionMap.put(Objects.requireNonNull(secCode), Objects.requireNonNull(commission));
        return this;
    }

    @Override
    public double getCommission(final double volume, final String secCode, final double price) {
        Commission commission = commissionMap.get(secCode);
        if (commission == null) {
            commission = defaultCommission;
        }
        return commission.getCommission(volume, secCode, price);
    }
}
