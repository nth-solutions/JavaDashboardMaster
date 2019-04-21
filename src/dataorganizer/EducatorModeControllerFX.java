package dataorganizer;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class EducatorModeControllerFX implements Initializable {



    /*
    GOALS:
    1. Finish Test Parameters List
    2. Incorporate all Methods into appropriate Buttons/ActionEvents
    3. Add a way for data from the spring lab to be input into the templates (Brandon's help probably required)
    4. In Output settings, disable read test until after a selection type has been made for output
     */

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

    /***
     * ActionEvent that shows the calibrationTab, the tab responsible for running module calibration
     *
     * @param event
     */
    @FXML
    private void selectCalibrationTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(calibrationTab);
    }

    /***
     * ActionEvent that shows the motionVisualizationTab, the tab responsible for the graph-video player interaction
     *
     * @param event
     */
    @FXML
    private void selectMotionVisualizationTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(motionVisualizationTab);
    }

    /***
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

    /***
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

    /***
     * ActionEvent that gets the selected index of the test type combo box and displays the correlated tab of the test type tab pane for user entry
     * @param event
     */
    @FXML
    private void displayTestParameterTab(ActionEvent event) {
        int selectedIndex = testTypeComboBox.getSelectionModel().getSelectedIndex();
        testParametersTabPane.getSelectionModel().select(selectedIndex);
    }


}
