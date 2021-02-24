package com.bioforceanalytics.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used by the Data Analysis Graph to store the data associated with a single axis (eg. Acceleration X).
 * Also handles converting data samples into physical quantities, applying moving averages, and filtering (in the future).
 */
public class AxisDataSeries {

	// the time axis for this data set
	private Double[] time;

	// data samples BEFORE any rolling average is applied
	// this is the base data used to calculate all rolling averages
	// this data will still be calibrated and normalized, NOT raw
	private Double[] originalData;

	// data samples after rolling average is applied to original data
	private Double[] smoothedData;

	// array of data samples with normalization offset applied
	private Double[] normalizedData;

	/*
	The default rolling block size used to smooth data.
	*/
	public final static int DEFAULT_BLOCK_SIZE = 100;

	// the sample block size used for smoothing data
	private int rollBlkSize;

	double timeOffset;

	// the amount the smoothed data set should be shifted up/down
	private double vertOffset = 0;

	/**
	 * The enum representation of this axis.
	 */
	public final AxisType axis;

	/**
	 * The length of the axis's test data in seconds.
	 */
	public final double testLength;

	/**
	 * The sample rate at which the axis's data was measured.
	 */
	public final int sampleRate;

	// acceleration due to gravity, modify this to add more sigfigs if needed
	private final double GRAVITY = 9.80665;

	private static final Logger logger = LogManager.getLogger();

	public static HashMap<String, AxisDataSeries> nameADSMap = new HashMap<String,AxisDataSeries>();

	public static AxisDataSeries getAxisDataSeries(String name){
		return nameADSMap.get(name);
	}
	public static void addADS(String name, AxisDataSeries ads){
        if(nameADSMap.containsKey(name)){
			name += "#" + Collections.frequency(nameADSMap.keySet(), name);
		}
		nameADSMap.put(name,ads);
    }

