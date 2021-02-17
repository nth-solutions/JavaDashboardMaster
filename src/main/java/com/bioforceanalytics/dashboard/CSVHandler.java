package com.bioforceanalytics.dashboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

/**
 * General-purpose class used for reading and writing data to CSV files.
 * Mainly used by the BioForce Graph for reading saved test data.
 */
public class CSVHandler {
	
	private static final Logger logger = LogController.start();

	/**
	 * Writes test parameters to a CSVP file.
	 * @param testParameters the list of test parameters
	 * @param settings the {@link com.bioforceanalytics.dashboard.Settings Settings} object used to store parameters such as save location path
	 * @param nameOfTest the name used in the created CSV file
	 * @param MPUMinMax the array of offsets applied to all acceleration calculations
	 */
	public static void writeCSVP(ArrayList<Integer> testParameters, String nameOfTest, int[][] MPUMinMax) throws FileNotFoundException {
	
		// only 3/9 indices are used (Accel X/Y/Z => 0/1/2)
		// kept at length of 9 to match # of DOFs (Gyro & Mag)
		int[] mpuOffsets = new int[9];

		// ensures that a NullPointerException isn't thrown later in GT
		if (MPUMinMax == null) {
			logger.warn("MPUMinMax offsets null, filling with 0s...");
			MPUMinMax = new int[][] {{0, 0}, {0, 0}, {0, 0}};
		}

		// populate MPU offsets by taking the avg of min and max
		// Currently used for acceleration calculations only
		for (int axi = 0; axi < MPUMinMax.length; axi++) {
			mpuOffsets[axi] = (MPUMinMax[axi][0]+MPUMinMax[axi][1])/2;
		}

		writeCSVP(testParameters, nameOfTest, mpuOffsets);

	}

	/**
	 * Writes test parameters to a CSVP file.
	 * @param testParameters the list of test parameters
	 * @param settings the {@link com.bioforceanalytics.dashboard.Settings Settings} object used to store parameters such as save location path
	 * @param nameOfTest the name used in the created CSV file
	 * @param mpuOffsets the array of offsets applied to all acceleration calculations
	 */
	public static void writeCSVP(ArrayList<Integer> testParameters, String nameOfTest, int[] mpuOffsets) throws FileNotFoundException {

		// retrieve the directory to write CSV/CSVP files to
		String testDirPath = Settings.get("CSVSaveLocation");

		// create new file in CSV Directory, file extension is .CSVP
		PrintWriter dataFile = new PrintWriter(testDirPath + "/" + nameOfTest); 

		// write all parameters to the file
		for(int i = 0; i < testParameters.size(); i++) { 
			dataFile.println(testParameters.get(i).toString());
		}
		
		for (int i = 0; i < 9; i++) {
			// write mpuOffsets values to file
			//
			// (this works because MPUMinMax is averaged to get mpuOffsets)
			// TODO this is kind of a hack, fix this at some point
			dataFile.println(mpuOffsets[i]);
			dataFile.println(mpuOffsets[i]);
		}
		
		dataFile.close();
		
	}
	
	/**
	 * Writes test data from a specified GenericTest to a CSV.
	 * Wrapper method that disables "Sign Data" and "Label Data" options.
	 * @param g the GenericTest to read test data from
	 * @param testName the name used in the created CSV file
	 * @throws FileNotFoundException
	 */
	public static void writeCSV(GenericTest g, String testName) throws FileNotFoundException {
		writeCSV(g, testName, false, false);
	}

