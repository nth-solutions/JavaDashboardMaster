package dataorganizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;


public class AxisDataSeries {
	
	private Double[] time;
	private Double[] data;
	private Dictionary<String, Integer> axes;
	private AxisType axis;
	
	// TODO rename for clarity
	// flags whether graph should be drawn
	private boolean isActive;
	
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, int[] accelOffsets, int accelSensitivity, int gyroSensitivity) {
		
		this.time = (Double[]) time.toArray();
		this.data = (Double[]) data.toArray();
		this.axis = axis;
		
		for (int i = 0; i < this.data.length; i++) {
			
			// convert raw data to signed data
			if (this.data[i] > 32768) {
				this.data[i] -= 65535;
			}
			
			// apply sensitivity for accel
			if (axis.getValue() >= 1 && axis.getValue() <= 3) {
				this.data[i] *= (double) accelSensitivity / 32768;
			}
			
			// apply sensitivity for gyro
			if (axis.getValue() >= 4 && axis.getValue() <= 6) {
				this.data[i] *= (double) gyroSensitivity / 32768;
			}
			
			// only apply offsets if AxisType is acceleration
			if(axis.getValue() >= 1 && axis.getValue() <= 3) {		
				
				// subtract offsets (which are signed)
				this.data[i] -= accelOffsets[i];
				
			}
			
		}
		
	}
	
	// doing trapezoidal rule for integration
	public List<Double> integrate() {
		
		Double[] result = new Double[data.length];
		
		for(int i = 0; i < data.length - 1; i++) {
			result[i] = (data[i] + data[i+1])/2 * (time[i+1] - time[i]);
		}
		
		// fill in data for last entry, doesn't really matter
		result[result.length - 1] = result[result.length - 2]; 

		return Arrays.asList(result);
	}
	
	public List<Double> differentiate() {
		
		Double[] result = new Double[data.length];
		
		for(int i = 0; i < data.length - 1; i++) {
			// TODO we should use a rolling average for this, come back to it
		}
		
		// fill in data for last entry, doesn't really matter
		result[result.length - 1] = result[result.length - 2]; 

		return Arrays.asList(result);
	}
	
	public ObservableList<XYChart.Series<Number, Number>> createSeries() {
		
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
        
        for (int i = 0; i < time.length && i < data.length; i++) {
            seriesData.add(new XYChart.Data<>(time[i], data[i]));
        }

        series.setName(axis.toString());
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
