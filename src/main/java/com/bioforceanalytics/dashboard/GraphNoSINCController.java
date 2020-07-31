package com.bioforceanalytics.dashboard;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

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

	// keeps track of first point in secant line calculation
	private Double[] slopePoint;

	// keeps track of first point in area calculation
	private Double[] areaPoint;

	// the GraphData of the first point in slope/area calculations
	// used to check if the user selected points from two different data sets
	private GraphData selectedGraphData;

	// number of sig figs that labels are rounded to
	// TODO make this an advanced user setting
	private final int SIG_FIGS = 3;

	@FXML
	private BFALineChart<Number, Number> lineChart;

	@FXML
	private BFANumberAxis xAxis;

	@FXML
	private BFANumberAxis yAxis;

	@FXML
	private TextField rollingBlockTextField;

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

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		System.out.println("Initializing Data Analysis graph...");

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

				// adds the change in mouse position this tick to the zoom view, converted into
				// graph space
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
		
		// if ArrayList has GenericTests, create panels
		if (g.size() > 0) initializePanels();

	}

	/**
	 * Populates the data analysis graph by creating a GenericTest from a CSV and
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
	 * Populates the data analysis graph by creating a GenericTest from a CSV and
	 * CSVP file.
	 * 
	 * @param CSVPath  the location of the CSV file containing test data
	 * @param CSVPPath the location of the CSVP file containing test parameters
	 */
	public void setGenericTestsFromCSV(ArrayList<String> paths) {

		genericTests.clear();

		Alert a = new Alert(AlertType.NONE, "Loading test data...");
		a.setResult(ButtonType.OK);
		a.show();

		CSVHandler reader = new CSVHandler();

		for (String s : paths) {
			GenericTest g = new GenericTest(reader.readCSV(s), reader.readCSVP(s + "p"));
			genericTests.add(g);
		}

		initializePanels();
		a.close();

	}

	private void initializePanels() {

		// get primary test
		GenericTest g = genericTests.get(0);

		// get reference to root element
		Accordion a = (Accordion) lineChart.getScene().lookup("#dataSetAccordion");
		generalStatusLabel.setText(g.getGraphTitle());

		// remove existing panels
		panels.clear();
		a.getPanes().clear();

		// if primary test is a lab template, create experiment panel
		if (!g.getClass().equals(GenericTest.class)) {

			ExperimentPanel experimentPanel = new ExperimentPanel();
			genericTests.get(0).setupExperimentPanel(experimentPanel);
			a.getPanes().add(experimentPanel);

		}

		// create data set panels
		for (int i = 0; i < genericTests.size(); i++) {

			DataSetPanel d = new DataSetPanel(i);

			d.setText("Data Set " + (i + 1));

			// convey checkbox ticking on/off from child class to this class
			d.currentAxis.addListener((obs, oldVal, newVal) -> {

				// TODO part of the hack w/ change listeners
				if (newVal.intValue() == -1)
					return;
				graphAxis(AxisType.valueOf(newVal.intValue()), d.getGTIndex());

			});

			panels.add(d);
			a.getPanes().add(d);

		}

		// graph any default axes
		// runs after data set panel is loaded
		Platform.runLater(() -> {

			clearGraph();

			// TODO the first test isn't always the desired one, so we might want to change
			// this
			for (AxisType axis : g.getDefaultAxes()) {
				graphAxis(axis, 0);
			}

			double testLength = g.getAxis(g.getDefaultAxes()[0]).testLength;

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
		for (AxisType a : multiAxis.axisChartMap.keySet()) {
			((BFANumberAxis) (multiAxis.axisChartMap.get(a).getYAxis())).setTickUnit(
					Math.pow(2, Math.floor(Math.log(zoomviewH) / Math.log(2)) - 2) * multiAxis.getAxisScalar(a));
			((BFANumberAxis) (multiAxis.axisChartMap.get(a).getXAxis()))
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

			System.out.println("Graphing " + axis);

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
			for (int i = 0; i < data.size(); i+=resolution) {

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
			multiAxis.addSeries(d, Color.rgb(((axis.getValue() + 20) % 31) * 8,((axis.getValue() + 30) % 31) * 8,((axis.getValue() + 10) % 31) * 8));

			// hide all data point symbols UNLESS they are for the legend
			for (Node n : lineChart.lookupAll(".chart-line-symbol")) {
				if (!n.getStyleClass().contains(".chart-legend-item-symbol")) {
					n.setStyle("-fx-background-color: transparent;");
				}
			}

			// tick the checkbox
			panels.get(GTIndex).setCheckBox(true, axis);

		// if axis is already graphed:
		} else {

			System.out.println("Removing " + axis);

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

		System.out.println("Updating " + axis);

		// retrieve XYChart.Series and ObservableList from HashMap
		XYChart.Series<Number, Number> series = findGraphData(GTIndex, axis).data;
		ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();

		// clear samples in ObservableList
		seriesData.clear();

		// get time/samples data sets
		List<Double> time = genericTests.get(GTIndex).getAxis(axis).getTime();
		List<Double> data = genericTests.get(GTIndex).getAxis(axis).getSamples();

		// create (Time, Data) -> (X,Y) pairs
		for (int i = 0; i < data.size(); i+=resolution) {

			XYChart.Data<Number, Number> dataEl = new XYChart.Data<>(time.get(i), data.get(i) / multiAxis.getAxisScalar(axis));

			// add tooltip with (x,y) when hovering over data point
			dataEl.setNode(new DataPointLabel(time.get(i), data.get(i), axis, GTIndex));

			seriesData.add(dataEl);

		}

		// add ObservableList to XYChart.Series
		series.setData(seriesData);

		// hide all data point symbols UNLESS they are for the legend
		for (Node n : lineChart.lookupAll(".chart-line-symbol")) {
			if (!n.getStyleClass().contains(".chart-legend-item-symbol")) {
				n.setStyle("-fx-background-color: transparent;");
			}
		}

	}

	/**
	 * Removes all currently drawn axes from the graph.
	 */
	public void clearGraph() {

		// looping backwards to avoid ConcurrentModificationException
		for (int i = dataSets.size() - 1; i >= 0; i--) {

			// toggling a graph that's already drawn removes it
			graphAxis(dataSets.get(i).axis, dataSets.get(i).GTIndex);
		}

	}

	@FXML
	public void handleReset() {

		zoomviewX = resetZoomviewX;
		zoomviewY = resetZoomviewY;
		zoomviewW = resetZoomviewW;
		zoomviewH = resetZoomviewH;
		redrawGraph();

	}

	@FXML
	public void applyMovingAvg() {

		// verify that the input is a number
		try {

			int blockSize = Integer.parseInt(rollingBlockTextField.getText());

			// apply moving avgs to all currently drawn axes
			for (GraphData d : dataSets) {
				genericTests.get(d.GTIndex).getAxis(d.axis).applyCustomMovingAvg(blockSize);
				updateAxis(d.axis, d.GTIndex);
			}
		
		} catch (NumberFormatException e) {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Invalid input");
			alert.setContentText("Please change your rolling average block size to a numerical value.");

			alert.showAndWait();

		}

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

			System.out.println("Applying new baseline average [" + start + "," + end + "]");

			// update all currently drawn axes with new baseline
			for (GraphData g : dataSets) {
				AxisDataSeries a = genericTests.get(g.GTIndex).getAxis(g.axis);
				a.applyNormalizedData(start, end, a.sampleRate);
				updateAxis(g.axis, g.GTIndex);
			}
			
		} catch (Exception e) {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Invalid inputs");
			alert.setContentText("Please make sure your baseline intervals are correct.");

			alert.showAndWait();

		}

	}

	@FXML
	public void toggleSlopeMode(ActionEvent event) {

		System.out.println("Toggling slope mode...");

		if (mode != GraphMode.SLOPE) {
			setGraphMode(GraphMode.SLOPE);
		}
		else setGraphMode(GraphMode.NONE);

	}

	@FXML
	public void toggleAreaMode(ActionEvent event) {

		System.out.println("Toggling area mode...");

		if (mode != GraphMode.AREA) {
			setGraphMode(GraphMode.AREA);
		}
		else setGraphMode(GraphMode.NONE);

	}

	@FXML
	public void importCSV(ActionEvent event) {

		// used to load CSV test data directory
		Settings settings = new Settings();
		settings.loadConfigFile();

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a CSV");
		fileChooser.setInitialDirectory(new File(settings.getKeyVal("CSVSaveLocation")));

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

				System.out.println("No matching CSVP file found for '" + CSVFilePath + "'");
				return;

			}

			paths.add(CSVFilePath);

		}

		setGenericTestsFromCSV(paths);

	}

	@FXML
	private void changeResolution(ActionEvent event) {

		TextInputDialog dialog = new TextInputDialog(Integer.toString(resolution));

		dialog.setTitle("Change Resolution");
		dialog.setHeaderText("Change Resolution");
		dialog.setContentText("Warning: entering a value below the default (20) could severely reduce performance.");

		Optional<String> result = dialog.showAndWait();

		try {
			if (result.isPresent()) resolution = Integer.parseInt(result.get());
		}
		catch (NumberFormatException e) {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Invalid input");
			alert.setContentText("Please enter a numerical value.");

			alert.showAndWait();
			return;

		}

		Alert a = new Alert(AlertType.NONE, "Reloading data sets...");
		a.setResult(ButtonType.OK);
		a.show();

		// TODO add ControlsFX to make async loading popup
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
		slopePoint = new Double[2];
		areaPoint = new Double[2];

		selectedGraphData = null;

		switch (g) {

			case NONE:
				lineChart.getScene().setCursor(Cursor.DEFAULT);
				break;

			case SLOPE:
			case AREA:
				lineChart.getScene().setCursor(Cursor.CROSSHAIR);
				break;

			default:
				System.out.println("Error setting graph mode");
				break;

		}

	}

	/**
	 * Graphs a line tangent to the given point.
	 */
	public void graphSlope(double x, double y, AxisType axis, int GTIndex) {

		clearSlope();

		// get slope value "m"
		double m = genericTests.get(GTIndex).getAxis(axis).getSlope(x, resolution);

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

		setGraphMode(GraphMode.NONE);

	}

	/**
	 * Clears the slope line at/between points (if currently drawn).
	 */
	private void clearSlope() {
		if (slopeLine != null) lineChart.getData().remove(slopeLine);
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

				// temporarily draw the data point symbol
				// this is done by removing the "transparent" style
				setStyle("");

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

				if (mode == GraphMode.SLOPE) {

					// tangent line graphing mode
					if (e.isShiftDown()) {
						System.out.println("Graphing tangent line...");
						graphSlope(x, y, axis, GTIndex);
					}
					else {

						// secant line graphing mode
						if (slopePoint[0] == null && slopePoint[1] == null) {
							System.out.println("Selected first slope point");
							slopePoint = new Double[] {x,y};
							selectedGraphData = new GraphData(GTIndex, axis, null);
						}
						else {

							// check for any issues with calculating b/t different data sets
							if (selectedGraphData != null && (selectedGraphData.GTIndex != GTIndex || selectedGraphData.axis != axis)) {

								Alert a = new Alert(AlertType.ERROR, "Slope calculations only work when selecting points from the same data set.");
								a.showAndWait();
								
								setGraphMode(GraphMode.NONE);
								return;
								
							}

							System.out.println("Graphing secant line...");
							graphSlope(slopePoint[0], slopePoint[1], x, y, axis, GTIndex);
						}
					}

				}
				else if (mode == GraphMode.AREA) {

					// select first point of area calculation
					if (areaPoint[0] == null && areaPoint[1] == null) {
						System.out.println("Selected first area point");
						areaPoint = new Double[] {x,y};
						selectedGraphData = new GraphData(GTIndex, axis, null);
					}
					// calculate and shade area
					else {

						System.out.println("Graphing area...");

						// check for any issues with calculating b/t different data sets
						if (selectedGraphData != null && (selectedGraphData.GTIndex != GTIndex || selectedGraphData.axis != axis)) {

							Alert a = new Alert(AlertType.ERROR, "Area calculations only work when selecting points from the same data set.");
							a.showAndWait();

							setGraphMode(GraphMode.NONE);
							return;
						
						}

						// ensures that x1 is always less than x2
						double[] areaBounds = new double[] {areaPoint[0], x};
						Arrays.sort(areaBounds);

						// calculate the definite integral with the given limits
						double area = genericTests.get(GTIndex).getAxis(axis).getAreaUnder(areaBounds[0], areaBounds[1]);

						double axisScalar = multiAxis.getAxisScalar(axis);

						// p1 = (x1, y1), p2 = (x2, y2)
						XYChart.Data<Double, Double> p1 = new XYChart.Data<Double, Double>(areaPoint[0], areaPoint[1] / axisScalar);
						XYChart.Data<Double, Double> p2 = new XYChart.Data<Double, Double>(x, y / axisScalar);

						// ensure the lower bound is less than the upper bound
						if (areaPoint[0] == areaBounds[0]) {
							lineChart.graphArea(p1, p2, findGraphData(GTIndex, axis).data.getData(), area, SIG_FIGS);
						}
						else {
							lineChart.graphArea(p2, p1, findGraphData(GTIndex, axis).data.getData(), area, SIG_FIGS);
						}

						setGraphMode(GraphMode.NONE);
					}

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
	 * Internal enum used to designate the state of data analysis;
	 * <p><code>GraphMode.NONE</code> is when the user is zooming/panning,</p>
	 * <p><code>GraphMode.SLOPE</code> is when the user is selecting a single point for a slope calculation,</p>
	 * <p>and <code>GraphMode.Area</code> is when the user is selecting the section for an area calculation.</p>
	 */
	private enum GraphMode {
		NONE,
		SLOPE,
		AREA
	}

}