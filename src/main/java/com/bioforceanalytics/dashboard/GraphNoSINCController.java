package com.bioforceanalytics.dashboard;

import java.awt.Desktop;
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
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Throwables;
import org.controlsfx.dialog.ProgressDialog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller class for the BioForce Graph. Handles all user interaction with
 * the user interface, as well as processing data sets for JavaFX use,
 * retrieving calculations from
 * {@link com.bioforceanalytics.dashboard.GenericTest GenericTests}, calculating
 * zooming/panning/scaling of the graph, displaying data point labels, tracking
 * currently graphed data sets, and more.
 */
public class GraphNoSINCController implements Initializable {

	// GenericTest represents a single module and associated test data
	private ArrayList<GenericTest> genericTests;

	// CustomTest represents an abstract GenericTest that is not associated with a
	// module axis
	private ArrayList<CustomTest> customTests;

	// tracks the axes currently graphed on the line chart
	private ArrayList<GraphData> dataSets;

	// holds all the data set panels instantiated
	private ArrayList<DataSetPanel> panels;

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

	private double yShift = 0.0; 

	// BFA icon
	private Image icon;

	// determins whether or not the simulataneous listeners should fire for the
	// block size value
	private boolean blockSizeIsChanging;

	private static final Logger logger = LogController.start();

	@FXML
	private BFALineChart<Number, Number> lineChart;

	@FXML
	private Slider blockSizeSlider;

	@FXML
	private TextField blockSizeField;

	@FXML
	private MultiAxisLineChart multiAxis;

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private Text generalStatusLabel;

	@FXML
	private HBox accelNormForm;

	@FXML
	private CheckBox accelNormCheckBox;

	@FXML
	private Button accelNormBtn;

	@FXML
	private TextField baselineStartField;

	@FXML
	private TextField baselineEndField;

	@FXML
	private Button lineUpBtn;

	public ArrayList<CustomEquation> customEquations = new ArrayList<CustomEquation>();

	public static ArrayList<Variable> variables = new ArrayList<Variable>();

	public EquationPanel equationPanel;

	private CustomTest customAxisTest;
	@FXML
	private MediaView mediaView;
	@FXML
	private Pane mediaViewPane;
	@FXML
	private Rectangle scrubber;

	@FXML
	private HBox sincControls;
	@FXML
	private Slider playbackSlider;

	private double[] areaBounds; 
	private double area; 
	private double axisScalar; 
	private XYChart.Data<Double, Double> p1 = new XYChart.Data<Double, Double>(); 
	private XYChart.Data<Double, Double> p2 = new XYChart.Data<Double, Double>(); 

	private boolean areaFlag; 
	private boolean slopeFlag; 

	//slope 
	private double m; 

	private ArrayList<Double> prevY1 = new ArrayList<Double>();
    private ArrayList<Double> prevY2 = new ArrayList<Double>();
	private ArrayList<Double> prevY = new ArrayList<Double>();

	public void setAreaFlag(boolean areaFlag) {
        this.areaFlag = areaFlag;
    }

	public void setSlopeFlag(boolean slopeFlag) {
        this.slopeFlag = slopeFlag;
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		logger.info("Initializing BioForce Graph...");

		icon = new Image(getClass().getResource("images/bfa.png").toExternalForm());

		dataSets = new ArrayList<GraphData>();
		panels = new ArrayList<DataSetPanel>();
		genericTests = new ArrayList<GenericTest>();
		lineChart = multiAxis.getBaseChart();
		lineChart.initSINC(mediaView, scrubber);

		// pass reference of controller to graph
		multiAxis.setController(this);

		Platform.runLater(() -> {

			// initialize graph mode variables
			setGraphMode(GraphMode.NONE);

			// update smoothing in real-time
			blockSizeSlider.valueProperty().addListener(e -> {
				if (!blockSizeIsChanging) {
					applyMovingAvg(blockSizeSlider);
				}

			});
			
			// this allows the change to occur either from the slider or the text field
			blockSizeField.textProperty().addListener(e -> {
				if (!blockSizeIsChanging && blockSizeField.getText().length() > 0) {
					applyMovingAvg(blockSizeField);
				}
			});
			// update playback speed in real-time
			playbackSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
				lineChart.setPlaybackSpeed(newValue.doubleValue());
			});

			// change accel normalization status + graph when enabled/disabled
			accelNormCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {

				// if normalization was disabled
				if (!newValue) {
					resetNorm();
				}
				// if normalization was enabled
				else {
					applyBaseline();
				}

				// enable/disable Accel Normalization section
				accelNormBtn.setDisable(!newValue);
				accelNormForm.setDisable(!newValue);

			});

			Scene s = multiAxis.getScene();

