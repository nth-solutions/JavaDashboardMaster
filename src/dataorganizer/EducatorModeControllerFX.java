package dataorganizer;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;


import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    Tab calibrationTab;
    @FXML
    Tab motionVisualizationTab;
    @FXML
    Button backButton;
    @FXML
    ComboBox<String> testTypeComboBox;

    //Calibration FXML Components
    @FXML
    Button configureForCalibrationButton;
    @FXML
    Button importCalibrationDataButton;
    @FXML
    Button applyOffsetButton;
    @FXML
    Label generalStatusLabel;
    @FXML
    TextField videoFilePathTextField;
    @FXML
    TextField delayAfterStartTextField;
    @FXML
    TextField timer0OffsetTextField;


    private int experimentTabIndex = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        testTypeComboBox.getItems().addAll("Conservation of Momentum (Elastic Collision)", "Conservation of Angular Momentum", "Conservation of Energy", "Inclined Plane", "Physical Pendulum", "Spinny Stool", "Spring Test - Simple Harmonics");
        backButton.setVisible(false);
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
    private void selectCalibrationTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(calibrationTab);
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


    //TODO: Analyze method implementation in Swing and convert all methods to FX



    /*End Experiment Tab Methods*/


    /* Begin Calibration Tab Methods*/


    //TODO: Fix the commented out methods once threading and serial handling has been added

//    /**
//     * ActionEvent that configures the module for calibration
//     * @param event
//     */
//    @FXML
//    private void configureModuleForCalibration(ActionEvent event) {
//        backButton.setDisable(true);
//        importCalibrationDataButton.setDisable(false);
//        applyOffsetButton.setDisable(true);
//
//        try {
//            if(!serialHandler.configForCalibration()) {                                                                 //TODO: Figure out this import
//                generalStatusLabel.setText("Error Communicating With Module");
//            }
//            else {
//                generalStatusLabel.setText("Module Configured for Calibration, Use Configuration Tab to Exit");
//            }
//
//            backButton.setDisable(false);
//            importCalibrationDataButton.setDisable(false);
//            applyOffsetButton.setDisable(false);
//        }
//        catch (IOException e) {
//            generalStatusLabel.setText("Error Communicating With Serial Dongle");
//        }
//        catch (PortInUseException e) {
//            generalStatusLabel.setText("Serial Port Already In Use");
//        }
//        catch (UnsupportedCommOperationException e) {
//            generalStatusLabel.setText("Check Dongle Compatability");
//        }
//
//		Thread calConfigThread = new Thread(calforConfigOperation);                                                     //TODO: Figure out this import
//                calConfigThread.start();
//    }

    /**
     * ActionEvent that opens a file chooser window and sets the file path as a string within the text field
     * @param event
     */
    @FXML
    private void browseVideoFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Calibration Video File");
        chooser.setInitialDirectory(new java.io.File("."));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*"));


        File file = chooser.showOpenDialog(null);
        videoFilePathTextField.setText(file.toString());

    }

    //    /**
//     * ActionEvent that imports the calibration video, calculates the necessary timer0 and Delay settings, and displays the result
//     * @param event
//     */
//    @FXML
//    private void importCalibrationData(ActionEvent event) {
//
//        configureForCalibrationButton.setDisable(true);
//        importCalibrationDataButton.setDisable(true);
//        applyOffsetButton.setDisable(true);
//        try {
//
//            BlackFrameAnalysis bfo = new BlackFrameAnalysis(videoFilePathTextField.getText());
//
//            delayAfterStartTextField.setText(Integer.toString(bfo.getDelayAfterStart()));
//            timer0OffsetTextField.setText(Integer.toString(bfo.getTMR0Offset()));
//
//            configureForCalibrationButton.setDisable(false);
//            importCalibrationDataButton.setDisable(false);
//            applyOffsetButton.setDisable(false);
//
//        } catch (IOException e) {
//            generalStatusLabel.setText("Error Communicating With Serial Dongle");
//        }
//
//        Thread getConfigsOperationThread = new Thread(getConfigsOperation);                                             //TODO: Figure out what this does
//        getConfigsOperationThread.start();
//    }

//    /**
//     * ActionEvent that applies the calculated offset to the module firmware
//     * @param event
//     */
//    @FXML
//    private void applyOffset(ActionEvent event) {
//        configureForCalibrationButton.setDisable(true);
//        importCalibrationDataButton.setDisable(true);
//        applyOffsetButton.setDisable(true);
//
//        try {
//            if(!serialHandler.applyCalibrationOffsets(Integer.parseInt(timer0OffsetTextField.getText()), Integer.parseInt(delayAfterStartTextField.getText()))) { //Constant 0 because we dont do Timer0 Calibration... yet
//                generalStatusLabel.setText("Error Communicating With Module");
//
//            }
//            else {
//                generalStatusLabel.setText("Offset Successfully Applied, Camera and Module are now Synced");
//
//            }
//
//            configureForCalibrationButton.setDisable(false);
//            importCalibrationDataButton.setDisable(false);
//            applyOffsetButton.setDisable(false);
//
//        }
//        catch (IOException e) {
//            generalStatusLabel.setText("Error Communicating With Serial Dongle");
//        }
//        catch (PortInUseException e) {
//            generalStatusLabel.setText("Serial Port Already In Use");
//        }
//        catch (UnsupportedCommOperationException e) {
//            generalStatusLabel.setText("Check Dongle Compatability");
//        }
//        Thread applyOffsetsHandlerThread = new Thread(getConfigsOperation);
//        applyOffsetsHandlerThread.start();
//    }

    /* End Calibration Tab Methods*/




    /*Begin Motion Visualization Tab Methods*/

    //Under Development... Will have methods soon

    /*End Motion Visualization Tab Methods*/



}


