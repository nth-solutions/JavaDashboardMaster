package dataorganizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/*** The Following Refers to Features That Need To Be Implemented Within the Program***/
//TODO: Add Ability to Graph Mag Data (Possibly Fixed?)
//TODO: Scale Y Based on Data


public class GraphController implements Initializable{

	@Override
	public void initialize(URL location, ResourceBundle resources){}

	//FXML Component Declarations




	@FXML
	private LineChart<Number, Number> lineChart;
	@FXML
	private NumberAxis xAxis;
	@FXML
	private NumberAxis yAxis;
	@FXML
	private Pane chartContainer;
	@FXML
	private HBox zoomControls;
	@FXML
	private Button zoomButton;
	@FXML
	private Button resetButton;
	@FXML
	private VBox dataControls;
	@FXML
	private TitledPane dataSourceTitledPane;
	@FXML
	private TitledPane dataSourceTitledPaneTwo;
	@FXML
	private TitledPane dataDisplayTitledPane;
	@FXML
	private FlowPane dataDisplayCheckboxesFlowPane;
	@FXML
	private FlowPane dataDisplayCheckboxesFlowPaneTwo;
	@FXML
	private TitledPane rawOrSignedDataDisplayTitledPane;
	@FXML
	private FlowPane rawOrSignedDataDisplayFlowPane;
	@FXML
	private CheckBox displayRawDataCheckbox;
	@FXML
	private CheckBox displaySignedDataCheckbox;
	@FXML
	private TextField maxYValueTextField;
	@FXML
	private TextField minYValueTextField;
	@FXML
	private Text generalStatusLabel;
	@FXML
	private Label dataOriginLabel; //TODO: Make this change text based on dataset it controls


	public void setDataCollector(DataOrganizer dataCollector, int index) {
		this.dataCollector[index] = dataCollector;
	}

	//Program Variable Declarations

	private ObservableList<DataSeries> dataSeries = FXCollections.observableArrayList();								//Initializes the list of series
	private ObservableList<DataSeries> dataSeriesTwo = FXCollections.observableArrayList();								//Initializes the list of series
	private DataOrganizer[] dataCollector = new DataOrganizer[2];
	private String csvFilePath;
	private int xRangeLow;
	private int xRangeHigh;
	private Rectangle currentTimeInMediaPlayer;																			//Frame-By-Frame Analysis Bar
	private final Rectangle zoomRect = new Rectangle();
	private int XOffsetCounter = 0;
	private int XOffsetCounterTwo = 0;
	private double yMax = 100;
	private double yMin = 0;
	private int numDataSets;


	/*** Event Handlers ***/

	@FXML
	public void handleZoom(ActionEvent event) {
		doZoom(zoomRect, lineChart);
	}

	@FXML
	public void handleReset(ActionEvent event) {
		yAxis.setUpperBound(yMax);
		yAxis.setLowerBound(yMin);

		zoomRect.setWidth(0);
		zoomRect.setHeight(0);
		
		if(dataSeries != null) {
			for (final DataSeries ds : dataSeries) {
				ds.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
			}	
		}
		if(dataSeriesTwo != null) {
			for (final DataSeries ds : dataSeriesTwo) {
				ds.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
			}	
		}
		repopulateData();
		restyleSeries();


	}

	@FXML
	public void handleDisplayRawData(ActionEvent event) {
		displaySignedDataCheckbox.setSelected(false);
		for (DataSeries ds: dataSeries) {
			ds.setDataConversionType(0);
			ds.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
			populateData(dataSeries, lineChart);
			styleSeries(dataSeries, lineChart);
		}
	}

	@FXML
	public void handleDisplaySignedData(ActionEvent event) {
		displayRawDataCheckbox.setSelected(false);
		for (DataSeries ds: dataSeries) {
			ds.setDataConversionType(1);
			ds.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
			populateData(dataSeries, lineChart);
			styleSeries(dataSeries, lineChart);
		}

	}

