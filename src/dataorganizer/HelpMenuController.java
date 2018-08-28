package dataorganizer;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

public class HelpMenuController implements Initializable{

	HashMap<Integer, String> tabIndexList = new HashMap<Integer, String>(7);
	int currentTab;
	
	@FXML
	Pane readModeBSPane = new Pane();
	@FXML
	TextArea readModeBSTextArea = new TextArea();
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		setKeys();
	}
	
	/* Set hashmap keys. Hashmap is used for code readability, the AdvancedMode passes an index (via setTabIndex()) representing the tab that the user is
	 * on when they press the help button. This will tell us what pane to display 
	 */
	public void setKeys() { 
		tabIndexList.put(0, "Read Mode");
		tabIndexList.put(1, "Configurations");
		tabIndexList.put(2, "Test/Erase");
		tabIndexList.put(3, "Calibration");
		tabIndexList.put(4, "Remote Configuration");
		tabIndexList.put(5, "Stored Tests");
	}
	
	// Passed index represents current tab in AdvancedMode 
	public void setTabIndex(int tabIndex) {
		currentTab = tabIndex;
	}
	
	
	
	
}
