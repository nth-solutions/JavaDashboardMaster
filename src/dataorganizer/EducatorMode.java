package dataorganizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.PureJavaIllegalStateException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.DefaultComboBoxModel;

public class EducatorMode extends JFrame {

	private JPanel contentPane;
	private volatile JProgressBar progressBar;
	private JLabel generalStatusLabel;
	private JButton disconnectButton;
	private JComboBox testTypeComboBox;
	private JComboBox commPortCombobox;
	private JButton readTestsBtn;
	public static EducatorMode educatorInstance;		//The single instance of the dashboard that can be referenced anywhere in the class. Defined to follow the Singleton Method: https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples		
	public ArrayList<Integer> writeData = new ArrayList<Integer>();
	
	//Operation Threads
		private Thread readThread;
	
	//Flags
		private boolean readMode = true;
		private boolean organizeAbort = false;
		private boolean readAbort = false;
		private boolean paramAbort = false;
		private boolean portInitialized = false;
		private boolean frameInitialized = false;
		private boolean portOpened = false;
		private boolean dataStreamsInitialized = false;
		
		
		public static EducatorMode educatorMode;
		
	//Serial Port Variables
		private SerialPort serialPort;      			//Object for the serial port class
		private static CommPortIdentifier portId;       //Object used for opening a comm ports
		private static Enumeration portList;            //Object used for finding comm ports
		private InputStream inputStream;                //Object used for reading serial data 
		private OutputStream outputStream;              //Object used for writing serial data
		
		public static final int NUM_TEST_PARAMETERS = 13;
		
		
	//Module test data variables
		ArrayList<Integer> testParameters;
		
	//Global file name for this instance
		String saveFileName="";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EducatorMode frame = new EducatorMode();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Necessary for singleton design pattern, especially the "synchronized" keyword for more info on the singleton method: https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples
	 * @return the one and only allowed dashboard instance, singleton pattern specifies only one instance can exist so there are not several instances of the dashboard with different variable values
	 */
	public static synchronized EducatorMode getFrameInstance() {
		if (educatorMode == null) {
			educatorMode = new EducatorMode();
		}
		return educatorMode;
	}
	
