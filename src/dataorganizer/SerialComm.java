/**
 * SerialComm.java
 * Purpose: This class handles all UART communications in a modular way so it can be used from any GUI we design.
 * Notes: This class should never refer to any outside GUI element. If a method needs to change a status label, progress bar, etc., pass it in as a parameter
 * 		  
 */



package dataorganizer;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.sun.javafx.collections.MappingChange.Map;

import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.PureJavaIllegalStateException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

public class SerialComm {

	//Input and Output Streams of the serial port, input stream must be buffered to prevent data loss due to buffer overflows, DO NOT USE a BufferedReader, it will encode bytes via UTF-8 
	private BufferedInputStream inputStream;       
	private OutputStream outputStream;              

	//Serial port identifiers for opening and the serial port
	private CommPortIdentifier portId;       		
	private SerialPort serialPort;
	private String serialPortName;


	//Flags that track object/process states
	private boolean dataStreamsInitialized = false;
	private boolean remoteTestActive = false;

	//Constructor not used to initialize anything right now.
	public SerialComm() {
	}

	/**
	 * Builds a list the names of all the serial ports to place in the combo box
	 * @param evt event pasted in by any button or action that this method was called by (method of passing info related to the source)
	 */
	public ArrayList<String> findPorts() {
		//Fills the portEnum data structure (functions like arrayList) with ports (data type that encapsulates the name and hardware interface info)
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();   

		//Stores the names of the ports
		ArrayList<String> portNames = new ArrayList<String>();

		//Iterate through each port object in the portEnumList and stores the name of the port in the portNames array
		while (portList.hasMoreElements()) {                   //adds the serial ports to a string array
			CommPortIdentifier portIdentifier = portList.nextElement();
			portNames.add(portIdentifier.getName());
		}

		//If at least 1 serial port is found, fill the combo box with all the known port names. Otherwise, notify the user that there are no visible dongles. 
		if (portNames.size() > 0) {
			return portNames;
		}

		return null;

	}



	/**
	 * Opens serial port with the name passed in as a parameter in addition to initializing input and output streams.
	 * @param commPortID Name of comm port that will be opened
	 */
	public boolean openSerialPort(String commPortID) throws IOException, PortInUseException {     
		//Creates a list of all the ports that are available of type Enumeration (data structure that can hold several info fields such as ID, hardware interface info, and other info used by the PC 
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();                     

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

		//Open the serial port with a 2 second timeout
		serialPort = (SerialPort) portId.open("portHandler", 2000);


		//Create a new buffered reader so we can define the buffer size to prevent a buffer overflow (explicitly defined in the configureForImport() method)
		inputStream = new BufferedInputStream(serialPort.getInputStream(), 756000);

		//Assign the output stream to the output stream of the serial port (no need for a buffer as far as we know)
		outputStream = serialPort.getOutputStream();  

		//Set flag so program knows that the data streams were initialized
		dataStreamsInitialized = true;

		return true;

	}


	/**
	 * Closes serial port and updates GUI labels/ software flags
	 */
	public boolean closeSerialPort() {
		//If the disconnect button is pressed: disconnects from the serial port and resets the UI   
		if (serialPort != null) {

			//Close the serial port
			serialPort.close();  

			//Let the whole class know that the data streams are no longer initialized
			dataStreamsInitialized = false;
			return true;

		}
		//Method failed so return false
		return false;
	}

	/**
	 * Clears the input stream buffer
	 */
	public boolean clearInputStream() throws IOException{
		//Executes if the data streams are currently initialized (prevents null pointer exception)
		if (dataStreamsInitialized) {
			//Executes while there is still data in the input stream buffer
			while (inputStream.available() > 0) {
				//Read a value from the buffer and don't store it, just throw it away
				inputStream.read();
			}
			return true;

		}
		//Method failed so return false
		return false;
	}

