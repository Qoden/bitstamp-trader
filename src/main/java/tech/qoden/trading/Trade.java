package tech.qoden.trading;

import java.math.BigDecimal;
import java.util.Date;

public class Trade {



    public enum Type { BUY, SELL;}
    private BigDecimal price;

    private BigDecimal amount;
    private Symbol symbol;
    private Date date;
    private String id;
    private Type type;
    public Trade(BigDecimal price, BigDecimal amount, Symbol symbol, Date date, String id, Type type) {
        if (price == null) throw new IllegalArgumentException("price");
        if (amount == null) throw new IllegalArgumentException("amount");
        if (symbol == null) throw new IllegalArgumentException("symbol");
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id");

        this.price = price;
        this.amount = amount;
        this.symbol = symbol;
        this.date = date;
        this.id = id;
        this.type = type;
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

    public Date getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trade trade = (Trade) o;

        if (!price.equals(trade.price)) return false;
        if (!amount.equals(trade.amount)) return false;
        if (!symbol.equals(trade.symbol)) return false;
        if (!date.equals(trade.date)) return false;
        if (!id.equals(trade.id)) return false;
        return type == trade.type;

    }

    @Override
    public int hashCode() {
        int result = price.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + symbol.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %f at %f", symbol.getName(), type, id, amount, price);
    }

    public static int compareDate(Trade x, Trade y) {
        return y.getDate().compareTo(x.getDate());
    }
}
