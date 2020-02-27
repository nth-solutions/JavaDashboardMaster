package dataorganizer;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GraphNoSINCMain extends Application {

    //REV 36
	private static GraphNoSINCController controller;
	private static ArrayList<Double> testDataSamples;
	private static ArrayList<Double> testDataTime;
    @Override
    public void start(Stage primaryStage) throws Exception{
    	System.out.println("test");
    	FXMLLoader loader = new FXMLLoader((getClass().getResource("GraphNoSINC.fxml")));
        Parent root = loader.load();
        controller = loader.getController();
        
        primaryStage.setTitle("BioForce Experiment Graph");
        Scene scene = new Scene(root, 690, 500);
        primaryStage.setScene(scene);
        //scene.getStylesheets().add(getClass().getResource("EducatorModeCSS.css").toExternalForm());
        primaryStage.show();
        
        primaryStage.setResizable(false);

        
        controller.createSeries(testDataTime, testDataSamples);
    }

    public static void main(String[] args) {
    	//ArrayList<Double> testDataSamples = new ArrayList<Double>();
    	//ArrayList<Double>[] testDataTime = new double[9600];
    	int size = 96000;
    	testDataSamples = new ArrayList<Double>();
    	testDataTime = new ArrayList<Double>();
    	System.out.println(testDataSamples.size());
    	for(int i = 0; i < size; i++) {
    		testDataSamples.add( 5 * Math.sin(i / 1000.0) * (((i-10000)/1000.0) / (1 + (((i-10000)/1000.0)*((i-10000)/1000.0)))));
    		testDataTime.add(i / 960.0);
    	}
    	launch(args);
    	
    	
    	
       
    }
}