	/**
	 * Constructor for data NOT natively recorded by the module OR from the magnetometer.
	 * @param time the time axis for the data set
	 * @param data the samples for the data set
	 * @param axis the {@link com.bioforceanalytics.dashboard.AxisType AxisType} identifying the data set
	 * @param signData indicates whether the data should be converted from unsigned to signed
	 * @param sampleRate the number of data samples recorded in one second
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, boolean signData, int sampleRate) {

		// casts Lists to Double[]'s
		// (this is done b/c DataOrganizer uses ArrayLists)
		// TODO change "time" and "data" to Double[] when GenericTest is updated to use SerialComm
		//
		this.time = new Double[time.size()];
		this.time = time.toArray(this.time);
		addADS(axis.getName(),this);
		// If dealing w/ magnetometer, only save every 10th data sample removing nulls
		// This is because mag data is sampled at 1/10 the rate of accel/gyro,
		// but the List "data" is filled w/ null samples assuming 960 samples/sec
		if (axis.getValue() / 4 == 6) {

			// this is to remove nulls from dataOrganizer series
			for (int i = data.size() - 1; i >= 0; i--) {

				if (data.get(i) == null) {
					data.remove(i);
				}

			}

		}

		this.originalData = new Double[data.size()];
		this.originalData = data.toArray(this.originalData);

		this.axis = axis;
		this.sampleRate = sampleRate;

		this.testLength = ((double) data.size()) / sampleRate; 
		this.rollBlkSize = axis.getValue() / 4 == 6 ? DEFAULT_BLOCK_SIZE / 10 : DEFAULT_BLOCK_SIZE;

		if (signData) {

			for (int i = 0; i < this.originalData.length; i++) {

				// convert raw data to signed data
				if (this.originalData[i] > 32768) {
					this.originalData[i] -= 65535;
				}

				// if axis class is magnetometer
				if (axis.getValue() / 4 == 6) {
					// apply mag sensitivity - is always 4800.  Divide by 8192 here b/c mag values are only 14 bits in the module
					this.originalData[i] *= (double)4800 /(double) 8192;
				}
			}
		}

		// if axis class is magnetometer (24-27), normalize data
		if (axis.getValue() / 4 == 6) {

			// create normalized data series using first two seconds of module data
			createNormalizedData(0.0, 2.0);

			// creates smoothedData by applying rolling average to normalized data
			this.smoothedData = applyMovingAvg(normalizedData, rollBlkSize);

		}
		else {
			// don't normalize if not a raw magnetometer series;
			// simply copy over original data for use in integrate()
			this.normalizedData = new Double[originalData.length];
			copyArray(this.originalData, this.normalizedData);

			this.smoothedData = applyMovingAvg(normalizedData, rollBlkSize);
		}

		// print AxisDataSeries debug info
		logger.debug(toString());

	}

	public AxisDataSeries(AxisData axisData,Axis axisName){
		this.sampleRate = 960;
		this.time =  new Double[axisData.getData().length];

		addADS(axisName.getName(),this);
		this.originalData = axisData.getData();
		this.normalizedData = axisData.getData();
		this.smoothedData = axisData.getData();
		this.axis = AxisType.AccelX;
		

		this.testLength = ((double) axisData.getData().length) / sampleRate; 
		for(int i = 0; i < time.length; i++){
			time[i] = ((double) i)/sampleRate;
		}
		

	}
	

/**
	 * Constructor for data NOT natively recorded by the module OR from the magnetometer. 
	 * Use this for CustomAxisType AxisDataSeries, because it does not require axis type
	 * @param time the time axis for the data set
	 * @param data the samples for the data set
	 * @param signData indicates whether the data should be converted from unsigned to signed
	 * @param sampleRate the number of data samples recorded in one second
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, boolean signData, int sampleRate) {
		this(time, data, AxisType.AccelX,signData,sampleRate); //NOTE: the AccelX is only for the purpose of passing some generic axis to the ADS constructor
	}
	/**
	 * Constructor for acceleration data.
	 * @param time the time axis for the data set
	 * @param data the samples for the data set
	 * @param axis the {@link com.bioforceanalytics.dashboard.AxisType AxisType} identifying the data set
	 * @param accelOffsets the array of offsets to be applied to the acceleration data;
	 * the first dimension indicates the axis type (X=0,Y=1,Z=2) and the second dimension
	 * stores the bounds, where index 0 is the minimum offset and index 1 is the maximum offset
	 * @param accelSensitivity the maximum value or "resolution" of the raw data, being 1, 2, 4, or 8 Gs;
	 * this value is multiplied by how close the measurement was to the maximum value to calculate the physical quantity.
	 * <p>In equation format: acceleration = sensitivity * (raw data / max value for data [32768])</p>
	 * @param sampleRate the number of data samples recorded in one second
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, int[] accelOffsets, int accelSensitivity, int sampleRate) {

		// casts Lists to Double[]'s
		// (this is done b/c DataOrganizer uses ArrayLists)
		// TODO change "time" and "data" to Double[] when GenericTest is updated to use SerialComm
		//
		this.time = new Double[time.size()];
		this.time = time.toArray(this.time);
		addADS(axis.getName(),this);
		this.originalData = new Double[data.size()];
		this.originalData = data.toArray(this.originalData);

		this.axis = axis;
		this.sampleRate = sampleRate;

		this.testLength = ((double) data.size()) / sampleRate;
		this.rollBlkSize = axis.getValue() / 4 == 6 ? DEFAULT_BLOCK_SIZE / 10 : DEFAULT_BLOCK_SIZE;

		for (int i = 0; i < this.originalData.length; i++) {

			// convert raw data to signed data
			if (this.originalData[i] > 32768) {
				this.originalData[i] -= 65535;
			}

			// subtract offsets (which are signed)
			// accel enum is 0-2 and offsets are 0-2 (X,Y,Z)
			this.originalData[i] -= accelOffsets[axis.getValue()];

			// apply sensitivity for accel (including acceleration due to gravity)
			this.originalData[i] *= ((double) accelSensitivity * GRAVITY) / 32768;

		}

		// create normalized data series using first second of module data
		createNormalizedData(0.0, 2.0);

		// creates smoothedData by applying rolling average to normalized data
		smoothedData = applyMovingAvg(normalizedData, rollBlkSize);

		// print AxisDataSeries debug info
		logger.debug(toString());

	}

	/**
	 * Constructor for gyroscope data.
	 * @param time the time axis for the data set
	 * @param data the samples for the data set
	 * @param axis the {@link com.bioforceanalytics.dashboard.AxisType AxisType} identifying the data set
	 * @param gyroSensitivity the maximum value or "resolution" of the raw data;
	 * this value is multiplied by how close the measurement was to the maximum value to calculate the physical quantity.
	 * In equation format: gyro = sensitivity * (raw data / max value for data [32768])
	 * @param sampleRate the number of data samples recorded in one second
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, int gyroSensitivity, int sampleRate) {

		// casts Lists to Double[]'s
		// (this is done b/c DataOrganizer uses ArrayLists)
		this.time = new Double[time.size()];
		this.time = time.toArray(this.time);
		addADS(axis.getName(),this);
		this.originalData = new Double[data.size()];
		this.originalData = data.toArray(this.originalData);

		this.axis = axis;
		this.sampleRate = sampleRate;

		this.testLength = ((double) data.size()) / sampleRate;
		this.rollBlkSize = axis.getValue() / 4 == 6 ? DEFAULT_BLOCK_SIZE / 10 : DEFAULT_BLOCK_SIZE;

		for (int i = 0; i < this.originalData.length; i++) {

			// convert raw data to signed data
			if (this.originalData[i] > 32768) {
				this.originalData[i] -= 65535;
			}

			// apply sensitivity for gyro
			this.originalData[i] *= ((double) gyroSensitivity) / 32768;

		}

		//create normalized data series using first second of module data
		createNormalizedData(0.0, 2.0);

		//creates smoothedData by applying rolling average to normalized data
		smoothedData = applyMovingAvg(normalizedData, rollBlkSize);

		// print AxisDataSeries debug info
		logger.debug(toString());

	}

	/**
	 * Constructor for creating a magnitude data set.
	 * @param a1 the X component of the data
	 * @param a2 the Y component of the data
	 * @param a3 the Z component of the data
	 */
	public AxisDataSeries(AxisDataSeries a1, AxisDataSeries a2, AxisDataSeries a3, AxisType axis) {

		// casts Lists to Double[]'s
		// (this is done b/c DataOrganizer uses ArrayLists)

		this.time = new Double[a1.getTime().size()];
		this.time = a1.getTime().toArray(this.time);
		addADS(axis.getName(),this);
		this.axis = axis;
		this.sampleRate = a1.sampleRate;

		int length = a1.getSamples().size();

		this.testLength = ((double) length) / sampleRate;
		this.rollBlkSize = axis.getValue() / 4 == 6 ? DEFAULT_BLOCK_SIZE / 10 : DEFAULT_BLOCK_SIZE;

		// convert data ArrayLists to arrays
		Double[] d1 = new Double[length];
		d1 = a1.getSamples().toArray(d1);

		Double[] d2 = new Double[length];
		d2 = a2.getSamples().toArray(d2);

		Double[] d3 = new Double[length];
		d3 = a3.getSamples().toArray(d3);


		Double[] result = new Double[d1.length];

		// calculate magnitude data set
		for (int i = 0; i < d1.length; i++) {

			// r = sqrt(d1^2 + d2^2 + d3^2)
			result[i] = Math.sqrt(Math.pow(d1[i], 2) + Math.pow(d2[i], 2) + Math.pow(d3[i], 2));

		}

		this.originalData = result;

		// copy originalData to normalizedData and smoothedData
		this.normalizedData = new Double[originalData.length];
		this.smoothedData = new Double[originalData.length];
		copyArray(this.originalData, this.normalizedData);
		copyArray(this.normalizedData, this.smoothedData);

		// print AxisDataSeries debug info
		logger.debug(toString());

	}

