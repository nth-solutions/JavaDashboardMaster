package dataorganizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.filechooser.FileSystemView;

public class DataOrganizer {

	private List<List<Double>> dataSamples;
	private List<List<Double>> signedDataSamples;
	private List<List<Double>> normalizedDataSamples;
	private List<Integer> testParameters;
	List<Double> dofTime;
	private String nameOfTest;
	private int sampleRate;
	private int magSampleRate;
	private int magInterval;
	private int accelSensitivity;
	private int gyroSensitivity;
	private int magSensitivity;
	private double lengthOfTest;
	private int numDof = 9;
	private String dataSourceID;
	private int sourceID;
	private String moduleSerialID;
	Settings settings = new Settings();

	private int delayAfterStart;
	private int lineNum;
	private double max = 0;
	private double min = 0;

	public DataOrganizer(ArrayList<Integer> testParameters, String testName) {
		this.nameOfTest = testName;
		this.testParameters = testParameters;
		delayAfterStart = testParameters.get(2);
		lengthOfTest = testParameters.get(6);
		sampleRate = testParameters.get(7);
		magSampleRate = testParameters.get(8);
		accelSensitivity = testParameters.get(9);
		gyroSensitivity = testParameters.get(10);
		magSensitivity = 4800;
		magInterval = sampleRate / magSampleRate;
	}

	public DataOrganizer() {
		numDof = 9;
		sampleRate = 960;
		lengthOfTest = 6.9;
	}

	public void setSourceID(String dataSourceID, int sourceID) {
		this.sourceID = sourceID;
		this.dataSourceID = dataSourceID;
	}
	
	public int getSourceID() {
		return this.sourceID;
	}
	
	public String getSourceId() {
		return this.dataSourceID;
	}
	
	public String getSerialID() {
		return this.moduleSerialID;
	}
	public void setSerialID(String ID) {
		this.moduleSerialID = ID;
	}
	
	
	public List<List<Double>> createDataSmpsRawData(int[] data) {

		boolean endCondition = false;
		int byteCounter = 0;

		dataSamples = new ArrayList<List<Double>>();
		for (int dof = 0; dof < 10; dof++) {
			List<Double> temp = new ArrayList<Double>();
			dataSamples.add(dof, temp);
		}

		if (delayAfterStart < 0) {
			int delayAdditionalLineNums = (int) Math.round(((double) delayAfterStart / -1000.0) * (double) sampleRate);

			for (int i = 0; i < delayAdditionalLineNums; i++) {
				dataSamples.get(0).add((double) i / (double) sampleRate);
				if (magInterval == 10 && i % 10 == 0) {
					for (int dof9 = 1; dof9 < 10; dof9++) {
						dataSamples.get(dof9).add(0.0);
					}
				} else if (magInterval == 10 && i % 10 != 0) {
					for (int dof6 = 1; dof6 < 7; dof6++) {
						dataSamples.get(dof6).add(0.0);
					}
				} else if (magInterval == 1) {
					for (int dof9 = 1; dof9 < 10; dof9++) {
						dataSamples.get(dof9).add(0.0);
					}
				}
			}
			lineNum = delayAdditionalLineNums;
		} else {
			lineNum = 0;
		}

		// System.out.println(data.length);
		// System.out.println(dataSmps);

		while (!endCondition) {
			for (int i = 0; i < magInterval && !endCondition; i++) {
				dataSamples.get(0).add(lineNum, (double) lineNum / (double) sampleRate); // adds the time to the first column

				if (i == 0) {
					for (int dof9 = 1; dof9 < 10; dof9++) { // starts at 1 because 0 is time
						if (data[byteCounter] == -1 || data[byteCounter + 1] == -1) {
							endCondition = true;
							break;
						}
						// System.out.println((data[byteCounter] * 256) + data[byteCounter + 1]);
						dataSamples.get(dof9).add(lineNum, (double) ((data[byteCounter] * 256) + data[byteCounter + 1]));
						byteCounter += 2;
					}
				} else {
					for (int dof6 = 1; dof6 < 10; dof6++) {
						if (data[byteCounter] == -1 || data[byteCounter + 1] == -1) {
							endCondition = true;
							break;
						}
						if (dof6 < 7) {
							dataSamples.get(dof6).add(lineNum, (double) (data[byteCounter] * 256) + data[byteCounter + 1]);
							byteCounter += 2;
						} else {
							dataSamples.get(dof6).add(lineNum, null);
						}
					}
				}
				lineNum++;
				// System.out.println(byteCounter - tempByteCounter);
			}

		}
		lineNum--;
		lengthOfTest = (double) lineNum / (double) sampleRate;


		return dataSamples;
	}

