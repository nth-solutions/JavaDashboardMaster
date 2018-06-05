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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import javax.swing.JSeparator;

public class Dashboard extends JFrame {

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
	private JPanel panel;

	//Labels
	private JLabel generalStatusLabel;
	private JLabel moduleSerialNumberLabel;
	private JLabel hardwareIDLabel;
	private JLabel firmwareIDLabel;

	//CheckBoxes
	private JCheckBox timedTestCheckbox;
	private JCheckBox delayAfterStartCheckbox;
	private JCheckBox manualCalibrationCheckbox;

	//Text Fields

	//Configuration Tab
	private JTextField testLengthTextField;
	private JTextField accelGyroSampleRateTextField;
	private JTextField magSampleRateTextField;
	private JTextField delayAfterStartTextField;
	private JTextField timer0TickThreshTextField;

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
	private JComboBox templateSourceFileList;
	private JComboBox csvDataFile;

	//Buttons
	private JButton refreshPortButton;
	private JButton disconnectButton;
	private JButton getCurrentConfigsButton;
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
	public static final int NUM_TEST_PARAMETERS = 13;

	//Test Parameters (All must be of type "int")
	private int timedTestFlag;
	private int testLength;      			
	private int accelGyroSampleRate;    		
	private int magSampleRate;          			
	private int accelSensitivity;       		
	private int gyroSensitivity;        		
	private int accelFilter;            			
	private int gyroFilter;             		

	//Flags
	private boolean readMode = true;
	private boolean organizeAbort = false;
	private boolean readAbort = false;
	private boolean paramAbort = false;
	private boolean portInitialized = false;
	private boolean frameInitialized = false;
	private boolean portOpened = false;
	private boolean dataStreamsInitialized = false;

	//Operation Threads
	private Thread readThread;
	private Thread paramThread;
	private Thread infoThread;
	private Thread bulkEraseThread;

	//Output File Info and Variables
	private String nameOfFile = "";     			//Sets the name of file to an empty string to start
	private String fileOutputDirectoryStr;			//The directory to write the test to
	private String templateChosen;
	private String csvDataFileChosen;
	private int expectedTestNum = 0;                //The number of test that are expected to be received 
	private Organizer organizer = new Organizer();  //Object of class used to organize passed in data to convert and format data into .CSV

	//Serial Port Variables
	private SerialPort serialPort;      			//Object for the serial port class
	private static CommPortIdentifier portId;       //Object used for opening a COMM ports
	private static Enumeration portList;            //Object used for finding COMM ports
	private BufferedReader inputStream;             //Object used for reading serial data 
	private OutputStream outputStream;              //Object used for writing serial data

	public static Dashboard dashboardInstance;		//The single instance of the dashboard that can be referenced anywhere in the class. Defined to follow the Singleton Method: https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples		



	/**
	 * Dashboard constructor that initialzies the name of the window, all the components on it, and the data within the necessary text fields
	 */
	private Dashboard() {
		setTitle("JavaDashboard_Rev-3 6/4/2018");
		createComponents();
		initDataFields();
		setVisible(true);
	}

	/**
	 * Necessary for singleton design pattern, especially the "synchronized" keyword for more info on the singleton method: https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples
	 * @return the one and only allowed dashboard instance, singleton pattern specifies only one instance can exist so there are not several instances of the dashboard with different variable values
	 */
	public static synchronized Dashboard getFrameInstance() {
		if (dashboardInstance == null) {
			dashboardInstance = new Dashboard();
		}
		return dashboardInstance;
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


		//Default the gui that will be opened to null (gui selected in following try/catch block
		JFrame gui = null;

		try {
			//Create settings object to load setting values for the save directory, profiles, template directory, and opening opening CSVs after opening a test.
			LoadSettings settings = new LoadSettings();
			//Set and load the file in which all that information is stored
			settings.loadConfigFile();

			//Executes if a default profile is defined within the configuration file
			if(settings.getKeyVal("DefaultProfile") != null) {

				//Load the default profile and it's associated GUI based on the default profile defined in the configuration file
				switch(settings.getKeyVal("DefaultProfile")) {

				//Adventurer GUI/ Profile
				case "Adventurer":
					gui = new AdventurerMode();
					break;

					//Educator GUI/ Profile
				case "Educator":
					gui = new EducatorMode();
					break;

					//Professional GUI/ Profile	
				case "Professional":
					Dashboard pro = Dashboard.getFrameInstance();
					//Find which serial ports are available and load them into the serial port selection combobox
					pro.findPorts();
					break;
				}

				//If the appropriate GUI was selected, then set it visible so the user can actually see it
				if(gui != null) {
					gui.setVisible(true);
				}
			}
		}

		//Catches all exceptions, not handled properly, to be fixed in future revision 
		catch(Exception e){
			gui = new Profiles();
		}

		while(true) {
		}
	}


	//Serial Port Methods******************************************************************************************************************************
	/**
	 * Builds a list the names of all the serial ports to place in the combo box
	 * @param evt event pasted in by any button or action that this method was called by (method of passing info related to the source)
	 */
	private void findPorts() {
		//Fills the portEnum data structure (functions like arrayList) with ports (data type that encapsulates the name and hardware interface info)
		Enumeration<CommPortIdentifier> portEnumList = CommPortIdentifier.getPortIdentifiers();   

		//Stores the names of the ports
		ArrayList<String> portNames = new ArrayList<String>();

		//Iterate through each port object in the portEnumList and stores the name of the port in the portNames array
		while (portEnumList.hasMoreElements()) {                   //adds the serial ports to a string array
			CommPortIdentifier portIdentifier = portEnumList.nextElement();
			portNames.add(portIdentifier.getName());
		}

		//If at least 1 serial port is found, fill the combo box with all the known port names. Otherwise, notify the user that there are no visible dongles. 
		if (portNames.size() > 0) {
			commPortCombobox.setEnabled(true);
			commPortCombobox.setModel(new DefaultComboBoxModel(portNames.toArray()));
		}
		else {
			generalStatusLabel.setText("No Serial Dongle Found");
		}
	}

