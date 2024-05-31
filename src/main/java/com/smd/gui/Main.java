package com.smd.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 640, 400);
        primaryStage.setTitle("SMD Application");
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true);

        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            MainController.closeDb();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}