	@FXML
	public void handleSetYRange(ActionEvent event) {

		try {
			yMax = Double.parseDouble(maxYValueTextField.getText());
			yMin = Double.parseDouble(minYValueTextField.getText());

			yAxis.setUpperBound(yMax);
			yAxis.setLowerBound(yMin);

			generalStatusLabel.setText("");

		} catch (NumberFormatException e) {
			generalStatusLabel.setText("Enter a valid Y-Axis Value");
			maxYValueTextField.setText(Double.toString(yMax));
			minYValueTextField.setText(Double.toString(yMin));

			yAxis.setUpperBound(yMax);
			yAxis.setLowerBound(yMin);
		}

	}

	@FXML
	public void addTenNullButtonHandler(ActionEvent event) {
		XOffsetCounter += 10;
		for(final DataSeries ds: dataSeries) {
			ds.addNulls(XOffsetCounter);
		}
		repopulateData();
		restyleSeries();
	}

	@FXML
	public void subTenNullButtonHandler(ActionEvent event) {
		XOffsetCounter -= 10;
		for(final DataSeries ds: dataSeries) {
			ds.addNulls(XOffsetCounter);
		}
		repopulateData();
		restyleSeries();
	}

	@FXML
	public void addOneNullButtonHandler(ActionEvent event) {
		XOffsetCounter += 1;
		for(final DataSeries ds: dataSeries) {
			ds.addNulls(XOffsetCounter);
		}
		repopulateData();
		restyleSeries();
	}

	@FXML
	public void subOneNullButtonHandler(ActionEvent event) {
		XOffsetCounter -= 1;
		for(final DataSeries ds: dataSeries) {
			ds.addNulls(XOffsetCounter);
		}
		repopulateData();
		restyleSeries();
	}

	//data shift for dataset two
	@FXML
	public void addTenNullButtonHandlerTwo(ActionEvent event) {
		XOffsetCounterTwo += 10;
		for(final DataSeries ds: dataSeriesTwo) {
			ds.addNulls(XOffsetCounterTwo);
		}
		repopulateData();
		restyleSeries();
	}

	@FXML
	public void subTenNullButtonHandlerTwo(ActionEvent event) {
		XOffsetCounterTwo -= 10;
		for(final DataSeries ds: dataSeriesTwo) {
			ds.addNulls(XOffsetCounterTwo);
		}
		repopulateData();
		restyleSeries();
	}

	@FXML
	public void addOneNullButtonHandlerTwo(ActionEvent event) {
		XOffsetCounterTwo += 1;
		for(final DataSeries ds: dataSeriesTwo) {
			ds.addNulls(XOffsetCounterTwo);
		}
		repopulateData();
		restyleSeries();
	}

	@FXML
	public void subOneNullButtonHandlerTwo(ActionEvent event) {
		XOffsetCounterTwo -= 1;
		for(final DataSeries ds: dataSeriesTwo) {
			ds.addNulls(XOffsetCounterTwo);
		}
		repopulateData();
		restyleSeries();
	}

	@FXML
	public void importCSV(ActionEvent event) {
		csvFilePath = csvBrowseButtonHandler();
		if(csvFilePath != null)
			loadCSVData();
	}
	
