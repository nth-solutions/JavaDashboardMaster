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
import javax.swing.JProgressBar;

import com.sun.javafx.collections.MappingChange.Map;

import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.PureJavaIllegalStateException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

public class SerialComm {
	private JFrame gui;

	private BufferedInputStream inputStream;        //Object used for reading serial data 
	private OutputStream outputStream;              //Object used for writing serial data
	private CommPortIdentifier portId;       		//Object used for opening a COMM ports
	private SerialPort serialPort;      			//Object for the serial port class

	private boolean frameInitialized = false;
	private boolean portOpened = false;
	private boolean dataStreamsInitialized = false;

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

		//Set flag lets the program know that the port opened successfully
		portOpened = true;

		//Create a new buffered reader so we can define the buffer size to prevent a buffer overflow (explicitly defined in the configureForImport() method)
		inputStream = new BufferedInputStream(serialPort.getInputStream(), 8192);

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

			//Clear program flags associated with the COMM port's status
			portOpened = false;
			dataStreamsInitialized = false;

			return true;

		}

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

		return false;
	}

	/**
	 * Configures the serial port and input/output streams for the handshake sequences (most important parameter is the baud rate)
	 * @return boolean that allows for easy exiting of the method in addition to notifying the caller that if it was successful
	 */
	public boolean configureForHandshake() throws IOException, PortInUseException, UnsupportedCommOperationException {
		//Close the current serial port if it is open (Must be done for dashboard to work properly for some reason, do not delete)
		if (dataStreamsInitialized) {
			serialPort.close();
		}

		//Reopen serial port
		openSerialPort(serialPort.getName());


		//Configure the serial port for 38400 baud for low speed handshakes
		serialPort.setSerialPortParams(38400,      
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		//Assign the output stream variable to the serial port's output stream
		outputStream = serialPort.getOutputStream();
		//Assign the input stream variable to the serial port's input stream via a buffered reader so we have the option to specify the buffer size
		inputStream = new BufferedInputStream(serialPort.getInputStream(), 8192);
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


		//Configure the serial port for 115200 baud for high speed exports
		serialPort.setSerialPortParams(115200,      
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		//Assign the output stream variable to the serial port's output stream
		outputStream = serialPort.getOutputStream();
		//Assign the input stream variable to the serial port's input stream via a buffered reader so we have the option to specify the buffer size
		inputStream = new BufferedInputStream(serialPort.getInputStream(), 725760);
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
	public boolean waitForPreamble(int start, int stop) throws IOException {
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
	public boolean waitForPostamble(int start, int stop) throws IOException {
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
			return false;
		}

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
			while((System.currentTimeMillis() - startTime) < 100) {
				//Executes if data is in the input stream buffer
				if (dataStreamsInitialized) {
					if (inputStream.available() > 0) {
						//Assign the newly received byte to a temp variable then break the while loop
						temp = inputStream.read();
						break;
					}	
				}
				else {
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
		waitForPostamble(4 , 1);


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
		waitForPostamble(4 , 1);


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

	public boolean pairNewRemote() throws IOException, PortInUseException, UnsupportedCommOperationException{

		if(!selectMode('+')) {
			return false;
		}
		waitForPostamble(4,1);
		if (inputStream.available() > 0) {
			int ackValue = inputStream.read();
			if (!(ackValue == (int)'A')) {
				return false;
			}
		}

		return true;
	}

	public boolean unpairAllRemotes() throws IOException, PortInUseException, UnsupportedCommOperationException {
		if(!selectMode('-')) {
			return false;
		}
		waitForPostamble(4,1);
		return true;
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

			//Initialize ID index to 0
			int idCounter = 0;
			//Initialize temporary ID parameter array
			ArrayList<Integer> moduleInfo = new ArrayList<Integer>();

			//Initialize start time so timeout can be used on subsequent while loop
			long startTime = System.currentTimeMillis();

			//Executes while it is still receiving ID info and a timeout has not occured
			while (idCounter < numIDParams && (System.currentTimeMillis() - startTime) < 1500) {

				//Wait for a preamble, exits method if the preamble times out
				if(!waitForPreamble(1,4)) {
					return null;
				}

				//Executes if data has been received from the module
				if (inputStream.available() >= 2) {
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

	public ArrayList<Integer> readTestParams() throws IOException, PortInUseException, UnsupportedCommOperationException {
		int expectedTestNum;

		//Put module into export test data mode, exit method if that routine fails
		if(!selectMode('G')) {
			return null;
		}


		configureForHandshake();

		if (dataStreamsInitialized) {

			boolean dataReceived = false;

			//Initialize start time so timeout can be used
			long paramListenerStartTime = System.currentTimeMillis();

			//Loops until internally exited with break or timeout occurs
			while ((System.currentTimeMillis() - paramListenerStartTime) < 5000) {

				//Executes if data is received from module
				if (inputStream.available() > 0) {                            


					//Check for test parameter preamble
					waitForPreamble(1,4);

					//Determine number of tests to expect/ get test parameters
					expectedTestNum = -1;
					while(expectedTestNum == -1) {
						if(inputStream.available() > 0) {
							expectedTestNum = inputStream.read();
						}

					}


					ArrayList<Integer> rawParamData = new ArrayList<Integer>();

					//Looks for stop condition (4321)
					for(int counter = 4; counter >= 1;) {
						if (inputStream.available() > 0) {
							//Store newly read byte in temp variable
							int temp = inputStream.read();
							//Add newly read byte to test data arraylist
							rawParamData.add(temp);

							//Executes if the temp == the counter (meaning this byte could possibly be the stop condition)
							if (temp == counter) {  
								counter--;
							} 

							else {
								//Reset stop condition counter
								counter = 4;
							}
						}

					}


					//Initialize arraylists to store test params and test data
					ArrayList<Integer> testParameters = new ArrayList<Integer>();  


					testParameters.add(expectedTestNum);
					for (int paramIndex = 0; paramIndex < rawParamData.size() - 4; paramIndex += 2) {
						testParameters.add(rawParamData.get(paramIndex) * 256 + rawParamData.get(paramIndex + 1));
					}

					return testParameters;

				}
			}
		}
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
			double progress = 0;
			double dataProgressPartition = 0;
			
			if (timedTestFlag) {
				dataProgressPartition = (1 / (double)expectedBytes) * (1 / (double)expectedTestNum);
			}
			//Loops until it all of the tests are collected
			for (int testNum = 0; testNum < expectedTestNum; testNum++) {
				
				testData.put(testNum, new ArrayList<Integer>());

				//Wait for start condition (preamble)
				waitForPreamble(1,8);
				long startTime = System.currentTimeMillis();
				
				//Executes while the stop condition has not been received (Main loop that actually stores testing data)
				while (true) {    
					
					//Looks for stop condition (87654321)
					for(int counter = 8; counter >= 1;) {
						
						//Executes if data was received in the serial buffer
						if (inputStream.available() >= 0) {
						
							//Store newly read byte in temp variable
							int temp = inputStream.read();

							

							progress += dataProgressPartition;
							progressBar.setValue((int)(progress * 100));

							//Add newly read byte to test data arraylist
							testData.get(testNum).add(temp);
							
							//Executes if the temp == the counter (meaning this byte could possibly be the stop condition)
							if (temp == counter) { 
								if (counter == 1) {
									break;
								}
								else {
									counter--;
								}
							} 
							
							else {
								//Reset stop condition counter
								counter = 8;
							}
							
							//Reset data timeout counter
							startTime = System.currentTimeMillis();
						}
						
						
						//Executes if no data was received in the serial buffer for a predefined number of cycles
						else if (System.currentTimeMillis() - startTime >= 100 && inputStream.available() <= 0){
							configureForHandshake();
							outputStream.write("1234M".getBytes());
							configureForImport();
							while (inputStream.available() <= 0) {
							}
							//Reset data timeout counter
							startTime = System.currentTimeMillis();
						}
								

					}
					
					//Executes if there were atleast 8 data points received (used to prevent range bounds error)
					if (testData.get(testNum).size() >= 8) {    
						//Set the first value in the stop condition to -1 so the next section of this method knows where the end of testing data is
						testData.get(testNum).set(testData.get(testNum).size() - 8, -1);   
						break;
					}
				}         
			}
			return testData;

		}
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
