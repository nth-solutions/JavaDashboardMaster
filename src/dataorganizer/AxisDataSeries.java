package dataorganizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Used by the Data Analysis Graph to store the data associated with a single axis (eg. Acceleration X).
 * Also handles converting bitcount data into physical quantities, applying moving averages, and filtering (in the future).
 */
public class AxisDataSeries {

	private Double[] time;

	// data samples BEFORE any rolling average is applied
	// this is the base data used to calculate all rolling averages
	// this data will still be calibrated and normalized, NOT raw
	private Double[] originalData;

	// data samples after rolling average is applied to original data
	private Double[] smoothedData;

	// data for display on the graph that the user can access and modify (via applying a custom rolling average) without modifying smoothedData
	// same as smoothedData by default
	private Double[] userSmoothedData;

	// array of data samples with normalization offset applied
	private Double[] normalizedData;

	// default rolling block size to smooth data for integration
	private int rollBlkSize = 100;

	private AxisType axis;

	// samples per second in passed in data series
	private int sampleRate;

	/**
	 * Constructor for data NOT natively recorded by the module OR from the magnetometer.
	 * @param time the time axis for the data set
	 * @param data the samples for the data set
	 * @param axis the {@link dataorganizer.AxisType AxisType} identifying the data set
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

		// If dealing w/ magnetometer, only save every 10th data sample removing nulls
		// This is because mag data is sampled at 1/10 the rate of accel/gyro,
		// but the List "data" is filled w/ null samples assuming 960 samples/sec
		if (axis.getValue() >= 24 && axis.getValue() <= 26) {

			// magnetometer uses rolling block size of 10
			rollBlkSize = 10;

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

		if (signData) {

			for (int i = 0; i < this.originalData.length; i++) {

				// convert raw data to signed data
				if (this.originalData[i] > 32768) {
					this.originalData[i] -= 65535;
				}

				if (axis.getValue() >= 24 && axis.getValue() <= 26) {
					// apply mag sensitivity - is always 4800.  Divide by 8192 here b/c mag values are only 14 bits in the module
					this.originalData[i] *= (double)4800 /(double) 8192;
				}
			}
		}


		if (axis.getValue() >= 24 && axis.getValue() <= 26) {

			// create normalized data series using first two seconds of module data
			createNormalizedData(0.0, 2.0, sampleRate);

			// creates smoothedData by applying rolling average to normalized data
			smoothedData = applyMovingAvg(normalizedData.clone(), rollBlkSize);

		}
		else {
			// don't normalize if not a raw magnetometer series
			smoothedData = applyMovingAvg(originalData.clone(), rollBlkSize);
		}

		// clones smoothedData for display on graph
		userSmoothedData = smoothedData.clone();

		// print debug info about AxisDataSeries
		System.out.println(toString());

	}

	/**
	 * Constructor for acceleration data.
	 * @param time the time axis for the data set
	 * @param data the samples for the data set
	 * @param axis the {@link dataorganizer.AxisType AxisType} identifying the data set
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

		this.originalData = new Double[data.size()];
		this.originalData = data.toArray(this.originalData);

		this.axis = axis;
		this.sampleRate = sampleRate;

		for (int i = 0; i < this.originalData.length; i++) {

			// convert raw data to signed data
			if (this.originalData[i] > 32768) {
				this.originalData[i] -= 65535;
			}

			// subtract offsets (which are signed)
			// accel enum is 0-2 and offsets are 0-2 (X,Y,Z)
			this.originalData[i] -= accelOffsets[axis.getValue()];

			// apply sensitivity for accel
			this.originalData[i] *= ((double) accelSensitivity) / 32768;

		}

		// create normalized data series using first second of module data
		createNormalizedData(0.0, 2.0, sampleRate);

		// creates smoothedData by applying rolling average to normalized data
		smoothedData = applyMovingAvg(normalizedData.clone(), rollBlkSize);

		// clones smoothedData for display on graph
		userSmoothedData = smoothedData.clone();

		// print debug info about AxisDataSeries
		System.out.println(toString());

	}

	/**
	 * Constructor for gyroscope data.
	 * @param time the time axis for the data set
	 * @param data the samples for the data set
	 * @param axis the {@link dataorganizer.AxisType AxisType} identifying the data set
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

		this.originalData = new Double[data.size()];
		this.originalData = data.toArray(this.originalData);

		this.axis = axis;
		this.sampleRate = sampleRate;

		for (int i = 0; i < this.originalData.length; i++) {

			// convert raw data to signed data
			if (this.originalData[i] > 32768) {
				this.originalData[i] -= 65535;
			}

			// apply sensitivity for gyro
			this.originalData[i] *= ((double) gyroSensitivity) / 32768;

		}

		//create normalized data series using first second of module data
		createNormalizedData(0.0, 2.0, sampleRate);

		//creates smoothedData by applying rolling average to normalized data
		smoothedData = applyMovingAvg(normalizedData.clone(), rollBlkSize);

		//clones smoothedData for display on graph
		userSmoothedData = smoothedData.clone();

		// print debug info about AxisDataSeries
		System.out.println(toString());

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

		this.axis = axis;

		int length = a1.getSamples().size();

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
		this.smoothedData = this.originalData.clone();
		this.userSmoothedData = this.smoothedData.clone();

		// print debug info about AxisDataSeries
		System.out.println(toString());

	}

	/**
	 * <b>USED FOR TESTING CALCULATIONS, NOT FOR PRODUCTION</b><p>
	 * Constructor for producing linear acceleration axis.
	 * Utilizes a low-pass filter to isolate the gravity vector,
	 * then subtracts this from raw acceleration to yield linear acceleration.
	 * @deprecated filter does not work as intended, will be removed in the future
	 * @param accel - the AxisDataSeries for the raw acceleration data set
	 */
	@Deprecated
	public AxisDataSeries(AxisDataSeries accel) {

		// casts Lists to Double[]'s
		// (this is done b/c DataOrganizer uses ArrayLists)
		//
		this.time = new Double[accel.getTime().size()];
		this.time = accel.getTime().toArray(this.time);

		this.originalData = accel.getOriginalData().clone();

		Double[] gravity = new Double[this.originalData.length];
		Arrays.fill(gravity, 0d);

		// unsure about the nature of ALPHA:
		// found it is calculated α = t / (t + dT),
		// but not sure what "t" (time constant) would be;
		// using a value that I have seen online.
		final float ALPHA = 0.9f;

		gravity[0] = originalData[0];

		for (int i = 1; i < gravity.length; i++) {

			gravity[i] = ALPHA * gravity[i-1] + (1 - ALPHA) * originalData[i];
			originalData[i] -= gravity[i];

		}

		this.smoothedData = this.originalData.clone();
		userSmoothedData = smoothedData.clone();

	}

