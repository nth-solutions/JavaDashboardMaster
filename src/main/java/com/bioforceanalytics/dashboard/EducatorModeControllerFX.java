package com.bioforceanalytics.dashboard;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;

public class EducatorModeControllerFX implements Initializable {

    //Out Files Deleted before push
    //
    //Test Parameter Variables and Constants
    public final int NUM_TEST_PARAMETERS = 13;
    public final int NUM_ID_INFO_PARAMETERS = 3;
    public final int CURRENT_FIRMWARE_ID = 26;

    private SerialComm serialHandler;

    //Primary UI Control FXML Components
    @FXML
    TabPane primaryTabPane;
    @FXML
    TabPane experimentTabPane;
    @FXML
    TabPane testParametersTabPane;
    @FXML
    TabPane remotePairingTabPane;
    @FXML
    Tab experimentTab;
    @FXML
    Tab motionVisualizationTab;
    @FXML
    Tab settingsTab;
    @FXML
    Tab sincCalibrationTab;
    @FXML
    Tab eraseConfirmationTab;

    @FXML
    Tab unpairRemotesTab;

    @FXML
    TabPane twoModuleExperimentTabPane;

    @FXML
    Tab runExperimentTab;

    @FXML
    Button nextButton;
    @FXML
    Button backButton;
    @FXML
    ComboBox<String> testTypeComboBox;
    //Experiment FXML Components
    @FXML
    Label generalStatusExperimentLabel;
    @FXML
    Button applyConfigurationsButton;
    @FXML
    Button pairNewRemoteButton;
    @FXML
    Button unpairAllRemotesButton;
    @FXML
    Button testRemotesButton;
    @FXML
    Button exitTestModeButton;
    @FXML
    RadioButton spreadsheetRadioButton;
    @FXML
    RadioButton graphRadioButton;
    @FXML
    RadioButton graphAndSpreadsheetRadioButton;
    @FXML
    RadioButton sincTechnologyRadioButton;
    @FXML
    RadioButton DAGRadioButton;
    @FXML
    Button readTestButton;
    @FXML
    Button sincCalibrationButton;
    @FXML
    ProgressBar progressBar;
    @FXML
    Button eraseButton;

    @FXML
    ToggleGroup outputType;

    @FXML
    Button eraseModuleButtonMainMenu;

    @FXML
    Button unpairRemotesButtonMainMenu;
    //Extra Test Parameter TextFields

    @FXML
    Label sincCalibrationTabGeneralStatusLabel;

    @FXML
    Label eraseModuleTabLabel;

    @FXML
    Label unpairRemotesTabLabel;


    @FXML
    TextField massOfLeftModuleTextField;


    @FXML
    TextField massOfLeftGliderTextField;
    @FXML
    TextField massOfRightModuleTextField;
    @FXML
    TextField massOfRightGliderTextField;
    @FXML
    TextField totalDropDistanceTextField;
    @FXML
    TextField massOfModuleAndHolderTextField;
    @FXML
    TextField momentOfInertiaCOETextField;
    @FXML
    TextField radiusOfTorqueArmCOETextField;
    @FXML
    TextField lengthOfPendulumTextField;
    @FXML
    TextField distanceFromPivotTextField;
    @FXML
    TextField massOfModuleTextField;
    @FXML
    TextField massOfHolderTextField;
    @FXML
    TextField springConstantTextField;
    @FXML
    TextField totalHangingMassTextField;
    @FXML
    TextField amplitudeSpringTextField;
    @FXML
    TextField massOfSpringTextField;
    @FXML
    TextField bottomAngle;
    @FXML
    TextField topAngle;
    @FXML
    Label applyConfigurationsToFirstModuleLabel;

    //Extra Module Parameters - CoM
    double massOfRightModule;
    double massOfRightGlider;
    double massOfLeftModule;
    double massOfLeftGlider;

    double massOfRightModuleAndRightGlider;
    double massOfLeftModuleAndLeftGlider;

    // Extra Module Parameters - CoE
    double totalDropDistance;
    double massOfModuleAndHolder;
    double momentOfInertiaCOE;
    double radiusOfTorqueArmCOE;
    // Extra Module Parameters - Pendulum
    double lengthOfPendulum;
    double distanceFromPivot;
    double massOfModule;
    double massOfHolder;
    // Extra Module Parameters - Spring
    double springConstant;
    double totalHangingMass;

    double amplitudeSpring;
    double massOfSpring;

    double angleFromBottom;
    double angleFromTop;

    private DataOrganizer dataOrgo;
    private DataOrganizer dataOrgoTwo;

    private int experimentType;
    
    // holds test data from modules
    // each GT represents a single trial from a module
    private ArrayList<GenericTest> genericTests;
    private ArrayList<DataOrganizer> dataOrgoList;

    // Colors
    private final Color LIGHT_GREEN = Color.rgb(51, 204, 51);

    private final String STYLE_SUCCESS = "-fx-accent: green";
    private final String STYLE_WORKING = "-fx-accent: #1f78d1";
    private final String STYLE_FAIL = "-fx-accent: red";

    // Dashboard Background Functionality
    private int experimentTabIndex = 0;
    private int selectedIndex = 0;
    private HashMap<String, ArrayList<Integer>> testTypeHashMap = new HashMap<>();
    public static String testType;

    // indicates the number of pages in the Experiment tab
    private final int NUM_OF_STEPS = 4;

    // actually indicates the last page for two-module tests;
    // since indices 0-4 are one-module, 5-10 are two-module
    private final int NUM_OF_STEPS_TWO_MODULE = 10;

    private Boolean oneModuleTest;

    private String selectedTab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //primaryTabPane.getSelectionModel().select(experimentTab);

        genericTests = new ArrayList<GenericTest>();
        dataOrgoList = new ArrayList<DataOrganizer>();

        testTypeComboBox.getItems().addAll("Conservation of Momentum (Elastic Collision)", "Conservation of Energy", "Inclined Plane - Released From Top", "Inclined Plane - Projected From Bottom", "Physical Pendulum", "Spring Test - Simple Harmonics","Generic Template - One Module","Generic Template - Two Modules"); //Create combobox of test names so users can select Test type that he / she wants to perform.
        backButton.setVisible(false);                                                                                   //Test selection is the first pane after the program is opened; it would not make sense to have a back button on the first pane.                                                                                   //See Method Comment
        fillTestTypeHashMap();                                                                                         //See Method Comment

        applyConfigurationsToFirstModuleLabel.setVisible(false);
        applyConfigurationsButton.setVisible(false);

        serialHandler = new SerialComm();

