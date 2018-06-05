/*************************************************
Title: DataOrganizerRev-20
Date of Last Edit: 5/24/2018
Author: Andrew McEntee and Brandon Fanti
Last Edited by: Andrew McEntee and Brandon Fanti
 ************************************************** */
package dataorganizer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;


import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;
import java.awt.Dimension;
import java.awt.ComponentOrientation;
import java.awt.Component;


public class Frame extends JFrame {
	public static final int NUM_TEST_PARAMETERS = 13;

	//Test Parameters (All must be of type "int")
	private int timedTestFlag = 1;
	private int testLength = 25;      			
	private int accelGyroSampleRate;    		
	private int magSampleRate;          			
	private int accelSensitivity;       		
	private int gyroSensitivity;        		
	private int accelFilter;            			
	private int gyroFilter;             		
	
	//Flags
	private boolean readMode = true;    			//Flag that tracks if the program is in read or write mode
	private boolean portInitialized = false;
	private boolean portOpened = false;
	private boolean dataStreamsInitialized = false;
	
	//Output File Info and Variables
	private String nameOfFile = "";     			//Sets the name of file to an empty string to start
	private String fileOutputDirectoryStr;			//The directory to write the test to
	private int expectedTestNum = 0;                //The number of test that are expected to be received 
	private Organizer organizer = new Organizer();  //Object used for creating .CSV files
	
	//Serial Port Variables
	private SerialPort serialPort;      			//Object for the serial port class
	private static CommPortIdentifier portId;       //Object used for opening a comm ports
	private static Enumeration portList;            //Object used for finding comm ports
	private InputStream inputStream;                //Object used for reading serial data 
	private OutputStream outputStream;              //Object used for writing serial data
	
	public static Frame frameInstance;


	private Frame() {
		setPreferredSize(new Dimension(490, 758));            //Constructor of Class; adds components to GUI and sets the behavior of the frame on which components are placed
		setTitle("Data Organizer Rev-20 (5/24/2018)");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initComponents();
		setVisible(true);
	}

	public static synchronized Frame getFrameInstance() {
		if (frameInstance == null) {
			frameInstance = new Frame();
		}
		return frameInstance;
	}