	public List<List<Double>> getSignedData() {
		signedDataSamples = new ArrayList<List<Double>>();
		for (int dof = 0; dof < 10; dof++) {
			List<Double> temp = new ArrayList<Double>();
			signedDataSamples.add(dof, temp);
		}

		signedDataSamples.get(0).addAll(dataSamples.get(0));

		for (int smp = 0; smp < lineNum; smp++) {
			for (int dof = 1; dof < 10; dof++) {
				if (dof < 4) {
					double curVal = dataSamples.get(dof).get(smp);
					if (curVal > 32768) {
						curVal -= 65535;
					}
					curVal = (curVal * accelSensitivity) / 32768;
					signedDataSamples.get(dof).add(smp, curVal);
				} 
				else if (dof < 7) {
					double curVal = dataSamples.get(dof).get(smp);
					if (curVal > 32768) {
						curVal -= 65535;
					}
					curVal = (curVal * gyroSensitivity) / 32768;
					signedDataSamples.get(dof).add(smp, curVal);
				} 
				else {
					signedDataSamples.get(dof).add(smp, dataSamples.get(dof).get(smp));
					/*if(curVal != null) {
						if (curVal > 32768) {
							curVal -= 65535;
						}
						curVal = (curVal * magSensitivity) / 32768;*/
					//signedDataSamples.get(dof).add(smp, curVal);
					//	}
				}
			}
		}

		// System.out.println("LineNum: " + lineNum);
		// System.out.println("Length of test: " + lengthOfTest);
		// System.out.println("Size of test: " + dataSmps.get(0).size());
		return signedDataSamples;
	}

	public void getCSVSignedData() {
		signedDataSamples = new ArrayList<List<Double>>();
		for (int dof = 0; dof < 10; dof++) {
			List<Double> temp = new ArrayList<Double>();
			signedDataSamples.add(dof, temp);
		}


		for(int dof = 0; dof < dataSamples.size();dof++) {
			for(Double smp: dataSamples.get(dof)) {
				if(dof < 4) {
					if (smp > 32768) {
						smp -= 65535;
					}
					smp = (smp * accelSensitivity) / 32768;
					signedDataSamples.get(dof).add(smp);
				}if(dof < 7) {
					if (smp > 32768) {
						smp -= 65535;
					}
					smp = (smp * gyroSensitivity) / 32768;
					signedDataSamples.get(dof).add(smp);
				}else
					signedDataSamples.get(dof).add(smp);
			}
		}
	}


	public List<List<Double>> getNormalizedDataRollingBlock(){
		normalizedDataSamples = new ArrayList<List<Double>>();
		for (int smp = 0; smp < lineNum; smp++) {
			for (int dof = 1; dof < 10; dof++) {


			}
		}

		return normalizedDataSamples;

	}

	/*
	 * Creates new .CSVP file for storing the test parameters of a given test, We need this alongside the CSV file for graphing purposes.
	 */
	public int createCSVP() {
		settings.loadConfigFile();
		String CSVPath = settings.getKeyVal("CSVSaveLocation"); //Pull up the directory of chosen as CSV location files
		PrintWriter dataFile = null;
		try {
			dataFile = new PrintWriter(CSVPath + File.separator + nameOfTest + "p"); //Create new file in CSVDirectory, file extension is .csvp 
			//System.out.println(CSVPath + File.separator + nameOfTest + "p");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		for(int i = 0; i < testParameters.size(); i++) { //Write all parameters to the file. We really only needed like 3 at the time of writing but this was easier and probably more effective in the future.
			dataFile.println(testParameters.get(i).toString());
		}
		dataFile.close();
		return 0;
	}