	/**
	 * Writes test data from a specified GenericTest to a CSV.
	 * @param g the GenericTest to read test data from
	 * @param testName the name used in the created CSV file
	 * @param signData indicates whether to sign test data
	 * @param labelData indicates whether to include a row header with axis labels
	 * @throws FileNotFoundException 
	 */
	public static void writeCSV(GenericTest g, String testName, boolean signData, boolean labelData) throws FileNotFoundException { 														
	
		StringBuilder builder = new StringBuilder();
		
		// add row header with 9DOF axis labels
		if (labelData) {
			builder.append("AccelX,AccelY,AccelZ,GyroX,GyroY,GyroZ,MagX,MagY,MagZ\n");
		}

		// this currently omits the last time instance of all three sensors due to potential out of bounds and alignment issues
		for (int i = 0; i < g.getDataSamples().get(1).size()-1; i++) {

			// populate accel and gyro data points 
			for (int j = 1; j < 7; j++) {

				double sample = g.getDataSamples().get(j).get(i);

				// convert unsigned -> signed if "Signed Data" was enabled
				sample = signData && sample > 32767 ? sample-65535 : sample;

				builder.append(sample);
				builder.append(",");
			}

			// populate mag data points
			if ((i%10==0) && ((i/10) < g.getDataSamples().get(7).size())) {
				for(int j = 7; j < 10; j ++) {

					double sample = g.getDataSamples().get(j).get(i/10);
				
					// convert unsigned -> signed if "Signed Data" was enabled
					sample = signData && sample > 32767 ? sample-65535 : sample;

					builder.append(sample);
					builder.append(",");
				}
			}

			builder.append("\n");
		}

		String testDir = Settings.get("CSVSaveLocation");
		PrintWriter writer = new PrintWriter(new File(testDir + "/" + testName));

		// write the string data to the file
		writer.write(builder.toString());

		// close the PrintWriter
		writer.close(); 
		
	}
	
	/**
	 * Reads test parameters from a given CSVP file.
	 * 
	 * @param CSVPFilePath the location of the CSVP file
	 * @return ArrayList of test parameters read from CSVP
	 */
	public static ArrayList<Integer> readCSVP(String CSVPFilePath) throws IOException, NumberFormatException {

		logger.info("Importing test parameters from '" + CSVPFilePath + "'...");

		// Reader for reading from the file
		BufferedReader CSVPFile = null; 

		String lineText = "";

		// Instantiate the testParameters object. 
		ArrayList<Integer> testParameters = new ArrayList<Integer>(); 
		
		// Open the file for reading
		CSVPFile = new BufferedReader(new FileReader(CSVPFilePath)); 

		//There are 19 test parameters counting MPU Min/Max values
		for (int i = 0; i < 19; i++) { 
			lineText = CSVPFile.readLine();
			// Parse as an int and add to test params
			testParameters.add(testParameters.size(), Integer.parseInt(lineText)); 
		}

		CSVPFile.close();
		
		return testParameters;

	}
	
	/**
	 * Reads data samples from a given CSV file.
	 * <p>
	 * <i>NOTE: The time axis (index 0) is left unpopulated to avoid having to pass
	 * in an additional parameter (sampleRate via testParameters).</i>
	 * </p>
	 * 
	 * @param CSVFilePath    the location of the CSV file
	 * @param testParameters the test parameters from the associated CSVP file (see
	 *                       {@link #writeCSVP})
	 * @return 2D list of 9 axes of raw data (already converted from bytes) and one
	 *         axis of time.
	 * @throws IOException
	 */
	public static List<List<Double>> readCSV(String CSVFilePath) throws IOException, NumberFormatException {

		logger.info("Importing test data from '" + CSVFilePath + "'...");
		
		List<List<Double>> dataSamples = new ArrayList<List<Double>>();

		// populate "dataSamples"'s inner lists
		for (int i = 0; i < 10; i++) {
			List<Double> axis = new ArrayList<Double>();
			dataSamples.add(axis);
		}

		BufferedReader br = new BufferedReader(new FileReader(CSVFilePath));

		String line = "";

		while ((line = br.readLine()) != null) {
			
			// splits each line into an array of samples for each axis
			String[] lineArr = line.split(",");

			for (int i = 0; i < lineArr.length; i++) {
			
				// parse characters from CSV to doubles
				dataSamples.get(i+1).add(Double.parseDouble(lineArr[i]));

			}
		}

		// close file reader
		br.close();
		
		return dataSamples;

	}
	
}
