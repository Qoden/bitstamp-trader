package tech.qoden.trading;

import java.util.stream.Stream;

public interface MutableRecentTrades extends RecentTrades {
    void update(Stream<Trade> tradeStream);

    void addVirtualTrade(Trade trade);
}
