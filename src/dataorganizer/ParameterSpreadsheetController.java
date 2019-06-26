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

            testTypeFileName = "Conservation of Momentum Template.xlsx";
        }
        else if(testType == "Conservation of Angular Momentum"){

            testTypeFileName = "Conservation of Angular Momentum Template.xlsx";
        }
        else if(testType == "Conservation of Energy"){

            testTypeFileName = "Conservation of Energy Template.xlsx";
        }
        else if(testType == "Inclined Plane") {

            testTypeFileName = "Inclined Plane Template.xlsx";
        }
        else if(testType == "Physical Pendulum"){

            testTypeFileName = "Pendulum Template REV-Q3.xlsx";

        }else if(testType == "Spinny Stool"){

            testTypeFileName = "Spinny Stool Template.xlsx";

        }else if(testType == "Spring Test - Simple Haromincs"){

            testTypeFileName = "Spring Test - Simple Haromincs Template.xlsx";
        }
        //testTypeFileName = "Pendulum Template REV-Q3.xlsx";
        //System.out.println(System.getProperty("user.home"));
        //System.out.println(testTypeFileName);
        documentsPath = System.getProperty("user.home") + "\\Documents\\Lab Templates\\" + testTypeFileName; //The User is asked to store the templates in their documents folder. This line accounts for the different file paths due to different user names across different machines.
        //documentsPath = "C:\\Users\\Falcon\\Documents\\Github\\JavaDashBoardMaster\\Pendulum Template REV-Q3.xlsx";
        try {
            this.workbook = new Workbook(documentsPath); // A new workbook is created from the template
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Invalid Workbook Path");
        }
    }

    /**
     * This method is used to save a modified template as a new spreadsheet at the desired output path.
     * @param outputPath the file save location
     */

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

    /**
     * These methods serve to write the specific parameters for each test to their correct locations in the template.
     * They are called in EducatorModeControllerFX.
     * @param pendulumLength
     * @param pendulumMass
     * @param moduleMass
     * @param moduleDistanceFromAOR
     */

    public void loadPendulumParameters (double pendulumLength, double pendulumMass, double moduleMass, double moduleDistanceFromAOR){
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

    public void loadConservationofEnergyParameters(double totalDropDistance, double massOfModuleAndHolder, double momentOfIntertia, double radiusOfTorqueArm) {
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(totalDropDistance);
        workbook.getWorksheets().get(4).getCells().get("C7").setValue(massOfModuleAndHolder);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(momentOfIntertia);
        workbook.getWorksheets().get(4).getCells().get("C10").setValue(radiusOfTorqueArm);
    }
    /**
     * Fills the Pendulum Template with all of the data recorded during a module test
     * @param rowOffset The offset integer used to identify the first row for data to be added
     * @param dataSamples 2-D List containing all of the data the module recorded during testing
     */
    public void fillTemplateWithData(int rowOffset, List<List<Double>> dataSamples) {
        for (int axis = 1; axis < 10; axis++) { // There are 9 different sets of data points. Accel x, y, and z; Gyro x, y, and z; and Mag x, y, and z
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