	/**
	 * Setter for generalStatusLabel so the status label can be set from external classes
	 * @param s the string that the generalStatusLabel will be set to
	 */
	public void setGeneralStatusLabel(String s) {
		generalStatusLabel.setText(s);
	}

	/**
	 * This method handles which methods will be called when the user selects a port from the COMM port combobox. This entails looking up which port they selected and then opening that port
	 */
	private void portSelectedHandler() {

		//Executes if the user selected a valid COMM port
		if (commPortCombobox.getSelectedItem() != null) {

			//Get the string identifier (name) of the port the user selected
			String selectedCommID = commPortCombobox.getSelectedItem().toString();      

			//Open the serial port with the selected name, initialize input and output streams, set necessary flags so the whole program know that everything is initialized
			openSerialPort(selectedCommID);                                      


			//Notify the user that the port as opened successfully and is ready for a new command
			generalStatusLabel.setText("Serial Port Opened Successfully, Awaiting Commands");

			portInitialized = true;
		}

	}


	/**
	 * Opens serial port with the name passed in as a parameter in addition to initializing input and output streams.
	 * @param commPortID Name of comm port that will be opened
	 */
	public void openSerialPort(String commPortID) {     
		//Creates a list of all the ports that are available of type Enumeration (data structure that can hold several info fields such as ID, hardware interface info, and other info used by the PC 
		portList = CommPortIdentifier.getPortIdentifiers();                     

		//Iterates through all ports on the ports on the port list
		while (portList.hasMoreElements()) { 

			//Set the temporary port to the current port that is being iterated through
			CommPortIdentifier tempPortId = (CommPortIdentifier) portList.nextElement();

			//Executes if the temporary port has the same name as the one selected by the user
			if (tempPortId.getName().equals(commPortID)) {                            

				//If it does match, then assign the portID variable so the desired port will be opened later
				portId = tempPortId;

				//break the while loop
				break;
			}
		}

		//Attempt to open the serial port and if it is in use, notify the user
		try {
			//Open the serial port with a 2 second timeout
			serialPort = (SerialPort) portId.open("portHandler", 2000);
			//Set flag lets the program know that the port opened successfully
			portOpened = true;
		} 

		//FIXME: Executes if the port is already in use, notify the user (not executing properly, try setting portOpened to false
		catch (PortInUseException e) {
			generalStatusLabel.setText("Dongle Already In Use");
		}

		//Executes if the port was opened successfully
		if (portOpened) {

			//Try initializing the input and output streams, set flags, and enable serial port control buttons
			try {

				//Create a new buffered reader so we can define the buffer size to prevent a buffer overflow (explicitly defined in the configureForImport() method)
				//TODO: Explicitly define buffer everywhere
				inputStream = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

				//Assign the output stream to the output stream of the serial port (no need for a buffer as far as we know)
				outputStream = serialPort.getOutputStream();  

				//Set flag so program knows that the data streams were initialized
				dataStreamsInitialized = true;

				//Enable the buttons that can now be used since the serial port opened
				disconnectButton.setEnabled(true);
				getModuleIDButton.setEnabled(true);
				readDataButton.setEnabled(true);
				writeConfigsButton.setEnabled(true);
			} 

			//Executes if there was an error accessing the input and output streams of the serial dongles
			catch (IOException e) {
				generalStatusLabel.setText("Error Communicating with Serial Dongle");
			}
		}
	}


	/**
	 * Closes serial port and updates GUI labels/ software flags
	 */
	private void closeSerialPort() {
		//If the disconnect button is pressed: disconnects from the serial port and resets the UI   
		if (serialPort != null) {

			//TODO: Remove this line or fix the functionality, this is supposed to close all active threads
			stopAllThreads();

			//Close the serial port
			serialPort.close();  

			//Clear program flags associated with the COMM port's status
			portInitialized = false;
			portOpened = false;
			dataStreamsInitialized = false;

			//Notify the user that the port has been closed
			generalStatusLabel.setText("Port Closed");  

			//Disable buttons that only work when the port is opened
			disconnectButton.setEnabled(false);
			getModuleIDButton.setEnabled(false);
			readDataButton.setEnabled(false);
			writeConfigsButton.setEnabled(false);
		}
	}

	/**
	 * Stops the send test parameter thread (not working)
	 */
	//TODO: Fix or remove
	public void stopParamThread() {
		if (paramThread != null) {
			if(!paramThread.isAlive()) {
				paramAbort = true;
				while(paramThread.isAlive()) {
				}
				paramAbort = false;
			}
		}
	}


	/**
	 * Stops the read test data thread (not working)
	 */
	//TODO: Fix or remove
	public void stopReadThread() {
		if (readThread != null) {
			if(!readThread.isAlive()) {
				readAbort = true;
				while(readThread.isAlive()) {
				}
				readAbort = false;
			}
		}
	}

	/**
	 * Stops all threads(not working)
	 */
	//TODO: Fix or remove
	public void stopAllThreads() {

		//Read Data Thread
		stopReadThread();

		//Send Parameters Thread
		stopParamThread();
	}

	/**
	 * Clears the input stream buffer
	 */
	//TODO: Define clearer implementation or design java such that it doesn't need this method
	public void clearInputStream(){
		//Executes if the data streams are currently initialized (prevents null pointer exception)
		if (dataStreamsInitialized) {
			try {
				//Executes while there is still data in the input stream buffer
				while (inputStream.ready()) {
					//Read a value from the buffer and don't store it, just throw it away
					inputStream.read();
				}
			}
			catch (IOException e) {
				generalStatusLabel.setText("Error Communicating with Dongle");
			}

		}
	}


