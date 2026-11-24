
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewAllUsers extends Application {

    private TableView<ObservableList<String>> table;

    @Override
    public void start(Stage stage) {

        Label title = new Label("👥 VIEW ALL USERS");
        title.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #d63384;"
        );

        Button back = new Button("← Back");
        back.setStyle(
            "-fx-background-color: #d63384;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;"
        );

        back.setOnAction(e -> {
            stage.close();
            new Dashboard().start(new Stage());
        });

        table = new TableView<>();
        table.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY
        );

        String[] columns = {
            "User ID", "Name", "Email", "Phone", "Role"
        };

        for (int i = 0; i < columns.length; i++) {
            final int index = i;

            TableColumn<ObservableList<String>, String> column =
                new TableColumn<>(columns[i]);

            column.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                    data.getValue().get(index)
                )
            );

            table.getColumns().add(column);
        }

        loadUsers();

        VBox root = new VBox(20, back, title, table);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #ffd6e7;");

        VBox.setVgrow(table, Priority.ALWAYS);

        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("View All Users");
        stage.setMaximized(true);
        stage.show();
    }

    private void loadUsers() {

        ObservableList<ObservableList<String>> data =
            FXCollections.observableArrayList();

        String sql =
            "SELECT USER_ID, NAME, EMAIL, PHONE, ROLE " +
            "FROM USERS ORDER BY USER_ID";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                ObservableList<String> row =
                    FXCollections.observableArrayList();

                row.add(String.valueOf(rs.getInt("USER_ID")));
                row.add(rs.getString("NAME"));
                row.add(rs.getString("EMAIL"));
                row.add(rs.getString("PHONE"));
                row.add(rs.getString("ROLE"));

                data.add(row);
            }

            table.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Unable to load users");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
