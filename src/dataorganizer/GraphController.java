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
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.text.Text;

/*** The Following Refers to Features That Need To Be Implemented Within the Program***/
//TODO: Add Ability to Graph Mag Data (Possibly Fixed?)
//TODO: Scale Y Based on Data


public class GraphController implements Initializable{

	@Override
	public void initialize(URL location, ResourceBundle resources){
	}





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
	private TitledPane dataDisplayTitledPane;
	@FXML
	private FlowPane dataDisplayCheckboxesFlowPane;
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
	private Text generalStatusText;


	public void setDataCollector(DataOrganizer dataCollector) {
		this.dataCollector = dataCollector;
	}



	//Program Variable Declarations

	private ObservableList<DataSeries> dataSeries = FXCollections.observableArrayList();								//Initializes the list of series
	private DataOrganizer dataCollector;
	private int xRangeLow;
	private int xRangeHigh;
	private Rectangle currentTimeInMediaPlayer;																			//Frame-By-Frame Analysis Bar
	private final Rectangle zoomRect = new Rectangle();
	int yMax = 100;
	int yMin = 0;


	/*** Event Handlers ***/

	@FXML
	public void handleZoom(ActionEvent event) {
		doZoom(zoomRect, lineChart);
	}

	@FXML
	public void handleReset(ActionEvent event) {
		final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(dataCollector.getLengthOfTest());

		zoomRect.setWidth(0);
		zoomRect.setHeight(0);
		for (final DataSeries ds : dataSeries) {
			ds.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
		}
		populateData(dataSeries, lineChart);
		styleSeries(dataSeries, lineChart);

		yAxis.setUpperBound(yMax);
		yAxis.setLowerBound(yMin);
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
			yMax = Integer.parseInt(maxYValueTextField.getText());
			yMin = Integer.parseInt(minYValueTextField.getText());

			yAxis.setUpperBound(yMax);
			yAxis.setLowerBound(yMin);

		} catch (NumberFormatException e) {
			maxYValueTextField.setText("Enter a valid number");
			minYValueTextField.setText("Enter a valid number");
		}

	}




	/*** Method for Preloading All Settings***/
	

	public void graphSettingsOnStart(){
		xAxis.setUpperBound(dataCollector.getLengthOfTest());
		xAxis.setMinorTickCount(dataCollector.getSampleRate()/16);

		lineChart.setTitle(dataCollector.getName());

		System.out.println(dataCollector.returnSignedData());
		
		for (int numDof = 1; numDof < 10; numDof++) {
			dataSeries.add(numDof - 1, new DataSeries(dataCollector, numDof));
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
				populateData(dataSeries, lineChart);
				styleSeries(dataSeries, lineChart);
			});
		}

		final BooleanBinding disableControls = zoomRect.widthProperty().lessThan(5).or(zoomRect.heightProperty().lessThan(0));
		zoomButton.disableProperty().bind(disableControls);
	}




	/*** creates the Frame-By-Frame Analysis Rectangle ***/



	private Rectangle drawRect(int x, int y, int FPS) {
		currentTimeInMediaPlayer = new Rectangle(0, 0, 1, 260);
		Node chartPlotArea = lineChart.lookup(".chart-plot-background");
		double xAxisOrigin = chartPlotArea.getLayoutX();
		x = (int) (410*x/(FPS * dataCollector.getLengthOfTest()));
		currentTimeInMediaPlayer.setX(5 + xAxisOrigin + x);			//range is XOrigin -> XOrigin + 412
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
		lineChart.getData().clear();
		for (DataSeries data : ds) {
			if (data.isActive()) {
				lineChart.getData().addAll(data.getSeries());
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

	public class DataSeries{
		private String name;
		private ObservableList<XYChart.Series<Number, Number>> series;
		private boolean isActive = true;
		private int dof;
		private String color;
		private DataOrganizer dataOrgo;
		private int dataConversionType = 1;

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
				case(10): name = "Magnitude"; color = "Black";
					break;
			}
			
			
			series = createSeries(name, dataOrgo.getZoomedSeries(0, dataOrgo.getLengthOfTest(), dof, dataConversionType));
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
			series = createSeries(name, dataOrgo.getZoomedSeries(start, end, dof, dataConversionType));
		}
	}
}
	


