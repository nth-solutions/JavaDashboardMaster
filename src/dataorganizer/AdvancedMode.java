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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import javax.swing.JSeparator;

public class AdvancedMode extends JFrame {

	/**
	 * COMMENT TERMINOLOGY:
	 * Caller: The class, object, location, or method in which the currently executing method was called. ex) The bulkEraseHandler() 'Calls' the bulkEraseModule() method so the bulkEraseHandler() is the caller
	 * User: The person using the dashboard, not the programmer.
	 */


	//GUI Elements, define here if they need to be accessed in the Dashboard class. To quickly identify what panel you want to reference, open the GUI in
	//WindowBuilder, click on the desired GUI item and see what it's "Variable" field name is. It will be defined at the bottom in the initComponents() method by default so move the definition up here

	//Panels
	private JPanel contentPanel;
	private JTabbedPane mainTabbedPanel;
	private JPanel startReadButtonPanel;
	private JPanel fileNamePanel;
	private JPanel paramPanel;
	private JPanel getModuleIDPanel;
	private JPanel fileNameModifierPanel;
	private JPanel fileLocationPanel;
	private JPanel serialPortPanel;
	private JPanel templateTools;
	private JPanel erasePanel;
	private JPanel eraseButtonPanel;
	private JPanel remoteTab;

	//Labels
	private JLabel generalStatusLabel;
	private JLabel moduleSerialNumberLabel;
	private JLabel hardwareIDLabel;
	private JLabel firmwareIDLabel;

	//CheckBoxes
	private JCheckBox timedTestCheckbox;
	private JCheckBox delayAfterStartCheckbox;
	private JCheckBox manualCalibrationCheckbox;
	private JCheckBox triggerOnReleaseCheckbox;

	//Text Fields

	//Configuration Tab
	private JTextField testLengthTextField;
	private JTextField accelGyroSampleRateTextField;
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

	//Combo Boxes
	private JComboBox commPortCombobox;
	private JComboBox accelSensitivityCombobox;
	private JComboBox gyroSensitivityCombobox;
	private JComboBox accelFilterCombobox;
	private JComboBox gyroFilterCombobox;
	private JComboBox csvDataFile;

	//Buttons
	private JButton refreshPortButton;
	private JButton disconnectButton;
	private JButton writeConfigsButton;
	private JButton getModuleIDButton;
	private JButton browseButton;
	private JButton readDataButton;
	private JButton bulkEraseButton;
	private JButton sectorEraseButton;
	private JButton openTemplateBtn;
	private JButton settingsWindowBtn;
	private JButton btnSelectCsv;
	private JButton sendQuitCMDButton;

	//Progress Bars
	private JProgressBar progressBar;

	//JSeparators
	private JSeparator separator;

	//Test Parameter Variables and Constants
	public static final int NUM_TEST_PARAMETERS = 15;
	public static final int NUM_ID_INFO_PARAMETERS = 3;

	private int expectedTestNum;
	//Test Parameters (All must be of type "int")
	private int timedTestFlag;
	private int triggerOnReleaseFlag;
	private int battTimeoutLength;
	private int timer0TickThreshold;
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
	private Thread getParamThread;

	//Output File Info and Variables
	private String nameOfFile = "";     			//Sets the name of file to an empty string to start
	private String fileOutputDirectoryStr;			//The directory to write the test to
	private String templateChosen;
	private String csvDataFileChosen;
	private static SerialComm serialHandler;
	private CSVBuilder csvBuilder = new CSVBuilder();  //Object of class used to organize passed in data to convert and format data into .CSV

	//Flags
	private boolean frameInitialized = false;

	//Serial Port Variables
	private SerialPort serialPort;      			//Object for the serial port class
	private static CommPortIdentifier portId;       //Object used for opening a COMM ports
	private static Enumeration portList;            //Object used for finding COMM ports
	private BufferedInputStream inputStream;             //Object used for reading serial data 
	private OutputStream outputStream;              //Object used for writing serial data