	/**
	 * Creates normalized data set with a "baseline" interval set to 0.
	 * @param startTime the x-value of the first data point
	 * @param endTime the x-value of the second data point
	 * @param sampleRate the sample rate of the data set being normalized
	 */
	public void createNormalizedData(Double startTime, Double endTime, int sampleRate) {

		// Convert times to sample #s
		int startIndex = (int) Math.round(startTime*sampleRate);
		int endIndex = (int) Math.round(endTime*sampleRate);

		double normOffset = 0;

		// calculate average value over the interval [startTime, endTime]
		for (int i = startIndex; i < endIndex; i++) {
			normOffset += originalData[i];
		}
		normOffset /= (double) (endIndex-startIndex);

		this.normalizedData = this.originalData.clone();

		// subtract offset from each data point to create normalized data
		for (int i = 0; i < originalData.length; i++) {
			normalizedData[i] = originalData[i] - normOffset;
		}
	}


	/**
	 * Applies a middle-based simple moving average to a data series.
	 * @param array the data series for the moving average to be applied to
	 * @param sampleBlockSize the number of samples used to calculate the moving average
	 */
	private Double[] applyMovingAvg(Double[] array, int sampleBlockSize) {

		Double[] newArray = array.clone();
		if (sampleBlockSize == 0) return array;

		for (int i = sampleBlockSize/2; i < newArray.length - sampleBlockSize/2; i++) {
			double localTotal = 0;
			for (int j = (i - sampleBlockSize/2); j < (i+sampleBlockSize/2); j++) {
				localTotal += array[j];
			}
			newArray[i] = localTotal / sampleBlockSize;
		}

		/*
		Zero out the start and end portions of the test.
		Currently disabled since it isn't necessary.

		for (int i = 0; i < sampleBlockSize/2; i++) {
			newArray[i] = 0.0;
		}

		for (int i = newArray.length - sampleBlockSize/2; i < newArray.length; i++) {
			newArray[i] = 0.0;
		}
		*/

		return newArray;
	}

