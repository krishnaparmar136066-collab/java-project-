
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.format.DateTimeFormatter;

public class TicketScreen extends Application {

    private final int bookingId;

    public TicketScreen() {
        this.bookingId = 0;
    }

    public TicketScreen(int bookingId) {
        this.bookingId = bookingId;
    }

    @Override
    public void start(Stage stage) {

        Label title = new Label("✈ FLIGHT BOOKING TICKET");

        title.setStyle(
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        Label details = new Label();

        details.setStyle(
            "-fx-font-size: 17px;" +
            "-fx-line-spacing: 8px;" +
            "-fx-text-fill: #333333;"
        );

        details.setWrapText(true);
        details.setMaxWidth(700);

        Label message = new Label();

        Button close = new Button("CLOSE");

        close.setPrefWidth(180);
        close.setPrefHeight(45);

        close.setStyle(
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );

        close.setOnAction(e -> stage.close());

        VBox card = new VBox(
            20,
            title,
            details,
            message,
            close
        );

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));
        card.setMaxWidth(800);

        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: #f3a6c8;" +
            "-fx-border-radius: 18;"
        );

        VBox root = new VBox(card);

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25));

        root.setStyle(
            "-fx-background-color: #fff0f6;"
        );

        if (bookingId <= 0) {

            details.setText("Invalid Booking ID");

        } else {

            String sql =
                "SELECT b.BOOKING_ID, " +
                "b.BOOKING_STATUS, " +
                "b.TOTAL_AMOUNT, " +
                "b.BOOKING_DATE, " +

                "p.PASSENGER_NAME, " +
                "p.AGE, " +
                "p.GENDER, " +

                "f.FLIGHT_NUMBER, " +
                "f.AIRLINE, " +
                "f.SOURCE, " +
                "f.DESTINATION, " +
                "f.FLIGHT_DATE, " +
                "f.DEPARTURE_TIME, " +
                "f.ARRIVAL_TIME, " +

                "s.SEAT_NUMBER, " +

                "(SELECT MAX(pm.PAYMENT_METHOD) " +
                "FROM PAYMENTS pm " +
                "WHERE pm.BOOKING_ID = b.BOOKING_ID) AS PAYMENT_METHOD, " +

                "(SELECT MAX(pm.PAYMENT_STATUS) " +
                "FROM PAYMENTS pm " +
                "WHERE pm.BOOKING_ID = b.BOOKING_ID) AS PAYMENT_STATUS " +

                "FROM BOOKINGS b " +

                "JOIN PASSENGERS p " +
                "ON p.BOOKING_ID = b.BOOKING_ID " +

                "JOIN FLIGHTS f " +
                "ON f.FLIGHT_ID = b.FLIGHT_ID " +

                "LEFT JOIN SEATS s " +
                "ON s.SEAT_ID = b.SEAT_ID " +

                "WHERE b.BOOKING_ID = ?";

            try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
            ) {

                ps.setInt(1, bookingId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        String bookingStatus =
                            rs.getString("BOOKING_STATUS");

                        String paymentStatus =
                            rs.getString("PAYMENT_STATUS");

                        String paymentMethod =
                            rs.getString("PAYMENT_METHOD");

                        if (paymentStatus == null) {
                            paymentStatus = "NOT PAID";
                        }

                        if (paymentMethod == null) {
                            paymentMethod = "Not Available";
                        }

                        Date journeyDate =
                            rs.getDate("FLIGHT_DATE");

                        String formattedDate =
                            journeyDate == null
                            ? "Not Available"
                            : journeyDate.toLocalDate().format(
                                DateTimeFormatter.ofPattern("dd-MM-yyyy")
                            );

                        Timestamp departure =
                            rs.getTimestamp("DEPARTURE_TIME");

                        Timestamp arrival =
                            rs.getTimestamp("ARRIVAL_TIME");

                        String departureText =
                            departure == null
                            ? "Not Available"
                            : departure.toLocalDateTime().format(
                                DateTimeFormatter.ofPattern(
                                    "dd-MM-yyyy hh:mm a"
                                )
                            );

                        String arrivalText =
                            arrival == null
                            ? "Not Available"
                            : arrival.toLocalDateTime().format(
                                DateTimeFormatter.ofPattern(
                                    "dd-MM-yyyy hh:mm a"
                                )
                            );

                        String passenger =
                            rs.getString("PASSENGER_NAME");

                        String seat =
                            rs.getString("SEAT_NUMBER");

                        if (passenger == null) {
                            passenger = "Not Available";
                        }

                        if (seat == null) {
                            seat = "Not Available";
                        }

                        details.setText(

                            "TICKET NO: FBS-" +
                            rs.getInt("BOOKING_ID") + "\n\n" +

                            "PASSENGER DETAILS\n" +
                            "Passenger Name: " +
                            passenger + "\n" +

                            "Age: " +
                            rs.getInt("AGE") + "\n" +

                            "Gender: " +
                            rs.getString("GENDER") + "\n\n" +

                            "FLIGHT DETAILS\n" +
                            "Flight Number: " +
                            rs.getString("FLIGHT_NUMBER") + "\n" +

                            "Airline: " +
                            rs.getString("AIRLINE") + "\n" +

                            "From: " +
                            rs.getString("SOURCE") + "\n" +

                            "To: " +
                            rs.getString("DESTINATION") + "\n" +

                            "Journey Date: " +
                            formattedDate + "\n\n" +

                            "Departure: " +
                            departureText + "\n" +

                            "Arrival: " +
                            arrivalText + "\n\n" +

                            "Seat Number: " +
                            seat + "\n\n" +

                            "BOOKING DETAILS\n" +
                            "Booking ID: " +
                            rs.getInt("BOOKING_ID") + "\n" +

                            "Booking Date: " +
                            rs.getTimestamp("BOOKING_DATE") + "\n" +

                            "Total Amount: ₹" +
                            rs.getBigDecimal("TOTAL_AMOUNT") + "\n\n" +

                            "PAYMENT DETAILS\n" +
                            "Payment Method: " +
                            paymentMethod + "\n" +

                            "Payment Status: " +
                            paymentStatus + "\n" +

                            "Booking Status: " +
                            bookingStatus
                        );

                        if ("CONFIRMED".equalsIgnoreCase(bookingStatus)
                            && "SUCCESS".equalsIgnoreCase(paymentStatus)) {

                            message.setText(
                                "Booking Confirmed! Have a Safe Journey!"
                            );

                            message.setStyle(
                                "-fx-text-fill: green;" +
                                "-fx-font-size: 18px;" +
                                "-fx-font-weight: bold;"
                            );

                        } else {

                            message.setText(
                                "This booking is not a confirmed ticket yet."
                            );

                            message.setStyle(
                                "-fx-text-fill: #d63384;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;"
                            );
                        }

                    } else {

                        details.setText(
                            "Booking details not found for ID " +
                            bookingId
                        );
                    }
                }

            } catch (Exception ex) {

                details.setText(
                    "Could not load ticket: " +
                    ex.getMessage()
                );

                ex.printStackTrace();
            }
        }

        stage.setTitle("Final Flight Ticket");

        stage.setScene(
            new Scene(root, 1000, 750)
        );

        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
