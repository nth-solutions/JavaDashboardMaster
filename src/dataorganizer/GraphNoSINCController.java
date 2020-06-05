package dataorganizer;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class GraphNoSINCController implements Initializable {

	// TODO implement 2nd module functionality ("genericTestTwo" not used currently)

	// GenericTest represents a single module and associated test data
	private GenericTest genericTestOne;

	// "genericTestTwo" will NOT be assigned if running a single module test
	private GenericTest genericTestTwo;
	
	private int testLength;
	private double zoomLevel;
	
	private double graphWidth;
	private double graphHeight;
	
	
	private Map<AxisType, XYChart.Series<Number, Number>> dataSets;

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

	@FXML
	private LineChart<Number,Number> lineChart;
	
	@FXML
	private NumberAxis xAxis;
	
	@FXML
	private NumberAxis yAxis;

	@FXML
	private TextField rollingBlockTextField;


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		dataSets = new HashMap<AxisType, XYChart.Series<Number, Number>>();

		zoomLevel = 1.0;
		resolution = 20;
		
		resetZoomviewX = 0;
		resetZoomviewY = 0;
		
		zoomviewX = 0;
		zoomviewY = 0;
		
		lineChart.setOnScroll(new EventHandler<ScrollEvent>() {

			public void handle(ScrollEvent event) {	
				
				scrollCenterX = event.getX();
				scrollCenterY = event.getY();

				// Scaling the increase in zoom level by itself so zooming is more fluid feeling
				zoomLevel +=  zoomLevel * event.getDeltaY() / 250;

				// TODO restore "0.01" to a calculated value at some point; warnings for too many tickmarks are still printed
				zoomLevel = zoomLevel < 0 ? 0.01 : zoomLevel;

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

	/**
	 * <p>Populates the data analysis graph with GenericTests.</p>
	 * <p>g2 will be null for a One Module setup.</p>
	 * @param g1 GenericTest associated with module 1
	 * @param g2 GenericTest associated with module 2 (if applicable)
	 */
	public void setGenericTests(GenericTest g1, GenericTest g2) {

		// g1/g2 are allowed to be null here (differentiating One/Two Module setup)
		genericTestOne = g1;
		genericTestTwo = g2;

		// the AxisType is arbitrary (all non-magnetometer axes are same length)
		testLength = g1.getAxis(AxisType.AccelX).getOriginalData().length;
	
		// TEST CODE - TO BE REPLACED LATER
		// TODO select data set to graph based on type of GenericTest
		// (pendulum -> angular velocity/pos, inclined plane -> AccelX)
	 	graphAxis(AxisType.AccelX);
		
	}
	
	/**
	 * Handles zooming/panning of the graph.
	 * Called every time the event loop (Timer) ticks.
	 */
	public void redrawGraph() {

		zoomviewW = testLength / zoomLevel;
		zoomviewH = testLength / zoomLevel;

		xAxis.setLowerBound(zoomviewX - zoomviewW/1920);
		xAxis.setUpperBound(zoomviewX + zoomviewW/1920);
		
		yAxis.setLowerBound((-zoomviewH/9600) + zoomviewY);
		yAxis.setUpperBound((zoomviewH/9600) + zoomviewY);
		
		// DEBUG CODE FOR TESTING -- NOT FOR PRODUCTION
		/* TODO rework resolution/partial rendering of samples
		if (debugIgnoreResCheckbox.isSelected()) {
			resolution = 1;
		} else { 
			resolution = (int)(96 / zoomLevel);
		}
		*/

		/*
		ArrayList<Double> cleanTimeData = new ArrayList<Double>();										// setup new array list for time data
		ArrayList<Double> cleanSamplesData = new ArrayList<Double>();	
		*/

		/*
		for(int i = 0; i < originalSamples.size(); i+=resolution) {											//takes every "resolution" sample from the input data and adds it to the clean array lists for displaying
			
			cleanTimeData.add(originalTime.get(i));
			cleanSamplesData.add(originalSamples.get(i));
			
		}
		*/
		
		/*
		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();					//sets up a new series
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
		
		for (int i = 0; i < cleanSamplesData.size(); i++) {		
            seriesData.add(new XYChart.Data<>(cleanTimeData.get(i), cleanSamplesData.get(i))); 
		}

		lineChart.getData().clear();
		lineChart.getData().add(series);
		
		series.setData(seriesData);
		*/
		
	}

	/**
	 * Draws/removes an axis from the graph.
	 * @param axis the AxisType to be drawn/removed
	 */
	public void graphAxis(AxisType axis) {

		// get index of data set in line chart (if -1, does not exist)
		int dataIndex = lineChart.getData().indexOf(dataSets.get(axis));

		// get checkbox by looking up FXID (the name of the AxisType)
		CheckBox c = (CheckBox) lineChart.getScene().lookup("#" + axis);

		// if axis is not already graphed:
		if (dataIndex == -1) {

			System.out.println("Graphing " + axis);

			/*
			Hierarchy of graph in application:

			1) LineChart - contains all data sets and graphs, does not change
			2) XYChart.Series - child of LineChart; there can be multiple of these under one LineChart
			3) XYChart.Data - numerical data set component, only one per XYChart.Series
			*/
			XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
			ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

			// get time/samples data sets
			List<Double> time = genericTestOne.getAxis(axis).getTime();
			List<Double> data = genericTestOne.getAxis(axis).getSamples();

			// create (Time, Data) -> (X,Y) pairs
			for (int i = 0; i < data.size(); i+=resolution) {
				seriesData.add(new XYChart.Data<>(time.get(i), data.get(i)));
			}
	
			// TODO switch this to a pretty-printed version of AxisType?
			series.setName(axis.toString());

			// add XYChart.Data to XYChart.Series
			series.setData(seriesData);

			// add to HashMap of currently drawn axes
			dataSets.put(axis, series);
			
			// add XYChart.Series to LineChart
			lineChart.getData().add(series);

			// tick the checkbox
			c.setSelected(true);

		// if axis is already graphed:
		} else {

			System.out.println("Removing " + axis);

			// remove XYChart.Series from LineChart
			lineChart.getData().remove(dataSets.get(axis));

			// remove axis & XYChart.Series key-value pair from HashMap
			dataSets.remove(axis);

			// untick the checkbox
			c.setSelected(false);

		}

	}

	/**
	 * Redraws an axis already on the graph.
	 * @param axis the AxisType to be drawn/removed
	 */
	public void updateAxis(AxisType axis) {

		System.out.println("Updating " + axis);

		// retrieve XYChart.Series and ObservableList from HashMap
		XYChart.Series<Number, Number> series = dataSets.get(axis);
		ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();

		// clear samples in ObservableList
		seriesData.clear();

		// get time/samples data sets
		List<Double> time = genericTestOne.getAxis(axis).getTime();
		List<Double> data = genericTestOne.getAxis(axis).getSamples();

		// create (Time, Data) -> (X,Y) pairs
		for (int i = 0; i < data.size(); i+=resolution) {
			seriesData.add(new XYChart.Data<>(time.get(i), data.get(i)));
		}
		
		// add XYChart.Data to XYChart.Series
		series.setData(seriesData);

	}

	@FXML
	public void handleReset(ActionEvent event) {
		
		zoomviewX = resetZoomviewX;
		zoomviewY = resetZoomviewY;
		zoomLevel = 1.0;
		
		redrawGraph();
		
	}

	@FXML
	public void rollingBlockHandler(ActionEvent event) {
		
		int sampleBlockSize = 0;

		try {
			sampleBlockSize = Integer.parseInt(rollingBlockTextField.getText());
		}
		catch (NumberFormatException e) {
			Alert alert = new Alert(AlertType.ERROR);
			//alert.setTitle("Information Dialog");
			alert.setHeaderText("Invalid input");
			alert.setContentText("Please change your rolling average block size to a numerical value.");

			alert.showAndWait();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// workaround to "local variable defined in enclosing scope must be final or effectively final"
		final int blockSize = sampleBlockSize;

		// apply moving avgs to all currently drawn axes
		dataSets.forEach((axis, series) -> {
			genericTestOne.getAxis(axis).applyMovingAvg(blockSize);
			updateAxis(axis);
		});

	}
	
	/**
	 * Called by JavaFX when a data set's checkbox is ticked.
	 */
	@FXML
	public void chooseGraphAxis(ActionEvent event) {

		// get AxisType from checkbox
		CheckBox c = (CheckBox) event.getSource();
		String axis = (String) c.getId();
		AxisType a = AxisType.valueOf(axis);

		graphAxis(a);

	}
	
	/**
	 * Old method of passing data to NewGraph reading from DataOrganizer(s).
	 * @deprecated use {@link #setGenericTests(GenericTest, GenericTest)} instead.
	 */
	@Deprecated
	public void createTest(DataOrganizer d1, DataOrganizer d2) {
		
		// Create GenericTest object if module exists -- otherwise, "null"
		// "null" on one of these differentiates b/t One/Two Module setup
		if (d1 != null) genericTestOne = new GenericTest(d1);
        if (d2 != null) genericTestTwo = new GenericTest(d2);
		
		// TEST CODE - TO BE REPLACED LATER
		// TODO select data set to graph based on type of GenericTest
		// (pendulum -> angular velocity/pos, inclined plane -> AccelX)
        graphAxis(AxisType.AccelX);
		
	}

	/**
	 * Old method for graphing data sets.
	 * @deprecated use {@link #graphAxis(AxisType)} instead.
	 * @param timeData
	 * @param samplesData
	 */
	@Deprecated
	public void createSeries(ArrayList<Double> timeData, ArrayList<Double> samplesData) {
		
		originalTime = (ArrayList<Double>) timeData.clone();
		originalSamples = (ArrayList<Double>) samplesData.clone();
		
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

	//=========================================================
	// DEBUG CODE BELOW USED FOR TESTING -- NOT FOR PRODUCTION
	//=========================================================
	@FXML
	private CheckBox debugIgnoreResCheckbox;

	
	@FXML
	public void debugShowAllSamples(ActionEvent event) {

		CheckBox c = (CheckBox) event.getSource();
		resolution = c.isSelected() ? 1 : 20;
		
	}

	@FXML
	public void debugGraphAxis(ActionEvent event) {
		
		List<String> choices = new ArrayList<>();
		
		for (int i = 0; i < AxisType.values().length; i++) {

			choices.add(AxisType.valueOf(i).toString());

		}

		ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
		dialog.setTitle("[DEBUG] Graph Axis");
		dialog.setHeaderText("This is a testing feature to graph an AxisDataSeries.");
		dialog.setContentText("Choose axis:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			graphAxis(AxisType.valueOf(result.get()));
		}

	}

}