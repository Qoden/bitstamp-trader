package tech.qoden.trading;

import java.util.List;

public interface OrderBook {
    List<Position> getBids();

    List<Position> getAsks();
}
