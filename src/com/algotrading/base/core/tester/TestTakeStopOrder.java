package com.algotrading.base.core.tester;

import com.algotrading.base.core.commission.Commission;

public class TestTakeStopOrder extends TestOrder {

    public enum Type {
        Active,
        TakeProfit,
        StopLoss,
    }

    public final double takePrice;
    public final Commission takeCommission;
    public final String takeComment;
    public final double stopPrice;
    public final Commission stopCommission;
    public final String stopComment;
    public Type type = Type.Active;

    public TestTakeStopOrder(final long volume,
                             final double takePrice, final Commission takeCommission, final String takeComment,
                             final double stopPrice, final Commission stopCommission, final String stopComment) {
        super(volume);
        this.takePrice = takePrice;
        this.takeCommission = takeCommission;
        this.takeComment = takeComment;
        this.stopPrice = stopPrice;
        this.stopCommission = stopCommission;
        this.stopComment = stopComment;
    }

    @Override
    public String toString() {
        return "TakeStopOrder " + volume + " @ " + takePrice + "/" + stopPrice
               + " (" + executionPrice + "), comment=" + takeComment + "/" + stopComment;
    }
}
