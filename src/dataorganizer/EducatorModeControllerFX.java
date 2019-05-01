package dataorganizer;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class EducatorModeControllerFX implements Initializable {



    /*
    GOALS:
    1. Finish Test Parameters List
    2. Incorporate all Methods into appropriate Buttons/ActionEvents
    3. Add a way for data from the spring lab to be input into the templates (Brandon's help probably required)
    4. In Output settings, disable read test until after a selection type has been made for output
    5. Only allow one output radio button to be selected at a time
     */

    //Primary UI Control FXML Components
    @FXML
    TabPane primaryTabPane;
    @FXML
    TabPane experimentTabPane;
    @FXML
    TabPane testParametersTabPane;
    @FXML
    Tab experimentTab;
    @FXML
    Tab motionVisualizationTab;
    @FXML
    Tab settingsTab;
    @FXML
    Button backButton;
    @FXML
    ComboBox<String> testTypeComboBox;

    //Experiment FXML Components
    @FXML
    Label generalStatusExperimentLabel;
    @FXML
    Button pairNewRemoteButton;
    @FXML
    Button unpairAllRemotesButton;
    @FXML
    Button testRemotesButton;






    private int experimentTabIndex = 0;
    private static SerialComm serialHandler;
    private HashMap<String, ArrayList<Integer>> testTypeHashMap = new HashMap<String, ArrayList<Integer>>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        testTypeComboBox.getItems().addAll("Conservation of Momentum (Elastic Collision)", "Conservation of Energy", "Inclined Plane", "Physical Pendulum", "Spring Test - Simple Harmonics");
        backButton.setVisible(false);
        serialHandler = new SerialComm();
    }


    /**
     * ActionEvent that shows the experimentTab, the tab responsible for setting up and interacting with the module
     *
     * @param event
     */
    @FXML
    private void selectExperimentTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(experimentTab);
    }

    /**
     * ActionEvent that shows the calibrationTab, the tab responsible for running module calibration
     *
     * @param event
     */
    @FXML
    private void selectSettingsTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(settingsTab);
    }

    /**
     * ActionEvent that shows the motionVisualizationTab, the tab responsible for the graph-video player interaction
     *
     * @param event
     */
    @FXML
    private void selectMotionVisualizationTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(motionVisualizationTab);
    }

    /**
     * ActionEvent that increments the tab index by one to move to the next tab in the experimental tab pane
     *
     * @param event
     */
    @FXML
    private void nextTab(ActionEvent event) {
        generalStatusExperimentLabel.setText("");   // Resets the status text to blank for each new page

        if (experimentTabIndex == 4) {  // If the Index is 4, the maximum tab index has been reached and the index is reset to origin
            experimentTabIndex = -1;
        }

        experimentTabIndex += 1;    // Increments the tab index each time the ActionEvent is triggered

        if (experimentTabIndex == 0) {   // If the tab index is 0, the back button is hidden because no previous pane exists
            backButton.setVisible(false);
        } else {
            backButton.setVisible(true);
        }

        experimentTabPane.getSelectionModel().select(experimentTabIndex);

    }

    /**
     * ActionEvent that decrements the tab index by one to move to the previous tab in the experimental tab pane
     *
     * @param event
     */
    @FXML
    private void backTab(ActionEvent event) {
        generalStatusExperimentLabel.setText("");   // Resets the status text to blank for each new page

        if (experimentTabIndex != 0) {  // If the index does not equal 0 (the first pane), the index will decrement
            experimentTabIndex -= 1;
        }

        if (experimentTabIndex == 0) {
            backButton.setVisible(false);
        } else {
            backButton.setVisible(true);
        }

        experimentTabPane.getSelectionModel().select(experimentTabIndex);
    }

    /**
     * ActionEvent that gets the selected index of the test type combo box and displays the correlated tab of the test type tab pane for user entry
     *
     * @param event
     */
    @FXML
    private void displayTestParameterTab(ActionEvent event) {
        int selectedIndex = testTypeComboBox.getSelectionModel().getSelectedIndex();
        testParametersTabPane.getSelectionModel().select(selectedIndex);
    }


    /* Begin Experiment Tab Methods */

    @FXML
    private void pairNewRemote(ActionEvent event) {

        Runnable pairNewRemoteOperation = () -> {
            //Disable buttons that should not be used in the middle of a sequence
            pairNewRemoteButton.setDisable(true);
            unpairAllRemotesButton.setDisable(true);
            testRemotesButton.setDisable(true);

            generalStatusExperimentLabel.setText("Module Listening for New Remote, Hold 'A' or 'B' Button to Pair");

            try {
                if(serialHandler.pairNewRemote()) {
                    generalStatusExperimentLabel.setText("New Remote Successfully Paired");

                }
                else {
                    generalStatusExperimentLabel.setText("Pair Unsuccessful, Receiver Timed Out");

                }


            }
            catch (IOException e) {
                generalStatusExperimentLabel.setText("Error Communicating With Serial Dongle");

            }
            catch (PortInUseException e) {
                generalStatusExperimentLabel.setText("Serial Port Already In Use");

            }
            catch (UnsupportedCommOperationException e) {
                generalStatusExperimentLabel.setText("Check Dongle Compatability");

            }

            //Enable buttons that can now be used since the bulk erase completed
            pairNewRemoteButton.setDisable(false);
            unpairAllRemotesButton.setDisable(false);
            testRemotesButton.setDisable(false);


        };

        //Define a new thread to run the operation previously defined
        Thread pairNewRemoteThread = new Thread(pairNewRemoteOperation);
        //Start the thread
        pairNewRemoteThread.start();
    }






    /*End Experiment Tab Methods*/







    /*Begin Motion Visualization Tab Methods*/

    //Under Development... Will have methods soon

    /*End Motion Visualization Tab Methods*/




    /* Module Parameter Settings */

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

    /***
     *  Fills the testTypeHashMap with the module settings associated with each test type
     *
     * @param timedTest
     */
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
        testParams.add(4);
        //10 Gyro Sensitivity
        testParams.add(1000);
        //11 Accel Filter
        testParams.add(92);
        //12 Gyro Filter
        testParams.add(92);

        testTypeHashMap.put("Spring Test - Simple Harmonics", testParams);

        testParams.clear();

        //TODO: Finish adding test type parameters
    }



}


