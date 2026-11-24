
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaymentScreen extends Application {

    private int bookingId;
    private double paymentAmount = 0.0;

    public PaymentScreen() {
    }

    public PaymentScreen(int bookingId) {
        this.bookingId = bookingId;
    }

    @Override
    public void start(Stage stage) {

        Label title = new Label("Payment");

        title.setStyle(
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        Label bookingLabel =
            new Label("Booking ID: " + bookingId);

        Label amountLabel =
            new Label("Loading amount...");

        amountLabel.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        Label methodLabel =
            new Label("Select Payment Method");

        ComboBox<String> paymentMethod = new ComboBox<>();

        paymentMethod.getItems().addAll(
            "UPI",
            "CARD",
            "NET_BANKING"
        );

        paymentMethod.setPromptText("Select Payment Method");
        paymentMethod.setPrefWidth(300);

        Button payButton = new Button("PAY NOW");

        payButton.setPrefWidth(300);
        payButton.setPrefHeight(50);

        payButton.setStyle(
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;"
        );

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        messageLabel.setStyle(
            "-fx-font-size: 15px;"
        );

        // Load booking amount
        loadBookingAmount(
            amountLabel,
            messageLabel,
            payButton
        );

        payButton.setOnAction(e -> {

            if (bookingId <= 0) {
                messageLabel.setText("Invalid Booking ID!");
                return;
            }

            if (paymentMethod.getValue() == null) {
                messageLabel.setText(
                    "Please select payment method."
                );
                return;
            }

            payButton.setDisable(true);

            messageLabel.setText(
                "Processing demo payment..."
            );

            messageLabel.setStyle(
                "-fx-text-fill: #d63384;"
            );

            try (Connection con = DBConnection.getConnection()) {

                con.setAutoCommit(false);

                try {

                    // Lock booking and check status
                    String checkSql =
                        "SELECT TOTAL_AMOUNT, BOOKING_STATUS " +
                        "FROM BOOKINGS " +
                        "WHERE BOOKING_ID = ? " +
                        "FOR UPDATE";

                    try (PreparedStatement ps =
                             con.prepareStatement(checkSql)) {

                        ps.setInt(1, bookingId);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new Exception(
                                    "Booking not found!"
                                );
                            }

                            String status =
                                rs.getString("BOOKING_STATUS");

                            if (status == null ||
                                !status.trim().equalsIgnoreCase("PENDING")) {

                                throw new Exception(
                                    "This booking is not pending payment."
                                );
                            }

                            paymentAmount =
                                rs.getDouble("TOTAL_AMOUNT");

                            if (paymentAmount <= 0) {
                                throw new Exception(
                                    "Invalid payment amount."
                                );
                            }
                        }
                    }

                    // Check existing successful payment
                    String existingPaymentSql =
                        "SELECT COUNT(*) FROM PAYMENTS " +
                        "WHERE BOOKING_ID = ? " +
                        "AND UPPER(TRIM(PAYMENT_STATUS)) = 'SUCCESS'";

                    try (PreparedStatement ps =
                             con.prepareStatement(existingPaymentSql)) {

                        ps.setInt(1, bookingId);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (rs.next() && rs.getInt(1) > 0) {
                                throw new Exception(
                                    "Payment already completed for this booking."
                                );
                            }
                        }
                    }

                    // Insert payment record
                    String paymentSql =
                        "INSERT INTO PAYMENTS " +
                        "(PAYMENT_ID, BOOKING_ID, PAYMENT_DATE, " +
                        "PAYMENT_METHOD, PAYMENT_AMOUNT, PAYMENT_STATUS) " +
                        "VALUES (PAYMENT_SEQ.NEXTVAL, ?, SYSDATE, ?, ?, ?)";

                    try (PreparedStatement ps =
                             con.prepareStatement(paymentSql)) {

                        ps.setInt(1, bookingId);
                        ps.setString(2, paymentMethod.getValue());
                        ps.setDouble(3, paymentAmount);
                        ps.setString(4, "SUCCESS");

                        ps.executeUpdate();
                    }

                    // Confirm booking
                    String updateSql =
                        "UPDATE BOOKINGS " +
                        "SET BOOKING_STATUS = 'CONFIRMED' " +
                        "WHERE BOOKING_ID = ? " +
                        "AND UPPER(TRIM(BOOKING_STATUS)) = 'PENDING'";

                    try (PreparedStatement ps =
                             con.prepareStatement(updateSql)) {

                        ps.setInt(1, bookingId);

                        if (ps.executeUpdate() != 1) {
                            throw new Exception(
                                "Booking confirmation failed."
                            );
                        }
                    }

                    con.commit();

                    messageLabel.setText(
                        "Demo Payment Successful!"
                    );

                    messageLabel.setStyle(
                        "-fx-text-fill: green;" +
                        "-fx-font-weight: bold;"
                    );

                    TicketScreen ticketScreen =
                        new TicketScreen(bookingId);

                    ticketScreen.start(new Stage());

                    stage.close();

                } catch (Exception ex) {

                    con.rollback();
                    throw ex;
                }

            } catch (Exception ex) {

                payButton.setDisable(false);

                messageLabel.setText(
                    "Payment Failed: " + ex.getMessage()
                );

                messageLabel.setStyle(
                    "-fx-text-fill: red;"
                );

                ex.printStackTrace();
            }
        });

        VBox card = new VBox(
            18,
            title,
            bookingLabel,
            amountLabel,
            methodLabel,
            paymentMethod,
            payButton,
            messageLabel
        );

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(550);

        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 15px;"
        );

        VBox root = new VBox(card);

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        root.setStyle(
            "-fx-background-color: #ffd6e7;"
        );

        stage.setScene(
            new Scene(root, 1000, 700)
        );

        stage.setTitle("Payment");
        stage.setMaximized(true);
        stage.show();
    }

    private void loadBookingAmount(
        Label amountLabel,
        Label messageLabel,
        Button payButton
    ) {

        if (bookingId <= 0) {
            amountLabel.setText("Invalid Booking ID");
            messageLabel.setText("Please create booking again.");
            payButton.setDisable(true);
            return;
        }

        String sql =
            "SELECT TOTAL_AMOUNT, BOOKING_STATUS " +
            "FROM BOOKINGS WHERE BOOKING_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    paymentAmount =
                        rs.getDouble("TOTAL_AMOUNT");

                    String status =
                        rs.getString("BOOKING_STATUS");

                    if (status == null ||
                        !status.trim().equalsIgnoreCase("PENDING")) {

                        amountLabel.setText(
                            "Booking is not pending."
                        );

                        messageLabel.setText(
                            "Payment is not available for this booking."
                        );

                        payButton.setDisable(true);
                        return;
                    }

                    if (paymentAmount <= 0) {
                        throw new Exception(
                            "Invalid booking amount."
                        );
                    }

                    amountLabel.setText(
                        String.format(
                            "Payment Amount: ₹%.2f",
                            paymentAmount
                        )
                    );

                } else {

                    throw new Exception("Booking not found!");
                }
            }

        } catch (Exception ex) {

            amountLabel.setText("Amount not available");

            messageLabel.setText(
                "Database Error: " + ex.getMessage()
            );

            payButton.setDisable(true);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
