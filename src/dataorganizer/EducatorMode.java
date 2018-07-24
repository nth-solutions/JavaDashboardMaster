package dataorganizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.JComboBox;
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
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;

public class EducatorMode extends JFrame {

	private JPanel contentPane;
	private JTextField magnetometerSampleRateTextField;
	private JTextField testDurationTextField;
	private JTextField batteryTimeoutLengthTextField;
	private JTextField tmr0TickThreshTextField;
	private JTextField delayAfterStartTextField;
	private JComboBox testTypeCombobox;
	private static SerialComm serialHandler;
	private Integer wIndex = 0; //Lol windex
	private JPanel testTakingPanel;
	private JPanel stepOne;
	private JLabel generalStatusLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
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
			return 3848;
		}
	}

	public void updateInfoFields() {
		ArrayList<Integer> testParams = new ArrayList<Integer>();
		switch(testTypeCombobox.getSelectedIndex()) {	//Fill out fields
		case 0:
			//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
			testParams.add(0);
			//1 Timer0 Tick Threshold
			testParams.add(getTickThreshold(960));
			//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
			testParams.add(0);
			//3 Battery timeout flag
			testParams.add(300);
			//4 Timed test flag
			testParams.add(0);
			//5 Trigger on release flag
			testParams.add(1);
			//6 Test Length
			testParams.add(1);
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
		case 1: 
		case 2: 
		case 3:
		case 4:
		case 5:
		case 6:
		default:
		}

		try {
			serialHandler.sendTestParams(testParams);
		} 
		catch (IOException e) {
			generalStatusLabel.setText("Error Communicating With Serial Dongle");
		}
		catch (PortInUseException e) {
			generalStatusLabel.setText("Serial Port Already In Use");
		}
		catch (UnsupportedCommOperationException e) {
			generalStatusLabel.setText("Check Dongle Compatability");
		}
	}


	public void updateBywIndex(Integer index) {
		testTakingPanel.removeAll();
		switch(index) {
			case 0:
				testTakingPanel.add(stepOne);
		}
	}

	/**
	 * Create the frame.
	 */
	public EducatorMode() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 700, 500);
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
		testTypeCombobox.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
				updateInfoFields();
			}
		});
		testTypeCombobox.setBounds(10, 27, 506, 26);
		stepOne.add(testTypeCombobox);
		testTypeCombobox.setModel(new DefaultComboBoxModel(new String[] {"Conservation of Momentum (Elastic Collision)", "Convservation of Angular Momentum", "Convservation of Energy", "Inclined Plane", "Physical Pendulum", "Spring Test - Simple Harmonics"}));

		JButton applyConfigurationsBtn = new JButton("Apply Configurations");
		applyConfigurationsBtn.setBounds(10, 361, 506, 26);
		stepOne.add(applyConfigurationsBtn);

		JComboBox accelGyroSampleRateCombobox = new JComboBox();
		accelGyroSampleRateCombobox.setEnabled(false);
		accelGyroSampleRateCombobox.setModel(new DefaultComboBoxModel(new String[] {"60", "120", "240", "480", "500", "960"}));
		accelGyroSampleRateCombobox.setSelectedIndex(5);
		accelGyroSampleRateCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelGyroSampleRateCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accel/Gyro Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		accelGyroSampleRateCombobox.setBounds(10, 64, 250, 53);
		stepOne.add(accelGyroSampleRateCombobox);

		magnetometerSampleRateTextField = new JTextField();
		magnetometerSampleRateTextField.setEnabled(false);
		magnetometerSampleRateTextField.setToolTipText("Automatically updates based on Accel/Gyro Sample Rate. Type desired sample rate then press 'Enter'");
		magnetometerSampleRateTextField.setText("96");
		magnetometerSampleRateTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		magnetometerSampleRateTextField.setEditable(false);
		magnetometerSampleRateTextField.setColumns(10);
		magnetometerSampleRateTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Magnetometer Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		magnetometerSampleRateTextField.setBounds(259, 64, 257, 53);
		stepOne.add(magnetometerSampleRateTextField);

		JComboBox accelerometerSensitivityCombobox = new JComboBox();
		accelerometerSensitivityCombobox.setEnabled(false);
		accelerometerSensitivityCombobox.setModel(new DefaultComboBoxModel(new String[] {"2", "4", "8", "16"}));
		accelerometerSensitivityCombobox.setSelectedIndex(2);
		accelerometerSensitivityCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelerometerSensitivityCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accelerometer Sensitivity (G)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		accelerometerSensitivityCombobox.setBounds(10, 117, 250, 53);
		stepOne.add(accelerometerSensitivityCombobox);

		JComboBox gyroSensitivityComboBox = new JComboBox();
		gyroSensitivityComboBox.setEnabled(false);
		gyroSensitivityComboBox.setModel(new DefaultComboBoxModel(new String[] {"250", "500", "1000", "2000"}));
		gyroSensitivityComboBox.setSelectedIndex(2);
		gyroSensitivityComboBox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		gyroSensitivityComboBox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gyroscope Sensitivity (dps)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		gyroSensitivityComboBox.setBounds(259, 117, 257, 53);
		stepOne.add(gyroSensitivityComboBox);

		JComboBox accelFilterCombobox = new JComboBox();
		accelFilterCombobox.setEnabled(false);
		accelFilterCombobox.setModel(new DefaultComboBoxModel(new String[] {"5", "10", "20", "41", "92", "184", "460", "1130 (OFF)"}));
		accelFilterCombobox.setSelectedIndex(4);
		accelFilterCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelFilterCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accelerometer Filter (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		accelFilterCombobox.setBounds(10, 170, 250, 53);
		stepOne.add(accelFilterCombobox);

		JComboBox gyroscoprFilterCombobox = new JComboBox();
		gyroscoprFilterCombobox.setEnabled(false);
		gyroscoprFilterCombobox.setModel(new DefaultComboBoxModel(new String[] {"10", "20", "41", "92", "184", "250", "3600", "8800 (OFF)"}));
		gyroscoprFilterCombobox.setSelectedIndex(3);
		gyroscoprFilterCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		gyroscoprFilterCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gyroscope Filter (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		gyroscoprFilterCombobox.setBounds(259, 170, 257, 53);
		stepOne.add(gyroscoprFilterCombobox);

		testDurationTextField = new JTextField();
		testDurationTextField.setEnabled(false);
		testDurationTextField.setEditable(false);
		testDurationTextField.setToolTipText("Minimum of 2 seconds, maximum of 65535 seconds");
		testDurationTextField.setText("25");
		testDurationTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		testDurationTextField.setColumns(10);
		testDurationTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Test Duration (Seconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		testDurationTextField.setBounds(10, 223, 250, 53);
		stepOne.add(testDurationTextField);

		batteryTimeoutLengthTextField = new JTextField();
		batteryTimeoutLengthTextField.setEnabled(false);
		batteryTimeoutLengthTextField.setEditable(false);
		batteryTimeoutLengthTextField.setToolTipText("Minimum of 1 second, maximum of 65535 seconds");
		batteryTimeoutLengthTextField.setText("300");
		batteryTimeoutLengthTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		batteryTimeoutLengthTextField.setColumns(10);
		batteryTimeoutLengthTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Battery Timeout Length (Seconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		batteryTimeoutLengthTextField.setBounds(259, 223, 257, 53);
		stepOne.add(batteryTimeoutLengthTextField);

		tmr0TickThreshTextField = new JTextField();
		tmr0TickThreshTextField.setEnabled(false);
		tmr0TickThreshTextField.setText("");
		tmr0TickThreshTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		tmr0TickThreshTextField.setEditable(false);
		tmr0TickThreshTextField.setColumns(10);
		tmr0TickThreshTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Timer0 Tick Threshold (Read Only)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		tmr0TickThreshTextField.setBounds(10, 276, 250, 53);
		stepOne.add(tmr0TickThreshTextField);

		delayAfterStartTextField = new JTextField();
		delayAfterStartTextField.setEnabled(false);
		delayAfterStartTextField.setText("");
		delayAfterStartTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		delayAfterStartTextField.setEditable(false);
		delayAfterStartTextField.setColumns(10);
		delayAfterStartTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Delay After Start (Microseconds) (Read Only)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		delayAfterStartTextField.setBounds(259, 276, 257, 53);
		stepOne.add(delayAfterStartTextField);
		
		JButton nextWindowBtn = new JButton("Next");
		nextWindowBtn.setFont(new Font("Tahoma", Font.PLAIN, 11));
		nextWindowBtn.setBounds(449, 412, 67, 23);
		stepOne.add(nextWindowBtn);
		
		JButton backWindowBtn = new JButton("Back");
		backWindowBtn.setBounds(10, 412, 72, 23);
		stepOne.add(backWindowBtn);
		
		generalStatusLabel = new JLabel("");
		generalStatusLabel.setBounds(92, 412, 353, 23);
		stepOne.add(generalStatusLabel);

		JPanel stepTwo = new JPanel();
		testTakingPanel.add(stepTwo, "stepTwo");

		JPanel calibrationPanel = new JPanel();
		mainTabbedPane.addTab("Calibration Panel", null, calibrationPanel, null);

		JPanel motionVisualizationPanel = new JPanel();
		mainTabbedPane.addTab("Motion Visualization", null, motionVisualizationPanel, null);
	}
}
