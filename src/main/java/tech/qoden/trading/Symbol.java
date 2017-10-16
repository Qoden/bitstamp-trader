package tech.qoden.trading;

public class Symbol {

    public static final Symbol BTC_USD = new Symbol("BTC/USD");
    private String name;

    public Symbol(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return name.equals(symbol.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
