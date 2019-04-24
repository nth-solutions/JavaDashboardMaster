package dataorganizer;

import com.aspose.cells.*;

public class PendulumSpreadsheetController {


    //TODO: Dynamically get path of template
    private Workbook workbook;
    private String workbookPath1 = "C:\\Users\\Kinobo\\Documents\\JavaDashboardMaster\\Current Version\\JavaDashboardMaster - 2019-04-23 - EndOfDay - Working Copy\\src\\dataorganizer\\Pendulum Template REV-Q3 1.xlsx";

//    public PendulumSpreadsheetController(String workbookPath) throws Exception {
//        workbook = new Workbook(workbookPath);
//        workbookPath1 = workbookPath;
//    }



    public void loadPendulumParameters (double pendulumLength, double pendulumMass, double moduleMass, double moduleDistanceFromAOR){ //
        try{
            workbook = new Workbook(workbookPath1);
        }
        catch(Exception eg){
            System.out.println("test");
        }
        workbook.getWorksheets().get(4).getCells().get("C7").setValue(pendulumLength);
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(pendulumMass);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(moduleMass);
        workbook.getWorksheets().get(4).getCells().get("C10").setValue(moduleDistanceFromAOR);
        System.out.println(pendulumLength);
        System.out.println(pendulumMass);
        System.out.println(moduleMass);
        System.out.println(moduleDistanceFromAOR);

        try{
            workbook.save(workbookPath1, FileFormatType.XLSX);
        }
        catch(Exception e){
            System.out.println("Invalid Path");
        }
    }



}
