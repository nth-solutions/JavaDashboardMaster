package dataorganizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

//import application.DynamicLineChart.Event;
//import application.DynamicLineChart.Event;
//import application.DynamicLineChart.Event;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class Graph {
	private LineChart<Number, Number> lineChart;
	private ObservableList<DataSeries> dataSeries;
	private DataOrganizer dataCollector;
	private int xRangeLow;
	private int xRangeHigh;
	private Pane chartContainer;
	private Rectangle currentTimeInMediaPlayer;
	
	public Graph(DataOrganizer dataCollector) {
		this.dataCollector = dataCollector;
	}
	
	public Scene startGraph(Stage stage) {
		stage.setWidth(690);
		stage.setResizable(false);
		//Create x and y axis for the line chart
		final NumberAxis xAxis = new NumberAxis();	
		final NumberAxis yAxis = new NumberAxis();
		//Configure the axis to show helpful information
		xAxis.setLabel("Time");
		yAxis.setLabel("Accel/Gyro");
		
		
		yAxis.setMinorTickVisible(true);
		yAxis.setAutoRanging(false);
		yAxis.setTickUnit(0.5);
		
		xAxis.setMinorTickVisible(true);
		xAxis.setAutoRanging(false);
		
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(dataCollector.getLengthOfTest()); 
		xAxis.setMinorTickCount(dataCollector.getSampleRate()/16);
		xAxis.setTickUnit(1);
		
		//yAxis.setLowerBound(dataCollector.minTestValAxis());		//TODO: Find min and max of only ACTIVE dataSeries
		//yAxis.setUpperBound(dataCollector.maxTestValAxis());

		//Create line chart with the x and y axis
		lineChart = new LineChart<Number, Number>(xAxis, yAxis);

		lineChart.setAnimated(false);		//Turn off the animation so series can be toggled
		lineChart.setCreateSymbols(false);	//Turn off the data symbols

		lineChart.setTitle(dataCollector.getName());			//Add title to graph
		
		dataSeries = FXCollections.observableArrayList(); //Initialize list of series
		
		//dataSeries.add(0, new DataSeries("DataSet 1", dataCollector, 1));
		//dataSeries.add(1, new DataSeries("DataSet 1", dataCollector, 2));
		
		for (int numDof = 1; numDof < 10; numDof++) {		//Fill data series array with multiple elements of graphable series
			dataSeries.add(numDof - 1, new DataSeries(dataCollector, numDof));
		}

		
		populateData(dataSeries, lineChart);		//Graph the series if the checkbox corresponding to the series is active
		styleSeries(dataSeries, lineChart);
		
		//Create the scene
		chartContainer = new Pane();
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
				for (final DataSeries ds : dataSeries) {
					ds.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
					
				}
				populateData(dataSeries, lineChart);
				styleSeries(dataSeries, lineChart);
			}
		});
		final BooleanBinding disableControls = zoomRect.widthProperty().lessThan(5)
				.or(zoomRect.heightProperty().lessThan(0));
		zoomButton.disableProperty().bind(disableControls);
		zoomControls.getChildren().addAll(zoomButton, resetButton);

		// create some controls which can toggle series display on and off.
		final VBox dataControls = new VBox(10);
		FlowPane content = new FlowPane(Orientation.VERTICAL);
		dataControls.setStyle("-fx-padding: 10;");
		dataControls.setAlignment(Pos.CENTER);
		final TitledPane dataSeriesPane = new TitledPane("Data Series Box", content);
		dataSeriesPane.setCollapsible(true);
		dataSeriesPane.setAlignment(Pos.CENTER_RIGHT);
		for (final DataSeries ds : dataSeries) {
			final CheckBox box = new CheckBox(ds.getName());
			box.setSelected(true);
			// Line line = new Line(0, 10, 50, 10);

			// box.setGraphic(line);
			content.getChildren().add(box);
			box.setOnAction(action -> {
				ds.setActive(box.isSelected());
				populateData(dataSeries, lineChart);
				styleSeries(dataSeries, lineChart);
			});
		}
	
		
		
		FlowPane content1 = new FlowPane(Orientation.VERTICAL);
		final TitledPane dataAnalysisPane = new TitledPane("Data Analysis Tools", content1);
		dataAnalysisPane.setCollapsible(true);
		dataAnalysisPane.setAlignment(Pos.CENTER_RIGHT);
		final CheckBox chckbxRawData = new CheckBox("Display Raw Data");
		final CheckBox chckbxSignedData = new CheckBox("Display Signed Data");
		content1.getChildren().add(chckbxRawData);
		chckbxRawData.setSelected(false);
		chckbxRawData.setOnAction(action ->{
			chckbxSignedData.setSelected(false);
			for(DataSeries ds: dataSeries) {
				ds.setDataConversionType(0);
				ds.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
				populateData(dataSeries, lineChart);
				styleSeries(dataSeries, lineChart);
			}
		});
		
		
		chckbxSignedData.setSelected(true);
		content1.getChildren().add(chckbxSignedData);
		chckbxSignedData.setOnAction(action ->{
			chckbxRawData.setSelected(false);
			for(DataSeries ds: dataSeries) {
				ds.setDataConversionType(1);
				ds.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
				populateData(dataSeries, lineChart);
				styleSeries(dataSeries, lineChart);
			}
		});
		
		dataControls.getChildren().addAll(dataSeriesPane, dataAnalysisPane);
		
		stage.setTitle("Graph Viewer");

		final BorderPane root = new BorderPane();
		root.setCenter(chartContainer);
		root.setBottom(zoomControls);
		root.setRight(dataControls);
		
		
		final Scene scene = new Scene(root, 600, 400);
		stage.setScene(scene);
		stage.show();
		
		
		stage.setOnHiding(new EventHandler<WindowEvent>() {
	         @Override
	         public void handle(WindowEvent event) {
	             Platform.runLater(new Runnable() {
	                 @Override
	                 public void run() {
	                     stage.hide();
	                 }
	             });
	         }
	    });
		return scene;
	}
	

	
	private ObservableList<XYChart.Series<Number, Number>> createSeries(String name, List<List<Double>> data) {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(name);
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

		for (int j = 0; j < data.get(0).size(); j++) {
				seriesData.add(new XYChart.Data<>(data.get(0).get(j), data.get(1).get(j)));
		}

		series.setData(seriesData);

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
	
	public class DataSeries {
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
	
	private void updateViewToActiveDataSeries() {
		
	}
	
}