	/**
	 * Create the frame.
	 */
	public EducatorMode() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Data Organizer Rev-3 (Educator/StudentMode)(6/06/2018)(Under Construction)");
		setBounds(100, 100, 521, 360);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		//Set the look and feel to whatever the system default is.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch(Exception e) {
			System.out.println("Error Setting Look and Feel: " + e);
		}
				
		setContentPane(contentPane);
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(500, 150));
		contentPane.add(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		panel_1.setLayout(new GridLayout(1, 3, 0, 0));
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(500, 20));
		contentPane.add(progressBar);
		
		commPortCombobox = new JComboBox();
		commPortCombobox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				portSelectedHandler();
			}
		});
		
		JButton refreshPortButton = new JButton("Refresh Port List");
		refreshPortButton.setBorder(null);
		refreshPortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findPorts();
			}
		});
		panel_1.add(refreshPortButton);
		
		
		panel_1.add(commPortCombobox);
		
		disconnectButton = new JButton("Disconnect");
		disconnectButton.setEnabled(false);
		disconnectButton.setForeground(Color.BLACK);
		disconnectButton.setBorder(null);
		panel_1.add(disconnectButton);
		
		generalStatusLabel = new JLabel("Please Select Port to Begin");
		generalStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		generalStatusLabel.setForeground(Color.BLUE);
		generalStatusLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		generalStatusLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.add(generalStatusLabel);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2);
		panel_2.setLayout(new GridLayout(0, 3, 0, 0));
		
		JLabel lblNewLabel = new JLabel("Select Test Type");
		panel_2.add(lblNewLabel);
		
		JSeparator separator = new JSeparator();
		separator.setForeground(Color.WHITE);
		panel_2.add(separator);
		
		JComboBox testTypeComboBox = new JComboBox();
		testTypeComboBox.setModel(new DefaultComboBoxModel(new String[] {"Conservation of Momentum", "Pendulum"}));
		panel_2.add(testTypeComboBox);
		
		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3);
		panel_3.setLayout(new GridLayout(0, 1, 0, 0));
		
		JButton programBtn = new JButton("Program Adventure Module");
		programBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				writeButtonHandler();
			}
		});
		panel_3.add(programBtn);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setForeground(Color.WHITE);
		contentPane.add(separator_1);
		
		readTestsBtn = new JButton("Read Tests");
		readTestsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				readButtonHandler();
			}
		});
		contentPane.add(readTestsBtn);
		
		JButton settingsBtn = new JButton("...");
		settingsBtn.setHorizontalAlignment(SwingConstants.LEFT);
		settingsBtn.setVerticalAlignment(SwingConstants.BOTTOM);
		contentPane.add(settingsBtn);
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
	 * Configures the serial port and input/output streams for the import sequences (most important parameter is the baud rate)
	 * @return boolean that allows for easy exiting of the method in addition to notifying the caller that if it was successful
	 */
	public boolean configureForImport() {
		//Close the current serial port if it is open (Must be done for dashboard to work properly for some reason, do not delete)
		if (portInitialized) {
			serialPort.close();
		}
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
			inputStream = inputStream = new BufferedInputStream(serialPort.getInputStream(), 725760);
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
		if (dataStreamsInitialized) {
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
					if (inputStream.available() > 0) {     

						//Reset progress bar
						progressBar.setValue(0);                     

						//Initialize arraylists to store test params and test data
						ArrayList<Integer> testParameters = new ArrayList<Integer>();     
						ArrayList<Integer> testData = new ArrayList<Integer>();

						//Check for test parameter preamble
						waitForPreamble(1,4);

						//Determine number of tests to expect/ get test parameters
						int expectedTestNum = -1;
						while(expectedTestNum == -1) {
							if(inputStream.available() > 0) {
								expectedTestNum = inputStream.read();
							}

						}

						//Notify the user the number of tests that are being imported
						generalStatusLabel.setText("Importing and Converting Data for " + expectedTestNum + " Test(s)");

						//Reset param index
						int paramNum = 0;

						//Executes while it is still receiving test parameters
						//TODO: add handshakes/ timeout
						while (paramNum < NUM_TEST_PARAMETERS) {
							if (inputStream.available() > 2) {
								//Store newly received test parameter in arraylist at index specified by paramNum
								testParameters.add(paramNum, (inputStream.read() * 256 + inputStream.read()));
								paramNum++;
							}

						}				

						//Get date for file name
						Date date = new Date();
						


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
									if (inputStream.available() > 0) {
										//Store newly read byte in temp variable
										temp = inputStream.read();
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

							}

							//Set the last value of the array as -1 so organizer knows that this is the end of test
							//TODO: Identify if this can be removed
							finalData[j] = -1;


							//Set name of file string based on prefixes and suffixes
							saveFileName += " (#" + (testNum) + ") "  + " "  + ".CSV";  //Add a number and .CSV to the file name

							//Assign test variables to temporary final variables so they can be used as parameters in the organizer thread below
							final int testID = testNum;		
							final int numTests = expectedTestNum;

							//Define new operation that can be run in a thread
							Runnable organizerOperation = new Runnable() {
								public void run() {
									//Organize data into .CSV
									LoadSettings settings = new LoadSettings();
									settings.loadConfigFile();
									Organizer organizer = new Organizer();
									organizer.sortData(finalData, testID, numTests, saveFileName, (testParameters.get(7) / testParameters.get(8)), (1 / testParameters.get(8)), settings.getKeyVal("CSVSaveLocation")+saveFileName);  //create the .CSV with neccessary parameters
								}
							};

							//Define new thread to run predefined operation
							Thread organizerThread = new Thread(organizerOperation);
							//Start new thread
							organizerThread.start();      
							//Increment test index
							testNum++;             
						}

						if (testNum == expectedTestNum) {
							dataReceived = true;
						}
					}
				}

				//Executes if the test data was successfully received
				//TODO: Refactor to make sense, the data received flag must be set to true somewhere
				if(dataReceived) {
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
					if (inputStream.available() > 0) {
						int temp;
						//Iterates until the specified preamble is received
						//TODO: Add timeout to this loop
						for(int counter = start; counter <= stop;) {

							//Store newly read byte in the temp variable (Must mod by 256 to get single byte due to quirks in BufferedReader class)
							temp = inputStream.read();

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
					if (inputStream.available() > 0) {
						int temp;
						//Iterates until the specified postamble is received
						//TODO: Add timeout to this loop
						for(int counter = start; counter >= stop;) {
							//Store newly read byte in the temp variable (Must mod by 256 to get single byte due to quirks in BufferedReader class)
							temp = inputStream.read();

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
	 * This method handles the transmission of test parameters to the module with build in handshakes to verify each parameter is correctly received
	 * @return flag that states whether the operation was successful (used mostly for easy exiting of this method upon comm failure)
	 */
	private boolean sendParameters() {
		//Method for writing data to URI module. Sends programming config to URI module    
		configureForHandshake();
		progressBar.setValue(0);

		try {
			if (dataStreamsInitialized && !paramAbort) {

				if(!selectMode('P')) {
					return false;
				}

				switch(testTypeComboBox.getSelectedItem().toString()) {
					case "Conservation of Momentum":
						//0 Serial Number
						writeData.add(0);
						//1 Hardware Version
						writeData.add(5);
						//2 Firmware Version
						writeData.add(20);
						//3 Accel Gyro Sample Rate
						writeData.add(getTickThreshold(960));
						//4 Delay after start
						writeData.add(0);
						//5 Battery timeout flag
						writeData.add(300);
						//6 Timed test flag
						writeData.add(1);
						//7 Trigger on release flag
						writeData.add(1);
						//8 Test Length
						writeData.add(25);
						//9 Accel Gyro Sample Rate
						writeData.add(120);
						//10 Mag Sample Rate
						writeData.add(120);
						//11 Accel Sensitivity
						writeData.add(8);
						//12 Gyro Sensitivity
						writeData.add(1000);
						//13 Accel Filter
						writeData.add(92);
						//14 Gyro Filter
						writeData.add(92);
						break;
					case "Pendulum":
						//0 Serial Number
						writeData.add(0);
						//1 Hardware Version
						writeData.add(5);
						//2 Firmware Version
						writeData.add(20);
						//3 Accel Gyro Sample Rate
						writeData.add(getTickThreshold(960));
						//4 Delay after start
						writeData.add(0);
						//5 Battery timeout flag
						writeData.add(300);
						//6 Timed test flag
						writeData.add(1);
						//7 Trigger on release flag
						writeData.add(1);
						//8 Test Length
						writeData.add(25);
						//9 Accel Gyro Sample Rate
						writeData.add(120);
						//10 Mag Sample Rate
						writeData.add(120);
						//11 Accel Sensitivity
						writeData.add(8);
						//12 Gyro Sensitivity
						writeData.add(1000);
						//13 Accel Filter
						writeData.add(92);
						//14 Gyro Filter
						writeData.add(92);
						break;
				}
				

				for (int paramNum = 0; paramNum < writeData.size(); paramNum++) {
					boolean paramReceived = false;
					progressBar.setValue((int)(100 * ((double)paramNum / (double)writeData.size()) / 1.2));
					int attemptCounter = 0;
					while (!paramReceived) {

						//Send Preamble
						outputStream.write(new String("1234").getBytes());

						//Send parameter in binary (not ASCII) MSB first
						outputStream.write(writeData.get(paramNum) / 256);
						outputStream.write(writeData.get(paramNum) % 256);


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
						if (temp == writeData.get(paramNum)) {
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
							generalStatusLabel.setText("Module not Echoing Properly, Check Connections");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(255,0,0));
							//Exit method, communication failed
							return false;
						}

						if (paramAbort) {
							return false;
						}
					}
				}

			}
			else if (paramAbort){
				generalStatusLabel.setText("Write Aborted");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(255,0,0));
				//Exit method, communication failed
				return false;
			}
			else {
				generalStatusLabel.setText("Data Not Sent, No Port Selected");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(255,0,0));
				//Exit method, communication failed
				return false;
			}
		}
		catch (IOException e) {                                          //If there is an IOException
			generalStatusLabel.setText("Data Not Sent, Error Communicating with Dongle");    //Notify the user that something broke
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			//Exit method, communication failed
			return false;
		} 
		catch (NullPointerException e) {                                  //If there is a NullPointer
			generalStatusLabel.setText("Data Not Sent, No Port Selected");  //The serial port was not open; notifies the user about the mistake
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			//Exit method, communication failed
			return false;
		}



		generalStatusLabel.setText("Module Configuration Successful, Parameters Have Been Updated");
		progressBar.setForeground(new Color(51, 204, 51));
		progressBar.setValue(100);
		return true;
	}
	public boolean configureForHandshake() {
		serialPort.close();
		openSerialPort(serialPort.getName());
		try {
			serialPort.setSerialPortParams(38400,      //Opens the serial port at 115200 Baud for high speed reading
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			outputStream = serialPort.getOutputStream();
			inputStream = serialPort.getInputStream();
			dataStreamsInitialized = true;
		} 
		catch (UnsupportedCommOperationException e) {
			generalStatusLabel.setText("Check Serial Dongle Compatability!");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		}
		catch (IOException e) {
			generalStatusLabel.setText("Error Communicating with Dongle");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		}
		return true;

	}
	public boolean selectMode(char modeDelimiter) {
		try {
			if(!configureForHandshake()) {
				return false;
			}


			clearInputStream();

			int attemptCounter = 0;
			boolean modeSelected = false;
			while(!modeSelected) {
				//Start condition followed by 'S' to tell firmware to start saving new parameters
				outputStream.write(new String("1111" + modeDelimiter).getBytes());

				//long startTime = System.currentTimeMillis();
				int temp = -1;
				long startTime = System.currentTimeMillis();
				while((System.currentTimeMillis() - startTime) < 50) {
					if (inputStream.available() > 0) {
						temp = inputStream.read();
						break;
					}	
				}
				if (temp == (int)modeDelimiter) {
					modeSelected = true;
					return true;
				}
				//If an unknown character or '?' is received, try sending again
				else {
					attemptCounter++;
				}

				if (attemptCounter == 10) {

					//Command not recognized module
					if (temp == '?') {
						generalStatusLabel.setText("Command Not Recognized by Module, Check Firmware Version");

					}

					else if (temp == -1) {
						generalStatusLabel.setText("Module Unresponsive or Connected Improperly (Timeout)");
					}

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
		catch (IOException e) {                                          //If there is an IOException
			generalStatusLabel.setText("Error Communicating with Dongle");    //Notify the user that something broke
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		} 
		catch (NullPointerException e) {                                  //If there is a NullPointer
			generalStatusLabel.setText("Please Select a Port");  //The serial port was not open; notifies the user about the mistake
			updateProgress(getProgressBar(), 100, new Color(255,0,0));
			//Exit method, communication failed
			return false;
		}
		return true;
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
			generalStatusLabel.setText("Dongle Already In Use");
		}

		if (portOpened) {
			try {
				inputStream = serialPort.getInputStream();              //creates input stream
				outputStream = serialPort.getOutputStream();            //creates output stream
				dataStreamsInitialized = true;
				disconnectButton.setEnabled(true);
			} 
			catch (IOException e) {
				generalStatusLabel.setText("Error Communicating with Serial Dongle");
			}

		}
	}
	public void clearInputStream(){
		if (dataStreamsInitialized) {
			try {
				while (inputStream.available() > 0) {
					inputStream.read();
				}
			}
			catch (IOException e) {
				generalStatusLabel.setText("Error Communicating with Dongle");
			}

		}
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
	
	/**
	 * Handles the button press of the write configuration button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume. See the method calls within the runnable for more info
	 * on what this handler actually does.
	 */
	private void writeButtonHandler() {
		SerialComm serialHandler = new SerialComm();
			Runnable sendParamOperation = new Runnable() {
				public void run() {
					//Disable write config button while the sendParameters() method is running
					try {
						if(!serialHandler.sendTestParams(writeData)) {
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
				}
			};

			//Assign new operation to a thread so that it can be run in the background
			final Thread paramThread = new Thread(sendParamOperation);
			//Start the new thread
			paramThread.start();
		}
	
	/**
	 * This method handles which methods will be called when the user selects a port from the comm port combobox
	 */
	private void portSelectedHandler() {

		if (commPortCombobox.getSelectedItem() != null) {

			String selectedCommID = commPortCombobox.getSelectedItem().toString();      //creates a string of selected item; Name of the comm port as a string

			openSerialPort(selectedCommID);                                        //opens the serial port with the selected comm Port


			generalStatusLabel.setText("Serial Port Opened Successfully, Awaiting Commands");

			portInitialized = true;
		}

	}
	
	public void readButtonHandler() {
		//Define operation that can be run in separate thread
		Runnable readOperation = new Runnable() {
			public void run() {
				//Disable read button while read is in progress
				readTestsBtn.setEnabled(false);

				//Read data from test data module
				readTestData();

				//Re-enable read button upon read completion
				readTestsBtn.setEnabled(true);
			}
		};

		//Set thread to execute previously defined operation
		readThread = new Thread(readOperation);
		//Start thread
		readThread.start();

	}

	public JProgressBar getProgressBar() {
		return this.progressBar;
	}
	
	/**
	 * Handles the button press of browse button. This is an action event which must handled before the rest of the program resumes. This method allows the user to navigate
	 * the file explorer and select a save location for the incoming data.
	 */
	public void browseButtonHandler() {
		JFileChooser chooser;
		chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			saveFileName = chooser.getSelectedFile().toString();
		}
		else {
			saveFileName = null;
		}
	}
	
	private void updateProgress(JProgressBar thisProgressBar, final int pbarVal, Color color) {
		    thisProgressBar.setValue(pbarVal);
			thisProgressBar.setForeground(color);
	}
}
