package tech.qoden.trading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class AbstractConnection implements ExchangeConnection {

    private static final Logger logger = LoggerFactory.getLogger(AbstractConnection.class);

    private final ExecutorService workQueue;
    private final Symbol symbol;
    private final List<ExchangeConnectionListener> listeners = new ArrayList<>();
    private final OrderBookImpl orderBook;
    private final RecentTradesImpl recentTrades;
    private CompletableFuture<Void> connectRequest;

    public AbstractConnection(Symbol symbol) {
        if (symbol == null) throw new IllegalArgumentException("symbol");

        workQueue = Executors.newSingleThreadExecutor();
        orderBook = new OrderBookImpl();
        recentTrades = new RecentTradesImpl();
        this.symbol = symbol;
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }

    public RecentTrades getRecentTrades() {
        return recentTrades;
    }

    protected MutableOrderBook getMutableOrderBook() {
        return orderBook;
    }

    protected MutableRecentTrades getMutableRecentTrades() {
        return recentTrades;
    }

    public void connect() {
        if (connectRequest != null) return;

        connectRequest = CompletableFuture.runAsync(() -> {
            try {
                doConnect();
                notifyListeners(l -> l.connected(this));
            } catch (Exception e) {
                logger.warn("Connect failed", e);
                notifyListeners(l -> l.connectionFailed(this, e));
                connectRequest = null;
            }
        }, workQueue);
    }

    protected abstract void doConnect() throws IOException;

    public void disconnect() {
        if (connectRequest != null) {
            connectRequest.thenApply(aVoid -> {
                doDisconnect();
                connectRequest = null;
                notifyListeners(l -> l.disconnected(this));
                return aVoid;
            });
        }
    }

    protected abstract void doDisconnect();

    public boolean isConnected() {
        return connectRequest != null && connectRequest.isDone();
    }

    public void addListener(ExchangeConnectionListener listener) {
        workQueue.submit(() -> {
            listeners.add(listener);
            if (isConnected()) {
                listener.connected(this);
            }
        });
    }

    public void removeListener(ExchangeConnectionListener listener) {
        workQueue.submit(() -> {
            listeners.remove(listener);
        });
    }

    protected void notifyListeners(Consumer<ExchangeConnectionListener> action) {
        if (!isConnected()) return;
        for (ExchangeConnectionListener l : listeners) {
            try {
                action.accept(l);
            } catch (Throwable throwable) {
                logger.warn("Listener failed {}", l, throwable);
                //ignore
            }
        }
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public ExecutorService getWorkQueue() {
        return workQueue;
    }
}