        // automatically connect to the module
        connectToModule(generalStatusExperimentLabel);
        oneModuleTest = true;
    }

    /**
     * ActionEvent that shows the experimentTab, the tab responsible for setting up and interacting with the module
     *
     * @param event
     */
    @FXML
    private void selectExperimentTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(experimentTab);
        selectedTab = "experimentTab";
    }

    /**
     * ActionEvent that shows the calibrationTab, the tab responsible for running module calibration
     *
     * @param event
     */
    @FXML
    private void selectSettingsTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(settingsTab);
        selectedTab = "settingsTab";
    }

    @FXML
    public EducationModeHelpMenuController helpmenu;

    @FXML
    private void selectHelpTab(ActionEvent event){
        helpmenu = startHelpMenu();

        if(selectedTab == "eraseConfirmationTab"){
            helpmenu.selectEraseModuelHelpTabOne();

        }else if (selectedTab == "motionVisualizationTab"){
            helpmenu.selectSINCTechnologyHelpTab();

        }else if (selectedTab == "sincCalibrationTab"){
            helpmenu.selectSINCModuleCalibrationTab();

        }else if (selectedTab == "experimentTab"){

            if(oneModuleTest){
                if(experimentTabIndex == 0){
                    helpmenu.selectExperimentHelpTabOne();
                }
                if(experimentTabIndex == 1){
                    helpmenu.selectExperimentHelpTabTwo();
                }
                if(experimentTabIndex == 2){
                    helpmenu.selectExperimentHelpTabThree();
                }
                if(experimentTabIndex == 3){
                    helpmenu.selectExperimentHelpTabFour();
                }
                if(experimentTabIndex == 4){
                    helpmenu.selectExperimentHelpTabFive();
                }
            }else if(!oneModuleTest){
                if(experimentTabIndex == 0){
                    helpmenu.selectExperimentHelpTabOne();
                }else{
                    helpmenu.selectBlankTab();
                }
            }
        }else if (selectedTab == "unpairRemotesTab"){
            helpmenu.selectUnpairRemotesHelpTab();
        }
    }

    @FXML
    SINCCalibrationHelpMenuController SINCCalibrationHelpMenu;

    @FXML
    public void launchSINCCalibrationHelpMenu(){
        SINCCalibrationHelpMenu = startSINCHelpMenu();
    }

    @FXML
    public EducationModeHelpMenuController startHelpMenu() {
        Stage primaryStage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/EducationModeHelpMenu.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(root!=null) primaryStage.setScene(new Scene(root, 1000, 600));

        primaryStage.setTitle("Education Mode Help Menu");
        primaryStage.show();
        primaryStage.setResizable(false);

        return loader.getController();
    }

    @FXML
    public SINCCalibrationHelpMenuController startSINCHelpMenu() {
        Stage primaryStage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/SINCCalibrationHelpMenu.fxml"));
        try{
            root = loader.load();
        }catch (IOException e){
            e.printStackTrace();
        }
        if(root!=null) primaryStage.setScene(new Scene(root, 1000, 800));

        primaryStage.setTitle("SINC Calibration Help Menu");
        primaryStage.show();
        primaryStage.setResizable(false);

        return loader.getController();
    }

    /**
     * ActionEvent that shows the motionVisualizationTab, the tab responsible for the graph-video player interaction
     *
     * @param event
     */
    @FXML
    private void selectMotionVisualizationTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(motionVisualizationTab);
        selectedTab = "motionVisualizationTab";
    }

    @FXML
    private void selectSINCCalibration(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(sincCalibrationTab);
        selectedTab = "sincCalibrationTab";
    }

    @FXML
    private void selectEraseConfirmationTab(ActionEvent event){
        primaryTabPane.getSelectionModel().select(eraseConfirmationTab);
        selectedTab = "eraseConfirmationTab";
    }

    @FXML
    private void selectUnpairRemotesTab(ActionEvent event){
        primaryTabPane.getSelectionModel().select(unpairRemotesTab);
        remotePairingTabPane.getSelectionModel().select(0);
        selectedTab = "unpairRemotesTab";
    }

    @FXML
    private void selectPairSingleModuleTab(ActionEvent event){
        remotePairingTabPane.getSelectionModel().select(1);
    }

    @FXML
    private void selectPairTwoModulesTab(ActionEvent event){
        remotePairingTabPane.getSelectionModel().select(2);
    }

    @FXML
    private void selectPairTwoModulesTabPageTwo(ActionEvent event){
        remotePairingTabPane.getSelectionModel().select(3);
    }

    @FXML
    private void selectPairTwoModulesTabPageThree(ActionEvent event){
        remotePairingTabPane.getSelectionModel().select(4);
    }


    @FXML
    private void goBackToIntialTabPairingRemotes(ActionEvent event){
        remotePairingTabPane.getSelectionModel().select(0);
    }

    /**
     * Moves the experiment tab to the next page.
     * Indices 0-4 are the one-module steps, 5-10 
     */
    @FXML
    private void nextTab() {

        // reset progress bar and status label
        displayProgress("", Color.BLACK, STYLE_WORKING, 0);

        // if on the last step, reset back to the first step
        if (experimentTabIndex == (oneModuleTest ? NUM_OF_STEPS : NUM_OF_STEPS_TWO_MODULE)) {

            experimentTabIndex = 0;

            // disable "next" and hide "back"
            nextButton.setDisable(true);
            backButton.setVisible(false);

        }
        // if this is a two-module test and the user clicks next on step 1, skip to the two-module steps at index 5
        else if (!oneModuleTest && experimentTabIndex == 0) {
            experimentTabIndex = 5;
            backButton.setVisible(true);
        }
        // otherwise, move to the next step
        else {
            experimentTabIndex += 1;
            backButton.setVisible(true);
        }

        // set the tab based on the index
        experimentTabPane.getSelectionModel().select(experimentTabIndex);

    }

    /**
     * ActionEvent that decrements the tab index by one to move to the previous tab in the experimental tab pane
     *
     * @param event
     */
    @FXML
    private void backTab(ActionEvent event) {

        // reset progress bar and status label
        displayProgress("", Color.BLACK, STYLE_WORKING, 0);

        // for one-module, if on any step other
        if ((experimentTabIndex != 0 && oneModuleTest) || (experimentTabIndex != NUM_OF_STEPS && !oneModuleTest)) {
            experimentTabIndex -= 1;
            backButton.setVisible(true);
        }

        // if user reaches last page after decrementing, hide "back"
        // if two-module, allow index to drop to "-1" and then jump to 0
        if (experimentTabIndex == 0 || experimentTabIndex == NUM_OF_STEPS) {

            if (!oneModuleTest) experimentTabIndex = 0;            
            backButton.setVisible(false);

        }

        // set the tab based on the index
        experimentTabPane.getSelectionModel().select(experimentTabIndex);
    }

    /**
     * ActionEvent that gets the selected index of the test type combo box and displays the correlated tab of the test type tab pane for user entry
     *
     * @param event
     */
    @FXML
    private void displayTestParameterTab(ActionEvent event) {
        selectedIndex = testTypeComboBox.getSelectionModel().getSelectedIndex() + 1; //Gets the index of the test type selected by user within the combobox
        testParametersTabPane.getSelectionModel().select(selectedIndex); //Since the number of tabs matches the length of the combobox selection model, the user's
        //selected index is used to select the matching tab pane index to display

        if(selectedIndex == 1 || selectedIndex == 2 || selectedIndex == 8 ){ //this means a test involving two modules is selected.
            applyConfigurationsToFirstModuleLabel.setText("Apply your configurations to Module 1");
            oneModuleTest = false;
        }else{
            applyConfigurationsToFirstModuleLabel.setText("Apply your configurations");
            oneModuleTest = true;
        }

        applyConfigurationsToFirstModuleLabel.setVisible(true);
        applyConfigurationsButton.setVisible(true);
    }

    /* Begin Experiment Tab Methods */

    @FXML
    private void connectExperimentTab() {
        connectToModule(generalStatusExperimentLabel);
    }

    @FXML
    private void connectRemoteTab() {
        connectToModule(unpairRemotesTabLabel);
    }

    @FXML
    private void connectEraseTab() {
        connectToModule(eraseModuleTabLabel);
    }

    @FXML
    private void connectSINCTab() {
        connectToModule(sincCalibrationTabGeneralStatusLabel);
    }

    /**
     * ActionEvent that writes the selected parameters and module configurations to the module for testing
     *
     * @param event
     */
    @FXML
    private void applyConfigurations(ActionEvent event) {
        writeButtonHandler();
    }

    @FXML
    private void applyConfigurations2(ActionEvent event){
        writeButtonHandler();
    }

    /**
     * A handler method called within the applyConfigurations() ActionEvent that writes pre-defined optimal parameters
     * to the module's firmware for use in one of several experiments
     */
    private void writeButtonHandler() {

        // reset status label and progress bar
        displayProgress("Saving test parameters...", Color.BLUE, STYLE_WORKING, 0.5);

        // checks if a test is selected before continuing; shouldn't normally run but just a safeguard
        if (testParametersTabPane.getSelectionModel().getSelectedIndex() == 0) { 
            displayProgress("Please choose a test type", Color.RED, STYLE_FAIL, 1);
            return;
        }

        //Disable write config button while the sendParameters() method is running
        applyConfigurationsButton.setDisable(true);
        nextButton.setDisable(true);


        String testName = testTypeComboBox.getSelectionModel().getSelectedItem();
        ArrayList<Integer> testParams = testTypeHashMap.get(testTypeComboBox.getSelectionModel().getSelectedItem());

        System.out.println(testName);
        System.out.println(testParams);

        Task<Void> sendTestParamsTask = new Task<Void>() {

            @Override
            protected Void call() {
                
                try {
                    
                    // update test information in EMFX
                    if (serialHandler.sendTestParams(testParams)) { 
                        updateTestParams(selectedIndex);
                        displayProgress("Test parameters saved successfully", Color.GREEN, STYLE_SUCCESS, 1);
                    } else {
                        displayProgress("Error saving test parameters to module, try again", Color.RED, STYLE_FAIL, 1);
                    }

                } catch (NumberFormatException e) {
                    displayProgress("Check that parameters are filled out correctly", Color.RED, STYLE_FAIL, 1);
                } catch (IOException e) {
                    displayProgress("Connection to module lost, try again", Color.RED, STYLE_FAIL, 1);
                } catch (PortInUseException e) {
                    displayProgress("Port in use by another application", Color.RED, STYLE_FAIL, 1);
                } catch (UnsupportedCommOperationException e) {
                    System.out.println("Unsupported comm operation");
                    displayProgress("Check USB dongle compatibility", Color.RED, STYLE_FAIL, 1);
                }

                // re-enable buttons
                Platform.runLater(() -> {
                    applyConfigurationsButton.setDisable(false);
                    nextButton.setDisable(false);
                });

                return null;

            }

        };

        // send test parameters asynchronously
        new Thread(sendTestParamsTask).start();
        
    }

    /**
     * Updates test parameters for the "Apply Configurations" handler.
     * Throws NumberFormatException to be caught in the aforementioned method.
     * @param comboBoxIndex index of the selected test in the dropdown menu
     */
    private void updateTestParams(int comboBoxIndex) throws NumberFormatException {

        // reset progress bar and status label
        experimentType = comboBoxIndex;

        switch (comboBoxIndex) {
            case 1:
                massOfRightModule = Double.parseDouble(massOfRightModuleTextField.getText());
                massOfRightGlider = Double.parseDouble(massOfRightGliderTextField.getText());
                massOfLeftModule = Double.parseDouble(massOfLeftModuleTextField.getText());
                massOfLeftGlider = Double.parseDouble(massOfLeftGliderTextField.getText());

                massOfLeftModuleAndLeftGlider = massOfLeftGlider + massOfLeftModule;
                massOfRightModuleAndRightGlider = massOfRightGlider + massOfRightModule;

                testType = "Conservation of Momentum (Elastic Collision)";
                break;

            case 2:
                totalDropDistance = Double.parseDouble(totalDropDistanceTextField.getText());
                massOfModuleAndHolder = Double.parseDouble(massOfModuleAndHolderTextField.getText());
                momentOfInertiaCOE = Double.parseDouble(momentOfInertiaCOETextField.getText());
                radiusOfTorqueArmCOE = Double.parseDouble(radiusOfTorqueArmCOETextField.getText());
                
                testType = "Conservation of Energy";
                break;

            case 3:
                angleFromTop = Double.parseDouble(topAngle.getText());   
                testType = "Inclined Plane - Released From Top";
                break;

            case 4:
                angleFromBottom = Double.parseDouble(bottomAngle.getText());
                testType = "Inclined Plane - Released From Bottom";
                break;
                
            case 5:
                lengthOfPendulum = Double.parseDouble(lengthOfPendulumTextField.getText());
                distanceFromPivot = Double.parseDouble(distanceFromPivotTextField.getText());
                massOfModule = Double.parseDouble(massOfModuleTextField.getText());
                massOfHolder = Double.parseDouble(massOfHolderTextField.getText());
                
                testType = "Physical Pendulum";
                break;

            case 6:
                springConstant = Double.parseDouble(springConstantTextField.getText());
                totalHangingMass = Double.parseDouble(totalHangingMassTextField.getText());
                amplitudeSpring = Double.parseDouble(amplitudeSpringTextField.getText());
                massOfSpring = Double.parseDouble(massOfSpringTextField.getText());

                testType = "Spring Test - Simple Harmonics";
                break;

            case 7:
                testType = "Generic Template - One Module";
                break;

            case 8:
                testType = "Generic Template - Two Modules";
                break;

            default:
                displayProgress("Invalid test parameter chosen", Color.RED, STYLE_FAIL, 1);
                break;
        }
    }

    /**
     * ActionEvent that configures the module using functionality from the SerialComm Class for pairing with an RF remote
     * control
     *
     * ***********IMPORTANT******This method and following methods use Tasks as UI Elements and backend elements must be modified concurrently. UI elements are bound to properties in the task. UpdateMessage and
     * UpdateProgess therefore update bound UI elements.
     *
     * @param event
     */
    @FXML
    private void pairNewRemote(ActionEvent event) {

        Task<Void> pairNewRemoteTask = new Task<Void>() {
            protected Void call() throws Exception {
                int maxProgress = 100;
                updateMessage("Module Listening for New Remote, Hold 'A' or 'B' Button to Pair");
                updateProgress(0, maxProgress);

                Platform.runLater(() -> {
                    unpairRemotesTabLabel.setTextFill(Color.BLACK);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

                try {
                    if (serialHandler.pairNewRemote()) {                                                                // Attempts to pair remote and a boolean is returned that indicates if it was successful.
                        updateMessage("New Remote Successfully Paired");
                        updateProgress(100, maxProgress);

                        Platform.runLater(() -> {                                                                       //Without the binding of elements to properties. Platform.runLater() allows UI elements to be modified in the task.
                            unpairRemotesTabLabel.setTextFill(LIGHT_GREEN);
                            progressBar.setStyle("-fx-accent: #1f78d1;");
                        });

                    } else {
                        updateMessage("Pair Unsuccessful, Receiver Timed Out");
                        updateProgress(100, maxProgress);

                        Platform.runLater(() -> {
                            unpairRemotesTabLabel.setTextFill(Color.RED);
                            progressBar.setStyle("-fx-accent: red;");
                        });
                    }

                } catch (IOException e) {
                    updateMessage("Error Communicating With Serial Dongle");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (PortInUseException e) {
                    updateMessage("Serial Port Already In Use");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (UnsupportedCommOperationException e) {
                    updateMessage("Check Dongle Compatability");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                }
                return null;
            }
        };

        /**
         * Here the UI elements are bound to properties so they can be modified in the Task
         *
         * After the task is completed, the UI elements are unbound.
         */

        pairNewRemoteButton.disableProperty().bind(pairNewRemoteTask.runningProperty());
        unpairAllRemotesButton.disableProperty().bind(pairNewRemoteTask.runningProperty());
        testRemotesButton.disableProperty().bind(pairNewRemoteTask.runningProperty());
        nextButton.disableProperty().bind(pairNewRemoteTask.runningProperty());
        backButton.disableProperty().bind(pairNewRemoteTask.runningProperty());
        unpairRemotesTabLabel.textProperty().bind(pairNewRemoteTask.messageProperty());
        progressBar.progressProperty().bind(pairNewRemoteTask.progressProperty());

        pairNewRemoteTask.setOnSucceeded(e -> {
            pairNewRemoteButton.disableProperty().unbind();
            unpairAllRemotesButton.disableProperty().unbind();
            testRemotesButton.disableProperty().unbind();
            nextButton.disableProperty().unbind();
            backButton.disableProperty().unbind();
            unpairRemotesTabLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();
        });

        new Thread(pairNewRemoteTask).start();
    }

    /**
     * ActionEvent that unpairs all known RF remote controls from the module using functionality from the SerialComm
     * class
     *
     * @param event
     */
    @FXML
    private void unpairRemotes(ActionEvent event) {

        Task<Void> unpairRemotesTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                int maxProgress = 100;

                updateMessage("Unpairing All Remotes...");
                updateProgress(0, maxProgress);

                Platform.runLater(() -> {
                    unpairRemotesTabLabel.setTextFill(Color.BLACK);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

                try {
                    serialHandler.unpairAllRemotes();                                                                   // Attempts to unpair all remotes
                } catch (IOException e) {

                    updateMessage("Error Communicating With Serial Dongle");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (PortInUseException e) {

                    updateMessage("Serial Port Already In Use");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (UnsupportedCommOperationException e) {

                    updateMessage("Check Dongle Compatability");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                }

                updateMessage("All Remotes Unpaired, There are 0 Remotes Paired to this Module");
                updateProgress(0, maxProgress);

                Platform.runLater(() -> {
                    unpairRemotesTabLabel.setTextFill(Color.BLACK);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

//                generalStatusExperimentLabel.setTextFill(Color.BLACK);
//                generalStatusExperimentLabel.setText("All Remotes Unpaired, There are 0 Remotes Paired to this Module");
//                progressBar.setProgress(0);

                return null;
            }
        };

        pairNewRemoteButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        unpairAllRemotesButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        testRemotesButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        nextButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        backButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        unpairRemotesTabLabel.textProperty().bind(unpairRemotesTask.messageProperty());
        progressBar.progressProperty().bind(unpairRemotesTask.progressProperty());

        unpairRemotesTask.setOnSucceeded(e -> {
            pairNewRemoteButton.disableProperty().unbind();
            unpairAllRemotesButton.disableProperty().unbind();
            testRemotesButton.disableProperty().unbind();
            nextButton.disableProperty().unbind();
            backButton.disableProperty().unbind();
            unpairRemotesTabLabel.textProperty().unbind();
            unpairRemotesTabLabel.textFillProperty().unbind();
            progressBar.progressProperty().unbind();
        });

        new Thread(unpairRemotesTask).start();

    }

    @FXML
    private void unpairRemotesMainMenu(ActionEvent event) {

        Task<Void> unpairRemotesTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                int maxProgress = 100;

                updateMessage("Unpairing All Remotes...");
                updateProgress(0, maxProgress);

                Platform.runLater(() -> {
                    unpairRemotesTabLabel.setTextFill(Color.BLACK);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

                try {
                    serialHandler.unpairAllRemotes();                                                                   // Attempts to unpair all remotes
                } catch (IOException e) {

                    updateMessage("Error Communicating With Serial Dongle");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (PortInUseException e) {

                    updateMessage("Serial Port Already In Use");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (UnsupportedCommOperationException e) {

                    updateMessage("Check Dongle Compatability");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                }

                updateMessage("All Remotes Unpaired, There are 0 Remotes Paired to this Module");
                updateProgress(0, maxProgress);

                Platform.runLater(() -> {
                    unpairRemotesTabLabel.setTextFill(Color.BLACK);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

//                generalStatusExperimentLabel.setTextFill(Color.BLACK);
//                generalStatusExperimentLabel.setText("All Remotes Unpaired, There are 0 Remotes Paired to this Module");
//                progressBar.setProgress(0);

                return null;
            }
        };

        pairNewRemoteButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        unpairAllRemotesButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        testRemotesButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        nextButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        backButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        unpairRemotesTabLabel.textProperty().bind(unpairRemotesTask.messageProperty());
        progressBar.progressProperty().bind(unpairRemotesTask.progressProperty());

        unpairRemotesTask.setOnSucceeded(e -> {
            pairNewRemoteButton.disableProperty().unbind();
            unpairAllRemotesButton.disableProperty().unbind();
            testRemotesButton.disableProperty().unbind();
            nextButton.disableProperty().unbind();
            backButton.disableProperty().unbind();
            unpairRemotesTabLabel.textProperty().unbind();
            unpairRemotesTabLabel.textFillProperty().unbind();
            progressBar.progressProperty().unbind();
        });

        new Thread(unpairRemotesTask).start();

    }


    /**
     * ActionEvent that puts the module into a testing mode using functionality from the SerialComm Class, wherein the
     * module records no data, but only detects when and which button the user is pressing on their remote
     *
     * @param event
     */
    @FXML
    private void testPairedRemote(ActionEvent event) {

        Task<Void> testPairedRemoteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int maxProgress = 100;

                Platform.runLater(() -> {
                    exitTestModeButton.setDisable(false);   //Enables the exitTestModeButton when the user starts the testPairedRemoteTask
                });

                updateMessage("Press a Button on a Remote to Test if it is Paired");
                updateProgress(0, maxProgress);

                Platform.runLater(() -> {
                    unpairRemotesTabLabel.setTextFill(Color.BLACK);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

                try {
                    unpairRemotesTabLabel.textProperty().unbind(); //Unbinds the Label from the testPairedRemoteTask so that the new task created in testRemotesFX can take control over it

                    if (!serialHandler.testRemotesFX(unpairRemotesTabLabel)) {

                        unpairRemotesTabLabel.textProperty().bind(messageProperty()); //Rebinds the Label to the testPairedRemotesTask

                        updateMessage("Error Communicating with Module");
                        updateProgress(100, maxProgress);

                        Platform.runLater(() -> {
                            unpairRemotesTabLabel.setTextFill(Color.RED);
                            progressBar.setStyle("-fx-accent: red;");
                        });

                    }
                } catch (IOException e) {

                    updateMessage("Error Communicating With Serial Dongle");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (PortInUseException e) {

                    updateMessage("Serial Port Already In Use");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (UnsupportedCommOperationException e) {

                    updateMessage("Check Dongle Compatability");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        unpairRemotesTabLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });
                }

                updateMessage("Test Mode Successfully Exited");
                updateProgress(100, maxProgress);

                Platform.runLater(() -> {
                    unpairRemotesTabLabel.setTextFill(LIGHT_GREEN);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

                return null;
            }
        };

        pairNewRemoteButton.disableProperty().bind(testPairedRemoteTask.runningProperty());
        unpairAllRemotesButton.disableProperty().bind(testPairedRemoteTask.runningProperty());
        testRemotesButton.disableProperty().bind(testPairedRemoteTask.runningProperty());
        //.disableProperty().bind(testPairedRemoteTask.runningProperty());
        //backButton.disableProperty().bind(testPairedRemoteTask.runningProperty());
        unpairRemotesTabLabel.textProperty().bind(testPairedRemoteTask.messageProperty());
        //progressBar.progressProperty().bind(testPairedRemoteTask.progressProperty());


        testPairedRemoteTask.setOnSucceeded(e -> {
            pairNewRemoteButton.disableProperty().unbind();
            unpairAllRemotesButton.disableProperty().unbind();
            testRemotesButton.disableProperty().unbind();
            //nextButton.disableProperty().unbind();
            //backButton.disableProperty().unbind();
            unpairRemotesTabLabel.textProperty().unbind();
            //progressBar.progressProperty().unbind();

            Platform.runLater(() -> {
                exitTestModeButton.setDisable(true);    //Disables the exitTestModeButton after the user has completed the testPairedRemoteTask
                progressBar.setProgress(0);
            });
        });

        new Thread(testPairedRemoteTask).start();

    }

    /**
     * ActionEvent that sets flag that will cause the testRemoteThread to exit the test remote mode, executed when
     * exit remote test mode button is pressed. Since this is an action event, it must complete before GUI changes
     * will be visible
     *
     * @param event
     */
    @FXML
    private void exitRemoteTestingMode(ActionEvent event) {
        try{
        serialHandler.exitRemoteTest();
        unpairRemotesTabLabel.setTextFill(Color.GREEN);
        unpairRemotesTabLabel.setText("Remote Testing Successfully Exited");
        }catch(Exception e){
            unpairRemotesTabLabel.setTextFill(Color.RED);
            unpairRemotesTabLabel.setText("Unable to Exit Remote Testing");
        }
    }

    /**
     * This method gets the selected toggle and its assigned userData from the outputTypeToggleGroup ToggleGroup and returns it as
     * a string.
     * @return String that details what output type has been selected from the outputTypeToggleGroup ToggleGroup
     */
    private RadioButton getOutputType() {
        return (RadioButton) outputType.getSelectedToggle();
    }

    /**
     * This method reads all of the data captured by the module during a testing period; then, depending on the output
     * type selected (from a defined ToggleGroup of output options), the data is then handled accordingly.
     * @param event
     */
    @FXML
    private void readTestsFromModule(ActionEvent event) {

        //connectToModule(eraseModuleTabLabel);

        Task<Void> readTask = new Task<Void>() {

            @Override
            protected Void call() {

                try {

                    ArrayList<Integer> testParameters = serialHandler.readTestParams(NUM_TEST_PARAMETERS);

                    // clear all previously read tests for one-module tests
                    if (oneModuleTest) {
                        genericTests.clear();
                        dataOrgoList.clear();
                    }

                    displayProgress("Reading tests from module...", Color.BLUE, STYLE_WORKING, 0);

                    // ensure test parameters have been read correctly
                    if (testParameters == null) {
                        displayProgress("Error reading test parameters from module", Color.RED, STYLE_FAIL, 100);
                        return null;
                    }

                    int expectedTestNum = testParameters.get(0);
                    int accelGyroSampleRate = testParameters.get(7);
                    int magSampleRate = testParameters.get(8);
                    int accelSensitivity = testParameters.get(9);
                    int gyroSensitivity = testParameters.get(10);
                    int accelFilter = testParameters.get(11);
                    int gyroFilter = testParameters.get(12);

                    String nameOfFile = "";

                    if (expectedTestNum == 0) {
                        displayProgress("No tests found on module", Color.RED, STYLE_FAIL, 1);
                        return null;
                    }

                    // Get date for file name
                    Date date = new Date();

                    // Assigns the name of file
                    nameOfFile += (" " + accelGyroSampleRate + "-" + magSampleRate + " " + accelSensitivity + "G-" + accelFilter + " " + gyroSensitivity + "dps-" + gyroFilter + " MAG-N " + date.getDate() + getMonth(date.getMonth()) + (date.getYear() - 100) + ".csv");

                    // Store the test data from the dashboard passing in enough info that the progress bar will be accurately updated
                    HashMap<Integer, ArrayList<Integer>> testData = serialHandler.readTestDataFX(expectedTestNum, progressBar, generalStatusExperimentLabel);

                    if (testData == null) {
                        displayProgress("Error reading tests from module", Color.RED, STYLE_FAIL, 1);
                        return null;
                    }

                    CSVHandler writer = new CSVHandler();

                    Settings settings = new Settings();
                    settings.loadConfigFile();

                    // loop through all tests read from the module
                    for (int i = 0; i < testData.size(); i++) {

                        int[] finalData = new int[testData.get(i).size()];

                        // loop through all samples in the data set
                        for (int j = 0; j < testData.get(i).size(); j++) {

                            // ensure that all negative byte data is -1
                            if (testData.get(i).get(j) != -1) {
                                finalData[j] = testData.get(i).get(j);
                            } else {
                                finalData[j] = -1;
                                break;
                            }
                        }
                        
                        int[][] MPUMinMax = serialHandler.getMPUMinMax();

                        String newName = "(#" + (i+1) + ") " + nameOfFile;

                        if (getOutputType().equals(sincTechnologyRadioButton)) {

                            // [OLD] stores test data for the SINC Graph
                            dataOrgo = new DataOrganizer(testParameters, "SINC_" + newName);
                            dataOrgo.setMPUMinMax(serialHandler.getMPUMinMax());
                            dataOrgoList.add(dataOrgo);

                            //Organize data into .CSV, finalData is passed to method. Method returns a list of lists of doubles.
                            dataOrgo.createDataSmpsRawData(finalData);
                            dataOrgo.getSignedData();

                            dataOrgo.createCSVP();
                            dataOrgo.createCSV(false, false);

                            dataOrgo.readAndSetTestParameters(System.getProperty("user.home") + "/Documents/" + newName+"p");

                        } else {

                            GenericTest newTest;

                            switch (experimentType) {

                                case 1: // TODO Conservation of Momentum
                                    newTest = new ConservationMomentumTest(testParameters, finalData, MPUMinMax, massOfRightModule, massOfLeftModule, massOfRightGlider, massOfLeftGlider);
                                    break;
                                case 2: // TODO Conservation of Energy
                                    newTest = new ConservationEnergyTest(testParameters, finalData, MPUMinMax, massOfModuleAndHolder, momentOfInertiaCOE, radiusOfTorqueArmCOE, totalDropDistance);
                                    break;
                                case 3: // Inclined Plane - Top
                                    newTest = new InclinedPlaneTopTest(testParameters, finalData, MPUMinMax, angleFromTop);
                                    break;
                                case 4: // Inclined Plane - Bottom
                                    newTest = new InclinedPlaneBottomTest(testParameters, finalData, MPUMinMax, angleFromBottom);
                                    break;
                                case 5: // Physical Pendulum
                                    newTest = new PhysicalPendulumTest(testParameters, finalData, MPUMinMax, lengthOfPendulum, distanceFromPivot, massOfModule, massOfHolder);
                                    break;
                                case 6: // Spring Test
                                    newTest = new SpringTest(testParameters, finalData, MPUMinMax, springConstant, totalHangingMass, amplitudeSpring, massOfSpring);
                                    break;
                                case 7: // Generic Template - One Module
                                    newTest = new GenericTest(testParameters, finalData, MPUMinMax);
                                    break;
                                case 8: // TODO Generic Template - Two Module
                                    newTest = new GenericTest(testParameters, finalData, MPUMinMax);
                                    break;
                                default:
                                    newTest = new GenericTest(testParameters, finalData, MPUMinMax);
                                    break;
                            }

                            System.out.println("Creating " + newTest.getClass().getName());
                            genericTests.add(newTest);

                            // write GenericTest to CSV
                            writer.writeCSV(newTest, settings, newName);
                            writer.writeCSVP(testParameters, settings, newName, MPUMinMax);

                        }

                        // update test data progress
                        displayProgress("Read test " + (i+1) + "/" + testData.size(), Color.GREEN, STYLE_WORKING, ((double) (i+1)) / ((double) testData.size()));
                    }

                    displayProgress("All tests read from module", Color.GREEN, STYLE_SUCCESS, 1);

                    // automatically launch the appropriate graph for one-module tests
                    if (oneModuleTest) {
                        
                        if (getOutputType().equals(sincTechnologyRadioButton)) {

                            System.out.println("Launching SINC Graph...");

                            Platform.runLater(() -> {
                                lineGraph = startGraphing();

                                // SINC Graph only supports 2 data sets, so hard-coding is okay
                                lineGraph.setDataCollector(dataOrgoList.get(0), 0);
                                lineGraph.setDataCollector(dataOrgoList.get(1), 1);

                            });

                        }
                        else {

                            System.out.println("Launching Data Analysis Graph...");

                            Platform.runLater(() -> {
                                GraphNoSINCController graph = startGraphingNoSINC();
                                graph.setGenericTests(genericTests);
                            });

                        }
                        
                        // move to the "Launch Graph" page
                        Platform.runLater(() -> nextTab());

                    }

                } catch (IOException e) {
                    displayProgress("Error reading tests -- USB connection lost", Color.RED, STYLE_FAIL, 1);
                } catch (PortInUseException e) {
                    displayProgress("Error reading tests -- USB port already in use", Color.RED, STYLE_FAIL, 1);
                } catch (UnsupportedCommOperationException e) {
                    displayProgress("Error reading tests -- check USB dongle compatibility", Color.RED, STYLE_FAIL, 1);
                }

                return null;

            }

        };

        // start read process asynchronously
        new Thread(readTask).start();

    }

    /**
     * Updates the status label and progress bar. Can be used for all experiment steps.
     * <p>This method does <b>NOT</b> need to be run inside of <code>Platform.runLater()</code>.</p>
     * @param message the message to display in the status label
     * @param color the color of the status label
     * @param style the style of the progress bar (usually <code>-fx-accent: [color]</code>)
     * @param progress the status of the progress bar between 0 and 1
     */
    private void displayProgress(String message, Color color, String style, double progress) {
        displayProgress(generalStatusExperimentLabel, message, color, style, progress);
    }

    /**
     * Overloaded variant of {@link #displayProgress} allowing a custom label.
     * @param label the status label to update
     * @param message the message to display in the status label
     * @param color the color of the status label
     * @param style the style of the progress bar (usually <code>-fx-accent: [color]</code>)
     * @param progress the status of the progress bar between 0 and 1
     */
    private void displayProgress(Label label, String message, Color color, String style, double progress) {

        if (message.length() > 0) System.out.println(message + " (" + progress * 100 + "%)");

        Platform.runLater(() -> {
            label.setText(message);
            label.setTextFill(color);
            progressBar.setStyle(style);
            progressBar.setProgress(progress);
        });

    }

    private String momentumTemplatePath;

    @FXML
    private void eraseTestsExperimentTab() {
        eraseTestsFromModule(generalStatusExperimentLabel);
    }

    @FXML
    private void eraseTestsEraseTab() {
        eraseTestsFromModule(eraseModuleTabLabel);
    }

    /**
     * Bulk erases all tests from a module.
     * Requires the module to be connected prior to running.
     * Returns the thread created for chaining.
     * @param label the status label to modify
     */
    private void eraseTestsFromModule(Label label) {

        Task<Void> eraseTestsTask = new Task<Void>() {

            @Override
            protected Void call() {

                displayProgress(label, "Bulk erasing...", Color.BLUE, STYLE_WORKING, 0);

                try {
                    if (serialHandler.bulkEraseModule()) {
                        displayProgress(label, "Bulk erase successful", Color.GREEN, STYLE_SUCCESS, 1);
                    } else {
                        displayProgress(label, "Bulk erase failed", Color.RED, STYLE_FAIL, 1);
                    }
                } catch (IOException e) {
                    displayProgress(label, "Error erasing module -- USB connection lost", Color.RED, STYLE_FAIL, 1);
                } catch (PortInUseException e) {
                    displayProgress(label, "Error erasing module -- port in use by another application", Color.RED, STYLE_FAIL, 1);
                }
                catch (UnsupportedCommOperationException e) {
                    System.out.println("Error erasing module -- unsupported communication operation");
                    displayProgress(label, "Error erasing module -- check USB dongle compatibility", Color.RED, STYLE_FAIL, 1);
                }
                catch (Exception e) {
                    displayProgress(label, "Error erasing module, try again", Color.RED, STYLE_FAIL, 1);
                }

                return null;
            }
        };

        new Thread(eraseTestsTask).start(); //Starts an anonymous thread, passing it the Task defined above

    }

    /*End Experiment Tab Methods*/

    /*Begin Motion Visualization Tab Methods*/

    private GraphController lineGraph;

    @FXML
    private void launchSINCGraph(ActionEvent event) {
        lineGraph = startGraphing();
    }

    @FXML
    private void launchDAG(ActionEvent event) {

        Settings settings = new Settings();
        settings.loadConfigFile();
        
        GraphNoSINCController g = startGraphingNoSINC(); 
        
        File directory = new File(settings.getKeyVal("CSVSaveLocation"));

        // fetches all CSV files from given folder
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv");
            }           
        });

        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;

        // if no CSV files could be found, don't continue
        if (files == null || files.length == 0) {
            System.out.println("No CSV files found");
            return;
        }

        // Finds the most recently modified file
        for (File f : files) {

            String filePath = f.toString();
            File csvp = new File(filePath.substring(0, filePath.length()-4) + ".csvp");

            // if CSVP file with same name 
            if (csvp.exists() && f.lastModified() > lastModifiedTime) {
                chosenFile = f;
                lastModifiedTime = f.lastModified();
            }
        }

        // if no CSV/CSVP file pair could be found, don't continue
        if (chosenFile == null) {
            System.out.println("No CSV/CSVP file pair found");
            return;
        }

        String pathToFile = chosenFile.toString();
        
        long start = System.nanoTime(); 
        g.setGenericTestFromCSV(pathToFile);
        long elapsedTime = System.nanoTime() - start;
        
        System.out.println("Loaded CSV in " + elapsedTime/1e9d + " seconds");
        
    }

    @FXML
    private void launchChosenGraph(ActionEvent event) {

        // launch SINC Graph
        if (getOutputType().equals(sincTechnologyRadioButton)) {

            Settings settings = new Settings();
            settings.loadConfigFile();
            String pathTofile = System.getProperty("user.home") + "/Documents/" + dataOrgo.getName();
            lineGraph = startGraphing();
            lineGraph.setCsvFilePath(pathTofile);
            lineGraph.loadCSVData();

        }
        // open Data Analysis Graph
        else {

            GraphNoSINCController graph = startGraphingNoSINC();
            graph.setGenericTests(genericTests);

        }

    }

    
    /**
     * <p><b>NOTE: THIS METHOD CAN ONLY BE CALLED THROUGH FXML.</b></p>
     * Launches the selected graph application by checking the button's fx:id.
     * @param event the event created by clicking the button
     */
    @FXML
    private void launchGraphTwoModule(ActionEvent event) {

        Button b = (Button) event.getSource();
        
        if (b.getId().equals("launchSINCTwoModule")) {

            Settings settings = new Settings();
            settings.loadConfigFile();
            lineGraph = startGraphing();

            // SINC Graph only supports 2 data sets, so hard-coding is okay
            lineGraph.setDataCollector(dataOrgoList.get(0), 0);
            lineGraph.setDataCollector(dataOrgoList.get(1), 1);

        }
        else if (b.getId().equals("launchDAGTwoModule")) {

            GraphNoSINCController graph = startGraphingNoSINC();
            graph.setGenericTests(genericTests);

        }

    }

    public MediaPlayerController startMediaPlayer() {
        Stage primaryStage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/MediaPlayerStructure.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        primaryStage.setTitle("Video Player");
        if(root!=null) primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.show();
        primaryStage.setResizable(false);
        return loader.getController();
    }

    /**
     * Loads and launches the Data Analysis Graph.
     */
    public GraphNoSINCController startGraphingNoSINC() {

    	Stage primaryStage = new Stage();
    	FXMLLoader loader = new FXMLLoader((getClass().getResource("fxml/GraphNoSINC.fxml")));
        Parent root;
        
		try {
			
			root = loader.load();
			primaryStage.setTitle("BioForce Data Analysis Graph");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("css/GraphNoSINC.css").toExternalForm());
	        primaryStage.setMinWidth(600);
	        primaryStage.setMinHeight(400);
	        primaryStage.setScene(scene);
            primaryStage.setResizable(true);
	        primaryStage.show();
	        
		} catch (IOException e) {
			System.out.println("Error loading Data Analysis Graph.");
			e.printStackTrace();
		}
        
        return loader.getController();
    }
    
    public GraphController startGraphing() {
        Stage primaryStage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/UpdatedGraphStructureEducator.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(root!=null) primaryStage.setScene(new Scene(root, 1320, 730));

        primaryStage.setTitle("BioForce SINC Technology Graph");
        primaryStage.show();
        primaryStage.setResizable(false);

        return loader.getController();
    }

    /*End Motion Visualization Tab Methods*/

    /*Begin SINC Module Calibration Tab Methods*/

    private int timerCalibrationOffset;

    private int delayAfterStart;

    @FXML
    private Button configureModuleForCalibrationButton;
    @FXML
    private Button importCalibrationDataButton;
    @FXML
    private Button applyOffsetToModuleButton;

    @FXML
    public void configureModuleForCalibrationHandler(){

        Task<Void> configureModuleForCalibrationTask = new Task<Void>() {
            @Override
            protected Void call(){

                try {

                    if(!serialHandler.applyCalibrationOffsets(0, 0)){                         //the TMR0 and delay after set must be reset to their defaults otherwise the ones from the last calibration will be used in this calibration.
                       sincCalibrationTabGeneralStatusLabel.setText("Error Communicating with Module");
                       sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                    }

                    if(!serialHandler.configForCalibration()){                                                          // Calibrates Module, returns a boolean indicating whether or not it was successful.

                        Platform.runLater(() -> {                                                                       // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                            sincCalibrationTabGeneralStatusLabel.setText("Error Communicating with Module");
                            sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                        });

                    //test2
                    }else{

                        Platform.runLater(() -> {                                                                       // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                            sincCalibrationTabGeneralStatusLabel.setText("Module Successfully Configured for Calibration");
                            sincCalibrationTabGeneralStatusLabel.setTextFill(Color.GREEN);
                        });
                    }
                }catch(IOException e){

                    Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                        sincCalibrationTabGeneralStatusLabel.setText("Error Communicating With Serial Dongle");
                        sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                    });
                }catch(PortInUseException e){

                    Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                        sincCalibrationTabGeneralStatusLabel.setText("Serial Port Already In Use");
                        sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                    });

                }catch(UnsupportedCommOperationException e){

                    Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                        sincCalibrationTabGeneralStatusLabel.setText("Check Dongle Compatability");
                        sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                    });

                }

                return null;
            }
        };
        new Thread(configureModuleForCalibrationTask).start();
    }




    @FXML
    public void videoFileHandler(ActionEvent event)
    {
        FileChooser chooser;
        chooser = new FileChooser();
        chooser.setInitialDirectory(new java.io.File("."));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Select a video file", "*.mp4","*.avi","*.flac","*.mov"));
        File fileChosen = chooser.showOpenDialog(null);
        if (fileChosen != null && videoFilePathTextField.getText() != null) {

            String fileout = fileChosen.toString();
            Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                videoFilePathTextField.setAlignment(Pos.CENTER_LEFT);
                videoFilePathTextField.setText(fileout);
                sincCalibrationTabGeneralStatusLabel.setTextFill(Color.GREEN);
                sincCalibrationTabGeneralStatusLabel.setText("File Copy Finished!");
            });

        } else {
            Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                sincCalibrationTabGeneralStatusLabel.setText("Invalid File Path");
                sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
            });
        }
    }


    @FXML
    private TextField videoFilePathTextField;

    public static int trueDelayAfterStart;

    @FXML
    public void applySINCHandler(){

        Task<Void> SINCTask = new Task<Void>() {
            @Override
            protected Void call(){

                try{

                    BlackFrameAnalysis bfo = new BlackFrameAnalysis(videoFilePathTextField.getText());                  // creates a new blackframe analysis object, video file path is passed and subsequently analysis to obtain offset and delay.
                    timerCalibrationOffset = bfo.getTMR0Offset();                                                       // sets offset to local variable
                    trueDelayAfterStart = bfo.getDelayAfterStart();                                                         // sets delay to local variable

                    if (trueDelayAfterStart >= 0){
                        delayAfterStart = trueDelayAfterStart;
                    }else{
                        delayAfterStart = 0;
                    }

                    System.out.println(timerCalibrationOffset);
                    System.out.println(delayAfterStart);

                }catch(IOException e){
                    Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                        sincCalibrationTabGeneralStatusLabel.setText("Error Reading File");
                        sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                    });
                }

                try {

                    if (!serialHandler.applyCalibrationOffsets(timerCalibrationOffset, delayAfterStart)) {                // apply obtains offset and delay to module and returns a boolean indicating if it was successful.
                        Platform.runLater(() -> {                                                                       // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                            sincCalibrationTabGeneralStatusLabel.setText("Error Communicating With Module");
                            sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                        });


                    } else {
                        Platform.runLater(() -> {
                            sincCalibrationTabGeneralStatusLabel.setText("Offset Successfully Applied, Camera and Module are now Synced");
                            sincCalibrationTabGeneralStatusLabel.setTextFill(Color.GREEN);
                        });

                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        sincCalibrationTabGeneralStatusLabel.setText("Error Communicating With Serial Dongle");
                        sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                    });

                } catch (PortInUseException e) {
                    Platform.runLater(() -> {                                                                               // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                        sincCalibrationTabGeneralStatusLabel.setText("Serial Port Already In Use");
                        sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                    });

                } catch (UnsupportedCommOperationException e) {
                    Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                        sincCalibrationTabGeneralStatusLabel.setText("Check Dongle Compatibility");
                        sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                    });

                }




                return null;
            }
        };
        new Thread(SINCTask).start();