	public void loadCSVData() {
		DataOrganizer dataOrgoObject = new DataOrganizer();
		dataOrgoObject.createDataSamplesFromCSV(csvFilePath);
		dataOrgoObject.getCSVSignedData();
		dataOrgoObject.setSourceID(new File(csvFilePath).getName(), 1);
		this.dataCollector[numDataSets] = dataOrgoObject;

		if(numDataSets == 0)
			dataSourceTitledPane.setText("CSV File: " + dataOrgoObject.getSourceId());
		else
			dataSourceTitledPaneTwo.setText("CSV File: " + dataOrgoObject.getSourceId());

		if(numDataSets == 0)
			for (int numDof = 1; numDof < 10; numDof++) {
				dataSeries.add(numDof - 1, new DataSeries(dataOrgoObject, numDof, 1));
			}
		else
			for (int numDof = 1; numDof < 10; numDof++) {
				dataSeriesTwo.add(numDof - 1, new DataSeries(dataOrgoObject, numDof, 1));
			}
		
		if(numDataSets == 0) {
			populateData(dataSeries, lineChart);
			styleSeries(dataSeries, lineChart);
		}else {
			populateData(dataSeriesTwo, lineChart);
			styleSeries(dataSeriesTwo, lineChart);
		}

		xAxis.setUpperBound(dataCollector[numDataSets].getLengthOfTest());
		xAxis.setLowerBound(0);

		zoomRect.setManaged(false);
		zoomRect.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
		chartContainer.getChildren().remove(zoomRect);
		chartContainer.getChildren().add(zoomRect);

		setUpZooming(zoomRect, lineChart);

		if(numDataSets == 0) {
			for (final DataSeries ds : dataSeries) {
				final CheckBox dataToDisplayCheckBox = new CheckBox(ds.getName());
				dataToDisplayCheckBox.setSelected(true);
				dataToDisplayCheckBox.setPadding(new Insets(5));
				// Line line = new Line(0, 10, 50, 10);

				// box.setGraphic(line);
				dataDisplayCheckboxesFlowPane.getChildren().add(dataToDisplayCheckBox);
				dataToDisplayCheckBox.setOnAction(action -> {
					ds.setActive(dataToDisplayCheckBox.isSelected());
					repopulateData();
					restyleSeries();
				});
			}
		}else {
			for (final DataSeries ds : dataSeriesTwo) {
				dataSourceTitledPaneTwo.setDisable(false);
				dataSourceTitledPaneTwo.setExpanded(true);
				final CheckBox dataToDisplayCheckBoxTwo = new CheckBox(ds.getName());
				dataToDisplayCheckBoxTwo.setSelected(true);
				dataToDisplayCheckBoxTwo.setPadding(new Insets(5));
				// Line line = new Line(0, 10, 50, 10);

				// box.setGraphic(line);
				dataDisplayCheckboxesFlowPaneTwo.getChildren().add(dataToDisplayCheckBoxTwo);
				dataToDisplayCheckBoxTwo.setOnAction(action -> {
					ds.setActive(dataToDisplayCheckBoxTwo.isSelected());
					repopulateData();
					restyleSeries();
				});
			}
		}

		final BooleanBinding disableControls = zoomRect.widthProperty().lessThan(5).or(zoomRect.heightProperty().lessThan(0));
		zoomButton.disableProperty().bind(disableControls);

		if (maxYValueTextField.getText().equals("") && minYValueTextField.getText().equals("")) {
			maxYValueTextField.setText(Double.toString(yMax));
			minYValueTextField.setText(Double.toString(yMin));
		}
		numDataSets++;
	}

	@FXML
	public void clearDataAll() {
		lineChart.getData().clear();
		dataSourceTitledPane.setText("");
		dataSourceTitledPaneTwo.setText("");
		dataDisplayCheckboxesFlowPane.getChildren().clear();
		dataDisplayCheckboxesFlowPaneTwo.getChildren().clear();
		dataSeries = FXCollections.observableArrayList(); 
		dataSeriesTwo = FXCollections.observableArrayList();
		numDataSets = 0;
	}

	@FXML
	public void clearDataSetOne() {
		lineChart.getData().removeAll(dataSeries);
		dataDisplayCheckboxesFlowPane.getChildren().removeAll();
		dataSourceTitledPane.setText("");
		dataSeries = dataSeriesTwo;
		dataSeriesTwo = FXCollections.observableArrayList();
		populateData(dataSeries, lineChart);
		styleSeries(dataSeries, lineChart);
		numDataSets--;
	}

	@FXML
	public void clearDataSetTwo() {
		lineChart.getData().removeAll(dataSeriesTwo);
		dataDisplayCheckboxesFlowPaneTwo.getChildren().removeAll();
		dataSourceTitledPaneTwo.setText("");
		numDataSets--;
	}


	/*** Method for Preloading All Settings***/

