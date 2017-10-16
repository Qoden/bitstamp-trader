package tech.qoden.trading;

import java.math.BigDecimal;

public interface ExchangeConnection {
    void addListener(ExchangeConnectionListener listener);

    void removeListener(ExchangeConnectionListener listener);

    void connect();

    void disconnect();

    OrderBook getOrderBook();

    RecentTrades getRecentTrades();

    void buy(BigDecimal amount, BigDecimal price, String tradeBot);

    void sell(BigDecimal amount, BigDecimal price, String tradeBot);
}