//        importCalibrationDataHandler();
//        applyOffsetHandler();
    }


    @FXML
    public void importCalibrationDataHandler(){

        Task<Void> importCalibrationDataTask = new Task<Void>() {
            @Override
            protected Void call(){

                try{

                    BlackFrameAnalysis bfo = new BlackFrameAnalysis(videoFilePathTextField.getText());                  // creates a new blackframe analysis object, video file path is passed and subsequently analysis to obtain offset and delay.
                    timerCalibrationOffset = bfo.getTMR0Offset();                                                       // sets offset to local variable
                    delayAfterStart = bfo.getDelayAfterStart();                                                         // sets delay to local variable

                    System.out.println(timerCalibrationOffset);
                    System.out.println(delayAfterStart);

                    if(sincCalibrationTabGeneralStatusLabel.getText() != "File Copy Finished!")
                    {
//                        Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
//                            sincCalibrationTabGeneralStatusLabel.setText("Error Reading File");
//                            sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
//                        });
                    }else{
                        Platform.runLater(() -> {                                                                       // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                            sincCalibrationTabGeneralStatusLabel.setText("TMR0 and Delay After Start Calculated, you may now apply them");
                            sincCalibrationTabGeneralStatusLabel.setTextFill(Color.GREEN);
                        });
                    }

                }catch(IOException e){
                    Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                        sincCalibrationTabGeneralStatusLabel.setText("Error Reading File");
                        sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                    });
                }

                return null;
            }
        };
        new Thread(importCalibrationDataTask).start();
    }

    @FXML
    public void applyOffsetHandler(){
        Task<Void> applyOffsetTask = new Task<Void>() {
            @Override
            protected Void call() {
                //if (delayAfterStart >= 0) {
                    try {

                        if (!serialHandler.applyCalibrationOffsets(timerCalibrationOffset, delayAfterStart)) {                // apply obtains offset and delay to module and returns a boolean indicating if it was successful.
                            Platform.runLater(() -> {                                                                       // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                                sincCalibrationTabGeneralStatusLabel.setText("Error Communicating With Module");
                                sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                            });


                        } else {
                            Platform.runLater(() -> {
                                sincCalibrationTabGeneralStatusLabel.setText("Offset Successfully Applied, Camera and Module are now Synced");
                                sincCalibrationTabGeneralStatusLabel.setTextFill(Color.GREEN);
                            });

                        }
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            sincCalibrationTabGeneralStatusLabel.setText("Error Communicating With Serial Dongle");
                            sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                        });

                    } catch (PortInUseException e) {
                        Platform.runLater(() -> {                                                                               // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                            sincCalibrationTabGeneralStatusLabel.setText("Serial Port Already In Use");
                            sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                        });

                    } catch (UnsupportedCommOperationException e) {
                        Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                            sincCalibrationTabGeneralStatusLabel.setText("Check Dongle Compatibility");
                            sincCalibrationTabGeneralStatusLabel.setTextFill(Color.RED);
                        });

                    }
               // }

