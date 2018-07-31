package dataorganizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;

import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.JCheckBox;
import java.awt.FlowLayout;
import javax.swing.JProgressBar;

public class EducatorMode extends JFrame {


	//Test Parameter Variables and Constants
	public static final int NUM_TEST_PARAMETERS = 13;
	public static final int NUM_ID_INFO_PARAMETERS = 3;
	public static final int CURRENT_FIRMWARE_ID = 23;

	private JPanel contentPane;
	private JComboBox gyroSensitivityCombobox;
	private JComboBox gyroFilterCombobox;
	private JCheckBox timedTestCheckbox;
	private JProgressBar progressBar;
	private JComboBox testTypeCombobox;
	private JButton applyConfigurationsBtn;
	private JButton nextBtnOne;
	private JButton backBtnThree;
	private JButton nextBtnThree;
	private JButton readTestBtn;
	private JButton pairNewRemoteButton;
	private JButton unpairAllRemotesButton;
	private JButton testRemotesButton;
	private JButton exitTestModeButton;
	private ButtonGroup group;
	private static SerialComm serialHandler;
	private Integer wIndex = 1; //Lol windex
	private JPanel testTakingPanel;
	private JPanel stepOne;
	private JPanel stepTwo;
	private JPanel stepThree;
	private JPanel stepFour;
	private JLabel generalStatusLabelOne;
	private JLabel generalStatusLabelTwo;
	private JLabel generalStatusLabelThree;
	private JLabel generalStatusLabelFive;

