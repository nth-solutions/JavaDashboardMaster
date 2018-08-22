package dataorganizer;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import dataorganizer.Graph.DataSeries;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GraphController implements Initializable{
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {} //Stupid thing
	
	
	private Rectangle currentTimeInMediaPlayer;
	private Pane chartContainer;
	private LineChart lineChart;
	
	private DataOrganizer dataCollector;
	private ObservableList<DataSeries> dataSeries;
	
	
	
	
	public GraphController(DataOrganizer dataCollector) {
		this.dataCollector = dataCollector;
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
	  
	
	
}

