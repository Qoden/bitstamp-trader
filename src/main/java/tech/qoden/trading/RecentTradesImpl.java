package tech.qoden.trading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecentTradesImpl implements MutableRecentTrades {

    private final List<Trade> virtualTrades = new ArrayList<>();
    private volatile List<Trade> trades = Collections.emptyList();
    private volatile Date min;

    @Override
    public void update(Stream<Trade> tradeStream) {
        if (tradeStream == null) throw new IllegalArgumentException("tradeStream");
        trades = tradeStream.collect(Collectors.toList());
        min = trades.stream().map(Trade::getDate).min(Date::compareTo).orElseGet(() -> new Date(Long.MIN_VALUE));
    }

    @Override
    public void addVirtualTrade(Trade trade) {
        synchronized (virtualTrades) {
            virtualTrades.removeIf(x -> x.getDate().compareTo(min) <= 0);
            virtualTrades.add(trade);
        }
    }

    public List<Trade> getTrades() {
        //TODO do not create so much garbage
        synchronized (virtualTrades) {
            return Stream.concat(trades.stream(), virtualTrades.stream())
                    .sorted(Trade::compareDate)
                    .collect(Collectors.toList());
        }
    }
}
