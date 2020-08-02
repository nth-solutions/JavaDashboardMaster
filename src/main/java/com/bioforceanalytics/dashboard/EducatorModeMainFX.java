package com.bioforceanalytics.dashboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class EducatorModeMainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml/EducatorModeFXML.fxml"));
        primaryStage.setTitle("Education Mode");
        Scene scene = new Scene(root, 690, 500);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("css/EducatorModeCSS.css").toExternalForm());
        primaryStage.getIcons().add(new Image(getClass().getResource("images/bfa.png").toExternalForm()));
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
