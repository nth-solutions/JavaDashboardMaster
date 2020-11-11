package com.bioforceanalytics.dashboard;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.io.IoBuilder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class EducatorModeMainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // redirect stdout and stderr to Log4J: this adds more detailed info,
        // and most importantly, saves all console output to a .log file
        System.setErr(IoBuilder.forLogger(LogManager.getRootLogger()).setLevel(Level.ERROR).buildPrintStream());
        System.setOut(IoBuilder.forLogger(LogManager.getRootLogger()).setLevel(Level.INFO).buildPrintStream());

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
