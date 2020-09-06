package com.bioforceanalytics.dashboard;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.CardLayout;
import javax.swing.JTabbedPane;

/**
 * This class is responsible for the UI of a Settings Tab that can be open from the Advanced Mode Dashboard.
 * Modifications of the settings are handled through an object from the Settings class.
 */
@SuppressWarnings("serial")
public class SettingsWindow extends JFrame {

	private JPanel contentPane;
	private JTextField saveDirectoryTextField;
	private JCheckBox saveOnReadCheckBox;
	private JComboBox<String> profileComboBox;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SettingsWindow frame = new SettingsWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void updateUI() {
		saveDirectoryTextField.setText(Settings.get("CSVSaveLocation"));
		saveOnReadCheckBox.setSelected(Boolean.parseBoolean(Settings.get("AutoSave")));
		profileComboBox.setSelectedItem(Settings.get("DefaultProfile"));
	}
	
	public void restoreDefaultsBtnHandler() {
		Settings.restoreDefaultConfig();
	}
	
	public void saveBtnHandler() {
		Settings.loadConfigFile();
		Settings.set("CSVSaveLocation", saveDirectoryTextField.getText());
		Settings.set("AutoSave", String.valueOf(saveOnReadCheckBox.isSelected()));
		Settings.set("DefaultProfile", profileComboBox.getSelectedItem().toString());
		Settings.saveConfig();
	}
	
	
	/**
	 * Handles the button press of browse button. This is an action event which must handled before the rest of the program resumes. This method allows the user to navigate
	 * the file explorer and select a save location for the incoming data.
	 */
	public void saveDirectoryBrowseBtnHandler() {
		JFileChooser chooser;																							//File chooser is used for file selection
		chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);													//A file save location need to be a directory.
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			saveDirectoryTextField.setText(chooser.getSelectedFile().toString());
		}
		else {
			saveDirectoryTextField.setText(null);
		}
	}
	
	
	/**
	 * Create the frame; adds needed buttons and text fields.
	 */
	public SettingsWindow() {
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new CardLayout(0, 0));
		
		ArrayList<String> profileList = new ArrayList<String>();
		profileList.add("Adventurer");
		profileList.add("Educator");
		profileList.add("Advanced");
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, "name_759004656281180");
		
		JPanel directorySaveLocations = new JPanel();
		tabbedPane.addTab("Folder Locations", null, directorySaveLocations, null);
		directorySaveLocations.setLayout(null);
		
		JLabel saveDirectoryLabel = new JLabel("Save Directory:");
		saveDirectoryLabel.setBounds(12, 16, 75, 14);
		directorySaveLocations.add(saveDirectoryLabel);
		
		saveDirectoryTextField = new JTextField();
		saveDirectoryTextField.setBounds(97, 13, 240, 20);
		saveDirectoryTextField.setText((String) null);
		saveDirectoryTextField.setColumns(10);
		directorySaveLocations.add(saveDirectoryTextField);
		
		JButton saveDirectoryBrowseBtn = new JButton("Browse");
		saveDirectoryBrowseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveDirectoryBrowseBtnHandler();
			}
		});
		saveDirectoryBrowseBtn.setBounds(352, 12, 67, 23);
		directorySaveLocations.add(saveDirectoryBrowseBtn);
		
		JButton saveAndExitBtn = new JButton("Save and exit");
		saveAndExitBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveBtnHandler();
				dispose();
			}
		});
		saveAndExitBtn.setBounds(320, 200, 99, 23);
		saveAndExitBtn.setBackground(new Color(0, 128, 0));
		directorySaveLocations.add(saveAndExitBtn);
		
		JButton restoreDefaultsBtn = new JButton("Restore defaults");
		restoreDefaultsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restoreDefaultsBtnHandler();
				updateUI();
			}
		});
		restoreDefaultsBtn.setBounds(10, 200, 137, 23);
		directorySaveLocations.add(restoreDefaultsBtn);
		
		JLabel lblSaveOnRead = new JLabel("Save on Read:");
		lblSaveOnRead.setBounds(12, 58, 75, 14);
		directorySaveLocations.add(lblSaveOnRead);
		
		saveOnReadCheckBox = new JCheckBox("");
		saveOnReadCheckBox.setBounds(97, 58, 21, 23);
		directorySaveLocations.add(saveOnReadCheckBox);
		
		JLabel lblNewLabel = new JLabel("Profile:");
		lblNewLabel.setBounds(12, 102, 75, 14);
		lblNewLabel.setVisible(false); //This label is for the combo box below that is currently not being used.
		directorySaveLocations.add(lblNewLabel);
		
		profileComboBox = new JComboBox();
		profileComboBox.setModel(new DefaultComboBoxModel(new String[] {"Advanced", "Educator"}));
		profileComboBox.setBounds(97, 99, 106, 20);
		directorySaveLocations.add(profileComboBox);
		profileComboBox.setVisible(false); //This combo box is currently not used for anything.
		
		updateUI();
	}
}
