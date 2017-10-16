package tech.qoden.app;

import tech.qoden.trading.Position;

public class PositionViewModel {
    private Position position;

    public PositionViewModel(Position position) {
        if (position == null) throw new IllegalArgumentException("position");
        this.position = position;
    }

    public String getPrice() { return position.getPrice().toString();}
    public String getAmount() { return position.getAmount().toString();}
}
