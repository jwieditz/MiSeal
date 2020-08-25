package de.jwieditz.miseal.display;

import java.io.IOException;

import de.jwieditz.miseal.display.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainGUI extends Application {

    public static Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        MainGUI.stage = stage;

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon.png")));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/maingui.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage.setTitle("MiSeal");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(true);
        stage.setMinWidth(1280);
        stage.setMinHeight(960);

        // maximize on start
        stage.setMaximized(true);

        loader.<MainController> getController().createFirstTab();
    }

}
