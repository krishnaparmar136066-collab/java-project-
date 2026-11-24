
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PassengerDetails extends Application {

    private int flightId;
    private int userId;

    public PassengerDetails() {
    }

    public PassengerDetails(int flightId, int userId) {
        this.flightId = flightId;
        this.userId = userId;
    }

    @Override
    public void start(Stage stage) {

        // BACK ARROW
        Button backButton = new Button("←");
        backButton.setStyle(
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12px;" +
            "-fx-cursor: hand;"
        );

        backButton.setOnAction(e -> {
            try {
                FlightSearch flightSearch = new FlightSearch();
                flightSearch.start(new Stage());
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Label title = new Label("Passenger Details");
        title.setStyle(
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        Label nameLabel = new Label("Passenger Name");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter passenger name");
        nameField.setMaxWidth(500);
        nameField.setPrefHeight(40);

        // Name field intentionally empty

        Label ageLabel = new Label("Age");

        TextField ageField = new TextField();
        ageField.setPromptText("Enter age");
        ageField.setMaxWidth(500);
        ageField.setPrefHeight(40);

        Label genderLabel = new Label("Gender");

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other");
        genderBox.setPromptText("Select Gender");
        genderBox.setMaxWidth(500);
        genderBox.setPrefHeight(40);

        Label passportLabel = new Label("Passport Number");

        TextField passportField = new TextField();
        passportField.setPromptText("Enter passport number");
        passportField.setMaxWidth(500);
        passportField.setPrefHeight(40);

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setTextFill(Color.RED);

        Button continueButton = new Button("CONTINUE");
        continueButton.setPrefWidth(500);
        continueButton.setPrefHeight(65);

        continueButton.setStyle(
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15px;" +
            "-fx-cursor: hand;"
        );

        continueButton.setOnAction(e -> {

            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            String gender = genderBox.getValue();
            String passport = passportField.getText().trim();

            messageLabel.setText("");

            if (flightId <= 0) {
                messageLabel.setText("Invalid Flight ID.");
                return;
            }

            if (userId <= 0) {
                messageLabel.setText("Please login again.");
                return;
            }

            if (name.isEmpty()) {
                messageLabel.setText("Please enter passenger name.");
                return;
            }

            if (!name.matches("[\\p{L} .'-]+")) {
                messageLabel.setText(
                    "Please enter a valid passenger name."
                );
                return;
            }

            if (ageText.isEmpty()) {
                messageLabel.setText("Please enter age.");
                return;
            }

            int age;

            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException ex) {
                messageLabel.setText("Age must be a number.");
                return;
            }

            if (age < 1 || age > 120) {
                messageLabel.setText("Please enter a valid age.");
                return;
            }

            if (gender == null) {
                messageLabel.setText("Please select gender.");
                return;
            }

            if (passport.isEmpty() || passport.length() < 5) {
                messageLabel.setText(
                    "Please enter a valid passport number."
                );
                return;
            }

            BookingConfirmation.passengerName = name;
            BookingConfirmation.passengerAge = age;
            BookingConfirmation.passengerGender = gender;
            BookingConfirmation.passengerPassport = passport;

            try {
                SeatSelection seatSelection =
                    new SeatSelection(flightId, userId);

                seatSelection.start(new Stage());
                stage.close();

            } catch (Exception ex) {
                ex.printStackTrace();

                messageLabel.setText(
                    "Unable to open Seat Selection: " +
                    ex.getMessage()
                );
                messageLabel.setTextFill(Color.RED);
            }
        });

        VBox card = new VBox(
            18,
            title,
            nameLabel,
            nameField,
            ageLabel,
            ageField,
            genderLabel,
            genderBox,
            passportLabel,
            passportField,
            continueButton,
            messageLabel
        );

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(45));
        card.setMaxWidth(750);

        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 25px;"
        );

        BorderPane root = new BorderPane();
        root.setTop(backButton);
        root.setCenter(card);

        BorderPane.setAlignment(backButton, Pos.TOP_LEFT);
        BorderPane.setMargin(backButton, new Insets(20));

        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #ffd6e7;");

        Scene scene = new Scene(root);

        stage.setTitle("Flight Booking - Passenger Details");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