	/**
	 * Creates normalized data set with a "baseline" interval set to 0.
	 * @param startTime the x-value of the first data point
	 * @param endTime the x-value of the second data point
	 */
	private void createNormalizedData(Double startTime, Double endTime) {

		// Convert times to sample #s
		int startIndex = (int) Math.round(startTime*sampleRate);
		int endIndex = (int) Math.round(endTime*sampleRate);

		double normOffset = 0;

		// if data points are the same, manually make the baseline 20 samples
		// TODO fix this so that data can be normalized to "true" 0
		if (startIndex == endIndex) {
			startIndex -= 10;
			endIndex += 10;
		}

		// calculate the average over the interval [startTime, endTime]
		for (int i = startIndex; i < endIndex; i++) {
			normOffset += originalData[i];
		}

		normOffset /= (double) (endIndex-startIndex);

		// reset normalized data to original data
		this.normalizedData = new Double[originalData.length];
		copyArray(this.originalData, this.normalizedData);

		// subtract offset from each data point to create normalized data
		for (int i = 0; i < originalData.length; i++) {
			normalizedData[i] = originalData[i] - normOffset;
		}
	}

	/**
	 * Recalculates normalized data and smoothing.
	 * Used by the Data Analysis Graph for user control of the baseline average.
	 */
	public void applyNormalizedData(Double startTime, Double endTime) {

		createNormalizedData(startTime, endTime);

		// if smoothing is enabled, apply a moving average; otherwise, copy normalized data
		this.smoothedData = applyMovingAvg(this.normalizedData, this.rollBlkSize);
	
	}

