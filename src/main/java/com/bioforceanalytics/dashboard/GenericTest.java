package com.bioforceanalytics.dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Used by the Data Analysis Graph to store the data associated with a single module (in the form of multiple {@link com.bioforceanalytics.dashboard.AxisDataSeries AxisDataSeries}).
 * This is also the parent class of all educator mode "lab templates".
 */
public class GenericTest {
	
	private AxisDataSeries[] axes;
	private List<List<Double>> dataSamples;
	private String graphTitle;
	private AxisType[] defaultAxes;

	/**
	 * Creates a GenericTest using inputs read directly from the module via SerialComm.
	 * This is the preferred method of passing data to the Data Analysis Graph and features up-to-date calculations.
	 * @param testParameters array of test parameters
	 * @param finalData array of raw byte data from the module
	 * @param MPUMinMax array of constant MPU offsets specific to the module
	 */
	public GenericTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax) {

		int sampleRate = testParameters.get(7);
		int magSampleRate = testParameters.get(8);

		// only 3/9 indices are used (Accel X/Y/Z => 0/1/2)
		// kept at length of 9 to match # of DOFs (Gyro & Mag)
		int[] mpuOffsets = new int[9];

		// TODO MPUMinMax will randomly be read as "null" from SerialComm
		// this ensures that a NullPointerException isn't thrown later in GT
		if (MPUMinMax == null) {
			System.out.println("Error: MPUMinMax offsets null");
			MPUMinMax = new int[][] {{0, 0}, {0, 0}, {0, 0}};
		}

		// populate MPU offsets by taking the avg of min and max
		// Currently used for acceleration calculations only
		for (int axi = 0; axi < MPUMinMax.length; axi++) {
			mpuOffsets[axi] = (MPUMinMax[axi][0]+MPUMinMax[axi][1])/2;
		}
		
		dataSamples = new ArrayList<List<Double>>();
		
		// populate "dataSamples"'s inner lists
		for (int i = 0; i < 10; i++) {
			List<Double> axis = new ArrayList<Double>();
			dataSamples.add(axis);
		}

		// Populate acceleration x, y, and z
		for(int i = 0; i < finalData.length - 7; i+=7) {
			dataSamples.get(1).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(2).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(3).add((double)(finalData[i]*256)+finalData[i+1]); i+=1;
			
			//Starting at index 5, the counter must be incremented by an extra 6 every 126 bytes to account for the magnetometer data every 10th time index 
			if((i-5)%126==0) i+=6;		
		}

		// Populate gyroscope x, y, and z
		for (int i = 6; i < finalData.length - 7; i+=7) {
			dataSamples.get(4).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(5).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(6).add((double)(finalData[i]*256)+finalData[i+1]); i+=1;

			//Starting at index 11, the counter must be incremented by an extra 6 every 126 bytes to account for the magnetometer data every 10th time index
			if((i-11)%126==0) i+=6;	
		}
		
		// Populate magnetometer x, y, and z
		for (int i = 12; i < finalData.length - 121; i+=121) {
			dataSamples.get(7).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(8).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(9).add((double)(finalData[i]*256)+finalData[i+1]); i+=1;		
		}
		
		// TODO may try to implement use of timer0 in future
		List<Double> timeAxis = new ArrayList<Double>();
		List<Double> magTimeAxis = new ArrayList<Double>();

		// index of dataSamples list is arbitrary; anything other than mag data will work
		for (int i = 0; i < dataSamples.get(1).size(); i++) {

			timeAxis.add(new Double(i) / sampleRate);

			//for use with CSV writing
			dataSamples.get(0).add(new Double(i) / sampleRate);

			// since the magnetometer runs at 96 sps compared to 960,
			// it must have a separate time axis for its data set(s)
			magTimeAxis.add(new Double(i) / magSampleRate); 			
		}
		
		createAxisDataSeries(dataSamples, testParameters, mpuOffsets);

	}
	
	/**
	 * Creates a GenericTest using dataSamples 2D List.
	 * Used for creating GenericTest from CSVHandler or DataOrganizer.
	 * @param testParameters - array of test parameters
	 * @param dataSamples - 2D Array of data from 9 raw axes (and time(0))
	 */
	public GenericTest(List<List<Double>> dataSamples, ArrayList<Integer> testParameters) {

		// only 3/9 indices are used (Accel X/Y/Z => 0/1/2)
		// kept at length of 9 to match # of DOFs (Gyro & Mag)
		int[] mpuOffsets = new int[9];
		
		for(int i = 0; i < 3; i++) {
			// populate MPU offsets by taking the avg of min and max
			// Currently used for acceleration calculations only
			mpuOffsets[i] = (testParameters.get(i+13)+testParameters.get(i+14))/2;
		} 

		createAxisDataSeries(dataSamples, testParameters, mpuOffsets);

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
		setDefaultAxes(new AxisType[] { AxisType.AccelX });

		int sampleRate = testParameters.get(7);
		int magSampleRate = testParameters.get(8);
		int accelSensitivity = testParameters.get(9);
		int gyroSensitivity = testParameters.get(10);

		// TODO may try to implement use of timer0 in future
		List<Double> timeAxis = new ArrayList<Double>();
		List<Double> magTimeAxis = new ArrayList<Double>();

		// using number of accelx samples as proxy for total number of samples
		for (int i = 0; i < dataSamples.get(1).size(); i++) {

			timeAxis.add(new Double(i) / sampleRate);

			//populate to avoid complications from this being null for now
			dataSamples.get(0).add(new Double(i) / sampleRate);

			// since the magnetometer runs at 96 sps compared to 960,
			// it must have a separate time axis for its data set(s)
			magTimeAxis.add(new Double(i) / magSampleRate); 			
		}
		
		// initialize axis data series array
		axes = new AxisDataSeries[AxisType.values().length];

		// loops so that X=0, Y=1, Z=2
		for (int i = 0; i < 3; i++) {

			// acceleration (NATIVE ACCELEROMETER MEASUREMENT)
			axes[i] = new AxisDataSeries(timeAxis, dataSamples.get(i+1), AxisType.valueOf(i), mpuOffsets, accelSensitivity, sampleRate);

			// velocity
			axes[i+4] = new AxisDataSeries(timeAxis, axes[i].integrate(), AxisType.valueOf(i+4), false, sampleRate);

			// displacement
			axes[i+8] = new AxisDataSeries(timeAxis, axes[i+4].integrate(), AxisType.valueOf(i+8), false, sampleRate);

			// angular velocity (NATIVE GYROSCOPE MEASUREMENT)
			axes[i+16] = new AxisDataSeries(timeAxis, dataSamples.get(i+4), AxisType.valueOf(i+16), gyroSensitivity, sampleRate);

			// angular acceleration
			axes[i+12] = new AxisDataSeries(timeAxis, axes[i+16].differentiate(), AxisType.valueOf(i+12), false, sampleRate);	

			// angular displacement
			axes[i+20] = new AxisDataSeries(timeAxis, axes[i+16].integrate(), AxisType.valueOf(i+20), false, sampleRate);

			// magnetometer (NATIVE MEASUREMENT)
			axes[i+24] = new AxisDataSeries(magTimeAxis, dataSamples.get(i+7), AxisType.valueOf(i+24), true, magSampleRate);

		}

		// Creates magnitude data sets
		for (int i = 0; i < AxisType.values().length; i+=4) {

			// "axes[magnitude] = new AxisDataSeries(axes[X], axes[Y], axes[Z], AxisType.valueOf(magnitude))"
			axes[i+3] = new AxisDataSeries(axes[i], axes[i+1], axes[i+2], AxisType.valueOf(i+3));

		}

	}
	
	/**
	 * Retrieves an AxisDataSeries for graphing.
	 * @param axis the {@link com.bioforceanalytics.dashboard.AxisType AxisType} to retrieve
	 * @return the AxisDataSeries selected
	 */
	public AxisDataSeries getAxis(AxisType axis) {
		return axes[axis.getValue()];
	}
	
	/**
	 * Returns raw data samples organized from SerialComm in the appropriate format for CSV.
	 * @return a 2D ArrayList of raw data samples for each sensor
	 */
	public List<List<Double>> getDataSamples() {
		return dataSamples;
	}

	public void setupExperimentPanel(ExperimentPanel panel) {
		panel.setExperimentName("Generic Test");
		panel.applyParams();
	}

	public void setDefaultAxes(AxisType[] axes) {
		defaultAxes = axes;
	}
	
	public AxisType[] getDefaultAxes() {
		return defaultAxes;
	}

	public void setGraphTitle(String title) {
		graphTitle = title;
	}

	public String getGraphTitle() {
		return graphTitle;
	}
				
}
