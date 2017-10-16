package tech.qoden.app;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;
import tech.qoden.bitstamp.BitstampConnection;
import tech.qoden.trading.ExchangeConnection;
import tech.qoden.trading.Symbol;

public class BitstampSample extends javafx.application.Application {

    private ViewModel orderBookModel;
    private TradeRobot robot;
    private ExchangeConnection connection;

    @Override
    public void start(Stage primaryStage) throws Exception {

        LoggerFactory.getLogger("AAA").error("!!! ERR");
        LoggerFactory.getLogger("AAA").warn("!!! WARN");
        LoggerFactory.getLogger("AAA").debug("!!! DBG");

        Config config = new Config();
        connection = new BitstampConnection(Symbol.BTC_USD);

        orderBookModel = new ViewModel(connection, config);
        robot = new TradeRobot(connection, config);

        HBox orderBookPanel = new HBox();
        orderBookPanel.setPadding(new Insets(20, 10, 20, 10));
        orderBookPanel.setSpacing(20);

        VBox bidsPanel = createPositionsLayout("Bids", orderBookModel.getBids());
        VBox asksPanel = createPositionsLayout("Asks", orderBookModel.getAsks());
        VBox trades = createTradesLayout(orderBookModel.getTrades());

        orderBookPanel.getChildren().addAll(bidsPanel, asksPanel, trades);

        Scene scene = new Scene(orderBookPanel, 900, 500);
        primaryStage.setTitle("Bitstamp Trade Robot Demo");
        primaryStage.setScene(scene);
        primaryStage.show();

        connection.connect();
        robot.run();
    }

    @Override
    public void stop() throws Exception {
        robot.close();
        orderBookModel.close();
        connection.disconnect();
    }

    private VBox createPositionsLayout(String title, ObservableList<PositionViewModel> positions) {
        VBox bidsPanel = new VBox();
        Label titleLabel = new Label();
        titleLabel.setText(title);

        TableView<PositionViewModel> positionsTable = new TableView<>();

        TableColumn<PositionViewModel, String> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<PositionViewModel, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        //noinspection unchecked
        positionsTable.getColumns().addAll(priceColumn, amountColumn);
        positionsTable.setItems(positions);

        bidsPanel.getChildren().addAll(titleLabel, positionsTable);

        return bidsPanel;
    }

    private VBox createTradesLayout(ObservableList<TradeViewModel> trades) {
        VBox bidsPanel = new VBox();
        Label titleLabel = new Label();
        titleLabel.setText("Trades");

        TableView<TradeViewModel> tradesTable = new TableView<>();

        TableColumn<TradeViewModel, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<TradeViewModel, String> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<TradeViewModel, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<TradeViewModel, String> idColumn = new TableColumn<>("Id");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        //noinspection unchecked
        tradesTable.getColumns().addAll(idColumn, typeColumn, priceColumn, amountColumn);
        tradesTable.setItems(trades);

        bidsPanel.getChildren().addAll(titleLabel, tradesTable);

        return bidsPanel;
    }

    public static void main(String[] args) {

        BitstampSample.launch(args);
    }

}