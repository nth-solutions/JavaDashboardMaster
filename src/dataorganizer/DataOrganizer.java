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

	private List<List<Double>> dataSmps;
	private int sampleRate;
	private int magSampleRate;
	private int magInterval;
	private int accelSensitivity;
	private int gyroSensitivity;
	private double lengthOfTest;
	private int numDof;
	private boolean signedData;
	private int delayAfterStart;
	private int lineNum;
	private double max = 0;
	private double min = 0;
	
	
	public DataOrganizer(ArrayList<Integer> testParameters, boolean signedData, int numDof) {
		
		this.numDof = numDof;
		this.signedData = signedData;
		
		
		delayAfterStart = testParameters.get(2);
		lengthOfTest = testParameters.get(6);
		sampleRate = testParameters.get(7);
		magSampleRate = testParameters.get(8);
		accelSensitivity = testParameters.get(9);
		gyroSensitivity = testParameters.get(10);
		
		magInterval = sampleRate / magSampleRate;
		
	}
	
	
	public DataOrganizer() {
		numDof = 9;
		sampleRate = 960;
		lengthOfTest = 6.9;
	}
	
	public void createDataSmpsRawData(int[] data) {
		
		boolean endCondition = false;
		int byteCounter = 0;

		dataSmps = new ArrayList<List<Double>>();
		for (int dof = 0; dof < 10; dof++) {
			List<Double> temp = new ArrayList<Double>();
			dataSmps.add(dof, temp);
		}

		
		if(delayAfterStart < 0) {
			int delayAdditionalLineNums = (int)Math.round(((double)delayAfterStart / -1000.0) * (double)sampleRate);
			
	        for(int i = 0; i < delayAdditionalLineNums; i++) {
	        	dataSmps.get(0).add((double)i/(double)sampleRate);
	        	if(magInterval == 10 && i % 10 == 0) {
	        		for(int dof9 = 1; dof9 < 10; dof9++) {
	        			dataSmps.get(dof9).add(0.0);
	        		}
	        	} else if(magInterval == 10 && i % 10 != 0) {
	        		for(int dof6 = 1; dof6 < 7; dof6++) {
	        			dataSmps.get(dof6).add(0.0);
	        		}
	        	
	        	} else if (magInterval == 1) {
	        		for(int dof9 = 1; dof9 < 10; dof9++) {
	        			dataSmps.get(dof9).add(0.0);
	        		}
	        	}
	        }
			lineNum = delayAdditionalLineNums;
		} else {
			lineNum = 0;
		}
		
		//System.out.println(data.length);
		//System.out.println(dataSmps);
		
		while(!endCondition) {
			for(int i = 0; i < magInterval; i++) {
				dataSmps.get(0).add(lineNum, (double)lineNum / (double)sampleRate);
				int tempByteCounter = byteCounter;
				//System.out.println("ln: " + lineNum);
				if(i == 0) {
					for(int dof9 = 1; dof9 < 10; dof9++) {		//starts at 1 because 0 is time
						if(data[byteCounter] == -1 || data[byteCounter + 1] == -1) {
							endCondition = true;
							break;
						}
						dataSmps.get(dof9).add(lineNum, (double)((data[byteCounter] * 256) + data[byteCounter + 1]));
						byteCounter += 2;
					}
				} else {
					for(int dof6 = 1; dof6 < 10; dof6++) {
						if(data[byteCounter] == -1 || data[byteCounter + 1] == -1) {
							endCondition = true;
							break;
						}
						if(dof6 < 7) {
							dataSmps.get(dof6).add(lineNum, (double)(data[byteCounter] * 256) + data[byteCounter + 1]);
							byteCounter += 2;
						} else {
							dataSmps.get(dof6).add(lineNum, null);
						}
						
						
						
					}
				}
				lineNum++;
				System.out.println(byteCounter - tempByteCounter);
			}
		
		}
		lineNum --;
		System.out.println(lineNum);
		if(signedData) {
			
			for(int smp = 0; smp < lineNum; smp++) {
				for(int dof = 1; dof < 10; dof++) {
					if(dof < 4) {
						double curVal = dataSmps.get(dof).get(smp);
						if(curVal > 32768) {
							curVal -= 65535;
						}
						curVal = (curVal * accelSensitivity) / 32768;
						dataSmps.get(dof).add(smp, curVal);
					} else if(dof < 7) {
						double curVal = dataSmps.get(dof).get(smp);
						if(curVal > 32768) {
							curVal -= 65535;
						}
						curVal = (curVal * gyroSensitivity) / 32768;
						dataSmps.get(dof).add(smp, curVal);	
					}
				}	
			}
		}
	}
	
	public int CreateCSV(boolean labelData, String fileOutputDirectory, String nameOfFile) {
		StringBuilder builder = new StringBuilder();
        PrintWriter DataFile = null;
        if (!labelData) {
	        for (int smp = 0; smp < lineNum - 1; smp++) {
	        	for(int dof = 1; dof <= numDof; dof++) {
	        		builder.append(dataSmps.get(dof).get(smp));
	        		builder.append(",");
	        	}
	        	builder.append("\n");
	        }
        } else {
        	builder.append("t,AccelX,AccelY,AccelZ,GyroX,GyroY,GyroZ,MagX,MagY,MagZ,\n");
	        for (int smp = 0; smp < lineNum - 1; smp++) {
	        	for(int dof = 0; dof < numDof + 1; dof++) {
	        		builder.append(dataSmps.get(dof).get(smp));
	        		builder.append(",");
	        	}
	        	builder.append("\n");
	        }       		
        }
        
        
        try {
        	if(fileOutputDirectory != null) {
        		DataFile = new PrintWriter(fileOutputDirectory + File.separator + nameOfFile);
        	}
        	else{
        		DataFile = new PrintWriter(new File((FileSystemView.getFileSystemView()     //Creates .CSV file in default directory which is documents
                    .getDefaultDirectory().toString()
                    + File.separator + nameOfFile)));
        	}
        } 
        catch (FileNotFoundException e) {
   
            return 1;
        } 
  
        DataFile.write(builder.toString());     //writes the string buffer to the .CSV creating the file
        DataFile.close();                       //close the .CSV
		return 0;
		
		
	}
	
	
	public void createDataSmpsCSV(String CSVFilePath) {
		dataSmps = new ArrayList<List<Double>>(7);
		

		double interval = (1.0 / sampleRate);
		int numSample = (int)(sampleRate * lengthOfTest);
		

		for (int dof = 0; dof < numDof; dof++) {
			List<Double> temp = new ArrayList<Double>();
			dataSmps.add(temp);
		}

		
		BufferedReader br = null;

		try {
			String line = "";
			br = new BufferedReader(new FileReader(CSVFilePath));

			while ((line = br.readLine()) != null) {

				String[] sample = line.split(",");

				for (int str = 0; str < sample.length; str++) {
					try {
						dataSmps.get(str).add(Double.parseDouble(sample[str]));
						min = Math.min(Double.parseDouble(sample[str]), min);
						max = Math.max(Double.parseDouble(sample[str]), max);
					} catch (NumberFormatException nfe) {
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
	
	
	public List<List<Double>> getDataSmps() {
		return dataSmps;
	}

	public double getLengthOfTest() {
		return lengthOfTest;
	}
	
	public int getNumDof() {
		return numDof;
	}

	public int getSampleRate() {
		return sampleRate;
	}
	
	

	public List<List<Double>> getZoomedSeries(double start, double end, int dofNum) {

		/*double duration = end - start;
		double numSamples = (duration * (double) sampleRate);
		double rate = 6242.0 / numSamples;
		double newSps =  (sampleRate * rate);
		double modifier = sampleRate / newSps;
		
		System.out.println(modifier);
		*/
		List<List<Double>> dofData = new ArrayList<List<Double>>();

		List<Double> dofTime = new ArrayList<Double>();
		List<Double> dofAxis = new ArrayList<Double>();

		//for(int smp = 0; smp < sampleRate * lengthOfTest; smp++) {
		//	dofTime.add(smp, dataSmps.get(0).get(smp));
		//}
		
	//	for (int smp = 0; smp < 6242 - 1; smp++) {
	//		dofTime.add(smp, dataSmps.get(0).get((int)(smp * modifier)));
	//	}
		dofData.add(0, dataSmps.get(0));
		
		//for(int smp = 0; smp < sampleRate * lengthOfTest; smp++) {
	//		dofAxis.add(smp, dataSmps.get(dofNum).get(smp));
	//	}
	
	//	for (int smp = 0; smp < 6242 - 1; smp++) {
	//		dofAxis.add(smp, dataSmps.get(dofNum).get((int)(smp * modifier)));
	//	}
		dofData.add(1, dataSmps.get(dofNum));

		//System.out.println(dofData.get(0).size());

		return dofData;
	}

}
