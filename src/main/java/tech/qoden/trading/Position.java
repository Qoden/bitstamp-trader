package tech.qoden.trading;

import java.math.BigDecimal;

public class Position {
    BigDecimal price;
    BigDecimal amount;
    Symbol symbol;

    public Position(BigDecimal price, BigDecimal amount, Symbol symbol) {
        if (price == null) throw new IllegalArgumentException("value");
        if (amount == null) throw new IllegalArgumentException("value");
        if (symbol == null) throw new IllegalArgumentException("value");

        this.price = price;
        this.amount = amount;
        this.symbol = symbol;
    }

    public static int orderByAmount(Position x, Position y) {
        return x.getPrice().compareTo(y.getPrice());
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return String.format("%s %f at %f", symbol.getName(), amount, this.price);
    }
}
