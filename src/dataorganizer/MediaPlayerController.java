package dataorganizer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Key;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import static javafx.scene.media.MediaPlayer.Status.PAUSED;
import static javafx.scene.media.MediaPlayer.Status.PLAYING;

//TODO: Fullscreen video capabilities/resizing
//TODO: Add encoder that changes all file types to mp4

//IDEA: Change range on the playback slider to 0-1 with larger increments
//IDEA: Import frame rate from dashboard


public class MediaPlayerController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectFrameRateComboBox.getItems().addAll("30 fps", "60 fps", "120 fps", "240 fps");

    }


    private MediaPlayer mediaPlayer;                                                                                                             // Variable Declarations
    private String filePath;
    private double playbackRate;
    private Boolean playing = false;
    private File fileCopy;

    @FXML                                                                                                                                       // FXML component Declarations
    private MediaView mediaView;
    @FXML
    private Slider timeStampSlider;
    @FXML
    private Slider rateChangeSlider;
    @FXML
    private Button stopButton;
    @FXML
    private Button playPauseButton;
    @FXML
    private Button exitButton;
    @FXML
    private Text rateText;
    @FXML
    private Text playbackTimeText;
    @FXML
    private Text totalVideoTimeText;
    @FXML
    private Text generalStatusText;








    @FXML
    public void handleFileOpener (ActionEvent event) {
        FileChooser fileChooser = new FileChooser();                                                                                             // Creates a FileChooser Object
        fileChooser.setTitle("Select a Video File");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a File (*.mp4)", "*.mp4");             // Creates a filter that limits fileChooser's search parameters to *.mp4 files
        fileChooser.getExtensionFilters().add(filter);                                                                                           // Initializes the filter into the fileChooser object

        File file = fileChooser.showOpenDialog(null);                                                                               // Specifies the parent component for the dialog

        fileCopy = file;

        if (file != null) {                                                                                                                      // If the filepath contains a valid file the following code is initiated
            filePath = file.toURI().toString();                                                                                                  // Sets the user's selection to a file path that will be used to select the video file to be displayed
            Media media = new Media(filePath);                                                                                                   // Sets the media object to the selected file path
            mediaPlayer = new MediaPlayer(media);                                                                                                // Creates a mediaPlayer object, mediaPlayer is utilized for video playback controls
            mediaView.setMediaPlayer(mediaPlayer);                                                                                               // Sets the mediaPlayer to be the controller for the mediaVew object

            stopButton.setDisable(false);                                                                                                        // Enables buttons following a valid file selection
            playPauseButton.setDisable(false);
            exitButton.setDisable(false);
            timeStampSlider.setDisable(false);
            rateChangeSlider.setDisable(false);
            frameByFrameCheckbox.setDisable(false);


            mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {                                                       // Displays a moving slider bar that corresponds to the current point of playback within the video sequence
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                    timeStampSlider.setMax(mediaPlayer.getTotalDuration().toMillis());
                    timeStampSlider.setValue(newValue.toMillis());

                    playbackTimeText.setText(String.valueOf(new DecimalFormat("#.000").format(mediaPlayer.getCurrentTime().toSeconds()) + "s"));          // Changes current time counter according to playback time
                    totalVideoTimeText.setText(String.valueOf(new DecimalFormat("#.000").format(mediaPlayer.getTotalDuration().toSeconds()) + "s"));      // Displays the total video length in seconds
                }
            });


            mediaPlayer.play();                                                                                                                  // Begins video playback on the opening of the file
            playPauseButton.setText("Pause");
            generalStatusText.setText("");


        }

    }

    @FXML
    public void handleSelectTimeStamp(MouseEvent event) {                                                                                        // Allows user to select a point along the video's playback to resume playing
        mediaPlayer.seek(Duration.millis(timeStampSlider.getValue()));
        mediaPlayer.play();
        playPauseButton.setText("Pause");
    }

    @FXML
    public void handleSelectTimeStampKeys(KeyEvent event) {                                                                                      // Allows user to select a point along the video's playback using left and right arrow keys
        if (event.getCode()==KeyCode.RIGHT) {
            mediaPlayer.seek(Duration.millis(timeStampSlider.getValue() * 1.05));
            mediaPlayer.play();
        } else if (event.getCode()==KeyCode.LEFT) {
            mediaPlayer.seek(Duration.millis(timeStampSlider.getValue() * .95));
            mediaPlayer.play();
        }
        playPauseButton.setText("Pause");
    }

    @FXML
    public void handlePausePlaybackOnKeyPress(KeyEvent event) {                                                                                  // Pauses the video to prevent change listener conflicts in the event a user implements the handleSelectTimeStampKeys Key listener
        if (event.getCode()==KeyCode.RIGHT || event.getCode()==KeyCode.LEFT) {
            mediaPlayer.pause();
        }
        //playPauseButton.setText("Play");
    }

    @FXML
    public void handleSelectTimeStampDrag(MouseEvent event){                                                                                     // Prevents the ChangeListener from overwriting slider placement
        mediaPlayer.pause();
        playPauseButton.setText("Play");
    }

    @FXML
    public void handlePlayPauseVideo(ActionEvent event) {                                                                                             // Event listener responsible for handling the 'play' button
        if (playing) {
            mediaPlayer.play();
            playPauseButton.setText("Pause");
            playing = false;
        } else {
            mediaPlayer.pause();
            playPauseButton.setText("Play");
            playing = true;
        }

    }

    @FXML
    public void handlePlayVideoKey(KeyEvent event) {                                                                                              // Event listener responsible for assigning the space key to a play function
        if (event.getCode() != KeyCode.SPACE) {
            mediaPlayer.play();
            playPauseButton.setText("Pause");
        }
    }




    @FXML
    public void handleResetVideo(ActionEvent event) throws InterruptedException {
        mediaPlayer.dispose();                                                                                                                   // Just obliterates it all
        if (fileCopy != null) {                                                                                                                  // If the filepath contains a valid file the following code is initiated
            filePath = fileCopy.toURI().toString();                                                                                              // Sets the user's selection to a file path that will be used to select the video file to be displayed
            Media media = new Media(filePath);                                                                                                   // Sets the media object to the selected file path
            mediaPlayer = new MediaPlayer(media);                                                                                                // Creates a mediaPlayer object, mediaPlayer is utilized for video playback controls
            mediaView.setMediaPlayer(mediaPlayer);                                                                                               // Sets the mediaPlayer to be the controller for the mediaVew object

            stopButton.setDisable(false);                                                                                                        // Enables buttons following a valid file selection
            selectFrameToDisplayTextField.setDisable(true);
            selectFrameRateComboBox.setDisable(true);
            goToFrameButton.setDisable(true);
            decrementOneFrameButton.setDisable(true);
            incrementOneFrameButton.setDisable(true);

            mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {                                                       // Displays a moving slider bar that corresponds to the current point of playback within the video sequence
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                    timeStampSlider.setMax(mediaPlayer.getTotalDuration().toMillis());
                    timeStampSlider.setValue(newValue.toMillis());

                    playbackTimeText.setText(String.valueOf(new DecimalFormat("#.000").format(mediaPlayer.getCurrentTime().toSeconds()) + "s"));          // Changes current time counter according to playback time
                    totalVideoTimeText.setText(String.valueOf(new DecimalFormat("#.000").format(mediaPlayer.getTotalDuration().toSeconds()) + "s"));      // Displays the total video length in seconds
                }
            });



            mediaPlayer.play();
            playPauseButton.setText("Pause");
            timeStampSlider.setValue(0);
            playbackTimeText.setText("000s");
            frameByFrameCheckbox.setSelected(false);
        }
    }

    @FXML
    public void handleExitProgram(ActionEvent event) {                                                                                           // Event listener responsible for handling the 'Exit' button
        System.exit(0);
    }

    @FXML
    public void handleRateChange(MouseEvent event) {                                                                                             // The rate method supports playback rates between 0.0 and 8.0. This mouse listener
        playbackRate = rateChangeSlider.getValue();                                                                                              // is responsible for gathering the rate assigned to the rateChangeSlider and changing
        mediaPlayer.setRate(playbackRate);                                                                                                       // the playback speed accordingly

        rateText.setText((Double.toString(Math.floor(mediaPlayer.getRate() * 10) / 10)) + "x");
    }

    @FXML
    public void handleRateChangeKeys(KeyEvent event) {
        playbackRate = rateChangeSlider.getValue();                                                                                              //This method supports the movement of the playback rate slider with the movement of key presses
        mediaPlayer.setRate(playbackRate);

        rateText.setText((Double.toString(Math.floor(mediaPlayer.getRate() * 10) / 10)) + "x");
    }





    //IN TESTING: MAY THROW ERRORS
    //TODO: Make frame handlers permanently store values so as not to have to select the frame rate multiple times - Rewrite drop down menu
    //TODO: Throw error message when text is entered into the frame selection box & if number is out of bounds
    //TODO: Add Single Step Frame Move
    //TODO: Total Frame Count



    @FXML
    private CheckBox frameByFrameCheckbox;
    @FXML
    private TextField selectFrameToDisplayTextField;
    @FXML
    private ComboBox selectFrameRateComboBox;
    @FXML
    private Button goToFrameButton;
    @FXML
    private Button decrementOneFrameButton;
    @FXML
    private Button incrementOneFrameButton;
    @FXML
    private Text totalFrameText;
    @FXML
    private Button totalFrameDisplayButton;


    double totalDuration;
    double frameSelected;
    double frameToDisplay;
    double[] millisPerFrame = {33.333, 16.666 , 8.333, 4.166};                                                                                  // Each value within the array corresponds to the number of milliseconds per frame at recording speeds of 30, 60, 120, and 240 fps



    @FXML
    public void handleSelectFrameByFrameAnalysis(ActionEvent event) {
        if (frameByFrameCheckbox.isSelected()) {
            selectFrameToDisplayTextField.setDisable(false);
            mediaPlayer.pause();
            playPauseButton.setText("Play");
        } else if (!frameByFrameCheckbox.isSelected()) {
            selectFrameToDisplayTextField.setDisable(true);
            selectFrameRateComboBox.setDisable(true);
            goToFrameButton.setDisable(true);
            incrementOneFrameButton.setDisable(true);
            decrementOneFrameButton.setDisable(true);
        }
    }


    @FXML
    public void handleUnlockFrameRateSelectionButton(ActionEvent event) {                                                                           // Enables/Disables the frame by frame analysis buttons when the frameByFrameCheckbox is selected/de-selected
        try {
            if (frameSelected >= 0 && frameToDisplay <= totalDuration) {
                Integer.parseInt(selectFrameToDisplayTextField.getText());
                selectFrameRateComboBox.setDisable(false);
                goToFrameButton.setDisable(false);
                incrementOneFrameButton.setDisable(false);
                decrementOneFrameButton.setDisable(false);
                totalFrameDisplayButton.setDisable(false);
            } else {
                generalStatusText.setText("Not a Number");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            generalStatusText.setText("Was it text?");
        }

    }




    @FXML
    public void handleSeekToSelectedFrame(ActionEvent event) {                                                                                                       // Seeks video playback to the selected frame rate

        totalDuration = mediaPlayer.getTotalDuration().toMillis();
        frameSelected = Double.parseDouble(selectFrameToDisplayTextField.getText());
        frameToDisplay = frameSelected * millisPerFrame[selectFrameRateComboBox.getSelectionModel().getSelectedIndex()];


        if (frameToDisplay >= totalDuration && frameSelected <= 0) {                                                                                                  // Conditional prevents the user from selecting an out-of-bounds frame and throwing a compile error
                generalStatusText.setText("Frame Out of Bounds.");
            } else {
                mediaPlayer.seek(Duration.millis(frameToDisplay));
                mediaPlayer.pause();
                playPauseButton.setText("Play");
            }
    }

    @FXML
    public void handleFrameCounterChangeByOne(ActionEvent event) {
        if(event.getSource() == decrementOneFrameButton){
            frameToDisplay -= millisPerFrame[selectFrameRateComboBox.getSelectionModel().getSelectedIndex()];
            selectFrameToDisplayTextField.setText(Integer.toString(Integer.parseInt(selectFrameToDisplayTextField.getText())-1));
        }else{
            frameToDisplay += millisPerFrame[selectFrameRateComboBox.getSelectionModel().getSelectedIndex()];
            selectFrameToDisplayTextField.setText(Integer.toString(Integer.parseInt(selectFrameToDisplayTextField.getText())+1));
        }
        mediaPlayer.seek(Duration.millis(frameToDisplay));
        mediaPlayer.pause();
    }

    @FXML
    public void handleDisplayTotalFrames(ActionEvent event) {
        totalFrameText.setText(String.valueOf(mediaPlayer.getTotalDuration().toMillis() * millisPerFrame[selectFrameRateComboBox.getSelectionModel().getSelectedIndex()]));
        totalFrameText.setVisible(true);
    }


    public int getCurrentFrame(){
        //wish we didnt need the user for this input ¯\_(ツ)_/¯ ffmpeg later
        //Current Time position in milliseconds / milliseconds per frame = frame
        return (int)Math.round((mediaPlayer.getCurrentTime().toSeconds()*1000) / millisPerFrame[selectFrameRateComboBox.getSelectionModel().getSelectedIndex()]);
    }
}
