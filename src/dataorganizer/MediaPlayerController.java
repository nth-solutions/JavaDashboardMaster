package dataorganizer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import static java.lang.Math.round;

//GOALS:
//TODO: INTEGRATE FFMPEG INTO PROGRAM *SHELVED FOR LATER DEVELOPMENT

//BUG FIXES:



public class MediaPlayerController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /*** BEGINNING OF PRIMARY VIDEO PLAYER COMPONENTS ***/

    private MediaPlayer mediaPlayer;                                                                                                                            // Variable Declarations
    private String filePath;
    private double playbackRate;
    private Boolean playing = false;
    private File fileCopy;
    private Boolean videoLoaded = false;
    private double totalFrames;
    private Media media;
    private int videoFrameRate;
    private double millisPerFrame;
    private String currentFrame;

    @FXML                                                                                                                                                       // FXML component Declarations
    private MediaView mediaView;
    @FXML
    private Slider timeStampSlider;
    @FXML
    private Slider rateChangeSlider;
    @FXML
    private Button selectFileButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button playPauseButton;
    @FXML
    private Text rateText;
    @FXML
    private Text playbackTimeText;
    @FXML
    private Text totalVideoTimeText;
    @FXML
    private Text generalStatusText;
    @FXML
    private StackPane mediaViewPane;
    @FXML
    private Text noVideoSelectedText;


    @FXML
    public void handleFileOpener(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();                                                                                                            // Creates a FileChooser Object
        Settings settings = new Settings();
        settings.loadConfigFile();
        fileChooser.setInitialDirectory(new File(settings.getKeyVal("CSVSaveLocation")));
        fileChooser.setTitle("Select a Video File");                                                                                                            // Sets the title of the file selector
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a File (*.mp4)", "*.mp4");                           // Creates a filter that limits fileChooser's search parameters to *.mp4 files
        fileChooser.getExtensionFilters().add(filter);                                                                                                          // Initializes the filter into the fileChooser object

        File file = fileChooser.showOpenDialog(null);                                                                                              // Specifies the parent component for the dialog

        fileCopy = file;                                                                                                                                        // File object necessary for use in the reset handler
        
        try {
			readFileFPSFromFFMpeg();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        if (file != null) {                                                                                                                                     // If the filepath contains a valid file the following code is initiated ->
            filePath = file.toURI().toString();                                                                                                                 // Sets the user's selection to a file path that will be used to select the video file to be displayed
            media = new Media(filePath);                                                                                                                        // Sets the media object to the selected file path
            mediaPlayer = new MediaPlayer(media);                                                                                                               // Creates a mediaPlayer object, mediaPlayer is utilized for video playback controls
            mediaView.setMediaPlayer(mediaPlayer);                                                                                                              // Sets the mediaPlayer to be the controller for the mediaVew object
            videoLoaded = true;                                                                                                                                 // Boolean to check if a video has been loaded

            selectFileButton.setDisable(true);                                                                                                                  // Disables the button used to select a file following a selection
            generalStatusText.setText("");                                                                                                                      // Removes the status text from the top of the player after the user selects a file
            noVideoSelectedText.setVisible(false);
            
            mediaPlayer.setOnReady(new Runnable() {                                                                                                             // Sets the maximum value of the slider bar equal to the total duration of the file
                @Override
                public void run() {
                    
                    resetButton.setDisable(false);                                                                                                                      // Enables buttons following a valid file selection
                    playPauseButton.setDisable(false);
                    timeStampSlider.setDisable(false);
                    rateChangeSlider.setDisable(false);
                    frameByFrameCheckbox.setDisable(false);

                    System.out.println("Here we play.");
                    mediaPlayer.play();                                                                                                                                 // Begins video playback on the opening of the file
                    currentFrame = String.valueOf((new DecimalFormat("#").format(mediaPlayer.getCurrentTime().toSeconds() * getFPS())));
                    playPauseButton.setText("Pause");                                                                                                                   // Changes the playPauseButton's display text to Pause for UI changes necessary with the pause/play functionality switch of the handlePlayPauseVideo event
                    timeStampSlider.setMax(round(media.getDuration().toMillis()));
                    totalFrames = round(Double.parseDouble(new DecimalFormat("#.000").format(mediaPlayer.getTotalDuration().toSeconds())) * getFPS());   // Sets the totalFrames variable equal to the total number of frames in the selected file
                }
            });

            mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {                                                                      // Displays a moving slider bar that corresponds to the current point of playback within the video sequence
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {

                    timeStampSlider.setValue(round(newValue.toMillis()));                                                                                       // Updates the slider circle to be located wherever the video is along playback

                    currentFrame = String.valueOf((new DecimalFormat("#").format(mediaPlayer.getCurrentTime().toSeconds() * getFPS())));
                    playbackTimeText.setText(String.valueOf(new DecimalFormat("#.0").format(mediaPlayer.getCurrentTime().toSeconds()) + "s"));          // Changes current time counter according to playback time
                    totalVideoTimeText.setText(String.valueOf(new DecimalFormat("#.0").format(media.getDuration().toSeconds()) + "s"));                 // Displays the total video length in seconds

                    currentFrameCounterTextField.setText("Current Frame: " + currentFrame); 															//updates the currentFrameCounterTextField to the current frame being displayed during playback
                totalFrameTextField.setText("Total Frames: " + String.valueOf((new DecimalFormat("#").format(totalFrames))));                           // Sets the text within the totalFrameTextField equal to the totalFrames variable calculated during the handleFileOpener event
                }
            });
        }
    }

    @FXML
    public void handleSelectTimeStamp(MouseEvent event) {                                                                                                       // Allows user to select a point along the video's playback to resume playing
        mediaPlayer.seek(Duration.millis(timeStampSlider.getValue()));                                                                                          // Seeks the videos playback to the value present at the area along the slider where the user dragged the slider circle
        if (!frameByFrameCheckbox.isSelected()) {                                                                                                               // If the frameByFrameCheckbox is not selected ->
            mediaPlayer.play();                                                                                                                                 // Resume's video playback after the slider is released
            playPauseButton.setText("Pause");                                                                                                                   // Changes the playPauseButton's display text to Pause for UI changes necessary with the pause/play functionality switch of the handlePlayPauseVideo event
        } else {                                                                                                                                                // If the frameByFrameCheckbox is selected ->
            mediaPlayer.pause();                                                                                                                                // Pauses the video playback after the slider is released for frame-analysis accuracy
            playPauseButton.setText("Play");                                                                                                                    // Changes the playPauseButton's display text to Play for UI changes necessary with the pause/play functionality switch of the handlePlayPauseVideo event
        }

    }

    @FXML
    public void handleSelectTimeStampKeys(KeyEvent event) {                                                                                                     // Allows user to select a point along the video's playback using left and right arrow keys
        if (event.getCode() == KeyCode.RIGHT) {
            mediaPlayer.seek(Duration.millis(timeStampSlider.getValue() * 1.03));                                                                               // Increments the slider bar to +3% of the original slider circle value following a right arrow keypress
            mediaPlayer.play();                                                                                                                                 // Resume's video playback
        } else if (event.getCode() == KeyCode.LEFT) {
            mediaPlayer.seek(Duration.millis(timeStampSlider.getValue() * .97));                                                                                // Decrement's the slider bar to -3% of the original slider circle value following a left arrow keypress
            mediaPlayer.play();                                                                                                                                 // Resume's video playback
        }
        playPauseButton.setText("Pause");                                                                                                                       // Changes the playPauseButton's display text to Pause for UI changes necessary with the pause/play functionality switch of the handlePlayPauseVideo event
    }

    @FXML
    public void handlePausePlaybackOnKeyPress(KeyEvent event) {                                                                                                 // Pauses the video to prevent change listener conflicts in the event a user implements the handleSelectTimeStampKeys Key listener
        if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT) {
            mediaPlayer.pause();
        }

    }

    @FXML
    public void handleSelectTimeStampDrag(MouseEvent event) {                                                                                                   // Prevents the ChangeListener from overwriting slider placement
        mediaPlayer.pause();                                                                                                                                    // Pauses the videos playback
        playPauseButton.setText("Play");                                                                                                                        //Changes the playPauseButton's display text to Play for UI changes necessary with the pause/play functionality switch of the handlePlayPauseVideo event
    }

    @FXML
