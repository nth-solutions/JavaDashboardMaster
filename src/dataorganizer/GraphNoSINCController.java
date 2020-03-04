package dataorganizer;

import static java.lang.Thread.getAllStackTraces;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class GraphNoSINCController implements Initializable {

	
	private static GenericTest genericTestOne; //object for holding the generic test for module 1
	private static GenericTest genericTestTwo; //object for holding the generic test for module 2
	
	private static double zoomLevel;
	
	private static double graphWidth;
	private static double graphHeight;
	
	private static ArrayList<Double> originalSamples;
	private static ArrayList<Double> originalTime;
	
	private static int resolution;
	
	private static double zoomviewX;
	private static double zoomviewY;
	private static double zoomviewW;
	private static double zoomviewH;
	
	private static double lastMouseX;
	private static double lastMouseY;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		zoomLevel = 1.0;
		resolution = 160;
		
		zoomviewX = 0;
		zoomviewY = 0;
		
				
		lineChart.setOnScroll(new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent event) {
				// TODO Auto-generated method stub
				zoomLevel += event.getDeltaY() / 250;
				zoomLevel = Double.min(Double.max(zoomLevel, 1),10);
				System.out.println(zoomLevel);
				redrawGraph();
			}
			
		});
		
		
		lineChart.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
				System.out.println("Clicked on graph");
			}
			
		});
		
		
		lineChart.widthProperty().addListener((obs) -> {
		    //redrawGraph();
		});
		lineChart.heightProperty().addListener((obs) -> {
		    //redrawGraph();
		});
		
	}
		
	
	public void assignGenericTestOne(GenericTest genericTestOne) { //assigns generic test object to module 1
		this.genericTestOne = genericTestOne;
	}
	
	public void assignGenericTestTwo(GenericTest genericTestTwo) { //assigns generic test object to module 2
		this.genericTestTwo = genericTestTwo;
	}
	

	public void redrawGraph() {
		
		ArrayList<Double> cleanTimeData = new ArrayList<Double>();										// setup new array list for time data
		ArrayList<Double> cleanSamplesData = new ArrayList<Double>();	
		System.out.println("redrawing Graph");
		if(!originalSamples.isEmpty() && !originalTime.isEmpty()) {
			zoomviewW = originalSamples.size() / zoomLevel;
			zoomviewH = originalTime.size() / zoomLevel;
		}
		
		
		for(int i = 0; i < originalTime.size(); i+= resolution) {											//takes every "resolution" sample from the input data and adds it to the clean array lists for displaying
			cleanTimeData.add(originalTime.get(i));
			cleanSamplesData.add(originalSamples.get(i));
		}
		
		zoomviewY = 5;
		xAxis.setLowerBound(zoomviewX);
		xAxis.setUpperBound(zoomviewX + zoomviewW/960);
		yAxis.setLowerBound((-zoomviewH/9600) + zoomviewY);
		yAxis.setUpperBound((zoomviewH/9600) + zoomviewY);
		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();					//setups a new series
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
		
		for (int i = 0; i < cleanTimeData.size(); i++) {
			
            seriesData.add(new XYChart.Data<>(cleanTimeData.get(i), cleanSamplesData.get(i)));
            
        }
		lineChart.getData().clear();
		lineChart.getData().add(series);
		System.out.println("changing data");
		series.setData(seriesData);
		
	}
	
	public void createSeries(ArrayList<Double> timeData, ArrayList<Double> samplesData) {
		
		originalSamples = new ArrayList<Double>();
		originalTime = new ArrayList<Double>();
		
		for(int i = 0; i < timeData.size(); i++) {
			originalSamples.add(samplesData.get(i));
			originalTime.add(timeData.get(i));
		}
	
		zoomviewW = originalSamples.size();
		zoomviewH = originalTime.size();
		
		ArrayList<Double> cleanTimeData = new ArrayList<Double>();										// setup new array list for time data
		ArrayList<Double> cleanSamplesData = new ArrayList<Double>();									// setup new array list for samples data
		
		graphHeight = lineChart.getHeight();
		graphWidth = lineChart.getWidth();
		
		System.out.println(graphHeight + " x " + graphWidth);
		
																					//the interval between data points to take for graph  -- GREATLY SPEEDS UP DISPLAY TIME, NO DATA IS LOST
		
		for(int i = 0; i < timeData.size(); i+= resolution) {											//takes every "resolution" sample from the input data and adds it to the clean array lists for displaying
			cleanTimeData.add(timeData.get(i));
			cleanSamplesData.add(samplesData.get(i));
		}
		
		
		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();					//setups a new series
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
		
		for (int i = 0; i < cleanTimeData.size(); i++) {
			
            seriesData.add(new XYChart.Data<>(cleanTimeData.get(i), cleanSamplesData.get(i)));
            
        }
		
		lineChart.getData().add(series);
		series.setData(seriesData);
		
	}
	@FXML
	private LineChart<Number,Number> lineChart;
	
	@FXML
	private NumberAxis xAxis;
	
	@FXML
	private NumberAxis yAxis;
	
	@FXML
	public void handleReset(ActionEvent event) {
		
	}
	@FXML
	public void handleZoom(ActionEvent event) {
		
	}
	@FXML
	public void handleSetYRange(ActionEvent event) {
		
	}
	@FXML
	public void rollingBlockHandler(ActionEvent event) {
		
	}
	public void importCSV(ActionEvent event) {
		
	}
	@FXML
	public void graphMomentum(ActionEvent event) {
		
	}
	@FXML
	public void clearDataAll(ActionEvent event) {
		
	}
	@FXML
	public void clearDataSetOne(ActionEvent event) {
		
	}
	@FXML
	public void clearDataSetTwo(ActionEvent event) {
		
	}
	
	


}




