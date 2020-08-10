package com.bioforceanalytics.dashboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Entry point for BioForce's software suite.
 * This should be the starting class for building JARs/executables.
 */
public class DashboardSelector extends Application {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void start(Stage primaryStage) throws Exception {

        logger.info("Launching Dashboard Selector...");

        Parent root = FXMLLoader.load(getClass().getResource("fxml/DashboardSelector.fxml"));
        primaryStage.setTitle("Dashboard Selector");
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResource("images/bfa.png").toExternalForm()));
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }

}

