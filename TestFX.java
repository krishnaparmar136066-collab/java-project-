import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TestFX extends Application {

    @Override
    public void start(Stage stage) {

        Label label = new Label("🩷 Flight Booking App");

        Scene scene = new Scene(label, 400, 250);

        stage.setTitle("Flight Booking System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}