	public static AdvancedMode guiInstance;		//The single instance of the dashboard that can be referenced anywhere in the class. Defined to follow the Singleton Method: https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples		
	private JButton unpairAllRemotesButton;
	private JPanel RemoteButtonPanel;
	private JButton pairNewRemoteButton;
	private JButton getCurrentConfigurationsButton;
	private JButton testRemotesButton;



	/**
	 * Dashboard constructor that initialzies the name of the window, all the components on it, and the data within the necessary text fields
	 */
	private AdvancedMode() {
		setTitle("JavaDashboardMaster");
		createComponents();
		initDataFields();
		updateCommPortComboBox();
		setVisible(true);
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



		while(true) {
		}
	}

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

	public void pairNewRemoteHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable pairNewRemoteOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				pairNewRemoteButton.setEnabled(false);
				unpairAllRemotesButton.setEnabled(false);

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
				
			}
		};

		//Define a new thread to run the operation previously defined
		pairNewRemoteThread = new Thread(pairNewRemoteOperation);
		//Start the thread
		pairNewRemoteThread.start();
	}


	public void unpairAllRemotesHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable unpairAllRemotesOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				pairNewRemoteButton.setEnabled(false);
				unpairAllRemotesButton.setEnabled(false);

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
				//Notify the user that the bulk erase sequence has began
				generalStatusLabel.setText("Bulk Erasing...");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {

					serialHandler.bulkEraseModule();
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
				bulkEraseButton.setEnabled(true);
				sectorEraseButton.setEnabled(true);
				//Notify the user that the sequence has completed
				generalStatusLabel.setText("Bulk Erase Complete");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(51, 204, 51));
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
				//Notify the user that the bulk erase sequence has began
				generalStatusLabel.setText("Sector Erasing...");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					serialHandler.sectorEraseModule();
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
				bulkEraseButton.setEnabled(true);
				sectorEraseButton.setEnabled(true);
				//Notify the user that the sequence has completed
				generalStatusLabel.setText("Sector Erase Complete");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(51, 204, 51));
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
						generalStatusLabel.setText("Module Information Successfully Received");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
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

	public void getConfigsHandler() {
		//Disable read button while read is in progress
		getCurrentConfigurationsButton.setEnabled(false);
		Runnable getConfigsOperation = new Runnable() {
			public void run() {
				try {

					generalStatusLabel.setText("Reading Current Module Configurations...");
					progressBar.setValue(0);
					progressBar.setForeground(new Color(51, 204, 51));
					
					ArrayList<Integer> testParameters = new ArrayList<Integer>();

					testParameters = serialHandler.readTestParams();

					//Assign local variables to their newly received values from the module
					timer0TickThreshold = testParameters.get(4);
					battTimeoutLength = testParameters.get(5);
					timedTestFlag = testParameters.get(7);
					triggerOnReleaseFlag = testParameters.get(7);
					testLength = testParameters.get(9);
					accelGyroSampleRate = testParameters.get(10);
					magSampleRate = testParameters.get(11);
					accelSensitivity = testParameters.get(12);
					gyroSensitivity = testParameters.get(13);
					accelFilter = testParameters.get(14);
					gyroFilter = testParameters.get(15);					


					//TODO: Populate gui with the parameters sent by the module
					if(timedTestFlag > 0) {
						timedTestCheckbox.setSelected(true);
					}
					else {
						timedTestCheckbox.setSelected(false);
					}
					if(triggerOnReleaseFlag > 0) {
						triggerOnReleaseCheckbox.setSelected(true);
					}
					else {
						triggerOnReleaseCheckbox.setSelected(false);
					}
					
					accelGyroSampleRateTextField.setText(Integer.toString(accelGyroSampleRate));
					magSampleRateTextField.setText(Integer.toString(magSampleRate));
					//accelSensitivityCombobox.setSelectedIndex(lookupAccelSensitivityIndex(accelSensitivity));
					//accelSensitivityCombobox.setSelectedIndex(lookupGyroSensitivityIndex(gyroSensitivity));
					
					timer0TickThreshTextField.setText(Integer.toString(timer0TickThreshold));
					magSampleRateTextField.setText(Integer.toString(magSampleRate));
					batteryTimeoutTextField.setText(Integer.toString(battTimeoutLength));
					
					
					generalStatusLabel.setText("Current Module Configurations Received and Displayed");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(51, 204, 51));

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
		if (updateMagSampleRate()) {
			//Define no operation that can be run in a thread
			Runnable sendParamOperation = new Runnable() {
				public void run() {
					//Disable write config button while the sendParameters() method is running
					writeConfigsButton.setEnabled(false);

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


						//0 Serial Number
						testParams.add(0);
						//1 Hardware Version
						testParams.add(5);
						//2 Firmware Version
						testParams.add(20);
						//3 Accel Gyro Sample Rate
						testParams.add(getTickThreshold(Integer.parseInt(accelGyroSampleRateTextField.getText())));
						//4 Delay after start
						testParams.add(0);
						//5 Battery timeout flag
						testParams.add(Integer.parseInt(batteryTimeoutTextField.getText()));
						//6 Timed test flag
						testParams.add(timedTestFlag);
						//7 Trigger on release flag
						testParams.add(triggerOnReleaseFlag);
						//8 Test Length
						testParams.add(Integer.parseInt(testLengthTextField.getText()));
						//9 Accel Gyro Sample Rate
						testParams.add(Integer.parseInt(accelGyroSampleRateTextField.getText()));
						//10 Mag Sample Rate
						testParams.add(Integer.parseInt(magSampleRateTextField.getText()));
						//11 Accel Sensitivity
						testParams.add(Integer.parseInt(accelSensitivityCombobox.getSelectedItem().toString()));
						//12 Gyro Sensitivity
						testParams.add(Integer.parseInt(gyroSensitivityCombobox.getSelectedItem().toString()));
						//13 Accel Filter
						testParams.add(Integer.parseInt(accelFilterCombobox.getSelectedItem().toString()));
						//14 Gyro Filter
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
				}
			};

			//Assign new operation to a thread so that it can be run in the background
			paramThread = new Thread(sendParamOperation);
			//Start the new thread
			paramThread.start();
		}

	}


	/**
	 * Handles the button press of read data from module button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume. See the method calls within the runnable for more info
	 * on what this handler actually does.
	 */
	public void readButtonHandler() {
		//Define operation that can be run in separate thread
		Runnable readOperation = new Runnable() {
			public void run() {
				//Disable read button while read is in progress
				readDataButton.setEnabled(false);

				try {

					ArrayList<Integer> testParameters = new ArrayList<Integer>();

					generalStatusLabel.setText("Reading Data from Module...");
					progressBar.setValue(0);
					progressBar.setForeground(new Color(51, 204, 51));
					
					testParameters = serialHandler.readTestParams();

					if (testParameters != null) {
						expectedTestNum = testParameters.get(0);
						//Assign local variables to their newly received values from the module
						timedTestFlag = testParameters.get(7);
						//Trigger on release is 8
						testLength = testParameters.get(9);
						accelGyroSampleRate = testParameters.get(10);
						magSampleRate = testParameters.get(11);
						accelSensitivity = testParameters.get(12);
						gyroSensitivity = testParameters.get(13);
						accelFilter = testParameters.get(14);
						gyroFilter = testParameters.get(15);				
						
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

						if(expectedTestNum > 0) {

							//Get date for file name
							Date date = new Date();

							//Assign file name
							nameOfFile = "";
							nameOfFile += (" " + accelGyroSampleRate + "-" + magSampleRate + " " + accelSensitivity + "G-" + accelFilter + " " + gyroSensitivity + "dps-" + gyroFilter + " MAG-N " + date.getDate() + getMonth(date.getMonth()) + (date.getYear() - 100) + ".csv");
							fileNameTextField.setText(nameOfFile);

							HashMap<Integer, ArrayList<Integer>> testData;

							testData = serialHandler.readTestData(expectedTestNum, progressBar, timedTest, (int) (bytesPerSample * accelGyroSampleRate * testLength));
							
							generalStatusLabel.setText("All Data Received from Module");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(51, 204, 51));

							if(testData != null) {
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

									
									//Define operation that can be run in separate thread
									Runnable organizerOperation = new Runnable() {
										public void run() {
											//Organize data into .CSV
											csvBuilder.sortData(finalData, nameOfFile, (accelGyroSampleRate / magSampleRate), fileOutputDirectoryStr);  
										}
									};

									//Set thread to execute previously defined operation
									organizerThread = new Thread(organizerOperation);
									//Start thread
									organizerThread.start();
								}
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
		if (mainTabbedPanel.getSelectedIndex() == 1) {
			//Checkboxes
			timedTestCheckbox.setSelected(true);
			delayAfterStartCheckbox.setSelected(false);
			manualCalibrationCheckbox.setSelected(false);

			//Text Fields
			testLengthTextField.setText("25");
			accelGyroSampleRateTextField.setText("120");
			magSampleRateTextField.setText("120");
			delayAfterStartTextField.setText("0");
			timer0TickThreshTextField.setText("0");
			batteryTimeoutTextField.setText("300");


			//Comboboxes
			accelSensitivityCombobox.setModel(new DefaultComboBoxModel(new String [] {"2", "4", "8", "16"}));
			gyroSensitivityCombobox.setModel(new DefaultComboBoxModel(new String [] {"250", "500", "1000", "2000"}));
			accelFilterCombobox.setModel(new DefaultComboBoxModel(new String [] {"5", "10", "20", "41", "92", "184", "460", "1130 (OFF)"}));
			gyroFilterCombobox.setModel(new DefaultComboBoxModel(new String [] {"10", "20", "41", "92", "184", "250", "3600", "8800 (OFF)"}));

			//Set Default Selection for Comboboxes
			accelSensitivityCombobox.setSelectedIndex(3);	//16g
			gyroSensitivityCombobox.setSelectedIndex(3);	//2000dps
			accelFilterCombobox.setSelectedIndex(4);		//92Hz
			gyroFilterCombobox.setSelectedIndex(3);			//92Hz
		}

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
		}
	}
	

	public boolean updateMagSampleRate() {
		if (!accelGyroSampleRateTextField.getText().isEmpty()) {
			switch (Integer.parseInt(accelGyroSampleRateTextField.getText())) {
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
				generalStatusLabel.setText("Please Enter a Valid Accel/Gyro Sample Rate");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(255, 0, 0));
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
			return 3848;
		default:	//960-96
			return 3848;
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


	/**
	 * Creates and initializes the properties of all components on the main dashboard window. ex) panels, buttons, text fields, etc.
	 */
	public void createComponents() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 550, 665);
		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPanel);
		contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));


		serialPortPanel = new JPanel();
		serialPortPanel.setPreferredSize(new Dimension(500, 150));
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

		JPanel mainPanelContainer = new JPanel();
		contentPanel.add(mainPanelContainer);
		mainPanelContainer.setLayout(new GridLayout(0, 1, 0, 0));

		mainTabbedPanel = new JTabbedPane(JTabbedPane.TOP);
		mainTabbedPanel.setPreferredSize(new Dimension(500, 400));
		mainPanelContainer.add(mainTabbedPanel);

		mainTabbedPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				initDataFields();
			}
		});


		JPanel readPanel = new JPanel();
		readPanel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		mainTabbedPanel.addTab("Read Mode", null, readPanel, null);
		readPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		fileNamePanel = new JPanel();
		fileNamePanel.setPreferredSize(new Dimension(500, 150));
		readPanel.add(fileNamePanel);
		fileNamePanel.setLayout(new GridLayout(0, 1, 0, 0));

		startReadButtonPanel = new JPanel();
		fileNamePanel.add(startReadButtonPanel);
		startReadButtonPanel.setLayout(new GridLayout(0, 1, 0, 0));

		readDataButton = new JButton("Read Data from Module");
		readDataButton.setEnabled(false);
		startReadButtonPanel.add(readDataButton);
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
		fileNameTextField.setMaximumSize(new Dimension(350, 50));
		fileNameTextField.setBorder(new TitledBorder(null, "File Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		fileLocationPanel.add(fileNameTextField);
		fileNameTextField.setColumns(10);

		browseButton = new JButton("Browse");
		browseButton.setMaximumSize(new Dimension(150, 50));
		browseButton.setPreferredSize(new Dimension(81, 35));
		fileLocationPanel.add(browseButton);

		browseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				browseButtonHandler();
			}
		});

		paramPanel = new JPanel();
		paramPanel.setPreferredSize(new Dimension(500, 200));
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
		testLengthTextFieldRead.setBorder(new TitledBorder(null, "Test Length", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		paramPanel.add(testLengthTextFieldRead);

		accelGyroSampleRateTextFieldRead = new JTextField();
		accelGyroSampleRateTextFieldRead.setEditable(false);
		accelGyroSampleRateTextFieldRead.setColumns(10);
		accelGyroSampleRateTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accel/Gyro Sample Rate", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(accelGyroSampleRateTextFieldRead);

		magSampleRateTextFieldRead = new JTextField();
		magSampleRateTextFieldRead.setEditable(false);
		magSampleRateTextFieldRead.setColumns(10);
		magSampleRateTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Mag Sample Rate", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
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

		delayAfterStartCheckbox = new JCheckBox("Delay After Start");
		delayAfterStartCheckbox.setEnabled(false);
		delayAfterStartCheckbox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(delayAfterStartCheckbox);

		delayAfterStartCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateDataFields();
			}
		});

		manualCalibrationCheckbox = new JCheckBox("Manual Calibration");
		manualCalibrationCheckbox.setEnabled(false);
		manualCalibrationCheckbox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(manualCalibrationCheckbox);

		manualCalibrationCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateDataFields();
			}
		});

		triggerOnReleaseCheckbox = new JCheckBox("Trigger on Release");
		triggerOnReleaseCheckbox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		triggerOnReleaseCheckbox.setSelected(true);
		configurationPanel.add(triggerOnReleaseCheckbox);

		accelGyroSampleRateTextField = new JTextField();
		accelGyroSampleRateTextField.setToolTipText("Valid sample rates: 960, 500, 480, 240, 120, 60");
		accelGyroSampleRateTextField.setText("960");
		accelGyroSampleRateTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelGyroSampleRateTextField.setColumns(10);
		accelGyroSampleRateTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accel/Gyro Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		accelGyroSampleRateTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateMagSampleRate();
			}
		});

		configurationPanel.add(accelGyroSampleRateTextField);

		magSampleRateTextField = new JTextField();
		magSampleRateTextField.setToolTipText("Automatically updates based on Accel/Gyro Sample Rate. Type desired sample rate then press 'Enter'");
		magSampleRateTextField.setEnabled(false);
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

		timer0TickThreshTextField = new JTextField();
		timer0TickThreshTextField.setText("3689");
		timer0TickThreshTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		timer0TickThreshTextField.setEditable(false);
		timer0TickThreshTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Timer0 Tick Threshold", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(timer0TickThreshTextField);
		timer0TickThreshTextField.setColumns(10);

		delayAfterStartTextField = new JTextField();
		delayAfterStartTextField.setText("0");
		delayAfterStartTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		delayAfterStartTextField.setEditable(false);
		delayAfterStartTextField.setColumns(10);
		delayAfterStartTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Delay After Start (Microseconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(delayAfterStartTextField);

		testLengthTextField = new JTextField();
		testLengthTextField.setToolTipText("Minimum of 2 seconds, maximum of 65535 seconds");
		testLengthTextField.setText("25");
		testLengthTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		testLengthTextField.setColumns(10);
		testLengthTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Test Duration (Seconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(testLengthTextField);

		writeConfigsButton = new JButton("Write Configurations");
		writeConfigsButton.setToolTipText("Sends new test configurations to the module");
		writeConfigsButton.setEnabled(false);
		writeConfigsButton.setBorder(null);
		writeConfigsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				writeButtonHandler();
			}
		});

		batteryTimeoutTextField = new JTextField();
		batteryTimeoutTextField.setToolTipText("Minimum of 1 second, maximum of 65535 seconds");
		batteryTimeoutTextField.setText("300");
		batteryTimeoutTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		batteryTimeoutTextField.setColumns(10);
		batteryTimeoutTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Battery Timeout Length (Seconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(batteryTimeoutTextField);

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
		mainTabbedPanel.addTab("Erase", null, erasePanel, null);
		erasePanel.setLayout(new GridLayout(1, 0, 0, 0));

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

		bulkEraseButton = new JButton("Bulk Erase");
		bulkEraseButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		eraseButtonPanel.add(bulkEraseButton);
		bulkEraseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bulkEraseHandler();
			}
		});

		JPanel calibrationPanel = new JPanel();
		mainTabbedPanel.addTab("Calibration", null, calibrationPanel, null);

		templateTools = new JPanel();
		mainTabbedPanel.addTab("Template Tools", null, templateTools, null);

		LoadSettings settings = new LoadSettings();
		settings.loadConfigFile();

		templateTools.setLayout(new GridLayout(4, 1, 30, 0));


		openTemplateBtn = new JButton("Open");
		openTemplateBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String outputFile = null;
				JFileChooser chooser;
				chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					outputFile = chooser.getSelectedFile().toString();
				}
				else {
					//Closed
				}
				TemplateOpenerClass.start(settings.getKeyVal("TemplateDirectory")+templateChosen, outputFile, settings.getKeyVal("CSVSaveLocation")+"\\"+csvDataFileChosen);
			}
		});
		templateTools.add(openTemplateBtn);

		csvDataFile = new JComboBox();
		csvDataFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				csvDataFileChosen = csvDataFile.getSelectedItem().toString();
			}
		});
		csvDataFile.setToolTipText("CSV File Selection");

		File[] csvFileList = new File(settings.getKeyVal("CSVSaveLocation")).listFiles();
		ArrayList<String> csvList = new ArrayList<String>();
		if(csvFileList!=null) {
			for(int i=0; i<csvFileList.length;i++) {
				csvList.add(csvFileList[i].toString().substring(csvFileList[i].toString().lastIndexOf("\\")+1, csvFileList[i].toString().length()));
			}
			csvDataFile.setModel(new DefaultComboBoxModel(csvList.toArray()));
		}
		templateTools.add(csvDataFile);

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
		RemoteButtonPanel.add(testRemotesButton);
		unpairAllRemotesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				unpairAllRemotesHandler();
			}
		});



		JPanel progressPanel = new JPanel();
		contentPanel.add(progressPanel);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(500, 20));
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

		settingsWindowBtn = new JButton("...");
		contentPanel.add(settingsWindowBtn);
		settingsWindowBtn.setHorizontalAlignment(SwingConstants.LEFT);

		settingsWindowBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) { 
				new SettingsWindow().setVisible(true);
			}
		});

		separator = new JSeparator();
		contentPanel.add(separator);
		separator.setOrientation(SwingConstants.VERTICAL);

		JPanel copyrightPanel = new JPanel();
		contentPanel.add(copyrightPanel);
		copyrightPanel.setLayout(new BorderLayout(10, 0));

		JLabel copyrightLabel = new JLabel("Copyright nth Solutions LLC. 2018");
		contentPanel.add(copyrightLabel);


		frameInitialized = true;
	}

}
