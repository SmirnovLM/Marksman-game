package com.example.lab1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stageGame) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("authorization.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 250);
        stageGame.setTitle("Authorization!");
        stageGame.setScene(scene);
        stageGame.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
