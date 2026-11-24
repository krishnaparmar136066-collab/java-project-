
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

public class LoginScreen extends Application {

    public static int loggedInUserId;
    public static String loggedInUserName;
    public static String loggedInUserRole;

    @Override
    public void start(Stage stage) {

        Label heading = new Label("✈ FLIGHT BOOKING SYSTEM");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        heading.setTextFill(Color.web("#D63384"));

        Label subHeading = new Label("Welcome Back!");
        subHeading.setFont(Font.font("Arial", 20));
        subHeading.setTextFill(Color.web("#555555"));

        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email ID");
        emailField.setPrefHeight(45);
        emailField.setMaxWidth(320);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setPrefHeight(45);
        passwordField.setMaxWidth(320);

        Label message = new Label();
        message.setWrapText(true);
        message.setMaxWidth(320);

        Button loginButton = new Button("LOGIN");
        loginButton.setPrefWidth(320);
        loginButton.setPrefHeight(45);

        loginButton.setStyle(
            "-fx-background-color: #D63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );

        loginButton.setOnAction(e -> {

            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                message.setText("Please enter Email and Password!");
                message.setTextFill(Color.RED);
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                message.setText("Please enter a valid Email ID!");
                message.setTextFill(Color.RED);
                return;
            }

            String sql =
                "SELECT USER_ID, NAME, ROLE FROM USERS " +
                "WHERE LOWER(TRIM(EMAIL)) = LOWER(TRIM(?)) " +
                "AND TRIM(PASSWORD) = TRIM(?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, email);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        loggedInUserId = rs.getInt("USER_ID");
                        loggedInUserName = rs.getString("NAME");
                        loggedInUserRole = rs.getString("ROLE");

                        Alert alert = new Alert(
                            Alert.AlertType.INFORMATION
                        );

                        alert.setTitle("Login Successful");
                        alert.setHeaderText(
                            "Welcome " + loggedInUserName
                        );
                        alert.setContentText("Login Successful!");
                        alert.showAndWait();

                        try {
                            Dashboard dashboard = new Dashboard();
                            dashboard.start(new Stage());
                            stage.close();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showError(
                                "Dashboard Error: " + ex.getMessage()
                            );
                        }

                    } else {
                        message.setText(
                            "Invalid Email ID or Password!"
                        );
                        message.setTextFill(Color.RED);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Database Error: " + ex.getMessage());
            }
        });

        VBox box = new VBox(
            18,
            heading,
            subHeading,
            emailField,
            passwordField,
            loginButton,
            message
        );

        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(45));
        box.setMaxWidth(450);

        box.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 22;" +
            "-fx-border-color: #F3A6C8;" +
            "-fx-border-radius: 22;" +
            "-fx-border-width: 2;"
        );

        StackPane root = new StackPane(box);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #FCE4EC;");

        Scene scene = new Scene(root);

        stage.setTitle("Flight Booking System - Login");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Something went wrong");
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
