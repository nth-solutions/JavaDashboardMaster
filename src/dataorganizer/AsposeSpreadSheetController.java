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

	Workbook workbook;
	
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
	
	public List<List<Double>> getMomentumSamples() {
		Worksheet sheet = workbook.getWorksheets().get(4);
		Cells cells = sheet.getCells();
		FindOptions findOptions=  new FindOptions();
		findOptions.setLookAtType(LookAtType.ENTIRE_CONTENT);
		int numSamples = cells.get(19, 2).getIntValue();
		sheet = workbook.getWorksheets().get(5);
		cells = sheet.getCells();
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
