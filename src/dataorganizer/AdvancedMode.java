
package dataorganizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JTabbedPane;
import java.awt.Font;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;

import java.awt.CardLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.PureJavaIllegalStateException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import javax.swing.JSeparator;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * AdvancedMode.java
 * Purpose: This class handles the advanced gui and interacts with the SerialComm.java utility class to talk to the module
 *
 * Design Patterns: This class exclusively handles GUI elements and calls external methods for any other operations. Every action event should call a handler method
 * 					which sets the GUI elements, defines a Runnable operation then starts that operation in a separate thread. No GUI elements can be updated until
 * 					the action event completes so by starting the actual operation in a separate thread then completing the action event, the dashboard will update.
 * 					Additionally, most methods return a boolean if not a data structure in order to allow for easy exiting of the method if the execution is unsuccessful
 * 					If a method is unsuccessful, it will return false or return a null data structure (such as a null ArrayList). In many cases, it is necessary to surround
 * 					a method call with an if statement to track whether or not the method successfully completed.* 
 *
 * COMMENT TERMINOLOGY:
 *  - Caller: The class, object, location, or method in which the currently executing method was called. ex) The bulkEraseHandler() 'Calls' the bulkEraseModule() method so the bulkEraseHandler() is the caller
 *  - User: The person using the dashboard, not the programmer.
 */

public class AdvancedMode extends JFrame {


	//GUI Elements, define here if they need to be accessed in the Dashboard class. To quickly identify what panel you want to reference, open the GUI in
	//WindowBuilder, click on the desired GUI item and see what it's "Variable" field name is. It will be defined at the bottom in the initComponents() method by default so move the definition up here

	//Panels
	private JPanel contentPanel;
	private JPanel mainPanelContainer;
	private JTabbedPane mainTabbedPanel;
	private JPanel startReadButtonPanel;
	private JPanel fileNamePanel;
	private JPanel paramPanel;
	private JPanel getModuleIDPanel;
	private JPanel fileNameModifierPanel;
	private JPanel fileLocationPanel;
	private JPanel serialPortPanel;
	private JPanel adminPanel;
	private JPanel erasePanel;
	private JPanel eraseButtonPanel;
	private JPanel remoteTab;
	private JPanel RemoteButtonPanel;
	private JPanel VideoFilePane;

	//Labels
	private JLabel generalStatusLabel;
	private JLabel moduleSerialNumberLabel;
	private JLabel hardwareIDLabel;
	private JLabel firmwareIDLabel;

	//CheckBoxes
	private JCheckBox timedTestCheckbox;
	private JCheckBox triggerOnReleaseCheckbox;

	//Text Fields

	//Configuration Tab
	private JTextField testLengthTextField;

	private JTextField magSampleRateTextField;
	private JTextField delayAfterStartTextField;
	private JTextField timer0TickThreshTextField;
	private JTextField batteryTimeoutTextField;

	//Read Tab
	private JTextField prefixTextField;
	private JTextField suffixTextField;
	private JTextField fileNameTextField;
	private JTextField numTestsTextFieldRead;
	private JTextField testLengthTextFieldRead;
	private JTextField accelGyroSampleRateTextFieldRead;
	private JTextField magSampleRateTextFieldRead;
	private JTextField accelSensitivityTextFieldRead;
	private JTextField gyroSensitivityTextFieldRead;
	private JTextField accelFilterTextFieldRead;
	private JTextField gyroFilterTextFieldRead;
	private JTextField VideoFileTextField;

	//Calibration Tab
	private JTextField tmr0OffsetTextField;

	//Combo Boxes
	private JComboBox commPortCombobox;
	private JComboBox accelGyroSampleRateCombobox;
	private JComboBox accelSensitivityCombobox;
	private JComboBox gyroSensitivityCombobox;
	private JComboBox accelFilterCombobox;
	private JComboBox gyroFilterCombobox;

	//Buttons
	private JButton refreshPortButton;
	private JButton disconnectButton;
	private JButton writeConfigsButton;
	private JButton getModuleIDButton;
	private JButton browseButton;
	private JButton readDataButton;
	private JButton bulkEraseButton;
	private JButton sectorEraseButton;
	private JButton settingsWindowBtn;
	private JButton btnSelectCsv;
	private JButton sendQuitCMDButton;
	private JButton pairNewRemoteButton;
	private JButton getCurrentConfigurationsButton;
	private JButton testRemotesButton;
	private JButton exitTestModeButton;
	private JButton unpairAllRemotesButton;
	private JButton startTestBtn;
	private JButton configForCalButton;
	private JButton importCalDataButton;
	private JButton applyOffsetButton;
	private JButton browseVideoBtn;
	private ArrayList<JButton> saveTestBtn;
	private ArrayList<JButton> graphTestBtn;

	//Progress Bars
	private JProgressBar progressBar;

	//JSeparators
	private JSeparator separator;

	//Test Parameter Variables and Constants
	public static final int NUM_TEST_PARAMETERS = 13;
	public static final int NUM_ID_INFO_PARAMETERS = 3;
	public static final int CURRENT_FIRMWARE_ID = 23;
	public static final String CURRENT_FIRMWARE_STRING = "23";

	private int expectedTestNum;
	//Test Parameters (All must be of type "int")
	private int timedTestFlag;
	private int triggerOnReleaseFlag;
	private int battTimeoutLength;
	private int timer0TickThreshold;
	private int delayAfterStart;
	private int testLength;      			
	private int accelGyroSampleRate;    		
	private int magSampleRate;          			
	private int accelSensitivity;       		
	private int gyroSensitivity;        		
	private int accelFilter;            			
	private int gyroFilter;             		


	//Operation Threads
	private Thread readThread;
	private Thread paramThread;
	private Thread infoThread;
	private Thread organizerThread;
	private Thread bulkEraseThread;
	private Thread sectorEraseThread;
	private Thread pairNewRemoteThread;
	private Thread unpairAllRemotesThread;
	private Thread testRemoteThread;
	private Thread getParamThread;

	//Output File Info and Variables
	private String nameOfFile = "";     			//Sets the name of file to an empty string to start
	private String fileOutputDirectoryStr;			//The directory to write the test to
	private String templateChosen;
	private String csvDataFileChosen;
	private static SerialComm serialHandler;
	private String videoFileInput;
	private CSVBuilder csvBuilder = new CSVBuilder();  //Object of class used to organize passed in data to convert and format data into .CSV
	//private List<DataOrganizer> tests = new ArrayList<>();
	//Flags
	private boolean frameInitialized = false;
	private boolean corruptConfigFlag = false;

	//Serial Port Variables
	private SerialPort serialPort;      			//Object for the serial port class
	private static CommPortIdentifier portId;       //Object used for opening a COMM ports
	private static Enumeration portList;            //Object used for finding COMM ports
	private BufferedInputStream inputStream;             //Object used for reading serial data 
	private OutputStream outputStream;              //Object used for writing serial data

	public static AdvancedMode guiInstance;		//The single instance of the dashboard that can be referenced anywhere in the class. Defined to follow the Singleton Method: https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples		
	private JPanel calOffsetsPanel;
	private JTextField delayAfterTextField;
	private JPanel videoBrowsePanel;
	private JTextField videoFilePathTextField;
	private JButton videoBrowseButton;
	private JPanel panel;
	private JCheckBox checkBoxLabelCSV;
	private JCheckBox checkBoxSignedData;

	private ArrayList<Integer> testParameters = new ArrayList<Integer>();
	private JPanel testRecordationPanel;
	private ArrayList<JPanel> testNumPaneArray;
	private ArrayList<JTextField> testNameTextField;
	private final JFXPanel graphingPanel = new JFXPanel();


	/**
	 * Dashboard constructor that initialzies the name of the window, all the components on it, and the data within the necessary text fields
	 */
	AdvancedMode() {
		setTitle("JavaDashboard Rev-14");
		createComponents();
		initDataFields();
		updateCommPortComboBox();
		setVisible(true);
		findModuleCommPort();
	}

	/**
	 * Necessary for singleton design pattern, especially the "synchronized" keyword for more info on the singleton method: https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples
	 * @return the one and only allowed dashboard instance, singleton pattern specifies only one instance can exist so there are not several instances of the dashboard with different variable values
	 */
	public static synchronized AdvancedMode getFrameInstance() {
		if (guiInstance == null) {
			guiInstance = new AdvancedMode();
		}
		return guiInstance;
	}


	/**
	 * Main execution loop.
	 * @param args
	 */
	public static void main(String args[]) {

		//Set the look and feel to whatever the system default is.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch(Exception e) {
			System.out.println("Error Setting Look and Feel: " + e);
		}

		serialHandler = new SerialComm();
		//Default the gui that will be opened to null (gui selected in following try/catch block
		AdvancedMode gui = new AdvancedMode();


		//System.out.println(System.getProperty("os.name"));


