package dataorganizer;

import com.aspose.cells.*;

import java.util.List;

import javax.swing.JOptionPane;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JFileChooser;

public class PendulumSpreadsheetController {


    //TODO: Test if Workbook can be saved to a different location than its origin (Ex. Leave template in src and save the updated version to Documents)
    //TODO: Dynamically get path of template
    private Workbook workbook; 
    private String documentsPath;
    
    public PendulumSpreadsheetController() {
        documentsPath = System.getProperty("user.home") + "\\Documents\\Lab Templates\\Pendulum Template REV-Q3.xlsx";
        try {
            this.workbook = new Workbook(documentsPath);
        }catch(Exception e){
        	e.printStackTrace();
            System.out.println("Invalid Workbook Path");
        }
    }
    
//    public void chooseoutputPath() {
////    	JFrame outputpathChooser = new JFrame();
////    	String test = JOptionPane.showInputDialog(outputpathChooser, "Enter or choose the save location for spreadsheet");
////    	
//    	JFrame output = new JFrame();
//    	
//    	JFileChooser outputpathChooser;
//    	outputpathChooser = new JFileChooser();
//    	
//    	output.add(outputpathChooser);
//    	output.setVisible(true);
//    	
//    	System.out.println(test);
//    }
    
//    public void chooseoutputPath(JLabel label ) {
//		JFileChooser chooser;
//		chooser = new JFileChooser();
//		chooser.setCurrentDirectory(new java.io.File("."));
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setAcceptAllFileFilterUsed(false);
//		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//			saveWorkbook(chooser.getSelectedFile().toString());
//		}
//		else {
//			label.setText("Invalid File Path");
//		}
//	}
    	
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


    public void loadPendulumParameters (double pendulumLength, double pendulumMass, double moduleMass, double moduleDistanceFromAOR){
        workbook.getWorksheets().get(4).getCells().get("C7").setValue(pendulumLength);
        workbook.getWorksheets().get(4).getCells().get("C8").setValue(pendulumMass);
        workbook.getWorksheets().get(4).getCells().get("C9").setValue(moduleMass);
        workbook.getWorksheets().get(4).getCells().get("C10").setValue(moduleDistanceFromAOR);

//        System.out.println(pendulumLength);
//        System.out.println(pendulumMass);
//        System.out.println(moduleMass);
//        System.out.println(moduleDistanceFromAOR);

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
