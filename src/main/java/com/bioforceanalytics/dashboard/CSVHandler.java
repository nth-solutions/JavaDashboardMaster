package com.bioforceanalytics.dashboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * General-purpose class used for reading and writing data to CSV files.
 * Mainly used by the Data Analysis Graph for reading saved test data.
 */
public class CSVHandler {
	
	private static final Logger logger = LogManager.getLogger();

	/**
	 * Writes test parameters to a CSVP file.
	 * @param testParameters the list of test parameters
	 * @param settings the {@link com.bioforceanalytics.dashboard.Settings Settings} object used to store parameters such as save location path
	 * @param nameOfTest the name used in the created CSV file
	 * @param MPUMinMax the array of offsets applied to all acceleration calculations
	 */
	public void writeCSVP(ArrayList<Integer> testParameters, Settings settings, String nameOfTest, int[][] MPUMinMax) throws FileNotFoundException {
		
		settings.loadConfigFile();

		// pull up the Directory to write CSV/CSVP files to
		String CSVPath = settings.getKeyVal("CSVSaveLocation");

		String testDirPath = settings.getKeyVal("CSVSaveLocation");
		File testDir = new File(testDirPath);

		// if directory doesn't exist, create it
		if (!testDir.exists()) testDir.mkdirs();

		// create new file in CSV Directory, file extension is .CSVP
		PrintWriter dataFile = new PrintWriter(CSVPath + "/" + nameOfTest); 

		for(int i = 0; i < testParameters.size(); i++) { 
			// write all parameters to the file
			dataFile.println(testParameters.get(i).toString());
		}

		if (MPUMinMax == null) {
			logger.warn("MPU offsets null when writing CSVP, filling with 0s...");
			MPUMinMax = new int[][] {{0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}};
		}
		
		for (int i = 0; i < 9; i++) {
			// write MPUMinMax values to file
			dataFile.println(MPUMinMax[i][0]);
			dataFile.println(MPUMinMax[i][1]);
		}
		
		dataFile.close();
		
	}
	
	/**
	 * CSV Writing method modified from dataOrganizer.
	 * @param g GenericTest object to read test data from
	 * @param settings the {@link com.bioforceanalytics.dashboard.Settings Settings} object used to store parameters such as save location path
	 * @param nameOfTest the name used in the created CSV file
	 */
	public void writeCSV(GenericTest g, Settings settings, String nameOfTest) throws FileNotFoundException { 														
	
		StringBuilder builder = new StringBuilder();
			
		// this currently omits the last time instance of all three sensors due to potential out of bounds and alignment issues
		for (int i = 0; i < g.getDataSamples().get(1).size()-1; i++) {

			// populate accel and gyro data points 
			for (int j = 1; j < 7; j++) {
				builder.append(g.getDataSamples().get(j).get(i));
				builder.append(",");
			}

			// populate mag data points
			if ((i%10==0) && ((i/10) < g.getDataSamples().get(7).size())) {
				for(int j = 7; j < 10; j ++) {
					builder.append(g.getDataSamples().get(j).get(i/10));
					builder.append(",");
				}
			}

			builder.append("\n");
		}

		String testDirPath = settings.getKeyVal("CSVSaveLocation");
		File testDir = new File(testDirPath);

		// if directory doesn't exist, create it
		if (!testDir.exists()) testDir.mkdirs();

		PrintWriter writer = new PrintWriter(new File(testDir + "/" + nameOfTest));

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
	public ArrayList<Integer> readCSVP(String CSVPFilePath) throws IOException, NumberFormatException {

		logger.info("Importing test parameters from '" + CSVPFilePath + "'...");

		// Need to load keys from settings file. This tells us where CSVs are stored
		Settings settings = new Settings();
		settings.loadConfigFile();

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
	public List<List<Double>> readCSV(String CSVFilePath) throws IOException, NumberFormatException {

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
	
	/**
	 * <b>FOR TESTING PURPOSES ONLY - NOT FOR USE WITH GRAPHING APPLICATION</b> This
	 * method writes all 28 axes of a GenericTest to a CSV - this is not the CSV
	 * format that the graph accepts This method was only written to evaluate the
	 * GenericTest data in Excel.
	 * 
	 * @deprecated not for use in Data Analysis Graph
	 * @param g          GenericTest object to read test data from
	 * @param nameOfTest the name used in the created CSV file
	 * @throws FileNotFoundException
	 */
	@Deprecated
	public void writeGenericTestAxestoCSV(GenericTest g, String nameOfTest) throws FileNotFoundException {
		
		Settings settings = new Settings();
		StringBuilder builder = new StringBuilder();
		PrintWriter DataFile;
			
		//iterates through data points in the series (i is the line index)
		for (int i = 0; i < g.getAxis(AxisType.AccelX).getSamples().size(); i++) {

			//iterates through axes (j is the column index)
			for (int j = 0; j < AxisType.values().length; j++) {
				//controls for shorter mag series
				if (!(j > 23 && i > g.getAxis(AxisType.valueOf(j)).getSamples().size()-1)) {
					builder.append(g.getAxis(AxisType.valueOf(j)).getSamples().get(i));
					builder.append(",");
				}
			}
			builder.append("\n");
		}
			
		String testDirPath = settings.getKeyVal("CSVSaveLocation");
		File testDir = new File(testDirPath);

		// if directory doesn't exist, create it
		if (!testDir.exists()) testDir.mkdirs();

		DataFile = new PrintWriter(new File((FileSystemView.getFileSystemView().getDefaultDirectory().toString() + "/" + nameOfTest)));

		// writes the string buffer to the .CSV creating the file
		DataFile.write(builder.toString());
		// close the .CSV
		DataFile.close();

	}
	
}