	/**
	 * Applies a middle-based simple moving average to a data series.
	 * @param array the data series for the moving average to be applied to
	 * @param sampleBlockSize the number of samples used to calculate the moving average
	 */
	private Double[] applyMovingAvg(Double[] array, int sampleBlockSize) {

		// work on a copy of the data for safety
		Double[] newArray = new Double[array.length];
		copyArray(array, newArray);

		// a block size less than 0 indicates resetting smoothing
		if (sampleBlockSize <= 1) return newArray;

		// loop through all values except (block size / 2) on the ends of the data;
		// since this is a middle-based moving average, an index such as 0 will not work
		for (int i = sampleBlockSize/2; i < newArray.length - sampleBlockSize/2; i++) {

			double localTotal = 0;

			// loop through current block (i +- block size / 2) to accumulate total
			for (int j = (i - sampleBlockSize/2); j < (i+sampleBlockSize/2); j++) {
				localTotal += array[j];
			}

			// calculate average value over the block as the "smoothed" point
			newArray[i] = localTotal / sampleBlockSize;
		}

		/*
		===============================================================================
		Zero out the start and end portions of the test. Currently disabled since it
		makes the data more inaccurate than if the unsmoothed "ends" remain.
		TODO consider removing these "ends" from the test so that users don't see them.
		
		for (int i = 0; i < sampleBlockSize/2; i++) {
			newArray[i] = 0.0;
		}

		for (int i = newArray.length - sampleBlockSize/2; i < newArray.length; i++) {
			newArray[i] = 0.0;
		}
		===============================================================================
		*/

		return newArray;
	}

	/**
	 * Applies a midpoint-based simple moving average to this AxisDataSeries.
	 * Intended as wrapper method for {@link #applyMovingAvg} so other classes can smoothe the data set.
	 * @param sampleBlockSize the number of samples used to calculate the moving average
	 */
	public void smoothData(int sampleBlockSize) {

		logger.info("Smoothing " + axis + " (block size " + sampleBlockSize + ", vertical offset " + vertOffset + ")");

		this.rollBlkSize = sampleBlockSize;
		smoothedData = applyMovingAvg(normalizedData, this.rollBlkSize);

		// apply vertical offset to smoothed data
		for (int i = 0; i < smoothedData.length; i++) {
			smoothedData[i] += vertOffset;
		}

	}

	/**
	 * Resets the smoothed data set to the default smoothing value.
	 */
	public void resetSmoothing() {

		vertOffset = 0;
		smoothData(this.rollBlkSize);

	}

	// TODO make "createNormalizedData" use vertOffset as well?
	// this would allow us to eliminate the normalizedData array,
	// simplifying the codebase and being more consistent

	/**
	 * Vertically shifts the graph up or down.
	 * Used by the Data Analysis Graph to normalize a single data set.
	 * @param the amount to shift the graph up/down
	 */
	public void vertShift(double amount) {

		logger.info("Vertically shifting " + axis + " by " + amount);

		vertOffset += amount;
		smoothData(this.rollBlkSize);

	}

	/**
	 * Numerically integrates this AxisDataSeries.
	 * Calculated using the trapezoidal rule.
	 * @return the integral of this data set
	 */
	public List<Double> integrate() {

		// create empty array with the same length as data
		Double[] result = new Double[normalizedData.length];

		// temporarily set initial value to 0; necessary for loop below
		result[0] = 0.0;

		// start loop at 1 in order to look at previous value
		for (int i = 1; i < normalizedData.length; i++) {

			// Area of a trapezoid = (a + b) / 2 * h, where a = y1, b = y2, and h = ∆t
			result[i] = result[i-1] + (normalizedData[i] + normalizedData[i-1])/2 * (time[i] - time[i-1]);

		}

		// calculate the first integrated sample as our initial condition ("+C")
		result[0] = result[1];

		return Arrays.asList(result);
	}