	public void graphSettingsOnStart(String moduleSerialID){
		dataSourceTitledPane.setText("Module Serial ID: " + moduleSerialID);
		xAxis.setUpperBound(dataCollector[numDataSets].getLengthOfTest());
		xAxis.setMinorTickCount(dataCollector[numDataSets].getSampleRate()/16);

		lineChart.setTitle(dataCollector[numDataSets].getName());

		for (int numDof = 1; numDof < 10; numDof++) {
			dataSeries.add(numDof - 1, new DataSeries(dataCollector[numDataSets], numDof, 0));
		}

		populateData(dataSeries, lineChart);
		styleSeries(dataSeries, lineChart);

		zoomRect.setManaged(false);
		zoomRect.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
		chartContainer.getChildren().add(zoomRect);

		setUpZooming(zoomRect, lineChart);

		for (final DataSeries ds : dataSeries) {
			final CheckBox dataToDisplayCheckBox = new CheckBox(ds.getName());
			dataToDisplayCheckBox.setSelected(true);
			dataToDisplayCheckBox.setPadding(new Insets(5));
			// Line line = new Line(0, 10, 50, 10);

			// box.setGraphic(line);
			dataDisplayCheckboxesFlowPane.getChildren().add(dataToDisplayCheckBox);
			dataToDisplayCheckBox.setOnAction(action -> {
				ds.setActive(dataToDisplayCheckBox.isSelected());
				repopulateData();
				restyleSeries();
			});
		}

		final BooleanBinding disableControls = zoomRect.widthProperty().lessThan(5).or(zoomRect.heightProperty().lessThan(0));
		zoomButton.disableProperty().bind(disableControls);

		if (maxYValueTextField.getText().equals("") && minYValueTextField.getText().equals("")) {
			maxYValueTextField.setText(Double.toString(yMax));
			minYValueTextField.setText(Double.toString(yMin));
		}

		numDataSets++;
	}

