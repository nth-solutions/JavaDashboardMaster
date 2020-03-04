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
	
	// data samples BEFORE any rolling average is applied
	// this data will still be calibrated and normalized, NOT raw
	private Double[] originalData;
	
	// data samples AFTER a rolling average is applied
	// this is the data other classes will use
	private Double[] smoothedData;
	
	private Dictionary<String, Integer> axes;
	private AxisType axis;
	
	// TODO rename for clarity
	// flags whether graph should be drawn
	private boolean isActive;
	
	/**
	 * AxisDataSeries constructor for data NOT natively recorded by the module OR if it is from the magnetometer.
	 * @param time
	 * @param data
	 * @param axis
	 * @param signData
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, boolean signData) {
		
		this.time = (Double[]) time.toArray();
		this.originalData = (Double[]) data.toArray();
		this.axis = axis;
		
		if (signData) {
			
			for (int i = 0; i < this.originalData.length; i++) {
				
				// convert raw data to signed data
				if (this.originalData[i] > 32768) {
					this.originalData[i] -= 65535;
				}
				
			}
			
		}
		
		this.smoothedData = this.originalData;
		
	}
	
	/**
	 * AxisDataSeries constructor for acceleration data.
	 * @param time
	 * @param data
	 * @param axis
	 * @param accelOffsets
	 * @param accelSensitivity
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, int[] accelOffsets, int accelSensitivity) {
		
		this.time = (Double[]) time.toArray();
		this.originalData = (Double[]) data.toArray();
		this.axis = axis;
		
		for (int i = 0; i < this.originalData.length; i++) {
			
			// convert raw data to signed data
			if (this.originalData[i] > 32768) {
				this.originalData[i] -= 65535;
			}
			
			// apply sensitivity for accel
			if (axis.getValue() >= 1 ) {
				this.originalData[i] *= (double) accelSensitivity / 32768;
			}
			
			// subtract offsets (which are signed)
			// accel enum is 0-2 and offsets are 0-2 (X,Y,Z)
			this.originalData[i] -= accelOffsets[axis.getValue()];
			
		}
		
		this.smoothedData = this.originalData;
		
	}

	/**
	 * AxisDataSeries constructor for gyroscope data.
	 * @param time
	 * @param data
	 * @param axis
	 * @param gyroSensitivity
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, int gyroSensitivity) {
		
		this.time = (Double[]) time.toArray();
		this.originalData = (Double[]) data.toArray();
		this.axis = axis;
		
		for (int i = 0; i < this.originalData.length; i++) {
			
			// convert raw data to signed data
			if (this.originalData[i] > 32768) {
				this.originalData[i] -= 65535;
			}
			
			// apply sensitivity for gyro
			this.originalData[i] *= (double) gyroSensitivity / 32768;		
			
		}
		
		this.smoothedData = this.originalData;
		
	}

	
	// returns smoothed data using a middle-based rolling average
	public ObservableList<XYChart.Series<Number, Number>> getMovingAvg(int sampleBlockSize) {
		
		for (int i = sampleBlockSize/2; i < smoothedData.length - sampleBlockSize/2; i++) {
			double localTotal = 0;
			for (int j = (i - sampleBlockSize/2); j<(i+sampleBlockSize/2); j++) {
				localTotal+=originalData[i];
			}
			smoothedData[i]=(localTotal/sampleBlockSize);
		}
		
		return createSeries();
	}
		
	// doing trapezoidal rule for integration
	public List<Double> integrate() {
		
		Double[] result = new Double[smoothedData.length];
		
		for(int i = 1; i < smoothedData.length; i++) {
			result[i] = result[i-1] + (smoothedData[i] + smoothedData[i-1])/2 * (time[i] - time[i-1]);
		}
		
		// calculate the first integrated sample
		// this is our initial condition (our "+C")
		result[0] = result[1];

		return Arrays.asList(result);
	}
	
	public List<Double> differentiate() {
		
		Double[] result = new Double[smoothedData.length];
		
		for(int i = 1; i < smoothedData.length - 1; i++) {
			result[i] = (smoothedData[i+1]-smoothedData[i-1])/2;
		}
		
		//fill in first and last data point
		result[0] = result[1];
		result[smoothedData.length-1] = result[smoothedData.length-2];

		return Arrays.asList(result);
	}
	
	public ObservableList<XYChart.Series<Number, Number>> createSeries() {
		
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
        
        for (int i = 0; i < time.length && i < smoothedData.length; i++) {
            seriesData.add(new XYChart.Data<>(time[i], smoothedData[i]));
        }

        series.setName(axis.toString());
        series.setData(seriesData);
 
        return FXCollections.observableArrayList(Collections.singleton(series));
    }
	
	public Double[] getOriginalData() { 
		return this.originalData;
	}
	
	public void setOriginalData(Double[] data) {
		this.originalData = data;
	}
	
	public boolean getActive() {
		return isActive;
	}
	
	public void setActive(boolean b) {
		this.isActive = b;
	}
	
	public Double getSlope(Double time) {
		//TODO
		Double slope = 0d;
		return slope;
	}
	
	public Double getSlope(Double startTime, Double endTime) {
		//TODO
		Double slope = 0d;
		return slope;
	}
	
	public Double getAreaUnder(Double startTime, Double endTime) {
		//TODO
		Double area = 0d;
		return area;
	}

	public Double [] getSmoothedData() {
		return this.smoothedData;
	}
	
	public void setSmoothedData(int index, double value ) {
		this.smoothedData[index] = value;
		
	}
}
