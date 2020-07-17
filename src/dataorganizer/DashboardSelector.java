package dataorganizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for BioForce's software suite.
 * This should be the starting class for building JARs/executables.
 */
public class DashboardSelector extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("DashboardSelector.fxml"));
        primaryStage.setTitle("Dashboard Selector");
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        //scene.getStylesheets().add(getClass().getResource("EducatorModeCSS.css").toExternalForm());
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    public static void main(String[] args){
        OSManager osManager = new OSManager();
        System.out.println(osManager.getOSName());
        System.out.println(osManager.getOSType());
        launch(args);

    }

}

