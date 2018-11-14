package dataorganizer;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;

public class HelpMenuController implements Initializable{

					//Index  //tab name
	private HashMap<Integer, String> tabIndexList = new HashMap<Integer, String>(7);
	private int currentTab;
	
	@FXML
	Pane holderPane = new Pane();
	@FXML
	Label BullitizedStepsLabel = new Label();
	@FXML
	Label DetailedStepsLabel = new Label();
	@FXML
	Label titleDSPageLabel = new Label();
	@FXML
	Label titleBSPageLabel = new Label();
	@FXML
	Tab DSPageTab = new Tab();
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		setKeys();
	}
	
	/* Set hashmap keys. Hashmap is used for code readability, the AdvancedMode passes an index (via setTabIndex()) representing the tab that the user is
	 * on when they press the help button. This will tell us what pane to display 
	 */
	public void setKeys() { 
		tabIndexList.put(0, "Read Mode");
		tabIndexList.put(2, "Configurations");
		tabIndexList.put(3, "Test/Erase");
		tabIndexList.put(4, "Calibration");
		tabIndexList.put(5, "Remote Configuration");
		tabIndexList.put(1, "Stored Tests");
	}
	
	// Passed index represents current tab in AdvancedMode 
	public void setTabIndex(int tabIndex) {
		currentTab = tabIndex;
	}
	
	public void populateLabels() {
		DSPageTab.setDisable(false);
		titleDSPageLabel.setText(tabIndexList.get(currentTab));
		titleBSPageLabel.setText(tabIndexList.get(currentTab));
		switch(currentTab) {
			case 0:
				BullitizedStepsLabel.setText("1. Read the tests from the module with the \"Read Data from Module\" button. \n2. Optionally you may change the name of the file with the \"File name\" text field, otherwise the\n\t name will be generated from the parameters of the module and dated\n3. You can change the output directory from its default (your Documents folder), in the settings");
				DetailedStepsLabel.setText("1. Read the tests from the module with the \"Read Data from Module\" button. \n\t1. Use the \"Signed Data\" CheckBox to sign the data (raw data is 0 to 65535, signed is \n\t\t -32768 to 32768)\n\t2. Use the \"Label Data in CSV\" checkbox to add a time axis to simplify graphing. This is useful \n\t\t for ELAN, or Excel. \n2. You may suffix and prefix the name of the file with the \"File Name Prefix\" and \"File Name Suffix\" \n\t\trespectively. The suffix will be entered BEFORE the file extension.\n3. Use the browse button to change the output directory of the CSV's. When setting the output \n\tdirectory in this way, it will only change during this session of the dashboard. If you would like \n\tto permanently change it, do so in the settings.\n4. In depth definitions of the parameters shown below can be found in the \"Configurations\" \n\ttab help menu, but from this tab they are only viewable.");
				break;
			case 1: 
				BullitizedStepsLabel.setText("1. This menu is populated after reading tests from the module.\n2. Go to the \"Read mode\" Tab and Click \"Read Data from Module\" to populate the list.\n3. The maximum number of displayable tests is 8.");
				DSPageTab.setDisable(true);
				break;
			case 2:
				BullitizedStepsLabel.setText("1. When checked, the \"Timed Test\" checkbox will use the \"Test Duration\" to time\r\t the test. Otherwise the test must be manually exited. \n2. When checked, the \"Trigger on Release\" checkbox will make sure the module waits\n\t for you to release the trigger on the remote. Otherwise the test will be started\n\t immediately on button press.\n3. ALWAYS write the configurations to the module with \"Write Configurations\" button.\n\t You can verify the configurations were written correctly with the \"Get Current\n\t Configurations\" button.");
				DetailedStepsLabel.setText("1. \"Sample rate\" may be specified in the Accel/Gyro Sample Rate combobox. \"Magnetometer \n Sample\" Rate is calculated accordingly.\n2. The \"Accelerometer Sensitivity\" is in multiples of Earth gravity(G). At \"2G\" the maximum\r\t reading would be twice 9.8m/s^2 before saturation. \n3. \"Gyroscope Sensitivity\" is in degrees per second (d/s). \"Test Duration\" is the length of the test, \n if the module is set to take timed tests. \"Battery Timeout Length\" is the time that the module will \n stay on without user interaction.");
				break;
			case 3:
				BullitizedStepsLabel.setText("1. \"Sector Erase\" deletes the sectors that are recorded to have test data. \n2. \"Bulk Erase\" deletes all data on the module. This is left here as a debug step \ninternally.");
				DSPageTab.setDisable(true);
				break;
			case 4: 
				BullitizedStepsLabel.setText("1. Make sure you have paired a remote that will connect to your video recording\n\t device, and the module!\n2. \"Configure Module for Calibration\" puts the module into calibration mode.\n3. Once in calibration mode, run the calibration test.\n\t1. Take the module to a dark room and place the module and LED in frame of \n the Video Recording Device.\n\t2. Using the remote paired to both the Video Recording Device and Module,\n\t start the test.\n\t3. The calibration test will take 2 minutes. On completion of the test,\n\t load the video on to your computer and plug the module back in.\n4. In the dashboard click \"Browse\" and select the video you have just recorded.\n5. You may now click \"Import Calibration Data and Calculate Offset.\" The text fields \n will populate.\n6. You may click \"Apply Offset to Module\"");
				DSPageTab.setDisable(true);
				break;
			case 5: 
				BullitizedStepsLabel.setText("1. If you have a remote paired to the module, click on the \"Unpair New Remote\"\n\t button.\n2. Click \"Pair New Remote.\"\n\t1. Press a button on your remote, and wait for the \"New Remote Successfully\n\t\t Paired\" message in the status area. \n3. Optionally, you may test the remote you have just paired by clicking \"Test Paired\n\t Remotes\" button. \n\t1. You can see that the module detects a remote press by checking \n\t\t in the status area. \n\t2. When you're done testing the remote click \"Exit Test Mode\"");
				DSPageTab.setDisable(true);
				break;
		}
	}

}