	/**
	 * Applies a midpoint-based simple moving average to this AxisDataSeries.
	 * Intended as wrapper method for {@link #applyMovingAvg} so other classes can smoothe the data set.
	 * @param sampleBlockSize the number of samples used to calculate the moving average
	 */
	public void applyCustomMovingAvg(int sampleBlockSize) {
		userSmoothedData = applyMovingAvg(smoothedData, sampleBlockSize);
	}

	/**
	 * Numerically integrates this AxisDataSeries.
	 * Calculated using the trapezoidal rule.
	 * @return the integral of this data set
	 */
	public List<Double> integrate() {

		Double[] result = new Double[smoothedData.length];

		result[0] = 0.0;

		for(int i = 1; i < smoothedData.length; i++) {
			result[i] = result[i-1] + (smoothedData[i] + smoothedData[i-1])/2 * (time[i] - time[i-1]);
		}

		// calculate the first integrated sample
		// this is our initial condition (our "+C")
		result[0] = result[1];

		return Arrays.asList(result);
	}

	/**
	 * Numerically differentiates this AxisDataSeries.
	 * Calculated by finding slopes of secant lines between adjacent points.
	 * @return the derivative of this data set
	 */
	public List<Double> differentiate() {

		Double[] result = new Double[smoothedData.length];

		for(int i = 1; i < smoothedData.length - 1; i++) {
			result[i] = (smoothedData[i+1]-smoothedData[i-1])/(time[i+1]-time[i-1]);
		}

		// fill in first and last data point
		result[0] = result[1];
		result[smoothedData.length-1] = result[smoothedData.length-2];

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
		Double slope = (userSmoothedData[i+res]-userSmoothedData[i-res])/(this.time[i+res]-this.time[i-res]);

		return slope;

	}

	/**
	 * Finds slope of the line between two data points.
	 * @param startTime the x-value of the first data point
	 * @param endTime the x-value of the second data point
	 * @return the slope of the secant line
	 */
	public Double getSlope(Double startTime, Double endTime) {

		System.out.println(startTime);
		System.out.println(endTime);

		// if start and end times are the same, return slope of tangent line
		if (startTime.equals(endTime)) {
			/*
			TODO defaulting to "1" as resolution since GraphNoSINCController can't pass this info;
			this seems like an acceptable inaccuracy, since users shouldn't be graphing tangent lines with two points anyways
			*/
			return getSlope(startTime, 1);
		}

		// calculate indices of start and end times
		int a = (int) Math.round(startTime*this.sampleRate);
		int b = (int) Math.round(endTime*this.sampleRate);

		// slope (m) = ∆y/∆x
		Double slope = (userSmoothedData[b]-userSmoothedData[a])/(this.time[b]-this.time[a]);

		return slope;

	}

	// returns area under a section of a data curve utilizing the trapezoid method
	public Double getAreaUnder(Double startTime, Double endTime) {

		Double area = 0.0;

		// calculate indices of start and end times
		int a = (int) Math.round(startTime*this.sampleRate);
		int b = (int) Math.round(endTime*this.sampleRate);

		for (int i = a + 1; i < b + 1; i++) {
			area += (userSmoothedData[i] + userSmoothedData[i-1])/2 * (time[i] - time[i-1]);
		}

		return area;

	}

	@Override
	public String toString() {
		return this.axis + " | " + "Time: " + this.time.length + " | Data: " + this.smoothedData.length;
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
		return new ArrayList<Double>(Arrays.asList(userSmoothedData));
	}

	@Deprecated
	public void setOriginalDataPoint(int index, Double value) {
		this.originalData[index] = value;
	}

	@Deprecated
	public Double[] getSmoothedData() {
		return this.smoothedData;
	}

	@Deprecated
	public Double[] getOriginalData() {
		return this.originalData;
	}

	@Deprecated
	public void setOriginalData(Double[] data) {
		this.originalData = data;
	}
}