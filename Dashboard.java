
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Dashboard extends Application {

    private final int userId = LoginScreen.loggedInUserId;
    private final String userName = LoginScreen.loggedInUserName;
    private final String userRole = LoginScreen.loggedInUserRole;

    @Override
    public void start(Stage stage) {

        Label title = new Label("✈ Flight Booking System");
        title.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        Label welcome = new Label(
            "Welcome " + (userName == null ? "User" : userName)
        );
        welcome.setStyle("-fx-font-size: 20px;");

        String style =
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 10px;";

        Button logout = new Button("🚪 Logout");
        logout.setPrefSize(250, 45);
        logout.setStyle(style);

        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        box.getChildren().addAll(title, welcome);

        if (userRole != null &&
            userRole.trim().equalsIgnoreCase("ADMIN")) {

            Label adminLabel = new Label("ADMIN DASHBOARD");
            adminLabel.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #d63384;"
            );

            Button manageFlights = new Button("✈ Manage Flights");
            Button allBookings = new Button("🎫 View All Bookings");
            Button allUsers = new Button("👥 View All Users");

            for (Button b : new Button[]{
                manageFlights, allBookings, allUsers
            }) {
                b.setPrefSize(250, 45);
                b.setStyle(style);
            }

            // MANAGE FLIGHTS
            manageFlights.setOnAction(e -> {
                try {
                    new ManageFlights().start(new Stage());
                    stage.hide();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError(ex.getMessage());
                }
            });

            // VIEW ALL BOOKINGS
            allBookings.setOnAction(e -> {
                try {
                    new ViewAllBookings().start(new Stage());
                    stage.hide();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError(ex.getMessage());
                }
            });

            // VIEW ALL USERS
            allUsers.setOnAction(e -> {
                try {
                    new ViewAllUsers().start(new Stage());
                    stage.hide();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError(ex.getMessage());
                }
            });

            box.getChildren().addAll(
                adminLabel,
                manageFlights,
                allBookings,
                allUsers
            );

        } else {

            Button searchFlight = new Button("🔎 Search Flights");
            Button myBookings = new Button("🎫 My Bookings");

            for (Button b : new Button[]{
                searchFlight, myBookings
            }) {
                b.setPrefSize(250, 45);
                b.setStyle(style);
            }

            searchFlight.setOnAction(e -> {
                try {
                    new FlightSearch().start(new Stage());
                    stage.hide();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError(ex.getMessage());
                }
            });

            myBookings.setOnAction(e -> {
                try {
                    new MyBookings().start(new Stage());
                    stage.hide();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError(ex.getMessage());
                }
            });

            box.getChildren().addAll(
                searchFlight,
                myBookings
            );
        }

        // LOGOUT
        logout.setOnAction(e -> {
            LoginScreen.loggedInUserId = 0;
            LoginScreen.loggedInUserName = null;
            LoginScreen.loggedInUserRole = null;

            stage.close();
            new LoginScreen().start(new Stage());
        });

        box.getChildren().add(logout);

        VBox root = new VBox(box);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffd6e7;");

        stage.setScene(new Scene(root, 700, 550));
        stage.setTitle("Flight Booking Dashboard");
        stage.setMaximized(true);
        stage.show();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