	/*
	 * Read the test parameters from the file we stored them in. This file will be in the same directory as the .csv and has the 
	 * extension .csvp (extension is misleading. They are really separated by newlines, which is actually data inefficient I realize as I write this. \cr\lf is two bytes and a comma is one. no biggie right? )
	 */
	public int readAndSetTestParameters(String pathToFile) {
		File f = new File(pathToFile); //Create a file object of the .csvp, just to get the file name. I was told this way is more efficient than substrings. Its definitely shorter than string manipulation. Plus I would have had to taken other OS file separators into account
		this.nameOfTest = f.getName(); //set the name of the test
		settings.loadConfigFile(); //need to load keys from settings file. This tells us where CSV's are stored
		BufferedReader CSVPFile = null; //Reader for reading from the file
		String lineText = "";
		testParameters = new ArrayList<Integer>(); //Reinstantiate the testParameters object. Just for a quick while loop. 
		
		try {
			CSVPFile = new BufferedReader(new FileReader(pathToFile)); //open the file for reading
		} catch (FileNotFoundException e) {
			return -1;			//File Permissions error
		}

		try {
			while((lineText = CSVPFile.readLine()) != null){ //Read until EOF
				testParameters.add(testParameters.size(), Integer.parseInt(lineText)); //Parse as an int and add to test params
			}
		} catch (NumberFormatException e) {
			//NFE really shouldn't happen but it would mean that the file is corrupt.
			try {
				CSVPFile.close(); //Try to close the file
			} catch (IOException e1) {
				// I guess we can't close the corrupt file either.
			}
			return -2;	//Corrupt .CSVP
		} catch( IOException e) {
			try {
				CSVPFile.close();
			} catch (IOException e1) {
				// I guess we can't close the corrupt file either.
			}
			return -3; //Permissions error as well.
		}

		
		/*
		 * Just set these variables because thats where we reference them from most of the time.
		 */
		delayAfterStart = testParameters.get(2);
		lengthOfTest = testParameters.get(6);
		sampleRate = testParameters.get(7);
		magSampleRate = testParameters.get(8);
		accelSensitivity = testParameters.get(9);
		gyroSensitivity = testParameters.get(10);
		magSensitivity = 4800; //TODO: not constant

		try {
			CSVPFile.close();
		} catch (IOException e1) {
			// I guess we can't close the file either.
		}
		
		return 0;
	}

