package dataorganizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import com.sun.corba.se.impl.orb.ParserTable.TestBadServerIdHandler;
import com.sun.javafx.collections.MappingChange.Map;

public class CSVBuilder {        //Class for Creating .CSV files
	
	private static int additionalLineNums;

    public static int sortData(int[] data, String NameOfFile, int magInterval, String fileOutputDirectory, boolean elanCSV, boolean signedData, ArrayList<Integer> testParams) {
    
    	
    	PrintWriter DataFile = null;    //Object used to create .CSV file    
        
        
        //Finds the '-1' delimiter that specifies the end of the testing data
        int endPosition = -1;
        for(int pos = 0; pos < data.length; pos++) {
        	if(data[pos] == -1) {
        		endPosition = pos - 1;
        		break;
        	}
        }
        //System.out.println(endPosition);
        //Temporary array that holds the passed in data that has been converted from bytes to words
        double [] wordData = new double[endPosition + 1];
              
        //Convert passed in data from bytes to words
        int wordCounter = 0;
        for (int pos = 0; pos < endPosition + 1; pos += 2) {                       
            wordData[wordCounter] = (data[pos] * 256) + data[pos + 1];
            wordCounter++;
        }
        
        
        
        
        //Stores the test with a line number associated with a vector that holds the associated testing data
        HashMap<Integer, Vector<Double>> test = new HashMap<>();
        
        //Vector that holds the converted data that will be put in the 'test' hashmap with it's associated line number
        Vector<Double> sampleHolder = new Vector<Double>();
        
        additionalLineNums  = 0;
        
        if(testParams.get(2) < 0) {
        	
        	Vector<Double> vec9 = new Vector<Double>();
        	Vector<Double> vec6 = new Vector<Double>();
	        
        	for (int i = 0; i < 9; i++) {
        		if (i < 6) {
        			vec6.add(i, 0.0);
        		}
        		vec9.add(i, 0.0);
        	}
        	
        	double delayAfterStart = (double)testParams.get(2) / -1000.0;
	 
	        additionalLineNums = (int) Math.round(delayAfterStart * testParams.get(7));
	        
	        for(int i = 0; i < additionalLineNums; i++) {
	        	if(magInterval == 10 && i % 10 == 0) {
	        		test.put(i, vec9);
	        	} else if(magInterval == 10 && i % 10 != 0) {
	        		test.put(i, vec6);
	        	
	        	} else if (magInterval == 1) {
	        		test.put(i, vec9);
	        	}
	        }
        
        }
        
        
        
        //Initialize Counters/ Flags
        int lineNum = additionalLineNums;
        int sampleCounter = 0;
        int setCounter = 0;
        boolean nineAxisFlag = true;

        //Iterates through each data point
        for (int wordNum = 0; wordNum < wordData.length; wordNum++) { 
        	
        	//Executes if the Accel/Gyro : Mag ratio is 10:1, and the sample is a nine axis sample 
        	if (sampleCounter == 9 && (lineNum - additionalLineNums) % 10 == 0 && magInterval == 10 && nineAxisFlag == true) {    
        		
        		test.put(lineNum, sampleHolder);
            	//System.out.println("9 : " + test.get(lineNum).size());
            	sampleHolder = new Vector<Double>();  
            	lineNum++;
            	nineAxisFlag = false;
            	sampleCounter = 0;
            } 
        	
        	//Executes if the Accel/Gyro : Mag ratio is 10:1, and the sample is a six axis sample  
        	else if (sampleCounter == 6 && magInterval == 10 && nineAxisFlag == false ) {     
            	test.put(lineNum, sampleHolder);
            	//System.out.println("6 : " + test.get(lineNum).size());
            	sampleHolder = new Vector<Double>(); 
            	lineNum++;
            	setCounter++;
            	if (setCounter == 9) {
            		nineAxisFlag = true;
            		setCounter = 0;
            	}
            		
        		sampleCounter = 0;
            } 
			
        	//Executes if the Accel/Gyro : Mag ratio is 1:1 which means every sample is a 9 axis sample
        	else if (wordNum % 9 == 0 && magInterval == 1 && wordNum > 0) {
        		//System.out.println("9* : " + test.get(lineNum).size());
            	test.put(lineNum, sampleHolder);           	
            	sampleHolder = new Vector<Double>();
            	lineNum++;
            }
        	
        	if (sampleCounter < 3 && signedData) {
        		if(wordData[wordNum] > 32768) {
        			wordData[wordNum] -= 65535;
        		}
        		// converts to accel
        		wordData[wordNum] = ((wordData[wordNum]*testParams.get(9))/32768);
        	}
        	else if( sampleCounter < 6 && signedData) {
        		if(wordData[wordNum] > 32768) {
        			wordData[wordNum] -= 65535;
        		}
        		// gyro
        		wordData[wordNum] = ((wordData[wordNum]*testParams.get(10))/32768);		//Multiply by the sample rate, and divide by the the max value of a sample.
        	}
        	//Add a sample to the sampleHolder vector so it can be mapped to lineNumber when one of the conditionals listed above is true
            sampleHolder.add(wordData[wordNum]);  
                        
            sampleCounter++;
        } 
         
        lineNum --;
        int numSamples = lineNum;
        int dataFlag = 0;
        int sum = 0;

        //Determines the stop condition
        while (dataFlag < 3) {		//checks for 3 consecutive data points for the end condition
        	//System.out.println(test.get(lineNum).size());
        	for(int axis = 0; axis < test.get(lineNum).size(); axis++) {
        		sum += test.get(lineNum).get(axis); 		//sums all data in row of data file
        	}
        	if (magInterval == 1) {
        		if (sum != 0 && sum < 9 * 65535) {
        			dataFlag++;
        		}
        		else {
        			dataFlag = 0;
           		}
        		sum = 0;
           	}
        	else if (magInterval == 10 && test.get(lineNum).size() == 6) {
        		if (sum != 0 && sum < 6 * 65535) {
        			dataFlag++;
        		}
        		else {
        			dataFlag = 0;
           		}
        		sum = 0;
        	}
        	else if (magInterval == 10 && test.get(lineNum).size() == 9) {
        		if (sum != 0 && sum < 9 * 65535) {
        			dataFlag++;
        		}
        		else {
        			dataFlag = 0;
           		}
        		sum = 0;
        	}
        	lineNum --;
        }
        lineNum += 4;
        endPosition = lineNum;
        
        
        test = insertFirstMagSample(test);
       
        StringBuilder builder = new StringBuilder();
        
        if (!elanCSV) {
	        for (lineNum = 0; lineNum < endPosition; lineNum++) {
	        	for(int axis = 0; axis < test.get(lineNum).size(); axis++) {
	        		builder.append(test.get(lineNum).get(axis));
	        		builder.append(",");
	        	}
	        	builder.append("\n");
	        }
        } else {
        	builder.append("t,AccelX,AccelY,AccelZ,GyroX,GyroY,GyroZ,\n");
	        for (lineNum = 0; lineNum < endPosition; lineNum++) {
	        	builder.append((double)(lineNum) * (1.0/ (double)(testParams.get(7)))).append(",");
	        	for(int axis = 0; axis < test.get(lineNum).size(); axis++) {
	        		if(axis <= 5 && elanCSV) {
		        		builder.append(test.get(lineNum).get(axis));
		        		builder.append(",");
	        		}
	        	}
	        	builder.append("\n");
	        }        		
        }
        
        
        try {
        	if(fileOutputDirectory != null) {
        		DataFile = new PrintWriter(fileOutputDirectory + File.separator + NameOfFile);
        	}
        	else{
        		DataFile = new PrintWriter(new File((FileSystemView.getFileSystemView()     //Creates .CSV file in default directory which is documents
                    .getDefaultDirectory().toString()
                    + File.separator + NameOfFile)));
        	}
        } 
        catch (FileNotFoundException e) {
   
            return 0;
        } 
  
        DataFile.write(builder.toString());     //writes the string buffer to the .CSV creating the file
        DataFile.close();                       //close the .CSV
        
        
        
        return numSamples;        
    }
    

    
    public static HashMap<Integer, Vector<Double>> insertFirstMagSample(HashMap<Integer, Vector<Double>> map){
    	HashMap<Integer, Vector<Double>> newMap = new HashMap<Integer, Vector<Double>>();
    	
    	Vector<Double> line = map.get(additionalLineNums + 10);
    	Vector<Double> oldLine = map.get(additionalLineNums);

    	oldLine.set(6, line.get(6));
    	oldLine.set(7, line.get(7));
    	oldLine.set(8, line.get(8));
    	newMap = map;
    	newMap.put(additionalLineNums, oldLine);
    	return newMap;
    }
    
}