	/**
	 * Handles the button press of browse button. This is an action event which must handled before the rest of the program resumes. This method allows the user to navigate
	 * the file explorer and select a save location for the incoming data.
	 */
	public String csvBrowseButtonHandler() {
		final JFileChooser chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().toString();
		}
		return null;
	}



	/*** creates the Frame-By-Frame Analysis Rectangle ***/



	private Rectangle drawRect(int x, int y, int FPS) {
		currentTimeInMediaPlayer = new Rectangle(0, 0, 1, 260);
		Node chartPlotArea = lineChart.lookup(".chart-plot-background");
		double xAxisOrigin = chartPlotArea.getLayoutX() - 8;  //-8 to align to the x axis origin. XOrigin is slightly not aligned, reason unknown. 
		x = (int) (522*x/(FPS * dataCollector[0].getLengthOfTest())); //The index of which data set should not matter, if the tests are equal.
		currentTimeInMediaPlayer.setX(xAxisOrigin + x);			//range is XOrigin -> XOrigin + 522
		currentTimeInMediaPlayer.setY(40);
		currentTimeInMediaPlayer.setStroke(Color.RED);
		currentTimeInMediaPlayer.setStrokeWidth(1);
		return currentTimeInMediaPlayer;
	}

	public void updateCirclePos(int frameInMediaPlayer, int FPS) {
		int lastFrame = -2;
		if(frameInMediaPlayer != lastFrame){
			Platform.runLater(new Runnable() {
				@Override public void run() {
					chartContainer.getChildren().remove(currentTimeInMediaPlayer);
					chartContainer.getChildren().add(drawRect(frameInMediaPlayer, 0, FPS));
				}
			});
			lastFrame = frameInMediaPlayer;
		}
		//lineChart.getChildrenUnmodifiable().add(drawCircle(0, frameInMediaPlayer));
	}




	/*** Sets Up and Performs Zooming ***/




	private void setUpZooming(final Rectangle rect, final Node zoomingNode) {
		final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
		zoomingNode.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseAnchor.set(new Point2D(event.getX(), event.getY()));
				rect.setWidth(0);
				rect.setHeight(0);
			}
		});
		zoomingNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				double x = event.getX();
				double y = event.getY();
				rect.setX(Math.min(x, mouseAnchor.get().getX()));
				rect.setY(Math.min(y, mouseAnchor.get().getY()));
				rect.setWidth(Math.abs(x - mouseAnchor.get().getX()));
				rect.setHeight(Math.abs(y - mouseAnchor.get().getY()));
			}
		});
	}

	private void doZoom(Rectangle zoomRect, LineChart<Number, Number> chart) {
		Point2D zoomTopLeft = new Point2D(zoomRect.getX(), zoomRect.getY());
		Point2D zoomBottomRight = new Point2D(zoomRect.getX() + zoomRect.getWidth(), zoomRect.getY() + zoomRect.getHeight());
		final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
		Point2D yAxisInScene = yAxis.localToScene(0, 0);
		final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		Point2D xAxisInScene = xAxis.localToScene(0, 0);
		double xOffset = zoomTopLeft.getX() - yAxisInScene.getX() ;
		double yOffset = zoomBottomRight.getY() - xAxisInScene.getY();
		double xAxisScale = xAxis.getScale();
		double yAxisScale = yAxis.getScale();
		xAxis.setLowerBound(xAxis.getLowerBound() + xOffset / xAxisScale);
		xAxis.setUpperBound(xAxis.getLowerBound() + zoomRect.getWidth() / xAxisScale);
		yAxis.setLowerBound(yAxis.getLowerBound() + yOffset / yAxisScale);
		yAxis.setUpperBound(yAxis.getLowerBound() - zoomRect.getHeight() / yAxisScale);
		zoomRect.setWidth(0);
		zoomRect.setHeight(0);


		for (final DataSeries ds : dataSeries) {
			if(ds.isActive()) {
				ds.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
			}
		}

		xAxis.setTickUnit(xAxis.getUpperBound() - xAxis.getLowerBound() / 5);
	}




	/*** Data Handling and Functionality Components***/

	private void populateData(final ObservableList<DataSeries> ds, final LineChart<Number, Number> lineChart) {
		for (DataSeries data : ds) {
			if (data.isActive()) {
				lineChart.getData().addAll(data.getSeries());
			}
		}
	}


	private void repopulateData() {
		lineChart.getData().clear();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (DataSeries data : dataSeries) {
			if (data.isActive()) {
				lineChart.getData().addAll(data.getSeries());
			}
		}
		for (DataSeries data : dataSeriesTwo) {
			if (data.isActive()) {
				lineChart.getData().addAll(data.getSeries());
			}
		}
	}


	private void restyleSeries() {
		// force a css layout pass to ensure that subsequent lookup calls work.
		lineChart.applyCss();


		int nSeries = 0;
		for (DataSeries dof : dataSeries) {
			if (!dof.isActive()) continue;
			for (int j = 0; j < dof.getSeries().size(); j++) {
				XYChart.Series<Number, Number> series = dof.getSeries().get(j);
				Set<Node> nodes = lineChart.lookupAll(".series" + nSeries);
				for (Node n : nodes) {
					StringBuilder style = new StringBuilder();
					style.append("-fx-stroke: " +dof.getColor() + "; -fx-background-color: "+ dof.getColor() + ", white; ");

					n.setStyle(style.toString());
				}
				nSeries++;
			}
		}
		for (DataSeries dof : dataSeriesTwo) {
			if (!dof.isActive()) continue;
			for (int j = 0; j < dof.getSeries().size(); j++) {
				XYChart.Series<Number, Number> series = dof.getSeries().get(j);
				Set<Node> nodes = lineChart.lookupAll(".series" + nSeries);
				for (Node n : nodes) {
					StringBuilder style = new StringBuilder();
					style.append("-fx-stroke: " +dof.getColor() + "; -fx-background-color: "+ dof.getColor() + ", white; ");

					n.setStyle(style.toString());
				}
				nSeries++;
			}
		}
	}

	private void styleSeries(ObservableList<DataSeries> dataSeries, final LineChart<Number, Number> lineChart) {
		// force a css layout pass to ensure that subsequent lookup calls work.
		lineChart.applyCss();


		int nSeries = 0;
		for (DataSeries dof : dataSeries) {
			if (!dof.isActive()) continue;
			for (int j = 0; j < dof.getSeries().size(); j++) {
				XYChart.Series<Number, Number> series = dof.getSeries().get(j);
				Set<Node> nodes = lineChart.lookupAll(".series" + nSeries);
				for (Node n : nodes) {
					StringBuilder style = new StringBuilder();
					style.append("-fx-stroke: " +dof.getColor() + "; -fx-background-color: "+ dof.getColor() + ", white; ");

					n.setStyle(style.toString());
				}
				nSeries++;
			}
		}
	}

	private ObservableList<XYChart.Series<Number, Number>> createSeries(String name, List<List<Double>> data) {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(name);
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

		for (int j = 0; j < data.get(0).size() && j < data.get(1).size(); j++) {
			seriesData.add(new XYChart.Data<>(data.get(0).get(j), data.get(1).get(j)));

		}



		series.setData(seriesData);

		return FXCollections.observableArrayList(Collections.singleton(series));
	}


	// See Robs email
	public class DataSeries{
		private String name;
		private ObservableList<XYChart.Series<Number, Number>> series;
		private boolean isActive = true;
		private int dof;
		private String color;
		private DataOrganizer dataOrgo;
		private int dataConversionType = 1; //signed or unsigned, 
		private int source; //Int representing source type. 0 being live module data, 1 being file.
		private String dataSourceID;

		public DataSeries(String name, DataOrganizer dataOrgo) {
			this.name = name;
			this.dataOrgo = dataOrgo;
			series = createSeries(name, dataOrgo.getDataSamples());
		}

		public DataSeries(String name, DataOrganizer dataOrgo, int dof, int source) {
			this.name = name;
			this.dof = dof;
			this.dataOrgo = dataOrgo;
			this.source =  source;
			series = createSeries(name, dataOrgo.getZoomedSeries(source, 0, dataOrgo.getLengthOfTest(), dof, dataConversionType));
		}

		public DataSeries(DataOrganizer dataOrgo, int dof, int source) {
			this.dof = dof;
			this.dataOrgo = dataOrgo;

			switch(dof) {
			case(1): name = "Accel X"; color = "FireBrick";
			break;
			case(2): name = "Accel Y"; color = "DodgerBlue";
			break;
			case(3): name = "Accel Z"; color = "ForestGreen";
			break;
			case(4): name = "Gyro X"; color = "Gold";
			break;
			case(5): name = "Gyro Y"; color = "Coral";
			break;
			case(6): name = "Gyro Z"; color = "MediumBlue";
			break;
			case(7): name = "Mag X"; color = "DarkViolet";
			break;
			case(8): name = "Mag Y"; color = "DarkSlateGray";
			break;
			case(9): name = "Mag Z"; color = "SaddleBrown";
			break;
			case(10): name = "Magnitude"; color = "Black";
			break;
			}


			series = createSeries(name, dataOrgo.getZoomedSeries(source, 0, dataOrgo.getLengthOfTest(), dof, dataConversionType));
		}

		public String getName() {
			return name;
		}

		public String getColor() {
			return color;
		}

		public boolean isActive() {
			return isActive;
		}

		public void setActive(boolean isActive) {
			this.isActive = isActive;
		}

		public void setDataConversionType(int dataConversionType) {
			this.dataConversionType = dataConversionType;
		}

		public ObservableList<XYChart.Series<Number, Number>> getSeries() {
			return series;
		}

		public void updateZoom(double start, double end) {
			series = createSeries(name, dataOrgo.getZoomedSeries(source, start, end, dof, dataConversionType));
		}


		/*
		 * offsets the data in one direction or another. Add nulls on the front to move right (positive), remove data points to move left. 
		 */
		public void addNulls(int offset) {
			List<List<Double>> seriesData = new ArrayList<List<Double>>();
			List<Double> timeAxis = new ArrayList<Double>();
			List<Double> dataAxis = new ArrayList<Double>();

			timeAxis.addAll(dataOrgo.getTimeAxis()); 

			for(int i = 0; i < dataOrgo.getByConversionType(dataConversionType).get(dof).size() + offset; i++) { //Loop to "end of data + offset"
				if(offset >= i) { //if offset is still greater than the current sample (i) continue adding padding
					dataAxis.add(i, null);
					continue;
				}
				dataAxis.add(i, dataOrgo.getByConversionType(dataConversionType).get(dof).get(i - offset)); //If we have enough padding, start adding the samples
			}

			seriesData.add(timeAxis);
			seriesData.add(dataAxis);

			series = createSeries(name,seriesData); //create a series for the linechart
		}
	}
}



