
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;

public class ManageFlights extends Application {

    private TableView<Flight> table;
    private ObservableList<Flight> flights;

    private TextField flightNumber, airline, source, destination;
    private TextField departure, arrival, price, totalSeats, availableSeats;
    private DatePicker flightDate;

    @Override
    public void start(Stage stage) {

        Button backArrow = new Button("← Back");
        backArrow.setStyle(
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;"
        );
        backArrow.setPrefHeight(38);

        backArrow.setOnAction(e -> {
            stage.close();
            new Dashboard().start(new Stage());
        });

        Label title = new Label("✈ MANAGE FLIGHTS");
        title.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        flightNumber = new TextField();
        airline = new TextField();
        source = new TextField();
        destination = new TextField();
        departure = new TextField();
        arrival = new TextField();
        price = new TextField();
        totalSeats = new TextField();
        availableSeats = new TextField();
        flightDate = new DatePicker();

        flightNumber.setPromptText("Flight Number");
        airline.setPromptText("Airline");
        source.setPromptText("Source");
        destination.setPromptText("Destination");
        departure.setPromptText("yyyy-MM-dd HH:mm");
        arrival.setPromptText("yyyy-MM-dd HH:mm");
        price.setPromptText("Price");
        totalSeats.setPromptText("Total Seats");
        availableSeats.setPromptText("Available Seats");

        GridPane form = new GridPane();
        form.setPadding(new Insets(15));
        form.setHgap(10);
        form.setVgap(10);

        addField(form, "Flight Number", flightNumber, 0, 0);
        addField(form, "Airline", airline, 1, 0);
        addField(form, "Source", source, 2, 0);
        addField(form, "Destination", destination, 3, 0);
        addField(form, "Departure", departure, 0, 1);
        addField(form, "Arrival", arrival, 1, 1);
        addField(form, "Price", price, 2, 1);
        addField(form, "Total Seats", totalSeats, 3, 1);
        addField(form, "Available Seats", availableSeats, 0, 2);
        addField(form, "Flight Date", flightDate, 1, 2);

        Button add = new Button("Add Flight");
        Button update = new Button("Update Flight");
        Button delete = new Button("Delete Flight");
        Button clear = new Button("Clear");

        String buttonStyle =
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 8px;";

        for (Button b : new Button[]{add, update, delete, clear}) {
            b.setStyle(buttonStyle);
            b.setPrefHeight(38);
        }

        HBox buttons = new HBox(12, add, update, delete, clear);
        buttons.setAlignment(Pos.CENTER);

        table = new TableView<>();
        flights = FXCollections.observableArrayList();
        table.setItems(flights);

        addColumn("ID", "flightId");
        addColumn("Flight No", "flightNumber");
        addColumn("Airline", "airline");
        addColumn("Source", "source");
        addColumn("Destination", "destination");
        addColumn("Departure", "departure");
        addColumn("Arrival", "arrival");
        addColumn("Price", "price");
        addColumn("Total Seats", "totalSeats");
        addColumn("Available", "availableSeats");
        addColumn("Flight Date", "flightDate");

        table.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, selected) -> {
                if (selected != null) fillForm(selected);
            }
        );

        add.setOnAction(e -> addFlight());
        update.setOnAction(e -> updateFlight());
        delete.setOnAction(e -> deleteFlight());
        clear.setOnAction(e -> clearForm());

        HBox topBar = new HBox(backArrow);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(15, topBar, title, form, buttons, table);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffd6e7;");

        VBox.setVgrow(table, Priority.ALWAYS);

        stage.setScene(new Scene(root, 1200, 750));
        stage.setTitle("Admin - Manage Flights");
        stage.setMaximized(true);
        stage.show();

        loadFlights();
    }

    private void addField(
        GridPane grid, String label,
        Control control, int col, int row
    ) {
        grid.add(new Label(label), col * 2, row);
        grid.add(control, col * 2 + 1, row);
        control.setPrefWidth(190);
    }

    private void addColumn(String title, String property) {
        TableColumn<Flight, Object> column =
            new TableColumn<>(title);

        column.setCellValueFactory(
            new PropertyValueFactory<>(property)
        );

        table.getColumns().add(column);
    }

    private Connection getConnection() throws Exception {
        return DBConnection.getConnection();
    }

    private void loadFlights() {
        flights.clear();

        String sql =
            "SELECT FLIGHT_ID, FLIGHT_NUMBER, AIRLINE, SOURCE, " +
            "DESTINATION, DEPARTURE_TIME, ARRIVAL_TIME, PRICE, " +
            "TOTAL_SEATS, AVAILABLE_SEATS, FLIGHT_DATE " +
            "FROM FLIGHTS ORDER BY FLIGHT_ID";

        try (
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Timestamp dep = rs.getTimestamp("DEPARTURE_TIME");
                Timestamp arr = rs.getTimestamp("ARRIVAL_TIME");
                Date date = rs.getDate("FLIGHT_DATE");

                Flight f = new Flight(
                    rs.getInt("FLIGHT_ID"),
                    rs.getString("FLIGHT_NUMBER"),
                    rs.getString("AIRLINE"),
                    rs.getString("SOURCE"),
                    rs.getString("DESTINATION"),
                    dep == null ? "" : dep.toString(),
                    arr == null ? "" : arr.toString(),
                    rs.getDouble("PRICE"),
                    rs.getInt("TOTAL_SEATS"),
                    rs.getInt("AVAILABLE_SEATS"),
                    date == null ? "" : date.toString()
                );

                flights.add(f);
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void addFlight() {
        if (!validateForm()) return;

        String sql =
            "INSERT INTO FLIGHTS (" +
            "FLIGHT_ID, FLIGHT_NUMBER, AIRLINE, SOURCE, DESTINATION, " +
            "DEPARTURE_TIME, ARRIVAL_TIME, PRICE, TOTAL_SEATS, " +
            "AVAILABLE_SEATS, FLIGHT_DATE) " +
            "VALUES (FLIGHT_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            fillStatement(ps);
            ps.executeUpdate();

            showInfo("Flight added successfully!");
            clearForm();
            loadFlights();

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void updateFlight() {
        Flight selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a flight from the table.");
            return;
        }

        if (!validateForm()) return;

        String sql =
            "UPDATE FLIGHTS SET " +
            "FLIGHT_NUMBER=?, AIRLINE=?, SOURCE=?, DESTINATION=?, " +
            "DEPARTURE_TIME=?, ARRIVAL_TIME=?, PRICE=?, " +
            "TOTAL_SEATS=?, AVAILABLE_SEATS=?, FLIGHT_DATE=? " +
            "WHERE FLIGHT_ID=?";

        try (
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            fillStatement(ps);
            ps.setInt(11, selected.flightId);
            ps.executeUpdate();

            showInfo("Flight updated successfully!");
            clearForm();
            loadFlights();

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteFlight() {
        Flight selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a flight from the table.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Flight");
        confirm.setContentText(
            "Are you sure you want to delete " +
            selected.flightNumber + "?"
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        String sql = "DELETE FROM FLIGHTS WHERE FLIGHT_ID=?";

        try (
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, selected.flightId);
            ps.executeUpdate();

            showInfo("Flight deleted successfully!");
            clearForm();
            loadFlights();

        } catch (Exception ex) {
            showError(
                "Could not delete flight. It may have existing bookings.\n" +
                ex.getMessage()
            );
        }
    }

    private void fillStatement(PreparedStatement ps) throws Exception {
        ps.setString(1, flightNumber.getText().trim());
        ps.setString(2, airline.getText().trim());
        ps.setString(3, source.getText().trim());
        ps.setString(4, destination.getText().trim());

        ps.setTimestamp(5, Timestamp.valueOf(
            LocalDateTime.parse(
                departure.getText().trim().replace(" ", "T")
            )
        ));

        ps.setTimestamp(6, Timestamp.valueOf(
            LocalDateTime.parse(
                arrival.getText().trim().replace(" ", "T")
            )
        ));

        ps.setDouble(7, Double.parseDouble(price.getText().trim()));
        ps.setInt(8, Integer.parseInt(totalSeats.getText().trim()));
        ps.setInt(9, Integer.parseInt(availableSeats.getText().trim()));

        if (flightDate.getValue() == null) {
            ps.setNull(10, Types.DATE);
        } else {
            ps.setDate(10, Date.valueOf(flightDate.getValue()));
        }
    }

    private boolean validateForm() {
        try {
            if (
                flightNumber.getText().trim().isEmpty() ||
                airline.getText().trim().isEmpty() ||
                source.getText().trim().isEmpty() ||
                destination.getText().trim().isEmpty() ||
                departure.getText().trim().isEmpty() ||
                arrival.getText().trim().isEmpty() ||
                price.getText().trim().isEmpty() ||
                totalSeats.getText().trim().isEmpty() ||
                availableSeats.getText().trim().isEmpty()
            ) {
                showError("Please fill all required fields.");
                return false;
            }

            LocalDateTime dep = LocalDateTime.parse(
                departure.getText().trim().replace(" ", "T")
            );

            LocalDateTime arr = LocalDateTime.parse(
                arrival.getText().trim().replace(" ", "T")
            );

            if (!arr.isAfter(dep)) {
                showError("Arrival time must be after departure time.");
                return false;
            }

            double p = Double.parseDouble(price.getText().trim());
            int total = Integer.parseInt(totalSeats.getText().trim());
            int available = Integer.parseInt(
                availableSeats.getText().trim()
            );

            if (p < 0 || total <= 0 || available < 0 || available > total) {
                showError("Enter valid price and seat values.");
                return false;
            }

            return true;

        } catch (Exception ex) {
            showError(
                "Check date/time format and numeric values.\n" +
                "Departure/Arrival format: yyyy-MM-dd HH:mm"
            );
            return false;
        }
    }

    private void fillForm(Flight f) {
        flightNumber.setText(f.flightNumber);
        airline.setText(f.airline);
        source.setText(f.source);
        destination.setText(f.destination);

        departure.setText(
            f.departure.isEmpty() ? "" :
            f.departure.substring(0, 16).replace("T", " ")
        );

        arrival.setText(
            f.arrival.isEmpty() ? "" :
            f.arrival.substring(0, 16).replace("T", " ")
        );

        price.setText(String.valueOf(f.price));
        totalSeats.setText(String.valueOf(f.totalSeats));
        availableSeats.setText(String.valueOf(f.availableSeats));

        if (!f.flightDate.isEmpty()) {
            flightDate.setValue(
                java.time.LocalDate.parse(f.flightDate)
            );
        } else {
            flightDate.setValue(null);
        }
    }

    private void clearForm() {
        flightNumber.clear();
        airline.clear();
        source.clear();
        destination.clear();
        departure.clear();
        arrival.clear();
        price.clear();
        totalSeats.clear();
        availableSeats.clear();
        flightDate.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Something went wrong");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static class Flight {
        private final int flightId;
        private final String flightNumber;
        private final String airline;
        private final String source;
        private final String destination;
        private final String departure;
        private final String arrival;
        private final double price;
        private final int totalSeats;
        private final int availableSeats;
        private final String flightDate;

        public Flight(
            int flightId, String flightNumber, String airline,
            String source, String destination, String departure,
            String arrival, double price, int totalSeats,
            int availableSeats, String flightDate
        ) {
            this.flightId = flightId;
            this.flightNumber = flightNumber;
            this.airline = airline;
            this.source = source;
            this.destination = destination;
            this.departure = departure;
            this.arrival = arrival;
            this.price = price;
            this.totalSeats = totalSeats;
            this.availableSeats = availableSeats;
            this.flightDate = flightDate;
        }

        public int getFlightId() { return flightId; }
        public String getFlightNumber() { return flightNumber; }
        public String getAirline() { return airline; }
        public String getSource() { return source; }
        public String getDestination() { return destination; }
        public String getDeparture() { return departure; }
        public String getArrival() { return arrival; }
        public double getPrice() { return price; }
        public int getTotalSeats() { return totalSeats; }
        public int getAvailableSeats() { return availableSeats; }
        public String getFlightDate() { return flightDate; }
    }
}
