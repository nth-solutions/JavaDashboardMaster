package dataorganizer;

import java.util.ArrayList;
import java.util.List;

import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.FindOptions;
import com.aspose.cells.LookAtType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;

public class AsposeSpreadSheetController {

	private Workbook workbook;
	private int numSamples;
	
	public AsposeSpreadSheetController(String workbookPath) throws Exception {
		workbook = new Workbook(workbookPath);
	}
	
	public Integer castCellToInt(Cell cell) {
		return new Integer((cell.getValue().toString()));
	}
	
	public List<Integer> getTestParameters(){
		List<Integer> parameters = new ArrayList<Integer>(13);
		
		for (int i = 0; i < 13; i++) {
			parameters.add(null);
		}
		
		Worksheet sheet = workbook.getWorksheets().get(1);
		Cells cells = sheet.getCells();
		
		parameters.set(7, castCellToInt(cells.get(12, 1)));
		parameters.set(8, castCellToInt(cells.get(13, 1)));
		parameters.set(9, castCellToInt(cells.get(14, 1)));
		parameters.set(10, castCellToInt(cells.get(15, 1)));
		
		sheet = workbook.getWorksheets().get(4);
		cells = sheet.getCells();
		parameters.set(6, new Double(cells.get(19, 4).getValue().toString()).intValue());
		
		return parameters;
	}
	
	public int getNumberOfSamplesModuleOne() {
		Worksheet sheet = workbook.getWorksheets().get(4);
		Cells cells = sheet.getCells();
		numSamples = cells.get(19, 2).getIntValue();
		return numSamples;
	}
	
	public int getNumberOfSamplesModuleTwo() {
		Worksheet sheet = workbook.getWorksheets().get(4);
		Cells cells = sheet.getCells();
		numSamples = cells.get(20, 2).getIntValue();
		return numSamples;
	}
	
	public List<List<Double>> getMomentumSamplesModuleOne() {
		Worksheet sheet = workbook.getWorksheets().get(5);
		Cells cells = sheet.getCells();
		getNumberOfSamplesModuleOne();
		Object[][] cellArray =  cells.exportArray(5, 41, numSamples, 3);
		List<List<Double>> momentumSamples = new ArrayList<List<Double>>();
		momentumSamples.add(new ArrayList<Double>());
		momentumSamples.add(new ArrayList<Double>());
		momentumSamples.add(new ArrayList<Double>());
		numSamples-=7;//TODO: Remove. For temporary COE template fail
		for(int i = 0; i < numSamples; i++) {
			momentumSamples.get(0).add(new Double(cellArray[i][0].toString()));
			momentumSamples.get(1).add(new Double(cellArray[i][1].toString()));
			momentumSamples.get(2).add(new Double(cellArray[i][2].toString()));
		}
		return momentumSamples;
	}
	
	public List<List<Double>> getMomentumSamplesModuleTwo() {
		Worksheet sheet = workbook.getWorksheets().get(6);
		Cells cells = sheet.getCells();
		getNumberOfSamplesModuleTwo();
		numSamples-=7; //TODO: Remove. For temporary COE template fail
		Object[][] cellArray =  cells.exportArray(5, 41, numSamples, 3);
		List<List<Double>> momentumSamples = new ArrayList<List<Double>>();
		momentumSamples.add(new ArrayList<Double>());
		momentumSamples.add(new ArrayList<Double>());
		momentumSamples.add(new ArrayList<Double>());
		for(int i = 0; i < numSamples; i++) {
			momentumSamples.get(0).add(new Double(cellArray[i][0].toString()));
			momentumSamples.get(1).add(new Double(cellArray[i][1].toString()));
			momentumSamples.get(2).add(new Double(cellArray[i][2].toString()));
		}
		return momentumSamples;
	}
	
	public void printMomentumSamples(List<List<Double>> momentumSamples) {
		for(int i = 0; i < momentumSamples.get(0).size(); i++) {
			System.out.print(momentumSamples.get(0).get(i) + ", ");
			System.out.print(momentumSamples.get(1).get(i) + ", ");
			System.out.println(momentumSamples.get(2).get(i));
		}
	}
	
}
