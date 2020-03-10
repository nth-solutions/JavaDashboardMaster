package dataorganizer;

import java.util.ArrayList;
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
	
	//samples per second in passed in data series
	private int sampleRate;
	
	/**
	 * AxisDataSeries constructor for data NOT natively recorded by the module OR if it is from the magnetometer.
	 * @param time
	 * @param data
	 * @param axis
	 * @param signData
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, boolean signData, int sampleRate) {
		
		this.time = (Double[]) time.toArray();
		this.originalData = (Double[]) data.toArray();
		this.axis = axis;
		this.sampleRate = sampleRate;
		
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
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, int[] accelOffsets, int accelSensitivity, int sampleRate) {
		
		this.time = (Double[]) time.toArray();
		this.originalData = (Double[]) data.toArray();
		this.axis = axis;
		this.sampleRate = sampleRate;
		
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
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, int gyroSensitivity, int sampleRate) {
		
		this.time = (Double[]) time.toArray();
		this.originalData = (Double[]) data.toArray();
		this.axis = axis;
		this.sampleRate = sampleRate;
		
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
		
		if(sampleBlockSize == 0) {
			smoothedData = originalData;
			return createSeries();
		}
		
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
		
		Double[] result = new Double[originalData.length];
		
		for(int i = 1; i < originalData.length; i++) {
			result[i] = result[i-1] + (originalData[i] + originalData[i-1])/2 * (time[i] - time[i-1]);
		}
		
		// calculate the first integrated sample
		// this is our initial condition (our "+C")
		result[0] = result[1];

		return Arrays.asList(result);
	}
	
	//get slope of secant line between point directly before and after
	public List<Double> differentiate() {
		
		Double[] result = new Double[originalData.length]; 
		for(int i = 1; i < originalData.length - 1; i++) {
			result[i] = (originalData[i+1]-originalData[i-1])/(time[i+1]-time[i-1]);
		}
		
		//fill in first and last data point
		result[0] = result[1];
		result[originalData.length-1] = result[originalData.length-2];

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
	
	public Double getSlope(Double time) {
		//uses secant line method b/w sample directly before and after
		int i = (int) Math.round(time*this.sampleRate);
		Double slope = (originalData[i+1]-originalData[i-1])/(this.time[i+1]-this.time[i-1]);
		return slope;
	}
	
	
	public Double getSlope(Double startTime, Double endTime) {
		Double slope = 0.0;
		
		if (startTime == endTime) {
			int i = (int) Math.round(startTime*this.sampleRate);
			slope = (originalData[i+1]-originalData[i-1])/(this.time[i+1]-this.time[i-1]);
			return slope;
		}
		else {
		int i = (int) Math.round(startTime*this.sampleRate);
		int j = (int) Math.round(endTime*this.sampleRate);
		slope = (originalData[j]-originalData[i])/(this.time[j]-this.time[i]);}
		return slope;
	}
	
	public Double getAreaUnder(Double startTime, Double endTime) {
		//trapezoid method
		Double area = 0.0;
		int i = (int) Math.round(startTime*this.sampleRate);
		int j = (int) Math.round(endTime*this.sampleRate);
		area = ((originalData[i]+originalData[j])/2)*(this.time[j]-this.time[i]);
		return area;
	}

	public Double[] getSmoothedData() {
		return this.smoothedData;
	}
	
	public void setSmoothedData(Double[] data) {
		this.smoothedData = data;
	}
	
	public void setSmoothedDataPoint(int index, Double value) {
		this.smoothedData[index] = value;	
	}
	
	public Double[] getOriginalData() { 
		return this.originalData;
	}
	
	public void setOriginalData(Double[] data) {
		this.originalData = data;
	}
	
	public void setOriginalDataPoint(int index, Double value ) {
		this.originalData[index] = value;	
	}
	
	/**
	 * Used by Graph GUI to get time data from AxisDataSeries instances.
	 */
	public ArrayList<Double> getTime() {
		return (ArrayList<Double>) Arrays.asList(time);
	}
	
	/**
	 * Used by Graph GUI to get samples from AxisDataSeries instances.
	 */
	public ArrayList<Double> getSamples() {
		return (ArrayList<Double>) Arrays.asList(smoothedData);
	}
	
	public boolean getActive() {
		return isActive;
	}
	
	public void setActive(boolean b) {
		this.isActive = b;
	}
}
	
