package tech.qoden.trading;


public interface ExchangeConnectionListener {
    void connected(ExchangeConnection connection);

    void disconnected(ExchangeConnection connection);

    void orderBookUpdated(ExchangeConnection connection);

    void connectionFailed(ExchangeConnection connection, Exception e);

    void recentTradesUpdated(ExchangeConnection connection);
}
