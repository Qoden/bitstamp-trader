package tech.qoden.app;

import tech.qoden.trading.Trade;

public class TradeViewModel {

    private Trade trade;

    public TradeViewModel(Trade trade) {
        if (trade == null) throw new IllegalArgumentException("trade");
        this.trade = trade;
    }

    public String getPrice() {
        return trade.getPrice().toString();
    }

    public String getAmount() {
        return trade.getAmount().toString();
    }

    public String getDate() {
        return trade.getDate().toString();
    }

    public String getType() {
        return trade.getType().toString();
    }
    public String getId() {
        return trade.getId();
    }
}
