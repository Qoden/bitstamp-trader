package tech.qoden.trading;

import java.util.stream.Stream;

public interface MutableOrderBook extends OrderBook {
    void reset(Stream<Position> bids, Stream<Position> asks);

    void update(Stream<Position> bids, Stream<Position> asks);
}
