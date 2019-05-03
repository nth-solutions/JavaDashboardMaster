package dataorganizer;

import com.aspose.cells.*;

import java.util.List;

public class SpinnyStoolSpreadsheetController {

    private Workbook workbook;
    private String documentsPath;


    public SpinnyStoolSpreadsheetController() {

        documentsPath = System.getProperty("user.home") + "\\Documents\\Lab Templates\\Pendulum Template REV-Q3.xlsx";
        try {
            this.workbook = new Workbook(documentsPath);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Invalid Workbook Path");
        }
    }

    public void loadSpinnyStoolParameters(double massHandWeights, double wingspan, double massOfPerson, double shoulderWidth) {
        workbook.getWorksheets().get(4).getCells().get("C7").setValue(massHandWeights);
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(wingspan);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(massOfPerson);
        workbook.getWorksheets().get(4).getCells().get("C10").setValue(shoulderWidth);
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

