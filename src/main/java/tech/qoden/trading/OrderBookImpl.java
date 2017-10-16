package tech.qoden.trading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Order book can be read at any time and updated in a single thread at a time
 */
public class OrderBookImpl implements MutableOrderBook {
    private volatile List<Position> bids, asks;

    public final static Comparator<Position> ASKS_COMPARATOR = Position::orderByAmount;
    public final static Comparator<Position> BIDS_COMPARATOR = ASKS_COMPARATOR.reversed();

    @Override
    public void reset(Stream<Position> bids, Stream<Position> asks) {
        this.bids = Collections.unmodifiableList(bids.sorted(BIDS_COMPARATOR).collect(Collectors.toList()));
        this.asks = Collections.unmodifiableList(asks.sorted(ASKS_COMPARATOR).collect(Collectors.toList()));
    }

    @Override
    public void update(Stream<Position> bids, Stream<Position> asks) {
        // This could be done more efficiently in O(n+m).
        // But I'm lazy today to mess with efficient merge(Stream, List) utility.
        // Besides resulting O(m*log(n)) still very good for the problem at hand.
        // If performance is important then consider functional persistent map.
        List<Position> updatedBids = new ArrayList<>(this.bids);
        List<Position> updatedAsks = new ArrayList<>(this.asks);
        bids.forEach(p -> merge(p, updatedBids, BIDS_COMPARATOR));
        asks.forEach(p -> merge(p, updatedAsks, ASKS_COMPARATOR));
        this.bids = Collections.unmodifiableList(updatedBids);
        this.asks = Collections.unmodifiableList(updatedAsks);
    }

    @Override
    public List<Position> getBids() {
        return bids;
    }

    @Override
    public List<Position> getAsks() {
        return asks;
    }

    private static void merge(Position position, List<Position> positions, Comparator<Position> orderByAmount) {
        int pos = Collections.binarySearch(positions, position, orderByAmount);
        if (pos < 0) {
            if (position.getAmount().intValue() > 0) {
                //this could be confusing, see Collections.binarySearch javadoc
                //averbin
                pos = -pos - 1;
                positions.add(pos, position);
            }
        } else {
            if (position.getAmount().intValue() <= 0) {
                positions.remove(pos);
            } else {
                positions.set(pos, position);
            }
        }
    }

}
