package dataorganizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.filechooser.FileSystemView;

/**
 * This class writes the module data to CSV and and CSVP files after being converted from bitcount
 * constructors can be added
 *
 */

public class CSVWriter {
	
	/**
	 * CSVP writing class copied (more or less) from dataOrganizer
	 * @param testParameters
	 * @param settings
	 * @param nameOfTest
	 * @param MPUMinMax
	 * @return
	 */
	public int writeCSVP(ArrayList<Integer> testParameters, Settings settings, String nameOfTest, int[][]MPUMinMax) {
		
		settings.loadConfigFile();
		//Pull up the directory of chosen as CSV location files
		String CSVPath = settings.getKeyVal("CSVSaveLocation"); 
		PrintWriter dataFile = null;
		try {
			//Create new file in CSV Directory, file extension is .csvp 
			dataFile = new PrintWriter(CSVPath + File.separator + nameOfTest + "p"); 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		for(int i = 0; i < testParameters.size(); i++) { 
			//Write all parameters to the file. We really only needed like 3 at the time of writing but this was easier and probably more effective in the future.
			dataFile.println(testParameters.get(i).toString());
		}

		for(int i = testParameters.size(); i<32; i++){ 
			// This makes literally no sense but for some reason the data can't be graphed unless the csvp has 32 values. After the actual test parameters it doesn't matter what there is but there has to be something. It is unclear when this can be removed.
			dataFile.println(0);
		}
		for(int i = 0; i < 9; i++) {
			dataFile.println(MPUMinMax[i][0]);
			dataFile.println(MPUMinMax[i][1]);
	}
		dataFile.close();
		return 0;
		
	}
	
	
	/**
	 * CSV Writing method modified from dataOrganizer.  Not sure about int return type, might change?
	 * @param g
	 * @param settings
	 * @param nameOfTest
	 * @return
	 */
	public int writeCSV(GenericTest g, Settings settings, String nameOfTest) { 														
		
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
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
		// writes the string buffer to the .CSV creating the file
		DataFile.write(builder.toString());
		// close the .CSV
		DataFile.close(); 
		return 0;
	}

}


