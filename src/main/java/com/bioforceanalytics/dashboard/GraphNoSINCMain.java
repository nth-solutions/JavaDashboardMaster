package com.bioforceanalytics.dashboard;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Testing class used to launch the BioForce Graph.
 */
public class GraphNoSINCMain extends Application {

    // BFA icon used for the Dashboard and Graph
    private Image icon;

    private static final Logger logger = LogController.start();

    @Override
    public void start(Stage primaryStage) throws Exception {

        icon = new Image(getClass().getResource("images/bfa.png").toExternalForm());

    	FXMLLoader loader = new FXMLLoader((getClass().getResource("fxml/GraphNoSINC.fxml")));
        Parent root;
        
		try {
			
			root = loader.load();
			primaryStage.setTitle("BioForce Graph");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("css/GraphNoSINC.css").toExternalForm());
	        primaryStage.setMinWidth(900);
	        primaryStage.setMinHeight(600);
	        primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.getIcons().add(icon);
	        primaryStage.show();
	        
		} catch (IOException e) {
			logger.error("Error loading BioForce Graph.");
			e.printStackTrace();
		}

        GraphNoSINCController graph = loader.getController();

        File directory = new File(Settings.get("CSVSaveLocation"));

        // fetches all CSV files from given folder
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv");
            }
        });

        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;

        // if no CSV files could be found, don't continue
        if (files == null || files.length == 0) {
            logger.info("No CSV files found");
            return;
        }

        // Finds the most recently modified file
        for (File f : files) {

            String filePath = f.toString();
            File csvp = new File(filePath.substring(0, filePath.length()-4) + ".csvp");

            // if CSVP file with same name 
            if (csvp.exists() && f.lastModified() > lastModifiedTime) {
                chosenFile = f;
                lastModifiedTime = f.lastModified();
            }
        }

        // if no CSV/CSVP file pair could be found, don't continue
        if (chosenFile == null) {
            logger.info("No CSV/CSVP file pair found");
            return;
        }

        String pathToFile = chosenFile.toString();
        
        long start = System.nanoTime(); 
        graph.setGenericTestFromCSV(pathToFile);
        long elapsedTime = System.nanoTime() - start;
        
        logger.info("Loaded CSV in " + elapsedTime/1e9d + " seconds");

    }

    public static void main(String[] args) {
        logger.info("Version: " + Settings.getVersion());
    	logger.info("Build date: " + Settings.getBuildDate());
        launch(args);
    }
}
