
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;

public class FlightSearch extends Application {

    private ComboBox<String> sourceBox;
    private ComboBox<String> destinationBox;
    private TableView<Flight> table;
    private Label messageLabel;

    @Override
    public void start(Stage stage) {

        // BACK BUTTON
        Button backButton = new Button("← BACK");

        backButton.setPrefSize(120, 40);
        backButton.setStyle(
            "-fx-background-color: #D63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;"
        );

        backButton.setOnAction(e -> {
            try {
                Dashboard dashboard = new Dashboard();
                dashboard.start(new Stage());
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Unable to open Dashboard: " + ex.getMessage());
            }
        });

        HBox backBox = new HBox(backButton);
        backBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("✈ Flight Search");

        title.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        Label fromLabel = createLabel("From");

        sourceBox = new ComboBox<>();
        sourceBox.setPromptText("Select From");
        sourceBox.setPrefSize(220, 42);

        Label toLabel = createLabel("To");

        destinationBox = new ComboBox<>();
        destinationBox.setPromptText("Select To");
        destinationBox.setPrefSize(220, 42);

        VBox fromBox = new VBox(8, fromLabel, sourceBox);
        VBox toBox = new VBox(8, toLabel, destinationBox);

        Button searchButton = new Button("SEARCH FLIGHTS");
        searchButton.setPrefSize(190, 44);

        searchButton.setStyle(
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10px;"
        );

        searchButton.setOnMouseEntered(e ->
            searchButton.setStyle(
                "-fx-background-color: #f06aa5;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10px;"
            )
        );

        searchButton.setOnMouseExited(e ->
            searchButton.setStyle(
                "-fx-background-color: #d63384;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10px;"
            )
        );

        HBox searchArea = new HBox(
            25, fromBox, toBox, searchButton
        );

        searchArea.setAlignment(Pos.CENTER);

        messageLabel = new Label();

        messageLabel.setStyle(
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        table = new TableView<>();

        table.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY
        );

        // FLIGHT ID
        TableColumn<Flight, Number> idColumn =
            new TableColumn<>("ID");

        idColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(
                data.getValue().flightId
            )
        );

        // DATE
        TableColumn<Flight, String> dateColumn =
            new TableColumn<>("Date");

        dateColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(
                data.getValue().departureDate
            )
        );

        // FLIGHT NUMBER
        TableColumn<Flight, String> flightColumn =
            new TableColumn<>("Flight");

        flightColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(
                data.getValue().flightNumber
            )
        );

        // AIRLINE
        TableColumn<Flight, String> airlineColumn =
            new TableColumn<>("Airline");

        airlineColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(
                data.getValue().airline
            )
        );

        // SOURCE
        TableColumn<Flight, String> fromColumn =
            new TableColumn<>("From");

        fromColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(
                data.getValue().source
            )
        );

        // DESTINATION
        TableColumn<Flight, String> toColumn =
            new TableColumn<>("To");

        toColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(
                data.getValue().destination
            )
        );

        // PRICE
        TableColumn<Flight, Number> priceColumn =
            new TableColumn<>("Price");

        priceColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(
                data.getValue().price
            )
        );

        // AVAILABLE SEATS
        TableColumn<Flight, Number> seatsColumn =
            new TableColumn<>("Available Seats");

        seatsColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(
                data.getValue().availableSeats
            )
        );

        // BOOK BUTTON
        TableColumn<Flight, String> bookColumn =
            new TableColumn<>("Action");

        bookColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>("BOOK")
        );

        bookColumn.setCellFactory(column ->
            new TableCell<Flight, String>() {

                private final Button bookButton =
                    new Button("BOOK NOW");

                {
                    bookButton.setPrefSize(120, 35);

                    bookButton.setStyle(
                        "-fx-background-color: #d63384;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10px;"
                    );

                    bookButton.setOnAction(e -> {

                        Flight selectedFlight =
                            getTableView()
                            .getItems()
                            .get(getIndex());

                        if (selectedFlight.availableSeats <= 0) {
                            showError(
                                "No seats available for this flight."
                            );
                            return;
                        }

                        int currentUserId =
                            LoginScreen.loggedInUserId;

                        if (currentUserId <= 0) {
                            showError("Please login again.");
                            return;
                        }

                        try {

                            PassengerDetails details =
                                new PassengerDetails(
                                    selectedFlight.flightId,
                                    currentUserId
                                );

                            details.start(new Stage());

                        } catch (Exception ex) {

                            ex.printStackTrace();

                            showError(
                                "Unable to open Passenger Details: "
                                + ex.getMessage()
                            );
                        }
                    });
                }

                @Override
                protected void updateItem(
                    String item, boolean empty) {

                    super.updateItem(item, empty);

                    if (empty || getIndex() < 0 ||
                        getIndex() >= getTableView()
                        .getItems().size()) {

                        setGraphic(null);

                    } else {

                        Flight flight =
                            getTableView()
                            .getItems()
                            .get(getIndex());

                        bookButton.setDisable(
                            flight.availableSeats <= 0
                        );

                        setGraphic(bookButton);
                    }
                }
            });

        // ADD ALL COLUMNS
        table.getColumns().addAll(
            idColumn,
            dateColumn,
            flightColumn,
            airlineColumn,
            fromColumn,
            toColumn,
            priceColumn,
            seatsColumn,
            bookColumn
        );

        // LOAD CITIES
        loadLocations();

        // SEARCH BUTTON
        searchButton.setOnAction(e -> searchFlights());

        VBox root = new VBox(
            22,
            backBox,
            title,
            searchArea,
            messageLabel,
            table
        );

        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(25));

        root.setStyle(
            "-fx-background-color: #ffd6e7;"
        );

        Scene scene = new Scene(root, 1200, 750);

        stage.setTitle("Flight Search");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }

    // LABEL STYLE
    private Label createLabel(String text) {

        Label label = new Label(text);

        label.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        return label;
    }

    // LOAD CITIES FROM DATABASE
    private void loadLocations() {

        ObservableList<String> locations =
            FXCollections.observableArrayList();

        String sql =
            "SELECT DISTINCT SOURCE AS LOCATION FROM FLIGHTS " +
            "UNION " +
            "SELECT DISTINCT DESTINATION AS LOCATION FROM FLIGHTS " +
            "ORDER BY LOCATION";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                locations.add(rs.getString("LOCATION"));
            }

            sourceBox.setItems(
                FXCollections.observableArrayList(locations)
            );

            destinationBox.setItems(
                FXCollections.observableArrayList(locations)
            );

        } catch (Exception e) {

            e.printStackTrace();

            messageLabel.setText(
                "Database error: " + e.getMessage()
            );
        }
    }

    // SEARCH FLIGHTS
    private void searchFlights() {

        String source = sourceBox.getValue();
        String destination = destinationBox.getValue();

        table.getItems().clear();

        if (source == null || source.isEmpty()) {
            messageLabel.setText("Please select From.");
            return;
        }

        if (destination == null || destination.isEmpty()) {
            messageLabel.setText("Please select To.");
            return;
        }

        if (source.equalsIgnoreCase(destination)) {
            messageLabel.setText(
                "From and To cannot be same."
            );
            return;
        }

        String sql =
            "SELECT FLIGHT_ID, FLIGHT_NUMBER, AIRLINE, " +
            "SOURCE, DESTINATION, PRICE, AVAILABLE_SEATS, " +
            "DEPARTURE_TIME " +
            "FROM FLIGHTS " +
            "WHERE UPPER(TRIM(SOURCE)) = UPPER(TRIM(?)) " +
            "AND UPPER(TRIM(DESTINATION)) = UPPER(TRIM(?)) " +
            "ORDER BY DEPARTURE_TIME";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, source);
            ps.setString(2, destination);

            try (ResultSet rs = ps.executeQuery()) {

                ObservableList<Flight> flights =
                    FXCollections.observableArrayList();

                while (rs.next()) {

                    Flight flight = new Flight();

                    flight.flightId =
                        rs.getInt("FLIGHT_ID");

                    flight.flightNumber =
                        rs.getString("FLIGHT_NUMBER");

                    flight.airline =
                        rs.getString("AIRLINE");

                    flight.source =
                        rs.getString("SOURCE");

                    flight.destination =
                        rs.getString("DESTINATION");

                    flight.price =
                        rs.getDouble("PRICE");

                    flight.availableSeats =
                        rs.getInt("AVAILABLE_SEATS");

                    java.sql.Timestamp departure =
                        rs.getTimestamp("DEPARTURE_TIME");

                    if (departure != null) {
                        flight.departureDate =
                            departure.toLocalDateTime()
                            .format(
                                DateTimeFormatter.ofPattern(
                                    "dd-MM-yyyy"
                                )
                            );
                    } else {
                        flight.departureDate = "";
                    }

                    flights.add(flight);
                }

                table.setItems(flights);

                if (flights.isEmpty()) {
                    messageLabel.setText(
                        "No flights found for this route."
                    );
                } else {
                    messageLabel.setText(
                        flights.size() + " flight(s) found."
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            messageLabel.setText(
                "Database error: " + e.getMessage()
            );
        }
    }

    // ERROR MESSAGE
    private void showError(String text) {

        Alert alert =
            new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText("Something went wrong");
        alert.setContentText(text);

        alert.showAndWait();
    }

    // FLIGHT CLASS
    public static class Flight {

        int flightId;
        String departureDate;
        String flightNumber;
        String airline;
        String source;
        String destination;
        double price;
        int availableSeats;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