	private HashMap<String, ArrayList<Integer>> testTypeHashMap = new HashMap<String, ArrayList<Integer>>();
	private String testType;
	private JPanel stepFive;
	private JPanel navPanel;
	private JButton backBtnFive;
	private JButton nextBtnFive;
	private JButton eraseBtn;
	private JButton noBtn;
	private JButton btnLaunchMotionVisualization;
	private JPanel panel_5;
	private JButton configForCalButton;
	private JPanel videoBrowsePanel;
	private JTextField videoFilePathTextField;
	private JButton btnBrowse;
	private JButton importCalDataButton;
	private JPanel calOffsetsPanel;
	private JTextField tmr0OffsetTextField;
	private JTextField delayAfterTextField;
	private JButton applyOffsetButton;
	private JLabel generalStatusLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		//Set the look and feel to whatever the system default is.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch(Exception e) {
			System.out.println("Error Setting Look and Feel: " + e);
		}


		serialHandler = new SerialComm();
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
			return 3813;
		}
	}

	public void fillTestTypeHashMap(int timedTest) {
		ArrayList<Integer> testParams = new ArrayList<Integer>();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(4);
		//10 Gyro Sensitivity
		testParams.add(1000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Conservation of Momentum (Elastic Collision)", testParams);

		testParams.clear();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(4);
		//10 Gyro Sensitivity
		testParams.add(2000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Conservation of Angular Momentum", testParams);

		testParams.clear();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(16);
		//10 Gyro Sensitivity
		testParams.add(2000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Conservation of Energy", testParams);

		testParams.clear();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(4);
		//10 Gyro Sensitivity
		testParams.add(1000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Inclined Plane", testParams);

		testParams.clear();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(8);
		//10 Gyro Sensitivity
		testParams.add(2000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Physical Pendulum", testParams);

		testParams.clear();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(8);
		//10 Gyro Sensitivity
		testParams.add(1000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Spring Test - Simple Harmonics", testParams);

		testParams.clear();
	}

	public void importCalDataHandler() {
		Runnable getConfigsOperation = new Runnable() {
			public void run() {	
				configForCalButton.setEnabled(false);
				importCalDataButton.setEnabled(false);
				applyOffsetButton.setEnabled(false);
				try {

					BlackFrameAnalysis bfo = new BlackFrameAnalysis(videoFilePathTextField.getText());

					delayAfterTextField.setText(Integer.toString(bfo.getDelayAfterStart()));
					tmr0OffsetTextField.setText(Integer.toString(bfo.getTMR0Offset()));

					configForCalButton.setEnabled(true);
					importCalDataButton.setEnabled(true);
					applyOffsetButton.setEnabled(true);

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
	
	public void configForCalHandler() {
		Runnable calforConfigOperation = new Runnable() {
			public void run() {
				configForCalButton.setEnabled(false);
				importCalDataButton.setEnabled(true);
				applyOffsetButton.setEnabled(false);

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

				generalStatusLabelTwo.setText("Module Listening for New Remote, Hold 'A' or 'B' Button to Pair");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					if(serialHandler.pairNewRemote()) {
						generalStatusLabelTwo.setText("New Remote Successfully Paired");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}
					else {
						generalStatusLabelTwo.setText("Pair Unsuccessful, Receiver Timed Out");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}


				}
				catch (IOException e) {
					generalStatusLabelTwo.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabelTwo.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelTwo.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

				//Enable buttons that can now be used since the bulk erase completed
				pairNewRemoteButton.setEnabled(true);
				unpairAllRemotesButton.setEnabled(true);
				testRemotesButton.setEnabled(true);


			}
		};

		//Define a new thread to run the operation previously defined
		Thread pairNewRemoteThread = new Thread(pairNewRemoteOperation);
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

				generalStatusLabelTwo.setText("Unpairing all Remotes...");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					serialHandler.unpairAllRemotes();
				}
				catch (IOException e) {
					generalStatusLabelTwo.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabelTwo.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelTwo.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}


				pairNewRemoteButton.setEnabled(true);
				unpairAllRemotesButton.setEnabled(true);
				testRemotesButton.setEnabled(true);

				generalStatusLabelTwo.setText("All Remotes Unpaired, There are 0 Remotes Paired to this Module");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));
			}
		};

		//Define a new thread to run the operation previously defined
		Thread unpairAllRemotesThread = new Thread(unpairAllRemotesOperation);
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


				//Notify the user that the bulk erase sequence has began
				generalStatusLabelTwo.setText("Press a Button on a Remote to Test if it is Paired");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					if(!serialHandler.testRemotes(generalStatusLabelTwo)) {
						generalStatusLabelTwo.setText("Error Communicating with Module");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
				}
				catch (IOException e) {
					generalStatusLabelTwo.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabelTwo.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelTwo.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

				//Enable button
				pairNewRemoteButton.setEnabled(true);
				unpairAllRemotesButton.setEnabled(true);
				testRemotesButton.setEnabled(true);
				exitTestModeButton.setEnabled(false);

				//Notify the user that the sequence has completed
				generalStatusLabelTwo.setText("Test Mode Successfully Exited");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(51, 204, 51));
			}
		};

		//Define a new thread to run the operation previously defined
		Thread testRemoteThread = new Thread(testRemoteOperation);
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

	public String getSelectedButtonText(ButtonGroup buttonGroup) {
		for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();

			if (button.isSelected()) {
				return button.getText();
			}
		}

		return null;
	}

	public void updateBywIndex(Integer index) {
		testTakingPanel.removeAll();
		switch(index) {
		case 1:
			testTakingPanel.add(stepOne);
		case 2:
			testTakingPanel.add(stepTwo);
		case 3:
			testTakingPanel.add(stepThree);
		case 4:
			testTakingPanel.add(stepFour);
		case 5:
			testTakingPanel.add(stepFive);
		default:
			if(index>5) {
				testTakingPanel.add(stepOne);
				wIndex = 1;
				repaint();
			}
		}
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
				eraseBtn.setEnabled(false);
				//Notify the user that the bulk erase sequence has began
				generalStatusLabelFive.setText("Sector Erasing...");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					if(serialHandler.sectorEraseModule()) {
						//Notify the user that the sequence has completed
						generalStatusLabelFive.setText("Sector Erase Complete");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}
					else {

						//Notify the user that the sequence has failed
						generalStatusLabelFive.setText("Sector Erase Failed");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					//Enable buttons that can now be used since the sector erase completed
					eraseBtn.setEnabled(true);
				}
				catch (IOException e) {
					generalStatusLabelFive.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabelFive.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelFive.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};

		//Define a new thread to run the operation previously defined
		Thread sectorEraseThread = new Thread(sectorEraseOperation);
		//Start the thread
		sectorEraseThread.start();
	}


	public boolean findModuleCommPort() {
		class threadHack{
			private boolean status = false;

			public boolean getStatus(){
				return status;
			}
			public void setStatus(boolean x) {
				status = x;
			}
		}
		final threadHack th = new threadHack();

		Runnable findModuleOperation = new Runnable() {
			public void run() {
				try {
					ArrayList<String> commPortIDList = serialHandler.findPorts();
					boolean moduleFound = false;
					int commPortIndex = 0;
					while (!moduleFound && commPortIndex < commPortIDList.size()) {

						//Get the string identifier (name) of the current port
						String selectedCommID = commPortIDList.toArray()[commPortIndex].toString();      

						//Open the serial port with the selected name, initialize input and output streams, set necessary flags so the whole program know that everything is initialized
						if(serialHandler.openSerialPort(selectedCommID)){

							int attemptCounter = 0;
							while (attemptCounter < 3 && !moduleFound) {
								try {
									ArrayList<Integer> moduleIDInfo = serialHandler.getModuleInfo(NUM_ID_INFO_PARAMETERS);

									if (moduleIDInfo != null) {
										moduleFound = true;

										if (moduleIDInfo.get(2) != CURRENT_FIRMWARE_ID) {
											generalStatusLabelOne.setText("Incompatable Firmware Version: " + moduleIDInfo.get(2) + ", Program Module with Version " + CURRENT_FIRMWARE_ID);
										}
										else {
											generalStatusLabelOne.setText("Successfully Connected to Module");
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
						generalStatusLabelOne.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
						th.setStatus(false);
					}

				}
				catch (IOException e) {
					generalStatusLabelOne.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
					th.setStatus(false);
				}
				catch (PortInUseException e) {
					generalStatusLabelOne.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
					th.setStatus(false);
				}
			}
		};
		Thread findModuleThread = new Thread(findModuleOperation);
		findModuleThread.run();
		return th.getStatus();
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
	 * Handles the button press of read data from module button. This includes reading the test parameters, updating the read gui, and reading/converting the data to .csv. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume. See the method calls within the runnable for more info
	 * on what this handler actually does.
	 */
	public void readButtonHandler() {
		//Define operation that can be run in separate thread
		Runnable readOperation = new Runnable() {
			public void run() {
				//Disable read button while read is in progress
				backBtnThree.setEnabled(false);
				nextBtnThree.setEnabled(false);
				readTestBtn.setEnabled(false);

				try {
					generalStatusLabelThree.setText("Reading Data from Module...");

					//Read test parameters from module and store it in testParameters
					ArrayList<Integer> testParameters = serialHandler.readTestParams(NUM_TEST_PARAMETERS);

					//Executes if the reading of the test parameters was successful
					if (testParameters != null) {

						int expectedTestNum = testParameters.get(0);

						//Assign local variables to their newly received values from the module
						int timedTestFlag = testParameters.get(4);
						//Trigger on release is 8
						int testLength = testParameters.get(6);
						int accelGyroSampleRate = testParameters.get(7);
						int magSampleRate = testParameters.get(8);
						int accelSensitivity = testParameters.get(9);
						int gyroSensitivity = testParameters.get(10);
						int accelFilter = testParameters.get(11);
						int gyroFilter = testParameters.get(12);


						boolean timedTest = true;
						if (timedTestFlag == 0) {
							timedTest = false;
						}
						double bytesPerSample = 18;
						if (accelGyroSampleRate / magSampleRate == 10) {
							bytesPerSample = 12.6;
						}



						String nameOfFile = "";

						//Executes if there are tests on the module
						if(expectedTestNum > 0) { 

							//Get date for file name
							Date date = new Date();

							//Assign file name
							nameOfFile += (" " + accelGyroSampleRate + "-" + magSampleRate + " " + accelSensitivity + "G-" + accelFilter + " " + gyroSensitivity + "dps-" + gyroFilter + " MAG-N " + date.getDate() + getMonth(date.getMonth()) + (date.getYear() - 100) + ".csv");

							HashMap<Integer, ArrayList<Integer>> testData;

							//Store the test data from the dashboard passing in enough info that the progress bar will be accurately updated
							testData = serialHandler.readTestData(expectedTestNum, progressBar, generalStatusLabelThree);

							generalStatusLabelThree.setText("All Data Received from Module");

							//Executes if the data was received properly (null = fail)
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
									String tempName = "(#" + (testIndex+1) + ") " + nameOfFile; 
									//Define operation that can be run in separate thread
									Settings settings = new Settings();
									settings.loadConfigFile();
									Runnable organizerOperation = new Runnable() {
										public void run() {
											//Organize data into .CSV
											CSVBuilder.sortData(finalData, tempName, (accelGyroSampleRate / magSampleRate), settings.getKeyVal("CSVSaveLocation"), (getSelectedButtonText(group) == "Data (Excel)"), (timedTestFlag==1), testParameters); 
										}
									};

									//Set thread to execute previously defined operation
									Thread organizerThread = new Thread(organizerOperation);
									//Start thread
									organizerThread.start();
								}
							}
							else {
								generalStatusLabelThree.setText("Error Reading From Module, Try Again");
								progressBar.setValue(100);
								progressBar.setForeground(new Color(255, 0, 0));
							}
						}
						else {
							generalStatusLabelThree.setText("No Tests Found on Module");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(255, 0, 0));
						}
					}
					else {
						generalStatusLabelThree.setText("Error Reading From Module, Try Again");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}

				}

				catch (IOException e) {
					generalStatusLabelThree.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabelThree.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelThree.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

				//Re-enable read button upon read completion
				backBtnThree.setEnabled(true);
				nextBtnThree.setEnabled(true);
				readTestBtn.setEnabled(true);
			}
		};

		//Set thread to execute previously defined operation
		Thread readThread = new Thread(readOperation);
		//Start thread
		readThread.start();

	}

	/**
	 * Handles the button press of the write configuration button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume. See the method calls within the runnable for more info
	 * on what this handler actually does.
	 */
	private void writeButtonHandler() {
		//Define no operation that can be run in a thread
		Runnable sendParamOperation = new Runnable() {
			public void run() {
				//Disable write config button while the sendParameters() method is running
				applyConfigurationsBtn.setEnabled(false);
				nextBtnOne.setEnabled(false);
				if(findModuleCommPort()) {
					generalStatusLabelOne.setText("Initial connection to module successful");
				}
				try {
					if(!serialHandler.sendTestParams(testTypeHashMap.get(testTypeCombobox.getSelectedItem().toString()))) {
						generalStatusLabelOne.setText("Module Not Responding, parameter write failed.");
					}
					else {
						generalStatusLabelOne.setText("Module Configuration Successful, Parameters Have Been Updated");
					}
				}
				catch (NumberFormatException e) {
					generalStatusLabelOne.setText("Please Fill out Every Field");
				}
				catch (IOException e) {
					generalStatusLabelOne.setText("Error Communicating With Serial Dongle");
				}
				catch (PortInUseException e) {
					generalStatusLabelOne.setText("Serial Port Already In Use");
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelOne.setText("Check Dongle Compatability");
				}

				//Re-enable the write config button when the routine has completed
				applyConfigurationsBtn.setEnabled(true);
				nextBtnOne.setEnabled(true);
			}
		};


		//Assign new operation to a thread so that it can be run in the background
		Thread paramThread = new Thread(sendParamOperation);
		//Start the new thread
		paramThread.start();
	}


	/**
	 * Create the frame.
	 */
	public EducatorMode() {
		initComponents();
		findModuleCommPort();
	}

	public void initComponents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 688, 560);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel mainPanel = new JPanel();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		mainPanel.add(mainTabbedPane);

		testTakingPanel = new JPanel();
		mainTabbedPane.addTab("Run Experiment", null, testTakingPanel, null);
		mainTabbedPane.setEnabledAt(0, true);
		testTakingPanel.setLayout(new CardLayout(0, 0));

		stepOne = new JPanel();
		testTakingPanel.add(stepOne, "stepOne");
		stepOne.setLayout(null);

		testTypeCombobox = new JComboBox();
		testTypeCombobox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				fillTestTypeHashMap(timedTestCheckbox.isSelected()?1:0);
				testType = testTypeCombobox.getSelectedItem().toString();
				testTypeHashMap.get(testTypeCombobox.getSelectedItem());
			}
		});

		testTypeCombobox.setBounds(10, 27, 506, 26);
		stepOne.add(testTypeCombobox);
		testTypeCombobox.setModel(new DefaultComboBoxModel(new String[] {"Conservation of Momentum (Elastic Collision)", "Conservation of Angular Momentum", "Conservation of Energy", "Inclined Plane", "Physical Pendulum", "Spring Test - Simple Harmonics"}));

		applyConfigurationsBtn = new JButton("Apply Configurations");
		applyConfigurationsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeButtonHandler();
			}
		});
		applyConfigurationsBtn.setBounds(10, 348, 506, 39);
		stepOne.add(applyConfigurationsBtn);

		JPanel navPanelOne = new JPanel();
		navPanelOne.setBounds(0, 398, 538, 108);
		stepOne.add(navPanelOne);
		navPanelOne.setLayout(null);

		generalStatusLabelOne = new JLabel("");
		generalStatusLabelOne.setHorizontalAlignment(SwingConstants.CENTER);
		generalStatusLabelOne.setBounds(22, 37, 365, 26);
		navPanelOne.add(generalStatusLabelOne);

		nextBtnOne = new JButton("Next");
		nextBtnOne.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateBywIndex((wIndex += 1));
			}
		});
		nextBtnOne.setBounds(415, 0, 93, 88);
		navPanelOne.add(nextBtnOne);

		timedTestCheckbox = new JCheckBox("Timed Test");
		timedTestCheckbox.setBounds(0, 0, 97, 23);
		navPanelOne.add(timedTestCheckbox);

		noBtn = new JButton("New button");
		noBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new SettingsWindow().setVisible(true);
			}
		});
		noBtn.setBounds(576, 11, 32, 16);
		stepOne.add(noBtn);

		stepTwo = new JPanel();
		testTakingPanel.add(stepTwo, "name_92124154026185");
		stepTwo.setLayout(new GridLayout(5, 1, 0, 0));

		pairNewRemoteButton = new JButton("Pair New Remote");
		pairNewRemoteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pairNewRemoteHandler();
			}
		});
		pairNewRemoteButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		stepTwo.add(pairNewRemoteButton);

		unpairAllRemotesButton = new JButton("Unpair All Remotes");
		unpairAllRemotesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unpairAllRemotesHandler();
			}
		});
		unpairAllRemotesButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		stepTwo.add(unpairAllRemotesButton);

		testRemotesButton = new JButton("Test Paired Remotes");
		testRemotesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testRemotesHandler();
			}
		});
		testRemotesButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		stepTwo.add(testRemotesButton);

		exitTestModeButton = new JButton("Exit Test Mode");
		exitTestModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitTestModeHandler();
			}
		});
		exitTestModeButton.setEnabled(false);
		exitTestModeButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		stepTwo.add(exitTestModeButton);

		JPanel navPanelTwo = new JPanel();
		stepTwo.add(navPanelTwo);
		navPanelTwo.setLayout(null);

		generalStatusLabelTwo = new JLabel("");
		generalStatusLabelTwo.setHorizontalAlignment(SwingConstants.CENTER);
		generalStatusLabelTwo.setBounds(113, 33, 292, 24);
		navPanelTwo.add(generalStatusLabelTwo);

		JButton nextBtnTwo = new JButton("Next");
		nextBtnTwo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex += 1));
			}
		});
		nextBtnTwo.setBounds(415, 0, 93, 88);
		navPanelTwo.add(nextBtnTwo);

		JButton button = new JButton("Back");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex -= 1));
			}
		});
		button.setBounds(10, 0, 93, 88);
		navPanelTwo.add(button);

		stepThree = new JPanel();
		testTakingPanel.add(stepThree, "name_92908667926372");
		stepThree.setLayout(null);

		JPanel outputPanel = new JPanel();
		outputPanel.setBounds(0, 0, 548, 302);
		stepThree.add(outputPanel);
		outputPanel.setLayout(null);

		group = new ButtonGroup();

		JRadioButton dataExcelRadioBtn = new JRadioButton("Data (Spreadsheet)");
		dataExcelRadioBtn.setHorizontalAlignment(SwingConstants.CENTER);
		dataExcelRadioBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		dataExcelRadioBtn.setBounds(6, 5, 536, 50);
		outputPanel.add(dataExcelRadioBtn);

		group.add(dataExcelRadioBtn);

		JRadioButton motionVisualizationRadioBtn = new JRadioButton("Motion Visualization");
		motionVisualizationRadioBtn.setBounds(147, 145, 269, 36);
		outputPanel.add(motionVisualizationRadioBtn);
		motionVisualizationRadioBtn.setHorizontalAlignment(SwingConstants.CENTER);
		motionVisualizationRadioBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		group.add(motionVisualizationRadioBtn);

		JRadioButton graphRadioBtn = new JRadioButton("Graph (Using template)");
		graphRadioBtn.setBounds(143, 58, 268, 48);
		outputPanel.add(graphRadioBtn);
		graphRadioBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		graphRadioBtn.setHorizontalAlignment(SwingConstants.CENTER);
		graphRadioBtn.setEnabled(false);
		group.add(graphRadioBtn);

		JRadioButton rdbtnBothgraphingAnd = new JRadioButton("Both (Graphing and Spreadsheet output)");
		rdbtnBothgraphingAnd.setEnabled(false);
		rdbtnBothgraphingAnd.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnBothgraphingAnd.setBounds(112, 114, 332, 23);
		outputPanel.add(rdbtnBothgraphingAnd);

		readTestBtn = new JButton("Read Test");
		readTestBtn.setBounds(0, 303, 548, 101);
		readTestBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readButtonHandler();
			}
		});
		readTestBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		stepThree.add(readTestBtn);

		JPanel navPanelThree = new JPanel();
		navPanelThree.setBounds(0, 404, 548, 101);
		stepThree.add(navPanelThree);
		navPanelThree.setLayout(null);

		generalStatusLabelThree = new JLabel("");
		generalStatusLabelThree.setHorizontalAlignment(SwingConstants.CENTER);
		generalStatusLabelThree.setBounds(128, 63, 281, 25);
		navPanelThree.add(generalStatusLabelThree);

		backBtnThree = new JButton("Back");
		backBtnThree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex -= 1));
			}
		});
		backBtnThree.setBounds(10, 0, 93, 88);
		navPanelThree.add(backBtnThree);

		nextBtnThree = new JButton("Next");
		nextBtnThree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex += 1));
			}
		});
		nextBtnThree.setBounds(415, 0, 93, 88);
		navPanelThree.add(nextBtnThree);

		progressBar = new JProgressBar();
		progressBar.setBounds(128, 38, 281, 14);
		navPanelThree.add(progressBar);

		stepFour = new JPanel();
		testTakingPanel.add(stepFour, "name_96253525137854");
		stepFour.setLayout(new GridLayout(5, 1, 0, 0));

		panel_5 = new JPanel();
		stepFour.add(panel_5);

		JPanel panel_4 = new JPanel();
		stepFour.add(panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));

		btnLaunchMotionVisualization = new JButton("Launch Motion Visualization");
		btnLaunchMotionVisualization.setFont(new Font("Tahoma", Font.PLAIN, 40));
		panel_4.add(btnLaunchMotionVisualization);

		JPanel panel_3 = new JPanel();
		stepFour.add(panel_3);

		JPanel panel_2 = new JPanel();
		stepFour.add(panel_2);

		JPanel navPanelFour = new JPanel();
		stepFour.add(navPanelFour);
		navPanelFour.setLayout(null);

		JButton backBtnFour = new JButton("Back");
		backBtnFour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex -= 1));
			}
		});
		backBtnFour.setBounds(10, 0, 93, 88);
		navPanelFour.add(backBtnFour);

		JButton nextBtnFour = new JButton("Next");
		nextBtnFour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex += 1));
			}
		});
		nextBtnFour.setBounds(415, 0, 93, 88);
		navPanelFour.add(nextBtnFour);

		stepFive = new JPanel();
		testTakingPanel.add(stepFive, "name_116075093988858");
		stepFive.setLayout(null);

		eraseBtn = new JButton("Erase");
		eraseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sectorEraseHandler();
			}
		});

		navPanel = new JPanel();
		navPanel.setBounds(0, 406, 548, 101);
		stepFive.add(navPanel);
		navPanel.setLayout(null);

		backBtnFive = new JButton("Back");
		backBtnFive.setBounds(10, 0, 93, 88);
		backBtnFive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex -= 1));
			}
		});
		navPanel.add(backBtnFive);

		nextBtnFive = new JButton("Next");
		nextBtnFive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex += 1));
			}
		});
		nextBtnFive.setBounds(415, 0, 93, 88);
		navPanel.add(nextBtnFive);

		generalStatusLabelFive = new JLabel("");
		generalStatusLabelFive.setHorizontalAlignment(SwingConstants.CENTER);
		generalStatusLabelFive.setBounds(113, 30, 293, 28);
		navPanel.add(generalStatusLabelFive);
		eraseBtn.setFont(new Font("Tahoma", Font.PLAIN, 24));
		eraseBtn.setBounds(10, 11, 498, 394);
		stepFive.add(eraseBtn);

		JPanel calibrationPanel = new JPanel();
		mainTabbedPane.addTab("Calibration Panel", null, calibrationPanel, null);
		calibrationPanel.setLayout(new GridLayout(6, 1, 0, 0));
		
		configForCalButton = new JButton("Configure Module for Calibration");
		configForCalButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				configForCalHandler();
			}
		});
		configForCalButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		calibrationPanel.add(configForCalButton);
		
		videoBrowsePanel = new JPanel();
		calibrationPanel.add(videoBrowsePanel);
		videoBrowsePanel.setLayout(null);
		
		videoFilePathTextField = new JTextField();
		videoFilePathTextField.setBounds(0, 0, 444, 84);
		videoFilePathTextField.setBorder(new TitledBorder(null, "File Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		videoBrowsePanel.add(videoFilePathTextField);
		videoFilePathTextField.setColumns(10);
		
		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				videoBrowseButtonHandler();
			}
		});
		btnBrowse.setBounds(443, 0, 105, 84);
		videoBrowsePanel.add(btnBrowse);
		
		importCalDataButton = new JButton("Import calibration data and calculate offset");
		importCalDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				importCalDataHandler();
			}
		});
		importCalDataButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		calibrationPanel.add(importCalDataButton);
		
		calOffsetsPanel = new JPanel();
		calibrationPanel.add(calOffsetsPanel);
		calOffsetsPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		tmr0OffsetTextField = new JTextField();
		tmr0OffsetTextField.setBorder(new TitledBorder(null, "Timer0 Calibration Offset (Ticks)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		calOffsetsPanel.add(tmr0OffsetTextField);
		tmr0OffsetTextField.setColumns(10);
		
		delayAfterTextField = new JTextField();
		delayAfterTextField.setBorder(new TitledBorder(null, "Delay After Start (milliseconds)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		calOffsetsPanel.add(delayAfterTextField);
		delayAfterTextField.setColumns(10);
		
		applyOffsetButton = new JButton("Apply Offset");
		applyOffsetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyOffsetsHandler();
			}
		});
		applyOffsetButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		calibrationPanel.add(applyOffsetButton);
		
		generalStatusLabel = new JLabel("");
		calibrationPanel.add(generalStatusLabel);

		JPanel motionVisualizationPanel = new JPanel();
		mainTabbedPane.addTab("Motion Visualization", null, motionVisualizationPanel, null);
	}
}
