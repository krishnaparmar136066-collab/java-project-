
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class SeatSelection extends Application {

    private int flightId;
    private int userId;
    private String selectedSeat = null;

    private Label selectedLabel;
    private Label messageLabel;
    private GridPane seatGrid;
    private Stage primaryStage;

    private final Map<String, String> seatStatus = new HashMap<>();
    private final Map<String, Button> seatButtons = new HashMap<>();

    private final String PINK = "#F3A6C8";
    private final String GREEN = "#28A745";
    private final String DARK_PINK = "#D63384";

    public SeatSelection() {
        this.flightId = 1;
        this.userId = 0;
    }

    public SeatSelection(int flightId) {
        this.flightId = flightId;
        this.userId = LoginScreen.loggedInUserId;
    }

    public SeatSelection(int flightId, int userId) {
        this.flightId = flightId;
        this.userId = userId;
    }

    @Override
    public void start(Stage stage) {

        primaryStage = stage;

        selectedLabel = new Label("Selected Seat: None");
        messageLabel = new Label("Select your seat");

        selectedLabel.setFont(
            Font.font("Arial", FontWeight.BOLD, 17)
        );

        messageLabel.setFont(
            Font.font("Arial", 14)
        );

        Label flightLabel = new Label(
            "Flight ID: " + flightId
        );

        flightLabel.setFont(
            Font.font("Arial", FontWeight.BOLD, 24)
        );

        flightLabel.setTextFill(Color.web("#D63384"));

        Label heading = new Label("SELECT YOUR SEAT");

        heading.setFont(
            Font.font("Arial", FontWeight.BOLD, 30)
        );

        heading.setTextFill(Color.web("#D63384"));

        HBox legend = new HBox(
            25,
            createLegend("Available", PINK),
            createLegend("Booked", GREEN),
            createLegend("Selected", DARK_PINK)
        );

        legend.setAlignment(Pos.CENTER);

        seatGrid = new GridPane();

        seatGrid.setAlignment(Pos.CENTER);
        seatGrid.setHgap(12);
        seatGrid.setVgap(12);
        seatGrid.setPadding(new Insets(15));

        Button refreshButton = new Button("REFRESH");

        refreshButton.setPrefWidth(130);
        refreshButton.setPrefHeight(42);

        refreshButton.setStyle(
            "-fx-background-color: #D63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;"
        );

        refreshButton.setOnAction(e -> {
            selectedSeat = null;
            selectedLabel.setText("Selected Seat: None");
            loadSeats();
        });

        Button continueButton = new Button("CONTINUE");

        continueButton.setPrefWidth(180);
        continueButton.setPrefHeight(45);

        continueButton.setStyle(
            "-fx-background-color: #D63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;"
        );

        continueButton.setOnAction(e -> {

            if (selectedSeat == null) {

                messageLabel.setText(
                    "Please select an available seat!"
                );

                messageLabel.setTextFill(Color.RED);
                return;
            }

            if (userId <= 0) {
                messageLabel.setText("Please login again.");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            try {

                BookingConfirmation confirmation =
                    new BookingConfirmation(
                        flightId,
                        selectedSeat
                    );

                confirmation.start(new Stage());

                primaryStage.close();

            } catch (Exception ex) {

                ex.printStackTrace();

                messageLabel.setText(
                    "Unable to open Booking Confirmation: "
                    + ex.getMessage()
                );

                messageLabel.setTextFill(Color.RED);
            }
        });

        HBox buttonBox = new HBox(
            20,
            refreshButton,
            continueButton
        );

        buttonBox.setAlignment(Pos.CENTER);

        VBox mainBox = new VBox(
            20,
            heading,
            flightLabel,
            legend,
            seatGrid,
            selectedLabel,
            messageLabel,
            buttonBox
        );

        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(25));

        ScrollPane scrollPane = new ScrollPane(mainBox);

        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background: #FCE4EC;" +
            "-fx-background-color: #FCE4EC;"
        );

        StackPane root = new StackPane(scrollPane);

        root.setStyle(
            "-fx-background-color: #FCE4EC;"
        );

        Scene scene = new Scene(root, 1000, 750);

        stage.setTitle("Flight Booking - Seat Selection");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.show();

        loadSeats();
    }

    private HBox createLegend(String name, String color) {

        Label square = new Label("   ");

        square.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 5;"
        );

        Label text = new Label(name);

        text.setFont(
            Font.font("Arial", 16)
        );

        HBox box = new HBox(8, square, text);

        box.setAlignment(Pos.CENTER);

        return box;
    }

    private void loadSeats() {

        seatStatus.clear();
        seatButtons.clear();

        selectedSeat = null;

        selectedLabel.setText("Selected Seat: None");

        seatGrid.getChildren().clear();

        String sql =
            "SELECT s.SEAT_NUMBER, " +
            "CASE " +
            "WHEN UPPER(TRIM(s.SEAT_STATUS)) IN ('BOOKED','RESERVED') " +
            "OR EXISTS (" +
            "SELECT 1 FROM BOOKINGS b " +
            "WHERE b.SEAT_ID = s.SEAT_ID " +
            "AND b.FLIGHT_ID = s.FLIGHT_ID " +
            "AND NVL(UPPER(TRIM(b.BOOKING_STATUS)), 'CONFIRMED') <> 'CANCELLED' " +
            ") THEN 'BOOKED' " +
            "ELSE 'AVAILABLE' " +
            "END AS CURRENT_STATUS " +
            "FROM SEATS s " +
            "WHERE s.FLIGHT_ID = ? " +
            "ORDER BY s.SEAT_NUMBER";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, flightId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String seatNumber =
                        rs.getString("SEAT_NUMBER");

                    String status =
                        rs.getString("CURRENT_STATUS");

                    seatStatus.put(seatNumber, status);
                }
            }

            createSeatButtons();

            messageLabel.setText(
                "Select your available seat"
            );

            messageLabel.setTextFill(
                Color.web("#D63384")
            );

        } catch (Exception ex) {

            ex.printStackTrace();

            messageLabel.setText(
                "Unable to load seats: " + ex.getMessage()
            );

            messageLabel.setTextFill(Color.RED);
        }
    }

    private void createSeatButtons() {

        seatButtons.clear();

        for (int col = 0; col < 6; col++) {

            Label column = new Label(
                String.valueOf((char) ('A' + col))
            );

            column.setMinWidth(55);
            column.setAlignment(Pos.CENTER);

            column.setFont(
                Font.font("Arial", FontWeight.BOLD, 18)
            );

            seatGrid.add(column, col, 0);
        }

        for (int row = 1; row <= 20; row++) {

            for (int col = 0; col < 6; col++) {

                char letter = (char) ('A' + col);

                String seatNumber =
                    row + String.valueOf(letter);

                String status = seatStatus.getOrDefault(
                    seatNumber,
                    "AVAILABLE"
                );

                Button seatButton = new Button(seatNumber);

                seatButton.setMinSize(65, 55);
                seatButton.setMaxSize(65, 55);

                seatButton.setFont(
                    Font.font("Arial", FontWeight.BOLD, 15)
                );

                seatButtons.put(seatNumber, seatButton);

                if ("BOOKED".equals(status)) {

                    seatButton.setStyle(
                        "-fx-background-color: " + GREEN + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 12;"
                    );

                    seatButton.setDisable(true);

                } else {

                    seatButton.setStyle(
                        "-fx-background-color: " + PINK + ";" +
                        "-fx-text-fill: #333333;" +
                        "-fx-background-radius: 12;"
                    );

                    seatButton.setOnAction(e ->
                        selectSeat(seatNumber)
                    );
                }

                seatGrid.add(
                    seatButton,
                    col,
                    row
                );
            }
        }
    }

    private void selectSeat(String seatNumber) {

        String status = seatStatus.getOrDefault(
            seatNumber,
            "AVAILABLE"
        );

        if ("BOOKED".equals(status)) {

            messageLabel.setText(
                "This seat is already booked!"
            );

            return;
        }

        if (selectedSeat != null) {

            Button previous =
                seatButtons.get(selectedSeat);

            if (previous != null) {

                previous.setStyle(
                    "-fx-background-color: " + PINK + ";" +
                    "-fx-text-fill: #333333;" +
                    "-fx-background-radius: 12;"
                );
            }
        }

        selectedSeat = seatNumber;

        Button current = seatButtons.get(seatNumber);

        if (current != null) {

            current.setStyle(
                "-fx-background-color: " + DARK_PINK + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;"
            );
        }

        selectedLabel.setText(
            "Selected Seat: " + selectedSeat
        );

        messageLabel.setText(
            "Seat selected successfully!"
        );

        messageLabel.setTextFill(
            Color.GREEN
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