public void handlePlayPauseVideo(ActionEvent event) {                                                                                                           // Event listener responsible for changing the text and functionality of the playPauseButton button
        if (playing) {                                                                                                                                          // When the button is pressed, if the Boolean Playing is true ->
            mediaPlayer.play();                                                                                                                                 // The mediaPlayer resumes playback
            playPauseButton.setText("Pause");                                                                                                                   // The playPauseButton is then set to display "Pause"
            playing = false;                                                                                                                                    // The Boolean Playing is switched to false so as to activate the 'else' conditional of the code following a secondary press
        } else {                                                                                                                                                // When the button is pressed, if the Boolean Playing is false ->
            mediaPlayer.pause();                                                                                                                                // The mediaPlayer's playback is paused
            playPauseButton.setText("Play");                                                                                                                    // The playPause button is then set to display "Play"
            playing = true;                                                                                                                                     // The Boolean Playing is set to true so as to activate the 'if' conditional of the code following another press
        }

    }

    @FXML
    public void handleResetVideo(ActionEvent event) throws InterruptedException {
        mediaPlayer.dispose();                                                                                                                                  // Just obliterates it all
        if (fileCopy != null) {                                                                                                                                 // If the filepath contains a valid file the following code is initiated
            filePath = fileCopy.toURI().toString();                                                                                                             // Sets the user's selection to a file path that will be used to select the video file to be displayed
            Media media = new Media(filePath);                                                                                                                  // Sets the media object to the selected file path
            mediaPlayer = new MediaPlayer(media);                                                                                                               // Creates a mediaPlayer object, mediaPlayer is utilized for video playback controls
            mediaView.setMediaPlayer(mediaPlayer);                                                                                                              // Sets the mediaPlayer to be the controller for the mediaVew object

            mediaPlayer.setOnReady(new Runnable() {                                                                                                             // Sets the maximum value of the slider bar equal to the total duration of the file
                @Override
                public void run() {
                    timeStampSlider.setMax(round(media.getDuration().toMillis()));
                }
            });

            mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {                                                                      // Displays a moving slider bar that corresponds to the current point of playback within the video sequence
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {

                    timeStampSlider.setValue(round(newValue.toMillis()));                                                                                       // Updates the slider circle to be located wherever the video is along playback

                    playbackTimeText.setText(String.valueOf(new DecimalFormat("#.0").format(mediaPlayer.getCurrentTime().toSeconds()) + "s"));          // Changes current time counter according to playback time
                    totalVideoTimeText.setText(String.valueOf(new DecimalFormat("#.0").format(media.getDuration().toSeconds()) + "s"));                 // Displays the total video length in seconds

                    currentFrameCounterTextField.setText("Current Frame: " + currentFrame); 															//updates the currentFrameCounterTextField to the current frame being displayed during playback
                    totalFrameTextField.setText("Total Frames: " + String.valueOf((new DecimalFormat("#").format(totalFrames))));                       // Sets the text within the totalFrameTextField equal to the totalFrames variable calculated during the handleFileOpener event
                }
            });


            selectFrameToDisplayTextField.setDisable(true);                                                                                                     // Disables the Frame-By-Frame Analysis components following a program reset
            goToFrameButton.setDisable(true);
            decrementOneFrameButton.setDisable(true);
            incrementOneFrameButton.setDisable(true);
            totalFrameTextField.setDisable(true);
            currentFrameCounterTextField.setDisable(true);
            selectFileButton.setDisable(true);

            playPauseButton.setText("Play");                                                                                                                    // Resets various UI components for consistency following a program reset
            playing = true;
            timeStampSlider.setValue(0);
            playbackTimeText.setText(".0s");
            frameByFrameCheckbox.setSelected(false);
            generalStatusText.setText("");
            selectFrameToDisplayTextField.setText("");
            rateChangeSlider.setValue(1);
            rateText.setText("1x");
            currentFrameCounterTextField.clear();

        }
    }

    @FXML
    public void handleRateChange(MouseEvent event) {                                                                                                            // The rate method supports playback rates between 0.0 and 8.0. This mouse listener
        playbackRate = rateChangeSlider.getValue();                                                                                                             // is responsible for gathering the rate assigned to the rateChangeSlider and changing
        mediaPlayer.setRate(playbackRate);                                                                                                                      // the playback speed accordingly

        rateText.setText((Double.toString(Math.floor(mediaPlayer.getRate() * 10) / 10)) + "x");                                                              // Displays the current playback speed
    }

    @FXML
    public void handleRateChangeKeys(KeyEvent event) {                                                                                                          //This method supports the movement of the playback rate slider with the movement of key presses
        playbackRate = rateChangeSlider.getValue();
        mediaPlayer.setRate(playbackRate);

        rateText.setText((Double.toString(Math.floor(mediaPlayer.getRate() * 10) / 10)) + "x");
    }





    /*** BEGINNING OF FRAME-BY-FRAME ANALYSIS COMPONENT ***/





    private double totalDurationInMs;                                                                                                                           // Variable Declarations
    private int frameEnteredFromTextBox;
    private double frameConvertedToMsCount;


    @FXML                                                                                                                                                       // FXML component Declarations
    private CheckBox frameByFrameCheckbox;
    @FXML
    private TextField selectFrameToDisplayTextField;
    @FXML
    private Button goToFrameButton;
    @FXML
    private Button decrementOneFrameButton;
    @FXML
    private Button incrementOneFrameButton;
    @FXML
    private TextField totalFrameTextField;
    @FXML
    private TextField currentFrameCounterTextField;


    @FXML
    public void handleEnableFrameByFrameAnalysis(ActionEvent event) {                                                                                           // Method responsible for enabling/disabling the selectFrameToDisplayTextField following a checkbox selection
        if (frameByFrameCheckbox.isSelected()) {                                                                                                                // If the frameByFrameCheckbox is selected ->
            selectFrameToDisplayTextField.setDisable(false);                                                                                                    // Enables the selectFrameToDisplayTextField
            goToFrameButton.setDisable(false);
            mediaPlayer.pause();                                                                                                                                // Pauses the video playback
            playPauseButton.setText("Play");                                                                                                                    // Changes the playPauseButton's display text to Play for UI changes necessary with the pause/play functionality switch of the handlePlayPauseVideo event

            playPauseButton.setDisable(true);                                                                                                                   // Disables the primary video player components to ensure the user is only using the intended frame-by-frame analysis functions while the frameByFrameCheckbox is selected
            resetButton.setDisable(true);
            rateChangeSlider.setDisable(true);

        } else if (!frameByFrameCheckbox.isSelected()) {                                                                                                        // Else if the frameByFrameCheckbox is not selected ->
            selectFrameToDisplayTextField.setDisable(true);                                                                                                     // All components of the Frame-by-Frame analysis unit are disabled
            goToFrameButton.setDisable(true);
            incrementOneFrameButton.setDisable(true);
            decrementOneFrameButton.setDisable(true);
            totalFrameTextField.setDisable(true);
            currentFrameCounterTextField.setDisable(true);

            playPauseButton.setDisable(false);                                                                                                                  // All components of the primary video player are enabled
            resetButton.setDisable(false);
            rateChangeSlider.setDisable(false);

            mediaPlayer.play();                                                                                                                                 // The mediaPlayer resumes playback
            playPauseButton.setText("Pause");                                                                                                                   // The playPauseButton is then set to display "Pause"
            playing = false;                                                                                                                                    // The Boolean Playing is switched to false so as to activate the 'else' conditional of the code following a secondary press



            selectFrameToDisplayTextField.clear();                                                                                                              // Text within the selectFrameToDisplayTextField is cleared so the user can have a clean field upon next use of the frame-by-frame analysis tool
            currentFrameCounterTextField.clear();
        }
    }

    @FXML
    public void handleSeekToSelectedFrame(ActionEvent event) {


        try {
            frameEnteredFromTextBox = Integer.parseInt(selectFrameToDisplayTextField.getText());                                                                // Gets the frame selected by the user
            if (frameEnteredFromTextBox >= 0 && frameEnteredFromTextBox <= totalFrames && selectFrameToDisplayTextField.getText() != null) {

                goToFrameButton.setDisable(false);                                                                                                              // All Frame-By-Frame Analysis tools are enabled and any error message pop-ups are cleared
                incrementOneFrameButton.setDisable(false);
                decrementOneFrameButton.setDisable(false);
                totalFrameTextField.setDisable(false);
                currentFrameCounterTextField.setDisable(false);



                totalDurationInMs = Double.parseDouble(new DecimalFormat("#.000").format(media.getDuration().toMillis()));                              // Gets the total duration of the video and converts it to a millisecond value
                frameConvertedToMsCount = frameEnteredFromTextBox * getMPF();             // takes the users frame selection and the frame rate selected and calculates a millisecond value regarding where the frame is located within the video's playback

                mediaPlayer.seek(Duration.millis(frameConvertedToMsCount));                                                                                     // The video's playback seeks to the frameConvertedToMsCount calculated above
                mediaPlayer.pause();                                                                                                                            // pauses the video's playback
                playPauseButton.setText("Play");                                                                                                                // The playPauseButton is then set to display "Play"
                generalStatusText.setText("");                                                                                                                  // Clears the error message pop-up
                currentFrameCounterTextField.setText("Current Frame: " + String.valueOf(frameEnteredFromTextBox));
            } else {
                generalStatusText.setText("Not a Valid Frame");                                                                                                 // Displays an error message, informing the user that their selected frame is invalid
            }

        } catch (NumberFormatException e) {
            generalStatusText.setText("Not a Valid Frame");                                                                                                     // An error message is shown for all selected frames that are completely out of bounds / NaN
        }
    }

    @FXML
    public void handleFrameCounterChangeByOne(ActionEvent event) {                                                                                              // Method responsible for incrementing/decrementing the displayed frame by one
        if (event.getSource() == decrementOneFrameButton) {                                                                                                     // Checks which button the user hit, if the user hit the decrementOneFrameButton ->
            if (Double.parseDouble(selectFrameToDisplayTextField.getText()) > 0) {                                                                              // If the entered value is a valid frame ->
                frameConvertedToMsCount -= getMPF();                                      // The frame shown is decreased by one frame value and assigned to frameConvertedToMsCount
                selectFrameToDisplayTextField.setText(Integer.toString(Integer.parseInt(selectFrameToDisplayTextField.getText()) - 1));                      // The frame displayed in selectFrameToDisplayTextField is decreased by one
                currentFrameCounterTextField.setText("Current Frame: " + Integer.toString(Integer.parseInt(selectFrameToDisplayTextField.getText()) - 1));   // The frame displayed in currentFrameCounterTextField is decreased by one
                generalStatusText.setText("");                                                                                                                  // Clears the error message pop-up
            } else {                                                                                                                                            // If the frame entered is an invalid frame ->
                currentFrameCounterTextField.setText("Current Frame: 0");                                                                                       // currentFrameCounterTextField is set to its minimum value possible: 0
                selectFrameToDisplayTextField.setText("0");                                                                                                     // selectFrameToDisplayTextField is set to its minimum value possible: 0
                generalStatusText.setText("Minimum Frame Reached");                                                                                             // Displays an error message pop-up, warning the user that they can't decrement the selected frame any lower
            }

        } else {                                                                                                                                                // If the user didn't hit the decrementByOneFrameButton, the program knows they hit the incrementByOneFrameButton ->
            if (Double.parseDouble(selectFrameToDisplayTextField.getText()) < totalFrames) {                                                                    // If the entered value is a valid frame ->
                frameConvertedToMsCount += getMPF();                                      // The frame shown is increased by one frame value and assigned to frameConvertedToMsCount
                selectFrameToDisplayTextField.setText(Integer.toString(Integer.parseInt(selectFrameToDisplayTextField.getText()) + 1));                      // The frame displayed in selectFrameToDisplayTextField is increased by one
                currentFrameCounterTextField.setText("Current Frame: " + Integer.toString(Integer.parseInt(selectFrameToDisplayTextField.getText()) + 1));   // The frame displayed in the currentFrameCounterTextField is increased by one
                generalStatusText.setText("");                                                                                                                  // Clears the error message pop-up
            } else {                                                                                                                                            // If the entered value isn't a valid frame ->
                currentFrameCounterTextField.setText("Current Frame: " + (new DecimalFormat("#").format(totalFrames)));                                 // currentFrameCounterTextField is set to its maximum value possible: the calculated totalFrames double;
                selectFrameToDisplayTextField.setText(String.valueOf(new DecimalFormat("#").format(totalFrames)));                                      // selectFrameToDisplayTextField is set to its maximum value possible: the calculated totalFrames double;
                generalStatusText.setText("Maximum Frame Reached");                                                                                             // Displays an error message pop-up, warning the user that they can't increment the selected frame any higher
            }

        }
        mediaPlayer.seek(Duration.millis(frameConvertedToMsCount));                                                                                             // The mediaPlayer seeks to updated frameConvertedToMsCount value
        mediaPlayer.pause();                                                                                                                                    // Pauses the video playback
    }





    /*** BEGINNING OF DASHBOARD/GRAPH COMMUNICATIONS COMPONENT***/





    public int getCurrentFrame() {
        //wish we didnt need the user for this input ffmpeg later
        //Current Time position in milliseconds / milliseconds per frame = frame
        // Ryan Method: new DecimalFormat("#").format(mediaPlayer.getCurrentTime().toSeconds() * frameRates[selectFrameRateComboBox.getSelectionModel().getSelectedIndex()]))));
            return (int) round((mediaPlayer.getCurrentTime().toSeconds() * 1000) / getMPF());
    }

    public Boolean hasVideoSelected() {
        return videoLoaded;
    }

    public Boolean isVideoPlaying() {
        return playing;
    }

    public int getPlaybackSpeed() {
        return (int) rateChangeSlider.getValue();
    }

    public double getFPS() {
    	return videoFrameRate;
    }

    public double getMPF() {
    	return millisPerFrame;
    }

    public void readFileFPSFromFFMpeg() throws IOException {
        FfmpegSystemWrapper FfmpegSystemWrapper = new FfmpegSystemWrapper();
        FfmpegSystemWrapper.setSystemInfo();
        Process runFfmpeg = Runtime.getRuntime().exec(FfmpegSystemWrapper.getBinRoot() + "ffmpeg.exe -i \"" + fileCopy + "\"");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runFfmpeg.getErrorStream()));


        String ffmpegOutputLine;
        while ((ffmpegOutputLine = bufferedReader.readLine()) != null) {
            if ((ffmpegOutputLine.contains("fps"))) {
                String[] ffmpegOutputarray = ffmpegOutputLine.split(",");
                for (int i = 0; i < ffmpegOutputarray.length; i ++){
                    if (ffmpegOutputarray[i].contains("fps")) {
                        String[] fpsCountArray = ffmpegOutputarray[i].split(" ");
                        videoFrameRate = (int)Math.ceil(Double.parseDouble(fpsCountArray[1]));
                        millisPerFrame = 1000/videoFrameRate;
                    }
                }
            }

        }
    }
    
    public void scaleVideoAtStart() {                                                                                                                           // Scales the selected video so it's centered and scaled to fit within the bounds of the video player

        mediaView.setFitWidth(mediaViewPane.getWidth());
        mediaView.setFitHeight(mediaViewPane.getHeight());

        mediaView.setSmooth(true);
        mediaView.setPreserveRatio(true);
    }

}


