package com.bioforceanalytics.dashboard;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * This class helps to manage the selection of the two different modes, Educator and Advanced
 */
public class RootLauncher {

	private JFrame frame;
	private Settings settings;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		RootLauncher tmp = new RootLauncher();
		if(tmp.checkProp()) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try { 
						RootLauncher window = new RootLauncher();
						window.frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * Create the application.
	 */
	public RootLauncher() {
		initialize();
	}
	
	//Returns true if property is not set correctly
	public boolean checkProp() {
		settings = new Settings();
		settings.loadConfigFile();

		String defaultProfile = settings.getKeyVal("DefaultProfile");
		if(defaultProfile.equals("Professional")) {
			runProfessional();
			return false;
		}
		else if(defaultProfile.equals("Educator")){
			runEducator();
			return false;
		}
		return true;
	}
	
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton professionalBtn = new JButton("Professional");
		professionalBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				settings = new Settings();
				settings.loadConfigFile();
				settings.setProp("DefaultProfile", "Professional");
				settings.saveConfig();
				runProfessional();
				frame.setVisible(false);
			}
		});
		professionalBtn.setBounds(10, 40, 414, 78);
		frame.getContentPane().add(professionalBtn);
		
		JButton educatorBtn = new JButton("Educator");
		educatorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				settings = new Settings();
				settings.loadConfigFile();
				settings.setProp("DefaultProfile", "Educator");
				settings.saveConfig();
				runEducator();
				frame.setVisible(false);
			}
		});
		educatorBtn.setBounds(10, 140, 414, 78);
		frame.getContentPane().add(educatorBtn);
	}
	
	
	public void runEducator() {
		Runnable modeRunner = new Runnable() {
			public void run() {
				EducatorMode.main(null);
			}
		};
		Thread modeThread = new Thread(modeRunner);
		modeThread.run();
	}
	
	
	public void runProfessional() {
		Runnable modeRunner = new Runnable() {
			public void run() {
				AdvancedMode.main(null);
			}
		};
		Thread modeThread = new Thread(modeRunner);
		modeThread.run();
	}
	
}
