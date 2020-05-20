package dataorganizer;

import static java.lang.Thread.getAllStackTraces;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class GraphNoSINCController implements Initializable {

	
	private GenericTest genericTestOne; //object for holding the generic test for module 1
	private GenericTest genericTestTwo; //object for holding the generic test for module 2
	
	private double zoomLevel;
	
	private double graphWidth;
	private double graphHeight;
	
	private ArrayList<Double> originalSamples;
	private ArrayList<Double> originalTime;
	
	private int resolution;
	
	private double zoomviewX;
	private double zoomviewY;
	private double zoomviewW;
	private double zoomviewH;
	private double initialZoomviewX;
	private double initialZoomviewY;
	private double resetZoomviewX;
	private double resetZoomviewY;
	
	private double initialMouseX;
	private double initialMouseY;
	private double lastMouseX;
	private double lastMouseY;
	private double mouseDeltaX;
	private double mouseDeltaY;
	
	private double scrollCenterX;
	private double scrollCenterY;
	
	private boolean mouseIsHeld;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		zoomLevel = 1.0;
		resolution = 20;
		
		resetZoomviewX = 50;
		resetZoomviewY = 0;
		
		zoomviewX = 50;
		zoomviewY = 0;
		
		lineChart.setOnScroll(new EventHandler<ScrollEvent>() {

			public void handle(ScrollEvent event) {	
				
				scrollCenterX = event.getX();
				scrollCenterY = event.getY();
				zoomLevel += event.getDeltaY() / 250;
				zoomLevel = Double.min(Double.max(zoomLevel, 1),10);
				redrawGraph();
				
			}
			
		});
		
		
		
		lineChart.setOnMousePressed(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				
				mouseIsHeld = true;
				
				initialMouseX = event.getX();
				lastMouseX = event.getX();
				initialMouseY = event.getY();
				lastMouseY = event.getY();
				
				initialZoomviewX = zoomviewX;
				initialZoomviewY = zoomviewY;
			}
			
		});
		
		lineChart.setOnMouseReleased(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent arg0) {
				
				mouseIsHeld = false;

			}
			
		});
		
		lineChart.setOnMouseExited(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent arg0) {
				
				mouseIsHeld = false;
				mouseDeltaX = 0;
				mouseDeltaY = 0;
			}
			
		});
		
		lineChart.setOnMouseDragged(new EventHandler<MouseEvent>() {
			
			public void handle(MouseEvent event) {
				
				lastMouseX = event.getX();
				lastMouseY = event.getY();
				
				redrawGraph();

			}
			
		});
		
		
		Timer dragTimer = new Timer();
		TimerTask dragTask = new TimerTask() {

			public void run() {
				
				if(mouseIsHeld) {
					
					mouseDeltaX = initialMouseX - lastMouseX;
					mouseDeltaY = initialMouseY - lastMouseY;
					
					zoomviewX = initialZoomviewX + mouseDeltaX/(10.0 * zoomLevel);
					zoomviewY = initialZoomviewY - mouseDeltaY/(15.0 * zoomLevel);
					
				}
				
			}
			
		};
		
		dragTimer.schedule(dragTask, 0,16);
		 
		/* This code is for checking if the size of the graph window has changed, could be useful later
		lineChart.widthProperty().addListener((obs) -> {
		    //redrawGraph();
		});
		lineChart.heightProperty().addListener((obs) -> {
		    //redrawGraph();
		});
		*/
	}
	
	public void createTest(DataOrganizer d1, DataOrganizer d2) {
		
		// Create GenericTest object if module exists -- otherwise, "null"
		// "null" on one of these differentiates b/t One/Two Module setup
		if (d1 != null) genericTestOne = new GenericTest(d1);
        if (d2 != null) genericTestTwo = new GenericTest(d2);
		
		// TEST CODE - TO BE REPLACED LATER
        createSeries(genericTestOne.getAxis(AxisType.AccelX).getTime(), genericTestOne.getAxis(AxisType.AccelX).getSamples());
		
	}

	public void redrawGraph() {
		
		// DEBUG CODE FOR TESTING -- NOT FOR PRODUCTION
		if (debugIgnoreResCheckbox.isSelected()) {
			resolution = 1;
		} else { 
			resolution = (int)(160 / zoomLevel);
		}

		ArrayList<Double> cleanTimeData = new ArrayList<Double>();										// setup new array list for time data
		ArrayList<Double> cleanSamplesData = new ArrayList<Double>();	
		
		if(!originalSamples.isEmpty() && !originalTime.isEmpty()) {
			
			zoomviewW = originalSamples.size() / zoomLevel;
			zoomviewH = originalTime.size() / zoomLevel;
			
		}
		
		for(int i = 0; i < originalSamples.size(); i+=resolution) {											//takes every "resolution" sample from the input data and adds it to the clean array lists for displaying
			
			cleanTimeData.add(originalTime.get(i));
			cleanSamplesData.add(originalSamples.get(i));
			
		}
		
		xAxis.setLowerBound(zoomviewX - zoomviewW/1920);
		xAxis.setUpperBound(zoomviewX + zoomviewW/1920);
		
		yAxis.setLowerBound((-zoomviewH/9600) + zoomviewY);
		yAxis.setUpperBound((zoomviewH/9600) + zoomviewY);
		
		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();					//sets up a new series
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
		
		for (int i = 0; i < cleanSamplesData.size(); i++) {		
            seriesData.add(new XYChart.Data<>(cleanTimeData.get(i), cleanSamplesData.get(i))); 
		}

		lineChart.getData().clear();
		lineChart.getData().add(series);
		
		series.setData(seriesData);
		
	}
	
	public void createSeries(ArrayList<Double> timeData, ArrayList<Double> samplesData) {
		
		originalSamples = new ArrayList<Double>();
		originalTime = new ArrayList<Double>();
		
		for(int i = 0; i < samplesData.size(); i++) {
			originalSamples.add(samplesData.get(i));
			originalTime.add(timeData.get(i));
		}
		
		/*
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
		*/

		redrawGraph();
		
	}
	
	@FXML
	private LineChart<Number,Number> lineChart;
	
	@FXML
	private NumberAxis xAxis;
	
	@FXML
	private NumberAxis yAxis;

	@FXML
	private TextField rollingBlockTextField;
	
	@FXML
	public void handleReset(ActionEvent event) {
		
		zoomviewX = resetZoomviewX;
		zoomviewY = resetZoomviewY;
		zoomLevel = 1.0;
		
		redrawGraph();
		
	}
	@FXML
	public void handleZoom(ActionEvent event) {
		
	}

	//====================================================
	// DEBUG CODE USED FOR TESTING -- NOT FOR PRODUCTION
	@FXML
	private CheckBox debugIgnoreResCheckbox;

	@FXML
	public void debugGraphAxis(ActionEvent event) {
		
		List<String> choices = new ArrayList<>();
		
		for (int i = 0; i < 28; i++) {

			choices.add(AxisType.valueOf(i).toString());

		}

		ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
		dialog.setTitle("[DEBUG] Graph Axis");
		dialog.setHeaderText("This is a testing feature to graph an AxisDataSeries.");
		dialog.setContentText("Choose axis:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			createSeries(genericTestOne.getAxis(AxisType.valueOf(result.get())).getTime(), genericTestOne.getAxis(AxisType.valueOf(result.get())).getSamples());
		}

	}

	//====================================================

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