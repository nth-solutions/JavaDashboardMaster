package dataorganizer;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GraphNoSINCMain extends Application {

    //REV 36
    @Override
    public void start(Stage primaryStage) throws Exception{
    	System.out.println("test");
        Parent root = FXMLLoader.load(getClass().getResource("GraphNoSINC.fxml"));
       
        
        primaryStage.setTitle("BioForce Experiment Graph");
        Scene scene = new Scene(root, 690, 500);
        primaryStage.setScene(scene);
        //scene.getStylesheets().add(getClass().getResource("EducatorModeCSS.css").toExternalForm());
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
    	 
        launch(args);
    }
}
