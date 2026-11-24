
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

public class ViewAllBookings extends Application {

    private TableView<Booking> table;
    private ObservableList<Booking> bookings;

    @Override
    public void start(Stage stage) {

        Button back = new Button("← Back");
        back.setStyle(
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;"
        );

        back.setOnAction(e -> {
            stage.close();
            new Dashboard().start(new Stage());
        });

        Label title = new Label("ALL BOOKINGS");
        title.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        table = new TableView<>();
        bookings = FXCollections.observableArrayList();
        table.setItems(bookings);

        addColumn("Booking ID", "bookingId");
        addColumn("User ID", "userId");
        addColumn("User Name", "userName");
        addColumn("Flight No", "flightNumber");
        addColumn("Source", "source");
        addColumn("Destination", "destination");
        addColumn("Seat No", "seatNumber");
        addColumn("Booking Date", "bookingDate");
        addColumn("Status", "status");
        addColumn("Amount", "amount");

        HBox top = new HBox(back);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(18, top, title, table);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffd6e7;");

        VBox.setVgrow(table, Priority.ALWAYS);

        stage.setScene(new Scene(root, 1200, 750));
        stage.setTitle("Admin - All Bookings");
        stage.setMaximized(true);
        stage.show();

        loadBookings();
    }

    private void addColumn(String title, String property) {
        TableColumn<Booking, Object> column =
            new TableColumn<>(title);

        column.setCellValueFactory(
            new PropertyValueFactory<>(property)
        );

        table.getColumns().add(column);
    }

    private void loadBookings() {

        bookings.clear();

        String sql =
            "SELECT B.BOOKING_ID, B.USER_ID, U.NAME AS USER_NAME, " +
            "F.FLIGHT_NUMBER, F.SOURCE, F.DESTINATION, " +
            "S.SEAT_NUMBER, B.BOOKING_DATE, B.BOOKING_STATUS, " +
            "B.TOTAL_AMOUNT " +
            "FROM BOOKINGS B " +
            "JOIN USERS U ON B.USER_ID = U.USER_ID " +
            "JOIN FLIGHTS F ON B.FLIGHT_ID = F.FLIGHT_ID " +
            "JOIN SEATS S ON B.SEAT_ID = S.SEAT_ID " +
            "ORDER BY B.BOOKING_ID DESC";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Booking b = new Booking(
                    rs.getInt("BOOKING_ID"),
                    rs.getInt("USER_ID"),
                    rs.getString("USER_NAME"),
                    rs.getString("FLIGHT_NUMBER"),
                    rs.getString("SOURCE"),
                    rs.getString("DESTINATION"),
                    rs.getString("SEAT_NUMBER"),
                    rs.getDate("BOOKING_DATE") == null
                        ? "" : rs.getDate("BOOKING_DATE").toString(),
                    rs.getString("BOOKING_STATUS"),
                    rs.getDouble("TOTAL_AMOUNT")
                );

                bookings.add(b);
            }

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText("Unable to load bookings");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Booking {

        private final int bookingId;
        private final int userId;
        private final String userName;
        private final String flightNumber;
        private final String source;
        private final String destination;
        private final String seatNumber;
        private final String bookingDate;
        private final String status;
        private final double amount;

        public Booking(
            int bookingId, int userId, String userName,
            String flightNumber, String source, String destination,
            String seatNumber, String bookingDate,
            String status, double amount
        ) {
            this.bookingId = bookingId;
            this.userId = userId;
            this.userName = userName;
            this.flightNumber = flightNumber;
            this.source = source;
            this.destination = destination;
            this.seatNumber = seatNumber;
            this.bookingDate = bookingDate;
            this.status = status;
            this.amount = amount;
        }

        public int getBookingId() { return bookingId; }
        public int getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getFlightNumber() { return flightNumber; }
        public String getSource() { return source; }
        public String getDestination() { return destination; }
        public String getSeatNumber() { return seatNumber; }
        public String getBookingDate() { return bookingDate; }
        public String getStatus() { return status; }
        public double getAmount() { return amount; }
    }
}
