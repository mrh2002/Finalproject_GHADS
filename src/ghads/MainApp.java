package ghads;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        Scene scene = new Scene(root, 900, 650);
        scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());

        stage.setTitle("GHADS - Gaza Humanitarian Aid Distribution System");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void changeScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource(fxmlPath));
            primaryStage.setTitle(title);
            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
