package tech.qoden.trading;

public abstract class AbstractExchangeConnectionListener implements ExchangeConnectionListener {
    @Override
    public void connected(ExchangeConnection connection) {
    }

    @Override
    public void disconnected(ExchangeConnection connection) {
    }

    @Override
    public void orderBookUpdated(ExchangeConnection connection) {
    }

    @Override
    public void connectionFailed(ExchangeConnection connection, Exception e) {
    }

    @Override
    public void recentTradesUpdated(ExchangeConnection connection) {

    }
}
