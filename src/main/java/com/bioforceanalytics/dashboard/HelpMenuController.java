package com.bioforceanalytics.dashboard;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;

/**
 * Manages the display of a help menu in the advanced mode dashboard.
 */

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
	@FXML
	Label debugInfo;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		debugInfo.setText("Version: " + Settings.getVersion() + " | Build Date: " + Settings.getBuildDate());
		setKeys();
	}
	
	/* Set hashmap keys. Hashmap is used for code readability, the AdvancedMode passes an index (via setTabIndex()) representing the tab that the user is
	 * on when they press the help button. This will tell us what pane to display 
	 */
	public void setKeys() {

		tabIndexList.put(0, "Erase");
		tabIndexList.put(1, "Remote Control");
		tabIndexList.put(2, "Configure Test Parameters");
		tabIndexList.put(3, "Read Tests");
		tabIndexList.put(4, "Stored Tests");
		tabIndexList.put(5, "IMU Calibration");
		tabIndexList.put(6, "SINC Calibration ");
		tabIndexList.put(7, "Java Graph");
		tabIndexList.put(8, "Excel Graph");
		tabIndexList.put(9, "Admin");

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
				BullitizedStepsLabel.setText("1. \"Sector Erase\" deletes the sectors that are recorded to have test data. \n2. \"Bulk Erase\" deletes all data on the module. This is left here as a debug step \ninternally.");
				DSPageTab.setDisable(true);
				break;
			case 1:
				BullitizedStepsLabel.setText("1. If you have a remote paired to the module, click on the \"Unpair New Remote\"\n\t button.\n2. Click \"Pair New Remote.\"\n\t1. Press a button on your remote, and wait for the \"New Remote Successfully\n\t\t Paired\" message in the status area. \n3. Optionally, you may test the remote you have just paired by clicking \"Test Paired\n\t Remotes\" button. \n\t1. You can see that the module detects a remote press by checking \n\t\t in the status area. \n\t2. When you're done testing the remote click \"Exit Test Mode\"");
				DSPageTab.setDisable(true);
				break;
			case 2:
				BullitizedStepsLabel.setText("1. When checked, the \"Timed Test\" checkbox will use the \"Test Duration\" to time\r\t the test. Otherwise the test must be manually exited. \n2. When checked, the \"Trigger on Release\" checkbox will make sure the module waits\n\t for you to release the trigger on the remote. Otherwise the test will be started\n\t immediately on button press.\n3. ALWAYS write the configurations to the module with \"Write Configurations\" button.\n\t You can verify the configurations were written correctly with the \"Get Current\n\t Configurations\" button.");
				DetailedStepsLabel.setText("1. \"Sample rate\" may be specified in the Accel/Gyro Sample Rate combobox. \"Magnetometer \n Sample\" Rate is calculated accordingly.\n2. The \"Accelerometer Sensitivity\" is in multiples of Earth gravity(G). At \"2G\" the maximum\r\t reading would be twice 9.8m/s^2 before saturation. \n3. \"Gyroscope Sensitivity\" is in degrees per second (d/s). \"Test Duration\" is the length of the test, \n if the module is set to take timed tests. \"Battery Timeout Length\" is the time that the module will \n stay on without user interaction.");
				/**
				DetailedStepsLabel.setText("•\tTimed Test- Selecting this will limit data collection to the number of seconds input in the “Test Duration” field. If “un-selected” data will be collected from when the module is “turned on” until it is “turned off’. \n" +
						"•\tTrigger on Release- Selecting this will turn the module on once the remote’s button is released by the user. This gives the user more control over the start/stop time of the module. If un-selected, the module will turn on as soon as the remote’s button is pressed. \n" +
						"•\tAccel/Gyro Sample Rate- Sets the samples-per-second (sps) rate for both the accelerometer and the gyroscope. Both sensors will collect data at the same rate. Options are: 60, 120, 240, 480, 500, and 960 sps. The higher the sps the greater amount of data collected during a trial.\n" +
						"•\tAccelerometer Sensitivity- Sets the sensitivity of the accelerometer to increase or decrease the “resolution” of the data. Options are 2, 4, 8, or 16 g’s. For greater resolution of data, select a lower number, i.e. 2g’s is the greatest resolution. Accelerometer and Gyroscope sensitivity are independent of each other. \n" +
						"•\tAccelerometer Filter- Digital filters built into the module firmware to reduce  electronic “noise” inherent in all circuits. Increasing the filter level will also reduce sample resolution. The lower the number the greater the filtering effect. The default filter is 92, with Dashboard options of 5, 10, 20, 41, 92, 184, 460, and 1130 (OFF). \n" +
						"•\tMagnetometer Sample Rate- There is only one (1) sample rate for the magnetometer of 96 sps. This sample rate is independent of the accel/gyro sampling rate. At 960 sps for the accel/gyro, this would equate to a 10 : 1 ratio of sample accel/gyro : magnetometer. \n" +
						"•\tGyroscope Sensitivity- Sets the sensitivity of the gyroscope to increase or decrease the “resolution” of the data. Options are 250, 500, 1000, and 2000 degrees per second (dps). For greater resolution of data, select a lower number, i.e. 250 dps is the greatest resolution. Accelerometer and Gyroscope sensitivity are independent of each other.\n" +
						"•\tGyroscope Filter- Digital filters built into the module firmware to reduce  electronic “noise” inherent in all circuits. Increasing the filter level will also reduce sample resolution. The lower the number the greater larger the filtering effect. The default filter is 92, with Dashboard options of 10, 20, 41, 92, 184, 250, 3600 and 8800 (OFF).\n" +
						"•\tTest Duration-Sets the duration of a test (in seconds) when “Timed Test” is selected. \n" +
						"•\tBattery Timeout Length-Sets the length of time in seconds (1-300 seconds) before the module automatically shuts off and stops collecting data. The module can be manually stopped at any time before the timeout with the remote. \n" +
						"•\tWrite Configurations- Pressing this button will write the parameters set in the fields to be written to the module’s system memory. The Status bar will turn from RED to GREEN to indicate successful configuration. \n" +
						"•\tGet Current Configurations- Used to display the configuration currently written on the module. \n" +
						"•\tTimer0 Tick Threshold & Delay After Start- Read only fields to display data related to synchronizing the module with a video recording device for use in SINC™ Technology. \n"); */
				break;
			case 3:
				BullitizedStepsLabel.setText("1. Read the tests from the module with the \"Read Data from Module\" button. \n2. Optionally you may change the name of the file with the \"File name\" text field, otherwise the\n\t name will be generated from the parameters of the module and dated\n3. You can change the output directory from its default (your Documents folder), in the settings");
				DetailedStepsLabel.setText("1. Read the tests from the module with the \"Read Data from Module\" button. \n\t1. Use the \"Signed Data\" CheckBox to sign the data (raw data is 0 to 65535, signed is \n\t\t -32768 to 32768)\n\t2. Use the \"Label Data in CSV\" checkbox to add a time axis to simplify graphing. This is useful \n\t\t for ELAN, or Excel. \n2. You may suffix and prefix the name of the file with the \"File Name Prefix\" and \"File Name Suffix\" \n\t\trespectively. The suffix will be entered BEFORE the file extension.\n3. Use the browse button to change the output directory of the CSV's. When setting the output \n\tdirectory in this way, it will only change during this session of the dashboard. If you would like \n\tto permanently change it, do so in the settings.\n4. In depth definitions of the parameters shown below can be found in the \"Configurations\" \n\ttab help menu, but from this tab they are only viewable.");
				break;
			case 4:
				BullitizedStepsLabel.setText("1. This menu is populated after reading tests from the module.\n2. Go to the \"Read mode\" Tab and Click \"Read Data from Module\" to populate the list.\n3. The maximum number of displayable tests is 8.");
				DSPageTab.setDisable(true);
				break;
			case 5:
				BullitizedStepsLabel.setText("1)\tUse the Configure Test Parameters Tab* and the Remote Control Tab to configure the module and pair a remote.\n" +
						"2)\tStart test- \n" +
						"\ta. Place the module flat along the X axis for 3 seconds, then rotate the module along the X-axis\n" +
						"\tb. Place the module flat along the Y axis for 3 seconds, then rotate the module along the Y-axis\n" +
						"\tc. Place the module flat along the Z axis for 3 seconds, then rotate the module along the Z-axis\n" +
						"3)\tStop the test with the remote and reconnect the module\n" +
						"4)\tRead the test (Read Tests Tab) and save as a csv. file\n" +
						"5)\tSelect Browse and select the csv. Location\n" +
						"6)\tSelect “Calibrate”\n");
				break;
			case 6:
				BullitizedStepsLabel.setText("1. Make sure you have paired a remote that will connect to your video recording\n\t device, and the module!\n2. \"Configure Module for Calibration\" puts the module into calibration mode.\n3. Once in calibration mode, run the calibration test.\n\t1. Take the module to a dark room and place the module and LED in frame of \n the Video Recording Device.\n\t2. Using the remote paired to both the Video Recording Device and Module,\n\t start the test.\n\t3. The calibration test will take 2 minutes. On completion of the test,\n\t load the video on to your computer and plug the module back in.\n4. In the dashboard click \"Browse\" and select the video you have just recorded.\n5. You may now click \"Import Calibration Data and Calculate Offset.\" The text fields \n will populate.\n6. You may click \"Apply Offset to Module\"");
				DSPageTab.setDisable(true);
				break;
			case 7:
				BullitizedStepsLabel.setText("1)\tClick the button labelled “Graph”");
				DetailedStepsLabel.setText("•\tMotion data is displayed on the Vertical axis, time is displayed on the horizontal axis.\n" +
						"•\t“File” (drop-down menu located at the top left of the graph).\n" +
						"\to\tUse the menu to manually import a CSV file or video file for SINC Technology. \n" +
						"\to\tUse the menu to delete data from either Set 1 or Set 2\n" +
						"\to\tUse the menu to change the color of the graphed data lines.\n" +
						"•\tMenu Bar (right hand side)\n" +
						"\to\tData Analysis Tools Tab\n" +
						"\t\t\uF0A7\tRaw & Signed Data-select either option to display data\n" +
						"\t\t\uF0A7\tY-Axis Maximum & Minimum \n" +
						"\t\t\t•\tSelected the max/min value to display on the graph\n" +
						"\t\t\t•\tMoving Average\n" +
						"\to\tDisplay data for one or two modules (“Data Set 1” and “Data Set 2”)\n" +
						"\t\t\uF0A7\tFile name will display in the tab once imported\n" +
						"\t\t\t•\tData Series Box\n" +
						"\t\t\t\to\tSelect which set of data to graph by “checking” the respective box.\n" +
						"\t\t\t•\tAccel Magnitude\n" +
						"\t\t\t\to\tGraph the Accelerometer Magnitude by selecting the box. \n" +
						"Video Controls\n" +
						"•\tFunction Buttons\n" +
						"\t•\tPlay – playback the video\n" +
						"\t\t\uF0A7\tThe scale at the bottom allows the user to select the playback speed.\n" +
						"\t\t\uF0A7\tTime elapsed/ Total time displayed\n" +
						"\t•\tReset- Start video from the beginning\n" +
						"\t•\tFile- Select an MP4 file to display\n" +
						"\t•\tSend to Back / Bring to Front\n" +
						"\t\t\uF0A7\tAllows the Graph to be put in front of or behind the video for better viewing.\n" +
						"\t•\tVideo Opacity – Change the opacity of the video\n");
				break;
			case 8:
				BullitizedStepsLabel.setText("1)\tSelect template use wish to populate with data from the dropdown menu\n" +
						"2)\tSelect either the “Create One Module Template” or the “Create Two Module Template” button\n"+"\t\uF0A7 If you are using one module, the CSV you wish to extract data from should \n\t be named 'Module 1' (no quotes)\n"+"\t\uF0A7 If you are using two modules, one CSV should be named Module 1 and the \n\t other should be named Module 2\n");
				DSPageTab.setDisable(true);
				break;
			case 9:
				BullitizedStepsLabel.setText("1)\tEnter the password to open the tab. (The default password is 1234)\n" +
						"2)\tEdit the Serial Number and click “Write” to update the system memory.\n" +
						"3)\tEdit the Model Number and click “Write” to update the system memory.\n");
				DSPageTab.setDisable(true);
				break;
		}
	}

}