//                if (delayAfterStart < 0) {  // In Module Firmware 6, applying a negative delay after start will break the dashboard. Therefore, if a negative value is calculated, 0 will be applied. However, this prevents the storage of the true delay after start value in the csvp, preventing adjusting for the offset in the graph. In firmware 7, a negative delay after start will not break the module. It will still delay 0, but it will also write that negtaive value to the csvp. Allowing for correction in this graph. This section of code should be removed at that point.
//                    try {
//                        if (!serialHandler.applyCalibrationOffsets(timerCalibrationOffset, 0)) {                // apply obtains offset and delay to module and returns a boolean indicating if it was successful.
//                            Platform.runLater(() -> {                                                                       // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
//                                sincCalibrationTabGeneralStatusLabel.setText("Error Communicating With Module");
//                            });
//
//
//                        } else {
//                            Platform.runLater(() -> {
//                                sincCalibrationTabGeneralStatusLabel.setText("Offset Successfully Applied, Camera and Module are now Synced");
//                            });
//
//                        }
//                    } catch (IOException e) {
//                        Platform.runLater(() -> {
//                            sincCalibrationTabGeneralStatusLabel.setText("Error Communicating With Serial Dongle");
//                        });
//
//                    } catch (PortInUseException e) {
//                        Platform.runLater(() -> {                                                                               // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
//                            sincCalibrationTabGeneralStatusLabel.setText("Serial Port Already In Use");
//                        });
//
//                    } catch (UnsupportedCommOperationException e) {
//                        Platform.runLater(() -> {                                                                           // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
//                            sincCalibrationTabGeneralStatusLabel.setText("Check Dongle Compatibility");
//                        });
//
//                    }
//                }
//
//                Platform.runLater(() -> {                                                                               // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
//                    progressBar.setStyle("-fx-accent: #1f78d1;");                                                       //Updates the progress bar's color style with a CSS call, setting its color back to its origin
//                    generalStatusExperimentLabel.setTextFill(Color.BLACK);                                              //Updates the generalStatusExperimentLabel's text fill (coloring) back to black
//                });

                return null;
            }
        };

        new Thread(applyOffsetTask).start();
    }

    /*End SINC Module Calibration Tab Methods*/




    /* Module Parameter Settings */

    /**
     * Get the desired tick threshold for the desired sample rate. This effectively sets the sample rate of the module
     *
     * @param accelGyroSampleRate
     * @return
     */
    public int getTickThreshold(int accelGyroSampleRate) {
        switch (accelGyroSampleRate) {
            case (60):
                return 33173;
            case (120):
                return 33021;
            case (240):
                return 16343;
            case (480):
                return 8021;
            case (500):
                return 7679;
            case (960):
                return 3848;
            default:    //960-96
                return 3813;
        }
    }

    /***
     *  Fills the testTypeHashMap with the module settings associated with each test type
     *  After one test type is filled the testTypeHashMap is cleared and then next test type is inputted
     */
    public void fillTestTypeHashMap() {
        ArrayList<Integer> testParamsA = new ArrayList<Integer>();
        ArrayList<Integer> testParamsB = new ArrayList<Integer>();
        ArrayList<Integer> testParamsC = new ArrayList<Integer>();
        ArrayList<Integer> testParamsD = new ArrayList<Integer>();
        ArrayList<Integer> testParamsE = new ArrayList<Integer>();
        ArrayList<Integer> testParamsF = new ArrayList<Integer>();
        ArrayList<Integer> testParamsG = new ArrayList<Integer>();

        //0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
        testParamsA.add(0);
        //1 Timer0 Tick Threshold
        testParamsA.add(getTickThreshold(960));
        //2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
        testParamsA.add(0);
        //3 Battery timeout flag
        testParamsA.add(300);
        //4 Time Test Flag
        testParamsA.add(0);
        //5 Trigger on release flag
        testParamsA.add(1);
        //6 Test Length
        testParamsA.add(30);
        //7 Accel Gyro Sample Rate
        testParamsA.add(960);
        //8 Mag Sample Rate
        testParamsA.add(96);
        //9 Accel Sensitivity
        testParamsA.add(4);
        //10 Gyro Sensitivity
        testParamsA.add(1000);
        //11 Accel Filter
        testParamsA.add(92);
        //12 Gyro Filter
        testParamsA.add(92);

        testTypeHashMap.put("Conservation of Momentum (Elastic Collision)", testParamsA);

        /*
         * ***IMPORTANT*** The following commented out code is no longer in use but is being kept in case that we do decide to bring this lab back ***IMPORTANT***
         */

//        //0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
//        testParams.add(0);
//        //1 Timer0 Tick Threshold
//        testParams.add(getTickThreshold(960));
//        //2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
//        testParams.add(0);
//        //3 Battery timeout flag
//        testParams.add(300);
//        //5 Trigger on release flags
//        testParams.add(1);
//        //6 Test Length
//        testParams.add(30);
//        //7 Accel Gyro Sample Rate
//        testParams.add(960);
//        //8 Mag Sample Rate
//        testParams.add(96);
//        //9 Accel Sensitivity
//        testParams.add(4);
//        //10 Gyro Sensitivity
//        testParams.add(2000);
//        //11 Accel Filter
//        testParams.add(92);
//        //12 Gyro Filter
//        testParams.add(92);
//
//        testTypeHashMap.put("Conservation of Angular Momentum", testParams);

//        testParams.clear();

        //0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
        testParamsB.add(0);
        //1 Timer0 Tick Threshold
        testParamsB.add(getTickThreshold(960));
        //2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
        testParamsB.add(0);
        //3 Battery timeout flag
        testParamsB.add(300);
        //4 Time Test Flag
        testParamsB.add(0);
        //5 Trigger on release flag
        testParamsB.add(1);
        //6 Test Length
        testParamsB.add(30);
        //7 Accel Gyro Sample Rate
        testParamsB.add(960);
        //8 Mag Sample Rate
        testParamsB.add(96);
        //9 Accel Sensitivity
        testParamsB.add(16);
        //10 Gyro Sensitivity
        testParamsB.add(2000);
        //11 Accel Filter
        testParamsB.add(92);
        //12 Gyro Filter
        testParamsB.add(92);

        testTypeHashMap.put("Conservation of Energy", testParamsB);

        //0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
        testParamsC.add(0);
        //1 Timer0 Tick Threshold
        testParamsC.add(getTickThreshold(960));
        //2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
        testParamsC.add(0);
        //3 Battery timeout flag
        testParamsC.add(300);
        //4 Time Test Flag
        testParamsC.add(0);
        //5 Trigger on release flag
        testParamsC.add(1);
        //6 Test Length
        testParamsC.add(30);
        //7 Accel Gyro Sample Rate
        testParamsC.add(960);
        //8 Mag Sample Rate
        testParamsC.add(96);
        //9 Accel Sensitivity
        testParamsC.add(4);
        //10 Gyro Sensitivity
        testParamsC.add(1000);
        //11 Accel Filter
        testParamsC.add(92);
        //12 Gyro Filter
        testParamsC.add(92);

        testTypeHashMap.put("Inclined Plane", testParamsC);
        testTypeHashMap.put("Inclined Plane - Released From Top", testParamsC);
        testTypeHashMap.put("Inclined Plane - Projected From Bottom", testParamsC);

        //0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
        testParamsD.add(0);
        //1 Timer0 Tick Threshold
        testParamsD.add(getTickThreshold(960));
        //2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
        testParamsD.add(0);
        //3 Battery timeout flag
        testParamsD.add(300);
        //4 Time Test Flag
        testParamsD.add(0);
        //5 Trigger on release flag
        testParamsD.add(1);
        //6 Test Length
        testParamsD.add(30);
        //7 Accel Gyro Sample Rate
        testParamsD.add(960);
        //8 Mag Sample Rate
        testParamsD.add(96);
        //9 Accel Sensitivity
        testParamsD.add(8);
        //10 Gyro Sensitivity
        testParamsD.add(2000);
        //11 Accel Filter
        testParamsD.add(92);
        //12 Gyro Filter
        testParamsD.add(92);

        testTypeHashMap.put("Physical Pendulum", testParamsD);

        //0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
        testParamsE.add(0);
        //1 Timer0 Tick Threshold
        testParamsE.add(getTickThreshold(960));
        //2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
        testParamsE.add(0);
        //3 Battery timeout flag
        testParamsE.add(300);
        //4 Time Test Flag
        testParamsE.add(0);
        //5 Trigger on release flag
        testParamsE.add(1);
        //6 Test Length
        testParamsE.add(30);
        //7 Accel Gyro Sample Rate
        testParamsE.add(960);
        //8 Mag Sample Rate
        testParamsE.add(96);
        //9 Accel Sensitivity
        testParamsE.add(4);
        //10 Gyro Sensitivity
        testParamsE.add(1000);
        //11 Accel Filter
        testParamsE.add(92);
        //12 Gyro Filter
        testParamsE.add(92);

        testTypeHashMap.put("Spring Test - Simple Harmonics", testParamsE);

        //0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
        testParamsF.add(0);
        //1 Timer0 Tick Threshold
        testParamsF.add(getTickThreshold(960));
        //2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
        testParamsF.add(0);
        //3 Battery timeout flag
        testParamsF.add(300);
        //4 Time Test Flag
        testParamsF.add(0);
        //5 Trigger on release flag
        testParamsF.add(1);
        //6 Test Length
        testParamsF.add(30);
        //7 Accel Gyro Sample Rate
        testParamsF.add(960);
        //8 Mag Sample Rate
        testParamsF.add(96);
        //9 Accel Sensitivity
        testParamsF.add(4);
        //10 Gyro Sensitivity
        testParamsF.add(1000);
        //11 Accel Filter
        testParamsF.add(92);
        //12 Gyro Filter
        testParamsF.add(92);

        testTypeHashMap.put("Generic Template - One Module", testParamsF);

        //0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
        testParamsG.add(0);
        //1 Timer0 Tick Threshold
        testParamsG.add(getTickThreshold(960));
        //2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
        testParamsG.add(0);
        //3 Battery timeout flag
        testParamsG.add(300);
        //4 Time Test Flag
        testParamsG.add(0);
        //5 Trigger on release flag
        testParamsG.add(1);
        //6 Test Length
        testParamsG.add(30);
        //7 Accel Gyro Sample Rate
        testParamsG.add(960);
        //8 Mag Sample Rate
        testParamsG.add(96);
        //9 Accel Sensitivity
        testParamsG.add(4);
        //10 Gyro Sensitivity
        testParamsG.add(1000);
        //11 Accel Filter
        testParamsG.add(92);
        //12 Gyro Filter
        testParamsG.add(92);

        testTypeHashMap.put("Generic Template - Two Modules", testParamsG);

    }

    /**
     * Attempts to connect to a module.
     * Returns the thread created for chaining.
     */
    public Thread connectToModule(Label label) {

        Task<Void> connectTask = new Task<Void>() {
            
            @Override
            protected Void call() {

                displayProgress(label, "Connecting to module...", Color.BLUE, STYLE_WORKING, 0);

                // disable test parameters box
                Platform.runLater(() -> {
                    testTypeComboBox.setDisable(false);
                });

                // get all ports
                ArrayList<String> ports = serialHandler.findPorts();
                System.out.println("Searching available ports: " + ports);

                // loop through all ports
                for (int i = 0; i < ports.size(); i++) {

                    try {

                        // get the name of the current COM port
                        String selectedCommID = ports.get(i);

                        // attempt connection to serial port
                        serialHandler.closeSerialPort();
                        serialHandler.openSerialPort(selectedCommID);

                        // attempt to read module info (used to check firmware ID)
                        ArrayList<Integer> moduleIDInfo = serialHandler.getModuleInfo(NUM_ID_INFO_PARAMETERS);

                        // check if port has a module connected; if not, move onto next port
                        if (moduleIDInfo == null) {
                            System.out.println("Module ID Info null when connecting to module, trying next port...");
                            continue;
                        }

                        int firmwareID = moduleIDInfo.get(2);
                        System.out.println("Current Firmware ID: " + firmwareID);

                        // make sure that Dashboard's firmware version matches the module's
                        if (firmwareID == CURRENT_FIRMWARE_ID) {

                            displayProgress(label, "Successfully connected to module", Color.GREEN, STYLE_SUCCESS, 0.5);

                            // re-enable the test parameters selection box
                            Platform.runLater(() -> testTypeComboBox.setDisable(false));

                            return null;

                        }
                        else {
                            String firmwareMsg = "Incompatible firmware version, update module to " + CURRENT_FIRMWARE_ID + " (currently " + firmwareID + ")";
                            displayProgress(label, firmwareMsg, Color.RED, STYLE_FAIL, 1);
                        }

                    }
                    catch (PortInUseException e) {
                        displayProgress(label, "Error connecting to module -- port in use by another application", Color.RED, STYLE_FAIL, 1);
                    }
                    catch (UnsupportedCommOperationException e) {
                        System.out.println("Error connecting to module -- unsupported communication operation");
                        displayProgress(label, "Error connecting to module -- check USB dongle compatibility", Color.RED, STYLE_FAIL, 1);
                    }
                    catch (Exception e) {
                        displayProgress(label, "Error connecting to module, try again", Color.RED, STYLE_FAIL, 1);
                    }

                }

                System.out.println("No modules found connected to ports");
                displayProgress(label, "Make sure module is connected to the computer", Color.RED, STYLE_FAIL, 1);
                
                return null;
            }
        };

        // run the connection task 
        Thread t = new Thread(connectTask);
        t.start();

        return t;
    }

    /*
     * User selects an output path for the spreadsheet template
     */
    public String chooseSpreadsheetOutputPath(Label label) {
        generalStatusExperimentLabel.setTextFill(Color.BLACK);
        generalStatusExperimentLabel.setText("Copying File Template...");

        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File("."));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Name Output File (*.xlsx)", "*.xlsx"));
        File file = chooser.showSaveDialog(null);

        if (file != null) {

            String fileout = file.toString();

            generalStatusExperimentLabel.setTextFill(LIGHT_GREEN);
            generalStatusExperimentLabel.setText("File Copy finished!");

            if (!fileout.endsWith(".xlsx")) {
                return fileout + ".xlsx";
            } else {
                return fileout;
            }

        } else {
            generalStatusExperimentLabel.setTextFill(Color.RED);
            generalStatusExperimentLabel.setText("Invalid File Path Entered");
            return null;
        }
    }

    /**
     * Gets a 3 letter abbreviation for the passed in month for the automatic test title generation
     *
     * @param month an integer 0-11 that corresponds to the month with 0 = January and 11 = December
     * @return The 3 letter abbreviation for the month
     */
    public String getMonth(int month) {
        switch (month) {
            case (0):
                return "JAN";
            case (1):
                return "FEB";
            case (2):
                return "MAR";
            case (3):
                return "APR";
            case (4):
                return "MAY";
            case (5):
                return "JUN";
            case (6):
                return "JUL";
            case (7):
                return "AUG";
            case (8):
                return "SEP";
            case (9):
                return "OCT";
            case (10):
                return "NOV";
            case (11):
                return "DEC";
        }
        return "NOP";
    }

}