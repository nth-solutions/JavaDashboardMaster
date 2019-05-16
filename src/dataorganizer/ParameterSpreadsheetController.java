package dataorganizer;

import com.aspose.cells.*;

import java.util.List;

public class ParameterSpreadsheetController {

    private Workbook workbook;
    private String documentsPath;
    private String testTypeFileName;
    private String testType;

    public ParameterSpreadsheetController() {

        testType = EducatorModeControllerFX.testType; // gets the selected test from Educator mode

        if (testType == "Conservation of Momentum (Elastic Collision)"){ // changes the end of the file path to match the test. This file path is the location of the unaltered template

            testTypeFileName = "";
        }
        else if(testType == "Conservation of Angular Momentum"){

            testTypeFileName = "";
        }
        else if(testType == "Conservation of Energy"){

            testTypeFileName = "";
        }
        else if(testType == "Inclined Plane") {

            testTypeFileName = "";
        }
        else if(testType == "Physical Pendulum"){

            testTypeFileName = "Pendulum Template REV-Q3.xlsx";

        }else if(testType == "Spinny Stool"){

            testTypeFileName = "";

        }else if(testType == "Spring Test - Simple Haromincs"){

            testTypeFileName = "";
        }
        //testTypeFileName = "Pendulum Template REV-Q3.xlsx";
        //System.out.println(System.getProperty("user.home"));
        //System.out.println(testTypeFileName);
        documentsPath = System.getProperty("user.home") + "\\Documents\\Lab Templates\\" + testTypeFileName;
        //documentsPath = "C:\\Users\\Kinobo\\Documents\\Lab Templates\\Pendulum Template REV-Q3.xlsx";
        try {
            this.workbook = new Workbook(documentsPath);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Invalid Workbook Path");
        }
    }

    public void saveWorkbook(String outputPath){
        try {
            System.out.println(outputPath);
            workbook.save(outputPath, FileFormatType.XLSX);

        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
            System.out.println("Invalid output path");
        }

    }
    public void test(){
        System.out.println(testType);
    }
    public void loadPendulumParameters (double pendulumLength, double pendulumMass, double moduleMass, double moduleDistanceFromAOR){ //writes the PendulumParameters to their correct locations in the spreadsheet. Following load parameter classes do them same thing but for their respective test.
        workbook.getWorksheets().get(4).getCells().get("C7").setValue(pendulumLength);
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(pendulumMass);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(moduleMass);
        workbook.getWorksheets().get(4).getCells().get("C10").setValue(moduleDistanceFromAOR);
    }

    public void loadSpinnyStoolParameters(double massHandWeights, double wingspan, double massOfPerson, double shoulderWidth) {
        workbook.getWorksheets().get(4).getCells().get("C7").setValue(massHandWeights);
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(wingspan);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(massOfPerson);
        workbook.getWorksheets().get(4).getCells().get("C10").setValue(shoulderWidth);
    }

    public void loadSpringTestParameters(double springConstant, double totalMass, double momentofInertia, double radiusoftorquearm) {
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(springConstant);
        workbook.getWorksheets().get(4).getCells().get("C7").setValue(totalMass);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(momentofInertia);
        workbook.getWorksheets().get(4).getCells().get("C10").setValue(radiusoftorquearm);
    }
    public void loadConservationofMomentumParameters (double gliderOneMass, double gliderTwoMass){
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(gliderOneMass);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(gliderTwoMass);
    }

    public void loadConservationofEnergyParameters() {

    }

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

    }
}
