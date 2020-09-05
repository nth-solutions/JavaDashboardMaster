package com.bioforceanalytics.dashboard;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller class for the Data Analysis Graph. Handles all user interaction with the user interface,
 * as well as processing data sets for JavaFX use, retrieving calculations from
 * {@link com.bioforceanalytics.dashboard.GenericTest GenericTests}, calculating zooming/panning/scaling of the graph,
 * displaying data point labels, tracking currently graphed data sets, and more.
 */
public class GraphNoSINCController implements Initializable {

	// GenericTest represents a single module and associated test data
	private ArrayList<GenericTest> genericTests;

	// tracks the axes currently graphed on the line chart
	private ArrayList<GraphData> dataSets;

	// holds all the data set panels instantiated
	private ArrayList<DataSetPanel> panels;

	// the interval at which samples are drawn to the screen
	// if value is 20 (default), every 20th sample will be rendered
	// TODO make this an advanced user setting
	/**
	 * Internally used to calculate the graphing resolution.
	 * @deprecated DO NOT ACCESS THIS FIELD DIRECTLY, USE {@link #getResolution(AxisType)}
	 */
	@Deprecated
	private int resolution;

	// zooming + scrolling fields
	private double mouseX;
	private double mouseY;
	private double zoomviewScalarX;
	private double zoomviewScalarY;
	private double leftScrollPercentage;
	private double topScrollPercentage;

	/**
	 * The x-coordinate of the point denoting the center of the viewport.
	 */
	private double zoomviewX;

	/**
	 * The y-coordinate of the point denoting the center of the viewport.
	 */
	private double zoomviewY;

	/**
	 * The current width of the viewport.
	 */
	private double zoomviewW;

	/**
	 * The current height of the viewport.
	 */
	private double zoomviewH;

	private double resetZoomviewX;
	private double resetZoomviewY;
	private double resetZoomviewH;
	private double resetZoomviewW;

	private double lastMouseX;
	private double lastMouseY;

	private double scrollCenterX;
	private double scrollCenterY;

	// internal enum identifying the state of data analysis
	private GraphMode mode = GraphMode.NONE;

	// object containing the data for the slope line
	private XYChart.Series<Number, Number> slopeLine;

	// keeps track of a point selected during data analysis
	private Double[] selectedPoint;

	// the GraphData of the first point in slope/area calculations
	// used to check if the user selected points from two different data sets
	private GraphData selectedGraphData;

	// number of sig figs that labels are rounded to
	// TODO make this an advanced user setting
	private final int SIG_FIGS = 3;

	// BFA icon
	private Image icon;

	private static final Logger logger = LogManager.getLogger();

	@FXML
	private BFALineChart<Number, Number> lineChart;

	@FXML
	private BFANumberAxis xAxis;

	@FXML
	private BFANumberAxis yAxis;

	@FXML
	private Slider blockSizeSlider;
	
	@FXML
	private Label blockSizeLabel;

	@FXML
	private MultipleAxesLineChart multiAxis;

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private Text generalStatusLabel;

	@FXML
	private TextField baselineStartField;

	@FXML
	private TextField baselineEndField;

	@FXML
	private Button lineUpBtn;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		logger.info("Initializing Data Analysis graph...");

		icon = new Image(getClass().getResource("images/bfa.png").toExternalForm());

		dataSets = new ArrayList<GraphData>();
		panels = new ArrayList<DataSetPanel>();
		genericTests = new ArrayList<GenericTest>();

		// initialize graph mode variables
		Platform.runLater(() -> {
			setGraphMode(GraphMode.NONE);
		});

		// zoom/viewport settings
		resolution = 20;
		zoomviewScalarX = 1;
		zoomviewScalarY = 1;
		resetZoomviewX = 0;
		resetZoomviewY = 0;
		resetZoomviewW = 10;
		resetZoomviewH = 5;
		zoomviewX = 5;
		zoomviewY = 0;
		zoomviewW = 10;
		zoomviewH = 5;

		// initialize graph and axes
		lineChart = multiAxis.getBaseChart();
		lineChart.setAnimated(false);
		xAxis = (BFANumberAxis) lineChart.getXAxis();
		yAxis = (BFANumberAxis) lineChart.getYAxis();

		// hides symbols indicating data points on graph
		lineChart.setCreateSymbols(false);

		Platform.runLater(() -> {
			blockSizeSlider.valueProperty().addListener(e -> {
				applyMovingAvg();
			});
		});

		redrawGraph();

