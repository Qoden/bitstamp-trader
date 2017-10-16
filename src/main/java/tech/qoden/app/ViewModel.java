package tech.qoden.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tech.qoden.trading.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ViewModel implements Closeable {
    private final ExchangeConnection connection;
    private final ObservableList<PositionViewModel> bids = FXCollections.observableArrayList();
    private final ObservableList<PositionViewModel> asks = FXCollections.observableArrayList();
    private final ObservableList<TradeViewModel> trades = FXCollections.observableArrayList();
    private final JavaFxExchangeConnectionListener listener;
    private final Config config;

    public ViewModel(ExchangeConnection connection, Config config) {
        if (connection == null) throw new IllegalArgumentException("connection");
        if (config == null) throw new IllegalArgumentException("config");

        this.connection = connection;
        this.config = config;
        listener = new JavaFxExchangeConnectionListener(new Listener());
        this.connection.addListener(listener);
    }

    @Override
    public void close() throws IOException {
        this.connection.removeListener(listener);
    }

    public ObservableList<PositionViewModel> getBids() {
        return bids;
    }

    public ObservableList<PositionViewModel> getAsks() {
        return asks;
    }

    public ObservableList<TradeViewModel> getTrades() {
        return trades;
    }

    public class Listener extends AbstractExchangeConnectionListener {
        @Override
        public void orderBookUpdated(ExchangeConnection connection) {
            updatePositions(connection);
        }

        @Override
        public void connected(ExchangeConnection connection) {
            updatePositions(connection);
        }

        private void updatePositions(ExchangeConnection connection) {
            //This is not very effective UI update.
            //TODO Implement helper method to merge updates into bids and asks instead of replacing it content.
            bids.clear();
            asks.clear();
            OrderBook orderBook = connection.getOrderBook();
            bids.addAll(createPositions(orderBook.getBids()));
            asks.addAll(createPositions(orderBook.getAsks()));
        }

        private List<PositionViewModel> createPositions(List<Position> positions) {
            int items = config.getOrderBookLength();
            return positions.stream().limit(items).map(PositionViewModel::new).collect(Collectors.toList());
        }

        @Override
        public void recentTradesUpdated(ExchangeConnection connection) {
            //This is not very effective UI update.
            //TODO Implement helper method to merge changed trades instad of replacing collection contents.
            trades.clear();
            trades.addAll(createTrades(connection.getRecentTrades()));
        }

        private List<TradeViewModel> createTrades(RecentTrades recentTrades) {
            int items = config.getOrderBookLength();
            return recentTrades.getTrades().stream().limit(items).map(TradeViewModel::new).collect(Collectors.toList());
        }
    }
}
