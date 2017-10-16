package tech.qoden.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.qoden.trading.AbstractExchangeConnectionListener;
import tech.qoden.trading.ExchangeConnection;
import tech.qoden.trading.Trade;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class TradeRobot implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(TradeRobot.class);

    private Config config;
    private ExchangeConnection connection;
    private Listener listener;
    private boolean isRunning;
    private int ticks = 0;
    private Trade baseLineTrade;

    public TradeRobot(ExchangeConnection connection, Config config) {
        if (config == null) throw new IllegalArgumentException("config");
        if (connection == null) throw new IllegalArgumentException("connection");
        this.config = config;
        this.connection = connection;
        listener = new Listener();
    }

    public void run() {
        isRunning = true;
        connection.addListener(listener);
        List<Trade> trades = connection.getRecentTrades().getTrades();
        if (trades.size() > 0) {
            baseLineTrade = trades.get(0);
        }
    }

    public void close() {
        isRunning = false;
        ticks = 0;
        baseLineTrade = null;
        connection.removeListener(listener);
    }

    private class Listener extends AbstractExchangeConnectionListener {

        private final String BOT_ID = "TradeBot";
        private List<Trade> trades;

        @Override
        public void recentTradesUpdated(ExchangeConnection connection) {
            if (!isRunning) return;
            trades = connection.getRecentTrades().getTrades();
            if (trades.size() == 0) return;
            if (baseLineTrade == null) baseLineTrade = trades.get(0);
            if (baseLineTrade == null) return;

            ticks += countTicksSinceBaseLineTrade();
            baseLineTrade = trades.get(0);

            logger.debug("Ticks {}", ticks);

            if (ticks >= config.getTradeRobotUpTicks()) {
                ticks = 0;
                connection.buy(BigDecimal.valueOf(1), baseLineTrade.getPrice(), BOT_ID);
            } else if (-ticks >= config.getTradeRobotDownTicks()) {
                ticks = 0;
                connection.sell(BigDecimal.valueOf(1), baseLineTrade.getPrice(), BOT_ID);
            }
        }

        private int countTicksSinceBaseLineTrade() {
            int ticks = 0;
            Iterator<Trade> tradesIterator = trades.iterator();
            if (tradesIterator.hasNext()) {
                Trade trade = tradesIterator.next();
                while (tradesIterator.hasNext() && !trade.equals(baseLineTrade)) {
                    Trade prevTrade = tradesIterator.next();
                    int c = prevTrade.getPrice().compareTo(trade.getPrice());
                    if (c < 0) {
                        ticks++;
                    } else if (c > 0) {
                        ticks--;
                    }
                    trade = prevTrade;
                }
            }
            return ticks;
        }
    }
}
