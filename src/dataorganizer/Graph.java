package dataorganizer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

//import application.DynamicLineChart.Event;
//import application.DynamicLineChart.Event;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class Graph extends Application {
	private LineChart<Number, Number> lineChart;
	private ObservableList<DataSeries> dataSeries;
	private DataOrganizer dataCollector;
	
	public Graph(DataOrganizer dataCollector) {
		this.dataCollector = dataCollector;
	}
	
	@Override
	public void start(Stage stage) {
		
		//dataCollector = new DataOrganizer();		//Object for getting data; Calls from DataOrganizer class to get data
		//dataCollector.createDataSmpsCSV("C:\\Users\\Mason\\Documents\\(#1)  960-96 16G-92 2000dps-92 MAG-N 31JUL18.csv");
		//Create x and y axis for the line chart
		final NumberAxis xAxis = new NumberAxis();	
		final NumberAxis yAxis = new NumberAxis();
		//Configure the axis to show helpful information
		xAxis.setLabel("Time");
		yAxis.setLabel("Accel/Gyro");
		
		yAxis.setMinorTickVisible(true);
		yAxis.setAutoRanging(true);
		yAxis.setTickUnit(0.5);
		
		xAxis.setMinorTickVisible(true);
		xAxis.setAutoRanging(false);
		
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(dataCollector.getLengthOfTest());
		xAxis.setMinorTickCount(960 / 8);
		xAxis.setTickUnit(0.125);

		//Create line chart with the x and y axis
		lineChart = new LineChart<Number, Number>(xAxis, yAxis);

		lineChart.setAnimated(false);		//Turn off the animation so series can be toggled
		lineChart.setCreateSymbols(false);	//Turn off the data symbols

		lineChart.setTitle("Data");			//Add title to graph
		
		dataSeries = FXCollections.observableArrayList(); //Initialize list of series
		
		//dataSeries.add(0, new DataSeries("DataSet 1", dataCollector, 1));
		//dataSeries.add(1, new DataSeries("DataSet 1", dataCollector, 2));
		
		for (int numDof = 1; numDof < 7; numDof++) {		//Fill data series array with multiple elements of graphable series
			dataSeries.add(numDof - 1, new DataSeries(dataCollector, numDof));
		}


		
		populateData(dataSeries, lineChart);		//Graph the series if the checkbox corresponding to the series is active
		
		//Create the scene
		final StackPane chartContainer = new StackPane();
		chartContainer.getChildren().add(lineChart);
		//Zoom rectangle for highlighting data that will be zoomed
		final Rectangle zoomRect = new Rectangle();
		zoomRect.setManaged(false);
		zoomRect.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
		chartContainer.getChildren().add(zoomRect);

		setUpZooming(zoomRect, lineChart);

		final HBox zoomControls = new HBox(10);
		zoomControls.setPadding(new Insets(10));
		zoomControls.setAlignment(Pos.CENTER);

		final Button zoomButton = new Button("Zoom");
		final Button resetButton = new Button("Reset");
		zoomButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				doZoom(zoomRect, lineChart);
			}
		});
		resetButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
				xAxis.setLowerBound(0);
				xAxis.setUpperBound(dataCollector.getLengthOfTest());

				zoomRect.setWidth(0);
				zoomRect.setHeight(0);
			}
		});
		final BooleanBinding disableControls = zoomRect.widthProperty().lessThan(5)
				.or(zoomRect.heightProperty().lessThan(0));
		zoomButton.disableProperty().bind(disableControls);
		zoomControls.getChildren().addAll(zoomButton, resetButton);

		// create some controls which can toggle series display on and off.
		final HBox dataControls = new HBox(10);
		dataControls.setStyle("-fx-padding: 10;");
		dataControls.setAlignment(Pos.CENTER);
		final TitledPane controlPane = new TitledPane("Data Series Box", dataControls);
		controlPane.setCollapsible(true);
		for (final DataSeries ds : dataSeries) {
			final CheckBox box = new CheckBox(ds.getName());
			box.setSelected(true);
			// Line line = new Line(0, 10, 50, 10);

			// box.setGraphic(line);
			dataControls.getChildren().add(box);
			box.setOnAction(action -> {
				ds.setActive(box.isSelected());
				populateData(dataSeries, lineChart);
			});
		}

		stage.setTitle("Data");

		final BorderPane root = new BorderPane();
		root.setCenter(chartContainer);
		root.setBottom(zoomControls);
		root.setBottom(controlPane);
		final Scene scene = new Scene(root, 600, 400);
		stage.setScene(scene);
		stage.show();
	}
	

	
	private ObservableList<XYChart.Series<Number, Number>> createSeries(String name, List<List<Double>> data) {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(name);
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
		

		for (int j = 0; j < data.get(0).size(); j++) {
				seriesData.add(new XYChart.Data<>(data.get(0).get(j), data.get(1).get(j)));
		}

		series.setData(seriesData);
		
		/*Node line = series.getNode().lookup(".chart-series-line");
		String rgb = String.format("%d, %d, %d",
		        (int) (color.getRed() * 255),
		        (int) (color.getGreen() * 255),
		        (int) (color.getBlue() * 255));
		
		line.setStyle("-fx-stroke: rgba(" + rgb + ", 1.0);");*/
		return FXCollections.observableArrayList(Collections.singleton(series));
	}

	private void populateData(final ObservableList<DataSeries> ds, final LineChart<Number, Number> lineChart) {
		lineChart.getData().clear();
		for (DataSeries data : ds) {
			if (data.isActive()) {
				lineChart.getData().addAll(data.getSeries());
			}
		}
	}

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

		final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		final NumberAxis yAxis = (NumberAxis) chart.getYAxis();

		double xAxisScale = xAxis.getScale();
		
		Node chartPlotArea = chart.lookup(".chart-plot-background");
		double chartZeroX = chartPlotArea.getLayoutX();

		double xOffset = zoomRect.getX() - chartZeroX; 
		
		xAxis.setLowerBound(xAxis.getLowerBound() + (xOffset / xAxisScale));
		xAxis.setUpperBound(xAxis.getLowerBound() + (zoomRect.getWidth() / xAxisScale));
	
		zoomRect.setWidth(0);
		zoomRect.setHeight(0);
	}

	public class DataSeries {
		private String name;
		private ObservableList<XYChart.Series<Number, Number>> series;
		private boolean isActive = true;
		private int dof;
		private Color color;
		private DataOrganizer dataOrgo;

		public DataSeries(String name, DataOrganizer dataOrgo) {
			this.name = name;
			this.dataOrgo = dataOrgo;
			series = createSeries(name, dataOrgo.getDataSmps());
		}

		public DataSeries(String name, DataOrganizer dataOrgo, int dof) {
			this.name = name;
			this.dof = dof;
			this.dataOrgo = dataOrgo;
			series = createSeries(name, dataOrgo.getZoomedSeries(0, dataOrgo.getLengthOfTest(), dof));

		}

		public DataSeries(DataOrganizer dataOrgo, int dof) {
			this.dof = dof;
			this.dataOrgo = dataOrgo;
			
			switch(dof) {
				case(1): name = "Accel X"; color = Color.RED;
					break;
				case(2): name = "Accel Y"; color = Color.BLUE;
					break;
				case(3): name = "Accel Z"; color = Color.GREEN;
					break;
				case(4): name = "Gyro X"; color = Color.ORANGERED;
					break;
				case(5): name = "Gyro Y"; color = Color.DARKSLATEBLUE;
					break;
				case(6): name = "Gyro Z"; color = Color.DARKOLIVEGREEN;
					break;
				case(7): name = "Mag X"; color = Color.MEDIUMVIOLETRED;
					break;
				case(8): name = "Mag Y"; color = Color.DARKBLUE;
					break;
				case(9): name = "Mag Z"; color = Color.DARKGREEN;
					break;
			}
			
			series = createSeries(name, dataOrgo.getZoomedSeries(0, dataOrgo.getLengthOfTest(), dof));
			
		}

		public String getName() {
			return name;
		}

		private boolean isActive() {
			return isActive;
		}

		private void setActive(boolean isActive) {
			this.isActive = isActive;
		}

		public ObservableList<XYChart.Series<Number, Number>> getSeries() {
			return series;
		}

	}

	public static void main(String[] args) {
		launch(args);
	}
}