	//Read and Write Methods***************************************************************************************************************************
	/**
	 * Waits for a sequence of consecutive, increasing numbers then exits the loop
	 * @param start the number to start the counting sequence at. (must be less than the 'stop' parameter)
	 * @param stop the number at which the preamble is consider fully received, the ending number on the counter
	 * @return boolean that allows for easy exiting of the method in addition to notifying the caller that if it was successful or timed out
	 */
	public boolean waitForPreamble(int start, int stop) {
		try {
			//Get start time so a timeout can be used in subsequent while loop
			long startTime = System.currentTimeMillis();
			//Create and set flag so in the event of a timeout, an accurate error message can be displayed
			boolean preambleReceived = false;
			//While the loop has been executing for less than 500ms
			//TODO: This timeout will not work if it is in the internal for loop. Add timeout to for loop if necessary
			while (((System.currentTimeMillis() - startTime) < 500)) {
				//Executes if there is data in the input stream's buffer
				if (inputStream.ready()) {
					int temp;
					//Iterates until the specified preamble is received
					//TODO: Add timeout to this loop
					for(int counter = start; counter <= stop;) {

						//Store newly read byte in the temp variable (Must mod by 256 to get single byte due to quirks in BufferedReader class)
						temp = inputStream.read() % 256;

						//Executes of the byte received is equal to the current value of counter
						if (temp == counter) {    
							//Increment counter by 1
							counter++;
						} 

						//Executes if the counter != temp
						else {
							//Reset the counter
							counter = start;
						}
					}

					//Set the preamble flag to true so the the program knows that a timeout didn't occur to break the loop
					preambleReceived = true;
					//Break the while loop
					break;
				}
			}
			//Executes if the preamble was not received meaning there must have been a timeout
			if (!preambleReceived) {
				//Notify the user and exit the method
				generalStatusLabel.setText("Module Unresponsive (Timeout), Try Again");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(255, 0, 0));
				return false;
			}
		}
		//Executes if there is an error sending a command to the dongle
		catch(IOException e) {
			//Notify the user and exit the method
			generalStatusLabel.setText("Error Commicating with Dongle");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255, 0, 0));
			return false;
		}
		//Return true to exit the method and notify the caller that the method was successful
		return true;
	}


	/**
	 * Waits for a sequence of consecutive, decreasing numbers then exits the loop
	 * @param start the number to start the counting sequence at (must be greater than the 'stop' parameter)
	 * @param stop the number at which the postamble is consider fully received, the ending number on the counter
	 * @return boolean that allows for easy exiting of the method in addition to notifying the caller that if it was successful or timed out
	 */
	public boolean waitForPostamble(int start, int stop) {
		try {
			//Get start time so a timeout can be used in subsequent while loop
			long startTime = System.currentTimeMillis();
			//Create and set flag so in the event of a timeout, an accurate error message can be displayed
			boolean postambleReceived = false;
			//While the loop has been executing for less than 60s
			//TODO: This timeout will not work if it is in the internal for loop. Add timeout to for loop if necessary
			while (((System.currentTimeMillis() - startTime) < 60000)) {
				//Executes if there is data in the input stream's buffer
				if (inputStream.ready()) {
					int temp;
					//Iterates until the specified postamble is received
					//TODO: Add timeout to this loop
					for(int counter = start; counter >= stop;) {
						//Store newly read byte in the temp variable (Must mod by 256 to get single byte due to quirks in BufferedReader class)
						temp = inputStream.read() % 256;

						//Executes of the byte received is equal to the current value of counter
						if (temp == counter) {    
							//Decrement counter by 1
							counter--;
						} 

						//Executes if the counter != temp
						else {
							//Reset the counter
							counter = start;
						}
					}
					//Set the postamble flag to true so the the program knows that a timeout didn't occur to break the loop
					postambleReceived = true;
					//Break the while loop
					break;
				}
			}

			//Executes if a timeout occurred
			if (!postambleReceived) {
				//Notify the user then exit the method
				generalStatusLabel.setText("Module Unresponsive (Timeout), Try Again");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(255, 0, 0));
				return false;
			}
		}
		//Executes if there is an error sending a command to the dongle
		catch(IOException e) {
			//Notify the user then exit the method
			generalStatusLabel.setText("Error Commicating with Dongle");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255, 0, 0));
			return false;
		}
		//Return true to exit the method and notify the caller that the method was successful
		return true;
	}

	/**
	 * Configures the serial port and input/output streams for the handshake sequences (most important parameter is the baud rate)
	 * @return boolean that allows for easy exiting of the method in addition to notifying the caller that if it was successful
	 */
	public boolean configureForHandshake() {
		//Close the current serial port if it is open (Must be done for dashboard to work properly for some reason, do not delete)
		//TODO: Add if to check if it is open to prevent exception
		serialPort.close();
		//Reopen serial port
		openSerialPort(serialPort.getName());

		//Attempts to initialize the serial port settings and the input/output streams
		try {
			//Configure the serial port for 38400 baud for low speed handshakes
			serialPort.setSerialPortParams(38400,      
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			//Assign the output stream variable to the serial port's output stream
			outputStream = serialPort.getOutputStream();
			//Assign the input stream variable to the serial port's input stream via a buffered reader so we have the option to specify the buffer size
			//TODO: Explicitly assign buffer size, defaults to 8192 bytes
			inputStream = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			dataStreamsInitialized = true;
		} 

		//Executes if the dongle is configured for an unsupported setting
		catch (UnsupportedCommOperationException e) {
			//Notify the user then exit the method
			generalStatusLabel.setText("Check Serial Dongle Compatability!");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		}

		//Executes if there is an error communicating with the dongle
		catch (IOException e) {
			//Notify the user then exit the method
			generalStatusLabel.setText("Error Communicating with Dongle");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		}
		//Return true to exit the method and notify the caller that the method was successful
		return true;
	}

	/**
	 * Configures the serial port and input/output streams for the import sequences (most important parameter is the baud rate)
	 * @return boolean that allows for easy exiting of the method in addition to notifying the caller that if it was successful
	 */
	public boolean configureForImport() {
		//Close the current serial port if it is open (Must be done for dashboard to work properly for some reason, do not delete)
		//TODO: Add if to check if it is open to prevent exception
		serialPort.close();
		//Reopen serial port
		openSerialPort(serialPort.getName());

		//Attempts to initialize the serial port settings and the input/output streams
		try {
			//Configure the serial port for 115200 baud for high speed exports
			serialPort.setSerialPortParams(115200,      
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			//Assign the output stream variable to the serial port's output stream
			outputStream = serialPort.getOutputStream();
			//Assign the input stream variable to the serial port's input stream via a buffered reader so we have the option to specify the buffer size
			//TODO: Explicitly assign buffer size, defaults to 8192 bytes
			inputStream = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			dataStreamsInitialized = true;
		} 

		//Executes if the dongle is configured for an unsupported setting
		catch (UnsupportedCommOperationException e) {
			//Notify the user then exit the method
			generalStatusLabel.setText("Check Serial Dongle Compatability!");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		}

		//Executes if there is an error communicating with the dongle
		catch (IOException e) {
			//Notify the user then exit the method
			generalStatusLabel.setText("Error Communicating with Dongle");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		}
		//Return true to exit the method and notify the caller that the method was successful
		return true;

	}

	/**
	 * Handles the handshakes that tell the module to enter a mode specified by the passed in modeDelimiter character. ex) 'E' for export data (must be identified in the firmware as well).
	 * This method attempts several times before giving up and notifying the user that there is an error in the communication
	 * @param modeDelimiter The mode identifier that is specified in the firmware ex) 'E' for export data
	 * @return boolean that allows for easy exiting of the method if the method is successful or fails
	 */
	public boolean selectMode(char modeDelimiter) {
		try {
			
			//Attempt to configure the serial dongle for handshake mode, exit if it fails to do so
			if(!configureForHandshake()) {
				return false;
			}

			//Clear the input stream so any previously unread data in the buffer isn't interpreted as an echo from the module
			//TODO: Redesign program flow so this is not necessary
			clearInputStream();

			//Initialize attempt counter to 0
			int attemptCounter = 0;
			
			//Initialized flag to false so a timeout or attempt counter exit can be determined after while loop
			boolean modeSelected = false;
			
			//Executes while the mode has not been successfully selected. This loop can be exited by any internal timeouts, or attempt limits as well
			while(!modeSelected) {
				//Start condition followed by 'S' to tell firmware to start saving new parameters
				outputStream.write(new String("1111" + modeDelimiter).getBytes());

				//Initialize temp to value that is impossible to be read from the input stream so for debug
				int temp = -1;
				//Initialize start time so a timeout can be used
				long startTime = System.currentTimeMillis();
				
				//Loops until a timeout occurs or a byte is received and read from the input stream buffer
				while((System.currentTimeMillis() - startTime) < 50) {
					//Executes if data is in the input stream buffer
					if (inputStream.ready()) {
						//Assign the newly received byte to a temp variable then break the while loop
						temp = inputStream.read() % 256;
						break;
					}	
				}
				
				//Executes if the byte just read is the expected echo value
				if (temp == (int)modeDelimiter) {
					//Set flag so while loop knows the mode has been selected
					//TODO: Delete, no longer used
					modeSelected = true;
					//Exit method
					return true;
				}
				//Executes if '?' or unexpected character is received
				else {
					//Increment atttempt counter by 1
					attemptCounter++;
				}

				//Executes if there have already been 10 attempts. Assumes there is an error in the hardware, firmware, or connection
				if (attemptCounter == 10) {

					//'?' means the command is not recognized by the module meaning it is most likely not defined in the firmware
					if (temp == '?') {
						generalStatusLabel.setText("Command Not Recognized by Module, Check Firmware Version");

					}
					//Executes if a timeout occured and the temp variable was never overridden by data
					else if (temp == -1) {
						generalStatusLabel.setText("Module Unresponsive or Connected Improperly (Timeout)");
					}
					
					//Executes if the character received is completely unexpecting hinting towards a configuration or connection error (usually baud rate mismatch)
					else {
						generalStatusLabel.setText("Communication Error, Try Again");
					}
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255,0,0));
					//Exit method, communication failed
					return false;
				}
			}

		}
		
		//Executes if there was an error communicating with the dongle
		catch (IOException e) {                                       
			//Notify user then exit method
			generalStatusLabel.setText("Error Communicating with Dongle");    
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		} 
		//Executes if the port is not opened properly already
		catch (NullPointerException e) {   
			//Notify user then exit method
			generalStatusLabel.setText("Please Select a Port");  
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			//Exit method, communication failed
			return false;
		}
		//Return true to exit the method and notify the caller that the method was successful
		return true;
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
				//sectorEraseButton.setEnabled(false);
				
				//Bulk erase the module, will stay in this method until bulk erase completes
				bulkEraseModule();
				
				//Enable buttons that can now be used since the bulk erase completed
				bulkEraseButton.setEnabled(true);
				//sectorEraseButton.setEnabled(true);
			}
		};
		
		//Define a new thread to run the operation previously defined
		bulkEraseThread = new Thread(bulkEraseOperation);
		//Start the thread
		bulkEraseThread.start();
	}

	/**
	 * Sets the module into bulk erase mode and waits until it receives a 'Bulk erase complete handshake'.
	 * Since this method is called in a thread, the thread will terminate automatically when this method is completed
	 * @return boolean that allows easy exiting of the method. Since this is called in a thread, the return statement will automatically kill the thread on completion
	 */
	public boolean bulkEraseModule() {
		
		//Notify the user that the bulk erase sequence has began
		generalStatusLabel.setText("Bulk Erasing...");
		progressBar.setValue(0);
		progressBar.setForeground(new Color(51, 204, 51));
		
		//Put the module in bulk erase mode, exit if that routine fails
		if(!selectMode('B')) {
			return false;
		}
		
		//Wait for '4321' (in binary, not ascii) as a handshake so the dashboard knows the erase has completed
		waitForPostamble(4 , 1);
		
		//Notify the user that the sequence has completed
		generalStatusLabel.setText("Bulk Erase Complete");
		progressBar.setValue(100);
		progressBar.setForeground(new Color(51, 204, 51));
		
		//Return true to exit the method and notify the caller that the method was successful
		return true;

	}

	/**
	 * Handles the button press of the Exit UART Mode Button. This is an action event which must handled before the rest of the program resumes. 
	 * This method sets the necessary flags on the module so it will only listen to the PB or the Remote so it doesn't hang up on the serin2 listener (Temporary fix until pullup is added)
	 */
	//TODO: Add pullup or come up with more intuitive solution so this is not needed
	public boolean sendExitCommand() {
		
		//Tell the module to exit UART listening mode so it will only listen to the PB or Remote, exit if this routine fails
		if(!selectMode('Q')) {
			return false;
		}

		//Notify the user that the 
		generalStatusLabel.setText("UART Mode Exited; Hold PB or 'B' Button until LED is GRN+BLUE to re-enter UART Mode");
		return true;
	}

	/**
	 * Handles the button press of the sector erase button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume.
	 */
	public void sectorEraseModule() {

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
				
				//Get module info
				getModuleInfo();
				
				//Re-enable button since previous method call was complete
				getModuleIDButton.setEnabled(true);
			}
		};
		
		//Assign new thread to run the previously defined operation
		infoThread = new Thread(getIDInfoOperation);
		//Start separate thread
		infoThread.start();
	}

	/**
	 * Obtains identification info from the module and updates the text on the dashboard. This method can also be used as a non intrusive way of calibrating baud rate without the risk
	 * of corrupting test parameters or testing data. Since this method is called in a thread, the thread will terminate automatically when this method is completed
	 * @return boolean that allows easy exiting of the method. Since this is called in a thread, the return statement will automatically kill the thread on completion
	 */
	public boolean getModuleInfo() {


		//Configure Baud Rate for 38400 temporarily for handshakes
		configureForHandshake();

		//Executes if the data streams have already been initialized
		if (dataStreamsInitialized) {

			//Put module in 'send ID info' mode, exit method if that routine fails
			if(!selectMode('I')) {
				return false;
			}

			try {
				//Initialize flag and temp storage variable
				boolean infoReceived = false;
				int temp;
				
				//Initialize ID index to 0
				int idCounter = 0;
				//Initialize temporary ID parameter array
				//TODO: Define size based on easy to find constant
				int [] moduleInfo = new int[3];
				
				//Initialize start time so timeout can be used on subsequent while loop
				long startTime = System.currentTimeMillis();
				
				//Executes while it is still receiving ID info and a timeout has not occured
				while (idCounter < 3 && (System.currentTimeMillis() - startTime) < 1500) {

					//Wait for a preamble, exits method if the preamble times out
					if(!waitForPreamble(1,4)) {
						return false;
					}
					
					//Executes if data has been received from the module
					if (inputStream.ready()) {
						//Store 2 received bytes in MSB order and form into a word
						temp = inputStream.read() * 256 + inputStream.read() % 256;
						//Echo the value back
						outputStream.write(temp / 256);
						outputStream.write(temp % 256);
						
						//Initialize start time so timeout can be used on subsequent while loop
						long echoStart = System.currentTimeMillis();
						
						//Executes while the timeout has not occurred
						while (((System.currentTimeMillis() - echoStart) < 200)) {
							
							//TODO: Restructure this loop so it is more reliable, as if now, if the dashboard reads too fast, it may see the 'C', but not the 'A'
							//Executes if data has been received from the module
							if (inputStream.ready()) {
								
								//Reset timeout counter
								startTime = System.currentTimeMillis();
								
								//Executes if the the module echoed a 'C' which is the preamble for an acknowledge
								if (inputStream.read() % 256 == 'C') {
									
									//Executes if the next character is a 'A' for acknowledge
									if (inputStream.read() == 'A') {
										//Store the confirmed value
										moduleInfo[idCounter] = temp;
										//Increment the ID index so the next ID parameter is stored
										idCounter++;
									}
								}
							}
							//If the 3rd parameter has been read, set success flag and exit loop
							if (idCounter == 3) {
								infoReceived = true;
								break;
							}
						}
					}

				}

				//Executes if a timeout has occurred
				if (!infoReceived) {
					//Notify the user then exit
					generalStatusLabel.setText("Module Unresponsive (Timeout), Try Again");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
					return false;
				}
				//Executes if the module ID was successfully received
				else {
					//Update the ID text labels and notify the user that the routine was successful
					moduleSerialNumberLabel.setText("Module Serial Number: " + moduleInfo[0]);
					hardwareIDLabel.setText("Module Hardware ID: " + moduleInfo[1] + "x");
					firmwareIDLabel.setText("Module Firmware ID: " + moduleInfo[2]);
					generalStatusLabel.setText("Module Information Successfully Received");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(51, 204, 51));
				}
				//Exit the program
				return true;
			}
			
			//Executes if there is an error communicating with the dongle
			catch (IOException e) {
				generalStatusLabel.setText("Error Communicating with Dongle");
			}
		}
		//Exit the method, routine failed
		return false;
	}


	/**
	 * Handles the button press of the write configuration button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume. See the method calls within the runnable for more info
	 * on what this handler actually does.
	 */
	private void writeButtonHandler() {
		
		//TODO: Remove, unused
		stopAllThreads();
		
		//Define no operation that can be run in a thread
		Runnable sendParamOperation = new Runnable() {
			public void run() {
				//Disable write config button while the sendParameters() method is running
				writeConfigsButton.setEnabled(false);
				
				//Send new test parameters (with built in handshakes)
				sendParameters();
				
				//Re-enable the write config button when the routine has completed
				writeConfigsButton.setEnabled(true);
			}
		};

		//Assign new operation to a thread so that it can be run in the background
		paramThread = new Thread(sendParamOperation);
		//Start the new thread
		paramThread.start();

	}

	/**
	 * This method handles the transmission of test parameters to the module with build in handshakes to verify each parameter is correctly received. 
	 * Since this method is called in a thread, the thread will terminate automatically when this method is completed
	 * @return boolean that allows easy exiting of the method. Since this is called in a thread, the return statement will automatically kill the thread on completion
	 */
	//TODO: Remove aborts, this is a fairly short routine
	private boolean sendParameters() {
		//Configure the serial port for handshake   
		configureForHandshake();
		
		//Reset progress bar
		progressBar.setValue(0);

		try {
			//Executes if the data streams have been initialized and the thread has not been told to abort
			if (dataStreamsInitialized && !paramAbort) {
				
				//Put the module in save new test parameter mode, exit if that routine fails
				if(!selectMode('P')) {
					return false;
				}
				
				//Set local flag if the timed test checkbox is checked
				if (timedTestCheckbox.isSelected()) {
					timedTestFlag = 1;
				}
				else {
					timedTestFlag = 0;
				}

				//Define new array to store the test parameters so they can be quickly iterated through while sending the data
				int[] writeData = new int[NUM_TEST_PARAMETERS];
				
				//Assign each parameter to an index of the array
				writeData[0] = 0;			//Serial Number
				writeData[1] = 5;			//Module ID (Hardware Version)
				writeData[2] = 17;			//Firmware ID 
				writeData[3] = getTickThreshold(Integer.parseInt(accelGyroSampleRateTextField.getText()));		//Timer0 Tick Threshold (Interrupt)
				writeData[4] = 0;			//Delay After Start
				writeData[5] = timedTestFlag;			//Timed Test Flag
				writeData[6] = Integer.parseInt(testLengthTextField.getText());     //Test Duration
				writeData[7] = Integer.parseInt(accelGyroSampleRateTextField.getText());//Accel Gyro Sample Rate
				writeData[8] = Integer.parseInt(magSampleRateTextField.getText());    //Mag Sample Rate
				writeData[9] = Integer.parseInt(accelSensitivityCombobox.getSelectedItem().toString());  //Accel Sensitivity
				writeData[10] = Integer.parseInt(gyroSensitivityCombobox.getSelectedItem().toString());   //Gyro Sensitivity
				writeData[11] = Integer.parseInt(accelFilterCombobox.getSelectedItem().toString());  //Accel Filter
				writeData[12] = Integer.parseInt(gyroFilterCombobox.getSelectedItem().toString());  //Gyro Filter

				//Iterates through each parameter in the array
				for (int paramNum = 0; paramNum < writeData.length; paramNum++) {
					
					//Local boolean that is reset for each parameter
					boolean paramReceived = false;
					
					//Update progress bar (somewhat arbitrary)
					progressBar.setValue((int)(100 * ((double)paramNum / (double)writeData.length) / 1.2));
					//Reset attempt counter
					int attemptCounter = 0;
					//Loops until a parameter is successfully received by module 
					while (!paramReceived) {

						//Send Preamble
						outputStream.write(new String("1234").getBytes());

						//Send parameter in binary (not ASCII) MSB first
						outputStream.write(writeData[paramNum] / 256);
						outputStream.write(writeData[paramNum] % 256);


						int temp = 0;
						boolean paramEchoed = false;
						while(!paramEchoed) {

							//Executes if the data was received back from the module
							if (inputStream.ready()) {
								//Store the echoed number in a temporary variable
								temp = inputStream.read() % 256 * 256 + inputStream.read() % 256; 
								//Set a flag to break the loop
								paramEchoed = true;
								//TODO: break; instead of paramEchoed?
							}	
						}

						//If module echoed correctly, send 'CA' for Acknowledge, (C is preamble for acknowledge cycle)
						if (temp == writeData[paramNum]) {
							outputStream.write(new String("CA").getBytes());
							paramReceived = true;
							//Reset attempt counter
							attemptCounter = 0;
						}
						//If module echoed incorrectly, send 'CN' for Not-Acknowledge, (C is preamble for acknowledge cycle)
						else {
							outputStream.write(new String("CN").getBytes());
							//Increment attempt counter
							attemptCounter++;
						}

						//Executes after 3 failed attempts
						if (attemptCounter == 3) {
							//Notify the user
							generalStatusLabel.setText("Module not Echoing Properly, Check Connections");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(255,0,0));
							//Exit method, communication failed
							return false;
						}
						//Executes if the thread was told to abort externally
						if (paramAbort) {
							return false;
						}
					}
				}

			}
			
			//Executes if the thread was told to abort externally
			else if (paramAbort){
				generalStatusLabel.setText("Write Aborted");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(255,0,0));
				//Exit method, communication failed
				return false;
			}
			
			//Executes if the port has not been opened yet
			else {
				generalStatusLabel.setText("Data Not Sent, No Port Selected");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(255,0,0));
				//Exit method, communication failed
				return false;
			}
		}
		//Executes if ther is an error communicating with the dongle
		catch (IOException e) {                                         
			generalStatusLabel.setText("Data Not Sent, Error Communicating with Dongle");    
			Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, e);
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			//Exit method, communication failed
			return false;
		} 
		//Executes if the port has not been opened yet
		catch (NullPointerException e) {                                 
			generalStatusLabel.setText("Data Not Sent, No Port Selected");  
			Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, e);
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			//Exit method, communication failed
			return false;
		}
		//Notify the routine has been successful then exit the method
		generalStatusLabel.setText("Module Configuration Successful, Parameters Have Been Updated");
		progressBar.setForeground(new Color(51, 204, 51));
		progressBar.setValue(100);
		return true;
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
				
				//Read data from test data module
				readTestData();
				
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
	 * Tells the module to export it's test data and stores the info in a temporary buffer before calling the external organizer class to format the data into a .CSV
	 * Since this method is called in a thread, the thread will terminate automatically when this method is completed
	 * @return boolean that allows easy exiting of the method. Since this is called in a thread, the return statement will automatically kill the thread on completion
	 */
	//TODO: Remove aborts or redesign them
	//TODO: Add handshakes between each few sectors of data to ensure the buffer does not overflow
	public boolean readTestData() {  
		
		//Put module into export test data mode, exit method if that routine fails
		if(!selectMode('E')) {
			return false;
		}
		
		//Executes if the data streams are initialized and the program was not aborted externally
		if (dataStreamsInitialized && !readAbort) {
			try {
				

				//Notify the user that it is waiting reading data from module
				generalStatusLabel.setText("Reading Data From Module");
				
				//Configure serial port for data imports
				configureForImport();

				
				boolean dataReceived = false;
				
				//Initialize start time so timeout can be used
				long importStartTime = System.currentTimeMillis();
				
				//Loops until internally exited with break or timeout occurs
				while ((System.currentTimeMillis() - importStartTime) < 15000) {
					
					//Executes if data is received from module
					if (inputStream.ready()) {     
						
						//Reset progress bar
						progressBar.setValue(0);                         

						//Initialize arraylists to store test params and test data
						ArrayList<Integer> testParameters = new ArrayList<Integer>();     
						ArrayList<Integer> testData = new ArrayList<Integer>();

						//Check for test parameter preamble
						waitForPreamble(1,4);

						//Determine number of tests to expect/ get test parameters
						expectedTestNum = -1;
						while(expectedTestNum == -1) {
							if(inputStream.ready()) {
								expectedTestNum = inputStream.read() % 256;
							}

						}

						//Notify the user the number of tests that are being imported
						generalStatusLabel.setText("Importing and Converting Data for " + expectedTestNum + " Test(s)");
						
						//Reset param index
						int paramNum = 0;
						
						//Executes while it is still receiving test parameters
						//TODO: add handshakes/ timeout
						while (paramNum < NUM_TEST_PARAMETERS) {
							if (inputStream.ready()) {
								//Store newly received test parameter in arraylist at index specified by paramNum
								testParameters.add(paramNum, (int) ((inputStream.read() % 256 * 256) + (inputStream.read() % 256)));
								paramNum++;
							}
							
							//Executes if the method was externally aborted
							if (readAbort) {
								return false;
							}
						}


						//Assign local variables to their newly received values from the module
						timedTestFlag = testParameters.get(5);
						testLength = testParameters.get(6);
						accelGyroSampleRate = testParameters.get(7);
						magSampleRate = testParameters.get(8);
						accelSensitivity = testParameters.get(9);
						gyroSensitivity = testParameters.get(10);
						accelFilter = testParameters.get(11);
						gyroFilter = testParameters.get(12);					


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

						//Get date for file name
						Date date = new Date();
						
						//Assign file name
						nameOfFile = "";
						nameOfFile += (" " + accelGyroSampleRate + "-" + magSampleRate + " " + accelSensitivity + "G-" + accelFilter + " " + gyroSensitivity + "dps-" + gyroFilter + " MAG-N " + date.getDate() + getMonth(date.getMonth()) + (date.getYear() - 100));
						fileNameTextField.setText(nameOfFile);


						

						//Tracks which test is currently being imported
						int testNum = 1;    
                           
						//Loops until it all of the tests are collected
						while (testNum <= expectedTestNum) {
							//Update progress bar based on which test is currently being received
							//TODO: Code this better so it isn't just 80%
							progressBar.setValue((int) ((100 *((double) testNum / (double) expectedTestNum))/ 1.25));      
							int temp = 0;
							
							//reset temporary arraylist for storing test data
							testData = new ArrayList<Integer>();
							
							//Wait for start condition (preamble)
							waitForPreamble(1,8);
							boolean stopCondition = false;
							
							//Executes while the stop condition has not been received (Main loop that actually stores testing data)
							while (!stopCondition) {    
								
								//Looks for stop condition (87654321)
								for(int counter = 8; counter >= 1;) {
									if (inputStream.ready()) {
										//Store newly read byte in temp variable
										temp = inputStream.read() % 256;
										//Add newly read byte to test data arraylist
										testData.add(temp);
										
										//Executes if the temp == the counter (meaning this byte could possibly be the stop condition)
										if (temp == counter) {  
											counter--;
										} 
										
										else {
											//Reset stop condition counter
											counter = 8;
										}
									}

								}
								
								//Executes if there were atleast 8 data points received (used to prevent range bounds error)
								if (testData.size() >= 8) {    
									
									//Set the first value in the stop condition to -1 so the next section of this method knows where the end of testing data is
									testData.set(testData.size() - 8, -1);
									stopCondition = true;                    
									generalStatusLabel.setText("Found the Stop Condition For Test " + testNum + ".");    
								}
								
								//Executes if the thread was aborted externally
								if (readAbort) {
									return false;
								} 
							}

							//Executes if the stop condition was found
							//TODO: Remove, redundant statement
							if(stopCondition) {
								testData.set(testData.size() - 8, -1);
							}
							
							//Make new array with the size of the test data arraylist, must be final to be used in parameter in thread (see organizer thread below)
							int[] finalData = new int[testData.size()];                 
							
							int j = 0;
							//Loops until the -1 is found (previously inserted to delimit the stop condition
							while(testData.get(j) != -1) {
								
								//Copy data to new array
								finalData[j] = testData.get(j);
								j++;

								//Executes if thread is externally aborted
								if (readAbort) {
									return false;
								}
							}
							
							//Set the last value of the array as -1 so organizer knows that this is the end of test
							//TODO: Identify if this can be removed
							finalData[j] = -1;


							//Set name of file string based on prefixes and suffixes
							nameOfFile = prefixTextField.getText() + " (#" + (testNum) + ") " + fileNameTextField.getText() + " " + suffixTextField.getText() + ".CSV";  //Add a number and .CSV to the file name
							
							//Assign test variables to temporary final variables so they can be used as parameters in the organizer thread below
							final int testID = testNum;		
							final int numTests = expectedTestNum;
							
							//Define new operation that can be run in a thread
							Runnable organizerOperation = new Runnable() {
								public void run() {
									
									//Organize data into .CSV
									//TODO: Refactor and eliminate unused parameters
									organizer.sortData(finalData, testID, numTests, nameOfFile, (accelGyroSampleRate / magSampleRate), (1 / accelGyroSampleRate), false, false, fileOutputDirectoryStr);  //create the .CSV with neccessary parameters
								}
							};
							
							//Define new thread to run predefined operation
							Thread organizerThread = new Thread(organizerOperation);
							//Start new thread
							organizerThread.start();      
							//Increment test index
							testNum++;             
						}
					}
				}
				
				//Executes if the test data was successfully received
				//TODO: Refactor to make sense, the data received flag must be set to true somewhere
				if(!dataReceived) {
					//Notify the user that the data transfer is complete then exit method
					generalStatusLabel.setText("Data Transfer Complete");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(51, 204, 51));
					return true;
				}
				//Executes if a timeout occurs
				//TODO: Not operational, make this work
				else {
					//Notify the user then exit
					generalStatusLabel.setText("Timeout");
					return false;
				}

			}
			//Executes if there is an error talking to the serial dongle
			catch (IOException e){
				generalStatusLabel.setText("Comm Port Error! Try Again");
				return false;
			}
			//Executes if there is an issue with the purejavacomm library??? Not sure what causes this one to occur
			//TODO: Figure out what causes this exception to occur
			catch (PureJavaIllegalStateException e) {
				generalStatusLabel.setText("Error, Try Again");
				return false;
			}
		}
		return false;

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
	 * Getter for organizer class to see if the organize thread should be aborted
	 * @return
	 */
	//TODO: Fix or delete, not currently used
	public boolean getOrganizeAbortFlag() {
		return organizeAbort;
	}

	/**
	 * Get the desired tick threshold for the desired sample rate. This effectively sets the sample rate of the module
	 * @param accelGyroSampleRate
	 * @return
	 */
	public int getTickThreshold(int accelGyroSampleRate) {
		switch (accelGyroSampleRate) {
		case(60):			//60Hz
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
			return 3689;
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
	 * Getter that allows other classes to see if the serial port is currently initialized
	 * @return
	 */
	public boolean getPortInitializedFlag() {
		return portInitialized;
	}

	/**
	 * Setter that allows external classes to tell this class that the serial port has been initialized
	 * @param flag Boolean that specifies if the port is initialized or not
	 */
	public void setPortInitializedFlag(boolean flag) {
		portInitialized = flag;
	}

	/**
	 * getter that allows external classes to see what mode the dashboard is currently in
	 * @return
	 */
	//TODO: Fix or remove (not sure if it is currently used or necessary)
	public boolean getReadModeFlag() {
		return readMode;
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
				findPorts();
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
				closeSerialPort();
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
		delayAfterStartCheckbox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(delayAfterStartCheckbox);

		delayAfterStartCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateDataFields();
			}
		});

		manualCalibrationCheckbox = new JCheckBox("Manual Calibration");
		manualCalibrationCheckbox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(manualCalibrationCheckbox);

		manualCalibrationCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateDataFields();
			}
		});

		getCurrentConfigsButton = new JButton("Get Current Configurations");
		getCurrentConfigsButton.setEnabled(false);
		getCurrentConfigsButton.setBorder(null);
		getCurrentConfigsButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(getCurrentConfigsButton);

		accelGyroSampleRateTextField = new JTextField();
		accelGyroSampleRateTextField.setText("960");
		accelGyroSampleRateTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelGyroSampleRateTextField.setColumns(10);
		accelGyroSampleRateTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accel/Gyro Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(accelGyroSampleRateTextField);

		magSampleRateTextField = new JTextField();
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
		testLengthTextField.setText("25");
		testLengthTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		testLengthTextField.setColumns(10);
		testLengthTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Test Duration (Seconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(testLengthTextField);

		writeConfigsButton = new JButton("Write Configurations");
		writeConfigsButton.setEnabled(false);
		writeConfigsButton.setBorder(null);
		writeConfigsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				writeButtonHandler();
			}
		});
		writeConfigsButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(writeConfigsButton);

		erasePanel = new JPanel();
		mainTabbedPanel.addTab("Erase", null, erasePanel, null);
		erasePanel.setLayout(new GridLayout(1, 0, 0, 0));

		panel = new JPanel();
		erasePanel.add(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));

		bulkEraseButton = new JButton("Bulk Erase");
		bulkEraseButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel.add(bulkEraseButton);
		bulkEraseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bulkEraseHandler();
			}
		});

		sectorEraseButton = new JButton("Sector Erase");
		sectorEraseButton.setEnabled(false);
		sectorEraseButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel.add(sectorEraseButton);
		sectorEraseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sectorEraseModule();
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

		templateSourceFileList = new JComboBox();
		templateSourceFileList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				templateChosen = templateSourceFileList.getSelectedItem().toString();
			}
		});
		templateSourceFileList.setToolTipText("Template XLSX");
		File[] templateFileList = new File(settings.getKeyVal("TemplateDirectory")).listFiles();
		ArrayList<String> templateList = new ArrayList<String>();
		if(templateFileList!=null) {
			for(int i=0; i<templateFileList.length;i++) {
				templateList.add(templateFileList[i].toString().substring(templateFileList[i].toString().lastIndexOf("\\")+1, templateFileList[i].toString().length()));
			}
			templateSourceFileList.setModel(new DefaultComboBoxModel(templateList.toArray()));
		}
		templateTools.add(templateSourceFileList);

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

		JPanel progressPanel = new JPanel();
		contentPanel.add(progressPanel);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(500, 20));
		progressPanel.add(progressBar);

		sendQuitCMDButton = new JButton("Exit UART Mode");
		sendQuitCMDButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendExitCommand();
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
