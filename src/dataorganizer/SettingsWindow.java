package dataorganizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
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
import org.eclipse.wb.swing.FocusTraversalOnArray;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JTabbedPane;

public class SettingsWindow extends JFrame {

	private JPanel contentPane;
	private JTextField saveDirectoryTextField;
	private JTextField templateDirectoryTextField;
	private JComboBox profileComboBox;
	private JCheckBox openCSVOnReadCheckBox;

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

	
	public LoadSettings getSettingsInstance() {
		LoadSettings settings = new LoadSettings();
		String saveDirectoryString = null;
		try {
			settings.loadConfigFile();
			saveDirectoryString = settings.getKeyVal("CSVSaveLocation");
		}catch(Exception e) {
			e.printStackTrace();
			settings.loadDefaultConfig();
			settings.saveConfig();
		}
		return settings;
	}
	
	
	
	public void saveAndExitBtnHandler() {
		LoadSettings settings = getSettingsInstance();
		settings.setProp("CSVSaveLocation", saveDirectoryTextField.getText());
		settings.setProp("DefaultProfile", (String) profileComboBox.getSelectedItem());
		settings.setProp("TemplateDirectory", templateDirectoryTextField.getText());
		settings.setProp("OpenOnRead", openCSVOnReadCheckBox.getText());
		settings.saveConfig();
		dispose();
	}
	
	
	/**
	 * Create the frame.
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
		profileList.add("Professional");
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, "name_759004656281180");
		
		JPanel other = new JPanel();
		tabbedPane.addTab("General", null, other, null);
		other.setLayout(null);
		
		JLabel profileLabel = new JLabel("Selected Profile: ");
		profileLabel.setBounds(10, 14, 81, 14);
		other.add(profileLabel);
		
		profileComboBox = new JComboBox();
		profileComboBox.setBounds(101, 8, 318, 20);
		profileComboBox.setSelectedIndex(-1);
		other.add(profileComboBox);
		
		JLabel openCSVOnReadLabel = new JLabel("Open created CSV after reading a test:");
		openCSVOnReadLabel.setBounds(10, 44, 189, 14);
		other.add(openCSVOnReadLabel);
		
		openCSVOnReadCheckBox = new JCheckBox("True");
		openCSVOnReadCheckBox.setBounds(222, 35, 47, 23);
		openCSVOnReadCheckBox.setSelected(false);
		other.add(openCSVOnReadCheckBox);
		
		JButton restoreBtn = new JButton("Restore defaults");
		restoreBtn.setBounds(10, 200, 137, 23);
		other.add(restoreBtn);
		
		JButton saveBtn = new JButton("Save and exit");
		saveBtn.setBackground(new Color(0, 128, 0));
		saveBtn.setBounds(320, 200, 99, 23);
		other.add(saveBtn);
		
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
		saveDirectoryBrowseBtn.setBounds(352, 12, 67, 23);
		directorySaveLocations.add(saveDirectoryBrowseBtn);
		
		JLabel templateDirectoryLabel = new JLabel("Template Directory: ");
		templateDirectoryLabel.setBounds(10, 44, 98, 14);
		directorySaveLocations.add(templateDirectoryLabel);
		
		templateDirectoryTextField = new JTextField();
		templateDirectoryTextField.setBounds(107, 41, 231, 20);
		templateDirectoryTextField.setText((String) null);
		templateDirectoryTextField.setColumns(10);
		directorySaveLocations.add(templateDirectoryTextField);
		
		JButton templateDirectoryBrowseBtn = new JButton("Browse");
		templateDirectoryBrowseBtn.setBounds(352, 40, 67, 23);
		directorySaveLocations.add(templateDirectoryBrowseBtn);
		
		JButton saveAndExitBtn = new JButton("Save and exit");
		saveAndExitBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveAndExitBtnHandler();
			}
		});
		saveAndExitBtn.setBounds(320, 200, 99, 23);
		saveAndExitBtn.setBackground(new Color(0, 128, 0));
		directorySaveLocations.add(saveAndExitBtn);
		
		JButton restoreDefaultsBtn = new JButton("Restore defaults");
		restoreDefaultsBtn.setBounds(10, 200, 137, 23);
		directorySaveLocations.add(restoreDefaultsBtn);
		

	}
	
	
	
}
