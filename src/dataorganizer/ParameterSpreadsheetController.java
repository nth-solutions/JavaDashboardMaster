package dataorganizer;

import com.aspose.cells.*;

import java.util.List;

public class ParameterSpreadsheetController {

    private Workbook workbook;
    private String documentsPath;
    private String testTypeFileName;
    private String testType;

    public ParameterSpreadsheetController() {

        testType = EducatorMode.testType;

        if (testType == "Conservation of Momentum (Elastic Collision)"){

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
        documentsPath = System.getProperty("user.home") + "\\Documents\\Lab Templates\\" + testTypeFileName;
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

    public void loadSpringTestParameters(double springConstant, double totalMass, double amplitude, double massOfSpring) {
        workbook.getWorksheets().get(4).getCells().get("C7").setValue(springConstant);
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(totalMass);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(amplitude);
        workbook.getWorksheets().get(4).getCells().get("C10").setValue(massOfSpring);
    }
//    public void loadConservationofMomentumParameters (){
//        workbook.getWorksheets().get(4).getCells().get("C7").setValue(pendulumLength);
//        workbook.getWorksheets().get(4).getCells().get("C8").setValue(pendulumMass);
//        workbook.getWorksheets().get(4).getCells().get("C9").setValue(moduleMass);
//        workbook.getWorksheets().get(4).getCells().get("C10").setValue(moduleDistanceFromAOR);
//    }
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