	/**
	 * Configures the serial port and input/output streams for the handshake sequences (most important parameter is the baud rate)
	 * @return boolean that allows for easy exiting of the method in addition to notifying the caller that if it was successful
	 */
	public boolean configureForHandshake() throws IOException, PortInUseException, UnsupportedCommOperationException {
		//Close the current serial port if it is open (Must be done for dashboard to work properly for some reason, do not delete)
		if (dataStreamsInitialized) {
			serialPortName = serialPort.getName();
			serialPort.close();
		}

		//Reopen serial port
		if(serialPortName != null) {
			openSerialPort(serialPortName);
		}
		else {
			return false;
		}


		//Configure the serial port for 38400 baud for low speed handshakes
		serialPort.setSerialPortParams(38400,      
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		//Assign the output stream variable to the serial port's output stream
		outputStream = serialPort.getOutputStream();
		//Assign the input stream variable to the serial port's input stream via a buffered reader so we have the option to specify the buffer size
		inputStream = new BufferedInputStream(serialPort.getInputStream(), 756000);
		dataStreamsInitialized = true;

		//Return true to exit the method and notify the caller that the method was successful
		return true;
	}

	/**
	 * Configures the serial port and input/output streams for the import sequences (most important parameter is the baud rate)
	 * @return boolean that allows for easy exiting of the method in addition to notifying the caller that if it was successful
	 */
	public boolean configureForImport() throws IOException, PortInUseException, UnsupportedCommOperationException {
		//Close the current serial port if it is open (Must be done for dashboard to work properly for some reason, do not delete)
		if (dataStreamsInitialized) {
			serialPort.close();
		}
		//Reopen serial port
		openSerialPort(serialPort.getName());


		//Configure the serial port for 153600 baud for high speed exports
		serialPort.setSerialPortParams(921600,      
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		//Assign the output stream variable to the serial port's output stream
		outputStream = serialPort.getOutputStream();
		//Assign the input stream variable to the serial port's input stream via a buffered reader so we have the option to specify the buffer size
		inputStream = new BufferedInputStream(serialPort.getInputStream(), 756000);
		dataStreamsInitialized = true;


		//Return true to exit the method and notify the caller that the method was successful
		return true;

	}

	/**
	 * Waits for a sequence of consecutive, increasing numbers then exits the loop
	 * @param start the number to start the counting sequence at. (must be less than the 'stop' parameter)
	 * @param stop the number at which the preamble is consider fully received, the ending number on the counter
	 * @return boolean that allows for easy exiting of the method in addition to notifying the caller that if it was successful or timed out
	 */
	public boolean waitForPreamble(int start, int stop, int timeout) throws IOException {
		//Get start time so a timeout can be used in subsequent while loop
		long startTime = System.currentTimeMillis();
		//While the loop has been executing for less than 500ms
		while (((System.currentTimeMillis() - startTime) < timeout)) {
			//Executes if there is data in the input stream's buffer
			if (inputStream.available() > 0) {
				int temp;
				//Iterates until the specified preamble is received
				long preambleStart = System.currentTimeMillis();
				int counter = start;
				while (counter <= stop && (System.currentTimeMillis() - preambleStart) < timeout) {

					try {
						if (inputStream.available() > 0) {
	
							//Store newly read byte in the temp variable (Must mod by 256 to get single byte due to quirks in BufferedReader class)
							temp = inputStream.read();
							//System.out.println(temp);
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
							preambleStart = System.currentTimeMillis();
						}
					}
					catch(Exception PureJavaIllegalStateException) {
						return false;
					}
				}
				
				if (counter - 1 == stop) {
					//System.out.println("Start");
					//Return true to exit the method and notify the caller that the method was successful
					return true;
				}
				//System.out.println("TIMEOUT");
				return false;

			}
		}

		//Return true to exit the method and notify the caller that the method was successful
		return false;
	}


	/**
	 * Waits for a sequence of consecutive, decreasing numbers then exits the loop
	 * @param start the number to start the counting sequence at (must be greater than the 'stop' parameter)
	 * @param stop the number at which the postamble is consider fully received, the ending number on the counter
	 * @return boolean that allows for easy exiting of the method in addition to notifying the caller that if it was successful or timed out
	 */
	public boolean waitForPostamble(int start, int stop, int timeout) throws IOException {
		//Get start time so a timeout can be used in subsequent while loop
		long startTime = System.currentTimeMillis();

		//While the loop has been executing for less than 500ms
		//TODO: This timeout will not work if it is in the internal for loop. Add timeout to for loop if necessary
		while (((System.currentTimeMillis() - startTime) < timeout)) {
			//Executes if there is data in the input stream's buffer
			if (inputStream.available() > 0) {
				int temp;
				//Iterates until the specified preamble is received
				//TODO: Add timeout to this loop

				long postambleStart = System.currentTimeMillis();
				int counter = start;
				while (counter >= stop && (System.currentTimeMillis() - postambleStart) < timeout) {

					if (inputStream.available() > 0) {
						//Store newly read byte in the temp variable (Must mod by 256 to get single byte due to quirks in BufferedReader class)
						temp = inputStream.read();

						//Executes of the byte received is equal to the current value of counter
						if (temp == counter) {    
							//Increment counter by 1
							counter--;
						} 

						//Executes if the counter != temp
						else {
							//Reset the counter
							counter = start;
						}
						postambleStart = System.currentTimeMillis();
					}
				}

				if (counter + 1 == stop) {
					//Return true to exit the method and notify the caller that the method was successful
					return true;
				}
				return false;

			}
		}

		//Return true to exit the method and notify the caller that the method was successful
		return false;
	}


	/**
	 * Handles the stopping of a test. To stop a test we pull the line low and check on the firmware side. We then expect a handshake.
	 * @return boolean that allows for easy exiting of the method if the method is successful or fails
	 */
	public boolean stopTest() throws IOException, PortInUseException, UnsupportedCommOperationException{

		byte[] lineLow = {0,0,0,0};

		outputStream.write(lineLow);

		waitForPostamble(4,1,500);

		return true;
	}

	/**
	 * This method puts the module in calibration mode which will basically just tell it to run a calibration test next
	 * @return boolean that allows for easy exiting of the method if the method is successful or fails
	 */
	public boolean configForCalibration() throws IOException, PortInUseException, UnsupportedCommOperationException{
		//Attempt to configure the serial dongle for handshake mode, exit if it fails to do so
		if(!configureForHandshake()) {
			return false;
		}

		if(!selectMode('C')) {
			return false;
		}

		return true;
	}

	/**
	 * This method sends the passed in offset to the module with built in handshakes. This is the only means by which the offset
	 * variable can be overriden in the firmware.
	 * @param offset positive or negative number that will be sent to the module to be applied to the TMR0 tick threshold
	 * @return boolean that allows for easy exiting of the method if the method is successful or fails
	 */
	public boolean applyCalibrationOffsets(int tmr0Offset, int delayAfterStart) throws IOException, PortInUseException, UnsupportedCommOperationException {
		//Attempt to configure the serial dongle for handshake mode, exit if it fails to do so
		if(!configureForHandshake()) {
			return false;
		}

		if(!selectMode('O')) {
			return false;
		}

		//Reset attempt counter
		int attemptCounter = 0;
		
		int[] calData = new int[2];
		
		calData[0] = tmr0Offset;
		calData[1] = delayAfterStart;
		
		int dataIndex = 0;
		//Loops until both calibration values are sent to the module successfully or it fails too many times
		while (dataIndex < 2) {

			//Send Preamble
			outputStream.write(new String("1234").getBytes());

			if(dataIndex == 0) {
				//Send parameter in binary (not ASCII) First byte will specify if it is positive (+ = 1) or negative (- = 0)
				if (tmr0Offset < 0) {
					outputStream.write(0);
					tmr0Offset = tmr0Offset * -1;
				}
				else {
					outputStream.write(1);
				}	
			}


			outputStream.write(calData[dataIndex] / 256);
			outputStream.write(calData[dataIndex] % 256);


			int temp = -1;
			long echoStart = System.currentTimeMillis();
			while((System.currentTimeMillis() - echoStart) < 500) {

				//Executes if the data was received back from the module
				if (inputStream.available() >= 2) {
					//Store the echoed number in a temporary variable
					temp = (inputStream.read() * 256) + inputStream.read(); 
					//Set a flag to break the loop
					break;
				}	
			}

			//If module echoed correctly, send 'CA' for Acknowledge, (C is preamble for acknowledge cycle)
			if (temp == calData[dataIndex]) {
				outputStream.write(new String("CA").getBytes());
				//Reset attempt counter
				attemptCounter = 0;
				dataIndex++;
			}

			//If module echoed incorrectly, send 'CN' for Not-Acknowledge, (C is preamble for acknowledge cycle)
			else {
				outputStream.write(new String("CN").getBytes());
				//Increment attempt counter
				attemptCounter++;
			}

			//Executes after 5 failed attempts
			if (attemptCounter == 5) {
				//Exit method, communication failed
				return false;
			}
		}

		waitForPostamble(4 , 1, 500);

		return true;
	}


	/**
	 * Handles the starting of a test. Returns true for success false for failure. 
	 * @return boolean that allows for easy exiting of the method if the method is successful or fails
	 */
	public boolean startTest() throws IOException, PortInUseException, UnsupportedCommOperationException {

		//Attempt to configure the serial dongle for handshake mode, exit if it fails to do so
		if(!configureForHandshake()) {
			return false;
		}

		if(!selectMode('W')) {
			return false;
		}

		waitForPostamble(4 , 1, 500);

		return true;
	}


	/**
	 * Handles the handshakes that tell the module to enter a mode specified by the passed in modeDelimiter character. ex) 'E' for export data (must be identified in the firmware as well).
	 * This method attempts several times before giving up and notifying the user that there is an error in the communication
	 * @param modeDelimiter The mode identifier that is specified in the firmware ex) 'E' for export data
	 * @return boolean that allows for easy exiting of the method if the method is successful or fails
	 */
	public boolean selectMode(char modeDelimiter) throws IOException, PortInUseException, UnsupportedCommOperationException{

		//Attempt to configure the serial dongle for handshake mode, exit if it fails to do so
		if(!configureForHandshake()) {
			return false;
		}

		//Clear the input stream so any previously unread data in the buffer isn't interpreted as an echo from the module
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
			while((System.currentTimeMillis() - startTime) < 200) {
				//Executes if data is in the input stream buffer
				if (dataStreamsInitialized) {
					if (inputStream.available() > 0) {
						//Assign the newly received byte to a temp variable then break the while loop
						temp = inputStream.read();
						break;
					}	
				}
				else {
					//Method failed so return false
					return false;
				}

			}
			//Executes if the byte just read is the expected echo value
			if (temp == (int)modeDelimiter) {
				//Set flag so while loop knows the mode has been selected, no longer used, but legacy there for future code development
				modeSelected = true;
				//Exit method
				return true;
			}
			else if (temp == (int)'<') {
				//Exit method, communication failed
				return false;
			}
			//Executes if '?' or unexpected character is received
			else {
				//Increment attempt counter by 1
				attemptCounter++;
			}

			//Executes if there have already been 10 attempts. Assumes there is an error in the hardware, firmware, or connection
			if (attemptCounter == 10) {

				//Exit method, communication failed
				return false;
			}
		}

		return true;
	}


	/**
	 * Sets the module into bulk erase mode and waits until it receives a 'Bulk erase complete handshake'.
	 * Since this method is called in a thread, the thread will terminate automatically when this method is completed
	 * @return boolean that allows easy exiting of the method. Since this is called in a thread, the return statement will automatically kill the thread on completion
	 */
	public boolean bulkEraseModule() throws IOException, PortInUseException, UnsupportedCommOperationException {

		//Put the module in bulk erase mode, exit if that routine fails
		if(!selectMode('B')) {
			return false;
		}

		//Wait for '4321' (in binary, not ascii) as a handshake so the dashboard knows the erase has completed
		if(!waitForPostamble(4 , 1, 70000))
			return false;


		//Return true to exit the method and notify the caller that the method was successful
		return true;

	}

	/**
	 * Sets the module into sector erase mode and waits until it receives a 'sector erase complete handshake'.
	 * Since this method is called in a thread, the thread will terminate automatically when this method is completed
	 * @return boolean that allows easy exiting of the method. Since this is called in a thread, the return statement will automatically kill the thread on completion
	 */
	public boolean sectorEraseModule() throws IOException, PortInUseException, UnsupportedCommOperationException {

		//Put the module in bulk erase mode, exit if that routine fails
		if(!selectMode('S')) {
			return false;
		}

		//Wait for '4321' (in binary, not ascii) as a handshake so the dashboard knows the erase has completed
		if(!waitForPostamble(4 , 1, 70000))
			return false;


		//Return true to exit the method and notify the caller that the method was successful
		return true;
	}

	/**
	 * Handles the button press of the Exit UART Mode Button. This is an action event which must handled before the rest of the program resumes. 
	 * This method sets the necessary flags on the module so it will only listen to the PB or the Remote so it doesn't hang up on the serin2 listener (Temporary fix until pullup is added)
	 */
	//TODO: Add pullup or come up with more intuitive solution so this is not needed
	public boolean sendExitCommand() throws IOException, PortInUseException, UnsupportedCommOperationException {

		//Tell the module to exit UART listening mode so it will only listen to the PB or Remote, exit if this routine fails
		if(!selectMode('Q')) {
			return false;
		}

		return true;
	}

	/**
	 * This method tells the method to module to start listening for new remotes to pair. It then waits for a handshake to see if the pairing process was successful or timed out
	 * @return allows for easy exiting of the method
	 * @throws IOException Means that there is an error communicating with dongle, thrown to caller for cleaner handling
	 * @throws PortInUseException Means that the selected port is already in use, thrown to caller for cleaner handling
	 * @throws UnsupportedCommOperationException Means that the requested operation is unsupported by the dongle, thrown to caller for cleaner handling
	 */
	public boolean pairNewRemote() throws IOException, PortInUseException, UnsupportedCommOperationException{

		if(!selectMode('+')) {
			return false;
		}
		waitForPostamble(4, 1, 15000);
		int ackValue = -1;
		while (ackValue == -1) {
			if (inputStream.available() > 0) {
				ackValue = inputStream.read();
				if (!(ackValue == (int)'A')) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Tells the module to erase it's receiver memory to unpair all remotes then waits for a postamble to confirm if the process was successful or not
	 * @return allows for easy exiting of the method
	 * @throws IOException Means that there is an error communicating with dongle, thrown to caller for cleaner handling
	 * @throws PortInUseException Means that the selected port is already in use, thrown to caller for cleaner handling
	 * @throws UnsupportedCommOperationException Means that the requested operation is unsupported by the dongle, thrown to caller for cleaner handling
	 */
	public boolean unpairAllRemotes() throws IOException, PortInUseException, UnsupportedCommOperationException {
		if(!selectMode('-')) {
			return false;
		}

		if(!waitForPostamble(4, 1, 15000)) {
			return false;
		}
		return true;
	}

	/*
	 * Puts the module in a test mode that allows the user to press remote buttons to verify if they are being received by the transmitter. This mode can only be 
	 * exited by setting the remoteTestActive boolean to false which is what the exitRemoteTest() method does.
	 * @return allows for easy exiting of the method
	 * @throws IOException Means that there is an error communicating with dongle, thrown to caller for cleaner handling
	 * @throws PortInUseException Means that the selected port is already in use, thrown to caller for cleaner handling
	 * @throws UnsupportedCommOperationException Means that the requested operation is unsupported by the dongle, thrown to caller for cleaner handling
	 */
	public boolean testRemotes(JLabel statusLabel) throws IOException, PortInUseException, UnsupportedCommOperationException {

		//Set module to enter test remote mode
		if(!selectMode('=')) {
			return false;
		}
		//Set flag so class knows that it is in test mode
		remoteTestActive = true;

		//Loops until the remoteTestActive boolean is set to false externally
		while (remoteTestActive) {

			//If there is data, see if it corresponds to a button being pressed, update the status label accordingly
			if (inputStream.available() > 0) {
				int temp = inputStream.read();
				if (temp == (int)'@') {
					statusLabel.setText("'A' Button is being Pressed");
				}
				else if (temp == (int)'!') {
					statusLabel.setText("'B' Button is being Pressed");
				}
				else {
					statusLabel.setText("No Button is being Pressed");
				}
			}

		}

		//If the test mode was exited externally by setting the remoteTestActive boolean to false, send the exit command to the module and listen for an echo
		if(!selectMode('#')) {
			return false;
		}

		//Method successful, return true
		return true;
	}

	/**
	 * Sets boolean to exit remote test mode
	 */
	public void exitRemoteTest() {
		remoteTestActive = false;
	}

	/**
	 * Obtains identification info from the module and updates the text on the dashboard. This method can also be used as a non intrusive way of calibrating baud rate without the risk
	 * of corrupting test parameters or testing data. Since this method is called in a thread, the thread will terminate automatically when this method is completed
	 * @return boolean that allows easy exiting of the method. Since this is called in a thread, the return statement will automatically kill the thread on completion
	 */
	public ArrayList<Integer> getModuleInfo(int numIDParams) throws IOException, PortInUseException, UnsupportedCommOperationException {

		//Configure Baud Rate for 38400 temporarily for handshakes
		configureForHandshake();

		//Executes if the data streams have already been initialized
		if (dataStreamsInitialized) {

			//Put module in 'send ID info' mode, exit method if that routine fails
			if(!selectMode('I')) {
				return null;
			}

			//Initialize flag and temp storage variable
			int temp;

			boolean preambleFlag = false;
			
			//Initialize ID index to 0
			int idCounter = 0;
			//Initialize temporary ID parameter array
			ArrayList<Integer> moduleInfo = new ArrayList<Integer>();

			//Initialize start time so timeout can be used on subsequent while loop
			long startTime = System.currentTimeMillis();

			//Executes while it is still receiving ID info and a timeout has not occured
			while (idCounter < numIDParams && (System.currentTimeMillis() - startTime) < 1500) {

				if (!preambleFlag) {
					//Wait for a preamble, exits method if the preamble times out
					if(!waitForPreamble(1, 4, 500)) {
						return null;
					}
					preambleFlag = true;
				}
				
				
				startTime = System.currentTimeMillis();
				
				//Executes if data has been received from the module
				if (inputStream.available() >= 2) {
					preambleFlag = false;
					startTime = System.currentTimeMillis();
					//Store 2 received bytes in MSB order and form into a word
					temp = inputStream.read() * 256 + inputStream.read();
					//Echo the value back
					outputStream.write(temp / 256);
					outputStream.write(temp % 256);

					//Initialize start time so timeout can be used on subsequent while loop
					long echoStart = System.currentTimeMillis();

					//Executes while the timeout has not occurred
					//while (((System.currentTimeMillis() - echoStart) < 200)) {
					int echoAttemptCounter = 0;
					while(echoAttemptCounter < 5) {
						int ackPreamble = -1;
						int ackValue = -1;
						long ackStart = System.currentTimeMillis();
						while (((System.currentTimeMillis() - echoStart) < 500)) {
							if (inputStream.available() >= 2) {
								ackPreamble = inputStream.read();
								if(ackPreamble == (int)'C') {
									ackValue = inputStream.read();
									break;	
								}
							}
						}

						if (ackValue == (int)'A') {
							//Store the confirmed value
							moduleInfo.add(temp);
							//Increment the ID index so the next ID parameter is stored
							idCounter++;
							break;
						}
						else {
							echoAttemptCounter++;
						}
					}
				}

			}

			//Executes if a timeout has occurred
			if (idCounter != moduleInfo.size()) {
				return null;
			}
			//Exit the program
			return moduleInfo;
		}
		//Exit the method, routine failed
		return null;
	}

	/**
	 * This method handles the transmission of test parameters to the module with build in handshakes to verify each parameter is correctly received. 
	 * Since this method is called in a thread, the thread will terminate automatically when this method is completed
	 * @return boolean that allows easy exiting of the method. Since this is called in a thread, the return statement will automatically kill the thread on completion
	 */
	public boolean sendTestParams(ArrayList<Integer> params) throws IOException, PortInUseException, UnsupportedCommOperationException {
		//Configure the serial port for handshake   
		configureForHandshake();

		//Executes if the data streams have been initialized and the thread has not been told to abort
		if (dataStreamsInitialized) {

			//Put the module in save new test parameter mode, exit if that routine fails
			if(!selectMode('P')) {
				return false;
			}


			//Iterates through each parameter in the array
			for (int paramNum = 0; paramNum < params.size(); paramNum++) {

				//Reset attempt counter
				int attemptCounter = 0;
				//Loops until a parameter is successfully received by module 
				while (true) {

					//Send Preamble
					outputStream.write(new String("1234").getBytes());

					//Send parameter in binary (not ASCII) MSB first
					outputStream.write(params.get(paramNum) / 256);
					outputStream.write(params.get(paramNum) % 256);


					int temp = -1;
					long echoStart = System.currentTimeMillis();
					while((System.currentTimeMillis() - echoStart) < 500) {

						//Executes if the data was received back from the module
						if (inputStream.available() >= 2) {
							//Store the echoed number in a temporary variable
							temp = (inputStream.read() * 256) + inputStream.read(); 
							//Set a flag to break the loop
							break;
						}	
					}

					//If module echoed correctly, send 'CA' for Acknowledge, (C is preamble for acknowledge cycle)
					if (temp == params.get(paramNum)) {
						outputStream.write(new String("CA").getBytes());
						//Reset attempt counter
						attemptCounter = 0;
						break;
					}

					//If module echoed incorrectly, send 'CN' for Not-Acknowledge, (C is preamble for acknowledge cycle)
					else {
						outputStream.write(new String("CN").getBytes());
						//Increment attempt counter
						attemptCounter++;
					}

					//Executes after 5 failed attempts
					if (attemptCounter == 5) {
						//Exit method, communication failed
						return false;
					}
				}
			}

		}

		//Executes if the port has not been opened yet
		else {
			//Exit method, communication failed
			return false;
		}

		return true;
	}

	/**
	 * This method reads the test parameters and the number of tests on the module and returns those values in an arraylist. If the method is unsuccessful in any way, it will return null
	 * @return arraylist of the test parameters in order as sent by the married firmware offset back by one index since the first element is saved as the number of tests (index: 0)
	 * @throws IOException Means that there is an error communicating with dongle, thrown to caller for cleaner handling
	 * @throws PortInUseException Means that the selected port is already in use, thrown to caller for cleaner handling
	 * @throws UnsupportedCommOperationException Means that the requested operation is unsupported by the dongle, thrown to caller for cleaner handling
	 */
	public ArrayList<Integer> readTestParams(int numTestParams) throws IOException, PortInUseException, UnsupportedCommOperationException {
		int expectedTestNum;
		ArrayList<Integer> params = new ArrayList<Integer>();

		boolean preambleFlag = false;
		
		//Configure the serial port for handshake   
		configureForHandshake();

		//Executes if the data streams have been initialized and the thread has not been told to abort
		if (dataStreamsInitialized) {

			//Put module into export test data mode, exit method if that routine fails
			if(!selectMode('G')) {
				return null;
			}

			int paramCounter = 0;
			long startTime = System.currentTimeMillis();
			//Iterates through each parameter in the array
			while (paramCounter < numTestParams && (System.currentTimeMillis() - startTime) < 1500) {

				if (!preambleFlag) {
					//Wait for a preamble, exits method if the preamble times out
					if(!waitForPreamble(1, 4, 500)) {
						return null;
					}
					preambleFlag = true;
				}

				//Executes if data has been received from the module
				if (inputStream.available() >= 2) {
					preambleFlag = false;
					//Store 2 received bytes in MSB order and form into a word
					int temp = inputStream.read() * 256 + inputStream.read();
					//Echo the value back
					outputStream.write(temp / 256);
					outputStream.write(temp % 256);

					//Initialize start time so timeout can be used on subsequent while loop
					long echoStart = System.currentTimeMillis();

					//Executes while the timeout has not occurred
					//while (((System.currentTimeMillis() - echoStart) < 200)) {
					int echoAttemptCounter = 0;
					while(echoAttemptCounter < 5) {
						int ackPreamble = -1;
						int ackValue = -1;
						long ackStart = System.currentTimeMillis();
						while (((System.currentTimeMillis() - echoStart) < 500)) {
							if (inputStream.available() >= 2) {
								ackPreamble = inputStream.read();
								if(ackPreamble == (int)'C') {
									ackValue = inputStream.read();
									break;	
								}
							}
						}

						if (ackValue == (int)'A') {
							//Store the confirmed value
							params.add(temp);
							//Increment the ID index so the next ID parameter is stored
							paramCounter++;
							break;
						}
						else {
							echoAttemptCounter++;
						}
					}
				}
			}
			//If not all the data was received (Timeout), return null to tell caller that there was an error
			if (params.size() < numTestParams) {
				return null;
			}

			//Return the arraylist of word data, Element 0 = expected test num followed by all testing data
			return params;
		}

		//Executes if the
		return null;

	}

	/**
	 * Tells the module to export it's test data and stores the info in a temporary buffer before calling the external organizer class to format the data into a .CSV
	 * Since this method is called in a thread, the thread will terminate automatically when this method is completed
	 * @return boolean that allows easy exiting of the method. Since this is called in a thread, the return statement will automatically kill the thread on completion
	 */
	public HashMap<Integer, ArrayList<Integer>> readTestData(int expectedTestNum, JProgressBar progressBar, boolean timedTestFlag, int expectedBytes) throws IOException, PortInUseException, UnsupportedCommOperationException {  
		//Put module into export test data mode, exit method if that routine fails
		if(!selectMode('E')) {
			return null;
		}
		configureForImport();

		//Executes if the data streams are initialized and the program was not aborted externally
		if (dataStreamsInitialized) {

			HashMap<Integer, ArrayList<Integer>> testData = new HashMap<Integer, ArrayList<Integer>>();
			byte[] pullLow = {0,0,0,0};


			double progress = 0;
			double dataProgressPartition = 0;

			//Progress can only be estimated if it is a timed test so if the module just took a timed test, calculate how much to update the progress bar each time it receives a sector of data
			if (timedTestFlag) {
				dataProgressPartition = (2520 / (double)expectedBytes) * (1 / (double)expectedTestNum);
			}
		
			
			//Loops until it all of the tests are collected
			for (int testNum = 0; testNum < expectedTestNum; testNum++) {
			
			
				if(!waitForPreamble(1, 4, 5000)) {
					return null;
				}
			
				//Notify that the dashboard is ready for test data
				outputStream.write(pullLow);
				
				
				//Wait for start condition (preamble)
				if(!waitForPreamble(1, 8, 1500)) {
					return null;
				}
				//Create start time variable for timeouts
				//long startTime = System.currentTimeMillis();

				byte [] tempTestData;
				//Executes while the stop condition has not been received (Main loop that actually stores testing data)
				while (true) {    

					//Assign an empty arraylist to the test number that is currently being stored
					ArrayList<Integer> rawData = new ArrayList<Integer>();
					
					boolean preambleFlag = false;
					
					while(true) {
						if (!preambleFlag) {
							//Wait for a preamble, exits method if the preamble times out
							if(!waitForPreamble(1, 4, 1500)) {
								return null;
							}
							preambleFlag = true;
						}
						//System.out.println("Sector Start");
						if (inputStream.available() > 0) {
							preambleFlag = false;
							int temp = inputStream.read();

							if (temp == (int)'M') {
								while (inputStream.available() < 2520) {
								}

								tempTestData = new byte[2520];

								inputStream.read(tempTestData, 0, 2520);
								//System.out.println(tempTestData);
								//Update progress bar based on how much data was received (dataProgressPartition calculated at top of method)
								progress += dataProgressPartition;
								progressBar.setValue((int)(progress * 100));

								for (byte data : tempTestData) {

									//Add the bulk read data to the rawData arraylist. IMPORTANT: & 255 converts it from a signed byte to an unsigned byte 
									rawData.add((int)data & 255);
									//System.out.println(data & 255);
								}
								outputStream.write(pullLow);
							}

							else if (temp == (int)'P') {
								for(int counter = 8; counter >= 1;) {
									//Store newly read byte in the temp variable (Must mod by 256 to get single byte due to quirks in BufferedReader class)
									if (inputStream.available() > 0) {
										temp = inputStream.read();

										rawData.add(temp);

										//System.out.println(temp);

										//Executes of the byte received is equal to the current value of counter
										if (temp == counter) {    
											//Decrement counter by 1
											counter--;
										} 

										//Executes if the counter != temp
										else {
											//Reset the counter
											counter = 8;
										}
									}
								}
								break;
							}

						}
					}
					//TODO:: Does this remove the post-amble? "rawData.size() - 4"; if so should this be "rawData.size() - 8"?
					for (int i = rawData.size() - 8; i < rawData.size(); i++) {
						rawData.remove(i);
					}
					rawData.add(-1);

					testData.put(testNum, rawData);		

					outputStream.write(pullLow);

					break;

				}  
			}
			//Method successful, return the map of test data
			return testData;
		}
		//Method failed, return null
		return null;
	}




	public BufferedInputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public boolean streamsInitialized() {
		return dataStreamsInitialized;
	}


}
