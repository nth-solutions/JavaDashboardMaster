package dataorganizer;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import java.awt.BorderLayout;
import java.awt.Canvas;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.JTextPane;
import javax.swing.JTextField;

/**
 * THIS CLASS IM ONLY AND MAINLY USED IN THE ADVANCED MODE AND HAS 0 LINK TO THE EDUCATOR MODE
 * THIS CLASS MAINLY CONSISTS OF SWING CODE RELATING TO BUILDING THE UI
 */

public class VLCJMediaPlayerController {
	JFrame frame;
	private Canvas videoSurface;
	private JButton playBtn;
	private JLabel statusLabel;
	private JLabel timeTrackerLabel;
	Settings settings;

	private MediaPlayerFactory mediaPlayerFactory;
	private EmbeddedMediaPlayer mediaPlayer;

	private int count;
	private boolean videoLoaded = false;
	private JTextField frameNumberTextField;
	
	private String videoPath;
	private String totalTime;
	private float videoLength;
	private int frameNumber;
	private float fps;
	private volatile boolean stopSteppingBoolean;
	
	private Thread vlcjFrameStepperThread;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VLCJMediaPlayerController window = new VLCJMediaPlayerController();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public VLCJMediaPlayerController() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1180, 720);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);

		settings = new Settings();
		
		JPanel SouthContainer = new JPanel();
		SouthContainer.setBounds(0, 597, 1167, 84);
		frame.getContentPane().add(SouthContainer);
		SouthContainer.setLayout(null);
		
		JButton importVideo = new JButton("Import Video");
		importVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				importVideo();
			}
		});
		importVideo.setToolTipText("Open a browser window to select your video.");
		importVideo.setBounds(20, 16, 106, 57);
		SouthContainer.add(importVideo);
		
		JPanel SouthPanel = new JPanel();
		SouthPanel.setBounds(630, 16, 1, 1);
		SouthContainer.add(SouthPanel);
		SouthPanel.setLayout(null);
		
		JButton fileImportBtn = new JButton("File");
		fileImportBtn.setBounds(557, 5, 49, 23);
		SouthPanel.add(fileImportBtn);

		playBtn = new JButton("Play");
		playBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playBtnHandler();
			}
		});
		
		playBtn.setToolTipText("Open a browser window to select your video.");
		playBtn.setBounds(136, 16, 106, 57);
		SouthContainer.add(playBtn);
		
		timeTrackerLabel = new JLabel("0.00 / 0.00");
		timeTrackerLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		timeTrackerLabel.setBounds(275, 17, 120, 14);
		timeTrackerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		SouthContainer.add(timeTrackerLabel);
		
		JLabel frameNumberLabel = new JLabel("Current a Frame Number:");
		frameNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frameNumberLabel.setBounds(795, 19, 130, 14);
		SouthContainer.add(frameNumberLabel);
		
		frameNumberTextField = new JTextField();
		frameNumberTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		frameNumberTextField.setBounds(935, 16, 90, 20);
		SouthContainer.add(frameNumberTextField);
		frameNumberTextField.setColumns(10);
		
		JButton seekToFrameNumberBtn = new JButton("Seek");
		seekToFrameNumberBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (!videoLoaded) return;
					statusLabel.setVisible(false);
					int frameNumber = Integer.parseInt(frameNumberTextField.getText());
					seekToFrame(frameNumber);
				} catch(NumberFormatException e) {
					statusLabel.setVisible(true);
					statusLabel.setText("Invalid Frame");
				}

			}
		});
		seekToFrameNumberBtn.setBounds(1035, 16, 90, 20);
		SouthContainer.add(seekToFrameNumberBtn);
		
		JButton subFrameBtn = new JButton("-1 Frame");
		subFrameBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				seekLastFrame();
			}
		});
		subFrameBtn.setBounds(935, 47, 90, 20);
		SouthContainer.add(subFrameBtn);
		
		JButton addFrameBtn = new JButton("+1 Frame");
		addFrameBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				seekNextFrame();
			}
		});
		addFrameBtn.setBounds(1035, 47, 90, 20);
		SouthContainer.add(addFrameBtn);
		
		statusLabel = new JLabel("Select a video");
		statusLabel.setBounds(0, 0, 148, 46);
		frame.getContentPane().add(statusLabel);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setForeground(Color.BLACK);
		statusLabel.setBackground(new Color(0, 0, 0));
		
		videoSurface = new Canvas();
		videoSurface.setBounds(10, 10, 1144, 581);
		frame.getContentPane().add(videoSurface);
		videoSurface.setBackground(Color.BLACK);
		videoSurface.setForeground(Color.WHITE);
		
		
		try{
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:/Program Files/VideoLAN/VLC");
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:/Program Files(x86)/VideoLAN/VLC");
			Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		}catch(UnsatisfiedLinkError e) {
			System.out.println("There was no VLC installation found");
		}
		
		Runnable vlcjFrameStepRunner = new Runnable() {
			@Override
			public void run() {
				playByFrameStep();
			}
		};
		vlcjFrameStepperThread = new Thread(vlcjFrameStepRunner);
		
	}
	
	public int getCurrentSteppedFrame() {
		return frameNumber;
	}
	
	public JFrame getWindowFrame() {
		return frame;
	}
	
	public float getFPS() {
		return fps;
	}
	
	public int getCurrentFrame() {
		return frameNumber;
	}
	
	public float getTotalFrames() {
		return (fps * mediaPlayer.getLength()/1000);
	}
	
	public boolean hasVideoSelected() {
		return (videoPath!=null);
	}
	public double getMsPerFrame() {
		return 1000/getFPS();
	}
	
	@SuppressWarnings("deprecation") //It's deprecated, but it works
	public void playBtnHandler(){
		
	if(playBtn.getText() == "Play" && count == 0 && videoLoaded) {
		playBtn.setText("Pause");
		stopSteppingBoolean = false;
		
		mediaPlayer.play();
		vlcjFrameStepperThread.start();
		
		count++;
		
	}else if(playBtn.getText() == "Play" && count != 0 && videoLoaded) {
		playBtn.setText("Pause");
		stopSteppingBoolean = false;
		statusLabel.setVisible(false);
		
		mediaPlayer.play();
		vlcjFrameStepperThread.resume();
	
		count++;
	}else if (playBtn.getText() == "Pause" && count != 0 && videoLoaded){
		playBtn.setText("Play");
		stopSteppingBoolean = true;
		statusLabel.setText("Paused");
		statusLabel.setVisible(true);
		
		mediaPlayer.pause();
		vlcjFrameStepperThread.suspend();
		
		}
		
		count++;
	}
		

	
	private void seekLastFrame() {
		if(!videoLoaded) return;
		frameNumber -= 2; //Decrement frame number by two
		seekToFrame(frameNumber); //Seek frame
		
		mediaPlayer.play();
		
		try {
			Thread.sleep((long)getMsPerFrame()); //Sleep for a frame
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		frameNumber++;
		
		mediaPlayer.pause(); //Pause

		frameNumberTextField.setText(String.valueOf(frameNumber));
		playBtn.setText("Play");
		stopSteppingBoolean = true;
		statusLabel.setText("Paused");
	}
	
	private void seekNextFrame() {
		if(!videoLoaded) return;
		frameNumber++;
		seekToFrame(frameNumber); //Seek frame
		
		frameNumberTextField.setText(String.valueOf(frameNumber));
		playBtn.setText("Play");
		stopSteppingBoolean = false;
		statusLabel.setText("Paused");
	}
	
	//Modified to not advance frames, only keep time 
	public void playByFrameStep() {
		videoLength = (float)mediaPlayer.getLength()/1000; //getLength returns duration in ms, videoLength is in seconds
		//totalTime = String.valueOf(videoLength);
		float totalFrames = getTotalFrames();
		double msPerFrame = getMsPerFrame();
		long threadPauseTime = (long)msPerFrame; //
		
		String totalTimeString = String.valueOf(new DecimalFormat("#.00").format(videoLength));
		
		for(frameNumber = 0; frameNumber < totalFrames && stopSteppingBoolean == false; frameNumber++) {			
			
			try {
				Thread.sleep(threadPauseTime, (int)((msPerFrame - threadPauseTime)*1000));
			} catch (InterruptedException e) {
				System.out.println("Thread Interrupted");
			}
			
			frameNumberTextField.setText(String.valueOf(frameNumber));
			float currentTime = (float) (frameNumber * msPerFrame / 1000.0);
			
		
			
			String currentTimeString = String.valueOf(new DecimalFormat("#.00").format(currentTime));
			
			
			timeTrackerLabel.setText(currentTimeString + " / " + totalTimeString);
		}
		
	}
	
	/**
	 * Handles the button press of browse button. This is an action event which must handled before the rest of the program resumes. This method allows the user to navigate
	 * the file explorer and select a save location for the incoming data.
	 */
	public void importVideo() {
		JFileChooser chooser = new JFileChooser();
		settings.loadConfigFile();
		chooser.setCurrentDirectory(new File(settings.getKeyVal("CSVSaveLocation")));  
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			frame.setTitle(chooser.getSelectedFile().toString());
			videoPath = chooser.getSelectedFile().toString();
			videoLoaded = true;
			statusLabel.setVisible(false);
			count = 0;
		}
		else {
			videoPath = null;
			videoLoaded = false;
			return;
		}
		
		mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
		mediaPlayer.setVideoSurface(mediaPlayerFactory.newVideoSurface(videoSurface));
		mediaPlayer.prepareMedia(videoPath, "--verbose=-1");
		mediaPlayer.setScale((float)1.5);
		mediaPlayer.getMediaMeta();
		mediaPlayer.setAudioOutput("NULL");
		mediaPlayer.start();
		
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fps = mediaPlayer.getFps();
		
		 
		 mediaPlayer.setPause(true);													
		 mediaPlayer.pause();															
		 
		
		
		
		mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void playing(MediaPlayer mediaPlayer) {
				System.out.println(mediaPlayer.getLength());
				videoLength = mediaPlayer.getLength()/1000; //getLength returns duration in ms, videoLength is in seconds
				totalTime = String.valueOf(videoLength);
			}
		});
	}
	
	
	
	public void seekToFrame(int frameNumber) {
		try {
			this.frameNumber = frameNumber;
			statusLabel.setVisible(false);
			
			double videoLength = mediaPlayer.getLength(); // in ms
			
			double msPerFrame = getMsPerFrame();
			
			//Time stamp value of the exact frame the user would like to display
			double timeStamp = msPerFrame * frameNumber;
			
			double position = timeStamp / videoLength;
			
			System.out.println("Video Length: " + videoLength);
			System.out.println("FPS: " + getFPS());
			System.out.println("Number of ms per frame: " + msPerFrame);
			System.out.println("Time Stamp of Frame Selected: " + timeStamp);
			timeTrackerLabel.setText(String.valueOf(new DecimalFormat("#.00").format(timeStamp/1000)) + "/" + totalTime);
			System.out.println("Position %: " + position);
			
			mediaPlayer.setPosition((float) position);
			
		} catch (NumberFormatException e) {
			//Displays an error message when NaN is set as an input 
			statusLabel.setVisible(true);
			statusLabel.setText("Non-Valid Frame");
		}
		
		
			
			
			
		
		
	}
}