	/**
	 * Numerically integrates this AxisDataSeries.
	 * Calculated using the trapezoidal rule.
	 * @param scalar multiplies each datapoint by a double scalar
	 * @return the integral of this data set
	 */
	public List<Double> integrate(double scalar) {

		// integrate the data normally
		List<Double> data = integrate();

		// multiply by the scalar value
		for (Double d : data) d *= scalar;

		return data;
	}

	/**
	 * Numerically differentiates this AxisDataSeries.
	 * Calculated by finding slopes of secant lines between adjacent points.
	 * @return the derivative of this data set
	 */
	public List<Double> differentiate() {

		// create empty array with the same length as data
		Double[] result = new Double[normalizedData.length];

		// start loop at 1 in order to look at previous value
		for(int i = 1; i < normalizedData.length - 1; i++) {
			result[i] = (normalizedData[i+1]-normalizedData[i-1])/(time[i+1]-time[i-1]);
		}

		// first data point matches second
		result[0] = result[1];

		// last data point matches second to last
		result[normalizedData.length-1] = result[normalizedData.length-2];

		return Arrays.asList(result);
	}

	/**
	 * Finds slope of the line tangent to a data point.
	 * <p>Calculated by finding the slope of the secant line <code>n</code> indices to the left and right of the point.
	 * <code>n</code> is passed in as the <code>resolution</code> parameter, and is necessary to make tangent lines
	 * look accurate to the resolution of samples graphed.</p>
	 * @param x the x-value of the data point
	 * @param res the resolution of the graph, and the value <code>n</code> used to calculate the secant line.
	 * @return the slope of the line tangent to the point
	 */
	public Double getSlope(Double x, int res) {

		// calculate index of x-value
		int i = (int) Math.round(x*this.sampleRate);

		// slope (m) = ∆y/∆x, where the interval is the resolution of the graphed data set
		Double slope = (smoothedData[i+res]-smoothedData[i-res])/(this.time[i+res]-this.time[i-res]);

		return slope;

	}

	/**
	 * Finds slope of the line between two data points.
	 * @param startTime the x-value of the first data point
	 * @param endTime the x-value of the second data point
	 * @return the slope of the secant line
	 */
	public Double getSlope(Double startTime, Double endTime) {

		// calculate indices of start and end times
		int a = (int) Math.round(startTime*this.sampleRate);
		int b = (int) Math.round(endTime*this.sampleRate);

		// slope (m) = ∆y/∆x
		Double slope = (smoothedData[b]-smoothedData[a])/(this.time[b]-this.time[a]);

		return slope;

	}

	/**
	 * Finds the area under a section of the curve.
	 * Utilizes trapezoid rule for numerical integration.
	 * @param startTime the x-value of the first data point
	 * @param endTime the x-value of the last data point
	 * @return
	 */
	public Double getAreaUnder(Double startTime, Double endTime) {

		Double area = 0.0;

		// calculate indices of start and end times
		int a = (int) Math.round(startTime*this.sampleRate);
		int b = (int) Math.round(endTime*this.sampleRate);

		// loop through data points to calculate area of trapezoids
		for (int i = a + 1; i < b + 1; i++) {

			// Area of a trapezoid = (a + b) / 2 * h, where a = y1, b = y2, and h = ∆t
			area += (smoothedData[i] + smoothedData[i-1])/2 * (time[i] - time[i-1]);
			
		}

		return area;

	}

	/**
	 * Returns the time axis of this AxisDataSeries.
	 * Used by the Data Analysis Graph for plotting an axis.
	 */
	public ArrayList<Double> getTime() {
		return new ArrayList<Double>(Arrays.asList(time));
	}

	/**
	 * Returns the data samples of this AxisDataSeries.
	 * Used by the Data Analysis Graph for plotting an axis.
	 */
	public ArrayList<Double> getSamples() {
		return new ArrayList<Double>(Arrays.asList(smoothedData));
	}

	/**
	 * Internal wrapper for System.arraycopy(...).
	 * Creates a full copy of an array.
	 * @param src the array to copy data from
	 * @param dest the array to copy data to
	 */
	private void copyArray(Double[] src, Double[] dest) {
		System.arraycopy(src, 0, dest, 0, src.length);
	}

	@Override
	public String toString() {
		return this.axis + " | " + "Time: " + this.time.length + " | Data: " + this.smoothedData.length;
	}

	public void setTimeOffset(double offset){
		timeOffset = offset;
	}
	public double getTimeOffset(){
		return timeOffset;
	}

}