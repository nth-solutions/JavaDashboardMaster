package com.bioforceanalytics.dashboard;

import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class EducatorModeMainFX extends Application {

    private static final Logger logger = LogController.start();

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("fxml/EducatorModeFXML.fxml"));
        primaryStage.setTitle("BioForce Education Mode");
        Scene scene = new Scene(root, 690, 500);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("css/EducatorModeCSS.css").toExternalForm());
        primaryStage.getIcons().add(new Image(getClass().getResource("images/bfa.png").toExternalForm()));
        primaryStage.show();
        primaryStage.setResizable(false);

        logger.info("Version: " + Settings.getVersion());
        logger.info("Build date: " + Settings.getBuildDate());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
