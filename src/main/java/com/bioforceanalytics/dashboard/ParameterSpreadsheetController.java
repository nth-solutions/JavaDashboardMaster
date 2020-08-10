package com.bioforceanalytics.dashboard;

import java.util.List;

import com.aspose.cells.FileFormatType;
import com.aspose.cells.Workbook;

/**
 * Responsible for writing test parameters / test data to excel spreadsheets.
 * @deprecated Excel functionality has been removed from BioForce's software suite.
 */
@Deprecated
public class ParameterSpreadsheetController {

    private Workbook workbook;
    private String documentsPath;
    private String testTypeFileName;
    private String testType;
    private Boolean educationTemplateFound;

    public ParameterSpreadsheetController(String FilePath) {

        if (FilePath == "EducationMode"){ //If EducationMode is passed as the file path, the dashboard will use the selected test type to determine which template to use and where to get it from

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
            else if(testType == "Inclined Plane - Released From Top") {
                testTypeFileName = "Inclined Plane (Released From Top) Template.xlsx";
            }
            else if(testType == "Inclined Plane - Projected From Bottom"){
                testTypeFileName = "Inclined Plane (Projected From Bottom) Template.xlsx";
            }
            else if(testType == "Physical Pendulum"){

                testTypeFileName = "Pendulum Template.xlsx";

            }else if(testType == "Spinny Stool"){

                testTypeFileName = "Spinny Stool Template.xlsx";

            }else if(testType == "Spring Test - Simple Harmonics"){

                testTypeFileName = "Spring Test - Simple Harmonics Template.xlsx";
            }
            else if(testType == "Generic Template - One Module") {
                testTypeFileName = "Generic (One Module) Template.xlsx";
            }
            else if(testType == "Generic Template - Two Modules"){
                testTypeFileName = "Generic (Two Modules) Template.xlsx";
            }

            documentsPath = System.getProperty("user.home") + "/.BioForce Dashboard/Educator Templates/" + testTypeFileName; //The User is asked to store the templates in their documents folder. This line accounts for the different file paths due to different user names across different machines.

            try {
                this.workbook = new Workbook(documentsPath); // A new workbook is created from the template
                educationTemplateFound = true;
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Invalid Workbook Path");
                educationTemplateFound = false;
            }
        }else {
            documentsPath =  FilePath;
            try {
                this.workbook = new Workbook(documentsPath); // A new workbook is created from the template
                educationTemplateFound = true;
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Invalid Workbook Path");
                educationTemplateFound = false;
            }
        }

    }

    public Boolean getEducationTemplateFound(){
        return educationTemplateFound;
    }

    /**
     * This method is used to save a modified template as a new spreadsheet at the desired output path.
     * @param outputPath the file save location (must include the name of the actual file itself)
     */

