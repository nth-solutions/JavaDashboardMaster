package dataorganizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import static java.lang.Math.round;

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
	private SplitPane graphingPane;
	@FXML
	private Button zoomButton;
	@FXML
	private TitledPane dataSourceTitledPane;
	@FXML
	private TitledPane dataSourceTitledPaneTwo;
	@FXML
	private FlowPane dataDisplayCheckboxesFlowPane;
	@FXML
	private FlowPane dataDisplayCheckboxesFlowPaneTwo;
	@FXML
	private CheckBox displayRawDataCheckbox;
	@FXML
	private CheckBox displaySignedDataCheckbox;
	@FXML
	private CheckBox displayNormalizedDataCheckbox;
	@FXML
	private CheckBox AccelMagnitudeCheckBox;
	@FXML
	private TextField maxYValueTextField;
	@FXML
	private TextField minYValueTextField;
	@FXML
	private Text generalStatusLabel;
	@FXML
	private TextField rollingBlockTextField;
	@FXML
	private TextField accelerometerXAxisOffsetTextField;
	@FXML
	private TextField accelerometerYAxisOffsetTextField;
	@FXML
	private TextField accelerometerZAxisOffsetTextField;
	@FXML
	private TextField baselineLowerBound;
	@FXML
	private TextField baselineUpperBound;
	@FXML
	private Pane backgroundPane;

	public void setDataCollector(DataOrganizer dataCollector, int index) {
		this.dataCollector[index] = dataCollector;
	}

	//Program Variable Declarations

	private ObservableList<DataSeries> dataSeries = FXCollections.observableArrayList();								//Initializes the list of series
	private ObservableList<DataSeries> dataSeriesTwo = FXCollections.observableArrayList();								//Initializes the list of series
	private ObservableList<TemplateDataSeries> dataTemplateSeries = FXCollections.observableArrayList();								//Initializes the list of series
	private ObservableList<TemplateDataSeries> dataTemplateSeriesTwo = FXCollections.observableArrayList();								//Initializes the list of series
	private DataOrganizer[] dataCollector = new DataOrganizer[2];
	private GraphDataOrganizer GDO;
	private String csvFilePath;
	private String conservationOfMomentumFilePath;
	private Rectangle currentTimeInMediaPlayer;																			//Frame-By-Frame Analysis Bar
	private final Rectangle userCreatedZoomRectangleBox = new Rectangle();
	private final Rectangle baselineRect = new Rectangle();
	private int XOffsetCounter = 0;
	private int XOffsetCounterTwo = 0;
	private double yMax = 5;
	private double yMin = -5;
	private int numDataSets;
	private DecimalFormat roundTime = new DecimalFormat("#.#");

	
	/*** Event Handlers ***/

	@FXML
	public void handleZoom(ActionEvent event) {																			//TODO
		setUpZooming(userCreatedZoomRectangleBox, lineChart);
		doZoom(userCreatedZoomRectangleBox, lineChart);
	}
	
	@FXML 
	public void handleBaselineRange(ActionEvent event) {																//TODO
		setUpBaselineRangeSelection(baselineRect, lineChart);
	}

	@FXML
	public void handleReset(ActionEvent event) {																		//Resets the Graph to its default parameters (y-Axis scale, x-Axis scale and userCreatedZoomRectangleBox is reset to (0,0))
		if(dataCollector[0]!=null) {
			xAxis.setUpperBound(dataCollector[0].getLengthOfTest());					//Sets the Graph's x-Axis maximum value to the total time of the test
		}
		if(GDO != null) xAxis.setUpperBound(GDO.getLengthOfTest());
		xAxis.setLowerBound(0);																							//Sets the Graph's x-Axis minimum value to 0 - the location of the very first data sample
		yAxis.setUpperBound(yMax);																						//Sets the Graph's y-Axis maximum value to the defined y-Axis maximum (5 by default - varies based on user entry)
		yAxis.setLowerBound(yMin);																						//Sets the Graph's y-Axis minimum value to the defined y-Axis minimum (-5 by default - varies based on user entry)

		userCreatedZoomRectangleBox.setWidth(0);																		//Sets the Width of user's drag and drop zoom rectangle back to its original width value (0)
		userCreatedZoomRectangleBox.setHeight(0);																		//Sets the Height of the user's drag and drop zoom rectangle back to its original height value (0)
		
		if(dataSeries != null) {																						//If the first data series exists (contains data) ->
			for (final DataSeries axisOfDataSeries : dataSeries) {														//Iterates through each axis of the first data series
				axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());								//Updates the boundaries of the graph for each axis of the first data series
			}	
		}
		
		if(dataSeriesTwo != null) {																						//If the second data series exists (contains data) ->
			for (final DataSeries axisOfDataSeriesTwo : dataSeriesTwo) {												//Iterates through each axis of the second data series
				axisOfDataSeriesTwo.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());							//Updates the boundaries of the graph for each axis of the second data series
			}	
		}
		
		if(dataTemplateSeries != null) {
			for (final TemplateDataSeries axisOfTemplate : dataTemplateSeries) {												//Iterates through each axis of the second data series
				axisOfTemplate.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());							//Updates the boundaries of the graph for each axis of the second data series
			}	
		}
		
		repopulateData();																									//TODO

	}

	@FXML
	public void handleDisplayRawData(ActionEvent event) {																//Event handler that updates the data series to be raw data, then repopulates it within the lineChart
		displaySignedDataCheckbox.setSelected(false);																	//Deselects the displaySignedDataCheckbox
		//displayNormalizedDataCheckbox.setSelected(false);																		//Deselects the displayRawDataCheckbox
		lineChart.getData().clear();																				    //Removes all series currently displayed on the graph to prevent multiple data series from populating the lineChart
		for (DataSeries axisOfDataSeries: dataSeries) {																	//Iterates through each axis of the first data series
			axisOfDataSeries.setDataConversionType(0);																	//Sets the data conversion type to 0 (0 is the numeral used to indicate a conversion to raw data) and applies the conversion to each axis of the first data series
			axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());									//Updates the boundaries of the graph for each axis of the first data series
		}
		populateData(dataSeries, lineChart);																			//Repopulates the lineChart with the updated data within the dataSeries object
		styleSeries(dataSeries, lineChart);																				//TODO
	}

	@FXML
	public void handleDisplaySignedData(ActionEvent event) {															//Event handler that updates the data series to be signed data, then repopulates it within the lineChart
		displayRawDataCheckbox.setSelected(false);																		//Deselects the displayRawDataCheckbox
		//displayNormalizedDataCheckbox.setSelected(false);																		//Deselects the displayRawDataCheckbox
		lineChart.getData().clear();																					//Removes all series currently displayed on the graph to prevent multiple data series from populating the lineChart
		for (DataSeries axisOfDataSeries: dataSeries) {																	//Iterates through each axis of the first data series
			axisOfDataSeries.setDataConversionType(1);																	//Sets the data conversion type to 1 (1 is the numeral used to indicate a conversion to signed data) and applies the conversion to each axis of the first data series
			axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());									//Updates the boundaries of the graph for each axis of the first data series
		}
		populateData(dataSeries, lineChart);																			//Repopulates the lineChart with the updated data within the dataSeries object
		styleSeries(dataSeries, lineChart);																				//TODO

	}
	
	@FXML
	public void handleDisplayNormalizedData(ActionEvent event) {														//Event handler that updates the data series to be signed data, then repopulates it within the lineChart
		displayRawDataCheckbox.setSelected(false);																		//Deselects the displayRawDataCheckbox
		displaySignedDataCheckbox.setSelected(false);																	//Deselects the displayRawDataCheckbox
		lineChart.getData().clear();																					//Removes all series currently displayed on the graph to prevent multiple data series from populating the lineChart
		for (DataSeries axisOfDataSeries: dataSeries) {																	//Iterates through each axis of the first data series
			axisOfDataSeries.setDataConversionType(2);																	//Sets the data conversion type to 1 (1 is the numeral used to indicate a conversion to signed data) and applies the conversion to each axis of the first data series
			axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());									//Updates the boundaries of the graph for each axis of the first data series
		}
		populateData(dataSeries, lineChart);																			//Repopulates the lineChart with the updated data within the dataSeries object
		styleSeries(dataSeries, lineChart);																				//TODO

	}

	@FXML
	public void handleSetYRange(ActionEvent event) {																	//Event handler that sets the maximum and minimum value of the y-Axis

		try {																											//Catches all non-valid user inputs such as NaNs and invalid numerals
			yMax = Double.parseDouble(maxYValueTextField.getText());													//Sets the global variable yMax to the input entered by the user - this variable then becomes the default maximum that will be used when the graph is reset using handleReset
			yMin = Double.parseDouble(minYValueTextField.getText());													//Sets the global variable yMin to the input entered by the user - this variable then becomes the default minimum that will be used when the graph is reset using handleReset

			yAxis.setUpperBound(yMax);																					//Sets the maximum y-Axis value to the value of yMax
			yAxis.setLowerBound(yMin);																					//Sets the minimum y-Axis value to the value of yMin

			generalStatusLabel.setText("");																				//Clears the error message displayed if an exception is handled

		} catch (NumberFormatException e) {																				//If a number format exception is handled ->
			generalStatusLabel.setText("Enter a valid Y-Axis Value");													//An error message is displayed on the Graphing interface
			maxYValueTextField.setText(Double.toString(yMax));															//The maxYValueTextField is reset to the last valid value held by yMax
			minYValueTextField.setText(Double.toString(yMin));															//The minYValueTextField is reset to the last valid value held by yMin

			yAxis.setUpperBound(yMax);																					//Sets the maximum y-Axis value to the last valid value of yMax
			yAxis.setLowerBound(yMin);																					//Sets the minimum y-Axis value to the last valid value of yMin
		}
	}

	@FXML
	public void addTenNullButtonHandler(ActionEvent event) {															//Event handler that shifts the data being displayed on the line chart by +10 data samples
		XOffsetCounter += 10;																							//Incrementer that increments the amount offset that has been applied to the X-Axis by +10 and stores it in the XOffsetCounter variable
		if(dataSeries != null) {
			for(DataSeries axisOfDataSeries: dataSeries) {															//Iterates through each axis of the data series
				axisOfDataSeries.addNulls(XOffsetCounter);																	//Calls the addNulls method, passing the updated xOffset variable to each axis of data
			}
		}
		
		if(dataTemplateSeries != null) {
			for(TemplateDataSeries axisOfDataSeries : dataTemplateSeries) {
				axisOfDataSeries.addNulls(XOffsetCounter);
			}
		}
		
		repopulateData();																								//TODO
	}

	@FXML
	public void subTenNullButtonHandler(ActionEvent event) {															//Event handler that shifts the data being displayed on the line chart by -10 data samples
		XOffsetCounter -= 10;																							//Incrementer that increments the amount offset that has been applied to the X-Axis by +10 and stores it in the XOffsetCounter variable
		if(dataSeries != null) {
			for(DataSeries axisOfDataSeries: dataSeries) {															//Iterates through each axis of the data series
				axisOfDataSeries.addNulls(XOffsetCounter);																	//Calls the addNulls method, passing the updated xOffset variable to each axis of data
			}
		}
		
		if(dataTemplateSeries != null) {
			for(TemplateDataSeries axisOfDataSeries : dataTemplateSeries) {
				axisOfDataSeries.addNulls(XOffsetCounter);
			}
		}
		repopulateData();																								//TODO
	}

	@FXML
	public void addOneNullButtonHandler(ActionEvent event) {															//Event handler that shifts the data being displayed on the line chart by +1 data samples
		XOffsetCounter += 1;																							//Incrementer that increments the amount offset that has been applied to the X-Axis by +10 and stores it in the XOffsetCounter variable
		if(dataSeries != null) {
			for(DataSeries axisOfDataSeries: dataSeries) {															//Iterates through each axis of the data series
				axisOfDataSeries.addNulls(XOffsetCounter);																	//Calls the addNulls method, passing the updated xOffset variable to each axis of data
			}
		}
		
		if(dataTemplateSeries != null) {
			for(TemplateDataSeries axisOfDataSeries : dataTemplateSeries) {
				axisOfDataSeries.addNulls(XOffsetCounter);
			}
		}
		repopulateData();																								//TODO																							//TODO
	}

	@FXML
	public void subOneNullButtonHandler(ActionEvent event) {															//Event handler that shifts the data being displayed on the line chart by -1 data samples
		XOffsetCounter -= 1;																							//Incrementer that increments the amount offset that has been applied to the X-Axis by +10 and stores it in the XOffsetCounter variable
		if(dataSeries != null) {
			for(DataSeries axisOfDataSeries: dataSeries) {															//Iterates through each axis of the data series
				axisOfDataSeries.addNulls(XOffsetCounter);																	//Calls the addNulls method, passing the updated xOffset variable to each axis of data
			}
		}
		
		if(dataTemplateSeries != null) {
			for(TemplateDataSeries axisOfDataSeries : dataTemplateSeries) {
				axisOfDataSeries.addNulls(XOffsetCounter);
			}
		}
		repopulateData();																									//TODO
	}

	//Data Shift for the Second Data Set.

	@FXML
	public void addTenNullButtonHandlerTwo(ActionEvent event) {
		XOffsetCounterTwo += 10;
		if(dataSeriesTwo != null) {
			for(DataSeries axisOfDataSeries: dataSeriesTwo) {
				axisOfDataSeries.addNulls(XOffsetCounterTwo);
			}
		}
		
		if(dataTemplateSeriesTwo != null) {
			for(TemplateDataSeries axisOfDataSeries : dataTemplateSeriesTwo) {
				axisOfDataSeries.addNulls(XOffsetCounterTwo);
			}
		}
		repopulateData();
	}

	@FXML
	public void subTenNullButtonHandlerTwo(ActionEvent event) {
		XOffsetCounterTwo -= 10;
		if(dataSeriesTwo != null) {
			for(DataSeries axisOfDataSeries: dataSeriesTwo) {
				axisOfDataSeries.addNulls(XOffsetCounterTwo);
			}
		}
		
		if(dataTemplateSeriesTwo != null) {
			for(TemplateDataSeries axisOfDataSeries : dataTemplateSeriesTwo) {
				axisOfDataSeries.addNulls(XOffsetCounterTwo);
			}
		}
		repopulateData();
	}

	@FXML
	public void addOneNullButtonHandlerTwo(ActionEvent event) {
		XOffsetCounterTwo += 1;
		if(dataSeriesTwo != null) {
			for(DataSeries axisOfDataSeries: dataSeriesTwo) {
				axisOfDataSeries.addNulls(XOffsetCounterTwo);
			}
		}
		
		if(dataTemplateSeriesTwo != null) {
			for(TemplateDataSeries axisOfDataSeries : dataTemplateSeriesTwo) {
				axisOfDataSeries.addNulls(XOffsetCounterTwo);
			}
		}
		repopulateData();
	}

	@FXML
	public void subOneNullButtonHandlerTwo(ActionEvent event) {
		XOffsetCounterTwo -= 1;
		if(dataSeriesTwo != null) {
			for(DataSeries axisOfDataSeries: dataSeriesTwo) {
				axisOfDataSeries.addNulls(XOffsetCounterTwo);
			}
		}
		
		if(dataTemplateSeriesTwo != null) {
			for(TemplateDataSeries axisOfDataSeries : dataTemplateSeriesTwo) {
				axisOfDataSeries.addNulls(XOffsetCounterTwo);
			}
		}
		repopulateData();
	}

	@FXML
	public void importCSV(ActionEvent event) {																										//Event handler that imports an external CSV into the graphing interface

		try {																																		//Try/Catch that catches Null Pointer Exception when no file is selected
			FileChooser fileChooser = new FileChooser();																							//Creates a FileChooser Object
			fileChooser.setTitle("Select a CSV");																									//Sets the title of the FileChooser object
			Settings settings = new Settings();
			settings.loadConfigFile();
			fileChooser.setInitialDirectory(new File(settings.getKeyVal("CSVSaveLocation")));
			FileChooser.ExtensionFilter filterCSVs  = new FileChooser.ExtensionFilter("Select a File (*.csv)", "*.csv");		//Creates a filter object that restricts the available files within the FileChooser window strictly CSV files
			fileChooser.getExtensionFilters().add(filterCSVs);																						//Adds the filter to the FileChooser
			File fileChosen = fileChooser.showOpenDialog(null);																		//Assigns the user's selected file to the fileChosen variable

			if(fileChosen == null) return;
			csvFilePath = fileChosen.toString();																									//Converts the file path assigned to the fileChosen variable to a string and assigns it to the csvFilePath variable

			if (csvFilePath != null) {																												//Checks to make sure the given file path contains a valid value
				loadCSVData();																														//Calls the loadCSV method
			}

		} catch (NullPointerException e) {e.printStackTrace();}																											//Catches the NullPointer exception
	}

	/** RESUME COMMENTING HERE**/

	@FXML
	public void magnitudeDatasetOneCheckBoxHandler(ActionEvent event) {
		if(dataCollector[0] != null) {
			if(AccelMagnitudeCheckBox.isSelected()) {
				dataSeries.add(9, new DataSeries(dataCollector[0], 10));
				dataSeries.get(9).setActive(true);
			}else {
				dataSeries.get(9).setActive(false);
			}
			
			repopulateData();
			restyleSeries();
		}else {
			generalStatusLabel.setText("Please add a data set.");
		}
	}
	
	@FXML
	public void magnitudeDatasetTwoCheckBoxHandler(ActionEvent event) {
		if(dataCollector[1] != null) {
			if(AccelMagnitudeCheckBox.isSelected()) {
				dataSeries.add(9, new DataSeries(dataCollector[1], 10));
				dataSeries.get(9).setActive(true);
			}else {
				dataSeries.get(9).setActive(false);
			}
		
			repopulateData();
			restyleSeries();
		}else {
			generalStatusLabel.setText("Please add a second data set.");
		}
	}

	@FXML
	public void graphMomentum() {
		try {																																		//Try/Catch that catches Null Pointer Exception when no file is selected
			FileChooser fileChooser = new FileChooser();																							//Creates a FileChooser Object
			fileChooser.setTitle("Select a CSV");																									//Sets the title of the FileChooser object
			Settings settings = new Settings();
			settings.loadConfigFile();
			fileChooser.setInitialDirectory(new File(settings.getKeyVal("CSVSaveLocation")));
			FileChooser.ExtensionFilter filterCSVs  = new FileChooser.ExtensionFilter("Select a File (*.xlsx)", "*.xlsx");		//Creates a filter object that restricts the available files within the FileChooser window strictly CSV files
			fileChooser.getExtensionFilters().add(filterCSVs);																						//Adds the filter to the FileChooser
			File fileChosen = fileChooser.showOpenDialog(null);																		//Assigns the user's selected file to the fileChosen variable

			conservationOfMomentumFilePath = fileChosen.toString();																									//Converts the file path assigned to the fileChosen variable to a string and assigns it to the csvFilePath variable

			if (conservationOfMomentumFilePath != null) {																												//Checks to make sure the given file path contains a valid value
				loadConservationOfMomentumTemplate();																														//Calls the loadCSV method
			}

		} catch (NullPointerException e) {e.printStackTrace();}		
	}

	public void loadConservationOfMomentumTemplate() {
		AsposeSpreadSheetController assc = null;
		JFrame parent = new JFrame();
		Thread t = new Thread(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(parent, "Working...", "File Loading", 0);
			}
		});
		t.start();
		
		try {
			assc = new AsposeSpreadSheetController(conservationOfMomentumFilePath);
		} catch (Exception e) {
			generalStatusLabel.setText("Failed to read your template.");
			e.printStackTrace();
		}
		if(assc == null) return;
		
		GDO = new GraphDataOrganizer();
		GDO.setTestParams(assc.getTestParameters());
		GDO.setSamples(assc.getMomentumSamplesModuleOne());
		for (int numDof = 0; numDof < 3; numDof++) {
			dataTemplateSeries.add(new TemplateDataSeries("Module One: ", GDO, numDof));
		}
		
		GDO = new GraphDataOrganizer();
		GDO.setTestParams(assc.getTestParameters());
		GDO.setSamples(assc.getMomentumSamplesModuleTwo());
		for (int numDof = 0; numDof < 3; numDof++) {
			dataTemplateSeriesTwo.add(new TemplateDataSeries("Module Two: ", GDO, numDof));
		}
		
		for (TemplateDataSeries axisOfDataSeries : dataTemplateSeries) {
			final CheckBox dataToDisplayCheckBox = new CheckBox(axisOfDataSeries.getName());
			dataToDisplayCheckBox.setSelected(false);
			if(axisOfDataSeries.index == 0) dataToDisplayCheckBox.setSelected(true);
			dataToDisplayCheckBox.setPadding(new Insets(5));
			// Line line = new Line(0, 10, 50, 10);

			// box.setGraphic(line);
			dataDisplayCheckboxesFlowPane.getChildren().add(dataToDisplayCheckBox);
			dataToDisplayCheckBox.setOnAction(action -> {
				axisOfDataSeries.setActive(dataToDisplayCheckBox.isSelected());
				repopulateData();
			});
		}
		
		for (TemplateDataSeries axisOfDataSeries : dataTemplateSeriesTwo) {
			final CheckBox dataToDisplayCheckBox = new CheckBox(axisOfDataSeries.getName());
			dataToDisplayCheckBox.setSelected(false);
			if(axisOfDataSeries.index == 0) dataToDisplayCheckBox.setSelected(true);
			dataToDisplayCheckBox.setPadding(new Insets(5));
			// Line line = new Line(0, 10, 50, 10);

			// box.setGraphic(line);
			dataDisplayCheckboxesFlowPaneTwo.getChildren().add(dataToDisplayCheckBox);
			dataToDisplayCheckBox.setOnAction(action -> {
				axisOfDataSeries.setActive(dataToDisplayCheckBox.isSelected());
				repopulateData();
			});
		}
		
		GDO.setMinMaxYAxis();
		yMin = GDO.yMin;
		yMax = GDO.yMax;
		yAxis.setUpperBound(yMax);
		yAxis.setLowerBound(yMin);
		xAxis.setUpperBound(GDO.getLengthOfTest());
		xAxis.setLowerBound(0);

		userCreatedZoomRectangleBox.setManaged(true);
		userCreatedZoomRectangleBox.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
		baselineRect.setManaged(true);
		baselineRect.setFill(Color.LIGHTGOLDENRODYELLOW.deriveColor(0, 1, 1, 0.5));
		chartContainer.getChildren().remove(userCreatedZoomRectangleBox);
		chartContainer.getChildren().add(userCreatedZoomRectangleBox);
		chartContainer.getChildren().remove(baselineRect);
		chartContainer.getChildren().add(baselineRect);

		setUpZooming(userCreatedZoomRectangleBox, lineChart);
		populateTemplateData(dataTemplateSeries, lineChart);
		populateTemplateData(dataTemplateSeriesTwo, lineChart);
		parent.dispose();
	}
	
	public void loadCSVData() {
		createListenersResize();
		DataOrganizer dataOrgoObject = new DataOrganizer();
		dataOrgoObject.createDataSamplesFromCSV(csvFilePath);
		dataOrgoObject.getSignedData();
		dataOrgoObject.setSourceID(new File(csvFilePath).getName(), 1);
		
		this.dataCollector[numDataSets] = dataOrgoObject;

		if(numDataSets == 0)
			dataSourceTitledPane.setText("CSV File: " + dataOrgoObject.getSourceId());
		else
			dataSourceTitledPaneTwo.setText("CSV File: " + dataOrgoObject.getSourceId());

		if(numDataSets == 0)
			for (int numDof = 1; numDof < 10; numDof++) {
				dataSeries.add(numDof - 1, new DataSeries(dataOrgoObject, numDof));
			}
		else
			for (int numDof = 1; numDof < 10; numDof++) {
				dataSeriesTwo.add(numDof - 1, new DataSeries(dataOrgoObject, numDof));
			}
		
		dataSeries.get(0).setActive(true);
		
		if(numDataSets == 0) {
			populateData(dataSeries, lineChart);
			styleSeries(dataSeries, lineChart);
		}else {
			populateData(dataSeriesTwo, lineChart);
			styleSeries(dataSeriesTwo, lineChart);
		}

		xAxis.setUpperBound(dataCollector[numDataSets].getLengthOfTest());
		xAxis.setLowerBound(0);

		userCreatedZoomRectangleBox.setManaged(true);
		userCreatedZoomRectangleBox.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
		baselineRect.setManaged(true);
		baselineRect.setFill(Color.LIGHTGOLDENRODYELLOW.deriveColor(0, 1, 1, 0.5));
		chartContainer.getChildren().remove(userCreatedZoomRectangleBox);
		chartContainer.getChildren().add(userCreatedZoomRectangleBox);
		chartContainer.getChildren().remove(baselineRect);
		chartContainer.getChildren().add(baselineRect);

		setUpZooming(userCreatedZoomRectangleBox, lineChart);
		
		if(numDataSets == 0) {
			for (final DataSeries axisOfDataSeries : dataSeries) {
				final CheckBox dataToDisplayCheckBox = new CheckBox(axisOfDataSeries.getName());
				dataToDisplayCheckBox.setSelected(false);
				if(axisOfDataSeries.dof == 1) dataToDisplayCheckBox.setSelected(true);
				dataToDisplayCheckBox.setPadding(new Insets(5));
				// Line line = new Line(0, 10, 50, 10);

				// box.setGraphic(line);
				dataDisplayCheckboxesFlowPane.getChildren().add(dataToDisplayCheckBox);
				dataToDisplayCheckBox.setOnAction(action -> {
					axisOfDataSeries.setActive(dataToDisplayCheckBox.isSelected());
					repopulateData();
					restyleSeries();
				});
			}
		}else {
			for (final DataSeries axisOfDataSeries : dataSeriesTwo) {
				dataSourceTitledPaneTwo.setDisable(false);
				dataSourceTitledPaneTwo.setExpanded(true);
				final CheckBox dataToDisplayCheckBoxTwo = new CheckBox(axisOfDataSeries.getName());
				dataToDisplayCheckBoxTwo.setSelected(false);
				if(axisOfDataSeries.dof == 1) dataToDisplayCheckBoxTwo.setSelected(true);
				dataToDisplayCheckBoxTwo.setPadding(new Insets(5));
				// Line line = new Line(0, 10, 50, 10);

				// box.setGraphic(line);
				dataDisplayCheckboxesFlowPaneTwo.getChildren().add(dataToDisplayCheckBoxTwo);
				dataToDisplayCheckBoxTwo.setOnAction(action -> {
					axisOfDataSeries.setActive(dataToDisplayCheckBoxTwo.isSelected());
					repopulateData();
					restyleSeries();
				});
			}
		}

		final BooleanBinding disableControls = userCreatedZoomRectangleBox.widthProperty().lessThan(5).or(userCreatedZoomRectangleBox.heightProperty().lessThan(0));
		zoomButton.disableProperty().bind(disableControls);

		if (maxYValueTextField.getText().equals("") && minYValueTextField.getText().equals("")) {
			maxYValueTextField.setText(Double.toString(yMax));
			minYValueTextField.setText(Double.toString(yMin));
		}
		numDataSets++;
	}
	
	@FXML 
	public void rollingBlockHandler(ActionEvent event) {
		int rollingBlockValue = Integer.parseInt(rollingBlockTextField.getText());
		if(rollingBlockValue == 0) return;
		for(DataSeries axisOfDataSeries: dataSeries) {
			axisOfDataSeries.rollingBlock(rollingBlockValue);
			axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
		}	
		
		for(DataSeries axisOfDataSeries: dataSeriesTwo) {
			axisOfDataSeries.rollingBlock(rollingBlockValue);
			axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
		}	

		for(TemplateDataSeries axisOfDataSeries: dataTemplateSeries) {
			axisOfDataSeries.rollingBlock(rollingBlockValue);
			axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
		}	
		
		for(TemplateDataSeries axisOfDataSeries: dataTemplateSeriesTwo) {
			axisOfDataSeries.rollingBlock(rollingBlockValue);
			axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
		}	
		
		repopulateData();
	}
	
	@FXML
	public void applyAccelerometerOffsets(ActionEvent event) {
		try {
			Double[] axisAccel = {(double) Integer.parseInt(accelerometerXAxisOffsetTextField.getText()), (double) Integer.parseInt(accelerometerYAxisOffsetTextField.getText()),  (double) Integer.parseInt(accelerometerZAxisOffsetTextField.getText())};
			
			for(int i = 0; i < 3; i++) {
				if(axisAccel[i] > 32768) {
					axisAccel[i] -= 65535;
				}
				axisAccel[i] = axisAccel[i]*dataCollector[0].accelSensitivity/32768;
				
				dataSeries.get(i).dataOrgo.getSignedData();
				dataSeries.get(i).applyCalibrationOffset(axisAccel[i]);
				dataSeries.get(i).updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
			}
			
			repopulateData();
			restyleSeries();
			
			/*This can be done better. 
			double xAxisAccelerometer = Integer.parseInt(accelerometerXAxisOffsetTextField.getText());
			double yAxisAccelerometer = Integer.parseInt(accelerometerYAxisOffsetTextField.getText());
			double zAxisAccelerometer = Integer.parseInt(accelerometerZAxisOffsetTextField.getText());
			
			if (xAxisAccelerometer > 32768) {
				xAxisAccelerometer -= 65535;
			}
			xAxisAccelerometer = (xAxisAccelerometer * dataCollector[0].accelSensitivity) / 32768;
			
			if (yAxisAccelerometer > 32768) {
				yAxisAccelerometer -= 65535;
			}
			yAxisAccelerometer = (yAxisAccelerometer * dataCollector[0].accelSensitivity) / 32768;
			
			if (zAxisAccelerometer > 32768) {
				zAxisAccelerometer -= 65535;
			}
			zAxisAccelerometer = (zAxisAccelerometer * dataCollector[0].accelSensitivity) / 32768;
			
			dataSeries.get(0).dataOrgo.getSignedData();
			dataSeries.get(1).dataOrgo.getSignedData();
			dataSeries.get(2).dataOrgo.getSignedData();
			
			dataSeries.get(0).applyCalibrationOffset(xAxisAccelerometer);
			dataSeries.get(1).applyCalibrationOffset(yAxisAccelerometer);
			dataSeries.get(2).applyCalibrationOffset(zAxisAccelerometer);
			
			dataSeries.get(0).updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
			dataSeries.get(1).updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
			dataSeries.get(2).updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
			
			repopulateData();
			restyleSeries();
			
			generalStatusLabel.setText("Normalized");*/
			

		} catch (NumberFormatException e) {

			generalStatusLabel.setText("Enter a number");

		}
	}



	@FXML
	public void clearDataAll() {
		lineChart.getData().clear();                                                                                    //Removes the data attached to the lineChart object
		dataSourceTitledPane.setText("");                                                                               //Removes the name of the file being displayed on dataSourceTitledPane
		dataSourceTitledPaneTwo.setText("");                                                                            //Removes the name of the file being displayed on dataSourceTitledPaneTwo
		dataDisplayCheckboxesFlowPane.getChildren().clear();                                                            //Removes all of the checkboxes generated when the First Data Series is imported to the Graphing Interface
		dataDisplayCheckboxesFlowPaneTwo.getChildren().clear();                                                         //Removes all of the checkboxes generated when the First Data Series is imported to the Graphing Interface
		dataSeries = FXCollections.observableArrayList();                                                               //TODO
		dataSeriesTwo = FXCollections.observableArrayList();                                                            //TODO
		numDataSets = 0;                                                                                                //Sets numDataSets to 0 to indicate that zero active data sets are currently loaded
	}

	@FXML
	public void clearDataSetOne() {
		lineChart.getData().removeAll(dataSeries);                                                                      //Removes the First Data Series from the linechart object
		dataDisplayCheckboxesFlowPane.getChildren().removeAll();                                                        //Removes all of the checkboxes generated when the First Data Series is imported to the Graphing Interface
		dataSourceTitledPane.setText("");                                                                               //Removes the name of the file being displayed on dataSourceTitledPane
		dataSeries = dataSeriesTwo;                                                                                     //TODO
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

	@SuppressWarnings("rawtypes")
	public void createListenersResize() {
		graphingPane.heightProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				double height = (double) arg2;
				lineChart.setPrefHeight(height-100);
			}
		});
		graphingPane.widthProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				double width = (double) arg2;
				lineChart.setPrefWidth(width-width/4);
			}
		});
	}
	
	
	/*** Method for Preloading All Settings***/

	public void graphSettingsOnStart(String moduleSerialID){
		createListenersResize();
		dataSourceTitledPane.setText("Module Serial ID: " + moduleSerialID);
		xAxis.setUpperBound(dataCollector[numDataSets].getLengthOfTest());
		xAxis.setMinorTickCount(dataCollector[numDataSets].getSampleRate()/16);

		lineChart.setTitle(dataCollector[numDataSets].getName());

		for (int numDof = 1; numDof < 10; numDof++) {
			dataSeries.add(numDof - 1, new DataSeries(dataCollector[numDataSets], numDof));
		}

		dataSeries.get(0).setActive(true);
		
		populateData(dataSeries, lineChart);
		styleSeries(dataSeries, lineChart);

		userCreatedZoomRectangleBox.setManaged(true);
		userCreatedZoomRectangleBox.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
		chartContainer.getChildren().add(userCreatedZoomRectangleBox);

		setUpZooming(userCreatedZoomRectangleBox, lineChart);

		for (final DataSeries axisOfDataSeries : dataSeries) {
			final CheckBox dataToDisplayCheckBox = new CheckBox(axisOfDataSeries.getName());
			dataToDisplayCheckBox.setSelected(false);
			if(axisOfDataSeries.dof == 1) dataToDisplayCheckBox.setSelected(true);
			dataToDisplayCheckBox.setPadding(new Insets(5));
			// Line line = new Line(0, 10, 50, 10);

			// box.setGraphic(line);
			dataDisplayCheckboxesFlowPane.getChildren().add(dataToDisplayCheckBox);
			dataToDisplayCheckBox.setOnAction(action -> {
				axisOfDataSeries.setActive(dataToDisplayCheckBox.isSelected());
				repopulateData();
				restyleSeries();
			});
		}
		
		final BooleanBinding disableControls = userCreatedZoomRectangleBox.widthProperty().lessThan(5).or(userCreatedZoomRectangleBox.heightProperty().lessThan(0));
		zoomButton.disableProperty().bind(disableControls);

		if (maxYValueTextField.getText().equals("") && minYValueTextField.getText().equals("")) {
			maxYValueTextField.setText(Double.toString(yMax));
			minYValueTextField.setText(Double.toString(yMin));
		}

		numDataSets++;
	}

	/*** creates the Frame-By-Frame Analysis Rectangle ***/



	private Rectangle drawRect(int x, int y, int FPS) {
		double lineChartHeight = lineChart.getHeight();
		currentTimeInMediaPlayer = new Rectangle(0, -515, 1, lineChartHeight - lineChartHeight/6);
		Node chartPlotArea = lineChart.lookup(".chart-plot-background");
		double xAxisOrigin = chartPlotArea.getLayoutX() + 4;  //+4 to align to the x axis origin. XOrigin is slightly not aligned, reason unknown. 
		double lineChartWidth = lineChart.getWidth() - 91; //Magic number 91, because the linechart doesn't know its own width.
		if(dataCollector[0] != null) {
			x = (int) (lineChartWidth*x/(FPS * dataCollector[0].getLengthOfTest())); //multiply the width of the chart by the frame number and divide by the number of frames in the first data set (The index of which data set should not matter, if the tests are equal.)
		}
		if(GDO != null) {
			x = (int) (lineChartWidth*x/(FPS * GDO.getLengthOfTest())); //multiply the width of the chart by the frame number and divide by the number of frames in the first data set (The index of which data set should not matter, if the tests are equal.)
		}
		currentTimeInMediaPlayer.setX(xAxisOrigin + x);			//range is XOrigin -> XOrigin + $length (of chart)
		currentTimeInMediaPlayer.setY(14);
		currentTimeInMediaPlayer.setStroke(Color.RED);
		currentTimeInMediaPlayer.setStrokeWidth(1);
		return currentTimeInMediaPlayer;
	}

	public void updateCirclePos(int frameInMediaPlayer, double FPS) {
		Platform.runLater(new Runnable() {
			@Override public void run() {
				chartContainer.getChildren().remove(currentTimeInMediaPlayer);
				if(xAxis.getLowerBound() != 0) return;
				chartContainer.getChildren().add(drawRect(frameInMediaPlayer, 0, (int)FPS));
			}
		});
	}



	public void setUpBaselineRangeSelection(final Rectangle rect, final Node zoomingNode) {
		final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
		zoomingNode.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseAnchor.set(new Point2D(event.getX(), event.getY()));
				rect.setWidth(0);
				rect.setHeight(0);
				zoomingNode.removeEventHandler(MouseEvent.MOUSE_PRESSED, this);
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
				zoomingNode.removeEventHandler(MouseEvent.MOUSE_DRAGGED, this);
				getBaselineRange(rect, (LineChart<Number,Number>) zoomingNode);
			}
		});
	}
	
	public void getBaselineRange(Rectangle rect, LineChart<Number, Number> chart) {
		Point2D zoomTopLeft = new Point2D(userCreatedZoomRectangleBox.getX(), userCreatedZoomRectangleBox.getY());
		Point2D zoomBottomRight = new Point2D(userCreatedZoomRectangleBox.getX() + userCreatedZoomRectangleBox.getWidth(), userCreatedZoomRectangleBox.getY() + userCreatedZoomRectangleBox.getHeight());
		final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
		Point2D yAxisInScene = yAxis.localToScene(0, 0);
		final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		Point2D xAxisInScene = xAxis.localToScene(0, 0);
		double xOffset = zoomTopLeft.getX() - yAxisInScene.getX();
		double yOffset = zoomBottomRight.getY() - xAxisInScene.getY();
		double xAxisScale = xAxis.getScale();
		double yAxisScale = yAxis.getScale();
		
		//xRangeLow = (xAxis.getLowerBound() + xOffset / xAxisScale) - MagicNumberOne;
		//xRangeHigh = Double.parseDouble(roundTime.format((xAxis.getLowerBound() + userCreatedZoomRectangleBox.getWidth() / xAxisScale) - MagicNumberTwo) );
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
				zoomingNode.removeEventHandler(MouseEvent.MOUSE_PRESSED, this);
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
				zoomingNode.removeEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			}
		});
	}

	private void doZoom(Rectangle userCreatedZoomRectangleBox, LineChart<Number, Number> chart) {
		Point2D zoomTopLeft = new Point2D(userCreatedZoomRectangleBox.getX(), userCreatedZoomRectangleBox.getY());
		Point2D zoomBottomRight = new Point2D(userCreatedZoomRectangleBox.getX() + userCreatedZoomRectangleBox.getWidth(), userCreatedZoomRectangleBox.getY() + userCreatedZoomRectangleBox.getHeight());
		final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
		Point2D yAxisInScene = yAxis.localToScene(0, 0);
		final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		Point2D xAxisInScene = xAxis.localToScene(0, 0);
		double xOffset = zoomTopLeft.getX() - yAxisInScene.getX() - 0;
		double yOffset = zoomBottomRight.getY() - xAxisInScene.getY() + 103;
		double xAxisScale = xAxis.getScale();
		double yAxisScale = yAxis.getScale();
		xAxis.setLowerBound((xAxis.getLowerBound() + xOffset / xAxisScale));
		xAxis.setUpperBound(xAxis.getLowerBound() + userCreatedZoomRectangleBox.getWidth() / xAxisScale);
		yAxis.setLowerBound((yAxis.getLowerBound() + yOffset / yAxisScale));
		yAxis.setUpperBound(yAxis.getLowerBound() - userCreatedZoomRectangleBox.getHeight() / yAxisScale);
		userCreatedZoomRectangleBox.setWidth(0);
		userCreatedZoomRectangleBox.setHeight(0);


		for (final DataSeries axisOfDataSeries : dataSeries) {
			if(axisOfDataSeries.isActive()) {
				axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
			}
		}

		xAxis.setTickUnit(1);
	}




	/*** Data Handling and Functionality Components***/

	private void populateData(final ObservableList<DataSeries> axisOfDataSeries, final LineChart<Number, Number> lineChart) {
		for (DataSeries data : axisOfDataSeries) {
			if (data.isActive()) {
				lineChart.getData().addAll(data.getSeries());
			}
		}
	}
	
	private void populateTemplateData(final ObservableList<TemplateDataSeries> axisOfDataSeries, final LineChart<Number, Number> lineChart) {
		for (TemplateDataSeries data : axisOfDataSeries) {
			if(data.isActive()) {
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
		for(TemplateDataSeries data : dataTemplateSeries) {
			if(data.isActive()) {
				lineChart.getData().addAll(data.getSeries());
			}
		}
		for(TemplateDataSeries data : dataTemplateSeriesTwo) {
			if(data.isActive()) {
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
		for (TemplateDataSeries dof : dataTemplateSeries) {
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
		for (TemplateDataSeries dof : dataTemplateSeriesTwo) {
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

	
	public class TemplateDataSeries {
		private ObservableList<XYChart.Series<Number, Number>> series;
		private GraphDataOrganizer GDO;
		private int index;
		private String name;
		private String color;
		private boolean active;
		
		public TemplateDataSeries(String origin, GraphDataOrganizer GraphDataOrganizerObj, int index) {
			this.index = index;
			GDO = GraphDataOrganizerObj;
			switch(this.index) {
			case(0):
				name = origin + "Momentum X"; active = true;
				break;
			case(1): 
				name = origin + "Momentum Y"; active = false;
				break;
			case(2): 
				name = origin + "Momentum Z";  active = false;
				break;
			}
			
			series = createSeries(name, GDO.getZoomedSeries(0, index));
		}
		
		/*
		 * offsets the data in one direction or another. Add nulls on the front to move right (positive), remove data points to move left. 
		 */
		public void addNulls(int offset) {
			List<List<Double>> seriesData = new ArrayList<List<Double>>();
			List<Double> timeAxis = new ArrayList<Double>();
			List<Double> dataAxis = new ArrayList<Double>();

			timeAxis.addAll(GDO.createTimeAxis(xAxis.getLowerBound())); 

			for(int i = 0; i < GDO.samples.get(index).size() + offset; i++) { //Loop to "end of data (int given axis) + offset"
				if(offset >= i) { //if offset is still greater than the current sample (i) continue adding padding
					dataAxis.add(0, null);
					continue;
				}
				dataAxis.add(i, GDO.samples.get(index).get(i - offset)); //If we have enough padding, start adding the samples
			}

			seriesData.add(timeAxis);
			seriesData.add(dataAxis);

			series = createSeries(name, seriesData); //create a series for the linechart
		}

		public String getColor() {
			return color;
		}
		
		public void updateZoom(double start, double end) {
			series = createSeries(name, GDO.updateZoom(start, end, index));
		}
	
		public ObservableList<XYChart.Series<Number, Number>> getSeries() {
			return series;
		}
		
		public boolean isActive() {
			return active;
		}
		
		public void setActive(boolean active) {
			this.active = active;
		}
		
		public String getName() {
			return name;
		}

		public void rollingBlock(int rollRange) {
			GDO.rollingBlock(rollRange, index);
		}
	}

	// See Robs email
	public class DataSeries{
		private String name;
		private ObservableList<XYChart.Series<Number, Number>> series;
		private boolean isActive = false;
		private int dof;
		private String color;
		private DataOrganizer dataOrgo;
		private int dataConversionType = 1; //raw, signed, normalized; 0, 1, 2
		private int appliedAccelOffset;

		public DataSeries(String name, DataOrganizer dataOrgo) {
			this.name = name;
			this.dataOrgo = dataOrgo;
			series = createSeries(name, dataOrgo.getDataSamples());
		}

		public DataSeries(String name, DataOrganizer dataOrgo, int dof) {
			this.name = name;
			this.dof = dof;
			this.dataOrgo = dataOrgo;
			series = createSeries(name, dataOrgo.getZoomedSeries(0, dataOrgo.getLengthOfTest(), dof, dataConversionType));
		}

		public DataSeries(DataOrganizer dataOrgo, int dof) {
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
			case(10): name = "Accel Magnitude"; color = "Black";
			break;
			}

			if(dof != 10)
				series = createSeries(name, dataOrgo.getZoomedSeries(0, dataOrgo.getLengthOfTest(), dof, dataConversionType));
			
			if(dof == 10)
				series = createSeries(name, dataOrgo.getMagnitudeSeries(0, dataOrgo.getLengthOfTest(), dataConversionType));
		}

		public void applyCalibrationOffset(double AccelOffset) {
			dataOrgo.applyAccelOffset(AccelOffset, dof);
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
			series = createSeries(name, dataOrgo.getZoomedSeries(start, end, this.dof, this.dataConversionType));
		}
		
		/*
		 * offsets the data in one direction or another. Add nulls on the front to move right (positive), remove data points to move left. 
		 */
		public void addNulls(int offset) {
			List<List<Double>> seriesData = new ArrayList<List<Double>>();
			List<Double> timeAxis = new ArrayList<Double>();
			List<Double> dataAxis = new ArrayList<Double>();

			timeAxis.addAll(dataOrgo.getTimeAxis()); 

			for(int i = 0; i < dataOrgo.getByConversionType(dataConversionType).get(dof).size() + offset; i++) { //Loop to "end of data (int given axis) + offset"
				if(offset >= i) { //if offset is still greater than the current sample (i) continue adding padding
					dataAxis.add(0, null);
					continue;
				}
				dataAxis.add(i, dataOrgo.getByConversionType(dataConversionType).get(dof).get(i - offset)); //If we have enough padding, start adding the samples
			}

			seriesData.add(timeAxis);
			seriesData.add(dataAxis);

			series = createSeries(name, seriesData); //create a series for the linechart
		}
		
		public void rollingBlock(int rollRange) {
			if(dof > 9 ) return;
			dataOrgo.rollingBlock(dataConversionType, rollRange, dof);
		}
	}

	/* Media Player Controls*/

	private MediaPlayer mediaPlayer;                                                                                                                            // Variable Declarations
	private String filePath;
	private double playbackRate;
	private Boolean playing = false;
	private File fileCopy;
	private Boolean videoLoaded = false;
	private double totalFrames;
	private Media media;
	private int videoFrameRate;
	private double millisPerFrame;
	private String currentFrame;

	@FXML
	private MediaView mediaView;
	@FXML
	private Button selectFileButton;
	@FXML
	CheckBox videoVisibleCheckBox;
	@FXML
	Slider playbackSlider;


	@FXML
	public void handleFileOpener(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();                                                                                                            // Creates a FileChooser Object
		Settings settings = new Settings();
		settings.loadConfigFile();
		fileChooser.setInitialDirectory(new File(settings.getKeyVal("CSVSaveLocation")));
		fileChooser.setTitle("Select a Video File");                                                                                                            // Sets the title of the file selector
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a File (*.mp4)", "*.mp4");                           // Creates a filter that limits fileChooser's search parameters to *.mp4 files
		fileChooser.getExtensionFilters().add(filter);                                                                                                          // Initializes the filter into the fileChooser object

		File file = fileChooser.showOpenDialog(null);                                                                                              // Specifies the parent component for the dialog

		fileCopy = file;                                                                                                                                        // File object necessary for use in the reset handler

		try {
			readFileFPSFromFFMpeg();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		if (file != null) {                                                                                                                                     // If the filepath contains a valid file the following code is initiated ->
			filePath = file.toURI().toString();                                                                                                                 // Sets the user's selection to a file path that will be used to select the video file to be displayed
			media = new Media(filePath);                                                                                                                        // Sets the media object to the selected file path
			mediaPlayer = new MediaPlayer(media);                                                                                                               // Creates a mediaPlayer object, mediaPlayer is utilized for video playback controls
			mediaView.setMediaPlayer(mediaPlayer);                                                                                                              // Sets the mediaPlayer to be the controller for the mediaVew object
			videoLoaded = true;                                                                                                                                 // Boolean to check if a video has been loaded
			playbackSlider.setMax(media.getDuration().toMillis());
			selectFileButton.setDisable(true);                                                                                                                  // Disables the button used to select a file following a selection

			mediaPlayer.setOnReady(new Runnable() {                                                                                                             // Sets the maximum value of the slider bar equal to the total duration of the file
				@Override
				public void run() {



					mediaPlayer.play();                                                                                                                                 // Begins video playback on the opening of the file
					currentFrame = String.valueOf((new DecimalFormat("#").format(mediaPlayer.getCurrentTime().toSeconds() * getFPS())));
					totalFrames = round(Double.parseDouble(new DecimalFormat("#.000").format(mediaPlayer.getTotalDuration().toSeconds())) * getFPS());   // Sets the totalFrames variable equal to the total number of frames in the selected file
				}
			});

			mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {                                                                      // Displays a moving slider bar that corresponds to the current point of playback within the video sequence
				@Override
				public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {

					currentFrame = String.valueOf((new DecimalFormat("#").format(mediaPlayer.getCurrentTime().toSeconds() * getFPS())));
					}
			});
		}
	}

	@FXML
	private void toggleVideoVisibility(ActionEvent event) {
		if (videoVisibleCheckBox.isSelected()) {
			mediaView.setVisible(true);
		} else {
			mediaView.setVisible(false);
		}
	}

	@FXML
	private void updateTrackerPosition(DragEvent event) {
		System.out.println("Hi");

	}


	public void readFileFPSFromFFMpeg() throws IOException {
		FfmpegSystemWrapper FfmpegSystemWrapper = new FfmpegSystemWrapper();
		FfmpegSystemWrapper.setSystemInfo();
		Process runFfmpeg = Runtime.getRuntime().exec(FfmpegSystemWrapper.getBinRoot() + "ffmpeg.exe -i \"" + fileCopy + "\"");

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runFfmpeg.getErrorStream()));


		String ffmpegOutputLine;
		while ((ffmpegOutputLine = bufferedReader.readLine()) != null) {
			if ((ffmpegOutputLine.contains("fps"))) {
				String[] ffmpegOutputarray = ffmpegOutputLine.split(",");
				for (int i = 0; i < ffmpegOutputarray.length; i ++){
					if (ffmpegOutputarray[i].contains("fps")) {
						String[] fpsCountArray = ffmpegOutputarray[i].split(" ");
						videoFrameRate = (int)Math.ceil(Double.parseDouble(fpsCountArray[1]));
						millisPerFrame = 1000/videoFrameRate;
					}
				}
			}

		}
	}

	public double getFPS() {
		return videoFrameRate;
	}


}



