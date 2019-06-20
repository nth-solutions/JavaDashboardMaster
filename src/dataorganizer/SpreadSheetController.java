package dataorganizer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import com.incesoft.tools.excel.xlsx.CellStyle;
import com.incesoft.tools.excel.xlsx.Fill;
import com.incesoft.tools.excel.xlsx.Font;
import com.incesoft.tools.excel.xlsx.Sheet;
import com.incesoft.tools.excel.xlsx.SimpleXLSXWorkbook;

public class SpreadSheetController {
	private SimpleXLSXWorkbook workbook;
	private Sheet sheet;
	private int sheetIndex = 0;
	private Font font;
	private Fill fill;
	private CellStyle style;

	
	
	/*
	 * SpreadSheet Controller initialization method. We initialize the style of the cells we write to here. 
	 * @param csv is a string containing the path of the csv file this is used to create a workbook.
	 */
	SpreadSheetController(String csv) {
		workbook = new SimpleXLSXWorkbook(new File(csv));
		font = workbook.createFont();
		font.setColor("FFFFFFFF");
		fill = workbook.createFill();
		fill.setFgColor("00000000");
		style = workbook.createStyle(font, fill);
	}

	/*
	 * SpreadSheet Controller initialization method. We initialize the style of the cells we write to here. 
	 * @param csv is a string containing the path of the csv file this is used to create a workbook, and index is the sheet where we start at. 
	 */
	SpreadSheetController(String csv, int index) {
		workbook = new SimpleXLSXWorkbook(new File(csv));		
		sheet = workbook.getSheet(index, false);
		font = workbook.createFont();
		font.setColor("FFFFFFFF");
		fill = workbook.createFill();
		fill.setFgColor("00000000");
		style = workbook.createStyle(font, fill);
	}
	
	
	/*
	 * This method sets the index of the sheet to write to. Before modifying a sheet, this method must be called.
	 * @param index is the number of the sheet that we are referencing.
	 */
	public void setActiveSheet(int index) {
		sheetIndex = index;
		sheet = workbook.getSheet(index, false);
	}
	
	/*
	 * This method checks which sheet we are currently writing to. 
	 */
	public int getActiveSheet() {
		return sheetIndex;
	}
	
	
	/* This method writes to the x(column), and y(row), the value of string, if the sheet is set. 
	 */
	public void modifyCell(int x, int y, String value) {
		if(sheet != null) {
			sheet.modify(x, y, value, style);
		}
	}
	
	/*
	 * This method saves the file to the location passed by param
	 */
	public void save(String outputFile) throws Exception {
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		workbook.commit(outputStream);
	}
	
	/*
	 * This method writes the module data and parameters of the test to the worksheet, we define these locations nowhere. 
	 * @param offsets are the 
	 */
	public void writeDataSetOneWithParams(int[][] MpuMinMax, List<Integer> params, List<List<Double>> CSVData) {
		setActiveSheet(0);
		copyDataToTemplate(2, CSVData);
		setActiveSheet(1);
		writeMPUMaxMinToTemplate(MpuMinMax);
		writeModuleParams(params);
	}
	
	/*
	 * This method does the same but for a second dataset.
	 */
	public void writeDataSetTwoWithParams(int[][] MpuMinMax, List<Integer> params, List<List<Double>> CSVData) {
		setActiveSheet(2);
		copyDataToTemplate(2, CSVData);
		setActiveSheet(3);
		writeMPUMaxMinToTemplate(MpuMinMax);
		writeModuleParams(params);
	}
	
	
	public void writeMPUMaxMinToTemplate(int[][] MpuOffsets) {
		for(int axi = 0; axi < MpuOffsets.length; axi++ ) {
			this.modifyCell(axi+2, 1, Integer.toString(MpuOffsets[axi][0]));
			this.modifyCell(axi+2, 2, Integer.toString(MpuOffsets[axi][1]));
		}
	}
	
	public void copyMPUOffsetsToTemplate(List<String> offsets) {
		if(offsets != null)
			for(int i = 0; i < offsets.size() && i < 6; i++) { //Write 
				this.modifyCell(i, 0, offsets.get(i));
			}
	}
	
	public void writeModuleParams(List<Integer> params) {
		for(int i = 6; i < params.size()-2; i++) {
			this.modifyCell(i+5, 1, Integer.toString(params.get(i))); //Add one to i in first parameter of modify cell to write to correct 
		}
	}

	/**
	 * Writes the actual sensor data to the spreadsheet.
	 * @param rowOffset offset integer used to identify the first row in which data is added.
	 * @param CSVData actual data that should be written to spreadsheet.
	 */
	
	public void copyDataToTemplate(int rowOffset, List<List<Double>> CSVData) {					//copy data from datafile to sheet starting at rowOffset to rowCount     
		for(int axi = 1; axi < 10; axi++) {														// iterates through every axis, one for each dof
			if(CSVData != null) {
				List<Double> ColumnData = CSVData.get(axi);										// creates a list of doubles for each coloumn
				for(int i = 0; i < ColumnData.size(); i++) {
					if(ColumnData.get(i) == null) continue;
					this.modifyCell(rowOffset+i, axi-1, String.valueOf(ColumnData.get(i)));
				}
			}
		}
	}
	
}
