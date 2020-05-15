package dataorganizer;

import static java.lang.Thread.getAllStackTraces;

import java.io.File;
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
	
	
	private double initialMouseX;
	private double initialMouseY;
	private double lastMouseX;
	private double lastMouseY;
	private double mouseDeltaX;
	private double mouseDeltaY;
	
	
	private boolean mouseIsHeld;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		zoomLevel = 1.0;
		resolution = 20;
		
		zoomviewX = 50;
		zoomviewY = 0;
		
				
		yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis){ 
			@Override public String toString(Number object){
				return String.format("%2.2f", object); 
				} 
			});
		lineChart.setOnScroll(new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent event) {
				// TODO Auto-generated method stub
				zoomLevel += event.getDeltaY() / 250;
				zoomLevel = Double.min(Double.max(zoomLevel, 1),10);
				redrawGraph();
			}
			
		});
		
		
		
		lineChart.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				mouseIsHeld = true;
				initialMouseX = event.getX();
				lastMouseX = event.getX();
				initialMouseY = event.getY();
				lastMouseY = event.getY();
				
				initialZoomviewX = zoomviewX;
				initialZoomviewY = zoomviewY;
				
				//xAxis.setTickLabelsVisible(false);
				//yAxis.setTickLabelsVisible(false);
			}
			
		});
		
		lineChart.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				mouseIsHeld = false;
				//xAxis.setTickLabelsVisible(true);
				//yAxis.setTickLabelsVisible(true);
			}
			
			
		});
		lineChart.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
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
					//System.out.println(lastMouseX +" , " + lastMouseY);
				//}
			}
		});
		
		
		Timer dragTimer = new Timer();
		TimerTask dragTask = new TimerTask() {

			@Override
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
		 
		
		
		lineChart.widthProperty().addListener((obs) -> {
		    //redrawGraph();
		});
		lineChart.heightProperty().addListener((obs) -> {
		    //redrawGraph();
		});
		
	}
	
	public void createTest(String CSVFilePath) {
		
		DataOrganizer d = new DataOrganizer();
		
        d.createDataSamplesFromCSV(CSVFilePath);
        d.setSourceID(new File(CSVFilePath).getName(), 1);
        
        genericTestOne = new GenericTest(d);
        
        createSeries(genericTestOne.getAxis(AxisType.AccelX).getTime(), genericTestOne.getAxis(AxisType.AccelX).getSamples());
		
	}
	
	public void assignGenericTestOne(GenericTest genericTestOne) { //assigns generic test object to module 1
		this.genericTestOne = genericTestOne;
	}
	
	public void assignGenericTestTwo(GenericTest genericTestTwo) { //assigns generic test object to module 2
		this.genericTestTwo = genericTestTwo;
	}
	

	public void redrawGraph() {
		
		resolution = 160 / (int) zoomLevel;
		
		ArrayList<Double> cleanTimeData = new ArrayList<Double>();										// setup new array list for time data
		ArrayList<Double> cleanSamplesData = new ArrayList<Double>();	
		
		if(!originalSamples.isEmpty() && !originalTime.isEmpty()) {
			zoomviewW = originalSamples.size() / zoomLevel;
			zoomviewH = originalTime.size() / zoomLevel;
		}
		
		for(int i = 0; i < originalTime.size(); i+= resolution) {											//takes every "resolution" sample from the input data and adds it to the clean array lists for displaying
			cleanTimeData.add(originalTime.get(i));
			cleanSamplesData.add(originalSamples.get(i));
		}
		
		xAxis.setLowerBound(zoomviewX - zoomviewW/1920);
		xAxis.setUpperBound(zoomviewX + zoomviewW/1920);
		yAxis.setLowerBound((-zoomviewH/9600) + zoomviewY);
		yAxis.setUpperBound((zoomviewH/9600) + zoomviewY);
		
		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();					//setups a new series
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
		
		for (int i = 0; i < cleanTimeData.size(); i++) {		
            seriesData.add(new XYChart.Data<>(cleanTimeData.get(i), cleanSamplesData.get(i)));
        }
		
		lineChart.getData().clear();
		lineChart.getData().add(series);
		series.setData(seriesData);
		
	}
	
	public void createSeries(ArrayList<Double> timeData, ArrayList<Double> samplesData) {
		
		originalSamples = new ArrayList<Double>();
		originalTime = new ArrayList<Double>();
		
		for(int i = 0; i < timeData.size(); i++) {
			originalSamples.add(samplesData.get(i));
			originalTime.add(timeData.get(i));
		}
	
		redrawGraph();
		
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




