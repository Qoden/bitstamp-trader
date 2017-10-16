package tech.qoden.bitstamp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.qoden.trading.AbstractConnection;
import tech.qoden.trading.Position;
import tech.qoden.trading.Symbol;
import tech.qoden.trading.Trade;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class BitstampConnection extends AbstractConnection {

    private static final Logger logger = LoggerFactory.getLogger(AbstractConnection.class);

    public static final URL ORDER_BOOK_URL, RECENT_TRADES_URL;
    public static final Map<Symbol, String> BITSTAMP_SYMBOLS;

    private final Channel orderBookChannel;
    private final Pusher pusher;
    private final URL orderBookUrl, recentTradesUrl;
    private ScheduledFuture<?> recentTradesPoll;
    private final Gson gson = new Gson();
    private final ScheduledExecutorService recentTradesPoller;

    static {
        try {
            ORDER_BOOK_URL = new URL("https://www.bitstamp.net/api/v2/order_book/");
            RECENT_TRADES_URL = new URL("https://www.bitstamp.net/api/v2/transactions/");
        } catch (MalformedURLException e) {
            logger.error("Static init failed", e);
            throw new RuntimeException(e);
        }
        BITSTAMP_SYMBOLS = new HashMap<>();
        BITSTAMP_SYMBOLS.put(Symbol.BTC_USD, "btcusd");
    }

    public BitstampConnection(Symbol symbol) {
        super(symbol);

        if (!BITSTAMP_SYMBOLS.containsKey(symbol)) {
            throw new IllegalArgumentException("symbol");
        }

        PusherOptions options = new PusherOptions();
        pusher = new Pusher("de504dc5763aeef9ff52", options);
        orderBookChannel = pusher.subscribe("diff_order_book");

        try {
            orderBookUrl = new URL(ORDER_BOOK_URL, BITSTAMP_SYMBOLS.get(symbol) + "/");
            recentTradesUrl = new URL(RECENT_TRADES_URL, BITSTAMP_SYMBOLS.get(symbol) + "/");
        } catch (MalformedURLException e) {
            logger.error("Init failed", e);
            throw new RuntimeException(e);
        }

        recentTradesPoller = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void buy(BigDecimal amount, BigDecimal price, String id) {
        logger.debug("Buy {} {} at {}", getSymbol().getName(), amount, price);
        getMutableRecentTrades().addVirtualTrade(new Trade(price, amount, getSymbol(), new Date(), id, Trade.Type.BUY));
        getWorkQueue().submit(() -> notifyListeners(l -> l.recentTradesUpdated(this)));
    }

    @Override
    public void sell(BigDecimal amount, BigDecimal price, String id) {
        logger.debug("Sell {} {} at {}", getSymbol().getName(), amount, price);
        getMutableRecentTrades().addVirtualTrade(new Trade(price, amount, getSymbol(), new Date(), id, Trade.Type.SELL));
        getWorkQueue().submit(() -> notifyListeners(l -> l.recentTradesUpdated(this)));
    }

    public class BitstampOrderBook {
        public List<List<BigDecimal>> bids;
        public List<List<BigDecimal>> asks;
    }

    public class BitstampTransaction {
        public long date;
        public String tid;
        public BigDecimal price;
        public BigDecimal amount;
        public int type;
    }

    public static final Type BITSTAMP_TRANSACTIONS_TYPE = new TypeToken<ArrayList<BitstampTransaction>>() {
    }.getType();

    @Override
    protected void doConnect() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) orderBookUrl.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.connect();
        try (JsonReader rdr = new JsonReader(new InputStreamReader(conn.getInputStream()))) {
            BitstampOrderBook bitstampOrderBook = gson.fromJson(rdr, BitstampOrderBook.class);
            Stream<Position> bids = bitstampOrderBook.bids.stream().map(this::toPosition);
            Stream<Position> asks = bitstampOrderBook.asks.stream().map(this::toPosition);

            getMutableOrderBook().reset(bids, asks);
        }
        pusher.connect();
        orderBookChannel.bind("data", this::onOrderBookData);
        recentTradesPoll = recentTradesPoller.scheduleAtFixedRate(this::pollForRecentTrades, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doDisconnect() {
        recentTradesPoll.cancel(false);
        pusher.disconnect();
    }

    private void onOrderBookData(String channelName, String eventName, String data) {
        BitstampOrderBook bitstampOrderBook = gson.fromJson(data, BitstampOrderBook.class);
        Stream<Position> bids = bitstampOrderBook.bids.stream().map(this::toPosition);
        Stream<Position> asks = bitstampOrderBook.asks.stream().map(this::toPosition);
        getWorkQueue().submit(() -> {
            getMutableOrderBook().update(bids, asks);
            notifyListeners(l -> l.orderBookUpdated(this));
        });
    }

    private void pollForRecentTrades() {
        try {
            HttpURLConnection conn = (HttpURLConnection) recentTradesUrl.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.connect();
            try (JsonReader rdr = new JsonReader(new InputStreamReader(conn.getInputStream()))) {
                List<BitstampTransaction> transactions = gson.fromJson(rdr, BITSTAMP_TRANSACTIONS_TYPE);
                getMutableRecentTrades().update(transactions.stream().map(this::toTrade));

                getWorkQueue().submit(() -> notifyListeners(l -> l.recentTradesUpdated(this)));
            }
        } catch (Exception e) {
            logger.warn("Recent trades failed", e);
        }
    }

    private Trade toTrade(BitstampTransaction t) {
        return new Trade(t.price,
                t.amount,
                getSymbol(),
                Date.from(Instant.ofEpochSecond(t.date)),
                t.tid,
                t.type == 0 ? Trade.Type.BUY : Trade.Type.SELL);
    }

    private Position toPosition(List<BigDecimal> bigDecimals) {
        return new Position(bigDecimals.get(0), bigDecimals.get(1), getSymbol());
    }
}
