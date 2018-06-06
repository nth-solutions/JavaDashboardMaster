package dataorganizer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.incesoft.tools.excel.xlsx.Sheet;
import com.incesoft.tools.excel.xlsx.SimpleXLSXWorkbook;

import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.AWTException;
import java.awt.Desktop;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;


public class TemplateOpenerClass {
	//Copies template, writes data from datafile to the copy(outputfile)
	public static void start(String TemplateFile, String outputFile, String datafile) {
		LoadSettings settings = new LoadSettings();
		settings.loadConfigFile();
		ArrayList<String> templateList = new ArrayList<String>();
		if(TemplateFile.contains("null")) {
			settings.getKeyVal("TemplateDirectory");
			File[] templateFileList = new File(settings.getKeyVal("TemplateDirectory")).listFiles(); 
			if(templateFileList!=null) {
				for(int i=0; i<templateFileList.length;i++) {
					templateList.add(templateFileList[i].toString().substring(templateFileList[i].toString().lastIndexOf("\\")+1, templateFileList[i].toString().length()));
				}
			}
		}
		
		
		
		TemplateFile = settings.getKeyVal("TemplateDirectory") + JOptionPane.showInputDialog(null, "Select template","", JOptionPane.QUESTION_MESSAGE, null, templateList.toArray(), templateList.get(0));
		System.out.println(TemplateFile);
		SimpleXLSXWorkbook workbook = new SimpleXLSXWorkbook(new File(TemplateFile));							
		 OutputStream o=null;
		 //Try to open file with and without .xlsx extension.
		 try {
			 if(outputFile.contains(".xlsx")) {
				 o = new BufferedOutputStream(new FileOutputStream(outputFile));								//Creates output stream for writing the new file(had extension)
			 }
			 else {
				 o = new BufferedOutputStream(new FileOutputStream(outputFile+".xlsx"));						//Creates output stream for writing the new file
			 }
		 //If file does not exist
		 }catch(FileNotFoundException e) {
			 Dashboard.getFrameInstance().setGeneralStatusLabel("Could not create file at given directory");
		 }catch(NullPointerException e) {
			 //User closed window
		 }
		 if(o!=null) {
			//Writes all data from the datafile to outputstream with workbook as template
			 testWrite(workbook, o, datafile);
		 }
		//Check if xlsx extension was typed
		 if(outputFile.contains(".xlsx")) {																		
			 openNewFile(outputFile);																			//Opens file with default application
		 }else {
			 openNewFile(outputFile+".xlsx");																	//opens file with default application (and file suffix needed adding)
		 }
		 Robot robot=null;
		 
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		//Robot to clear flags in excel and update cells
		 robot.delay(3500);																					//Initial delay for application startup
		 robot.keyPress(KeyEvent.VK_LEFT);
		 robot.keyPress(KeyEvent.VK_ENTER);
		 robot.delay(100);
		 robot.keyRelease(KeyEvent.VK_LEFT);
		 robot.keyRelease(KeyEvent.VK_ENTER);
		 robot.delay(100);
		 robot.keyPress(KeyEvent.VK_ENTER);
		 robot.keyRelease(KeyEvent.VK_ENTER);
		 robot.delay(100);
		 robot.keyPress(KeyEvent.VK_CONTROL);																//Press CTRL
		 robot.keyPress(KeyEvent.VK_A);																		//Press A
		 robot.delay(250);																					//Make sure Excel has all selected
		 robot.keyRelease(KeyEvent.VK_CONTROL);																//Release CTRL
		 robot.keyRelease(KeyEvent.VK_A);																	//Release A
		 robot.keyPress(KeyEvent.VK_DELETE);																//Press Delete
		 robot.keyRelease(KeyEvent.VK_DELETE);																//Release Delete
		 robot.keyPress(KeyEvent.VK_CONTROL);																//Press CTRL
		 robot.keyPress(KeyEvent.VK_Z);																		//Press Z
		 robot.delay(250);																					//Make sure Excel undoes
		 robot.keyRelease(KeyEvent.VK_CONTROL);																//Release CTRL
		 robot.keyRelease(KeyEvent.VK_Z);																	//Release Z
		 File ofp = new File(outputFile);																	//Reference outputFile as File
		 try {
			Thread.sleep(100);
			 ofp.delete();
		} catch (InterruptedException e) {
		
		}
																								//Delete the outputFile from disk (Held in ram by Excel)
	 }
	/**
	 * 
	 * @param outputFile
	 * 		Where to write the file to (and open from)
	 */
	private static void openNewFile(String outputFile) {													//Opens file with windows default application (.xlsx = Excel)
		Desktop desktop = Desktop.getDesktop();																//Create desktop object
		File file = new File(outputFile);																	//Create file object to reference
		try{
			if(file.toString().contains(".xlsx")) {
				desktop.open(file);																			//Opens file with default windows program
			}
		}catch(IOException e) {
			Dashboard.getFrameInstance().setGeneralStatusLabel("Could not open the newly created template");
		}
	}

	private static void testWrite(SimpleXLSXWorkbook workbook, OutputStream outputStream, String datafile) {				//write datafile to outputstream with workbook template
		Sheet sheet = workbook.getSheet(0);																					//Reference FIRST sheet in book (0 indexed)
		if(sheet == null) {
			System.out.println("sheet null");
		}
		File csvData = null;    
		 csvData = new File(datafile);																						//Open raw data file
		 List<String> CSVData = null;
		try {
			CSVData = Files.readAllLines(csvData.toPath(),Charset.defaultCharset());										//Read all lines in csv file
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Dashboard.getFrameInstance().setGeneralStatusLabel("CSV could not be opened");									//Alert user file either did not exist or was not accessible
			
		}	//Read all lines into CSVData for loop
		
		
		JComboBox templateSourceFileList = new JComboBox();
		templateSourceFileList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String templateChosen = templateSourceFileList.getSelectedItem().toString();
			}
		});
		templateSourceFileList.setToolTipText("Template XLSX");
		
		copyDatatoTemplate(sheet, CSVData.size(), 0, CSVData); 																//copy data from datafile from row 0 to row 1200 into sheet
	    try {
			workbook.commit(outputStream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}							//Save
	}
	
	private static void copyDatatoTemplate(Sheet sheet, int rowCount, int rowOffset, List<String> CSVData) {					//copy data from datafile to sheet starting at rowOffset to rowCount     
		int countX=0;																				//Count rows written
		
		//Loop rows of sheet
		for (int r = rowOffset; r < rowCount; r++) {
			
		    //If the data was not null
			if(CSVData != null) {
		    	 int modfiedRowLength = sheet.getModfiedRowLength();								//See current Row to write
		    	 CSVData.get(countX);
		         String[] RowData = CSVData.get(countX).split(",");									//Copy all datapoints in row
		         //Loop for each cell of a row
		         for(int i=0;i<RowData.length;i++) {												
		        	 sheet.modify(modfiedRowLength, i, RowData[i], null);							//Write each datapoint(RowData[i]) to sheet by row(modifiedRowLength) and position in row(i) with style 'null'
		         }
		         countX++;
		    }
		}
	}
}