		//Loop infinitely so window doesn't close unless user presses close button
		while(true) {
		}
	}

	/**
	 * Updates the ports combobox with the string ID's of the available serial ports
	 */
	public void updateCommPortComboBox() {
		ArrayList<String> commPortIDList = serialHandler.findPorts();
		if (commPortIDList != null) {
			commPortCombobox.setEnabled(true);
			commPortCombobox.setModel(new DefaultComboBoxModel(commPortIDList.toArray()));
		}
		else {
			generalStatusLabel.setText("No Serial Dongle Found");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255, 0, 0));
		}

	}

	public void findModuleCommPort() {
		Runnable findModuleOperation = new Runnable() {
			public void run() {
				try {
					ArrayList<String> commPortIDList = serialHandler.findPorts();
					boolean moduleFound = false;
					int commPortIndex = 0;
					if(commPortIDList != null)
						while (!moduleFound && commPortIndex < commPortIDList.size()) {
	
							//Get the string identifier (name) of the current port
							String selectedCommID = commPortCombobox.getItemAt(commPortIndex).toString();      
	
							//Open the serial port with the selected name, initialize input and output streams, set necessary flags so the whole program know that everything is initialized
							if(serialHandler.openSerialPort(selectedCommID)){
	
								int attemptCounter = 0;
								while (attemptCounter < 10 && !moduleFound) {
									try {
										ArrayList<Integer> moduleIDInfo = serialHandler.getModuleInfo(NUM_ID_INFO_PARAMETERS);
	
										if (moduleIDInfo != null) {
											moduleFound = true;
	
											moduleSerialNumberLabel.setText("Module Serial Number: " + moduleIDInfo.get(0));
											hardwareIDLabel.setText("Module Hardware ID: " + moduleIDInfo.get(1) + "x");
											firmwareIDLabel.setText("Module Firmware ID: " + moduleIDInfo.get(2));
											if (moduleIDInfo.get(2) != CURRENT_FIRMWARE_ID) {
												generalStatusLabel.setText("Incompatable Firmware Version: " + moduleIDInfo.get(2) + ", Program Module with Version " + CURRENT_FIRMWARE_STRING);
												progressBar.setValue(100);
												progressBar.setForeground(new Color(255, 0, 0));	
											}
											else {
												enableTabChanges();
												generalStatusLabel.setText("Successfully Connected to Module");
												progressBar.setValue(100);
												progressBar.setForeground(new Color(51, 204, 51));
	
												//Enable the buttons that can now be used since the serial port opened
												disconnectButton.setEnabled(true);
												getModuleIDButton.setEnabled(true);
												readDataButton.setEnabled(true);
												writeConfigsButton.setEnabled(true);
												getCurrentConfigurationsButton.setEnabled(true);
												mainTabbedPanel.setEnabled(true);
												//Disable COMM port combobox so the user doesn't accidentally reopen a port
												commPortCombobox.setEnabled(false);
											}
										}
										else {
											attemptCounter++;
										}
									}
									catch (IOException e) {
										attemptCounter++;
									}
									catch (PortInUseException e) {
										attemptCounter++;
									}
									catch (UnsupportedCommOperationException e) {
										attemptCounter++;
									}
								}
	
							}
							commPortIndex++;
						}
					if (!moduleFound) {
						generalStatusLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));	
						mainTabbedPanel.setEnabled(false);
					}

				}
				catch (IOException e) {
					generalStatusLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
					mainTabbedPanel.setEnabled(false);
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
					mainTabbedPanel.setEnabled(false);
				}
			}
		};
		Thread findModuleThread = new Thread(findModuleOperation);
		findModuleThread.run();

	}

	/**
	 * This method handles which methods will be called when the user selects a port from the COMM port combobox. This entails looking up which port they selected and then opening that port
	 */
	private void portSelectedHandler() {
		try {
			//Executes if the user selected a valid COMM port
			if (commPortCombobox.getSelectedItem() != null) {

				//Get the string identifier (name) of the port the user selected
				String selectedCommID = commPortCombobox.getSelectedItem().toString();      

				//Open the serial port with the selected name, initialize input and output streams, set necessary flags so the whole program know that everything is initialized
				if(serialHandler.openSerialPort(selectedCommID)){
					enableTabChanges();
					
					//Notify the user that the port as opened successfully and is ready for a new command
					generalStatusLabel.setText("Serial Port Opened Successfully, Awaiting Commands");
					progressBar.setValue(0);
					progressBar.setForeground(new Color(51, 204, 51));

					//Enable the buttons that can now be used since the serial port opened
					disconnectButton.setEnabled(true);
					getModuleIDButton.setEnabled(true);
					readDataButton.setEnabled(true);
					writeConfigsButton.setEnabled(true);
					getCurrentConfigurationsButton.setEnabled(true);

					//Disable COMM port combobox so the user doesn't accidentally reopen a port
					commPortCombobox.setEnabled(false);
				}
			}
		}
		catch (IOException e) {
			generalStatusLabel.setText("Error Communicating With Serial Dongle");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255, 0, 0));
		}
		catch (PortInUseException e) {
			generalStatusLabel.setText("Serial Port Already In Use");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255, 0, 0));
		}

	}

	/**
	 * Executed when disconnect button is pressed. Since this is an action event, it must complete before GUI changes will be visible 
	 */
	public void disconnectButtonHandler() {
		serialHandler.closeSerialPort();

		//Notify the user that the port has been closed
		generalStatusLabel.setText("Port Closed");  

		//Disable buttons that only work when the port is opened
		disconnectButton.setEnabled(false);
		getModuleIDButton.setEnabled(false);
		readDataButton.setEnabled(false);
		writeConfigsButton.setEnabled(false);
		getCurrentConfigurationsButton.setEnabled(false);

		//Re-enable COMM port combobox so the user can select a new port to connect to
		commPortCombobox.setEnabled(true);
	}

	public void startTestBtnHandler() {
		Runnable startTestOperation = new Runnable() {
			public void run() {
				try{
					generalStatusLabel.setText("Taking a test...");
					boolean NullPtrExcept = false;
					try	{
						if(startTestBtn.getText().toString() == "Start Test") {
							startTestBtn.setEnabled(false);
							startTestBtn.setText("Stop Test");

							if(serialHandler.startTest()) {
								startTestBtn.setText("Start Test");
							}
							else {
								generalStatusLabel.setText("Error configuring for handshake or setting mode of module");
							}
						}
						else {
							if(serialHandler.stopTest()) {
								startTestBtn.setText("Start Test");
							}
							else {
								generalStatusLabel.setText("Error configuring for handshake or setting mode of module");
							}
						}
					}
					catch(NullPointerException e){
						startTestBtn.setText("Start Test");
						generalStatusLabel.setText("Not connected to serial port.");
						NullPtrExcept = true;
					}
					if(!NullPtrExcept) {
						generalStatusLabel.setText("Test Taken Successfully! Read the test back in \"Read Mode\" ");
						progressBar.setForeground(new Color(51, 204, 51));
						progressBar.setValue(100);
						NullPtrExcept = !NullPtrExcept;
					}
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};
		Runnable btnCntrl = new Runnable() {
			public void run() {		
				long startTime = System.currentTimeMillis();
				//While the loop has been executing for less than 500ms
				while (((System.currentTimeMillis() - startTime) < 5500)) {
					startTestBtn.setEnabled(false);
					getModuleIDButton.setEnabled(false);
				}

				startTestBtn.setEnabled(true);
				getModuleIDButton.setEnabled(true);

			}
		};


		Thread startTestOperationThread = new Thread(startTestOperation);
		startTestOperationThread.start();

		Thread btnCntrlThread = new Thread(btnCntrl);
		btnCntrlThread.start();  
	}

	/**
	 * Executed when pair new remote button is pressed. Since this is an action event, it must complete before GUI changes will be visible 
	 */
	public void pairNewRemoteHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable pairNewRemoteOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				pairNewRemoteButton.setEnabled(false);
				unpairAllRemotesButton.setEnabled(false);
				testRemotesButton.setEnabled(false);
				disconnectButton.setEnabled(false);
				getModuleIDButton.setEnabled(false);

				generalStatusLabel.setText("Module Listening for New Remote, Hold 'A' or 'B' Button to Pair");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					if(serialHandler.pairNewRemote()) {
						generalStatusLabel.setText("New Remote Successfully Paired");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}
					else {
						generalStatusLabel.setText("Pair Unsuccessful, Receiver Timed Out");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}


				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

				//Enable buttons that can now be used since the bulk erase completed
				pairNewRemoteButton.setEnabled(true);
				unpairAllRemotesButton.setEnabled(true);
				testRemotesButton.setEnabled(true);
				disconnectButton.setEnabled(true);
				getModuleIDButton.setEnabled(true);


			}
		};

		//Define a new thread to run the operation previously defined
		pairNewRemoteThread = new Thread(pairNewRemoteOperation);
		//Start the thread
		pairNewRemoteThread.start();
	}


	/**
	 * Executed when unpair all remotes button is pressed. Since this is an action event, it must complete before GUI changes will be visible 
	 */
	public void unpairAllRemotesHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable unpairAllRemotesOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				pairNewRemoteButton.setEnabled(false);
				unpairAllRemotesButton.setEnabled(false);
				testRemotesButton.setEnabled(false);
				disconnectButton.setEnabled(false);
				getModuleIDButton.setEnabled(false);
				disableTabChanges();

				generalStatusLabel.setText("Unpairing all Remotes...");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					serialHandler.unpairAllRemotes();
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}


				pairNewRemoteButton.setEnabled(true);
				unpairAllRemotesButton.setEnabled(true);
				testRemotesButton.setEnabled(true);
				disconnectButton.setEnabled(true);
				getModuleIDButton.setEnabled(true);
				enableTabChanges();

				generalStatusLabel.setText("All Remotes Unpaired, There are 0 Remotes Paired to this Module");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));
			}
		};

		//Define a new thread to run the operation previously defined
		unpairAllRemotesThread = new Thread(unpairAllRemotesOperation);
		//Start the thread
		unpairAllRemotesThread.start();
	}

	/**
	 * Runs a thread that will put the module in a test remote mode that will automatically update the GUI based on which remote button is pressed 
	 * Executed when test remote button is pressed. Since this is an action event, it must complete before GUI changes will be visible 
	 */
	public void testRemotesHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable testRemoteOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				pairNewRemoteButton.setEnabled(false);
				unpairAllRemotesButton.setEnabled(false);
				testRemotesButton.setEnabled(false);
				exitTestModeButton.setEnabled(true);
				disconnectButton.setEnabled(false);
				getModuleIDButton.setEnabled(false);
				disableTabChanges();

				//Notify the user that the bulk erase sequence has began
				generalStatusLabel.setText("Press a Button on a Remote to Test if it is Paired");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					if(!serialHandler.testRemotes(generalStatusLabel)) {
						generalStatusLabel.setText("Error Communicating with Module");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

				//Enable button
				pairNewRemoteButton.setEnabled(true);
				unpairAllRemotesButton.setEnabled(true);
				testRemotesButton.setEnabled(true);
				exitTestModeButton.setEnabled(false);
				disconnectButton.setEnabled(true);
				getModuleIDButton.setEnabled(true);
				enableTabChanges();

				//Notify the user that the sequence has completed
				generalStatusLabel.setText("Test Mode Successfully Exited");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(51, 204, 51));
			}
		};

		//Define a new thread to run the operation previously defined
		testRemoteThread = new Thread(testRemoteOperation);
		//Start the thread
		testRemoteThread.start();

	}

	/**
	 * Sets flag that will cause the testRemoteThread to exit the test remote mode
	 * Executed when exit remote test mode button is pressed. Since this is an action event, it must complete before GUI changes will be visible 
	 */
	public void exitTestModeHandler() {
		serialHandler.exitRemoteTest();
	}


	/**
	 * Handles the button press of the bulk erase button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume.
	 */
	public void bulkEraseHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable bulkEraseOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				bulkEraseButton.setEnabled(false);
				sectorEraseButton.setEnabled(false);
				testRemotesButton.setEnabled(false);
				getModuleIDButton.setEnabled(false);
				disableTabChanges();
				//Notify the user that the bulk erase sequence has began
				generalStatusLabel.setText("Bulk Erasing...");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {

					if(serialHandler.bulkEraseModule()) {
						//Notify the user that the sequence has completed
						generalStatusLabel.setText("Bulk Erase Complete");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}
					else {
						//Notify the user that the sequence has failed
						generalStatusLabel.setText("Bulk Erase Failed");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					//Enable buttons that can now be used since the sector erase completed
					bulkEraseButton.setEnabled(true);
					sectorEraseButton.setEnabled(true);
					getModuleIDButton.setEnabled(true);
					enableTabChanges();
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};

		//Define a new thread to run the operation previously defined
		bulkEraseThread = new Thread(bulkEraseOperation);
		//Start the thread
		bulkEraseThread.start();
	}



	/**
	 * Handles the button press of the sector erase button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume.
	 */
	public void sectorEraseHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable sectorEraseOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				bulkEraseButton.setEnabled(false);
				sectorEraseButton.setEnabled(false);
				getModuleIDButton.setEnabled(false);
				disableTabChanges();
				//Notify the user that the bulk erase sequence has began
				generalStatusLabel.setText("Sector Erasing...");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					if(serialHandler.sectorEraseModule()) {
						//Notify the user that the sequence has completed
						generalStatusLabel.setText("Sector Erase Complete");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}
					else {

						//Notify the user that the sequence has failed
						generalStatusLabel.setText("Sector Erase Failed");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					//Enable buttons that can now be used since the sector erase completed
					bulkEraseButton.setEnabled(true);
					sectorEraseButton.setEnabled(true);
					getModuleIDButton.setEnabled(true);
					enableTabChanges();
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};

		//Define a new thread to run the operation previously defined
		sectorEraseThread = new Thread(sectorEraseOperation);
		//Start the thread
		sectorEraseThread.start();
	}



	/**
	 * Handles the button press of the get module info button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume. See the method calls within the runnable for more info
	 * on what this handler actually does.
	 */
	public void getModuleInfoButtonHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable getIDInfoOperation = new Runnable() {
			public void run() {
				//Disable button until routine is complete
				getModuleIDButton.setEnabled(false);

				try {
					ArrayList<Integer> moduleIDInfo = serialHandler.getModuleInfo(NUM_ID_INFO_PARAMETERS);

					if (moduleIDInfo != null) {
						moduleSerialNumberLabel.setText("Module Serial Number: " + moduleIDInfo.get(0));
						hardwareIDLabel.setText("Module Hardware ID: " + moduleIDInfo.get(1) + "x");
						firmwareIDLabel.setText("Module Firmware ID: " + moduleIDInfo.get(2));
						if (moduleIDInfo.get(2) != CURRENT_FIRMWARE_ID) {
							generalStatusLabel.setText("Incompatable Firmware Version: " + moduleIDInfo.get(2) + ", Program Module with Version " + CURRENT_FIRMWARE_STRING);
							progressBar.setValue(100);
							progressBar.setForeground(new Color(255, 0, 0));	
						}
						else {
							generalStatusLabel.setText("Module Information Successfully Received");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(51, 204, 51));
						}
					}
					else {
						generalStatusLabel.setText("Module not Responding, Check Connections");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}


				//Re-enable button since previous method call was complete
				getModuleIDButton.setEnabled(true);
			}
		};

		//Assign new thread to run the previously defined operation
		infoThread = new Thread(getIDInfoOperation);
		//Start separate thread
		infoThread.start();
	}


	public void configForCalHandler() {
		Runnable calforConfigOperation = new Runnable() {
			public void run() {
				configForCalButton.setEnabled(false);
				importCalDataButton.setEnabled(true);
				applyOffsetButton.setEnabled(false);
				getModuleIDButton.setEnabled(false);
				disableTabChanges();

				try {
					if(!serialHandler.configForCalibration()) {
						generalStatusLabel.setText("Error Communicating With Module");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					else {
						generalStatusLabel.setText("Module Configured for Calibration, Use Configuration Tab to Exit");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}

					configForCalButton.setEnabled(true);
					importCalDataButton.setEnabled(true);
					applyOffsetButton.setEnabled(true);
					getModuleIDButton.setEnabled(true);
					enableTabChanges();
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};
		Thread calConfigThread = new Thread(calforConfigOperation);
		calConfigThread.start();
	}

	public void importCalDataHandler() {
		Runnable getConfigsOperation = new Runnable() {
			public void run() {	
				configForCalButton.setEnabled(false);
				importCalDataButton.setEnabled(false);
				applyOffsetButton.setEnabled(false);
				getModuleIDButton.setEnabled(false);
				disableTabChanges();
				try {

					BlackFrameAnalysis bfo = new BlackFrameAnalysis(videoFilePathTextField.getText());

					delayAfterTextField.setText(Integer.toString(bfo.getDelayAfterStart()));
					tmr0OffsetTextField.setText(Integer.toString(bfo.getTMR0Offset()));

					configForCalButton.setEnabled(true);
					importCalDataButton.setEnabled(true);
					applyOffsetButton.setEnabled(true);
					getModuleIDButton.setEnabled(true);
					enableTabChanges();

				} catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};

		Thread getConfigsOperationThread = new Thread(getConfigsOperation);
		getConfigsOperationThread.start();

	}



	public void applyOffsetsHandler() {
		Runnable getConfigsOperation = new Runnable() {
			public void run() {
				configForCalButton.setEnabled(false);
				importCalDataButton.setEnabled(false);
				applyOffsetButton.setEnabled(false);
				getModuleIDButton.setEnabled(false);
				disableTabChanges();

				try {
					if(!serialHandler.applyCalibrationOffsets(Integer.parseInt(tmr0OffsetTextField.getText()), Integer.parseInt(delayAfterTextField.getText()))) { //Constant 0 because we dont do Timer0 Calibration... yet
						generalStatusLabel.setText("Error Communicating With Module");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					else {
						generalStatusLabel.setText("Offset Successfully Applied, Camera and Module are now Synced");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}

					configForCalButton.setEnabled(true);
					importCalDataButton.setEnabled(true);
					applyOffsetButton.setEnabled(true);
					getModuleIDButton.setEnabled(true);
					enableTabChanges();

				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};
		Thread applyOffsetsHandlerThread = new Thread(getConfigsOperation);
		applyOffsetsHandlerThread.start();

	}

	/**
	 * Obtains test parameters from module and updates each data field on the configuration tab with those newly red values
	 * Executed when get current configs button is pressed. Since this is an action event, it must complete before GUI changes will be visible 
	 */
	public void getConfigsHandler() {
		Runnable getConfigsOperation = new Runnable() {
			public void run() {
				//Disable get configs button while read is in progress
				getCurrentConfigurationsButton.setEnabled(false);
				writeConfigsButton.setEnabled(false);
				getModuleIDButton.setEnabled(false);
				disableTabChanges();

				corruptConfigFlag = false;

				try {

					generalStatusLabel.setText("Reading Current Module Configurations...");
					progressBar.setValue(0);
					progressBar.setForeground(new Color(51, 204, 51));



					testParameters = serialHandler.readTestParams(NUM_TEST_PARAMETERS);

					if(testParameters != null) {
						//Assign local variables to their newly received values from the module
						timer0TickThreshold = testParameters.get(1);
						delayAfterStart = testParameters.get(2);
						battTimeoutLength = testParameters.get(3);
						timedTestFlag = testParameters.get(4);
						triggerOnReleaseFlag = testParameters.get(5);
						testLength = testParameters.get(6);
						accelGyroSampleRate = testParameters.get(7);
						magSampleRate = testParameters.get(8);
						accelSensitivity = testParameters.get(9);
						gyroSensitivity = testParameters.get(10);
						accelFilter = testParameters.get(11);
						gyroFilter = testParameters.get(12);					

						System.out.println(delayAfterStart);
						if(delayAfterStart > 2000) {
							delayAfterStart = ~delayAfterStart & 65535;
							delayAfterStart *= -1;
							testParameters.set(2, delayAfterStart);
						}

						if(timedTestFlag > 0) {
							timedTestCheckbox.setSelected(true);
							testLengthTextField.setEnabled(true);
						}
						else {
							timedTestCheckbox.setSelected(false);
							testLengthTextField.setEnabled(false);
						}
						if(triggerOnReleaseFlag > 0) {
							triggerOnReleaseCheckbox.setSelected(true);
						}
						else {
							triggerOnReleaseCheckbox.setSelected(false);
						}

						//Assign/lookup values on gui based on read configs. If any of the lookup methods detect corrupt data they set corruptConfigFlag to true
						testLengthTextField.setText(Integer.toString(testLength));
						accelGyroSampleRateCombobox.setSelectedIndex(lookupAccelGyroSampleRateIndex(accelGyroSampleRate));
						//Executes if the magnetometer value is corrupted
						if(!updateMagSampleRate()) {
							//Set flag that will be used to determine the status label text
							corruptConfigFlag = true;
						}
						accelSensitivityCombobox.setSelectedIndex(lookupAccelSensitivityIndex(accelSensitivity));
						gyroSensitivityCombobox.setSelectedIndex(lookupGyroSensitivityIndex(gyroSensitivity));
						accelFilterCombobox.setSelectedIndex(lookupAccelFilterIndex(accelFilter));
						gyroFilterCombobox.setSelectedIndex(lookupGyroFilterIndex(gyroFilter));
						delayAfterStartTextField.setText(Integer.toString(delayAfterStart));
						timer0TickThreshTextField.setText(Integer.toString(timer0TickThreshold));
						magSampleRateTextField.setText(Integer.toString(magSampleRate));
						batteryTimeoutTextField.setText(Integer.toString(battTimeoutLength));


						if(corruptConfigFlag) {
							initDataFields();
							generalStatusLabel.setText("Module Configurations Corrupted, Default Values Displayed");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(255, 0, 0));
						}
						else {
							generalStatusLabel.setText("Current Module Configurations Received and Displayed");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(51, 204, 51));
						}
					}
					else {
						generalStatusLabel.setText("Error Communicating With Module, Try Again");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}

				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

				//Re-enable the write config button when the routine has completed
				getCurrentConfigurationsButton.setEnabled(true);
				writeConfigsButton.setEnabled(true);
				getModuleIDButton.setEnabled(true);
				enableTabChanges();
			}
		};

		//Assign new operation to a thread so that it can be run in the background
		getParamThread = new Thread(getConfigsOperation);
		//Start the new thread
		getParamThread.start();

	}
	/**
	 * Handles the button press of the write configuration button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume. See the method calls within the runnable for more info
	 * on what this handler actually does.
	 */
	private void writeButtonHandler() {
		updateTickThresh();
		if (updateMagSampleRate()) {
			//Define no operation that can be run in a thread
			Runnable sendParamOperation = new Runnable() {
				public void run() {
					//Disable write config button while the sendParameters() method is running
					writeConfigsButton.setEnabled(false);
					getCurrentConfigurationsButton.setEnabled(false);
					getModuleIDButton.setEnabled(false);
					disableTabChanges();
					try {
						ArrayList<Integer> testParams = new ArrayList<Integer>();

						//Set local flags for checkbox states
						if (timedTestCheckbox.isSelected()) {
							timedTestFlag = 1;
						}
						else {
							timedTestFlag = 0;
						}

						if (triggerOnReleaseCheckbox.isSelected()) {
							triggerOnReleaseFlag = 1;
						}
						else {
							triggerOnReleaseFlag = 0;
						}


						//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
						testParams.add(0);
						//1 Timer0 Tick Threshold
						testParams.add(getTickThreshold(Integer.parseInt(accelGyroSampleRateCombobox.getSelectedItem().toString())));
						//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
						testParams.add(0);
						//3 Battery timeout flag
						testParams.add(Integer.parseInt(batteryTimeoutTextField.getText()));
						//4 Timed test flag
						testParams.add(timedTestFlag);
						//5 Trigger on release flag
						testParams.add(triggerOnReleaseFlag);
						//6 Test Length
						if(timedTestFlag == 1) {
							testParams.add(Integer.parseInt(testLengthTextField.getText()));
						}
						else {
							testParams.add(0);
						}
						//7 Accel Gyro Sample Rate
						testParams.add(Integer.parseInt(accelGyroSampleRateCombobox.getSelectedItem().toString()));
						//8 Mag Sample Rate
						testParams.add(Integer.parseInt(magSampleRateTextField.getText()));
						//9 Accel Sensitivity
						testParams.add(Integer.parseInt(accelSensitivityCombobox.getSelectedItem().toString()));
						//10 Gyro Sensitivity
						testParams.add(Integer.parseInt(gyroSensitivityCombobox.getSelectedItem().toString()));
						//11 Accel Filter
						testParams.add(Integer.parseInt(accelFilterCombobox.getSelectedItem().toString()));
						//12 Gyro Filter
						testParams.add(Integer.parseInt(gyroFilterCombobox.getSelectedItem().toString()));

						if(!serialHandler.sendTestParams(testParams)) {
							generalStatusLabel.setText("Module Not Responding");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(255, 0, 0));
						}
						else {
							generalStatusLabel.setText("Module Configuration Successful, Parameters Have Been Updated");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(51, 204, 51));
						}
					}
					catch (NumberFormatException e) {
						generalStatusLabel.setText("Please Fill out Every Field");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					catch (IOException e) {
						generalStatusLabel.setText("Error Communicating With Serial Dongle");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					catch (PortInUseException e) {
						generalStatusLabel.setText("Serial Port Already In Use");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					catch (UnsupportedCommOperationException e) {
						generalStatusLabel.setText("Check Dongle Compatability");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}

					//Re-enable the write config button when the routine has completed
					writeConfigsButton.setEnabled(true);
					getCurrentConfigurationsButton.setEnabled(true);
					getModuleIDButton.setEnabled(true);
					enableTabChanges();
				}
			};

			//Assign new operation to a thread so that it can be run in the background
			paramThread = new Thread(sendParamOperation);
			//Start the new thread
			paramThread.start();
		}

	}


	/**
	 * Handles the button press of read data from module button. This includes reading the test parameters, updating the read gui, and reading/converting the data to .csv. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume. See the method calls within the runnable for more info
	 * on what this handler actually does.
	 */
	public void readButtonHandler() {
		//Define operation that can be run in separate thread
		Runnable readOperation = new Runnable() {
			public void run() {
				//Disable read button while read is in progress
				readDataButton.setEnabled(false);
				getModuleIDButton.setEnabled(false);
				disableTabChanges();

				try {



					generalStatusLabel.setText("Reading Data from Module...");
					progressBar.setValue(0);
					progressBar.setForeground(new Color(51, 204, 51));

					//Read test parameters from module and store it in testParameters
					testParameters = serialHandler.readTestParams(NUM_TEST_PARAMETERS);

					//Executes if the reading of the test parameters was successful
					if (testParameters != null) {

						expectedTestNum = testParameters.get(0);
						delayAfterStart = testParameters.get(2);

						//Assign local variables to their newly received values from the module
						timedTestFlag = testParameters.get(4);
						//Trigger on release is 8
						testLength = testParameters.get(6);
						accelGyroSampleRate = testParameters.get(7);
						magSampleRate = testParameters.get(8);
						accelSensitivity = testParameters.get(9);
						gyroSensitivity = testParameters.get(10);
						accelFilter = testParameters.get(11);
						gyroFilter = testParameters.get(12);				

						//System.out.println(delayAfterStart);
						if(delayAfterStart > 2000) {
							delayAfterStart = ~delayAfterStart & 65535;
							delayAfterStart *= -1;
							testParameters.set(2, delayAfterStart);
						}


						boolean timedTest = true;
						if (timedTestFlag == 0) {
							timedTest = false;
						}
						double bytesPerSample = 18;
						if (accelGyroSampleRate / magSampleRate == 10) {
							bytesPerSample = 12.6;
						}



						//Populate dashboard with the parameters sent by the module
						testLengthTextFieldRead.setText(Integer.toString(testLength));            		//Test Length
						numTestsTextFieldRead.setText(Integer.toString(expectedTestNum));				//Number of tests
						accelGyroSampleRateTextFieldRead.setText(Integer.toString(accelGyroSampleRate));//Accel Gyro Sample Rate
						magSampleRateTextFieldRead.setText(Integer.toString(magSampleRate));           	//Mag Sample Rate
						accelSensitivityTextFieldRead.setText(Integer.toString(accelSensitivity));      //Accel Sensitivity
						gyroSensitivityTextFieldRead.setText(Integer.toString(gyroSensitivity));        //Gyro Sensitivity
						accelFilterTextFieldRead.setText(Integer.toString(accelFilter));           		//Accel Filter
						gyroFilterTextFieldRead.setText(Integer.toString(gyroFilter));             		//Gyro Filter 
						nameOfFile = fileNameTextField.getText();

						//Executes if there are tests on the module
						if(expectedTestNum > 0) {

							//Get date for file name
							Date date = new Date();

							//Assign file name
							nameOfFile = "";
							nameOfFile += prefixTextField.getText();
							nameOfFile += (" " + accelGyroSampleRate + "-" + magSampleRate + " " + accelSensitivity + "G-" + accelFilter + " " + gyroSensitivity + "dps-" + gyroFilter + " MAG-N " + date.getDate() + getMonth(date.getMonth()) + (date.getYear() - 100) + ".csv");
							fileNameTextField.setText(nameOfFile);

							HashMap<Integer, ArrayList<Integer>> testData;

							//Store the test data from the dashboard passing in enough info that the progress bar will be accurately updated
							testData = serialHandler.readTestData(expectedTestNum, progressBar, generalStatusLabel);

							generalStatusLabel.setText("All Data Received from Module");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(51, 204, 51));

							//Executes if the data was received properly (null = fail)
							if(testData != null) {
								List<DataOrganizer> dataOrgoList = new ArrayList<DataOrganizer>(testData.size()-1);
								for (int testIndex = 0; testIndex < testData.size(); testIndex++) {

									int [] finalData = new int[testData.get(testIndex).size()];

									for(int byteIndex = 0; byteIndex < testData.get(testIndex).size(); byteIndex++) {
										if (testData.get(testIndex).get(byteIndex) != -1){
											finalData[byteIndex] = testData.get(testIndex).get(byteIndex);
										}
										else {
											finalData[byteIndex] = -1;
											break;
										}
									}
									String tempNameOfFile = "(#" + (testIndex+1) + ")" + nameOfFile; 

									DataOrganizer dataOrgo = new DataOrganizer(testParameters, tempNameOfFile);
									dataOrgoList.add(dataOrgo);
									Runnable organizerOperation = new Runnable() {
										public void run() {
											dataOrgo.createDataSmpsRawData(finalData);
											dataOrgo.getSignedData();
										}
									};

									organizerThread = new Thread(organizerOperation);
									//Start thread
									organizerThread.start();	
								}
								addTestsToRecordationPane(dataOrgoList);
							}
							else {
								generalStatusLabel.setText("Error Reading From Module, Try Again");
								progressBar.setValue(100);
								progressBar.setForeground(new Color(255, 0, 0));
							}
						}
						else {
							fileNameTextField.setText("");
							generalStatusLabel.setText("No Tests Found on Module");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(255, 0, 0));
						}
					}
					else {
						generalStatusLabel.setText("Error Reading From Module, Try Again");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}

				}

				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

				//Re-enable read button upon read completion
				readDataButton.setEnabled(true);
				getModuleIDButton.setEnabled(true);
				enableTabChanges();
			}
		};

		//Set thread to execute previously defined operation
		readThread = new Thread(readOperation);
		//Start thread
		readThread.start();

	}

	/**
	 * Initializes the data fields of the Configurations tab of the dashboard. These are hardcoded for now so they will reset every time you leave this tab and come back
	 */
	public void initDataFields() {
		//Checkboxes
		timedTestCheckbox.setSelected(true);

		//Comboboxes
		accelGyroSampleRateCombobox.setModel(new DefaultComboBoxModel(new String [] {"60", "120", "240", "480", "500", "960"}));
		accelSensitivityCombobox.setModel(new DefaultComboBoxModel(new String [] {"2", "4", "8", "16"}));
		gyroSensitivityCombobox.setModel(new DefaultComboBoxModel(new String[] {"250", "500", "1000", "2000"}));
		accelFilterCombobox.setModel(new DefaultComboBoxModel(new String [] {"5", "10", "20", "41", "92", "184", "460", "1130 (OFF)"}));
		gyroFilterCombobox.setModel(new DefaultComboBoxModel(new String[] {"10", "20", "41", "92", "184", "250", "3600", "8800 (OFF)"}));

		//Set Default Selection for Comboboxes
		accelGyroSampleRateCombobox.setSelectedIndex(5);//960-96	
		accelSensitivityCombobox.setSelectedIndex(2);	//8g
		gyroSensitivityCombobox.setSelectedIndex(2);	//1000dps
		accelFilterCombobox.setSelectedIndex(4);		//92Hz
		gyroFilterCombobox.setSelectedIndex(3);			//92Hz

		//Text Fields
		updateMagSampleRate();
		testLengthTextField.setText("25");
		batteryTimeoutTextField.setText("300");
		timer0TickThreshTextField.setText("");
		delayAfterStartTextField.setText("");

	}

	/**
	 * Updates the data fields on whichever tab is currently selected if applicable
	 */
	public void updateDataFields() {
		if (frameInitialized && mainTabbedPanel.getSelectedIndex() == 1) {
			//Timed Test Checkbox (Allows user to swap between timed and untimed test)
			if (timedTestCheckbox.isSelected()) {
				testLengthTextField.setEditable(true);
				testLengthTextField.setEnabled(true);
			}
			else {
				testLengthTextField.setEditable(false);
				testLengthTextField.setEnabled(false);
			}

			/*
			//Delay After Start Checkbox (Allows Editing of Timer0 Tick Threshold)
			if (delayAfterStartCheckbox.isSelected()) {
				delayAfterStartTextField.setEditable(true);
				delayAfterStartTextField.setEnabled(true);
			}
			else {
				delayAfterStartTextField.setEditable(false);
				delayAfterStartTextField.setEnabled(false);
			}

			//Manual Calibration Checkbox (Allows Editing of Timer0 Tick Threshold)
			if (manualCalibrationCheckbox.isSelected()) {
				timer0TickThreshTextField.setEditable(true);
				timer0TickThreshTextField.setEnabled(true);
			}
			else {
				timer0TickThreshTextField.setEditable(false);
				timer0TickThreshTextField.setEnabled(false);
			}
			 */
		}
	}

	public void disableTabChanges() {
		int currentTab = mainTabbedPanel.getSelectedIndex();
		for (int i = 0; i < mainTabbedPanel.getTabCount(); i++) {
			if (i != currentTab) {
				mainTabbedPanel.setEnabledAt(i, false);
			}
		}
	}

	public void enableTabChanges() {
		for (int i = 0; i < mainTabbedPanel.getTabCount(); i++) {
			mainTabbedPanel.setEnabledAt(i, true);
		}
	}


	/**
	 * Updates the magnetometer text field based on the accel gyro sample rate text field 
	 * @return
	 */
	public boolean updateMagSampleRate() {
		if (!accelGyroSampleRateCombobox.getSelectedItem().toString().isEmpty()) {
			switch (Integer.parseInt(accelGyroSampleRateCombobox.getSelectedItem().toString())) {
			case(60):			
				magSampleRateTextField.setText("60");
			break;
			case(120):
				magSampleRateTextField.setText("120");
			break;
			case (240):
				magSampleRateTextField.setText("24");
			break;
			case (480):
				magSampleRateTextField.setText("48");
			break;
			case (500):
				magSampleRateTextField.setText("50");
			break;
			case (960):
				magSampleRateTextField.setText("96");
			break;
			default:	
				corruptConfigFlag = true;
				return false;
			}
		}
		else {
			generalStatusLabel.setText("Please Enter a Valid Accel/Gyro Sample Rate");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255, 0, 0));
			return false;
		}
		progressBar.setValue(0);
		progressBar.setForeground(new Color(51, 204, 51));
		return true;
	}

	/**
	 * Update the text field for the tick threshold based on the accel/gyro sample rate
	 * @return
	 */
	public boolean updateTickThresh() {
		if (!accelGyroSampleRateCombobox.getSelectedItem().toString().isEmpty()) {
			int tickThresh = getTickThreshold(Integer.parseInt(accelGyroSampleRateCombobox.getSelectedItem().toString()));
			timer0TickThreshTextField.setText(Integer.toString(tickThresh));
		}
		return true;
	}


	/**
	 * Handles the button press of browse button. This is an action event which must handled before the rest of the program resumes. This method allows the user to navigate
	 * the file explorer and select a save location for the incoming data.
	 */
	public void browseButtonHandler() {
		JFileChooser chooser;
		chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			fileOutputDirectoryStr = chooser.getSelectedFile().toString();
		}
		else {
			fileOutputDirectoryStr = null;
		}
	}

	/**
	 * Handles the button press of browse button. This is an action event which must handled before the rest of the program resumes. This method allows the user to navigate
	 * the file explorer and select a save location for the incoming data.
	 */
	public void videoBrowseButtonHandler() {
		JFileChooser chooser;
		chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			videoFilePathTextField.setText(chooser.getSelectedFile().toString());
		}
		else {
			videoFilePathTextField.setText(null);
		}
	}
	
	/**
	 * Handles the button press of browse button. This is an action event which must handled before the rest of the program resumes. This method allows the user to navigate
	 * the file explorer and select a csv file to load
	 */
	public void csvBrowseButtonHandler() {
		JFileChooser chooser;
		chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			ArrayList<DataOrganizer> dataOrgoList = new ArrayList<DataOrganizer>();
			DataOrganizer dataOrgo = new DataOrganizer();
			dataOrgoList.add(dataOrgo);
			dataOrgo.createDataSamplesFromCSV(chooser.getSelectedFile().toString());
			dataOrgo.getSignedData();
			addTestsToRecordationPane(dataOrgoList);
			repaint();
		}
		else {
			generalStatusLabel.setText("Not a valid CSV file.");
		}
	}

	/**
	 * Setter that allows external classes to set the progress bar's value
	 * @param progress integer value between 0-100 that corresponds to the desired percentage to be displayed
	 */
	public void updateProgress(int progress) {  
		progressBar.setValue(progress);
	}

	/**
	 * Setter that allows external classes to set the writeStatusLabel's text
	 * @param label the text that the writeStatusLabel will display
	 */
	public void setWriteStatusLabel(String label) {
		generalStatusLabel.setText(label);        //Tell the user a new .CSV has been created.
	}

	public int lookupAccelGyroSampleRateIndex(int aGSampleRate) {
		switch (aGSampleRate) {
		case(60):		
			return 0;
		case(120):
			return 1;
		case(240):
			return 2;
		case(480):
			return 3;
		case(500):
			return 4;
		case(960):
			return 5;
		default:
			corruptConfigFlag = true;
			return accelGyroSampleRateCombobox.getSelectedIndex();
		}
	}

	/**
	 * Looks up the selection index for the accel sensitivity combobox
	 * @param accelSensitivity
	 * @return
	 */
	public int lookupAccelSensitivityIndex(int accelSensitivity){
		switch (accelSensitivity) {
		case(2):		
			return 0;
		case(4):
			return 1;
		case(8):
			return 2;
		case(16):
			return 3;
		default:
			corruptConfigFlag = true;
			return accelSensitivityCombobox.getSelectedIndex();
		}
	}

	/**
	 * Looks up the selection index for the gyro sensitivity combobox
	 * @param accelSensitivity
	 * @return
	 */
	public int lookupGyroSensitivityIndex(int gyroSensitivity){
		switch (gyroSensitivity) {
		case(250):		
			return 0;
		case(500):
			return 1;
		case(1000):
			return 2;
		case(2000):
			return 3;
		default:
			corruptConfigFlag = true;
			return gyroSensitivityCombobox.getSelectedIndex();
		}
	}

	/**
	 * Looks up the selection index for the accel filter combobox
	 * @param accelSensitivity
	 * @return
	 */
	public int lookupAccelFilterIndex(int accelFilter){
		switch (accelFilter) {
		case(5):		
			return 0;
		case(10):
			return 1;
		case(20):
			return 2;
		case(41):
			return 3;
		case(92):		
			return 4;
		case(184):
			return 5;
		case(460):
			return 6;
		case(1130):
			return 7;
		default:
			corruptConfigFlag = true;
			return accelFilterCombobox.getSelectedIndex();
		}
	}

	/**
	 * Looks up the selection index for the gyro sensitivity combobox
	 * @param accelSensitivity
	 * @return
	 */
	public int lookupGyroFilterIndex(int gyroFilter){
		switch (gyroFilter) {
		case(10):
			return 0;
		case(20):
			return 1;
		case(41):
			return 2;
		case(92):		
			return 3;
		case(184):
			return 4;
		case(250):
			return 5;
		case(3600):
			return 6;
		case(8600):
			return 7;
		default:
			corruptConfigFlag = true;
			return gyroFilterCombobox.getSelectedIndex();
		}
	}


	/**
	 * Get the desired tick threshold for the desired sample rate. This effectively sets the sample rate of the module
	 * @param accelGyroSampleRate
	 * @return
	 */
	public int getTickThreshold(int accelGyroSampleRate) {
		switch (accelGyroSampleRate) {
		case(60):		
			return 33173;
		case(120):
			return 33021;
		case (240):
			return 16343;
		case (480):
			return 8021;
		case (500):
			return 7679;
		case (960):
			return 3813;
		default:	//960-96
			return 3813;
		}
	}

	/**
	 * Gets a 3 letter abbreviation for the passed in month for the automatic test title generation
	 * @param month an integer 0-11 that corresponds to the month with 0 = January and 11 = December
	 * @return The 3 letter abbreviation for the month
	 */
	public String getMonth(int month) { 
		switch (month) {
		case (0):
			return "JAN";
		case (1):
			return "FEB";
		case (2):
			return "MAR";
		case (3):
			return "APR";
		case (4):
			return "MAY";
		case (5):
			return "JUN";
		case (6):
			return "JUL";
		case (7):
			return "AUG";
		case (8):
			return "SEP";
		case (9):
			return "OCT";
		case (10):
			return "NOV";
		case (11):
			return "DEC";
		}
		return "NOP";
	}

	public void initFX(List<DataOrganizer> dataOrgo, ActionEvent e) {
		final int viewableTests = dataOrgo.size();
		Platform.setImplicitExit(false);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for(int i = 0; i < viewableTests; i++) {
					if(graphTestBtn.get(i) == e.getSource()) {
						GraphController lineGraph = startGraphing();
						MediaPlayerController mediaController = startMediaPlayer();
						shareFrameGraphAndMedia(lineGraph, mediaController);
					}
				}
			}
		});
	}
	
	public MediaPlayerController startMediaPlayer() {
		Stage primaryStage = new Stage();
		Parent root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MediaPlayerStructure.fxml"));
		try {
			root = loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    primaryStage.setTitle("Video Player");
	    if(root!=null) primaryStage.setScene(new Scene(root, 1280, 720));
	    primaryStage.show();
	    primaryStage.setResizable(false);
	    return loader.getController();
	}
	
	
	
	public GraphController startGraphing() {
		Stage primaryStage = new Stage();
		Parent root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("GraphStructure.fxml"));
		try {
			root = loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(root!=null) primaryStage.setScene(new Scene(root, 800, 500));
		
	    primaryStage.setTitle("Graph");
		primaryStage.show();
		primaryStage.setResizable(true);
		
		return loader.getController();
	}
	
	public void addTestsToRecordationPane(List<DataOrganizer> dataOrgo) {
		if(dataOrgo != null) {
			final int viewableTests = dataOrgo.size();
			testNumPaneArray = new ArrayList<JPanel>(viewableTests);
			saveTestBtn = new ArrayList<JButton>(viewableTests);
			graphTestBtn = new ArrayList<JButton>(viewableTests);
			testNameTextField = new ArrayList<JTextField>(viewableTests);

			for(int i = 0; i < viewableTests; i++) {
				testNumPaneArray.add(new JPanel());
				testNumPaneArray.get(i).setBounds(0, i*48, 625, 47);
				testNumPaneArray.get(i).setLayout(null);
				testNumPaneArray.get(i).setBorder(new LineBorder(new Color(0, 0, 0)));

				testNameTextField.add(new JTextField());
				testNameTextField.get(i).setFont(new Font("Tahoma", Font.PLAIN, 12));
				testNameTextField.get(i).setBounds(10, 11, 335, 29);
				testNameTextField.get(i).setColumns(10);
				testNameTextField.get(i).setText(dataOrgo.get(i).getName());


				testNumPaneArray.get(i).add(testNameTextField.get(i));

				saveTestBtn.add(new JButton("Save"));
				saveTestBtn.get(i).setBounds(355, 11, 70, 23);
				saveTestBtn.get(i).addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for(int i = 0; i < viewableTests; i++) {
							if(saveTestBtn.get(i) == e.getSource()) {
								dataOrgo.get(i).createCSV(checkBoxLabelCSV.isSelected(), checkBoxSignedData.isSelected());
							}
						}
					}
				});


				testNumPaneArray.get(i).add(saveTestBtn.get(i));

				graphTestBtn.add(new JButton("Graph"));
				graphTestBtn.get(i).setBounds(435, 11, 69, 23);
				graphTestBtn.get(i).addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						initFX(dataOrgo, e);
					}
				});
				testNumPaneArray.get(i).add(graphTestBtn.get(i));
			}

			for(int i = 0; i < testNumPaneArray.size();i++) {
				testRecordationPanel.add(testNumPaneArray.get(i));
			}
		}
	}

	public void shareFrameGraphAndMedia(GraphController graph, MediaPlayerController MPC) {
		Runnable updatePosInGraph = new Runnable() {
			public void run() {
				try {
					int currentFrame = -1;
					while(true) {
						if(MPC.hasVideoSelected()) {
							while(currentFrame != MPC.getCurrentFrame()) {
								Thread.sleep(10);
								graph.updateCirclePos(MPC.getCurrentFrame(), MPC.getFPS());
								currentFrame = MPC.getCurrentFrame();
							}
						}
						Thread.sleep(100);
					} 
				}catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		Thread updatePosInGraphThread = new Thread(updatePosInGraph);
		updatePosInGraphThread.start();
	}
	

	/**
	 * Creates and initializes the properties of all components on the main dashboard window. ex) panels, buttons, text fields, etc.
	 */
	public void createComponents() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 638, 659);
		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
		setContentPane(contentPanel);
		contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));


		serialPortPanel = new JPanel();
		serialPortPanel.setPreferredSize(new Dimension(630, 150));
		contentPanel.add(serialPortPanel);
		serialPortPanel.setLayout(new GridLayout(0,1, 0, 0));

		JPanel commPortPanel = new JPanel();
		serialPortPanel.add(commPortPanel);

		refreshPortButton = new JButton("Refresh Port List");
		refreshPortButton.setBorder(null);
		refreshPortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateCommPortComboBox();
			}
		});

		commPortCombobox = new JComboBox();
		commPortCombobox.setEnabled(false);
		commPortPanel.setLayout(new GridLayout(1, 3, 0, 0));
		commPortPanel.add(refreshPortButton);
		commPortPanel.add(commPortCombobox);

		commPortCombobox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				portSelectedHandler();
				enableTabChanges();
			}
		});

		disconnectButton = new JButton("Disconnect");
		disconnectButton.setBorder(null);
		disconnectButton.setEnabled(false);
		disconnectButton.setForeground(Color.BLACK);
		commPortPanel.add(disconnectButton);
		disconnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				disconnectButtonHandler();
			}
		});

		getModuleIDPanel = new JPanel();
		serialPortPanel.add(getModuleIDPanel);
		getModuleIDPanel.setLayout(new GridLayout(0, 1, 0, 0));

		getModuleIDButton = new JButton("Get Module Information");
		getModuleIDButton.setEnabled(false);
		getModuleIDPanel.add(getModuleIDButton);

		getModuleIDButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getModuleInfoButtonHandler();
				enableTabChanges();
			}
		});

		JPanel serialNumberPanel = new JPanel();
		serialNumberPanel.setBorder(null);
		serialPortPanel.add(serialNumberPanel);

		moduleSerialNumberLabel = new JLabel("Module Serial Number:");
		moduleSerialNumberLabel.setBorder(null);
		serialNumberPanel.add(moduleSerialNumberLabel);

		JPanel moduleInfoPanel = new JPanel();
		serialPortPanel.add(moduleInfoPanel);
		moduleInfoPanel.setLayout(new GridLayout(0, 2, 0, 0));

		hardwareIDLabel = new JLabel("Module Hardware ID:");
		hardwareIDLabel.setBorder(null);
		moduleInfoPanel.add(hardwareIDLabel);

		firmwareIDLabel = new JLabel("Module Firmware ID:");
		firmwareIDLabel.setBorder(null);
		moduleInfoPanel.add(firmwareIDLabel);

		generalStatusLabel = new JLabel("Please Select Port to Begin");
		generalStatusLabel.setForeground(Color.BLUE);
		generalStatusLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		generalStatusLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
		generalStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		serialPortPanel.add(generalStatusLabel);

		mainPanelContainer = new JPanel();
		contentPanel.add(mainPanelContainer);
		mainPanelContainer.setLayout(new GridLayout(0, 1, 0, 0));

		mainTabbedPanel = new JTabbedPane(JTabbedPane.TOP);
		mainTabbedPanel.setPreferredSize(new Dimension(630, 400));
		mainPanelContainer.add(mainTabbedPanel);

		JPanel readPanel = new JPanel();
		readPanel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		mainTabbedPanel.addTab("Read Mode", null, readPanel, null);
		readPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		fileNamePanel = new JPanel();
		fileNamePanel.setPreferredSize(new Dimension(630, 150));
		readPanel.add(fileNamePanel);
		fileNamePanel.setLayout(new GridLayout(0, 1, 0, 0));

		startReadButtonPanel = new JPanel();
		fileNamePanel.add(startReadButtonPanel);
		startReadButtonPanel.setLayout(new GridLayout(0, 2, 0, 0));

		readDataButton = new JButton("Read Data from Module");
		readDataButton.setEnabled(false);
		startReadButtonPanel.add(readDataButton);

		panel = new JPanel();
		startReadButtonPanel.add(panel);
		panel.setLayout(new GridLayout(2, 2, 0, 0));

		checkBoxLabelCSV = new JCheckBox("Label Data in .CSV");

		checkBoxSignedData = new JCheckBox("Signed Data");
		checkBoxSignedData.setSelected(true);

		panel.add(checkBoxSignedData);
		panel.add(checkBoxLabelCSV);
		readDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				readButtonHandler();
			}
		});

		fileNameModifierPanel = new JPanel();
		fileNamePanel.add(fileNameModifierPanel);
		fileNameModifierPanel.setLayout(new GridLayout(1, 0, 0, 0));

		prefixTextField = new JTextField();
		prefixTextField.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "File Name Prefix", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		fileNameModifierPanel.add(prefixTextField);
		prefixTextField.setColumns(10);

		suffixTextField = new JTextField();
		suffixTextField.setBorder(new TitledBorder(null, "File Name Suffix", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		fileNameModifierPanel.add(suffixTextField);
		suffixTextField.setColumns(10);

		fileLocationPanel = new JPanel();
		fileNamePanel.add(fileLocationPanel);
		fileLocationPanel.setLayout(new BoxLayout(fileLocationPanel, BoxLayout.X_AXIS));

		fileNameTextField = new JTextField();
		fileNameTextField.setMinimumSize(new Dimension(600, 50));
		fileNameTextField.setMaximumSize(new Dimension(500, 50));
		fileNameTextField.setBorder(new TitledBorder(null, "File Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		fileLocationPanel.add(fileNameTextField);
		fileNameTextField.setColumns(10);

		browseButton = new JButton("Browse");
		browseButton.setMaximumSize(new Dimension(160, 50));
		browseButton.setPreferredSize(new Dimension(81, 35));
		fileLocationPanel.add(browseButton);

		browseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				browseButtonHandler();
			}
		});

		paramPanel = new JPanel();
		paramPanel.setPreferredSize(new Dimension(630, 200));
		readPanel.add(paramPanel);
		paramPanel.setLayout(new GridLayout(0, 2, 0, 0));

		numTestsTextFieldRead = new JTextField();
		numTestsTextFieldRead.setEditable(false);
		numTestsTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Number Of Tests", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(numTestsTextFieldRead);
		numTestsTextFieldRead.setColumns(10);

		testLengthTextFieldRead = new JTextField();
		testLengthTextFieldRead.setEditable(false);
		testLengthTextFieldRead.setColumns(10);
		testLengthTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Test Length (s)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(testLengthTextFieldRead);

		accelGyroSampleRateTextFieldRead = new JTextField();
		accelGyroSampleRateTextFieldRead.setEditable(false);
		accelGyroSampleRateTextFieldRead.setColumns(10);
		accelGyroSampleRateTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accel/Gyro Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(accelGyroSampleRateTextFieldRead);

		magSampleRateTextFieldRead = new JTextField();
		magSampleRateTextFieldRead.setEditable(false);
		magSampleRateTextFieldRead.setColumns(10);
		magSampleRateTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Mag Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(magSampleRateTextFieldRead);

		accelSensitivityTextFieldRead = new JTextField();
		accelSensitivityTextFieldRead.setEditable(false);
		accelSensitivityTextFieldRead.setColumns(10);
		accelSensitivityTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accelerometer Sensitivity (G)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(accelSensitivityTextFieldRead);

		gyroSensitivityTextFieldRead = new JTextField();
		gyroSensitivityTextFieldRead.setEditable(false);
		gyroSensitivityTextFieldRead.setColumns(10);
		gyroSensitivityTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gyroscope Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(gyroSensitivityTextFieldRead);

		accelFilterTextFieldRead = new JTextField();
		accelFilterTextFieldRead.setEditable(false);
		accelFilterTextFieldRead.setColumns(10);
		accelFilterTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accelerometer Filter (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(accelFilterTextFieldRead);

		gyroFilterTextFieldRead = new JTextField();
		gyroFilterTextFieldRead.setEditable(false);
		gyroFilterTextFieldRead.setColumns(10);
		gyroFilterTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gyroscope Filter (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(gyroFilterTextFieldRead);

		JPanel configurationPanel = new JPanel();
		configurationPanel.setPreferredSize(new Dimension(500, 1000));
		configurationPanel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		mainTabbedPanel.addTab("Configurations", null, configurationPanel, null);
		configurationPanel.setLayout(new GridLayout(0, 2, 0, 0));

		timedTestCheckbox = new JCheckBox("Timed Test");
		timedTestCheckbox.setSelected(true);
		timedTestCheckbox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(timedTestCheckbox);

		timedTestCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateDataFields();
			}
		});

		triggerOnReleaseCheckbox = new JCheckBox("Trigger on Release");
		triggerOnReleaseCheckbox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		triggerOnReleaseCheckbox.setSelected(true);
		configurationPanel.add(triggerOnReleaseCheckbox);


		accelGyroSampleRateCombobox = new JComboBox();
		accelGyroSampleRateCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelGyroSampleRateCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accel/Gyro Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(accelGyroSampleRateCombobox);
		accelGyroSampleRateCombobox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateMagSampleRate();
				updateTickThresh();
			}
		});

		magSampleRateTextField = new JTextField();
		magSampleRateTextField.setEditable(false);
		magSampleRateTextField.setToolTipText("Automatically updates based on Accel/Gyro Sample Rate. Type desired sample rate then press 'Enter'");
		magSampleRateTextField.setText("96");
		magSampleRateTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		magSampleRateTextField.setColumns(10);
		magSampleRateTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Magnetometer Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(magSampleRateTextField);

		accelSensitivityCombobox = new JComboBox();
		accelSensitivityCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelSensitivityCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accelerometer Sensitivity (G)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(accelSensitivityCombobox);

		gyroSensitivityCombobox = new JComboBox();
		gyroSensitivityCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		gyroSensitivityCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gyroscope Sensitivity (dps)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(gyroSensitivityCombobox);

		accelFilterCombobox = new JComboBox();
		accelFilterCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelFilterCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accelerometer Filter (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(accelFilterCombobox);

		gyroFilterCombobox = new JComboBox();
		gyroFilterCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		gyroFilterCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gyroscope Filter (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(gyroFilterCombobox);

		testLengthTextField = new JTextField();
		testLengthTextField.setToolTipText("Minimum of 2 seconds, maximum of 65535 seconds");
		testLengthTextField.setText("25");
		testLengthTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		testLengthTextField.setColumns(10);
		testLengthTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Test Duration (Seconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(testLengthTextField);

		batteryTimeoutTextField = new JTextField();
		batteryTimeoutTextField.setToolTipText("Minimum of 1 second, maximum of 65535 seconds");
		batteryTimeoutTextField.setText("300");
		batteryTimeoutTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		batteryTimeoutTextField.setColumns(10);
		batteryTimeoutTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Battery Timeout Length (Seconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(batteryTimeoutTextField);

		timer0TickThreshTextField = new JTextField();
		timer0TickThreshTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		timer0TickThreshTextField.setEditable(false);
		timer0TickThreshTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Timer0 Tick Threshold (Read Only)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(timer0TickThreshTextField);
		timer0TickThreshTextField.setColumns(10);

		delayAfterStartTextField = new JTextField();
		delayAfterStartTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		delayAfterStartTextField.setEditable(false);
		delayAfterStartTextField.setColumns(10);
		delayAfterStartTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Delay After Start (Milliseconds) (Read Only)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(delayAfterStartTextField);

		writeConfigsButton = new JButton("Write Configurations");
		writeConfigsButton.setToolTipText("Sends new test configurations to the module");
		writeConfigsButton.setEnabled(false);
		writeConfigsButton.setBorder(null);
		writeConfigsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				writeButtonHandler();
			}
		});

		getCurrentConfigurationsButton = new JButton("Get Current Configurations");
		getCurrentConfigurationsButton.setToolTipText("Reads and displays current module configurations on this tab");
		getCurrentConfigurationsButton.setEnabled(false);	
		getCurrentConfigurationsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getConfigsHandler();
			}
		});


		configurationPanel.add(getCurrentConfigurationsButton);
		writeConfigsButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(writeConfigsButton);


		erasePanel = new JPanel();
		mainTabbedPanel.addTab("Test/Erase", null, erasePanel, null);
		erasePanel.setLayout(new GridLayout(3, 1, 0, 0));

		eraseButtonPanel = new JPanel();
		erasePanel.add(eraseButtonPanel);
		eraseButtonPanel.setLayout(new GridLayout(0, 1, 0, 0));

		sectorEraseButton = new JButton("Sector Erase");
		sectorEraseButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		eraseButtonPanel.add(sectorEraseButton);
		sectorEraseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sectorEraseHandler();
			}
		});

		startTestBtn = new JButton("Start Test");
		startTestBtn.setEnabled(false);
		startTestBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startTestBtnHandler();
			}
		});

		bulkEraseButton = new JButton("Bulk Erase");
		bulkEraseButton.setToolTipText("Make sure the LED is YELLOW after pressing this button! There is a 70 second timeout.");
		erasePanel.add(bulkEraseButton);
		bulkEraseButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bulkEraseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bulkEraseHandler();
			}
		});
		startTestBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		erasePanel.add(startTestBtn);

		JPanel calibrationPanel = new JPanel();
		mainTabbedPanel.addTab("Calibration", null, calibrationPanel, null);
		calibrationPanel.setLayout(new GridLayout(0, 1, 0, 0));

		configForCalButton = new JButton("Configure Module for Calibration");
		configForCalButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		configForCalButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				configForCalHandler();
			}
		});
		calibrationPanel.add(configForCalButton);

		importCalDataButton = new JButton("Import Calibration Data and Calculate Offset");
		importCalDataButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		importCalDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				importCalDataHandler();
			}
		});

		videoBrowsePanel = new JPanel();
		calibrationPanel.add(videoBrowsePanel);
		videoBrowsePanel.setLayout(new BoxLayout(videoBrowsePanel, BoxLayout.X_AXIS));

		videoFilePathTextField = new JTextField();
		videoFilePathTextField.setMaximumSize(new Dimension(500, 2147483647));
		videoFilePathTextField.setMinimumSize(new Dimension(500, 100));
		videoFilePathTextField.setColumns(10);
		videoFilePathTextField.setBorder(new TitledBorder(null, "File Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		videoBrowsePanel.add(videoFilePathTextField);

		videoBrowseButton = new JButton("Browse");
		videoBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				videoBrowseButtonHandler();
			}
		});
		videoBrowseButton.setMinimumSize(new Dimension(160, 100));
		videoBrowseButton.setPreferredSize(new Dimension(81, 35));
		videoBrowseButton.setMaximumSize(new Dimension(160, 100));
		videoBrowsePanel.add(videoBrowseButton);
		calibrationPanel.add(importCalDataButton);

		applyOffsetButton = new JButton("Apply Offset to Module");
		applyOffsetButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		applyOffsetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				applyOffsetsHandler();
			}
		});

		calOffsetsPanel = new JPanel();
		calibrationPanel.add(calOffsetsPanel);
		calOffsetsPanel.setLayout(new GridLayout(0, 2, 0, 0));

		tmr0OffsetTextField = new JTextField();
		calOffsetsPanel.add(tmr0OffsetTextField);
		tmr0OffsetTextField.setHorizontalAlignment(SwingConstants.LEFT);
		tmr0OffsetTextField.setText("0");
		tmr0OffsetTextField.setFont(new Font("Tahoma", Font.PLAIN, 16));
		tmr0OffsetTextField.setColumns(10);
		tmr0OffsetTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Timer0 Calibration Offset (Ticks)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));

		delayAfterTextField = new JTextField();
		delayAfterTextField.setText("0");
		delayAfterTextField.setHorizontalAlignment(SwingConstants.LEFT);
		delayAfterTextField.setFont(new Font("Tahoma", Font.PLAIN, 16));
		delayAfterTextField.setColumns(10);
		delayAfterTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Delay After Start (milliseconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		calOffsetsPanel.add(delayAfterTextField);
		calibrationPanel.add(applyOffsetButton);

		remoteTab = new JPanel();
		mainTabbedPanel.addTab("Remote Configuration", null, remoteTab, null);
		remoteTab.setLayout(new GridLayout(0, 1, 0, 0));

		RemoteButtonPanel = new JPanel();
		remoteTab.add(RemoteButtonPanel);
		RemoteButtonPanel.setLayout(new GridLayout(0, 1, 0, 0));

		pairNewRemoteButton = new JButton("Pair New Remote");
		pairNewRemoteButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		RemoteButtonPanel.add(pairNewRemoteButton);
		pairNewRemoteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pairNewRemoteHandler();
			}
		});

		unpairAllRemotesButton = new JButton("Unpair All Remotes");
		unpairAllRemotesButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		RemoteButtonPanel.add(unpairAllRemotesButton);

		testRemotesButton = new JButton("Test Paired Remotes");
		testRemotesButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		testRemotesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				testRemotesHandler();
			}
		});

		RemoteButtonPanel.add(testRemotesButton);

		exitTestModeButton = new JButton("Exit Test Mode");
		exitTestModeButton.setEnabled(false);
		exitTestModeButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		exitTestModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exitTestModeHandler();
			}
		});

		RemoteButtonPanel.add(exitTestModeButton);

		testRecordationPanel = new JPanel();
		mainTabbedPanel.addTab("Stored Tests", null, testRecordationPanel, null);
		testRecordationPanel.setLayout(null);
		
		JButton loadTestBtn = new JButton("Load Test From File");
		loadTestBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				csvBrowseButtonHandler();
			}
		});
		loadTestBtn.setBounds(485, 349, 140, 23);
		testRecordationPanel.add(loadTestBtn);

		adminPanel = new JPanel();
		mainTabbedPanel.addTab("Admin Panel", null, adminPanel, null);

		adminPanel.setLayout(new GridLayout(4, 1, 30, 0));
		unpairAllRemotesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				unpairAllRemotesHandler();
			}
		});



		JPanel progressPanel = new JPanel();
		contentPanel.add(progressPanel);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(630, 20));
		progressPanel.add(progressBar);

		sendQuitCMDButton = new JButton("Exit UART Mode");
		sendQuitCMDButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					serialHandler.sendExitCommand();
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

			}
		});
		contentPanel.add(sendQuitCMDButton);

		separator = new JSeparator();
		contentPanel.add(separator);
		separator.setOrientation(SwingConstants.VERTICAL);

		JPanel copyrightPanel = new JPanel();
		contentPanel.add(copyrightPanel);
		copyrightPanel.setLayout(new BorderLayout(10, 0));

		JLabel copyrightLabel = new JLabel("Copyright nth Solutions LLC. 2018");
		contentPanel.add(copyrightLabel);

		settingsWindowBtn = new JButton("Settings");
		contentPanel.add(settingsWindowBtn);
		settingsWindowBtn.setHorizontalAlignment(SwingConstants.LEFT);

		settingsWindowBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) { 
				new SettingsWindow().setVisible(true);
			}
		});

		
		

		frameInitialized = true;
	}
}