		// listener that runs every tick the mouse scrolls, calculates zooming
		multiAxis.setOnScroll(event -> {

			// saves the mouse location of the scroll event to x and y variables
			scrollCenterX = event.getX();
			scrollCenterY = event.getY();

			/**
			 * calculates the percentage of scroll either on the left or top of the screen
			 * e.g. if the mouse is at the middle of the screen, leftScrollPercentage is
			 * 0.5, if it is three quarters to the right, it is 0.75
			 */
			leftScrollPercentage = (scrollCenterX - 48) / (lineChart.getWidth() - 63);
			topScrollPercentage = (scrollCenterY - 17) / (lineChart.getHeight() - 88);

			// vertically scale the graph
			if (!event.isAltDown()) {
				zoomviewW -= zoomviewW * event.getDeltaY() / 300;
				zoomviewW = Math.max(lineChart.getWidth() * .00005, zoomviewW); 
				zoomviewX += zoomviewW * event.getDeltaY() * (leftScrollPercentage - .5) / 300;
			}

			// horizontally scale the graph
			if (!event.isControlDown()) {
				// decreases the zoomview width and height by an amount relative to the scroll
				// and the current size of the zoomview (slows down zooming at high levels of
				// zoom)
				zoomviewH -= zoomviewH * event.getDeltaY() / 300;

				zoomviewH = Math.max(lineChart.getHeight() * .00005, zoomviewH); 
				// moves the center of the zoomview to accomodate for the zoom, accounts for the
				// position of the mouse to try an keep it in the same spot
				zoomviewY -= zoomviewH * event.getDeltaY() * (topScrollPercentage - .5) / 300;
			}

			redrawGraph();

		});

		// listener that runs every tick the mouse is dragged, calculates panning
		multiAxis.setOnMouseDragged(event -> {

			if (mode == GraphMode.NONE) {

				// get the mouse x and y position relative to the line chart
				mouseX = event.getX();
				mouseY = event.getY();

				// calculate a scalar to convert pixel space into graph space (mouse data in
				// pixels, zoomview in whatever units the graph is in)
				zoomviewScalarX = (xAxis.getUpperBound() - xAxis.getLowerBound())
						/ (lineChart.getWidth() - yAxis.getWidth());
				zoomviewScalarY = (yAxis.getUpperBound() - yAxis.getLowerBound())
						/ (lineChart.getHeight() - xAxis.getHeight());

				// adds the change in mouse position this tick to the zoom view, converted into graph space
				zoomviewX -= (mouseX - lastMouseX) * zoomviewScalarX;
				zoomviewY += (mouseY - lastMouseY) * zoomviewScalarY;

				redrawGraph();

				// sets last tick's mouse data as this tick's
				lastMouseX = mouseX;
				lastMouseY = mouseY;

			}

		});

		// listener that runs when the mouse is clicked, only runs once per click, helps
		// to differentiate between drags
		multiAxis.setOnMousePressed(event -> {
			lastMouseX = event.getX();
			lastMouseY = event.getY();
		});

		// ADD ALL FULL WINDOW LISTENERS HERE
		Platform.runLater(() -> {

			Scene s = multiAxis.getScene();

			// reset graph mode on right click
			s.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
				if (e.getButton() == MouseButton.SECONDARY) setGraphMode(GraphMode.NONE);
			});

