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

import com.incesoft.tools.excel.xlsx.CellStyle;
import com.incesoft.tools.excel.xlsx.Fill;
import com.incesoft.tools.excel.xlsx.Font;
import com.incesoft.tools.excel.xlsx.Sheet;
import com.incesoft.tools.excel.xlsx.SimpleXLSXWorkbook;
import com.incesoft.tools.excel.xlsx.SimpleXLSXWorkbook.Commiter;

public class SpreadSheetController {
	private SimpleXLSXWorkbook workbook;
	private Sheet sheet;
	private int sheetIndex = 0;
	private Font font;
	private Fill fill;
	private CellStyle style;

	SpreadSheetController(String csv) {
		workbook = new SimpleXLSXWorkbook(new File(csv));
		font = workbook.createFont();
		font.setColor("FFFFFFFF");
		fill = workbook.createFill();
		fill.setFgColor("00000000");
		style = workbook.createStyle(font, fill);
	}
	
	SpreadSheetController(String csv, int index) {
		workbook = new SimpleXLSXWorkbook(new File(csv));		
		sheet = workbook.getSheet(index);
		font = workbook.createFont();
		font.setColor("FFFFFFFF");
		fill = workbook.createFill();
		fill.setFgColor("00000000");
		style = workbook.createStyle(font, fill);
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
	
	public void writeDataSetOneWithParams(List<String> offsets, List<Integer> params, List<List<Double>> CSVData) {
		setActiveSheet(0);
		copyDataToTemplate(2, CSVData);
		setActiveSheet(1);
		copyMPUOffsetsToTemplate(offsets);
		writeModuleParams(params);
	}
	
	public void writeDataSetTwoWithParams(List<String> offsets, List<Integer> params, List<List<Double>> CSVData) {
		setActiveSheet(2);
		copyDataToTemplate(2, CSVData);
		setActiveSheet(3);
		copyMPUOffsetsToTemplate(offsets);
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
	
	public void copyDataToTemplate(int rowOffset, List<List<Double>> CSVData) {					//copy data from datafile to sheet starting at rowOffset to rowCount     
		for(int axi = 1; axi < 10; axi++) {
			if(CSVData != null) {
				List<Double> RowData = CSVData.get(axi);
				for(int i = 0; i < RowData.size(); i++) {
					if(RowData.get(i) == null) continue;
					this.modifyCell(rowOffset+i, axi-1, String.valueOf(RowData.get(i)));
				}
			}
		}
	}
	
}
