package dataorganizer;

import static java.lang.Thread.getAllStackTraces;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

public class GraphNoSINCController implements Initializable {

	
	private static GenericTest genericTestOne; //object for holding the generic test for module 1
	private static GenericTest genericTestTwo; //object for holding the generic test for module 2
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public void assignGenericTestOne(GenericTest genericTestOne) { //assigns generic test object to module 1
		this.genericTestOne = genericTestOne;
	}
	
	public void assignGenericTestTwo(GenericTest genericTestTwo) { //assigns generic test object to module 2
		this.genericTestTwo = genericTestTwo;
	}
	
	public void createSeries(ArrayList<Double> timeData, ArrayList<Double> samplesData) {
		
		ArrayList<Double> cleanTimeData = new ArrayList<Double>();										// setup new array list for time data
		ArrayList<Double> cleanSamplesData = new ArrayList<Double>();									// setup new array list for samples data
		
		int resolution = 1920;																			//the interval between data points to take for graph  -- GREATLY SPEEDS UP DISPLAY TIME, NO DATA IS LOST
		
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
		System.out.println(series.getName());
	}
	@FXML
	private LineChart<Number,Number> lineChart;
	
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
	@FXML
	public void subOneNullButtonHandler(ActionEvent event) {
		
	}
	@FXML
	public void addOneNullButtonHandler(ActionEvent event) {
		
	}
	@FXML
	public void subTenNullButtonHandler(ActionEvent event) {
		
	}
	@FXML
	public void addTenNullButtonHandler(ActionEvent event) {
		
	}
	@FXML
	public void magnitudeDatasetOneCheckBoxHandler(ActionEvent event) {
		
	}
	@FXML
	public void subTenNullButtonHandlerTwo(ActionEvent event) {
		
	}
	@FXML
	public void addTenNullButtonHandlerTwo(ActionEvent event) {
		
	}
	@FXML
	public void subOneNullButtonHandlerTwo(ActionEvent event) {
		
	}
	@FXML
	public void addOneNullButtonHandlerTwo(ActionEvent event) {
		
	}
	@FXML
	public void magnitudeDatasetTwoCheckBoxHandler(ActionEvent event) {
		
	}
	@FXML
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




