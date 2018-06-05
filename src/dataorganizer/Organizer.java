package dataorganizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import com.sun.corba.se.impl.orb.ParserTable.TestBadServerIdHandler;
import com.sun.javafx.collections.MappingChange.Map;

public class Organizer {        //Class for Creating .CSV files
	private int progress = 0;
	
    public boolean sortData(int[] data, int testNum, int totalNumTests, String NameOfFile, int magInterval, double period, boolean timeStamp, boolean only9Axis, String fileOutputDirectory) {
    //Method to create .CSV
    	Dashboard dashBoard = Dashboard.getFrameInstance();
    	LoadSettings settings = new LoadSettings();
    	dashBoard.setWriteStatusLabel("Creating CSV for Test #" + (testNum));        //Tell the user a new .CSV has been created.
    	dashBoard.updateProgress(0);
    	PrintWriter DataFile = null;    //Object used to create .CSV file    
        
        int [] wordData = new int[data.length];
        
        int word = 0;                                   //Variable used to hold data
        int lineNumber = 0;                             //the current line number or sample number
        int endPosition = 0;
        for(int pos = 0; pos < data.length; pos++) {
        	if(data[pos] == -1) {
        		endPosition = pos;
        	}
        }
        
        int wordCounter = 0;
        for (int pos = 0; pos < endPosition - 1; pos += 2) {                        //While there is more data that needs to be processed
        	//Frame.updateProgress((int) ( ( (double)(i) ) / (double)(endPosition) ) * 100);    //Updates the progress bar with a progress of creating teh .CSV file
            wordData[wordCounter] = (data[pos] * 256) + data[pos + 1];
            wordCounter++;
        }

              
        
        HashMap<Integer, Vector<Integer>> test = new HashMap<>();
        Vector<Integer> sampleHolder = new Vector();
        int lineNum = 0;
        int sampleCounter = 0;
        int setCounter = 0;
        boolean nineAxisFlag = true;
        for (int wordNum = 0; wordNum < wordData.length; wordNum++) {   //tracks if mag data should be written to the .CSV for just Accel/Gyro
            progress = (int)(100 * ((double)wordNum / (double)(wordData.length))) / 2;
        	dashBoard.updateProgress(progress);
        	if (sampleCounter == 9 && lineNum % 10 == 0 && magInterval == 10 && nineAxisFlag == true) {     //if new line is zero, then the mag data should is in this block of data so it needs to written to the .CSV
            	test.put(lineNum, sampleHolder);
            	//System.out.println("9 : " + test.get(lineNum).size());
            	sampleHolder = new Vector();  
            	lineNum++;
            	nineAxisFlag = false;
            	sampleCounter = 0;
            } 
        	
        	else if (sampleCounter == 6 && magInterval == 10 && nineAxisFlag == false ) {     //if new line is zero, then the mag data should is in this block of data so it needs to written to the .CSV
            	test.put(lineNum, sampleHolder);
            	//System.out.println("6 : " + test.get(lineNum).size());
            	sampleHolder = new Vector();  
            	lineNum++;
            	setCounter++;
            	if (setCounter == 9) {
            		nineAxisFlag = true;
            		setCounter = 0;
            	}
            		
        		sampleCounter = 0;
            } 
			
        	else if (wordNum % 9 == 0 && magInterval == 1 && wordNum > 0) {
            	test.put(lineNum, sampleHolder);           	
            	sampleHolder = new Vector();
            	//System.out.println(test.get(lineNum).size());
            	lineNum++;
            }
            sampleHolder.add(wordData[wordNum]);  
            sampleCounter++;
        } 
        
        lineNum --;
        //System.out.println("BEFORE");
        int dataFlag = 0;
        int sum = 0;
      
        while (dataFlag < 3) {
        	progress = 50 + dataFlag * 3;
        	dashBoard.updateProgress(progress);
        	for(int axis = 0; axis < test.get(lineNum).size(); axis++) {
        		//System.out.println(test.get(lineNum).get(axis));
        		sum += test.get(lineNum).get(axis); 
        	}
        	//System.out.println(sum);
        	if (magInterval == 1) {
        		//System.out.println(sum);
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
       
        StringBuilder builder = new StringBuilder();
        
        //System.out.println(test.get(lineNum).size());
        
        /*
        if (timeStamp) {   //If the Time Stamp Boolean is true, then add the time stamp on the first column of the .CSV
            builder.append(lineNumber * period).append(",");
        }
        */
        
        for (lineNum = 0; lineNum < endPosition; lineNum++) {
        	progress = 59 + (int)((100 * (double) lineNum / (double) endPosition) / 2.5);
        	//System.out.println(test.get(lineNum).size());
        	for(int axis = 0; axis < test.get(lineNum).size(); axis++) {
        		builder.append(test.get(lineNum).get(axis));
        		builder.append(",");
        	}
        	builder.append("\n");
        }
        
        //System.out.println("AFTER");
        
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
   
            return false;
        } 
        
        if (testNum == totalNumTests) {
        	dashBoard.setWriteStatusLabel("Data Transfer Complete");
        }
        else {
        	dashBoard.setWriteStatusLabel("Finished Creating CSV for Test #" + testNum);
        }
        
        dashBoard.updateProgress(100);
        DataFile.write(builder.toString());     //writes the string buffer to the .CSV creating the file
        DataFile.close();                       //close the .CSV
        
        settings.loadConfigFile();
        if(Boolean.parseBoolean(settings.getKeyVal("OpenOnRead"))){
        	TemplateOpenerClass toc = new TemplateOpenerClass();
        	//FIXME: static outputfile, template needs to be chosen internally to TemplateOpenerClass.
        	toc.start(settings.getKeyVal("TemplateDirectory")+"5 Second Rev-D.xlsx", "tmp.xlsx", NameOfFile);
        }
        
        
        return true;        
    }
    

}


