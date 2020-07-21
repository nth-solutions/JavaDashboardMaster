package dataorganizer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;

/**
 * Custom JavaFX component for the Data Analysis Graph's panel of checkboxes toggling graphing of data sets.
 * Created so that data set panels can be dynamically created depending on the number of modules in a test.
 */
public class ExperimentPanel extends TitledPane {

    public ExperimentPanel(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ExperimentPanel.fxml"));
		loader.setRoot(this);
        loader.setController(this);
        
        try {
			loader.load();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error loading ExperimentPanel JavaFX component");
		}
    }

}