	public int createCSV(boolean labelData, boolean signedData) {
		List<List<Double>> modifiedDataSmps = new ArrayList<List<Double>>();

		if(!signedData)
			modifiedDataSmps = dataSamples;
		else
			modifiedDataSmps = signedDataSamples;

		System.out.println(modifiedDataSmps.get(0).size());
		
		StringBuilder builder = new StringBuilder();
		PrintWriter DataFile = null;
		if (!labelData) {
			for (int smp = 0; smp < lineNum - 1; smp++) {
				for (int dof = 1; dof <= numDof; dof++) {
					if (modifiedDataSmps.get(dof).get(smp) != null) {
						builder.append(modifiedDataSmps.get(dof).get(smp));
						builder.append(",");

					}
				}
				builder.append("\n");
			}
		} else {
			builder.append("t,AccelX,AccelY,AccelZ,GyroX,GyroY,GyroZ,MagX,MagY,MagZ,\n");
			for (int smp = 0; smp < lineNum - 1; smp++) {
				for (int dof = 0; dof <= numDof; dof++) {
					if (modifiedDataSmps.get(dof).get(smp) != null) {
						builder.append(modifiedDataSmps.get(dof).get(smp));
						builder.append(",");

					}
				}
				builder.append("\n");
			}
		}

		String fileOutputDirectory = settings.getKeyVal("CSVSaveLocation");

		try {
			if (fileOutputDirectory != null) {
				DataFile = new PrintWriter(new File(fileOutputDirectory + File.separator + nameOfTest));
			} else {
				DataFile = new PrintWriter(new File((FileSystemView.getFileSystemView() // Creates .CSV file in default
						// directory which is documents
						.getDefaultDirectory().toString() + File.separator + nameOfTest)));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
		DataFile.write(builder.toString()); // writes the string buffer to the .CSV creating the file
		DataFile.close(); // close the .CSV
		return 0;

	}


	public void createDataSamplesFromCSV(String CSVFilePath) { 
		readAndSetTestParameters(CSVFilePath + 'p'); //CSVP file. Should be kept with CSV File
		dataSamples = new ArrayList<List<Double>>(9);	

		double interval = (1.0 / sampleRate);
		int numSample = (int) (sampleRate * lengthOfTest);

		for (int dof = 0; dof < numDof; dof++) {
			List<Double> temp = new ArrayList<Double>();
			dataSamples.add(temp);
		}

		BufferedReader br = null;

		try {
			String line = "";
			br = new BufferedReader(new FileReader(CSVFilePath));

			while ((line = br.readLine()) != null) {

				String[] sample = line.split(",");

				for (int str = 0; str < sample.length; str++) {
					try {
						dataSamples.get(str).add(Double.parseDouble(sample[str]));
						min = Math.min(Double.parseDouble(sample[str]), min);
						max = Math.max(Double.parseDouble(sample[str]), max);
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						break;
					}
				}
			}
		} catch (FileNotFoundException ex) {
			System.out.println(" NO FIle FOUND");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public List<List<Double>> getDataSamples() {
		return dataSamples;
	}
	
	
	public void setDataSmps(List<List<Double>> dataSmps) {
		this.dataSamples = dataSmps;
	}


	public double getLengthOfTest() {
		this.lengthOfTest = (double)dataSamples.get(0).size()/(double)sampleRate;
		return lengthOfTest;
	}

	public int getNumDof() {
		return numDof;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public String getName() {
		return nameOfTest;
	}

	public void setName(String name) {
		this.nameOfTest = name;
	}

	public List<List<Double>> returnSignedData(){
		return signedDataSamples;
	}

	public List<List<Double>> getByConversionType(int dataConversionType) {
		switch(dataConversionType) {
		case 1: return signedDataSamples;
		case 2: return dataSamples;
		}
		return null;
	}

	public List<Double> getTimeAxis(){
		return dofTime;
	}
	
	public List<List<Double>> getZoomedSeries(int source, double start, double end, int dofNum, int dataConversionType ){
		switch(source) {
			case 0: //Live module data
				return getZoomedSeriesModule(start, end, dofNum, dataConversionType);
			case 1: //File data
				return getZoomedSeriesCSV(start, end, dofNum, dataConversionType);
			default: 
				return null;
		}
	}
	
	
	public List<List<Double>> getZoomedSeriesModule(double start, double end, int dofNum, int dataConversionType) {
		List<List<Double>> modifiedDataSmps = new ArrayList<List<Double>>();
		switch(dataConversionType) {
		case(0): 
			modifiedDataSmps = dataSamples;
		break;
		case(1): 
			modifiedDataSmps = signedDataSamples;
		break;
		}


		int numSamples = (int) Math.round((end - start) * sampleRate);

		double rate = 7000.0 / (double) numSamples;
		double newSps = (sampleRate * rate);
		double modifier = sampleRate / newSps;

		List<List<Double>> dofData = new ArrayList<List<Double>>();

		dofTime = new ArrayList<Double>();
		List<Double> dofAxis = new ArrayList<Double>();

		if (modifier < 1)
			modifier = 1;
		//System.out.println(modifier);
		//System.out.println(start*sampleRate);
		//System.out.println(modifiedDataSmps.get(0).size());
		for (int sample = 0; sample < 7000 && ((start * sampleRate) + sample) < (modifiedDataSmps.get(0).size() - 1); sample++) {
			dofTime.add(sample, modifiedDataSmps.get(0).get((int) ((start * sampleRate) + (int) (sample * modifier))));
		}

		dofData.add(0, dofTime);


		for (int sample = 0; sample < 7000 && ((start * sampleRate) + sample) < (modifiedDataSmps.get(dofNum).size() - 1); sample++) {
			dofAxis.add(sample, modifiedDataSmps.get(dofNum).get((int) ((start * sampleRate) + (int) (sample * modifier))));
		}
		dofData.add(1, dofAxis);

		return dofData;
	}

	public List<List<Double>> getZoomedSeriesCSV(double start, double end, int dofNum, int dataConversionType) {
		dofNum--; //hardcore hack. Not really. I had an off by one error but could only account for it here instead of graph, because our the DataSeries object expects 1-9, whereas we index 0,8. Hence decrease dofnum 1 for indexes here.
		List<List<Double>> modifiedDataSmps = new ArrayList<List<Double>>();
		switch(dataConversionType) { //What type of data. 0 is raw, 1 is signed. We are expecting to expand on this supposedly? thats why its a switch and not a bool
		case(0): 
			modifiedDataSmps = dataSamples;
		break;
		case(1): 
			modifiedDataSmps = signedDataSamples;
		break;
		}

		int numSamples = (int) Math.round((end - start) * sampleRate); //How many samples are we interested in

		double rate = 7000.0 / (double) numSamples; //Whats our rate? Rob made up 7000 and I stole it.
		double newSps = (sampleRate * rate); 
		double modifier = sampleRate / newSps; 

		List<List<Double>> dofData = new ArrayList<List<Double>>();

		dofTime = new ArrayList<Double>();
		dofTime.add(0, 0.0);
		List<Double> dofAxis = new ArrayList<Double>();

		if (modifier < 1) // We cant have more data than we have
			modifier = 1;

		for(int sample = 1; sample < 7000 && sample < modifiedDataSmps.get(0).size(); sample++) {
			dofTime.add(sample, ((1/(double)sampleRate)*((double)sample*modifier)));
		}

		dofData.add(0, dofTime);

		//TODO
		//TODO
		//TODO
		//With tests that sampled mag equally as often as accel/gyro, 9/10 mag samples will be skipped. Temporarily did this because graphs needed spacers where sample rate was not sampled at an equal speed to accel/gyro. Also 80/20 rule. 
		if(dofNum <= 5)
			for (int sample = 0; sample < 7000 && ((start * sampleRate) + sample) < (modifiedDataSmps.get(dofNum).size() - 1); sample++) {
				dofAxis.add(sample, modifiedDataSmps.get(dofNum).get((int) ((start * sampleRate) + (int) (sample*modifier))));
			}
		else if(dofNum > 5)
			for (int sample = 0; sample < 7000 && ((start * sampleRate) + sample) < (modifiedDataSmps.get(dofNum).size() - 1) * 10; sample++) {
				if(sample % 10 == 0)
					dofAxis.add(sample, modifiedDataSmps.get(dofNum).get((int) ((start * sampleRate) + (int) (sample/10))));
				else
					dofAxis.add(sample, null);
			}
		
		dofData.add(1, dofAxis);

		//System.out.println(dofNum +":"+ dofTime.size() +":"+ dofAxis.size());
		//System.out.println(dofNum +":"+ dofTime);
		//System.out.println(dofNum +":"+ dofAxis);

		return dofData;
	}


	public double maxTestValAxis() {
		double max = -32768;

		for(List<Double> column : signedDataSamples){
			for(Double sample : column) {
				if(sample != null)
					if(sample>max)
						max = sample;
			}
		}
		return max;
	}

	public double minTestValAxis() {
		double min = 32768;

		for(List<Double> column: signedDataSamples){
			for(Double sample: column) {
				if(sample != null)
					if(sample<min)
						min = sample;
			}
		}
		return min;
	}
}