    public void saveWorkbook(String outputPath){
        try {
            workbook.save(outputPath, FileFormatType.XLSX);
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
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
        workbook.getWorksheets().get(2).getCells().get("AB3").setValue(pendulumLength);
        workbook.getWorksheets().get(2).getCells().get("AB4").setValue(pendulumMass);
        workbook.getWorksheets().get(2).getCells().get("AB5").setValue(moduleMass);
        workbook.getWorksheets().get(2).getCells().get("AB6").setValue(moduleDistanceFromAOR);
    }

    public void loadSpinnyStoolParameters(double massHandWeights, double wingspan, double massOfPerson, double shoulderWidth) {
        workbook.getWorksheets().get(4).getCells().get("C7").setValue(massHandWeights);
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(wingspan);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(massOfPerson);
        workbook.getWorksheets().get(4).getCells().get("C10").setValue(shoulderWidth);
    }

    public void loadSpringTestParameters(double springConstant, double totalMass, double Amplitude, double massOfSpring) {
        workbook.getWorksheets().get(3).getCells().get("C7").setValue(totalMass);
        workbook.getWorksheets().get(3).getCells().get("C8").setValue(springConstant);
        workbook.getWorksheets().get(3).getCells().get("C9").setValue(massOfSpring);
        //workbook.getWorksheets().get(3).getCells().get("C9").setValue(Amplitude);
    }
    public void loadConservationofMomentumParameters (double gliderOneAndModuleOneMass, double gliderTwoAndModuleTwoMass){
        workbook.getWorksheets().get(7).getCells().get("C8").setValue(gliderOneAndModuleOneMass);
        workbook.getWorksheets().get(7).getCells().get("C9").setValue(gliderTwoAndModuleTwoMass);
    }

    public void loadConservationofEnergyParameters(double totalDropDistance, double massOfModuleAndHolder, double momentOfIntertia, double radiusOfTorqueArm) {
        workbook.getWorksheets().get(6).getCells().get("C9").setValue(massOfModuleAndHolder);
        workbook.getWorksheets().get(6).getCells().get("C8").setValue(momentOfIntertia);
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

    /**
     * Fills the momentum template, a special method is used so that raw data can be written to any sheet in the workbook
     * @param rowOffset See above
     * @param dataSamples See above
     * @param workbookSheet The sheet that the 2-D list of DataSamples is written to.
     */

    public void fillTwoModuleTemplateWithData(int rowOffset, List<List<Double>> dataSamples, int workbookSheet){
        for (int axis = 1; axis < 10; axis++) { // There are 9 different sets of data points. Accel x, y, and z; Gyro x, y, and z; and Mag x, y, and z
            if (dataSamples != null) {
                List<Double> ColumnData = dataSamples.get(axis);    //Splits the 2-D List into Individual Lists based on axis
                for (int columnIndex = 0; columnIndex < ColumnData.size(); columnIndex++) { //Loops through every data point in the List
                    if (ColumnData.get(columnIndex) == null) continue;  //If the index holds an invalid value, continue
                    workbook.getWorksheets().get(workbookSheet).getCells().get(columnIndex + rowOffset, axis-1).setValue(ColumnData.get(columnIndex).intValue());  //Adds the data held at columnIndex to each row of the data
                }
            }
        }
    }

    /**
     *This class writes the MPUOffsets of the module to their appropriate location in the Conservation of Momentum Test Template.
     * @param rowOffset For Momentum Template, should be 2
     * @param columnOffset For Momentum Template, should be 1
     * @param MPUMinMax What is being written
     * @param workbookSheet The sheet that it is being written to; for two module templates, multiple MPUMinMax may be written to the same workbook.
     */

    public void writeMPUMinMaxToMomentumTemplate(int rowOffset, int columnOffset, int MPUMinMax [][], int workbookSheet){
        for(int axis = 0; axis < MPUMinMax.length; axis++){  // for this particular case, the axis can be thought of as the rows of the grid of MPUOffsets. Therefore, the for loop iterates through each row of the grid. The grid has two columns.
            workbook.getWorksheets().get(workbookSheet).getCells().get(rowOffset+axis, columnOffset).setValue(MPUMinMax[axis][0]); // For the first column of the MPUOffsets, the value of the current row is written to its location in the workbook.
            workbook.getWorksheets().get(workbookSheet).getCells().get(rowOffset +axis, columnOffset+1).setValue(MPUMinMax[axis][1]); // For the second column of the MPUOffsets, the value of the current row is written to its location in the workbook.
        }
    }

    /**
     * This class writes the Test Params to their appropriate location in the Conservation of Momentum Test Template.
     * @param rowOffset For Momentume Template, should be 11
     * @param columnOffset For Momentum Template, should be 1
     * @param params What is being written
     * @param workbookSheet The sheet that it is being written to; for two module templates, multiple MPUMinMax may be written to the same workbook.
     */
    public void writeTestParamsToMomentumTemplate(int rowOffset, int columnOffset, List<Integer> params, int workbookSheet){
        for (int i = 0; i < params.size()-10; i++){// this loop iterates through the list of values for the parameters, and writes parameters to their locations. when i = 0 , the first parameter will be accessed and written, i = 1 will result in the access and writing of the second parameter, and so on.
            workbook.getWorksheets().get(workbookSheet).getCells().get(i+rowOffset,columnOffset).setValue(params.get(i+6));// Each parameter is written to the row equal to the parameters position in the list plus a certain offset. For example, parameter 1 may be wrirten to row 12, parameter 2 to row 13, and so on. The data is written to a single column, so the column that it is written to is equal to the column offset and does not change.
        }
    }

    public void writeTMR0AndDelayAfterStartToMomentumTemplate(int tmr0, int delayAfterStart){
        workbook.getWorksheets().get(1).getCells().get(16,1).setValue(tmr0);
        workbook.getWorksheets().get(1).getCells().get(17,1).setValue(delayAfterStart);
    }

}