			// reset graph mode on escape
			s.setOnKeyPressed(e -> {
				if (e.getCode() == KeyCode.ESCAPE) setGraphMode(GraphMode.NONE);
			});

		});

	}

	/**
	 * Populates the data analysis graph with a single GenericTest.
	 * @param g the GenericTest representing a single trial
	 */
	public void setGenericTest(GenericTest g) {
		genericTests.add(g);
		initializePanels();
	}

	/**
	 * Populates the data analysis graph with multiple GenericTests. This
	 * constructor should be used when multiple modules/trials are used in a test.
	 * 
	 * @param g array of GenericTests (each one represents one trial)
	 */
	public void setGenericTests(ArrayList<GenericTest> g) {
		genericTests = g;
		initializePanels();
	}

	/**
	 * Populates the Data Analysis Graph by creating a GenericTest from a CSV and
	 * CSVP file.
	 * 
	 * @param CSVPath  the location of the CSV file containing test data
	 * @param CSVPPath the location of the CSVP file containing test parameters
	 */
	public void setGenericTestFromCSV(String CSVPath) {

		// wrapper for array version of CSV reading
		setGenericTestsFromCSV(new ArrayList<String>(Arrays.asList(CSVPath)));

	}

	/**
	 * Populates the Data Analysis Graph by creating a GenericTest from a CSV and
	 * CSVP file.
	 * 
	 * @param CSVPath  the location of the CSV file containing test data
	 * @param CSVPPath the location of the CSVP file containing test parameters
	 */
	public void setGenericTestsFromCSV(ArrayList<String> paths) {

		Alert loading = new Alert(AlertType.NONE, "Loading test data...");
		loading.setResult(ButtonType.OK);
		loading.show();

		CSVHandler reader = new CSVHandler();

		// read test data and create GenericTests
		for (String s : paths) {

			// try/catch placed inside loop to allow subsequent files to load,
			// even if loading one of them causes an error
			try {
				GenericTest g = new GenericTest(reader.readCSV(s), reader.readCSVP(s + "p"));
				genericTests.add(g);
			}
			catch (IOException e) {
				logger.error("IOException while loading test data");
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("Error reading test data");
				alert.setContentText("There was a problem loading \"" + s + "/p.\".");
				alert.showAndWait();
			}
			catch (NumberFormatException e) {
				logger.error("Could not parse test data");
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("Invalid test data");
				alert.setContentText("Error parsing data in \"" + s + "/p\".");
				alert.showAndWait();
			}

		}

		initializePanels();
		loading.close();

	}

	/**
	 * Internal method that generates data set panels from loaded GenericTests.
	 */
	private void initializePanels() {

		// get reference to root element
		Accordion a = (Accordion) lineChart.getScene().lookup("#dataSetAccordion");

		// remove existing panels
		panels.clear();
		a.getPanes().clear();

		// stop if no GTs are loaded
		if (genericTests.size() == 0) {
			logger.warn("Attempted to initialize panels with 0 GenericTests loaded");
			return;
		}

		// disable "line up trials" if only one GT exists 
		if (genericTests.size() == 1) {
			lineUpBtn.setDisable(true);
		}
		else {
			lineUpBtn.setDisable(false);
		}

		// get primary test
		GenericTest primaryTest = genericTests.get(0);

		// set graph title
		generalStatusLabel.setText(primaryTest.getGraphTitle());

		// if primary test is a lab template, create experiment panel
		if (!primaryTest.getClass().equals(GenericTest.class)) {

			ExperimentPanel experimentPanel = new ExperimentPanel();

			// set up experiment panels
			if (primaryTest instanceof ConservationMomentumModule) {
				((ConservationMomentumModule) primaryTest).getController().setupExperimentPanel(experimentPanel);
			} else if (primaryTest instanceof ConservationEnergyModule) {
				((ConservationEnergyModule) primaryTest).getController().setupExperimentPanel(experimentPanel);
			} else {
				primaryTest.setupExperimentPanel(experimentPanel);
			}

			// add panel to window
			a.getPanes().add(experimentPanel);

		}

		// create data set panels
		for (int i = 0; i < genericTests.size(); i++) {

			DataSetPanel d = new DataSetPanel(i);

			d.setText("Data Set " + (i + 1));

			// convey checkbox ticking on/off from child class to this class
			d.currentAxis.addListener((obs, oldVal, newVal) -> {

				// TODO part of the hack w/ change listeners
				if (newVal.intValue() == -1) return;

				// graph the given data set
				graphAxis(AxisType.valueOf(newVal.intValue()), d.getGTIndex());

			});

			panels.add(d);
			a.getPanes().add(d);

		}

		// graph any default axes (runs after data set panel is loaded)
		Platform.runLater(() -> {

			clearGraph();

			// TODO the first test isn't always the desired one, so we might want to change this
			//
			// graph all default axes for each GenericTest
			for (GenericTest g : genericTests) {
				for (AxisType axis : g.getDefaultAxes()) {
					graphAxis(axis, genericTests.indexOf(g));
				}
			}

			double testLength = primaryTest.getAxis(primaryTest.getDefaultAxes()[0]).testLength;

			// set width of viewport to fit the start and end of the test
			resetZoomviewX = testLength / 2;
			resetZoomviewY = 0;
			resetZoomviewW = testLength;
			resetZoomviewH = 10;

			handleReset();

		});

	}

	/**
	 * Handles zooming/panning of the graph.
	 */
	private void redrawGraph() {
		
		multiAxis.setXBounds(zoomviewX - zoomviewW / 2, zoomviewX + zoomviewW / 2);
		multiAxis.setYBounds(zoomviewY - zoomviewH / 2, zoomviewY + zoomviewH / 2);

		yAxis.setLowerBound(zoomviewY - zoomviewH / 2);
		yAxis.setUpperBound(zoomviewY + zoomviewH / 2);

		xAxis.setTickUnit(Math.pow(2, Math.floor(Math.log(zoomviewW) / Math.log(2)) - 3));
		yAxis.setTickUnit(Math.pow(2, Math.floor(Math.log(zoomviewH) / Math.log(2)) - 2));

		// update tick spacing based on zoom level
		for (GraphData d : multiAxis.axisChartMap.keySet()) {
			
			((BFANumberAxis) (multiAxis.axisChartMap.get(d).getYAxis())).setTickUnit(
					Math.pow(2, Math.floor(Math.log(zoomviewH) / Math.log(2)) - 2) * multiAxis.getAxisScalar(d.axis));
			((BFANumberAxis) (multiAxis.axisChartMap.get(d).getXAxis()))
					.setTickUnit(Math.pow(2, Math.floor(Math.log(zoomviewW) / Math.log(2)) - 3));
		}

		lineChart.clearArea();
		clearSlope();

	}

	/**
	 * Draws/removes an axis from the graph.
	 * @param axis the AxisType to be drawn/removed
	 * @param GTIndex the GenericTest to read data from
	 */
	public void graphAxis(AxisType axis, int GTIndex) {

		// if axis is not already graphed:
		if (findGraphData(GTIndex, axis) == null) {

			logger.info("Graphing " + axis + " for GT #" + (GTIndex+1));

			/*
			Hierarchy of graph in application:

			1) LineChart - contains all data sets and graphs, does not change
			2) XYChart.Series - child of LineChart; there can be multiple of these under one LineChart
			3) ObservableList - numerical data set component, only one per XYChart.Series
			*/
			XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
			ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

			List<Double> time;
			List<Double> data;

			// get time/samples data sets
			time = genericTests.get(GTIndex).getAxis(axis).getTime();
			data = genericTests.get(GTIndex).getAxis(axis).getSamples();

			// create (Time, Data) -> (X,Y) pairs
			for (int i = 0; i < data.size(); i += getResolution(axis)) {

				XYChart.Data<Number, Number> dataEl = new XYChart.Data<>(time.get(i), data.get(i) / multiAxis.getAxisScalar(axis));
			
				// add tooltip with (x,y) when hovering over data point
				dataEl.setNode(new DataPointLabel(time.get(i), data.get(i), axis, GTIndex));

				seriesData.add(dataEl);

			}

			// TODO switch this to a pretty-printed version of AxisType?
			series.setName(axis.toString());

			// add ObservableList to XYChart.Series
			series.setData(seriesData);

			GraphData d = new GraphData(GTIndex, axis, series);

			// add to list of currently drawn axes
			dataSets.add(d);

			// add graph with new axis
			multiAxis.addSeries(d);

			// hide all data point symbols
			for (Node n : lineChart.lookupAll(".chart-line-symbol")) {
				n.setStyle("-fx-background-color: transparent;");
			}

			// tick the checkbox
			panels.get(GTIndex).setCheckBox(true, axis);

		// if axis is already graphed:
		} else {

			logger.info("Removing " + axis + " for GT #" + (GTIndex+1));

			// remove axis from line chart
			multiAxis.removeAxis(axis, GTIndex);

			// remove GraphData from list of axes
			dataSets.remove(findGraphData(GTIndex, axis));

			// untick the checkbox
			panels.get(GTIndex).setCheckBox(false, axis);

		}

	}

	/**
	 * Redraws an axis already on the graph.
	 * @param axis the AxisType to be drawn/removed
	 * @param GTIndex the GenericTest to read data from
	 */
	public void updateAxis(AxisType axis, int GTIndex) {

		logger.info("Updating " + axis + " for GT #" + (GTIndex+1));

		// retrieve XYChart.Series and ObservableList from HashMap
		XYChart.Series<Number, Number> series = findGraphData(GTIndex, axis).data;
		ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();

		// clear samples in ObservableList
		seriesData.clear();

		// get time/samples data sets
		List<Double> time = genericTests.get(GTIndex).getAxis(axis).getTime();
		List<Double> data = genericTests.get(GTIndex).getAxis(axis).getSamples();

		// create (Time, Data) -> (X,Y) pairs
		for (int i = 0; i < data.size(); i += getResolution(axis)) {

			XYChart.Data<Number, Number> dataEl = new XYChart.Data<>(time.get(i), data.get(i) / multiAxis.getAxisScalar(axis));

			// add tooltip with (x,y) when hovering over data point
			dataEl.setNode(new DataPointLabel(time.get(i), data.get(i), axis, GTIndex));

			seriesData.add(dataEl);

		}

		// add ObservableList to XYChart.Series
		series.setData(seriesData);

		// hide all data point symbols
		for (Node n : lineChart.lookupAll(".chart-line-symbol")) {
			n.setStyle("-fx-background-color: transparent;");
		}

		// update legend colors
		multiAxis.styleLegend();

	}

	/**
	 * Removes all currently drawn axes from the graph.
	 * Does NOT clear the list of data sets or GenericTests.
	 */
	public void clearGraph() {

		// looping backwards to avoid ConcurrentModificationException
		for (int i = dataSets.size() - 1; i >= 0; i--) {

			// toggling a graph that's already drawn removes it
			graphAxis(dataSets.get(i).axis, dataSets.get(i).GTIndex);
		}

	}

	/**
	 * Updates the colors of currently graphed lines based on BFAColorMenu.
	 */
	public void updateGraphColors() {
		multiAxis.updateGraphColors();
	}

	@FXML
	public void handleReset() {

		zoomviewX = resetZoomviewX;
		zoomviewY = resetZoomviewY;
		zoomviewW = resetZoomviewW;
		zoomviewH = resetZoomviewH;

		// update all currently drawn data sets
		for (GraphData g : dataSets) {
			genericTests.get(g.GTIndex).addDataOffset(-genericTests.get(g.GTIndex).getDataOffset());
			updateAxis(g.axis, g.GTIndex);
		}

		redrawGraph();

	}

	public void applyMovingAvg() {

		// round slider to nearest integer
		int blockSize = (int) blockSizeSlider.getValue();

		// update smoothing label
		blockSizeLabel.setText("" + blockSize);

		// apply moving avgs to all currently drawn axes
		for (GraphData d : dataSets) {

			// if this is a magnetometer data set, divide block size by 10
			int axisBlockSize = d.axis.getValue() / 4 == 6 ? blockSize / 10 : blockSize;

			genericTests.get(d.GTIndex).getAxis(d.axis).smoothData(axisBlockSize);
			updateAxis(d.axis, d.GTIndex);
		}

	}

	@FXML
	public void resetMovingAvg() {

		// reset smoothing slider
		blockSizeSlider.setValue(AxisDataSeries.DEFAULT_BLOCK_SIZE);

		// apply moving average
		applyMovingAvg();

	}

	@FXML
	public void applyBaseline() {

		try {

			double start = Double.parseDouble(baselineStartField.getText());
			double end = Double.parseDouble(baselineEndField.getText());

			// make sure that baseline intervals are valid
			if (start - end == 0 || start > end || start < 0) {
				throw new Exception();
			}

			logger.info("Applying new baseline average [" + start + "," + end + "]");

			// loop through each GenericTest
			for (GenericTest g : genericTests) {

				// loop through each acceleration data set
				for (int i = 0; i <= AxisType.AccelMag.getValue(); i++) {

					// apply data normalization
					AxisDataSeries a = g.getAxis(AxisType.valueOf(i));
					a.applyNormalizedData(start, end);
				}

				// recalculate Vel/Disp data sets
				g.recalcKinematics();

			}

			// update all currently drawn acceleration axes
			for (GraphData g : dataSets) {
				
				// if axis class is kinematic data (Accel/Vel/Disp)
				if (g.axis.getValue() / 4 <= 2) {
					updateAxis(g.axis, g.GTIndex);
				}
			}
			
		} catch (Exception e) {

			logger.warn("Invalid baseline average inputs");
			e.printStackTrace();

			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Invalid inputs");
			alert.setContentText("Please make sure your baseline intervals are correct.");
			alert.showAndWait();

		}

	}

	@FXML
	public void toggleSlopeMode(ActionEvent event) {

		if (mode != GraphMode.SLOPE) {
			setGraphMode(GraphMode.SLOPE);
		}
		else setGraphMode(GraphMode.NONE);

	}

	@FXML
	public void toggleLineUpMode(ActionEvent event) {

		if (mode != GraphMode.LINEUP) {
			setGraphMode(GraphMode.LINEUP);
		}
		else setGraphMode(GraphMode.NONE);

	}

	@FXML
	public void toggleNormMode(ActionEvent event) {

		if (mode != GraphMode.NORM) {
			setGraphMode(GraphMode.NORM);
		}
		else setGraphMode(GraphMode.NONE);
		
	}

	@FXML
	public void toggleAreaMode(ActionEvent event) {

		if (mode != GraphMode.AREA) {
			setGraphMode(GraphMode.AREA);
		}
		else setGraphMode(GraphMode.NONE);

	}

	@FXML
	public void clearDataSets(ActionEvent event) {

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setHeaderText("Clear Data Sets");
		alert.setContentText("Are you sure you want to clear all data sets?");
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {

			logger.info("Clearing all data sets...");

			// clear GTs, un-graph data sets, then clear the data sets list
			genericTests.clear();
			clearGraph();
			dataSets.clear();

			// redraw data set panels
			initializePanels();
		}

	}

	@FXML
	private void importCSV(ActionEvent event) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a CSV");
		fileChooser.setInitialDirectory(new File(Settings.get("CSVSaveLocation")));

		// filters file selection to CSVs only
		FileChooser.ExtensionFilter filterCSVs = new FileChooser.ExtensionFilter("Select a File (*.csv)", "*.csv");
		fileChooser.getExtensionFilters().add(filterCSVs);

		List<File> files = fileChooser.showOpenMultipleDialog(null);

		// if user doesn't choose a file or closes window, don't continue
		if (files == null) return;

		// keep track of verified CSV/CSVP file paths
		ArrayList<String> paths = new ArrayList<String>();

		// loop through each file, checking for CSVP pair
		for (File f : files) {
			
			String CSVFilePath = f.toString();

			// if no matching CSVP file found, don't continue
			if (!new File(CSVFilePath + "p").exists()) {

				Alert alert = new Alert(AlertType.ERROR);

				alert.setHeaderText("Missing test data");
				alert.setContentText("The matching CSVP file could not be found.");
				alert.showAndWait();

				logger.warn("No matching CSVP file found for '" + CSVFilePath + "'");
				return;

			}

			paths.add(CSVFilePath);

		}

		setGenericTestsFromCSV(paths);

	}

	@FXML
	private void openColorMenu(ActionEvent event) {

		Stage primaryStage = new Stage();
        Parent root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/BFAColorMenu.fxml"));
		
        try {

			root = loader.load();

			// set parent of color menu to allow communication b/t classes
			((BFAColorMenu) loader.getController()).setParent(this);

        } catch (IOException e) {
			e.printStackTrace();
			return;
        }

        if (root != null) primaryStage.setScene(new Scene(root));

		// ensure that color menu stays on top and blocks everything else
		primaryStage.setAlwaysOnTop(true);
		primaryStage.initModality(Modality.APPLICATION_MODAL);
		primaryStage.initOwner(lineChart.getScene().getWindow());

        primaryStage.setTitle("Data Analysis Graph - Color Menu");
		primaryStage.getIcons().add(icon);
        primaryStage.show();
        primaryStage.setResizable(false);

	}

	@FXML
	private void changeResolution(ActionEvent event) {

		TextInputDialog dialog = new TextInputDialog(Integer.toString(resolution));

		dialog.setTitle("Change Resolution");
		dialog.setHeaderText("Change Resolution");
		dialog.setContentText("Warning: entering a value below the default (20) could severely reduce performance.");

		Optional<String> result = dialog.showAndWait();

		// if user clicks cancel, end method
		if (!result.isPresent()) return;

		try {

			resolution = Integer.parseInt(result.get());

			if (resolution <= 0) throw new IllegalArgumentException();

		}
		catch (NumberFormatException e) {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Invalid input");
			alert.setContentText("Please enter a numerical value.");

			alert.showAndWait();
			return;

		}
		catch (IllegalArgumentException e) {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Invalid input");
			alert.setContentText("Please enter a value greater than 0.");

			alert.showAndWait();
			return;

		}

		Alert a = new Alert(AlertType.NONE, "Reloading data sets...");
		a.setResult(ButtonType.OK);
		a.show();

		// TODO potentially add ControlsFX to make async loading popup
		// this would allow us to have a progress bar in a pop up like this for each axis
		for (GraphData d : dataSets) {
			updateAxis(d.axis, d.GTIndex);
		}

		a.close();

	}

	/**
	 * Sets the graphing mode of the application.
	 * Use this to change between viewing the graph and finding slope/area modes.
	 * @param g the {@link GraphMode} to change to.
	 */
	private void setGraphMode(GraphMode g) {

		mode = g;

		// first index is x, second index is y
		selectedPoint = new Double[2];

		selectedGraphData = null;

		switch (g) {

			case NONE:
				lineChart.getScene().setCursor(Cursor.DEFAULT);
				break;

			case SLOPE:
			case AREA:
			case LINEUP:
			case NORM:
				lineChart.getScene().setCursor(Cursor.CROSSHAIR);
				break;

			default:
				logger.error("Error setting graph mode");
				break;

		}

		logger.info("Set graph mode to " + g);

	}

	/**
	 * Graphs a line tangent to the given point.
	 */
	public void graphSlope(double x, double y, AxisType axis, int GTIndex) {

		clearSlope();

		// get slope value "m"
		double m = genericTests.get(GTIndex).getAxis(axis).getSlope(x, getResolution(axis));

		slopeLine = new XYChart.Series<Number, Number>();
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

		// Formula used is point-slope form of a line:
		// y - y0 = m(x - x0) -> y = m(x - x0) + y0

		double axisScalar = multiAxis.getAxisScalar(axis);

		// plot the point (x0,y0) shared by the graph and tangent line
		seriesData.add(new XYChart.Data<Number,Number>(x, y / axisScalar));

		// plot the point one x-unit to the left (x = x0-1)
		seriesData.add(new XYChart.Data<Number,Number>(x-1, (m * ((x-1)-x) + y) / axisScalar));

		// plot the point one x-unit to the right (x = x0+1)
		seriesData.add(new XYChart.Data<Number,Number>(x+1, (m * ((x+1)-x) + y) / axisScalar));

		// add label for slope value to the center of the line, above the tangent point
		seriesData.get(0).setNode(createSlopeLabel(m));

		slopeLine.setName("Slope (" + axis + ")");
		slopeLine.setData(seriesData);

		// TODO clean up, don't need to recreate XYChart.Series
		lineChart.getData().add(slopeLine);
		slopeLine.getNode().getStyleClass().add("slope-line");

		// update legend colors
		multiAxis.styleLegend();

		setGraphMode(GraphMode.NONE);

	}

	/**
	 * Graphs a secant line between the given points.
	 */
	public void graphSlope(double x1, double y1, double x2, double y2, AxisType axis, int GTIndex) {

		// if user chose the same point twice, graph a tangent line
		if (x1 == x2 && y1 == y2) {
			graphSlope(x1, y1, axis, GTIndex);
			return;
		}

		clearSlope();

		// get slope value "m"
		double m = genericTests.get(GTIndex).getAxis(axis).getSlope(x1, x2);

		slopeLine = new XYChart.Series<Number, Number>();
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

		// Formula used is point-slope form of a line:
		// y - y0 = m(x - x0) -> y = m(x - x0) + y0

		double axisScalar = multiAxis.getAxisScalar(axis);

		// plot the left endpoint of the line
		seriesData.add(new XYChart.Data<Number,Number>(x1, y1 / axisScalar));

		// plot the midpoint of the line
		seriesData.add(new XYChart.Data<Number, Number>((x1+x2)/2, ((y1+y2)/2) / axisScalar));

		// plot the right endpoint of the line
		seriesData.add(new XYChart.Data<Number,Number>(x2, y2 / axisScalar));

		// add label for slope value above the midpoint
		seriesData.get(1).setNode(createSlopeLabel(m));

		slopeLine.setName("Slope (" + axis + ")");
		slopeLine.setData(seriesData);

		// TODO clean up, don't need to recreate XYChart.Series
		lineChart.getData().add(slopeLine);
		slopeLine.getNode().getStyleClass().add("slope-line");

		// update legend colors
		multiAxis.styleLegend();

		setGraphMode(GraphMode.NONE);

	}

	/**
	 * Clears the slope line at/between points (if currently drawn).
	 */
	private void clearSlope() {

		if (slopeLine != null) {
			lineChart.getData().remove(slopeLine);

			// update legend colors
			multiAxis.styleLegend();
		}

	}

	/**
	 * Finds a GraphData object given its fields.
	 * @param GTIndex the GenericTest associated with the GraphData
	 * @param axis the AxisType associated with the GraphData
	 */
	private GraphData findGraphData(int GTIndex, AxisType axis) {

		for (GraphData g : dataSets) {
			if (g.GTIndex == GTIndex && g.axis == axis) return g;
		}

		return null;

	}

	/**
	 * Creates the label for the slope of a tangent/secant line.
	 * @param m the value for the slope
	 */
	private StackPane createSlopeLabel(double m) {

		double roundedM = new BigDecimal(m).round(new MathContext(SIG_FIGS)).doubleValue();
		Label label = new Label("Slope: " + roundedM);

		// add styling to label
		label.getStyleClass().addAll("hover-label");

		// place the label above the data point
		label.translateYProperty().bind(label.heightProperty().divide(-1));

		label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

		// make label display full floating-point number when clicked
		label.setOnMouseClicked(e -> label.setText("Slope: " + m));

		// place label in StackPane and return
		StackPane pane = new StackPane();

		pane.setPrefSize(15, 15);
		pane.setStyle("-fx-background-color: transparent");
		pane.getChildren().add(label);

		return pane;

	}

	/**
	 * JavaFX component added to data points on graph.
	 */
	class DataPointLabel extends StackPane {

		DataPointLabel(double x, double y, AxisType axis, int GTIndex) {

			// round to the given number of sig figs
			final double roundedX = new BigDecimal(x).round(new MathContext(SIG_FIGS)).doubleValue();
			final double roundedY = new BigDecimal(y).round(new MathContext(SIG_FIGS)).doubleValue();
			setPrefSize(15, 15);

			// allows mouse events to pass through label
			// makes selecting nearby data points easier
			setPickOnBounds(false);

			// when mouse hovers over data point, display label
			setOnMouseEntered(e -> {

				// add the hover (x,y) label
				getChildren().setAll(createLabel(roundedX, roundedY));

				// temporarily draw the data point symbol using the appropriate axis color
				setStyle("-fx-background-color: " + BFAColorMenu.getHexString(axis) + ", white;");

				// ensure the label is on top of the graph
				toFront();

			});

			// when mouse stops hovering over data point, remove label
			setOnMouseExited(e -> {

				// hide the data point symbol
				setStyle("-fx-background-color: transparent");

				// hide the label from the graph
				getChildren().clear();

			});
			setOnMouseClicked(e -> {

				// tracks if this is the first click in an action
				boolean firstClick;

				// check if point was selected and if so, save its info
				if (selectedPoint[0] == null && selectedPoint[1] == null) {

					logger.info("Selected point (" + x + "," + y + ")");
					selectedPoint = new Double[] {x,y};
					selectedGraphData = new GraphData(GTIndex, axis, null);

					firstClick = true;
					
				} else {
					firstClick = false;
				}

				if (mode == GraphMode.SLOPE) {

					// tangent line graphing mode
					if (e.isShiftDown()) {
						logger.info("Graphing tangent line...");
						graphSlope(x, y, axis, GTIndex);
					}
					else if (!firstClick) {

						// check for any issues with calculating b/t different data sets
						if (selectedGraphData.GTIndex != GTIndex || selectedGraphData.axis != axis) {

							Alert a = new Alert(AlertType.ERROR, "Slope calculations only work when selecting points from the same data set.");
							a.showAndWait();
							
							setGraphMode(GraphMode.NONE);
							return;
							
						}

						logger.info("Graphing secant line...");
						graphSlope(selectedPoint[0], selectedPoint[1], x, y, axis, GTIndex);

					}

				}
				else if (mode == GraphMode.AREA && !firstClick) {

					logger.info("Graphing area...");

					// check for any issues with calculating b/t different data sets
					if (selectedGraphData.GTIndex != GTIndex || selectedGraphData.axis != axis) {

						Alert a = new Alert(AlertType.ERROR, "Area calculations only work when selecting points from the same data set.");
						a.showAndWait();

						setGraphMode(GraphMode.NONE);
						return;
					
					}

					// ensures that x1 is always less than x2
					double[] areaBounds = new double[] {selectedPoint[0], x};
					Arrays.sort(areaBounds);

					// calculate the definite integral with the given limits
					double area = genericTests.get(GTIndex).getAxis(axis).getAreaUnder(areaBounds[0], areaBounds[1]);

					double axisScalar = multiAxis.getAxisScalar(axis);

					// p1 = (x1, y1), p2 = (x2, y2)
					XYChart.Data<Double, Double> p1 = new XYChart.Data<Double, Double>(selectedPoint[0], selectedPoint[1] / axisScalar);
					XYChart.Data<Double, Double> p2 = new XYChart.Data<Double, Double>(x, y / axisScalar);

					// ensure the lower bound is less than the upper bound
					if (selectedPoint[0] == areaBounds[0]) {
						lineChart.graphArea(p1, p2, findGraphData(GTIndex, axis), area, SIG_FIGS);
					}
					else {
						lineChart.graphArea(p2, p1, findGraphData(GTIndex, axis), area, SIG_FIGS);
					}

					setGraphMode(GraphMode.NONE);

				}
				else if (mode == GraphMode.LINEUP && !firstClick) {

					// shift the graph by this point's x-value minus the selected point's x-value
					genericTests.get(selectedGraphData.GTIndex).addDataOffset(roundedX - selectedPoint[0]);
					for(GraphData g : dataSets){
						updateAxis(g.axis, g.GTIndex);
						logger.info(genericTests.get(g.GTIndex).getDataOffset());
					}

					setGraphMode(GraphMode.NONE);

				}
				else if (mode == GraphMode.NORM) {

					AxisDataSeries a = genericTests.get(GTIndex).getAxis(axis);
					a.vertShift(-y);
					updateAxis(axis, GTIndex);

					setGraphMode(GraphMode.NONE);

				}
				else if (mode == GraphMode.NONE) {
					// display full floating-point number on click
					getChildren().setAll(createLabel(x, y));
				}

			});

		}

		// helper method to generate data point (x,y) label
		private Label createLabel(double x, double y) {

			Label label = new Label("(" + x + ", " + y + ")");

			// add styling to label
			label.getStyleClass().addAll("hover-label");

			// place the label above the data point
			label.translateYProperty().bind(label.heightProperty().divide(-1));

			label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

      		return label;

		}

	}

	/**
	 * Calculates the graphing resolution used when plotting data to the screen.
	 * This should be used instead of directly accessing the "resolution" field.
	 * @param axis the AxisType to get the resolution for
	 * @return the graphing resolution of the given axis
	 */
	private int getResolution(AxisType axis) {
		// if this is a magnetometer data set, divide resolution by 10 to match 960 sps data sets
		return (axis.getValue() / 4 == 6) ? resolution/10 : resolution;
	}

	/**
	 * Internal enum used to designate the state of data analysis;
	 * <p><code>GraphMode.NONE</code> is when the user is zooming/panning,</p>
	 * <p><code>GraphMode.SLOPE</code> is when the user is selecting a single point for a slope calculation,</p>
	 * <p><code>GraphMode.AREA</code> is when the user is selecting the section for an area calculation,</p>
	 * <p><code>GraphMode.LINEUP</code> is when the user is selecting the points to line up in two different data sets.</p>
	 */
	private enum GraphMode {
		NONE,
		SLOPE,
		AREA,
		LINEUP,
		NORM
	}

}