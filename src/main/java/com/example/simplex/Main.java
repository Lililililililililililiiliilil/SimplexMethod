package com.example.simplex;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage mainStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("Main.fxml"));
        MenuBar mb = new MenuBar();
        Scene scene = new Scene(loader.load(), 800, 600);
        mb.prefWidthProperty().bind(scene.widthProperty());

        mainStage.setScene(scene);
        mainStage.setTitle("Симплекс метод");
        mainStage.show();
    }
}