	public static void main(String args[]) {
		Frame frame = Frame.getFrameInstance();
		frame.findPorts();
		while(true) {
			
		}
	}

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
			commPortsComboBox.setModel(new DefaultComboBoxModel(portNames.toArray()));
		}
		else {
			mainStatusLabel.setText("No Serial Dongle Found");
		}
	}

	public void openSerialPort(String commPortID) {                     //Method that creates the serial port
		portList = CommPortIdentifier.getPortIdentifiers();                     //creates list of avaiable com ports
		while (portList.hasMoreElements()) {                                    //Loops through the com ports
			CommPortIdentifier tempPortId = (CommPortIdentifier) portList.nextElement();
			if (tempPortId.getName().equals(commPortID)) {                             //If the avaliable Comm Port equals the comm port that aws selected earlier
				portId = tempPortId;
				break;
			}
		}
		try {
			serialPort = (SerialPort) portId.open("portHandler", 2000);
			portOpened = true;
		} 
		catch (PortInUseException e) {
			commPortsBtn.setLabel("Dongle Already In Use");
			commPortsBtn.setBackground(new Color(255, 30, 20));
		}
		if (portOpened) {
			try {
				inputStream = serialPort.getInputStream();              //creates input stream
				outputStream = serialPort.getOutputStream();            //creates output stream
				dataStreamsInitialized = true;
			} 
			catch (IOException e) {
				commPortsBtn.setLabel("Error Communicating with Serial Dongle");
				commPortsBtn.setBackground(new Color(255, 30, 20));
			}

			try {
				if (readMode) {                                 //If read mode is enabled
					serialPort.setSerialPortParams(115200,      //Opens the serial port at 115200 Baud for high speed reading
							SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
				} 
				else {                                        //If write mode is enable
					serialPort.setSerialPortParams(38400,       //Opens serial port at 9600 Baud so the URI module can read the data
							SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
				}
			} 
			catch (UnsupportedCommOperationException e) {
				commPortsBtn.setLabel("Check Serial Dongle Compatability!");
				commPortsBtn.setBackground(new Color(255, 30, 20));
			}

		}
	}


	private void initSerialPort() {
		
		if (commPortsComboBox.getSelectedItem() != null) {

			String selectedCommID = commPortsComboBox.getSelectedItem().toString();      //creates a string of selected item; Name of the comm port as a string

			openSerialPort(selectedCommID);                                        //opens the serial port with the selected comm Port

			commPortsBtn.setLabel("Port Opened Successfully");      //sets the Button Label to notify the user that the port is open
			commPortsBtn.setBackground(new Color(124, 252, 0));
			serialToggleBtn.setEnabled(false);                      //Turns the changing mode button off because the mode cannot be changed after a serial port is open

			if (readMode) {                                         //If the program is set to read mode
				mainStatusLabel.setForeground(new Color(0, 0, 0));  //sets the font as black, it might gave been set as red from before
				mainStatusLabel.setText("Waiting for Data");                //Read mode is on, so the program is waiting for data
				Runnable readOperation = new Runnable() {
					public void run() {
						dataListener();
					}
				};
				Thread readThread = new Thread(readOperation);
				readThread.start();
			} 
			else {
				mainStatusLabel.setForeground(new Color(0, 0, 0));     //sets the font as black, it might gave been set as red from before 
				mainStatusLabel.setText("Press Button Below to Write Data");   //write mode is on, so the program is waiting to send data
			}
			portInitialized = true;
		}

	}


	private void closeSerialPort() {
		//If the disconnect button is pressed: disconnects from the serial port and resets the UI   
		if (serialPort != null) {
			serialPort.close();     //closes the serial port
			portInitialized = false;
			commPortsBtn.setLabel("Search Available Ports!");                //resets the serial label for the serial
			commPortsBtn.setBackground(new Color(153, 153, 153));   //sets the button back to grey
			mainStatusLabel.setForeground(new Color(0, 0, 0));          //turns the font black
			mainStatusLabel.setText("Port Closed");                              //says the port closed
			serialToggleBtn.setEnabled(true);                               //turns the button back on so the mode can be cahanged
		}
	}
	
	private void serialToggleBtnActionPerformed() {
		//Method that controls whether the program is in read or write mode
		if (readMode) { //If the program was in readMode, it will now be swapped to write mode
			readMode = !readMode;   //changes the mode    
			//Everything else is for disabling and enabling components and changing the colors
			serialToggleBtn.setLabel("Write Mode");
			serialToggleBtn.setBackground(new Color(0, 204, 0));
			writeModeStateLabel.setForeground(new Color(0, 0, 204));
			writeModeStateLabel.setText("Currently Enabled");
			readModeStateLabel.setForeground(new Color(255, 0, 0));
			readModeStateLabel.setText("Currently Disabled");
			readModeLabel.setForeground(new Color(0, 0, 0));
			writeModeLabel.setForeground(new Color(0, 153, 0));
			writeBtn.setBackground(new Color(124, 252, 0));
			timedTestCheckbox.setEnabled(true);
			accelGyroSampleField.setEditable(true);
			magSampleField.setEditable(true);
			testLengthField.setEditable(true);
			accelSensiComboBox.setEnabled(true);
			gyroSensiComboBox.setEnabled(true);
			accelFilterComboBox.setEnabled(true);
			gyroFilterComboBox.setEnabled(true);
			writeBtn.setEnabled(true);
			nameOfFileField.setEditable(false);
			timeStampCheck.setEnabled(false);
			saveOnly9AxisCheck.setEnabled(false);
			saveTestParamsCheck.setEnabled(false);
		} 
		else {    //if the program was in write mode, it will now be swapped 
			readMode = !readMode;   //swaps the mode
			//Everything else is for disabling and enabling components and changing the colors
			serialToggleBtn.setLabel("Read Mode");
			serialToggleBtn.setBackground(new Color(204, 0, 204));
			readModeStateLabel.setForeground(new Color(0, 0, 204));
			readModeStateLabel.setText("Currently Enabled");
			writeModeStateLabel.setForeground(new Color(255, 0, 0));
			writeModeStateLabel.setText("Currently Disabled");
			readModeLabel.setForeground(new Color(102, 0, 102));
			writeModeLabel.setForeground(new Color(0, 0, 0));
			writeBtn.setBackground(new Color(153, 153, 153));
			timedTestCheckbox.setEnabled(false);
			accelGyroSampleField.setEditable(false);
			magSampleField.setEditable(false);
			testLengthField.setEditable(false);
			accelSensiComboBox.setEnabled(false);
			gyroSensiComboBox.setEnabled(false);
			accelFilterComboBox.setEnabled(false);
			gyroFilterComboBox.setEnabled(false);
			writeBtn.setEnabled(false);
			nameOfFileField.setEditable(true);
			timeStampCheck.setEnabled(true);
			saveOnly9AxisCheck.setEnabled(true);
			saveTestParamsCheck.setEnabled(true);
		}
	}

	private void writeButtonHandler() {
		Runnable sendParamOperation = new Runnable() {
			public void run() {
				sendParameters();
			}
		};
		Thread paramThread = new Thread(sendParamOperation);
		paramThread.run();
	}
	private boolean sendParameters() {
		//Method for writing data to URI module. Sends programming config to URI module    

		boolean modeSelected = false;
		if (portInitialized && dataStreamsInitialized) {

			try {
				int attemptCounter = 0;
				
				while(!modeSelected) {
					//Start condition followed by 'S' to tell firmware to start saving new parameters
					outputStream.write(new String("1111S").getBytes());

					//long startTime = System.currentTimeMillis();
					int temp = -1;
					long startTime = System.currentTimeMillis();
					while(temp == -1 && (System.currentTimeMillis() - startTime) < 250) {
						if (inputStream.available() > 0) {
							temp = inputStream.read();
						}	
					}

					if (temp == 'S') {
						modeSelected = true;
					}

					//If an unknown character or '?' is received, try sending again
					else {
						attemptCounter++;
					}

					//After 3 failed attempts, show error
					if (attemptCounter == 50) {

						//Command not recognized module
						if (temp == '?') {
							mainStatusLabel.setText("Command Not Recognized by Module, Check Firmware Version");
						}

						//Timeout (not yet used)
						else if (temp == -1) {
							mainStatusLabel.setText("Module Unresponsive or Connected Improperly (Timeout)");
						}
						
						else {
							mainStatusLabel.setText("Communication Error, Try Again");
						}

						//Exit method, communication failed
						return false;
					}
				}
				
				if (timedTestCheckbox.isSelected()) {
					timedTestFlag = 1;
				}
				else {
					timedTestFlag = 0;
				}

				int[] writeData = new int[NUM_TEST_PARAMETERS];
				writeData[0] = 5;			//Module ID (Hardware Version)
				writeData[1] = 19;			//Serial Number
				writeData[2] = 15;			//Firmware ID 
				writeData[3] = getTickThreshold(Integer.parseInt(accelGyroSampleField.getText()));		//Timer0 Tick Threshold (Interrupt)
				writeData[4] = 0;			//Delay After Start
				writeData[5] = timedTestFlag;			//Timed Test Flag
				writeData[6] = Integer.parseInt(testLengthField.getText());     //Test Duration
				writeData[7] = Integer.parseInt(accelGyroSampleField.getText());//Accel Gyro Sample Rate
				writeData[8] = Integer.parseInt(magSampleField.getText());    //Mag Sample Rate
				writeData[9] = Integer.parseInt(accelSensiComboBox.getSelectedItem().toString());  //Accel Sensitivity
				writeData[10] = Integer.parseInt(gyroSensiComboBox.getSelectedItem().toString());   //Gyro Sensitivity
				writeData[11] = Integer.parseInt(accelFilterComboBox.getSelectedItem().toString());  //Accel Filter
				writeData[12] = Integer.parseInt(gyroFilterComboBox.getSelectedItem().toString());  //Gyro Filter

				for (int paramNum = 0; paramNum < writeData.length; paramNum++) {
					boolean paramReceived = false;
					attemptCounter = 0;
					while (!paramReceived) {

						//Send Preamble
						outputStream.write(new String("1234").getBytes());

						//Send parameter in binary (not ASCII) MSB first
						outputStream.write(writeData[paramNum] / 256);
						outputStream.write(writeData[paramNum] % 256);


						//long startTime = System.currentTimeMillis();
						int temp = 0;
						boolean paramEchoed = false;
						while(!paramEchoed) {

							//If the module echoed all 4 digits of the parameter, read and store them in the tempDigits array
							if (inputStream.available() >= 2) {
								temp = inputStream.read() * 256 + inputStream.read(); 
								paramEchoed = true;
							}	
						}

						//If module echoed correctly, send 'A' for Acknowledge
						if (temp == writeData[paramNum]) {
							outputStream.write(new String("CA").getBytes());
							paramReceived = true;
							attemptCounter = 0;
						}
						//If module echoed incorrectly, send 'N' for Not-Acknowledge
						else {
							outputStream.write(new String("CN").getBytes());
							attemptCounter++;
						}
						
						//After 3 failed attempts, exit and notify the user
						if (attemptCounter == 3) {
							mainStatusLabel.setText("Module not Echoing Properly, Check Connections");
							
							//Exit method, communication failed
							return false;
						}
					}
				}

			} 
			catch (IOException e) {                                          //If there is an IOException
				mainStatusLabel.setForeground(new Color(255, 0, 0));
				mainStatusLabel.setText("Data Not Sent, Error Communicating with Dongle");    //Notify the user that something broke
				Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, e);
				//Exit method, communication failed
				return false;
			} 
			catch (NullPointerException e) {                                  //If there is a NullPointer
				mainStatusLabel.setForeground(new Color(255, 0, 0));
				mainStatusLabel.setText("Data Not Sent, No Port Selected");  //The serial port was not open; notifies the user about the mistake
				Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, e);
				//Exit method, communication failed
				return false;
			}
		}
		else {
			mainStatusLabel.setText("Data Not Sent, No Port Selected");
			mainStatusLabel.setForeground(new Color(255, 0, 0));
			//Exit method, communication failed
			return false;
		}
		mainStatusLabel.setText("Module Configuration Successful, Parameters Have Been Updated");
		return true;
	}


	public void dataListener() {                                //serial Port listner. Checks for incomming data 
		while (readMode && dataStreamsInitialized) {
			try {
				if (inputStream.available() > 0) {      
					jProgressBar1.setStringPainted(true);       //Sets the progress bar up to display a percentage
					updateProgress(0);                           //sets the progress bat to 0 percent

					ArrayList<Integer> testParameters = new ArrayList<Integer>();     
					ArrayList<Integer> testData = new ArrayList<Integer>();
					//ArrayList<ArrayList<Integer>> Tests = new ArrayList<ArrayList<Integer>>();
					boolean paramCondition = false;				   //Flag that determines if the preamble for the test parameters was received
					boolean startCondition = false;                //Flag that determines if the start condition was received 
					int dataByte;                                  //reads data from the input stream and stores in momentarily. Just for comparison 

					//Check for test parameter preamble
					while (!paramCondition) {
						if (inputStream.available() > 0) {
							int temp;
							for(int counter = 1; counter < 5;) {
								temp = inputStream.read();
								//System.out.println(temp);
								if (temp == counter) {    
									counter++;
								} 
								else {
									counter = 1;
								}
							}
							paramCondition = true;
							//System.out.println(temp);
						}
					}


					//Determine number of tests to expect/ get test parameters
					expectedTestNum = (int) (inputStream.read());  
					mainStatusLabel.setText("Collecting Data for " + expectedTestNum + " Tests");//Tells the user how many tests are being transmitted
					//reads the parameters for the test that are sent from the URI module
					int paramNum = 0;
					while (paramNum < NUM_TEST_PARAMETERS) {
						if (inputStream.available() > 0) {
							testParameters.add(paramNum, (int) ((inputStream.read() * 256) + (inputStream.read())));
							paramNum++;
						}
					}
					

					//each parameter is sent as two bytes. The higher byte is multiplied by 256 and the bottom byte is added on
					timedTestFlag = testParameters.get(5);
					testLength = testParameters.get(6);
					accelGyroSampleRate = testParameters.get(7);
					magSampleRate = testParameters.get(8);
					accelSensitivity = testParameters.get(9);
					gyroSensitivity = testParameters.get(10);
					accelFilter = testParameters.get(11);
					gyroFilter = testParameters.get(12);					
					
					//Populate dashboard with the parameters sent by the module
					testLengthFieldR.setText(Integer.toString(testLength));            //Test Length
					numTestFieldR.setText(Integer.toString(expectedTestNum));
					accelGyroSampleFieldR.setText(Integer.toString(accelGyroSampleRate)); //Accel Gyro Sample Rate
					magSampleFieldR.setText(Integer.toString(magSampleRate));           //Mag Sample Rate
					accelSensiFieldR.setText(Integer.toString(accelSensitivity));       //Accel Sensitivity
					gyroSensiFieldR.setText(Integer.toString(gyroSensitivity));         //Gyro Sensitivity
					accelFilterFieldR.setText(Integer.toString(accelFilter));           //Accel Filter
					gyroFilterFieldR.setText(Integer.toString(gyroFilter));             //Gyro Filter 
					nameOfFile = nameOfFileField.getText();
					
					if (saveTestParamsCheck.getState()) {
						//Adds the parameters and date to the end of the file Name
						Date date = new Date();
						nameOfFile += (" " + accelGyroSampleRate + "-" + magSampleRate + " " + accelSensitivity + "G-" + accelFilter + " " + gyroSensitivity + "dps-" + gyroFilter + " MAG-N " + date.getDate() + getMonth(date.getMonth()) + (date.getYear() - 100));
						nameOfFileField.setText(nameOfFile);
					}
					//Loops until it all of the tests are collected

					int testNum = 1;                                     //tracks the current index the test is on

					while (testNum <= expectedTestNum) {
						int temp = 0;
						testData = new ArrayList<Integer>();
						//Start Condition test, The program is expecting to receive "1-2-3-4-5-6-7-8" as the start condition
						startCondition = false;
						while (!startCondition) {
							if (inputStream.available() > 0) {
								for(int counter = 1; counter < 9;) {
									temp = inputStream.read();
									//System.out.println(temp);
									if (temp == counter) {    
										counter++;
									} 
									else {
										counter = 1;
									}
								}
								startCondition = true;                      //start condition flag is set to true so data collection will begin
								writeStatusLabel.setText("Found the Start Condition For Test " + (testNum) + ". Now Collecting Data");    //display to the user where the program is
								System.out.println("Started " + (testNum));
							}
						}

						boolean stopCondition = false;
						while (!stopCondition) {    //read all of the data on the serial buffer and store it in the test array
							//System.out.println(inputStream.available());
							if (inputStream.available() > 0) {
								for(int counter = 8; counter > 0;) {
									temp = inputStream.read();
									//System.out.println(temp);
									testData.add(temp);
									//System.out.println(Tests.get(testNum).get(i) + ".");

									//System.out.println(temp);
									if (temp == counter) {    
										counter--;
									} 
									else {
										counter = 8;
									}

								}
								if (testData.size() >= 8) {    //if the start condition was received correctly
									testData.set(testData.size() - 8, -1);
									stopCondition = true;                      //start condition flag is set to true so data collection will begin
									writeStatusLabel.setText("Found the Stop Condition For Test " + testNum + ".");    //display to the user where the program is
									updateProgress(0);      //Update the progress bar so the last test is no longer being displayed
								}
							}
						}

						//System.out.println("EXIT");
						//ArrayList<Integer> finalData = new ArrayList<Integer>();
						int[] finalData = new int[testData.size()];                   //store all of the data from the single collected test in another array so it is final
						int j = 0;
						while(testData.get(j) != -1) {
							finalData[j] = testData.get(j);
							//System.out.println(finalData[j]);
							j++;
						}
						finalData[j] = -1;

						nameOfFile = PrefixNameOfFileField.getText() + " (#" + (testNum) + ") " + nameOfFileField.getText() + " " + SuffixNameOfFileField.getText() + ".CSV";  //Add a number and .CSV to the file name
						final int testID = testNum;		//Must be final to work in the sortData routine
						final int numTests = expectedTestNum;
						Runnable organizerOperation = new Runnable() {
							public void run() {
								organizer.sortData(finalData, testID, numTests, nameOfFile, (accelGyroSampleRate / magSampleRate), (1 / accelGyroSampleRate), timeStampCheck.getState(), saveOnly9AxisCheck.getState(), fileOutputDirectoryStr);  //create the .CSV with neccessary parameters

							}
						};
						Thread organizerThread = new Thread(organizerOperation);
						organizerThread.start();      //start the new thread

						testNum++;              //The test number is incremented to collect data for the next test
					}
				}
			}
			catch (IOException e){
				commPortsBtn.setLabel("Comm Port Error! Try Again");
				commPortsBtn.setBackground(new Color(255, 30, 20));
			}

		}
		writeStatusLabel.setText("Data Transfer Complete");
	}
	
	public void updateProgress(int progress) {   //Method that updates the progress with the percentage that has been completed so far in making the .CSV file
		jProgressBar1.setValue(progress);
	}
	
	public void setWriteStatusLabel(String label) {
		writeStatusLabel.setText(label);        //Tell the user a new .CSV has been created.
	}

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
	public String getMonth(int month) {  //Method for changing the data in int form to a string
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

	public boolean getPortInitializedFlag() {
		return portInitialized;
	}

	public void setPortInitializedFlag(boolean flag) {
		portInitialized = flag;
	}

	public boolean getReadModeFlag() {
		return readMode;
	}
	
	private void updateConfigFields() {
		
	}





	private void initComponents() {
		
		updateConfigFields();

		magSampleField = new javax.swing.JTextField();
		magSampleLabel = new javax.swing.JLabel();
		accelGyroSampleLabel = new javax.swing.JLabel();
		accelGyroSampleField = new javax.swing.JTextField();
		nameOfFileLabel = new javax.swing.JLabel();
		timeStampCheck = new java.awt.Checkbox();
		testLengthField = new javax.swing.JTextField();
		lenOfTestLabel = new javax.swing.JLabel();
		jProgressBar1 = new javax.swing.JProgressBar();
		saveOnly9AxisCheck = new java.awt.Checkbox();
		commPortsComboBox = new javax.swing.JComboBox<>();
		disconnectBtn = new java.awt.Button();
		commPortsBtn = new java.awt.Button();
		mainStatusLabel = new javax.swing.JLabel();
		writeStatusLabel = new javax.swing.JLabel();
		accelSensiLabel = new javax.swing.JLabel();
		gyroSensiLabel = new javax.swing.JLabel();
		accelFilterLabel = new javax.swing.JLabel();
		gyroFilterLabel = new javax.swing.JLabel();
		nameOfFileField = new javax.swing.JTextField();
		writeBtn = new java.awt.Button();
		saveTestParamsCheck = new java.awt.Checkbox();
		writeModeLabel = new javax.swing.JLabel();
		jSeparator1 = new javax.swing.JSeparator();
		writeModeStateLabel = new javax.swing.JLabel();
		jSeparator2 = new javax.swing.JSeparator();
		readModeLabel = new javax.swing.JLabel();
		readModeStateLabel = new javax.swing.JLabel();
		gyroSensiLabelR = new javax.swing.JLabel();
		gyroSensiFieldR = new javax.swing.JTextField();
		accelFilterLabelR = new javax.swing.JLabel();
		accelFilterFieldR = new javax.swing.JTextField();
		gyroFilterLabelR = new javax.swing.JLabel();
		gyroFilterFieldR = new javax.swing.JTextField();
		magSampleFieldR = new javax.swing.JTextField();
		magSampleLabelR = new javax.swing.JLabel();
		accelGyroSampleLabelR = new javax.swing.JLabel();
		accelGyroSampleFieldR = new javax.swing.JTextField();
		accelSensiLabelR = new javax.swing.JLabel();
		accelSensiFieldR = new javax.swing.JTextField();
		testLengthFieldR = new javax.swing.JTextField();
		lenOfTestLabelR = new javax.swing.JLabel();
		numTestsLabelR = new javax.swing.JLabel();
		numTestFieldR = new javax.swing.JTextField();
		serialToggleBtn = new java.awt.Button();
		accelSensiComboBox = new javax.swing.JComboBox<>();
		gyroSensiComboBox = new javax.swing.JComboBox<>();
		accelFilterComboBox = new javax.swing.JComboBox<>();
		gyroFilterComboBox = new javax.swing.JComboBox<>();

		magSampleField.setEditable(false);
		magSampleField.setText("120");
		magSampleField.setToolTipText("Must be equal to Accel/Gyro Sample or 1/10 of Accel/Gyro Sample " + "\n" + "NOTE: A Sample Rate over 100Hz might cause some data points to be saved multiple times");

		magSampleLabel.setText("Magnetometer Sample Rate: ");

		accelGyroSampleLabel.setText("Accel/Gyro Sample Rate:");

		accelGyroSampleField.setEditable(false);
		accelGyroSampleField.setText("120");
		accelGyroSampleField.setToolTipText("Maximum Value: 1000");

		nameOfFileLabel.setText("Name of File:");

		timeStampCheck.setLabel("Time Stamp Data");

		testLengthField.setEditable(false);
		testLengthField.setText("25");
		testLengthField.setToolTipText("Max Value: 65534");

		lenOfTestLabel.setText("Length of Test (Sec):");

		jProgressBar1.setToolTipText("Shows the progress on creating the .CSV file");

		saveOnly9AxisCheck.setLabel("Save Only 9 Axis Data");


		//Serial Port Combo Box Option Selected
		commPortsComboBox.setModel(new DefaultComboBoxModel(new String [] {"     --------------"}));
		commPortsComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				initSerialPort();
			}
		});

		//Disconnect Button
		disconnectBtn.setBackground(new Color(216, 25, 25));
		disconnectBtn.setLabel("Disconnect");
		disconnectBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				closeSerialPort();
			}
		});

		//Search Available Ports Button
		commPortsBtn.setBackground(new Color(153, 153, 153));
		commPortsBtn.setLabel("Refresh Port List");
		commPortsBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				findPorts();
			}
		});

		mainStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainStatusLabel.setText("Port Closed");

		writeStatusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

		accelSensiLabel.setText("Accel Sensitivity (G):");

		gyroSensiLabel.setText("Gyroscope Sensitivity (dps):");

		accelFilterLabel.setText("Accelerometer Filter (Hz):");

		gyroFilterLabel.setText("Gyroscope Filter (Hz):");

		nameOfFileField.setToolTipText("Do not add .CSV at the end of the file name; it is automatically added");

		writeBtn.setBackground(new Color(153, 153, 153));
		writeBtn.setEnabled(false);
		writeBtn.setLabel("Write Data");
		writeBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				writeButtonHandler();
			}
		});

		saveTestParamsCheck.setLabel("Save Test Parameters in Name of File");
		saveTestParamsCheck.setState(true);

		writeModeLabel.setFont(new Font("Tahoma", Font.PLAIN, 25)); // NOI18N
		writeModeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		writeModeLabel.setText("Write Mode");

		writeModeStateLabel.setForeground(new Color(255, 0, 0));
		writeModeStateLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		writeModeStateLabel.setText("Currently Disabled");

		readModeLabel.setFont(new Font("Tahoma", Font.PLAIN, 25)); // NOI18N
		readModeLabel.setForeground(new Color(102, 0, 102));
		readModeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		readModeLabel.setText("Read Mode");

		readModeStateLabel.setForeground(new Color(0, 0, 204));
		readModeStateLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		readModeStateLabel.setText("Currently Enabled");

		gyroSensiLabelR.setText("Gyroscope Sensitivity (dps):");

		gyroSensiFieldR.setEditable(false);

		accelFilterLabelR.setText("Accelerometer Filter (Hz):");

		accelFilterFieldR.setEditable(false);

		gyroFilterLabelR.setText("Gyroscope Filter (Hz):");

		gyroFilterFieldR.setEditable(false);

		magSampleFieldR.setEditable(false);

		magSampleLabelR.setText("Magnetometer Sample Rate: ");

		accelGyroSampleLabelR.setText("Accel/Gyro Sample Rate:");

		accelGyroSampleFieldR.setEditable(false);

		accelSensiLabelR.setText("Accel Sensitivity (G):");

		accelSensiFieldR.setEditable(false);

		testLengthFieldR.setEditable(false);

		lenOfTestLabelR.setText("Length of Test (Seconds):");

		numTestsLabelR.setText("Number of Tests:");

		numTestFieldR.setEditable(false);

		serialToggleBtn.setBackground(new Color(204, 0, 204));
		serialToggleBtn.setLabel("Read Mode");
		serialToggleBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				serialToggleBtnActionPerformed();
			}
		});

		accelSensiComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2", "4", "8", "16" }));
		accelSensiComboBox.setSelectedIndex(2);
		accelSensiComboBox.setEnabled(false);

		gyroSensiComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "250", "500", "1000", "2000" }));
		gyroSensiComboBox.setSelectedIndex(2);
		gyroSensiComboBox.setEnabled(false);

		accelFilterComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "5", "10", "20", "41", "92", "184", "460", "1130(OFF)" }));
		accelFilterComboBox.setSelectedIndex(4);
		accelFilterComboBox.setEnabled(false);

		gyroFilterComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "5", "10", "20", "41", "92", "184", "250", "3600", "8800(OFF)" }));
		gyroFilterComboBox.setSelectedIndex(4);
		gyroFilterComboBox.setEnabled(false);

		fileOutputDirectory = new JButton("Browse");
		fileOutputDirectory.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
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
		});

		PrefixNameOfFileField = new JTextField(); 
		SuffixNameOfFileField = new JTextField();
		SuffixNameOfFileField.setToolTipText("Do not add .CSV at the end of the file name; it is automatically added");

		lblFilePrefix = new JLabel("File Prefix");

		lblFileSuffix = new JLabel("File Suffix");
		
		timedTestCheckbox = new JCheckBox("Timed Test");
		timedTestCheckbox.setEnabled(false);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(
			layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addGap(23)
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addComponent(mainStatusLabel, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
						.addComponent(readModeStateLabel, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
						.addComponent(writeModeStateLabel, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
						.addComponent(timedTestCheckbox)
						.addComponent(timeStampCheck, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
						.addComponent(jProgressBar1, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
						.addComponent(writeStatusLabel, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
						.addComponent(saveOnly9AxisCheck, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(saveTestParamsCheck, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(layout.createSequentialGroup()
							.addComponent(accelGyroSampleLabel, GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(accelGyroSampleField, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(magSampleLabel, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
							.addComponent(magSampleField, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup()
							.addComponent(accelSensiLabel, GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(accelSensiComboBox, 0, 61, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(gyroSensiLabel, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(gyroSensiComboBox, 0, 64, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
							.addComponent(serialToggleBtn, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(commPortsBtn, GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(commPortsComboBox, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup()
							.addComponent(accelSensiLabelR, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(accelSensiFieldR, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(gyroSensiLabelR, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(gyroSensiFieldR, 61, 61, 61))
						.addGroup(layout.createSequentialGroup()
							.addComponent(accelFilterLabelR, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(accelFilterFieldR, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(gyroFilterLabelR, GroupLayout.PREFERRED_SIZE, 134, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(gyroFilterFieldR, 61, 61, 61))
						.addGroup(layout.createSequentialGroup()
							.addComponent(accelGyroSampleLabelR, GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(accelGyroSampleFieldR, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(magSampleLabelR, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(magSampleFieldR, GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
							.addComponent(numTestsLabelR, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(numTestFieldR, 65, 65, 65)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lenOfTestLabelR)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(testLengthFieldR, 61, 61, 61))
						.addComponent(disconnectBtn, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
							.addComponent(writeModeLabel, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup()
							.addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(readModeLabel, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(accelFilterLabel, GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
								.addComponent(lenOfTestLabel, GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(accelFilterComboBox, 0, 64, Short.MAX_VALUE)
								.addComponent(testLengthField, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(writeBtn, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
									.addComponent(gyroFilterLabel, GroupLayout.PREFERRED_SIZE, 134, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(gyroFilterComboBox, 0, 69, Short.MAX_VALUE))))
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(nameOfFileLabel)
								.addComponent(lblFilePrefix))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
									.addComponent(PrefixNameOfFileField, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
									.addGap(53)
									.addComponent(lblFileSuffix, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(SuffixNameOfFileField, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE))
								.addGroup(layout.createSequentialGroup()
									.addComponent(nameOfFileField, GroupLayout.PREFERRED_SIZE, 233, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(fileOutputDirectory, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)))))
					.addGap(23))
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING)
						.addComponent(writeBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
									.addComponent(commPortsBtn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(commPortsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addComponent(serialToggleBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(mainStatusLabel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(disconnectBtn, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
								.addComponent(writeModeLabel))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(writeModeStateLabel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(timedTestCheckbox)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(Alignment.BASELINE)
								.addComponent(magSampleLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(magSampleField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(accelGyroSampleField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(accelGyroSampleLabel, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(accelSensiLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(layout.createParallelGroup(Alignment.BASELINE)
									.addComponent(gyroSensiLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(accelSensiComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(gyroSensiComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(Alignment.BASELINE)
								.addComponent(accelFilterLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(gyroFilterLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(accelFilterComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(gyroFilterComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(4)
							.addGroup(layout.createParallelGroup(Alignment.BASELINE)
								.addComponent(testLengthField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lenOfTestLabel))))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
						.addGroup(layout.createSequentialGroup()
							.addComponent(readModeLabel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(readModeStateLabel)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addGroup(layout.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblFilePrefix)
							.addComponent(PrefixNameOfFileField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createParallelGroup(Alignment.BASELINE)
							.addComponent(SuffixNameOfFileField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblFileSuffix)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(nameOfFileLabel)
						.addComponent(nameOfFileField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(fileOutputDirectory))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(timeStampCheck, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(1)
					.addComponent(saveOnly9AxisCheck, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(saveTestParamsCheck, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(writeStatusLabel, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lenOfTestLabelR)
						.addComponent(testLengthFieldR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(numTestsLabelR)
						.addComponent(numTestFieldR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(magSampleLabelR, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(magSampleFieldR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(accelGyroSampleLabelR, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						.addComponent(accelGyroSampleFieldR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(accelSensiLabelR, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(Alignment.BASELINE)
							.addComponent(accelSensiFieldR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(gyroSensiLabelR, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(gyroSensiFieldR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(accelFilterLabelR, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(accelFilterFieldR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(gyroFilterFieldR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(gyroFilterLabelR, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(jProgressBar1, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addGap(14))
		);
		getContentPane().setLayout(layout);

		pack();
	}



	// Variables declaration - do not modify
	private javax.swing.JComboBox<String> accelFilterComboBox;
	private javax.swing.JTextField accelFilterFieldR;
	private javax.swing.JLabel accelFilterLabel;
	private javax.swing.JLabel accelFilterLabelR;
	private javax.swing.JTextField accelGyroSampleField;
	private javax.swing.JTextField accelGyroSampleFieldR;
	private javax.swing.JLabel accelGyroSampleLabel;
	private javax.swing.JLabel accelGyroSampleLabelR;
	private javax.swing.JComboBox<String> accelSensiComboBox;
	private javax.swing.JTextField accelSensiFieldR;
	private javax.swing.JLabel accelSensiLabel;
	private javax.swing.JLabel accelSensiLabelR;
	private java.awt.Button commPortsBtn;
	private javax.swing.JComboBox<String> commPortsComboBox;
	private javax.swing.JLabel writeStatusLabel;
	private java.awt.Button disconnectBtn;
	private javax.swing.JComboBox<String> gyroFilterComboBox;
	private javax.swing.JTextField gyroFilterFieldR;
	private javax.swing.JLabel gyroFilterLabel;
	private javax.swing.JLabel gyroFilterLabelR;
	private javax.swing.JComboBox<String> gyroSensiComboBox;
	private javax.swing.JTextField gyroSensiFieldR;
	private javax.swing.JLabel gyroSensiLabel;
	private javax.swing.JLabel gyroSensiLabelR;
	public static javax.swing.JProgressBar jProgressBar1;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JTextField testLengthField;
	private javax.swing.JTextField testLengthFieldR;
	private javax.swing.JLabel lenOfTestLabel;
	private javax.swing.JLabel lenOfTestLabelR;
	private javax.swing.JTextField magSampleField;
	private javax.swing.JTextField magSampleFieldR;
	private javax.swing.JLabel magSampleLabel;
	private javax.swing.JLabel magSampleLabelR;
	private javax.swing.JTextField nameOfFileField;
	private javax.swing.JLabel nameOfFileLabel;
	private javax.swing.JTextField numTestFieldR;
	private javax.swing.JLabel numTestsLabelR;
	private javax.swing.JLabel readModeLabel;
	private javax.swing.JLabel readModeStateLabel;
	private java.awt.Checkbox saveOnly9AxisCheck;
	private java.awt.Checkbox saveTestParamsCheck;
	private java.awt.Button serialToggleBtn;
	private static javax.swing.JLabel mainStatusLabel;
	private java.awt.Checkbox timeStampCheck;
	private java.awt.Button writeBtn;
	private javax.swing.JLabel writeModeLabel;
	private javax.swing.JLabel writeModeStateLabel;
	private JButton fileOutputDirectory;
	private JTextField PrefixNameOfFileField;
	private JTextField SuffixNameOfFileField;
	private JLabel lblFilePrefix;
	private JLabel lblFileSuffix;
	private JCheckBox timedTestCheckbox;
}
