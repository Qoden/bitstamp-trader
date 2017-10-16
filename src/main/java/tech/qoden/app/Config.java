package tech.qoden.app;

public class Config {
    private int tradeRobotUpTicks = 2;
    private int tradeRobotDownTicks = 2;
    private int orderBookLength = 10;

    public int getOrderBookLength() {
        return orderBookLength;
    }

    public void setOrderBookLength(int orderBookLength) {
        this.orderBookLength = orderBookLength;
    }

    public int getTradeRobotUpTicks() {
        return tradeRobotUpTicks;
    }

    public void setTradeRobotUpTicks(int tradeRobotUpTicks) {
        this.tradeRobotUpTicks = tradeRobotUpTicks;
    }

    public int getTradeRobotDownTicks() {
        return tradeRobotDownTicks;
    }

    public void setTradeRobotDownTicks(int tradeRobotDownTicks) {
        this.tradeRobotDownTicks = tradeRobotDownTicks;
    }
}
