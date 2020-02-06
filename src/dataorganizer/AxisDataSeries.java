package dataorganizer;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;


public class AxisDataSeries {
	
	private List<Double> time;
	private List<Double> data;
	private Dictionary<String, Integer> axes;
	
	private String axis;
	
	// TODO rename for clarity
	// flags whether graph should be drawn
	private boolean isActive;
	
	public AxisDataSeries(List<Double> time, List<Double> data, String axis) {
		
		this.time = time;
		this.data = data;
		this.axis = axis;
		
	}
	
	public ObservableList<XYChart.Series<Number, Number>> createSeries() {
		
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
        
        for (int i = 0; i < time.size() && i < data.size(); i++) {
            seriesData.add(new XYChart.Data<>(time.get(i), data.get(i)));
        }

        series.setName(axis);
        series.setData(seriesData);

        return FXCollections.observableArrayList(Collections.singleton(series));
    }
	
	public List<Double> getData() { 
		return data;
	}
	
	public void setData(List<Double> data) {
		this.data = data;
	}
	
	public boolean getActive() {
		return isActive;
	}
	
	public void setActive(boolean b) {
		this.isActive = b;
	}
	
	public Double getSlope(Double time) {
		
		Double slope = 0d;
		return slope;
	}
	
	public Double getSlope(Double startTime, Double endTime) {
		Double slope = 0d;
		return slope;
	}
	
	public Double getAreaUnder(Double startTime, Double endTime) {
		Double area = 0d;
		return area;
	}

}
