
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class MyBookings extends Application {

    private final int userId = LoginScreen.loggedInUserId;

    private final String PINK = "#D92F87";
    private final String LIGHT_PINK = "#FFF5FA";
    private final String BORDER_PINK = "#FFACD3";

    private VBox bookingList;
    private Label countLabel;

    @Override
    public void start(Stage stage) {

        Label heading = new Label("▣ My Bookings");
        heading.setFont(Font.font("Arial", 34));
        heading.setTextFill(Color.web(PINK));
        heading.setStyle("-fx-font-weight: bold;");

        countLabel = new Label("Loading bookings...");
        countLabel.setFont(Font.font("Arial", 14));
        countLabel.setTextFill(Color.web("#777777"));

        VBox top = new VBox(8, heading, countLabel);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(25, 10, 20, 10));

        bookingList = new VBox(20);
        bookingList.setPadding(new Insets(20));
        bookingList.setStyle("-fx-background-color: #F3F3F3;");

        ScrollPane scrollPane = new ScrollPane(bookingList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background: #F3F3F3;" +
            "-fx-background-color: #F3F3F3;"
        );

        Button refreshButton = new Button("Refresh");
        stylePinkButton(refreshButton);
        refreshButton.setOnAction(e -> loadBookings());

        Button backButton = new Button("← Back to Dashboard");
        stylePinkButton(backButton);

        backButton.setOnAction(e -> {
            try {
                new Dashboard().start(new Stage());
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                showMessage(
                    Alert.AlertType.ERROR,
                    "Unable to open Dashboard: " + ex.getMessage()
                );
            }
        });

        HBox bottom = new HBox(15, refreshButton, backButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(12, 10, 20, 10));
        bottom.setStyle("-fx-background-color: " + LIGHT_PINK + ";");

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(scrollPane);
        root.setBottom(bottom);
        root.setStyle("-fx-background-color: " + LIGHT_PINK + ";");

        Scene scene = new Scene(root, 1200, 700);

        stage.setTitle("My Bookings");
        stage.setScene(scene);
        stage.setMinWidth(850);
        stage.setMinHeight(550);

        if (userId <= 0) {
            countLabel.setText("Please login first.");
        } else {
            loadBookings();
        }

        stage.show();
    }

    private void stylePinkButton(Button button) {

        button.setFont(Font.font("Arial", 16));
        button.setTextFill(Color.WHITE);

        button.setStyle(
            "-fx-background-color: " + PINK + ";" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-cursor: hand;"
        );
    }

    private void loadBookings() {

        bookingList.getChildren().clear();

        if (userId <= 0) {
            countLabel.setText("Please login again.");
            return;
        }

        String sql =
            "SELECT B.BOOKING_ID, B.FLIGHT_ID, " +
            "B.SEAT_ID, S.SEAT_NUMBER, B.BOOKING_DATE, " +
            "B.BOOKING_STATUS, B.TOTAL_AMOUNT, " +
            "F.FLIGHT_NUMBER, F.SOURCE, F.DESTINATION, " +
            "F.FLIGHT_DATE " +
            "FROM BOOKINGS B " +
            "LEFT JOIN SEATS S ON B.SEAT_ID = S.SEAT_ID " +
            "LEFT JOIN FLIGHTS F ON B.FLIGHT_ID = F.FLIGHT_ID " +
            "WHERE B.USER_ID = ? " +
            "ORDER BY B.BOOKING_ID DESC";

        int total = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    total++;

                    int bookingId = rs.getInt("BOOKING_ID");
                    int flightId = rs.getInt("FLIGHT_ID");

                    String seat = rs.getString("SEAT_NUMBER");

                    Timestamp bookingTimestamp =
                        rs.getTimestamp("BOOKING_DATE");

                    java.sql.Date journeySqlDate =
                        rs.getDate("FLIGHT_DATE");

                    String bookingDate =
                        bookingTimestamp == null
                        ? "N/A"
                        : bookingTimestamp.toString();

                    String journeyDate =
                        journeySqlDate == null
                        ? "N/A"
                        : journeySqlDate.toLocalDate().format(
                            DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        );

                    String status = rs.getString("BOOKING_STATUS");
                    String flightNumber = rs.getString("FLIGHT_NUMBER");
                    String source = rs.getString("SOURCE");
                    String destination = rs.getString("DESTINATION");

                    double amount = rs.getDouble("TOTAL_AMOUNT");

                    if (seat == null) seat = "N/A";
                    if (status == null) status = "UNKNOWN";
                    if (flightNumber == null) flightNumber = "N/A";
                    if (source == null) source = "N/A";
                    if (destination == null) destination = "N/A";

                    bookingList.getChildren().add(
                        createBookingCard(
                            bookingId,
                            flightId,
                            seat,
                            bookingDate,
                            journeyDate,
                            status,
                            amount,
                            flightNumber,
                            source,
                            destination
                        )
                    );
                }
            }

            countLabel.setText("Total Bookings: " + total);

            if (total == 0) {
                Label empty = new Label("No bookings found.");
                empty.setFont(Font.font("Arial", 18));
                bookingList.getChildren().add(empty);
            }

        } catch (Exception ex) {

            ex.printStackTrace();
            bookingList.getChildren().clear();

            Label error = new Label(
                "Bookings load thai nathi.\n" + ex.getMessage()
            );

            error.setWrapText(true);
            error.setFont(Font.font("Arial", 16));
            error.setTextFill(Color.RED);

            bookingList.getChildren().add(error);
            countLabel.setText("Unable to load bookings");
        }
    }

    private VBox createBookingCard(
        int bookingId,
        int flightId,
        String seat,
        String bookingDate,
        String journeyDate,
        String status,
        double amount,
        String flightNumber,
        String source,
        String destination) {

        Label bookingIdLabel =
            new Label("Booking ID: " + bookingId);

        Label flightIdLabel =
            new Label("Flight: " + flightId + " - " + flightNumber);

        Label routeLabel =
            new Label("Route: " + source + " → " + destination);

        Label seatLabel =
            new Label("Seat: " + seat);

        Label bookingDateLabel =
            new Label("Booking Date: " + bookingDate);

        Label journeyDateLabel =
            new Label("Journey Date: " + journeyDate);

        Label statusLabel =
            new Label("Status: " + status);

        Label amountLabel =
            new Label(String.format("Amount: ₹%.2f", amount));

        Label[] labels = {
            bookingIdLabel,
            flightIdLabel,
            routeLabel,
            seatLabel,
            bookingDateLabel,
            journeyDateLabel,
            statusLabel,
            amountLabel
        };

        for (Label label : labels) {
            label.setFont(Font.font("Arial", 18));
            label.setTextFill(Color.web("#222222"));
        }

        VBox card = new VBox(7);
        card.setPadding(new Insets(20));
        card.setMaxWidth(Double.MAX_VALUE);

        card.setBackground(
            new Background(
                new BackgroundFill(
                    Color.WHITE,
                    new CornerRadii(16),
                    Insets.EMPTY
                )
            )
        );

        card.setBorder(
            new Border(
                new BorderStroke(
                    Color.web(BORDER_PINK),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(16),
                    new BorderWidths(1.3)
                )
            )
        );

        Button ticketButton = new Button("View Ticket");
        stylePinkButton(ticketButton);

        ticketButton.setOnAction(e -> {

            if (!"CONFIRMED".equalsIgnoreCase(status)) {
                showMessage(
                    Alert.AlertType.INFORMATION,
                    "This booking is not confirmed yet."
                );
                return;
            }

            try {
                new TicketScreen(bookingId).start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
                showMessage(
                    Alert.AlertType.ERROR,
                    "Unable to open ticket: " + ex.getMessage()
                );
            }
        });

        card.getChildren().addAll(
            bookingIdLabel,
            flightIdLabel,
            routeLabel,
            seatLabel,
            bookingDateLabel,
            journeyDateLabel,
            statusLabel,
            amountLabel,
            ticketButton
        );

        if ("CONFIRMED".equalsIgnoreCase(status)) {

            Button cancelButton = new Button("Cancel Booking");

            cancelButton.setFont(Font.font("Arial", 16));
            cancelButton.setTextFill(Color.WHITE);

            cancelButton.setStyle(
                "-fx-background-color: #F13737;" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 9 14 9 14;" +
                "-fx-cursor: hand;"
            );

            cancelButton.setOnAction(e ->
                cancelBooking(bookingId)
            );

            card.getChildren().add(cancelButton);

        } else {

            Label info = new Label(
                "Booking is " + status +
                " — cancellation not available."
            );

            info.setFont(Font.font("Arial", 15));
            info.setTextFill(Color.web("#888888"));

            card.getChildren().add(info);
        }

        return card;
    }

    private void cancelBooking(int bookingId) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Cancel Booking");
        confirm.setHeaderText("Cancel Booking ID: " + bookingId + "?");

        confirm.setContentText(
            "Are you sure you want to cancel this booking?"
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL)
            != ButtonType.OK) {
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                int seatId;
                int flightId;

                String findSql =
                    "SELECT SEAT_ID, FLIGHT_ID " +
                    "FROM BOOKINGS " +
                    "WHERE BOOKING_ID = ? " +
                    "AND USER_ID = ? " +
                    "AND BOOKING_STATUS = 'CONFIRMED' " +
                    "FOR UPDATE";

                try (PreparedStatement ps =
                         con.prepareStatement(findSql)) {

                    ps.setInt(1, bookingId);
                    ps.setInt(2, userId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new Exception(
                                "Booking already cancelled or not found."
                            );
                        }

                        seatId = rs.getInt("SEAT_ID");

                        if (rs.wasNull()) {
                            throw new Exception(
                                "Seat information not found."
                            );
                        }

                        flightId = rs.getInt("FLIGHT_ID");
                    }
                }

                String seatSql =
                    "UPDATE SEATS " +
                    "SET SEAT_STATUS = 'AVAILABLE' " +
                    "WHERE SEAT_ID = ? " +
                    "AND FLIGHT_ID = ? " +
                    "AND UPPER(TRIM(SEAT_STATUS)) = 'BOOKED'";

                try (PreparedStatement ps =
                         con.prepareStatement(seatSql)) {

                    ps.setInt(1, seatId);
                    ps.setInt(2, flightId);

                    if (ps.executeUpdate() != 1) {
                        throw new Exception(
                            "Seat could not be released. Cancellation stopped."
                        );
                    }
                }

                String flightSql =
                    "UPDATE FLIGHTS " +
                    "SET AVAILABLE_SEATS = " +
                    "LEAST(TOTAL_SEATS, AVAILABLE_SEATS + 1) " +
                    "WHERE FLIGHT_ID = ?";

                try (PreparedStatement ps =
                         con.prepareStatement(flightSql)) {

                    ps.setInt(1, flightId);

                    if (ps.executeUpdate() != 1) {
                        throw new Exception(
                            "Flight seat count could not be updated."
                        );
                    }
                }

                String updateBooking =
                    "UPDATE BOOKINGS " +
                    "SET BOOKING_STATUS = 'CANCELLED' " +
                    "WHERE BOOKING_ID = ? " +
                    "AND USER_ID = ? " +
                    "AND BOOKING_STATUS = 'CONFIRMED'";

                try (PreparedStatement ps =
                         con.prepareStatement(updateBooking)) {

                    ps.setInt(1, bookingId);
                    ps.setInt(2, userId);

                    if (ps.executeUpdate() != 1) {
                        throw new Exception(
                            "Booking could not be cancelled."
                        );
                    }
                }

                con.commit();

                showMessage(
                    Alert.AlertType.INFORMATION,
                    "Booking cancelled successfully!"
                );

                loadBookings();

            } catch (Exception ex) {

                con.rollback();
                throw ex;
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            showMessage(
                Alert.AlertType.ERROR,
                "Cancellation failed:\n" + ex.getMessage()
            );
        }
    }

    private void showMessage(
        Alert.AlertType type,
        String message
    ) {

        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