			// FULL WINDOW MOUSE LISTENERS
			s.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
				// reset graph mode on right click
				if (e.getButton() == MouseButton.SECONDARY)
					setGraphMode(GraphMode.NONE);
			});

			//clear slope when clicked anywhere on scene
			// s.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			// 	if((mode != GraphNoSINCController.GraphMode.SLOPE) && (mode != GraphNoSINCController.GraphMode.AREA) && (mode != GraphNoSINCController.GraphMode.NORM) && (mode != GraphNoSINCController.GraphMode.LINEUP) && (mode != GraphNoSINCController.GraphMode.LINEUP_SINC)){
			// 		clearSlope(); 
			// 	}
			// }); 

			// FULL WINDOW KEY LISTENERS
			s.addEventFilter(KeyEvent.KEY_PRESSED, e -> {

				// if key was pressed somewhere other than in a text field
				if (!(e.getTarget() instanceof TextField)) {

					switch (e.getCode()) {

						case ESCAPE:
							setGraphMode(GraphMode.NONE);
							break;

						case SPACE:
							lineChart.togglePlayback();
							break;

						case COMMA:
							lineChart.lastFrame();
							break;

						case PERIOD:
							lineChart.nextFrame();
							break;

						case LEFT:
							lineChart.jumpBack();
							break;

						case RIGHT:
							lineChart.jumpForward();
							break;

						default:
							break;

					}

					// prevent key from triggering further events
					e.consume();

				}

			});

			// allow users to drag and drop files
			multiAxis.getScene().setOnDragOver(e -> {

				// get all files dragged into graph window
				List<File> csvs = e.getDragboard().getFiles();

				// remove all non-CSV files from the list
				csvs.removeIf(x -> !x.getName().endsWith("csv"));

				// if the drag occurs from outside
				if (e.getGestureSource() == null && csvs.size() > 0) {
					e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				}

				// prevent from triggering further events
				e.consume();

			});

			// when user drops file(s) onto window
			multiAxis.getScene().setOnDragDropped(e -> {

				// get all files dragged into graph window
				List<File> csvs = e.getDragboard().getFiles();

				// remove all non-CSV files from the list
				csvs.removeIf(x -> !x.getName().endsWith("csv"));

				// convert File -> String
				List<String> csvPaths = csvs.stream().map(x -> x.getAbsolutePath()).collect(Collectors.toList());

				// load tests into graph
				setGenericTestsFromCSV(new ArrayList<String>(csvPaths));

			});

		});

		multiAxis.redrawGraph();

	}

	/**
	 * Populates the BioForce Graph with a single GenericTest.
	 * 
	 * @param g the GenericTest representing a single trial
	 */
	public void setGenericTest(GenericTest g) {
		genericTests.add(g);
		for (AxisType axis : AxisType.values()) {
			String name = axis.getName() + (genericTests.size() <= 1 ? "" : genericTests.size());
			AxisData.nameAxisDataMap.put(name,
					new AxisData((Double[]) g.getAxis(axis).getSamples().toArray(), g, name));

		}
		initializePanels();
	}

	/**
	 * Populates the BioForce Graph with multiple GenericTests. This constructor
	 * should be used when multiple modules/trials are used in a test.
	 * 
	 * @param g array of GenericTests (each one represents one trial)
	 */
	public void setGenericTests(ArrayList<GenericTest> g) {
		genericTests = g;
		updateIntermediateAxes();
		initializePanels();
	}

	public ArrayList<DataSetPanel> getDataSetPanels(){
		return panels; 
	}

	private void updateIntermediateAxes() {
		AxisData.allAxisData.clear();
		AxisData.nameAxisDataMap.clear();
		for (int i = 0; i < genericTests.size(); i++) {
			for (AxisType axis : AxisType.values()) {
				String name = axis.getExactName() + (i == 0 ? "" : i + 1);
				AxisData.nameAxisDataMap.put(name,
						new AxisData(genericTests.get(i).getAxis(axis).getSamples(), genericTests.get(i), name));
			}
		}
	}

	/**
	 * Populates the BioForce Graph by creating a GenericTest from a CSV and CSVP
	 * file.
	 * 
	 * @param CSVPath  the location of the CSV file containing test data
	 * @param CSVPPath the location of the CSVP file containing test parameters
	 */
	public void setGenericTestFromCSV(String CSVPath) {

		// wrapper for array version of CSV reading
		setGenericTestsFromCSV(new ArrayList<String>(Arrays.asList(CSVPath)));

	}
	
	/**
	 * Populates the BioForce Graph by creating a GenericTest from a CSV and CSVP
	 * file.
	 * 
	 * @param CSVPath  the location of the CSV file containing test data
	 * @param CSVPPath the location of the CSVP file containing test parameters
	 */
	public void setGenericTestsFromCSV(ArrayList<String> paths) {

		Alert loading = new Alert(AlertType.NONE, "Loading test data...");
		loading.setResult(ButtonType.OK);
		loading.show();

		// read test data and create GenericTests
		for (String s : paths) {

			// try/catch placed inside loop to allow subsequent files to load,
			// even if loading one of them causes an error
			try {

				// get test name from the file name minus the extension
				String fileName = new File(s).getName();
				String testName = fileName.substring(0, fileName.length() - 4);

				GenericTest g = new GenericTest(CSVHandler.readCSV(s), CSVHandler.readCSVP(s + "p"));
				g.setName(testName);
				genericTests.add(g);

			} catch (IOException e) {
				logger.error("IOException while loading test data");
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("Error reading test data");
				alert.setContentText("There was a problem loading \"" + s + "/p.\".");
				alert.showAndWait();
			} catch (NumberFormatException e) {
				logger.error("Could not parse test data");
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("Invalid test data");
				alert.setContentText("Error parsing data in \"" + s + "/p\".");
				alert.showAndWait();
			}

		}
		updateIntermediateAxes();
		initializePanels();
		loading.close();

	}

	/**
	 * Internal method that generates data set panels from loaded GenericTests.
	 */
	// TODO break this up into add/remove panel methods
	private void initializePanels() {

		logger.info("Initializing data set panels...");
		// get reference to root element
		Accordion a = (Accordion) lineChart.getScene().lookup("#dataSetAccordion");
		customAxisTest = new CustomTest();
		// remove existing panels
		panels.clear();
		a.getPanes().clear();

		equationPanel = new EquationPanel(this);

		a.getPanes().add(equationPanel);
		// stop if no GTs are loaded
		if (genericTests.size() == 0) {
			logger.warn("Attempted to initialize panels with 0 GenericTests loaded");
			return;
		}

		// disable "line up trials" if only one GT exists;
		// if this is a SINC trial, don't disable "line up trials"
		if (genericTests.size() == 1 || lineChart.hasSINC()) {
			lineUpBtn.setDisable(true);
		} else {
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

				CustomTest momentumGT = new CustomTest();
				AxisDataSeries[] momentumAxes = ((ConservationMomentumModule) primaryTest).getController()
						.getMomentumAxes();
				experimentPanel.currentAxis.addListener((obs, oldVal, newVal) -> {

					// TODO part of the hack w/ change listeners
					if (newVal.intValue() == -1)
						return;

					// graph the given data set
					graphAxis(CustomAxisType.getCustomAxisByIndex(newVal.intValue()), customTests.size() - 1);

				});
				for (int i = 0; i < momentumAxes.length; i++) {
					// momentumGT.addAxisDataSeries(momentumAxes[i], new CustomAxisType(
					// "Momentum " + ((i % 4 == 3) ? "Mag" : (char) (88 + (i % 4))), "kg-m/s", 1),
					// new CheckBox());
					customTests.add(momentumGT);
				}

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

			DataSetPanel d = new DataSetPanel(genericTests.get(i).getName(), i);
			d.setController(this);

			panels.add(d);
			a.getPanes().add(d);

		}

		// create array to hold bounds for each axis class
		// outer array represents each axis class, inner represents min/max
		Double[][] axisClassRanges = new Double[AxisType.values().length / 4][2];

		// set up variables to track min/max bounds for axis class
		Double min = Double.MAX_VALUE;
		Double max = Double.MIN_VALUE;

		// loop through each axis in the primary test
		for (int i = 0; i < AxisType.values().length; i++) {

			// get current AxisDataSeries by index
			AxisDataSeries axis = primaryTest.getAxis(AxisType.valueOf(i));

			// update min/max bounds for axis class
			min = axis.dataRange[0] < min ? axis.dataRange[0] : min;
			max = axis.dataRange[1] > max ? axis.dataRange[1] : max;

			// if this is last axis in axis class
			if ((i + 1) % 4 == 0) {

				// get axis class
				int axisClassIndex = i / 4;

				// save min/max for the given axis class
				axisClassRanges[axisClassIndex][0] = min;
				axisClassRanges[axisClassIndex][1] = max;

				// reset min/max for the current axis class
				min = Double.MAX_VALUE;
				max = Double.MIN_VALUE;

			}
		}

		// re-calculate axis scalars using min/max bounds
		multiAxis.setAxisScalars(axisClassRanges);

		// graph any default axes (runs after data set panel is loaded)
		Platform.runLater(() -> {

			clearGraph();

			for (GenericTest g : genericTests) {
				for (AxisType axis : g.getDefaultAxes()) {
					graphAxis(axis, genericTests.indexOf(g));
				}
			}

			// get test length off any of the default axes, since they should all have the
			// same length
			double testLength = primaryTest.getAxis(primaryTest.getDefaultAxes()[0]).testLength;

			// set width of viewport to fit the start and end of the test
			multiAxis.resetViewport(testLength / 2, 0d, testLength, 10d);

		});

	}

	public ArrayList<GraphData> getDataSets() {
		return dataSets; 
	}

	public ArrayList<GenericTest> getGenericTests() {
		return genericTests; 
	}

	public void loadEquations() throws Exception {

		ArrayList<GraphData> customAxes = new ArrayList<GraphData>();

		for (GraphData d : dataSets) {
			if (d.axis.isCustomAxis()) {
				customAxes.add(d);
				logger.info("Ungraphing custom axis " + d.axis.getName());
			}
		}

		for (GraphData d : customAxes) {
			graphAxis(d.axis, -1);
		}
		customAxisTest.customAxes.clear();
		equationPanel.reset();
		int i = 0;
		for (CustomEquation equation : customEquations) {
			i++;
			try {
				// CustomTest test = new CustomTest();
				// logger.info(tokenizeString(equation.getEquation()));
				Token result = processTokens(tokenizeString(equation.getEquation()));

				CustomAxisType customAxis = CustomAxisType.newCustomAxis(equation.getName(), equation.getUnits(),
						Double.parseDouble(equation.getScale()));
				BFAColorMenu.getColor(customAxis);
				equationPanel.addEquation(equation, customAxis);

				if (result.type == TokenType.AXIS) {

					AxisDataSeries resultSeries = new AxisDataSeries(result.axis, customAxis);

					if (customAxis.getAxisScalar() == 0) {
						// set the bound scale to the max/min of greatest magnitude
						double bound = Math.max(resultSeries.dataRange[0], resultSeries.dataRange[1]);

						double axisScale = MultiAxisLineChart.roundBound(bound) / 5;

						customAxis.setAxisScalar(axisScale);
					}

					customAxisTest.addAxisDataSeries(resultSeries, customAxis,
							(CheckBox) equationPanel.equationCheckboxMap.get(equation), equation);
				} else {
					throw new Exception(
							"Equation for \"" + equation.getName() + "\" is invalid. It must include an Axis!");
				}

				// graphAxis(customAxis,-1);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}

		}
		logger.info("Loaded " + i + " Equations");
	}

	/**
	 * Draws/removes an axis from the graph.
	 * 
	 * @param axis    the AxisType to be drawn/removed
	 * @param GTIndex the GenericTest to read data from
	 */
	public void graphAxis(Axis axis, int GTIndex) {

		// if axis is not already graphed:
		if (findGraphData(GTIndex, axis) == null) {

			logger.info("Graphing " + axis + " for GT #" + (GTIndex + 1));

			/*
			 * Hierarchy of graph in application:
			 * 
			 * 1) LineChart - contains all data sets and graphs, does not change 2)
			 * XYChart.Series - child of LineChart; there can be multiple of these under one
			 * LineChart 3) ObservableList - numerical data set component, only one per
			 * XYChart.Series
			 */
			XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
			ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

			List<Double> time;
			List<Double> data;

			double timeOffset;

			GenericTest test = getTest(axis, GTIndex);

			//color change only affects either x/y/z, not all for one axis type 
			if (Axis.getAxis(axis.getExactName()) == null) {
				Axis.addAxis(axis.getExactName(), axis);
			} 
			//test for custom menu for each data set 
			else {
				Axis.addAxis(axis.getExactName() + "2", axis);
			}

			System.out.println();
			// get time/samples data sets
			time = test.getAxis(axis).getTime();
			data = test.getAxis(axis).getSamples();
			timeOffset = test.getAxis(axis).getTimeOffset();

			// create (Time, Data) -> (X,Y) pairs
			for (int i = 0; i < data.size(); i += multiAxis.getResolution(axis)) {

				XYChart.Data<Number, Number> dataEl;
				dataEl = new XYChart.Data<Number, Number>(time.get(i) + timeOffset,
						data.get(i) / multiAxis.getAxisScalar(axis));

				// add tooltip with (x,y) when hovering over data point
				dataEl.setNode(new DataPointLabel(time.get(i) + timeOffset, data.get(i), axis, GTIndex));

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
			if (axis.isCustomAxis()) {
				customAxisTest.getCheckBox(axis).setSelected(true);
			} else {
				panels.get(GTIndex).setCheckBox(true, (AxisType) axis);
			}

			// if axis is already graphed:
		} else {

			logger.info("Removing " + axis + " for GT #" + (GTIndex + 1));

			// remove axis from line chart
			multiAxis.removeAxis(axis, GTIndex);

			// remove GraphData from list of axes
			dataSets.remove(findGraphData(GTIndex, axis));

			// untick the checkbox
			if (axis.isCustomAxis()) {
				// customAxisTest.getCheckBox(axis).setSelected(false);
			} else {
				panels.get(GTIndex).setCheckBox(false, (AxisType) axis);
			}

		}

	}

	public void updateAxis(Axis axis, int GTIndex) {
		logger.info("Updating " + axis + " for GT #" + (GTIndex + 1));

		updateAxis(findGraphData(GTIndex, axis));
	}

	/**
	 * Redraws an axis already on the graph.
	 * 
	 * @param axis    the AxisType to be drawn/removed
	 * @param GTIndex the GenericTest to read data from
	 */
	public void updateAxis(GraphData d) {
		GenericTest test = getTest(d);

		// retrieve XYChart.Series and ObservableList from HashMap
		XYChart.Series<Number, Number> series = d.data;
		ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();
		Axis axis = d.axis;
		List<Double> data;
		List<Double> time;
		double timeOffset = 0.0;  
		double vertOffset = 0.0;
		if (d.axis.isCustomAxis()) {
			logger.info("Update Custom Axis");

			try {
				Token result = processTokens(tokenizeString(customAxisTest.axisEquationMap.get(d.axis).getEquation()));
				AxisDataSeries resultSeries = new AxisDataSeries(result.axis, d.axis);

				// set the bound scale to the max/min of greatest magnitude
				double bound = Math.max(resultSeries.dataRange[0], resultSeries.dataRange[1]);

				double axisScale = MultiAxisLineChart.roundBound(bound) / 5;

				((CustomAxisType) (d.axis)).setAxisScalar(axisScale);

				time = resultSeries.getTime();
				data = resultSeries.getSamples();

				if (mode == GraphMode.LINEUP || mode == GraphMode.LINEUP_SINC){
					timeOffset = resultSeries.getTimeOffset();
				} else if (mode == GraphMode.NORM){
					//axis data series
					timeOffset = resultSeries.getTimeOffset();
					resultSeries.setVertOffset(yShift);
					vertOffset = resultSeries.getVertOffset(); 

				} else {
					timeOffset = 0.0;
					vertOffset = 0.0; 
				}

				logger.info("Updated Custom Axis Equation");
			} catch (Exception exception) {
				logger.debug(exception.getMessage());
				time = test.getAxis(axis).getTime();
				data = test.getAxis(axis).getSamples();
			}

		} else {

			time = test.getAxis(axis).getTime();
			data = test.getAxis(axis).getSamples();
			// get time offset 
			timeOffset = test.getAxis(axis).getTimeOffset();
		}
		// clear samples in ObservableList
		seriesData.clear();

		// create (Time, Data) -> (X,Y) pairs
		for (int i = 0; i < data.size(); i += multiAxis.getResolution(axis)) {

			XYChart.Data<Number, Number> dataEl = new XYChart.Data<>(time.get(i) + timeOffset,
					(data.get(i) + vertOffset) / multiAxis.getAxisScalar(axis));

			// add tooltip with (x,y) when hovering over data point
			dataEl.setNode(new DataPointLabel(time.get(i) + timeOffset, data.get(i) + vertOffset, axis, d.GTIndex));

			seriesData.add(dataEl);
		}

		// add ObservableList to XYChart.Series
		series.setData(seriesData);

		// hide all data point symbols
		for (Node n : lineChart.lookupAll(".chart-line-symbol")) {
			n.setStyle("-fx-background-color: transparent;");
		}

		// update legend colors
		// multiAxis.styleLegend();

	}

	/**
	 * Updates all currently drawn axes on the graph. Displays a loading message
	 * displaying progress.
	 */
	public void updateGraph() {

		// run updating process in separate thread
		Task<Void> loadingTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				// loop through each currently graphed data set
				for (int i = 0; i < dataSets.size(); i++) {

					// needed for Platform.runLater
					final int index = i;

					// update progress + message for dialog box
					updateProgress(index + 1, dataSets.size());
					updateMessage("Reloading " + dataSets.get(index).toString() + "...");

					// workaround used to sync progress + message w/ updateAxis()
					// TODO this is bad practice, maybe we can refactor updateAxis() to use a Task?
					final CountDownLatch waitForThread = new CountDownLatch(1);

					Platform.runLater(() -> {

						// redraw the given axis
						updateAxis(dataSets.get(index).axis, dataSets.get(index).GTIndex);
						waitForThread.countDown();

					});

					// wait until updateAxis() is complete
					waitForThread.await();

				}

				return null;
			}

		};

		ProgressDialog loading = new ProgressDialog(loadingTask);
		loading.setHeaderText("Please wait...");
		loading.setContentText("Reloading data sets...");

		// start reloading in the background
		new Thread(loadingTask).start();
		loading.showAndWait();

	}

	/**
	 * Removes all currently drawn axes from the graph. Does NOT clear the list of
	 * data sets or GenericTests.
	 */
	public void clearGraph() {

		// looping backwards to avoid ConcurrentModificationException
		for (int i = dataSets.size() - 1; i >= 0; i--) {

			// toggling a graph that's already drawn removes it
			graphAxis(dataSets.get(i).axis, dataSets.get(i).GTIndex);
		}

	}

	/**
	 * Removes all currently drawn axes from a specific GenericTest. Does NOT clear
	 * the list of data sets.
	 */
	public void clearGraph(int GTIndex) {

		// looping backwards to avoid ConcurrentModificationException
		for (int i = dataSets.size() - 1; i >= 0; i--) {

			// only remove if on the right GenericTest
			if (dataSets.get(i).GTIndex == GTIndex) {
				// toggling a graph that's already drawn removes it
				graphAxis(dataSets.get(i).axis, dataSets.get(i).GTIndex);
			}
		}

	}

	/**
	 * Removes a GenericTest and its data set panel from the DAG.
	 * 
	 * @param GTIndex the index of the GenericTest
	 */
	public void removeGT(int GTIndex) {

		// remove all currently graphed axes
		clearGraph(GTIndex);

		// loop through all data sets
		for (int i = dataSets.size() - 1; i >= 0; i--) {

			int currentGTIndex = dataSets.get(i).GTIndex;

			// every index above the removed index needs to be shifted down;
			// e.g. removing index 4 in [0,9] means indices [5,9] become [4,8]
			if (currentGTIndex > GTIndex) {

				// update GTIndex with correct value
				dataSets.get(i).GTIndex--;

			}
		}

		// remove the GenericTest
		genericTests.remove(GTIndex);

		// redraw data set panels
		initializePanels();

	}

	/**
	 * Renames a given test.
	 * 
	 * @param name    name of GenericTest
	 * @param GTIndex the index of the GenericTest
	 */
	public void renameGT(String name, int GTIndex) {
		genericTests.get(GTIndex).setName(name);
	}

	/**
	 * Updates the colors of currently graphed lines based on BFAColorMenu.
	 */
	public void updateGraphColors(GraphData g) {
		multiAxis.updateGraphColors(g);
	}

	@FXML
	public void handleReset() {

		// update all currently drawn data sets
		for (GraphData g : dataSets) {
			if (!g.axis.isCustomAxis()) {
				// get GenericTest from data set
				GenericTest test = genericTests.get(g.GTIndex);

				// if time offset is not 0, reset it and redraw
				if (test.getTimeOffset() != 0) {
					test.resetTimeOffset();
					updateAxis(g.axis, g.GTIndex);
				}
			}

		}
		multiAxis.resetViewport();
		lineChart.resetVideo();

	}

	/**
	 * Recalculates moving averages for all currently drawn data sets. Uses the
	 * block size from the smoothing slider in the Graph.
	 */
	public void applyMovingAvg(Object source) {

		// round slider to nearest integer
		int blockSize;
		blockSizeIsChanging = true;
		if (source == blockSizeSlider) {
			blockSize = (int) blockSizeSlider.getValue();
			// update smoothing label
			blockSizeField.setText("" + blockSize);
		} else {
			blockSize = Integer.parseInt(blockSizeField.getText());

			// update smoothing label
			blockSizeSlider.setValue(blockSize);
		}
		blockSizeIsChanging = false;
		GenericTest test;

		// apply moving avgs to all currently drawn axes
		for (GraphData d : dataSets) {
			test = getTest(d);
			// if this is a magnetometer data set, divide block size by 10
			int axisBlockSize = d.axis.getUnits() == "µT" ? blockSize / 10 : blockSize;
			test.getAxis(d.axis).smoothData(axisBlockSize);
			updateAxis(d.axis, d.GTIndex);

		}

	}

	/**
	 * Resets moving averages for all currently graphed data sets to its default
	 * setting.
	 */
	@FXML
	public void resetMovingAvg() {

		// reset smoothing slider
		blockSizeSlider.setValue(AxisDataSeries.DEFAULT_BLOCK_SIZE);

		// apply moving average
		applyMovingAvg(null);

	}

	/**
	 * Applies a normalization acceleration baseline based on the bounds selected.
	 */
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
					//changed
					a.normalizeAccel(start, end, 0.0);
				}

				// recalculate Vel/Disp data sets
				g.recalcKinematics();

			}

			// update all currently drawn acceleration axes
			for (GraphData g : dataSets) {

				// if axis class is kinematic data (Accel/Vel/Disp)
				if (g.axis.getUnits() == "m/s" || g.axis.getUnits() == "m" || g.axis.getUnits() == "m/s²") {
					updateAxis(g.axis, g.GTIndex);
				}
			}

		} catch (Exception e) {

			logger.warn("Invalid baseline average inputs");
			// e.printStackTrace();

			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Invalid inputs");
			alert.setContentText("Please make sure your baseline intervals are correct.");
			alert.showAndWait();

		}

	}

	/**
	 * Resets + disables acceleration normalization.
	 */
	public void resetNorm() {

		// loop through each GenericTest
		for (GenericTest g : genericTests) {

			// loop through each acceleration data set
			for (int i = 0; i <= AxisType.AccelMag.getValue(); i++) {

				// reset normalization offset for axis
				AxisDataSeries a = g.getAxis(AxisType.valueOf(i));
				a.resetSmoothing();

			}

			// recalculate Vel/Disp data sets
			g.recalcKinematics();

		}

		// update all currently drawn acceleration axes
		for (GraphData g : dataSets) {

			// if axis class is kinematic data (Accel/Vel/Disp) or momentum
			if (g.axis.getValue() / 4 <= 2 || g.axis.getValue() / 4 <= 7) {
				updateAxis(g.axis, g.GTIndex);
			}
		}

	}

	@FXML
	public void toggleSlopeMode(ActionEvent event) {

		if (mode != GraphMode.SLOPE) {
			setGraphMode(GraphMode.SLOPE);
		} else
			setGraphMode(GraphMode.NONE);

	}

	@FXML
	public void toggleLineUpMode(ActionEvent event) {

		if (mode != GraphMode.LINEUP) {
			setGraphMode(GraphMode.LINEUP);
		} else
			setGraphMode(GraphMode.NONE);

	}

	@FXML
	public void toggleNormMode(ActionEvent event) {

		if (mode != GraphMode.NORM) {
			setGraphMode(GraphMode.NORM);
		} else
			setGraphMode(GraphMode.NONE);

	}

	@FXML
	public void toggleAreaMode(ActionEvent event) {

		if (mode != GraphMode.AREA) {
			setGraphMode(GraphMode.AREA);
		} else
			setGraphMode(GraphMode.NONE);

	}

	@FXML
	public void clearDataSets(ActionEvent event) {

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setHeaderText("Remove All Tests");
		alert.setContentText("Are you sure you want to remove all currently loaded tests?");
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {

			logger.info("Removing all tests...");

			// clear GTs, un-graph data sets, then clear the data sets list
			genericTests.clear();
			clearGraph();
			dataSets.clear();

			// redraw data set panels
			initializePanels();
		}

	}

	@FXML
	private void importVideo(ActionEvent event) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a Video");
		fileChooser.setInitialDirectory(new File(Settings.get("CSVSaveLocation")));

		// filters file selection to videos only
		//
		// TODO add support for .mov by adding method to BlackFrameAnalysis converting
		// to .mp4;
		// this will use "Files.createTempDirectory()" and use ffmpeg (Jaffree) to
		// create an .mp4.
		FileChooser.ExtensionFilter filterVideos = new FileChooser.ExtensionFilter("Select a Video File", "*.mp4",
				"*.mov");
		fileChooser.getExtensionFilters().add(filterVideos);

		File videoFile = fileChooser.showOpenDialog(null);

		// if user doesn't choose a file or closes window, don't continue
		if (videoFile == null)
			return;

		// if the selected video file does not use H.264 codec or is not an .mp4, prompt
		// the user for conversion
		if (!MediaConverter.getCodec(videoFile.getAbsolutePath()).equals("h264")
				|| !MediaConverter.getFileExt(videoFile.getAbsolutePath()).equals("mp4")) {

			Alert alert = new Alert(AlertType.CONFIRMATION);

			// TODO add a file chooser so that users can change save location
			alert.setHeaderText("Import SINC Video");
			alert.setContentText(
					"The video you have chosen needs to be converted to an .mp4 for use with SINC Technology.\n\n"
							+ "Would you like the BioForce Graph to automatically save this new video file to \""
							+ MediaConverter.convertFileExt(videoFile.getAbsolutePath()) + "\"?");

			Optional<ButtonType> result = alert.showAndWait();

			// if "yes", convert file to MP4 and replace old video file variable
			if (result.get() == ButtonType.OK) {

				// store file path to pre-converted video
				String originalVideo = videoFile.getAbsolutePath();

				// update video file object to use new converted file path
				videoFile = new File(MediaConverter.convertFileExt(videoFile.getAbsolutePath()));

				try {
					Task<Void> conversionTask = new Task<Void>() {

						@Override
						protected Void call() {

							// update progress bar when media conversion has progress update
							MediaConverter.progressProperty().addListener((obs, oldVal, newVal) -> {
								updateProgress(newVal.doubleValue(), 1);
							});

							// perform media conversion via FFmpeg
							MediaConverter.convertToMP4(originalVideo);
							return null;
						}

					};

					ProgressDialog converting = new ProgressDialog(conversionTask);

					converting.setHeaderText("Please wait...");
					converting.setContentText("Converting your video. This might take a while.");

					// begin media conversion & show progress dialog
					new Thread(conversionTask).start();
					converting.showAndWait();

				}
				// if ffmpeg encounters an error, gracefully handle it
				catch (RuntimeException e) {

					Alert error = new Alert(AlertType.ERROR);
					String errorMessage = Throwables.getRootCause(e).getMessage();

					error.setHeaderText("Error converting video");
					error.setContentText("There was an problem converting your video: \n\n" + errorMessage);
					error.showAndWait();

					logger.info("Error converting video: " + errorMessage);
					return;

				}
			}
		}

		sincControls.setVisible(true);
		lineUpBtn.setDisable(false);

		// start SINC playback
		shiftSINC();
		lineChart.playVideo(videoFile);

	}

	@FXML
	public void exitSINC() {

		// hide SINC control bar
		sincControls.setVisible(false);

		// if there are less than two tests, disable "Line up"
		if (genericTests.size() < 2) {
			lineUpBtn.setDisable(true);
		}

		// stop SINC features in line chart
		lineChart.exitSINC();

		multiAxis.redrawGraph();
	}

	// SINC PLAYBACK CONTROL HANDLERS
	@FXML
	void togglePlayback() {
		lineChart.togglePlayback();
	}

	@FXML
	void lastFrame() {
		lineChart.lastFrame();
	}

	@FXML
	void nextFrame() {
		lineChart.nextFrame();
	}

	@FXML
	void jumpBack() {
		lineChart.jumpBack();
	}

	@FXML
	void jumpForward() {
		lineChart.jumpForward();
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
		if (files == null)
			return;

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
	private void openVariableMenu(ActionEvent event) {
		Stage primaryStage = new Stage();
		Parent root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/VariableMenu.fxml"));

		try {

			root = loader.load();

			// set parent of the equaotion menu to allow communication b/t classes
			((VariableMenu) loader.getController()).setParent(this);

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		if (root != null)
			primaryStage.setScene(new Scene(root));

		// ensure that color menu stays on top and blocks everything else
		primaryStage.setAlwaysOnTop(true);
		primaryStage.initModality(Modality.APPLICATION_MODAL);
		primaryStage.initOwner(lineChart.getScene().getWindow());

		primaryStage.setTitle("Data Analysis Graph - Variable Menu");
		primaryStage.getIcons().add(icon);
		primaryStage.show();
		primaryStage.setResizable(false);
	}

	@FXML
	private void openCustomAxisMenu(ActionEvent event) {
		Stage primaryStage = new Stage();
		Parent root = null;

		// FXMLLoader loader = new
		// FXMLLoader(getClass().getResource("fxml/CustomAxisMenu.fxml"));
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/CombinedCustomMenu.fxml"));
		try {

			root = loader.load();
			((CombinedCustomMenu) loader.getController()).setParent(this);
			// set parent of the equaotion menu to allow communication b/t classes

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		if (root != null)
			primaryStage.setScene(new Scene(root));

		// ensure that color menu stays on top and blocks everything else
		primaryStage.setAlwaysOnTop(false);
		primaryStage.initModality(Modality.APPLICATION_MODAL);
		primaryStage.initOwner(lineChart.getScene().getWindow());
		primaryStage.setWidth(715);
		primaryStage.setTitle("Data Analysis Graph - Custom Axis Menu");
		primaryStage.getIcons().add(icon);
		primaryStage.show();
		primaryStage.setResizable(false);
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

		if (root != null)
			primaryStage.setScene(new Scene(root));

		// ensure that color menu stays on top and blocks everything else
		primaryStage.setAlwaysOnTop(true);
		primaryStage.initModality(Modality.APPLICATION_MODAL);
		primaryStage.initOwner(lineChart.getScene().getWindow());

		primaryStage.setTitle("BioForce Graph - Color Menu");
		primaryStage.getIcons().add(icon);
		primaryStage.show();
		primaryStage.setResizable(false);

	}

	@FXML
	private void changeResolution(ActionEvent event) {

		TextInputDialog dialog = new TextInputDialog(Integer.toString(multiAxis.getResolution(AxisType.AccelX)));

		dialog.setTitle("Change Resolution");
		dialog.setHeaderText("Change Resolution");
		dialog.setContentText("Warning: entering a value below the default (20) could severely reduce performance.");

		Optional<String> result = dialog.showAndWait();

		// if user clicks cancel, end method
		if (!result.isPresent())
			return;

		try {

			multiAxis.setResolution(Integer.parseInt(result.get()));
			if (multiAxis.getResolution(AxisType.AccelX) <= 0)
				throw new IllegalArgumentException();

		} catch (NumberFormatException e) {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Invalid input");
			alert.setContentText("Please enter a numerical value.");

			alert.showAndWait();
			return;

		} catch (IllegalArgumentException e) {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Invalid input");
			alert.setContentText("Please enter a value greater than 0.");

			alert.showAndWait();
			return;

		}

		updateGraph();

	}

	/**
	 * Exports a snapshot of the graph.
	 */
	@FXML
	public void exportGraphImage() {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Graph Image");
		fileChooser.setInitialDirectory(new File(Settings.get("CSVSaveLocation")));

		// filters file types to save only
		FileChooser.ExtensionFilter filterVideos = new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg");
		fileChooser.getExtensionFilters().add(filterVideos);

		File imgFile = fileChooser.showSaveDialog(null);

		// if user doesn't choose a save location or closes window, don't continue
		if (imgFile == null)
			return;

		// take snapshot of graph
		WritableImage img = multiAxis.snapshot(null, null);

		try {

			// save image to chosen file
			ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", imgFile);

			// open saved image in default application
			Desktop.getDesktop().open(imgFile);

		} catch (IOException e) {
			Alert a = new Alert(AlertType.ERROR, "Error saving graph image, please try again.");
			a.showAndWait();
		}

	}

	/**
	 * Sets the graphing mode of the application. Use this to change between viewing
	 * the graph and finding slope/area modes.
	 * 
	 * @param g the {@link GraphMode} to change to.
	 */
	public void setGraphMode(GraphMode g) {

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
			case LINEUP_SINC:
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
	 * Gets the enum representing the state of data analysis.
	 * 
	 * @return the enum representing the state of data analysis
	 */
	public GraphMode getGraphMode() {
		return mode;
	}

	/**
	 * Graphs a line tangent to the given point.
	 */
	public void graphSlope(double x, double y, Axis axis, int GTIndex) {
		GenericTest test = getTest(axis, GTIndex);
		clearSlope();
		double axisScalar;

		prevY.add(y); 

		if(mode == GraphMode.LINEUP || mode == GraphMode.LINEUP_SINC || mode == GraphMode.NORM){
			m = m; 
		} else{
			// get slope value "m"
			// m = test.getAxis(axis).getSlope(x, multiAxis.getResolution(axis), test.getDataOffset());
			m = test.getAxis(axis).getTangent(x, multiAxis.getResolution(axis));
		}

		slopeLine = new XYChart.Series<Number, Number>();
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

		// Formula used is point-slope form of a line:
		// y - y0 = m(x - x0) -> y = m(x - x0) + y0

		axisScalar = axis.isCustomAxis() ? axis.getAxisScalar() : multiAxis.getAxisScalar(axis);

		// plot the point (x0,y0) shared by the graph and tangent line
		seriesData.add(new XYChart.Data<Number, Number>(x, y / axisScalar));

		// plot the point one x-unit to the left (x = x0-1)
		seriesData.add(new XYChart.Data<Number, Number>(x - 1, (m * ((x - 1) - x) + y) / axisScalar));

		// plot the point one x-unit to the right (x = x0+1)
		seriesData.add(new XYChart.Data<Number, Number>(x + 1, (m * ((x + 1) - x) + y) / axisScalar));

		// add label for slope value to the center of the line, above the tangent point
		seriesData.get(0).setNode(createSlopeLabel(m));

		slopeLine.setName("Slope (" + axis + ")");
		slopeLine.setData(seriesData);

		// TODO clean up, don't need to recreate XYChart.Series
		lineChart.getData().add(slopeLine);
		slopeLine.getNode().getStyleClass().add("slope-line");

		// update legend colors
		// multiAxis.styleLegend();

		setGraphMode(GraphMode.NONE);

	}

	/**
	 * Graphs a secant line between the given points.
	 */
	public void graphSlope(double x1, double y1, double x2, double y2, Axis axis, int GTIndex) {
		GenericTest test = getTest(axis, GTIndex);
		// if user chose the same point twice, graph a tangent line
		if (x1 == x2 && y1 == y2) {
			graphSlope(x1, y1, axis, GTIndex);
			return;
		}
		
		prevY1.add(y1); 
		prevY2.add(y2); 

		clearSlope();

		if(mode == GraphMode.LINEUP || mode == GraphMode.LINEUP_SINC || mode == GraphMode.NORM){
			m = m; 
		} else{
			// get slope value "m"
			// m = test.getAxis(axis).getSlope(x1, x2, test.getDataOffset());
			m = test.getAxis(axis).getSlopeShift(x1, x2);
		}

		slopeLine = new XYChart.Series<Number, Number>();
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

		// Formula used is point-slope form of a line:
		// y - y0 = m(x - x0) -> y = m(x - x0) + y0

		double axisScalar = axis.isCustomAxis() ? axis.getAxisScalar() : multiAxis.getAxisScalar(axis);

		// plot the left endpoint of the line
		seriesData.add(new XYChart.Data<Number, Number>(x1, y1 / axisScalar));

		// plot the midpoint of the line
		seriesData.add(new XYChart.Data<Number, Number>((x1 + x2) / 2, ((y1 + y2) / 2) / axisScalar));

		// plot the right endpoint of the line
		seriesData.add(new XYChart.Data<Number, Number>(x2, y2 / axisScalar));

		// add label for slope value above the midpoint
		seriesData.get(1).setNode(createSlopeLabel(m));

		slopeLine.setName("Slope (" + axis + ")");
		slopeLine.setData(seriesData);

		// TODO clean up, don't need to recreate XYChart.Series
		lineChart.getData().add(slopeLine);
		slopeLine.getNode().getStyleClass().add("slope-line");

		// update legend colors
		// multiAxis.styleLegend();

		setGraphMode(GraphMode.NONE);

	}

	/**
	 * Clears the slope line at/between points (if currently drawn).
	 */
	public void clearSlope() {

		if (slopeLine != null) { 
			lineChart.getData().remove(slopeLine);

			// update legend colors
			// multiAxis.styleLegend();
		}

	}
	
	public void redrawSlope(double xOffset, double yOffset, Axis axis, int GTIndex){

		double newX1;
		double newX2; 
		double newY1;
		double newY2; 

		newX1 = slopeLine.getData().get(0).getXValue().doubleValue() + xOffset;  
		newX2 = slopeLine.getData().get(2).getXValue().doubleValue() + xOffset;

		if(newX1 == newX2 - 1){
			newX2 = newX2 - 1; 
			newY1 = prevY.get(prevY.size()-1) + yOffset;
			newY2 = newY1; 
		} else{
			newY1 = prevY1.get(prevY1.size() - 1) + yOffset; 
			newY2 = prevY2.get(prevY2.size() - 1) + yOffset;
		}

		graphSlope(newX1, newY1, newX2, newY2, axis, GTIndex); 
	}

	/**
	 * Shifts all GenericTests by their SINC calibration offset. This uses index 2
	 * of test parameters, <code>delayAfterStart</code>.
	 */
	private void shiftSINC() {

		// loop through each GT and shift its time axis
		for (GenericTest g : genericTests) {

			// converting milliseconds to seconds
			double delayAfterStart = ((double) g.getTestParam(2)) / 1000;

			// reset any shift in the x-axis
			g.resetTimeOffset();

			// if delayAfterStart is negative, the camera starts earlier than the module;
			// we compensate by shifting the graph by -delayAfterStart (to the right)
			if (delayAfterStart < 0) {
				//quick fix 
				g.addTimeOffset(-delayAfterStart, 0);
			}
			// if delayAfterStart is positive, the camera starts later than the module;
			// compensation is done in firmware, but we must also apply manual SINC
			// correction
			// (see BFALineChart.SINC_TIME_ERROR for more information)
			else if (delayAfterStart > 0) {
				//quick fix
				g.addTimeOffset(lineChart.SINC_TIME_ERROR, 0);
			}

			// TODO intentionally not accounting for delayAfterStart == 0;
			// more testing has to be done to see how to correct this case
		}

		// update each currently drawn axis
		for (GraphData g : dataSets) {
			updateAxis(g.axis, g.GTIndex);
			logger.info("Shifted GT #{}'s time axis by {}", g.GTIndex + 1, genericTests.get(g.GTIndex).getTimeOffset());
		}

	}

	/**
	 * Finds a GraphData object given its fields.
	 * 
	 * @param GTIndex the GenericTest associated with the GraphData
	 * @param axis    the AxisType associated with the GraphData
	 */
	private GraphData findGraphData(int GTIndex, Axis axis) {
		for (GraphData g : dataSets) {

			if (g.GTIndex == GTIndex && g.axis == axis)
				return g;
		}
		return null;

	}

	private GenericTest getTest(GraphData d) {
		return d.axis.isCustomAxis() ? customAxisTest : genericTests.get(d.GTIndex);
	}

	private GenericTest getTest(Axis axis, int GTIndex) {
		if (GTIndex == -1)
			return customAxisTest;
		return axis.isCustomAxis() ? customAxisTest : genericTests.get(GTIndex);
	}

	/**
	 * Creates the label for the slope of a tangent/secant line.
	 * 
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

	public ArrayList<Token> tokenizeString(String input) throws Exception {
		String code = input.replaceAll("\\s", "");
		ArrayList<Token> tokens = new ArrayList<Token>();
		String currentToken = "";
		String letter = "";
		boolean generatingToken = false;
		for (int i = 0; i < code.length(); i++) {
			letter = code.substring(i, i + 1);
			if (letter.equals("+") || letter.equals("-") || letter.equals("*") || letter.equals("/")
					|| letter.equals("^") || letter.equals("(") || letter.equals(")")) {
				if (generatingToken) {
					tokens.add(new Token(currentToken));
					currentToken = "";
					generatingToken = false;
				}
				tokens.add(new Token(letter));

			} else {
				currentToken += letter;
				generatingToken = true;
			}
			if (i == code.length() - 1 && currentToken != "") {
				tokens.add(new Token(currentToken));
			}
		}
		return tokens;
	}

	public static void replaceCollection(List<Token> tokens, Token token, int startPos, int endPos) {
		tokens.add(endPos + 1, token);
		for (int j = endPos; j >= startPos; j--) {
			tokens.remove(j);
		}
	}

	public static Token processTokens(List<Token> tokens) throws Exception {
		Token result;
		int currentOperationLevel = 0;
		while (tokens.size() > 1) {
			int openIndex = -1;
			int parenthesesCounter = 0;
			int closeIndex = -1;
			if (currentOperationLevel == 0) {
				for (int i = 0; i < tokens.size(); i++) {
					if (tokens.get(i).operation == Operation.OPEN_PAREN) {
						if (parenthesesCounter == 0)
							openIndex = i;
						parenthesesCounter++;
					}
					if (tokens.get(i).operation == Operation.CLOSE_PAREN)
						parenthesesCounter--;
					if (openIndex != -1 && parenthesesCounter == 0) {
						replaceCollection(tokens, processTokens(tokens.subList(openIndex + 1, i)), openIndex,
								openIndex + 2);
					}
				}
				if (openIndex == -1) {
					currentOperationLevel++;
				}
			} else {
				for (int i = 0; i < tokens.size() - 2; i++) {
					try {
						if (tokens.get(i + 1).type == TokenType.OPERATOR) {
							if (tokens.get(i + 1).isInOrder(currentOperationLevel)) {
								replaceCollection(tokens, tokens.get(i + 1).operate(tokens.get(i), tokens.get(i + 2)),
										i, i + 2);
								i--;
							}
						}
					} catch (Exception e) {
						throw e;
					}
				}
				currentOperationLevel++;
			}
		}
		return tokens.get(0);

	}

	/**
	 * JavaFX component added to data points on graph.
	 */
	class DataPointLabel extends StackPane {

		DataPointLabel(double x, double y, Axis axis, int GTIndex) {
			// GraphData graphData = gd;
			// round to the given number of sig figs
			final double roundedX = new BigDecimal(x).round(new MathContext(SIG_FIGS)).doubleValue();
			final double roundedY = new BigDecimal(y).round(new MathContext(SIG_FIGS)).doubleValue();
			setPrefSize(15, 15);
			GenericTest test = getTest(axis, GTIndex);
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
					selectedPoint = new Double[] { x, y };
					selectedGraphData = new GraphData(GTIndex, axis);

					firstClick = true;

				} else {
					firstClick = false;
				}

				if (mode == GraphMode.SLOPE) {

					// tangent line graphing mode
					if (e.isShiftDown()) {
						logger.info("Graphing tangent line...");
						graphSlope(x, y, axis, GTIndex);
						slopeFlag = true; 
					} else if (!firstClick) {

						// check for any issues with calculating b/t different data sets
						if (selectedGraphData.GTIndex != GTIndex || selectedGraphData.axis != axis) {

							Alert a = new Alert(AlertType.ERROR,
									"Slope calculations only work when selecting points from the same data set.");
							a.showAndWait();

							setGraphMode(GraphMode.NONE);
							return;

						}

						logger.info("Graphing secant line...");
						graphSlope(selectedPoint[0], selectedPoint[1], x, y, axis, GTIndex);
						slopeFlag = true; 
					}

				} else if (mode == GraphMode.AREA && !firstClick) {

					logger.info("Graphing area...");

					// check for any issues with calculating b/t different data sets
					if (selectedGraphData.GTIndex != GTIndex || selectedGraphData.axis != axis) {

						Alert a = new Alert(AlertType.ERROR,
								"Area calculations only work when selecting points from the same data set.");
						a.showAndWait();

						setGraphMode(GraphMode.NONE);
						return;

					}

					// ensures that x1 is always less than x2
					areaBounds = new double[] { selectedPoint[0], x }; 
					Arrays.sort(areaBounds);

					// calculate the definite integral with the given limits
					// changed 
					area = test.getAxis(axis).getAreaUnderShift(areaBounds[0], areaBounds[1]);
					axisScalar = multiAxis.getAxisScalar(axis);

					// p1 = (x1, y1), p2 = (x2, y2)
					p1.setXValue(selectedPoint[0]);
					p1.setYValue(selectedPoint[1] / axisScalar);
					p2.setXValue(x);
					p2.setYValue(y / axisScalar);

					// ensure the lower bound is less than the upper bound
					if (selectedPoint[0] == areaBounds[0]) {
						lineChart.graphArea(p1, p2, findGraphData(GTIndex, axis), area, SIG_FIGS);
					} else {
						lineChart.graphArea(p2, p1, findGraphData(GTIndex, axis), area, SIG_FIGS);
					}
					
					areaFlag = true; 
					setGraphMode(GraphMode.NONE);

				} else if ((mode == GraphMode.LINEUP && !firstClick) || mode == GraphMode.LINEUP_SINC) {

					double finalX;
					double initialX;

					// final = scrubber, initial = this point
					if (mode == GraphMode.LINEUP_SINC) {
						finalX = lineChart.getCurrentTime();
						initialX = x;
					}
					// final = this point, initial = previous point
					else {
						finalX = x;
						initialX = selectedPoint[0];
					}

					// shift the graph by the difference between the final and initial x-values
					genericTests.get(selectedGraphData.GTIndex).addTimeOffset(finalX - initialX, yShift); 

					for (GraphData g : dataSets) {
						if (!g.axis.isCustomAxis()) {
							updateAxis(g.axis, g.GTIndex);
							logger.info("Shifted GT #{}'s time axis by {}", g.GTIndex + 1,
									genericTests.get(g.GTIndex).getTimeOffset());
						}
					}
					for (GraphData g : dataSets) {
						if (g.axis.isCustomAxis()) {
							updateAxis(g.axis, g.GTIndex);
							logger.info("Adjusted Custom Axis {}", g.axis.getName());
						}
					}
					if (areaFlag){
						lineChart.redrawArea();
					}
					if (slopeFlag){
						double shift = finalX-initialX;
						redrawSlope(shift, 0, axis, GTIndex);
					}
					setGraphMode(GraphMode.NONE);

				} else if (mode == GraphMode.NORM) {

					AxisDataSeries a = test.getAxis(axis);
					//used to set vertOffset
					yShift += -y; 
					a.vertShift(-y); 
					// updateAxis(axis, GTIndex); 

					for (GraphData g : dataSets) {
						if (!g.axis.isCustomAxis()) {
							updateAxis(g.axis, g.GTIndex);
							logger.info("Shifted GT #{}'s time axis by {}", g.GTIndex + 1,
									genericTests.get(g.GTIndex).getTimeOffset());
						}
					}
					for (GraphData g : dataSets) {
						if (g.axis.isCustomAxis()) {
							updateAxis(g.axis, g.GTIndex);
							logger.info("Adjusted Custom Axis {}", g.axis.getName());
						}
					}

					if (areaFlag){
						double areaNorm = test.getAxis(axis).getAreaUnderShift(lineChart.getAreaBoundX1(), lineChart.getAreaBoundX2()); 
						lineChart.redrawArea(0, 0, areaNorm);
					}
					if (slopeFlag){
						double shift = -y;
						redrawSlope(0, shift, axis, GTIndex);
					}

					setGraphMode(GraphMode.NONE);

				} else if (mode == GraphMode.NONE) {
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
	 * Enum used to designate the state of data analysis;
	 * <p>
	 * <code>GraphMode.NONE</code> is when the user is zooming/panning,
	 * </p>
	 * <p>
	 * <code>GraphMode.SLOPE</code> is when the user is selecting a single point for
	 * a slope calculation,
	 * </p>
	 * <p>
	 * <code>GraphMode.AREA</code> is when the user is selecting the section for an
	 * area calculation,
	 * </p>
	 * <p>
	 * <code>GraphMode.LINEUP</code> is when the user is selecting the points to
	 * line up in two different data sets,
	 * </p>
	 * <p>
	 * <code>GraphMode.LINEUP_SINC</code> is a special case of
	 * <code>GraphMode.LINEUP</code> where the second point is used instead of the
	 * first,
	 * </p>
	 * <p>
	 * and <code>GraphMode.NORM</code> is when the user is selecting a point on a
	 * data set to normalize.
	 * </p>
	 */
	public enum GraphMode {
		NONE, SLOPE, AREA, LINEUP, LINEUP_SINC, NORM
	}

}