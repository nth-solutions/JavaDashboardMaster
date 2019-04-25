package dataorganizer;

import com.aspose.cells.*;

import java.util.List;

public class PendulumSpreadsheetController {


    //TODO: Test if Workbook can be saved to a different location than its origin (Ex. Leave template in src and save the updated version to Documents)
    //TODO: Dynamically get path of template
    private Workbook workbook;
    private String workbookPath = "C:\\Users\\Mason\\Documents\\Lab Templates\\Pendulum Template REV-Q3.xlsx";



    public void loadPendulumParameters (double pendulumLength, double pendulumMass, double moduleMass, double moduleDistanceFromAOR){ //
        try{
            workbook = new Workbook(workbookPath);
        }
        catch(Exception eg){
            System.out.println("Failed to open workbook");
        }
        workbook.getWorksheets().get(4).getCells().get("C7").setValue(pendulumLength);
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(pendulumMass);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(moduleMass);
        workbook.getWorksheets().get(4).getCells().get("C10").setValue(moduleDistanceFromAOR);


        System.out.println(pendulumLength);
        System.out.println(pendulumMass);
        System.out.println(moduleMass);
        System.out.println(moduleDistanceFromAOR);

        /*try{
            workbook.save(workbookPath, FileFormatType.XLSX);
        }
        catch(Exception e){
            System.out.println("Invalid Path");
        }*/
    }


    //TODO: UNTESTED
    /**
     * Fills the Pendulum Template with all of the data recorded during a module test
     * @param rowOffset The offset integer used to identify the first row for data to be added
     * @param dataSamples 2-D List containing all of the data the module recorded during testing
     */
    public void fillTemplateWithData(int rowOffset, List<List<Double>> dataSamples) {
        for (int axis = 1; axis < 10; axis++) {
        	if (dataSamples != null) {
                List<Double> ColumnData = dataSamples.get(axis);    //Splits the 2-D List into Individual Lists based on axis
                for (int columnIndex = 0; columnIndex < ColumnData.size(); columnIndex++) { //Loops through every data point in the List
                    if (ColumnData.get(columnIndex) == null) continue;  //If the index holds an invalid value, continue
                    
                    workbook.getWorksheets().get(0).getCells().get(columnIndex + rowOffset, axis-1).setValue(ColumnData.get(columnIndex).intValue());  //Adds the data held at columnIndex to each row of the data
                }
            }
        }

        try{
            workbook.save(workbookPath, FileFormatType.XLSX);
        }
        catch(Exception e){
        	System.out.println(e.getStackTrace());
            System.out.println("Invalid Path");
        }

    }
}
