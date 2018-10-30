package dataorganizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.incesoft.tools.excel.xlsx.Sheet;
import com.incesoft.tools.excel.xlsx.SimpleXLSXWorkbook;
import com.incesoft.tools.excel.xlsx.SimpleXLSXWorkbook.Commiter;

public class SpreadSheetController {
	private SimpleXLSXWorkbook workbook;
	private Sheet sheet;
	private int sheetIndex = 0;
	private OutputStream os; //OutputStream for Commiter
	
	private static String csv = "C:\\Users\\Mason\\Documents\\Adventure Modules Test Data\\(#2) 960-96 16G-92 2000dps-92 MAG-N 9OCT18.csv";
	private static String template = "C:\\Users\\Mason\\Documents\\Adventure Modules Test Data\\Pendulum Template Rev G DAF.xlsx";
	
	public static void main(String[] args) throws Exception {
		List<String> csvData = null;
		csvData = Files.readAllLines(new File(csv).toPath());
		
		SpreadSheetController SSC = new SpreadSheetController(template, 1);
		SSC.copyDatatoTemplate(2, csvData);
		
		List<String> offsets = Arrays.asList("1","2","3","4","5","6");
		
		SSC.setActiveSheet(0);
		
		SSC.copyMPUOffsetsToTemplate(offsets);
		SSC.save("C:\\Users\\Mason\\Documents\\Adventure Modules Test Data\\templateModified.xlsx");
	}
	


	SpreadSheetController(String csv) {
		workbook = new SimpleXLSXWorkbook(new File(csv));
	}
	
	SpreadSheetController(String csv, int index) {
		workbook = new SimpleXLSXWorkbook(new File(csv));		
		sheet = workbook.getSheet(index);
	}
	
	public void setActiveSheet(int index) {
		sheetIndex = index;
		sheet = workbook.getSheet(index);
	}
	
	public int getActiveSheet(int index) {
		return sheetIndex;
	}
	
	public void modifyCell(int x, int y, String value) {
		if(sheet != null) {
			sheet.modify(x, y, value, null);
		}
	}
	
	public void save(String outputFile) throws Exception {
		workbook.commit(new FileOutputStream(outputFile));
	}
	
	public void copyMPUOffsetsToTemplate(List<String> offsets) {
		for(int i = 0; i < offsets.size(); i++) {
			this.modifyCell(-1, 0, offsets.get(i));
		}
	}
	
	private void copyDatatoTemplate(int rowOffset, List<String> CSVData) {					//copy data from datafile to sheet starting at rowOffset to rowCount     
		int countX=0;																				//Count rows written
		
		//Loop rows of sheet
		for (int r = rowOffset; r < CSVData.size(); r++) {
		    //If the data was not null
			if(CSVData != null) {
		    	 int modfiedRowLength = sheet.getModfiedRowLength();								//See current Row to write
		    	 CSVData.get(countX);
		         String[] RowData = CSVData.get(countX).split(",");									//Copy all datapoints in row
		         //Loop for each cell of a row
		         for(int i=0;i<RowData.length;i++) {												
		        	 sheet.modify(r, i, RowData[i], null);							//Write each datapoint(RowData[i]) to sheet by row(modifiedRowLength) and position in row(i) with style 'null'
		         }
		         countX++;
		    }
		}
	}
	
}
