
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BookingConfirmation extends Application {

    public static String passengerName = "Passenger";
    public static int passengerAge;
    public static String passengerGender = "";
    public static String passengerPassport = "";

    private int userId;
    private int flightId;
    private String selectedSeat;
    private double totalAmount;

    public BookingConfirmation() {
    }

    public BookingConfirmation(int flightId, String selectedSeat) {
        this.userId = LoginScreen.loggedInUserId;
        this.flightId = flightId;
        this.selectedSeat = selectedSeat;
    }

    @Override
    public void start(Stage stage) {

        Label title = new Label("Booking Confirmation");

        title.setStyle(
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        Label passengerLabel =
            new Label("Passenger: " + passengerName);

        Label ageLabel =
            new Label("Age: " + passengerAge);

        Label genderLabel =
            new Label("Gender: " + passengerGender);

        Label flightLabel =
            new Label("Flight ID: " + flightId);

        Label seatLabel =
            new Label("Selected Seat: " + selectedSeat);

        Label amountLabel =
            new Label("Total Amount: Loading...");

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        Button confirmButton = new Button("CONFIRM BOOKING");

        confirmButton.setPrefWidth(300);
        confirmButton.setPrefHeight(50);

        confirmButton.setStyle(
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );

        // Basic validation
        if (userId <= 0 || flightId <= 0 ||
            selectedSeat == null || selectedSeat.trim().isEmpty()) {

            messageLabel.setText(
                "Invalid login, flight or seat details. Please start again."
            );
            messageLabel.setStyle("-fx-text-fill: red;");
            confirmButton.setDisable(true);

        } else if (passengerName == null ||
                   passengerName.trim().isEmpty() ||
                   passengerAge <= 0 ||
                   passengerGender == null ||
                   passengerGender.trim().isEmpty() ||
                   passengerPassport == null ||
                   passengerPassport.trim().isEmpty()) {

            messageLabel.setText(
                "Passenger details missing. Please enter passenger details again."
            );
            messageLabel.setStyle("-fx-text-fill: red;");
            confirmButton.setDisable(true);

        } else {
            loadFlightPrice(amountLabel, messageLabel, confirmButton);
        }

        confirmButton.setOnAction(e -> {

            confirmButton.setDisable(true);
            messageLabel.setText("");

            try (Connection con = DBConnection.getConnection()) {

                con.setAutoCommit(false);

                try {
                    // Check flight and get actual price
                    String flightSql =
                        "SELECT PRICE, AVAILABLE_SEATS " +
                        "FROM FLIGHTS WHERE FLIGHT_ID = ? FOR UPDATE";

                    try (PreparedStatement ps =
                             con.prepareStatement(flightSql)) {

                        ps.setInt(1, flightId);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new Exception("Flight not found.");
                            }

                            totalAmount = rs.getDouble("PRICE");

                            if (rs.getInt("AVAILABLE_SEATS") <= 0) {
                                throw new Exception(
                                    "No seats available on this flight."
                                );
                            }
                        }
                    }

                    // Lock selected seat and verify availability
                    int seatId;

                    String seatSql =
                        "SELECT SEAT_ID FROM SEATS " +
                        "WHERE FLIGHT_ID = ? " +
                        "AND SEAT_NUMBER = ? " +
                        "AND UPPER(TRIM(SEAT_STATUS)) = 'AVAILABLE' " +
                        "FOR UPDATE";

                    try (PreparedStatement ps =
                             con.prepareStatement(seatSql)) {

                        ps.setInt(1, flightId);
                        ps.setString(2, selectedSeat);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new Exception(
                                    "Seat " + selectedSeat +
                                    " is not available. Please select another seat."
                                );
                            }

                            seatId = rs.getInt("SEAT_ID");
                        }
                    }

                    // Generate booking ID
                    int bookingId;

                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT BOOKING_SEQ.NEXTVAL FROM DUAL");
                         ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new Exception(
                                "Booking ID could not be generated."
                            );
                        }

                        bookingId = rs.getInt(1);
                    }

                    // Insert booking as PENDING
                    String insertBookingSql =
                        "INSERT INTO BOOKINGS " +
                        "(BOOKING_ID, USER_ID, FLIGHT_ID, SEAT_ID, " +
                        "BOOKING_DATE, BOOKING_STATUS, TOTAL_AMOUNT) " +
                        "VALUES (?, ?, ?, ?, SYSDATE, ?, ?)";

                    try (PreparedStatement ps =
                             con.prepareStatement(insertBookingSql)) {

                        ps.setInt(1, bookingId);
                        ps.setInt(2, userId);
                        ps.setInt(3, flightId);
                        ps.setInt(4, seatId);
                        ps.setString(5, "PENDING");
                        ps.setDouble(6, totalAmount);

                        ps.executeUpdate();
                    }

                    // Insert passenger details
                    String passengerSql =
                        "INSERT INTO PASSENGERS " +
                        "(PASSENGER_ID, BOOKING_ID, PASSENGER_NAME, " +
                        "ID_NUMBER, GENDER, AGE) " +
                        "VALUES (PASSENGER_SEQ.NEXTVAL, ?, ?, ?, ?, ?)";

                    try (PreparedStatement ps =
                             con.prepareStatement(passengerSql)) {

                        ps.setInt(1, bookingId);
                        ps.setString(2, passengerName);
                        ps.setString(3, passengerPassport);
                        ps.setString(4, passengerGender);
                        ps.setInt(5, passengerAge);

                        ps.executeUpdate();
                    }

                    // Reserve seat
                    String updateSeatSql =
                        "UPDATE SEATS SET SEAT_STATUS = 'BOOKED' " +
                        "WHERE SEAT_ID = ? " +
                        "AND UPPER(TRIM(SEAT_STATUS)) = 'AVAILABLE'";

                    try (PreparedStatement ps =
                             con.prepareStatement(updateSeatSql)) {

                        ps.setInt(1, seatId);

                        if (ps.executeUpdate() != 1) {
                            throw new Exception(
                                "Seat status could not be updated."
                            );
                        }
                    }

                    // Update available flight seats
                    String updateFlightSql =
                        "UPDATE FLIGHTS " +
                        "SET AVAILABLE_SEATS = AVAILABLE_SEATS - 1 " +
                        "WHERE FLIGHT_ID = ? AND AVAILABLE_SEATS > 0";

                    try (PreparedStatement ps =
                             con.prepareStatement(updateFlightSql)) {

                        ps.setInt(1, flightId);

                        if (ps.executeUpdate() != 1) {
                            throw new Exception(
                                "Flight seat count could not be updated."
                            );
                        }
                    }

                    con.commit();

                    messageLabel.setText("Booking created. Proceed to payment.");
                    messageLabel.setStyle("-fx-text-fill: green;");

                    PaymentScreen paymentScreen =
                        new PaymentScreen(bookingId);

                    paymentScreen.start(new Stage());

                    stage.close();

                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                }

            } catch (Exception ex) {

                confirmButton.setDisable(false);

                messageLabel.setText(
                    "Booking Failed: " + ex.getMessage()
                );

                messageLabel.setStyle(
                    "-fx-text-fill: red;" +
                    "-fx-font-size: 15px;"
                );

                ex.printStackTrace();
            }
        });

        VBox card = new VBox(
            15,
            title,
            passengerLabel,
            ageLabel,
            genderLabel,
            flightLabel,
            seatLabel,
            amountLabel,
            confirmButton,
            messageLabel
        );

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(650);

        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18px;"
        );

        VBox root = new VBox(card);

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        root.setStyle("-fx-background-color: #ffd6e7;");

        stage.setScene(new Scene(root, 1000, 700));
        stage.setTitle("Booking Confirmation");
        stage.setMaximized(true);
        stage.show();
    }

    private void loadFlightPrice(
        Label amountLabel,
        Label messageLabel,
        Button confirmButton
    ) {

        String sql =
            "SELECT PRICE FROM FLIGHTS WHERE FLIGHT_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, flightId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    totalAmount = rs.getDouble("PRICE");

                    amountLabel.setText(
                        String.format("Total Amount: ₹%.2f", totalAmount)
                    );

                } else {

                    amountLabel.setText("Flight price not found.");
                    messageLabel.setText("Flight does not exist.");
                    messageLabel.setStyle("-fx-text-fill: red;");
                    confirmButton.setDisable(true);
                }
            }

        } catch (Exception ex) {

            amountLabel.setText("Unable to load price.");

            messageLabel.setText(
                "Database Error: " + ex.getMessage()
            );

            messageLabel.setStyle("-fx-text-fill: red;");
            confirmButton.setDisable(true);

            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
