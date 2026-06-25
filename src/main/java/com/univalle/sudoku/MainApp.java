package com.univalle.sudoku;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Entry point for the Sudoku 6x6 JavaFX application.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/univalle/sudoku/sudoku-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 720);
        scene.getStylesheets().add(MainApp.class.getResource("/com/univalle/sudoku/styles/app.css").toExternalForm());

        stage.setTitle("Sudoku 6x6");
        stage.setScene(scene);
        stage.setMinWidth(640);
        stage.setMinHeight(680);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
