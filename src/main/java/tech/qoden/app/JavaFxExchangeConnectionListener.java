package tech.qoden.app;

import javafx.application.Platform;
import tech.qoden.trading.AbstractExchangeConnectionListener;
import tech.qoden.trading.ExchangeConnection;
import tech.qoden.trading.ExchangeConnectionListener;


public class JavaFxExchangeConnectionListener extends AbstractExchangeConnectionListener {

    private final ExchangeConnectionListener impl;

    public JavaFxExchangeConnectionListener(ExchangeConnectionListener impl) {
        if (impl == null) throw new IllegalArgumentException("impl");
        this.impl = impl;
    }

    @Override
    public void connected(ExchangeConnection connection) {
        Platform.runLater(() -> impl.connected(connection));
    }

    @Override
    public void disconnected(ExchangeConnection connection) {
        Platform.runLater(() -> impl.disconnected(connection));
    }

    @Override
    public void orderBookUpdated(ExchangeConnection connection) {
        Platform.runLater(() -> impl.orderBookUpdated(connection));
    }

    @Override
    public void connectionFailed(ExchangeConnection connection, Exception e) {
        Platform.runLater(() -> impl.connectionFailed(connection, e));
    }

    @Override
    public void recentTradesUpdated(ExchangeConnection connection) {
        Platform.runLater(() -> impl.recentTradesUpdated(connection));
    }
}
