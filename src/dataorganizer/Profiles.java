package dataorganizer;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.Dimension;

public class Profiles extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
					JFrame frame = null;
					try {
						LoadSettings settings = new LoadSettings();
						settings.loadConfigFile();
						if(settings.getKeyVal("DefaultProfile") != null) {
							switch(settings.getKeyVal("DefaultProfile")) {
								case "Adventurer":
									frame = new AdventurerMode();
								case "Educator":
									frame = new EducatorMode();
								case "Professional":
									frame = new AdvancedMode();
							}
						}
					}
					catch(Exception e){
						frame = new Profiles();
					}
					if(frame != null) {
						frame.setVisible(true);
					}
			}
		});
		
		while (true) {
			
		}
	}

	/**
	 * Create the frame.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public Profiles() {
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Data Organizer Rev-20 (Profiles)(5/10/2018)");
        setMinimumSize(new Dimension(405, 260));
        setResizable(false);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{128, 128, 128, 0};
		gbl_contentPane.rowHeights = new int[]{84, 52, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JButton AdventureBtn = new JButton("Adventurer");
		GridBagConstraints gbc_AdventureBtn = new GridBagConstraints();
		gbc_AdventureBtn.gridheight = 2;
		gbc_AdventureBtn.fill = GridBagConstraints.BOTH;
		gbc_AdventureBtn.insets = new Insets(0, 0, 0, 5);
		gbc_AdventureBtn.gridx = 0;
		gbc_AdventureBtn.gridy = 0;
		contentPane.add(AdventureBtn, gbc_AdventureBtn);
		
		JButton EducatorBtn = new JButton("Educator/Student");
		GridBagConstraints gbc_EducatorBtn = new GridBagConstraints();
		gbc_EducatorBtn.gridheight = 2;
		gbc_EducatorBtn.fill = GridBagConstraints.BOTH;
		gbc_EducatorBtn.insets = new Insets(0, 0, 0, 5);
		gbc_EducatorBtn.gridx = 1;
		gbc_EducatorBtn.gridy = 0;
		contentPane.add(EducatorBtn, gbc_EducatorBtn);
		
		JButton ProffessionalBtn = new JButton("Professional");
		GridBagConstraints gbc_ProffessionalBtn = new GridBagConstraints();
		gbc_ProffessionalBtn.gridheight = 2;
		gbc_ProffessionalBtn.fill = GridBagConstraints.BOTH;
		gbc_ProffessionalBtn.gridx = 2;
		gbc_ProffessionalBtn.gridy = 0;
		contentPane.add(ProffessionalBtn, gbc_ProffessionalBtn);
		
		
		AdventureBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AdventureBtnActionListener();
			}
			
		});
		EducatorBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EducatorBtnActionListener();
			}
			
		});
		ProffessionalBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ProfessionalBtnActionListener();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}
	
	private void disposeGUI() {
		this.dispose();
	}
	
	private void AdventureBtnActionListener() {
		this.dispose();
		new AdventurerMode().setVisible(true);
		
	}
	private void EducatorBtnActionListener() {
		this.dispose();
		new EducatorMode().setVisible(true);
	}
	private void ProfessionalBtnActionListener() throws FileNotFoundException, IOException {
		this.dispose();
		//new Frame().setVisible(true);
	}

}
