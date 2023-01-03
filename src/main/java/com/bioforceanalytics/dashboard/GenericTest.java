package com.bioforceanalytics.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

/**
 * Used by the BioForce Graph to store the data associated with a single trial
 * (in the form of multiple {@link com.bioforceanalytics.dashboard.AxisDataSeries AxisDataSeries}).
 * This is also the parent class of all educator mode "lab templates".
 */
public class GenericTest {

	private List<Double> timeAxis;
	private List<Double> magTimeAxis;

	private AxisDataSeries[] axes;
	private List<List<Double>> dataSamples;

	private String name;

	private int rollBlkSize = 100;
	private String graphTitle;
	private AxisType[] defaultAxes;

	private ArrayList<Integer> testParameters;
	private int[] MPUOffsets;
	private int sampleRate;
	private int timeOffset;

	private static final Logger logger = LogController.start();

	/**
	 * Creates a GenericTest using inputs read directly from the module via SerialComm.
	 * @param testParameters array of test parameters
	 * @param finalData array of raw byte data from the module
	 * @param MPUMinMax array of constant MPU offsets specific to the module
	 */
	public GenericTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax) {

		int sampleRate = testParameters.get(7);
		int magSampleRate = testParameters.get(8);

		this.testParameters = testParameters;
		
		// only 3/9 indices are used (Accel X/Y/Z => 0/1/2)
		// kept at length of 9 to match # of DOFs (Gyro & Mag)
		int[] mpuOffsets = new int[9];

		// TODO MPUMinMax is sometimes randomly be read as "null" from SerialComm
		// this ensures that a NullPointerException isn't thrown later in GT
		if (MPUMinMax == null) {
			logger.warn("MPUMinMax offsets null, filling with 0s...");
			MPUMinMax = new int[][] {{0, 0}, {0, 0}, {0, 0}};
		}

		// populate MPU offsets by unpacking 2d array to min max array
		// (currently used for acceleration calculations only)
		for (int axi = 0; axi < MPUMinMax.length; axi++) {
			mpuOffsets[2*axi] = MPUMinMax[axi][0];
			mpuOffsets[(2*axi)+1] = MPUMinMax[axi][1];
		}

		MPUOffsets = mpuOffsets;
		dataSamples = new ArrayList<List<Double>>();
		
		// populate dataSamples's inner lists
		for (int i = 0; i < 10; i++) {
			List<Double> axis = new ArrayList<Double>();
			dataSamples.add(axis);
		}

		// populate acceleration X, Z, and Z
		for(int i = 0; i < finalData.length - 7; i+=7) {
			dataSamples.get(1).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(2).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(3).add((double)(finalData[i]*256)+finalData[i+1]); i+=1;
			
			//Starting at index 5, the counter must be incremented by an extra 6 every 126 bytes to account for the magnetometer data every 10th time index 
			if((i-5)%126==0) i+=6;		
		}

		// Populate gyroscope X, Y, and Z
		for (int i = 6; i < finalData.length - 7; i+=7) {
			dataSamples.get(4).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(5).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(6).add((double)(finalData[i]*256)+finalData[i+1]); i+=1;

			//Starting at index 11, the counter must be incremented by an extra 6 every 126 bytes to account for the magnetometer data every 10th time index
			if((i-11)%126==0) i+=6;	
		}
		
		// Populate magnetometer X, Y, and Z
		for (int i = 12; i < finalData.length - 121; i+=121) {
			dataSamples.get(7).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(8).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(9).add((double)(finalData[i]*256)+finalData[i+1]); i+=1;		
		}

		if (!(this instanceof ConservationMomentumModule)) {
			createAxisDataSeries(dataSamples, testParameters, mpuOffsets);
		}
		
	}

	/**
	 * Creates a GenericTest using dataSamples 2D List.
	 * Used for creating GenericTest from CSVHandler or DataOrganizer.
	 * @param testParameters array of test parameters
	 * @param dataSamples 2D Array of data from 9 raw axes (and time(0))
	 */
	public GenericTest(List<List<Double>> dataSamples, ArrayList<Integer> testParameters) {

		// only 3/9 indices are used (Accel X/Y/Z => 0/1/2)
		// kept at length of 9 to match # of DOFs (Gyro & Mag)
		int[] mpuOffsets = new int[9];
		this.dataSamples = dataSamples;
		this.testParameters = testParameters;

		for (int i = 0; i < 3; i++) {
			// populate MPU offsets by taking the avg of min and max
			// Currently used for acceleration calculations only
			mpuOffsets[i] = (testParameters.get(i+13)+testParameters.get(i+14))/2;
		}

		MPUOffsets = mpuOffsets;
		createAxisDataSeries(dataSamples, testParameters, mpuOffsets);

	}
	GenericTest(){};
	/**
	 * Populates the AxisDataSeries list by looping through dataSamples.
	 * This logic is shared by both constructors.
	 */
	public void createAxisDataSeries() {
		createAxisDataSeries(dataSamples, testParameters, MPUOffsets);
	}

	/**
	 * Populates the AxisDataSeries list by looping through dataSamples.
	 * This logic is shared by both constructors.
	 * @param dataSamples 2D array of data from 9 raw axes (and time(0))
	 * @param testParameters array of test parameters
	 * @param mpuOffsets array of acceleration offsets
	 */
	private void createAxisDataSeries(List<List<Double>> dataSamples, ArrayList<Integer> testParameters, int[] mpuOffsets) {

		// TODO change this to a name property that can be changed as a test selection menu
		setGraphTitle("Generic Test");
		setDefaultAxes(AxisType.AccelX);

		// convert unsigned delayAfterStart to signed
		short delayAfterStart = testParameters.get(2).shortValue();
		testParameters.set(2, (int) delayAfterStart);

		sampleRate = testParameters.get(7);

		int magSampleRate = testParameters.get(8);
		int accelSensitivity = testParameters.get(9);
		int gyroSensitivity = testParameters.get(10);

		// TODO may try to implement use of timer0 in future
		List<Double> timeAxis = new ArrayList<Double>();
		List<Double> magTimeAxis = new ArrayList<Double>();

		// populate various axes by looping through each sample
		// using "1" (AccelX) is arbitrary here, any axis will work
		for (int i = 0; i < dataSamples.get(1).size(); i++) {

			// populate time axis and apply time offset (if any)
			timeAxis.add(new Double(i+timeOffset) / sampleRate);

			// populate to avoid complications from this being null for now
			dataSamples.get(0).add(new Double(i) / sampleRate);

			// since the magnetometer runs at 96 sps compared to 960,
			// it must have a separate time axis for its data set(s)
			magTimeAxis.add(new Double(i) / magSampleRate);
		}

		this.timeAxis = timeAxis;
		this.magTimeAxis = timeAxis;

		// initialize axis data series array
		axes = new AxisDataSeries[AxisType.values().length];
		

		// loops so that X=0, Y=1, Z=2
		for (int i = 0; i < 3; i++) {

			// acceleration (NATIVE ACCELEROMETER MEASUREMENT)
			axes[i] = new AxisDataSeries(timeAxis, dataSamples.get(i+1), AxisType.valueOf(i), mpuOffsets, accelSensitivity, sampleRate);

//			// velocity
//			axes[i+4] = new AxisDataSeries(timeAxis, axes[i].integrate(), AxisType.valueOf(i+4), false, sampleRate);
//
//			// displacement
//			axes[i+8] = new AxisDataSeries(timeAxis, axes[i+4].integrate(), AxisType.valueOf(i+8), false, sampleRate);

			// angular velocity (NATIVE GYROSCOPE MEASUREMENT)
			axes[i+16] = new AxisDataSeries(timeAxis, dataSamples.get(i+4), AxisType.valueOf(i+16), gyroSensitivity, sampleRate);

//			// angular acceleration
//			axes[i+12] = new AxisDataSeries(timeAxis, axes[i+16].differentiate(), AxisType.valueOf(i+12), false, sampleRate);
//
//			// angular displacement
//			axes[i+20] = new AxisDataSeries(timeAxis, axes[i+16].integrate(), AxisType.valueOf(i+20), false, sampleRate);

			// magnetometer (NATIVE MEASUREMENT)
			axes[i+24] = new AxisDataSeries(magTimeAxis, dataSamples.get(i+7), AxisType.valueOf(i+24), true, magSampleRate);

		}

		// Creates magnitude data sets
		for (int i = 0; i < AxisType.values().length; i+=4) {
			if (axes[i] != null && axes[i+1] != null && axes[i+2] != null) {
				axes[i + 3] = new AxisDataSeries(axes[i], axes[i + 1], axes[i + 2], AxisType.valueOf(i + 3));
			}
		}
	}

	/**
	 * Recalculates velocity and displacement when normalizing acceleration.
	 * If this is a conservation of momentum test, it will also recalculate momentum.
	 */
	public void recalcKinematics() {

		// loops through X,Y,Z
		for (int i = 0; i < 3; i++) {

			// velocity
			axes[i+4] = new AxisDataSeries(axes[i].getTime(), axes[i].integrate(), AxisType.valueOf(i+4), false, sampleRate);
			
			// displacement
			axes[i+8] = new AxisDataSeries(axes[i].getTime(), axes[i+4].integrate(), AxisType.valueOf(i+8), false, sampleRate);

			// momentum (if this is a CoM test)
			if (this instanceof ConservationMomentumModule) {

				double mass = ((ConservationMomentumModule) this).getMomentumScalar();
				logger.info("Mass: " + mass);

				axes[i+28] = new AxisDataSeries(axes[i+28].getTime(), axes[i].integrate(mass), AxisType.valueOf(i+28), false, sampleRate);
			
			}

		}

		// recalculate magnitude data sets
		for (int i = 0; i < AxisType.DispMag.getValue(); i+=4) {

			if (this instanceof ConservationMomentumModule || i < 28) {
				// "axes[magnitude] = new AxisDataSeries(axes[X], axes[Y], axes[Z], AxisType.valueOf(magnitude))"
				axes[i+3] = new AxisDataSeries(axes[i], axes[i+1], axes[i+2], AxisType.valueOf(i+3));
			}
			
		}

	}
	
	/**
	 * Retrieves an AxisDataSeries for graphing.
	 * @param axis the {@link com.bioforceanalytics.dashboard.AxisType AxisType} to retrieve
	 * @return the AxisDataSeries selected
	 */
	public AxisDataSeries getAxis(Axis axis) {
		return axes[axis.getValue()];
	}

	public AxisDataSeries getAxisWithProccessing(Axis axis) {
		if (axes[axis.getValue()] == null) {
			this.reproccessAxis(axis.getValue());
		}
		return axes[axis.getValue()];
	}

	public void reproccessAxis(int axisId) {
		logger.info(String.format("reproccessing Axis: %s", axisId));
		int i = axisId%4;

		if (i == 3 && axisId > 0) {
			this.reproccessAxis((axisId-3));
			this.reproccessAxis((axisId-2));
			this.reproccessAxis((axisId-1));
			axes[axisId] = new AxisDataSeries(axes[(axisId-3)], axes[(axisId-2)], axes[(axisId-1)], AxisType.valueOf(axisId), this.rollBlkSize);
		}

		if (axisId >= 4 && axisId <= 6) {
			axes[axisId] = new AxisDataSeries(timeAxis, axes[i].integrate(), AxisType.valueOf(axisId), false, sampleRate, this.rollBlkSize);
		}

		if (axisId >= 8 && axisId <= 10) {
			this.reproccessAxis(i+4);
			axes[axisId] = new AxisDataSeries(timeAxis, axes[i+4].integrate(), AxisType.valueOf(axisId), false, sampleRate, this.rollBlkSize);
		}

		if (axisId >= 12 && axisId <= 14) {
			this.reproccessAxis(i+16);
			axes[axisId] = new AxisDataSeries(timeAxis, axes[i+16].differentiate(), AxisType.valueOf(axisId), false, sampleRate, this.rollBlkSize);
		}

		if (axisId >= 20 && axisId <= 22) {
			this.reproccessAxis(i+16);
			axes[axisId] = new AxisDataSeries(timeAxis, axes[i+16].integrate(), AxisType.valueOf(axisId), false, sampleRate, this.rollBlkSize);
		}
	}

	/**
	 * Returns raw data samples organized from SerialComm in the appropriate format for CSV.
	 * @return a 2D ArrayList of raw data samples for each sensor
	 */
	public List<List<Double>> getDataSamples() {
		return dataSamples;
	}
	/**
	 * returns sample rate
	 * @return integer value of sample rate
	 */
	public int getSampleRate(){
		return sampleRate;
	}
	/**
	 * Retrieves a test parameter.
	 * @param index the index to retrieve
	 * @return the given test parameter
	 */
	public int getTestParam(int index) {
		return testParameters.get(index);
	}

	/**
	 * Retrieves all test parameters.
	 * @return the list of test parameters
	 */
	public ArrayList<Integer> getTestParams() {
		return testParameters;
	}

	/**
	 * Retrieves the list of acceleration MPU offsets.
	 * @return the list of acceleration MPU offsets
	 */
	public int[] getMPUOffsets() {
		return MPUOffsets;
	}

	/**
	 * Shifts the time axis of all AxisDataSeries for module synchronization.
	 * Used to shift all data sets in a GenericTest left/right.
	 * @param offset the number of samples by which the time axis should be offset;
	 * a positive value will shift the graph to the right.
	 */
	public void addTimeOffset(double offset) {
		timeOffset += (int) (offset * sampleRate);
		createAxisDataSeries();
	}

	/**
	 * Get the number of seconds by which the time axis is offset.
	 * @return the number of seconds by which the time axis is offset
	 */
	public double getTimeOffset() {
		return ((double) timeOffset) / sampleRate;
	}
	/**
	 * Get the number of samples by which the time axis is offset.
	 * @returnThe number of samples by which the time axis is offset
	 */
	public int getDataOffset() {
		return timeOffset;
	}
	/**
	 * Resets the amount by which the time axis is offset to 0.
	 * This resets all data sets in a GenericTest to starting from 0.
	 */
	public void resetTimeOffset() {
		timeOffset = 0;
	}

	/**
	 * Initializes the experimental panel associated with this test.
	 * This method is overriden in each experiment template class.
	 * @param panel the panel to be initialized
	 */
	public void setupExperimentPanel(ExperimentPanel panel) {
		panel.setExperimentName("Generic Test");
		panel.applyParams();
	}

	/**
	 * Sets the default axes to be graphed for this test.
	 * @param axes an array of AxisTypes with default axes
	 */
	public void setDefaultAxes(AxisType...axes) {
		defaultAxes = axes;
	}
	
	/**
	 * Gets the default axes to be graphed for this test.
	 * @return an array of AxisTypes with default axes
	 */
	public AxisType[] getDefaultAxes() {
		return defaultAxes;
	}

	/**
	 * Sets the title to be displayed above the graph.
	 * @param the graph title to be displayed
	 */
	public void setGraphTitle(String title) {
		graphTitle = title;
	}

	/**
	 * Gets the title to be displayed above the graph.
	 * @return the graph title to be displayed
	 */
	public String getGraphTitle() {
		return graphTitle;
	}

	/**
	 * Sets the name of this test.
	 * Used as the title of data set panels and read from
	 * the name of the CSV being imported.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of this test.
	 * @return the name of this test.
	 */
	public String getName() {
		return this.name;
	}

	public void setRollBlkSize(int rollBlkSize) {
		this.rollBlkSize = rollBlkSize;
	}
}
