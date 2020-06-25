package dataorganizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

/**
 * This class houses methods handle reading and writing data from/to CSV files
 * Constructors can be added as needed
 */

public class CSVHandler {
	
	/**
	 * CSVP writing class copied (more or less) from dataOrganizer
	 * @param testParameters
	 * @param settings
	 * @param nameOfTest
	 * @param MPUMinMax
	 * @return
	 */
	public void writeCSVP(ArrayList<Integer> testParameters, Settings settings, String nameOfTest, int[][]MPUMinMax) {
		/***********************************How CSVPs are organized******************************************
		 * 
		 * The parameters are listed in the file in the following order.  Here they are aligned with their indices in the testParameters list.
		 * MPU min and max values are added via int[][]MPUMinMax and are not intended to be stored in the testParameters list passed into this constructor.
		 * 
		 * 0. Number of Tests
		 * 1. timer0 
		 * 2. Delay After Start
		 * 3. Battery Timeout Flag
		 * 4. Time Test Flag
		 * 5. Trigger on Release Flag
		 * 6. Test Length
		 * 7. Accel/Gyro Sample Rate
		 * 8. Mag Sample Rate
		 * 9. Accel Sensitivity
		 * 10. Gyro Sensitivity
		 * 11. Accel Filter
		 * 12. Gyro Filter
		 * 13. Accel X Offset Min
		 * 14. Accel X Offset Max
		 * 15. Accel Y Offset Min
		 * 16. Accel Y Offset Max
		 * 17. Accel Z Offset Min
		 * 18. Accel Z Offset Max
		 */
		
		settings.loadConfigFile();
		//Pull up the Directory to write CSV/CSVP files to
		String CSVPath = settings.getKeyVal("CSVSaveLocation"); 
		PrintWriter dataFile = null;
		try {
			//Create new file in CSV Directory, file extension is .CSVP
			dataFile = new PrintWriter(CSVPath + File.separator + nameOfTest + "p"); 
		} 
		catch (FileNotFoundException e) {
		
			e.printStackTrace();
		}
		for(int i = 0; i < testParameters.size(); i++) { 
			//Write all parameters to the file
			dataFile.println(testParameters.get(i).toString());
		}
		
		for(int i = 0; i < 9; i++) {
			//Write MPUMinMax values to file
			dataFile.println(MPUMinMax[i][0]);
			dataFile.println(MPUMinMax[i][1]);
	}
		
		dataFile.close();
		
	}
	
	
	/**
	 * CSV Writing method modified from dataOrganizer.  Not sure about int return type, might change?
	 * @param g
	 * @param settings
	 * @param nameOfTest
	 * @return
	 */
	public void writeCSV(GenericTest g, Settings settings, String nameOfTest) { 														
		
		/*********************************How the CSV is organized******************************************
		 * a = accelerometer
		 * g = gyroscope
		 * m = magnetometer
		 * x,y,z = x,y,z axes
		 * 
		 * Data samples are written chronologically in the following order with all samples in a given row being from the same point in time:
		 * 
		 * 1. ax, ay, az, gx, gy, gz, mx, my, mz
		 * 2. ax, ay, az, gx, gy, gz
		 * 3. ax, ay, az, gx, gy, gz
		 * 4. ax, ay, az, gx, gy, gz
		 * 5. ax, ay, az, gx, gy, gz
		 * 6. ax, ay, az, gx, gy, gz
		 * 7. ax, ay, az, gx, gy, gz
		 * 8. ax, ay, az, gx, gy, gz
		 * 9. ax, ay, az, gx, gy, gz
		 * 10. ax, ay, az, gx, gy, gz
		 * 11. ax, ay, az, gx, gy, gz, mx, my, mx
		 * ...
		 * 
		 * Because the magnetometer has 1/10th the sample rate of the accelerometer and gyroscope, it only has data for every 10th
		 * point on the time axis.
		 */
		
		/*
		 * In this instance, we populate the CSV from GenericTest's 2D "dataSamples" list, which has the following format, with each list (with the exception of time)
		 * corresponding to a column in the CSV.
		 * 
		 * 0 - Time
		 * 1 - Acceleration X
		 * 2 - Acceleration Y
		 * 3 - Acceleration Z
		 * 4 - Gyroscope (Angular Velocity) X
		 * 5 - Gyroscope (Angular Velocity) Y
		 * 6 - Gyroscope (Angular Velocity) Z
		 * 7 - Magnetometer X
		 * 8 - Magnetometer Y
		 * 9 - Magnetometer Z
		 */ 
	
		StringBuilder builder = new StringBuilder();
		PrintWriter DataFile;
			
			//This currently omits the last time instance of all three sensors due to potential out of bounds and alignment issues
			for (int i = 0; i<g.getDataSamples().get(1).size()-1; i++) {
				//populate accel and gyro data points 
				for(int j = 1; j<7; j++) {
					builder.append(g.getDataSamples().get(j).get(i));
					builder.append(",");
					}
				//populate mag data points
				if ((i%10==0)&&((i/10)<g.getDataSamples().get(7).size())) {
					for( int k = 7; k <10; k ++) {
					builder.append(g.getDataSamples().get(k).get(i/10));
					builder.append(",");
					}
				}
				builder.append("\n");
				}
			

		String fileOutputDirectory = settings.getKeyVal("CSVSaveLocation");

		try {
			if (fileOutputDirectory != null) {
				DataFile = new PrintWriter(new File(fileOutputDirectory + File.separator + nameOfTest));
			} 
			else {
				
				DataFile = new PrintWriter(new File((FileSystemView.getFileSystemView().getDefaultDirectory().toString() + File.separator + nameOfTest)));
			}
			// writes the string buffer to the .CSV creating the file
			DataFile.write(builder.toString());
			// close the .CSV
			DataFile.close(); 
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Reads CSVP containing Test Parameters
	 * @param CSVPFilePath
	 * @return ArrayList of Test Parameters read from CSVP
	 */
	public ArrayList<Integer> readCSVP(String CSVPFilePath) {
		
		//Create a .CSVP file
		File f = new File(CSVPFilePath); 
		//Sets the name of the test
		String nameOfTest = f.getName(); 
		
		Settings settings = new Settings();
		//need to load keys from settings file. This tells us where CSV's are stored
		settings.loadConfigFile(); 
		//Reader for reading from the file
		BufferedReader CSVPFile = null; 
		String lineText = "";
		//Instantiate the testParameters object. 
		ArrayList<Integer>testParameters = new ArrayList<Integer>(); 
		
		try {
			//open the file for reading
			CSVPFile = new BufferedReader(new FileReader(CSVPFilePath)); 
		} 
		
		catch (FileNotFoundException e) {
			System.out.println("No file found");
			e.printStackTrace();
		}
		
		try {
			//There are 19 test parameters counting MPU Min/Max values
			for (int i = 0; i < 19; i++) { 
				lineText = CSVPFile.readLine();
				//Parse as an int and add to test params
				testParameters.add(testParameters.size(), Integer.parseInt(lineText)); 
			}
		}
		
		catch (NumberFormatException e) {
			//NFE really shouldn't happen but it would mean that the file is corrupt.
			e.printStackTrace();
			
			try {
				//Try to close the file
				CSVPFile.close(); 
			} 
			catch (IOException e1) {
				//Would occur if file can't be closed
				e1.printStackTrace();	
			}	
		} 
		
		catch(IOException e) {
			e.printStackTrace();
			
			try {
				//try to close if there is an IO Exception
				CSVPFile.close();
			} 
			
			catch (IOException e1) {
				//if file can't be closed
				e1.printStackTrace();
			}
		}

		try {
			//Try to close the file
			CSVPFile.close();
		} 
		
		catch (IOException e1) {
			//if file can't be closed
			e1.printStackTrace();
		}	
		
	return testParameters;
	}

	
	/**
	 * Reads CSV containing raw test data
	 * @param CSVFilePath
	 * @param testParameters
	 * @return 2D list of 9 axes of raw data (already converted from bytes) and one axis of time - NOTE: dataSamples.get(0) (time axis) is left unpopulated
	 * to avoid having to pass in an additional parameter (sampleRate via testParameters)
	 */
	public List<List<Double>> readCSV(String CSVFilePath) {
		
		List<List<Double>>dataSamples = new ArrayList<List<Double>>();
		// populate "dataSamples"'s inner lists
		for (int i = 0; i < 10; i++) {
			List<Double> axis = new ArrayList<Double>();
			dataSamples.add(axis);
		}

		BufferedReader br = null;

		try {
			String line = "";
			br = new BufferedReader(new FileReader(CSVFilePath));

			while ((line = br.readLine()) != null) {
				
				//splits each line into an array of samples for each axis
				String[] lineArr = line.split(",");
					for (int i = 0; i < lineArr.length; i++) {
					
						try {
						dataSamples.get(i+1).add(Double.parseDouble(lineArr[i]));
						} 
						catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						break;
						}	
					}
			}
		} 
		catch (FileNotFoundException ex) {
			
			System.out.println("NO FILE FOUND");
		} 
		
		catch (IOException e) {
			
			e.printStackTrace();	
		} 
		
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return dataSamples;
	}
	
	
	/**
	 * FOR TESTING PURPOSES ONLY - NOT FOR USE WITH GRAPHING APPLICATION
	 * This method writes all 28 axes of a GenericTest to a CSV - this is not the CSV format that the graph accepts
	 * This method was only written to evaluate the manipulated data in Excel
	 * @param g
	 */
	public void writeGenericTestAxestoCSV(GenericTest g, String nameOfTest) {
		Settings settings = new Settings();
		StringBuilder builder = new StringBuilder();
		PrintWriter DataFile;
			
			//iterates through data points in the series (i is the line index)
			for (int i = 0; i<g.getAxis(AxisType.valueOf(0)).getSamples().size(); i++) {
				//iterates through axes (j is the column index)
				for(int j = 0; j<28; j++) {
					//controls for shorter mag series
					if (!(j > 23 && i > g.getAxis(AxisType.valueOf(j)).getSamples().size()-1)) {
					builder.append(g.getAxis(AxisType.valueOf(j)).getSamples().get(i));
					builder.append(",");
					}
					}
				builder.append("\n");
				}
			
		String fileOutputDirectory = settings.getKeyVal("CSVSaveLocation");

		try {
			if (fileOutputDirectory != null) {
				DataFile = new PrintWriter(new File(fileOutputDirectory + File.separator + nameOfTest));
			} 
			else {
				
				DataFile = new PrintWriter(new File((FileSystemView.getFileSystemView().getDefaultDirectory().toString() + File.separator + nameOfTest)));
			}
			// writes the string buffer to the .CSV creating the file
			DataFile.write(builder.toString());
			// close the .CSV
			DataFile.close(); 
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	
}


