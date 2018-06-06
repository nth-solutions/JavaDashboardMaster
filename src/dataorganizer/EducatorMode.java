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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.DefaultComboBoxModel;

public class EducatorMode extends JFrame {

	private JPanel contentPane;
	private JProgressBar progressBar;
	private JLabel generalStatusLabel;
	private JButton disconnectButton;
	private JComboBox testTypeComboBox;
	private JComboBox commPortCombobox;
	
	//Flags
		private boolean readMode = true;
		private boolean organizeAbort = false;
		private boolean readAbort = false;
		private boolean paramAbort = false;
		private boolean portInitialized = false;
		private boolean frameInitialized = false;
		private boolean portOpened = false;
		private boolean dataStreamsInitialized = false;
		
	//Serial Port Variables
		private SerialPort serialPort;      			//Object for the serial port class
		private static CommPortIdentifier portId;       //Object used for opening a comm ports
		private static Enumeration portList;            //Object used for finding comm ports
		private InputStream inputStream;                //Object used for reading serial data 
		private OutputStream outputStream;              //Object used for writing serial data
		
		public static final int NUM_TEST_PARAMETERS = 13;

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
	 * Create the frame.
	 */
	public EducatorMode() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Data Organizer Rev-3 (Educator/StudentMode)(5/31/2018)(Under Construction)");
		setBounds(100, 100, 521, 280);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(500, 150));
		contentPane.add(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		panel_1.setLayout(new GridLayout(1, 3, 0, 0));
		
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
		panel_3.add(programBtn);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setForeground(Color.WHITE);
		contentPane.add(separator_1);
		
		JButton settingsBtn = new JButton("...");
		contentPane.add(settingsBtn);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(500, 20));
		contentPane.add(progressBar);
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

				int[] writeData = new int[NUM_TEST_PARAMETERS];
				switch(testTypeComboBox.getSelectedItem().toString()) {
					case "Conservation of Momentum":
						writeData[0] = 0;			//Serial Number
						writeData[1] = 5;			//Module ID (Hardware Version)
						writeData[2] = 16;			//Firmware ID 
						writeData[3] = getTickThreshold(960);		//Timer0 Tick Threshold (Interrupt)
						writeData[4] = 0;			//Delay After Start
						writeData[5] = 1;			//Timed Test Flag
						writeData[6] = 25;     //Test Duration
						writeData[7] = 960;//Accel Gyro Sample Rate
						writeData[8] = 96;    //Mag Sample Rate
						writeData[9] = 2;  //Accel Sensitivity
						writeData[10] = 250;   //Gyro Sensitivity
						writeData[11] = 5;  //Accel Filter
						writeData[12] = 10;  //Gyro Filter
						break;
					case "Pendulum":
						writeData[0] = 0;			//Serial Number
						writeData[1] = 5;			//Module ID (Hardware Version)
						writeData[2] = 16;			//Firmware ID
						writeData[3] = getTickThreshold(960);		//Timer0 Tick Threshold (Interrupt)
						writeData[4] = 0;			//Delay After Start
						writeData[5] = 1;			//Timed Test Flag
						writeData[6] = 25;     //Test Duration
						writeData[7] = 960;//Accel Gyro Sample Rate
						writeData[8] = 96;    //Mag Sample Rate
						writeData[9] = 2;  //Accel Sensitivity
						writeData[10] = 250;   //Gyro Sensitivity
						writeData[11] = 5;  //Accel Filter
						writeData[12] = 10;  //Gyro Filter
						break;
				}
				

				for (int paramNum = 0; paramNum < writeData.length; paramNum++) {
					boolean paramReceived = false;
					progressBar.setValue((int)(100 * ((double)paramNum / (double)writeData.length) / 1.2));
					int attemptCounter = 0;
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
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
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
}
