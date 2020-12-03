package com.bioforceanalytics.dashboard;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;

public class EducatorModeControllerFX implements Initializable {

    // CONSTANTS AND PARAMETERS
    private final int NUM_TEST_PARAMETERS = 13;
    private final int NUM_ID_INFO_PARAMETERS = 3;
    private final int CURRENT_FIRMWARE_ID = 26;

    private SerialComm serialHandler;

    // TABS
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

    // NEXT/BACK BUTTONS
    @FXML
    Button nextButton;
    @FXML
    Button backButton;

    @FXML
    ComboBox<String> testTypeComboBox;

    // EXPERIMENT TAB FXML COMPONENTS
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
    ProgressBar progressBar;

    @FXML
    ToggleGroup outputType;

    @FXML
    Label sincCalibrationTabGeneralStatusLabel;

    @FXML
    Label eraseModuleTabLabel;

    @FXML
    Label unpairRemotesTabLabel;


    // TEST PARAMETER FXML COMPONENTS
    @FXML
    TextField massOfCart1TextField;
    @FXML
    TextField massOfCart2TextField;
    @FXML
    TextField totalDropDistanceTextField;
    @FXML
    TextField massOfModuleAndHolderCOETextField;
    @FXML
    TextField momentOfInertiaCOETextField;
    @FXML
    TextField angleFromTopTextField;
    @FXML
    TextField lengthOfPendulumTextField;
    @FXML
    TextField distanceFromPivotTextField;
    @FXML
    TextField massOfModuleAndHolderTextField;
    @FXML
    TextField massOfPendulumTextField;
    @FXML
    TextField springConstantTextField;
    @FXML
    TextField totalHangingMassTextField;
    @FXML
    TextField amplitudeSpringTextField;
    @FXML
    TextField massOfSpringTextField;
    @FXML
    Label applyConfigurationsToFirstModuleLabel;

    //Extra Module Parameters - CoM
    double massOfCart1;
    double massOfCart2;
    // Extra Module Parameters - CoE
    double totalDropDistance;
    double massOfModuleAndHolderCOE;
    double momentOfInertiaCOE;
    // Extra Module Parameters - Pendulum
    double lengthOfPendulum;
    double distanceFromPivot;
    double massOfModuleAndHolder;
    double massOfPendulum;
    // Extra Module Parameters - Spring
    double springConstant;
    double totalHangingMass;
    double amplitudeSpring;
    double massOfSpring;

    // Extra Module Parameters - Inclined Plane
    double angleFromTop;

    private ConservationMomentumTest comTest;
    private ConservationEnergyTest engTest;
    
    private int experimentType;
    
    // holds test data from modules for DAG and SINC respectively
    // each object represents a trial on a module
    private ArrayList<GenericTest> genericTests;

    // Dashboard Background Functionality
    private int experimentTabIndex = 0;
    private int selectedIndex = 0;
    private HashMap<String, Integer[]> testTypeHashMap = new HashMap<>();
    public static String testType;

    // indicates the number of pages in the Experiment tab
    private final int NUM_OF_STEPS = 3;

    // actually indicates the last page for two-module tests;
    // since indices 0-3 are one-module, 4-9 are two-module
    private final int NUM_OF_STEPS_TWO_MODULE = 9;

    private Boolean oneModuleTest;

    // BFA icon used for the Dashboard and Graph
    private Image icon;

    private static final Logger logger = LogController.start();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        icon = new Image(getClass().getResource("images/bfa.png").toExternalForm());

        genericTests = new ArrayList<GenericTest>();

        testTypeComboBox.getItems().addAll("Conservation of Momentum (Elastic Collision)", "Conservation of Energy", "Inclined Plane", "Physical Pendulum", "Spring Test","Generic Template - One Module","Generic Template - Two Modules"); //Create combobox of test names so users can select Test type that he / she wants to perform.
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

    @FXML
    public EducationModeHelpMenuController helpmenu;

    @FXML
    private void selectHelpTab(ActionEvent event) {

        helpmenu = startHelpMenu();

        Tab t = primaryTabPane.getSelectionModel().getSelectedItem();

        if (t.equals(eraseConfirmationTab)) {
            helpmenu.selectEraseModuleHelpTabOne();
        } else if (t.equals(unpairRemotesTab)) {
            helpmenu.selectUnpairRemotesHelpTab();
        } else if (t.equals(sincCalibrationTab)) {
            helpmenu.selectSINCModuleCalibrationTab();
        }else if (t.equals(eraseConfirmationTab)) {
            helpmenu.selectUnpairRemotesHelpTab();
        } else if (t.equals(experimentTab)) {
            helpmenu.selectExperimentHelpTab(experimentTabIndex);
        }

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
        primaryStage.getIcons().add(icon);
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
    }

    @FXML
    private void selectSINCCalibration(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(sincCalibrationTab);
    }

    @FXML
    private void selectEraseConfirmationTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(eraseConfirmationTab);
    }

    @FXML
    private void selectUnpairRemotesTab(ActionEvent event) {
        primaryTabPane.getSelectionModel().select(unpairRemotesTab);
        remotePairingTabPane.getSelectionModel().select(0);
    }

    @FXML
    private void selectPairSingleModuleTab(ActionEvent event) {
        remotePairingTabPane.getSelectionModel().select(1);
        exitTestModeButton.setDisable(true);
    }

    @FXML
    private void selectPairTwoModulesTab(ActionEvent event) {
        remotePairingTabPane.getSelectionModel().select(2);
    }

    @FXML
    private void selectPairTwoModulesTabPageTwo(ActionEvent event) {
        remotePairingTabPane.getSelectionModel().select(3);
    }

    @FXML
    private void selectPairTwoModulesTabPageThree(ActionEvent event) {
        remotePairingTabPane.getSelectionModel().select(4);
    }


    @FXML
    private void goBackToIntialTabPairingRemotes(ActionEvent event) {
        remotePairingTabPane.getSelectionModel().select(0);
    }

    /**
     * Moves the experiment tab to the next page.
     */
    @FXML
    private void nextTab() {

        // reset progress bar and status label
        displayProgress("", 0, Status.NEUTRAL);

        // if on the last step, reset back to the first step
        if (experimentTabIndex == (oneModuleTest ? NUM_OF_STEPS : NUM_OF_STEPS_TWO_MODULE)) {

            experimentTabIndex = 0;

            // disable "next" and hide "back"
            nextButton.setDisable(true);
            backButton.setVisible(false);

        }
        // if this is a two-module test and the user clicks next on step 1, skip to the two-module steps at index 5
        else if (!oneModuleTest && experimentTabIndex == 0) {
            experimentTabIndex = NUM_OF_STEPS + 1;
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
        displayProgress("", 0, Status.NEUTRAL);

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
        selectedIndex = testTypeComboBox.getSelectionModel().getSelectedIndex(); //Gets the index of the test type selected by user within the combobox
        testParametersTabPane.getSelectionModel().select(selectedIndex + 1); //Since the number of tabs matches the length of the combobox selection model, the user's
        //selected index is used to select the matching tab pane index to display

        if (selectedIndex == 0 || selectedIndex == 1 || selectedIndex == 6) { //this means a test involving two modules is selected.
            applyConfigurationsToFirstModuleLabel.setText("Apply your configurations to Module 1");
            oneModuleTest = false;
        } else {
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
        saveTestParams();
    }

    /**
     * Writes test parameters to the module.
     */
    private void saveTestParams() {

        // reset status label and progress bar
        displayProgress("Saving test parameters...", 0.5, Status.WORKING);

        // checks if a test is selected before continuing; shouldn't normally run but just a safeguard
        if (testParametersTabPane.getSelectionModel().getSelectedIndex() == 0) { 
            displayProgress("Please choose a test type", 1, Status.FAIL);
            return;
        }

        // disable "Next" and "Apply Configurations" buttons
        applyConfigurationsButton.setDisable(true);
        nextButton.setDisable(true);

        String testName = testTypeComboBox.getSelectionModel().getSelectedItem();
        Integer[] testParams = testTypeHashMap.get(testTypeComboBox.getSelectionModel().getSelectedItem());

        // print out test name + parameters for debugging
        logger.info("Test name: " + testName);
        logger.info("Test parameters: " + Arrays.toString(testParams));

        Task<Void> sendTestParamsTask = new Task<Void>() {

            @Override
            protected Void call() {
                
                try {
                    
                    // save experiment test params to Dashboard
                    updateTestParams(selectedIndex);

                    // send test params to module
                    if (serialHandler.sendTestParams(testParams)) {
                        displayProgress("Test parameters saved successfully", 1, Status.SUCCESS);
                    } else {
                        displayProgress("Error saving test parameters to Module, try again", 1, Status.ERROR);
                    }

                } catch (NumberFormatException e) {
                    displayProgress("Check that parameters are filled out correctly", 1, Status.FAIL);
                } catch (IOException e) {
                    displayProgress("Connection to Module lost, try again", 1, Status.ERROR);
                } catch (PortInUseException e) {
                    displayProgress("Port in use by another application", 1, Status.ERROR);
                } catch (UnsupportedCommOperationException e) {
                    displayProgress("Check USB dongle compatibility", 1, Status.ERROR);
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
            case 0:
                massOfCart1 = Double.parseDouble(massOfCart1TextField.getText());
                massOfCart2 = Double.parseDouble(massOfCart2TextField.getText());
                
                comTest = new ConservationMomentumTest(massOfCart1, massOfCart2);
                testType = "Conservation of Momentum (Elastic Collision)";
                break;

            case 1:
                totalDropDistance = Double.parseDouble(totalDropDistanceTextField.getText());
                massOfModuleAndHolderCOE = Double.parseDouble(massOfModuleAndHolderCOETextField.getText());
                momentOfInertiaCOE = Double.parseDouble(momentOfInertiaCOETextField.getText());
                engTest = new ConservationEnergyTest(massOfModuleAndHolderCOE, momentOfInertiaCOE, totalDropDistance);
                testType = "Conservation of Energy";
                break;

            case 2:
                angleFromTop = Double.parseDouble(angleFromTopTextField.getText());   
                testType = "Inclined Plane";
                break;

            case 3:
                lengthOfPendulum = Double.parseDouble(lengthOfPendulumTextField.getText());
                distanceFromPivot = Double.parseDouble(distanceFromPivotTextField.getText());
                massOfModuleAndHolder = Double.parseDouble(massOfModuleAndHolderTextField.getText());
                massOfPendulum = Double.parseDouble(massOfPendulumTextField.getText());
                
                testType = "Physical Pendulum";
                break;

            case 4:
                springConstant = Double.parseDouble(springConstantTextField.getText());
                totalHangingMass = Double.parseDouble(totalHangingMassTextField.getText());
                amplitudeSpring = Double.parseDouble(amplitudeSpringTextField.getText());
                massOfSpring = Double.parseDouble(massOfSpringTextField.getText());

                testType = "Spring Test";
                break;

            case 5:
                testType = "Generic Template - One Module";
                break;

            case 6:
                testType = "Generic Template - Two Modules";
                break;

            default:
                displayProgress("Invalid test parameter chosen", 1, Status.FAIL);
                break;
        }
    }

    /**
     * Pairs a new remote to a module.
     */
    @FXML
    private void pairNewRemote(ActionEvent event) {

        Task<Void> pairNewRemoteTask = new Task<Void>() {

            protected Void call() throws Exception {

                displayProgress(unpairRemotesTabLabel, "Module listening for new Remote, hold any button to pair...", 0, Status.WORKING);

                try {

                    if (serialHandler.pairNewRemote()) {      
                        displayProgress(unpairRemotesTabLabel, "New Remote successfully paired", 1, Status.SUCCESS);
                    } else {
                        displayProgress(unpairRemotesTabLabel, "Pair unsuccessful, receiver timed out", 1, Status.FAIL);
                    }

                } catch (IOException e) {
                    displayProgress(unpairRemotesTabLabel, "Error pairing Remote -- USB connection lost", 1, Status.ERROR);
                } catch (PortInUseException e) {
                    displayProgress(unpairRemotesTabLabel, "Error pairing Remote -- USB port already in use", 1, Status.ERROR);
                } catch (UnsupportedCommOperationException e) {
                    displayProgress(unpairRemotesTabLabel, "Error pairing Remote -- check USB dongle compatibility", 1, Status.ERROR);
                }

                return null;
            }
        };

        new Thread(pairNewRemoteTask).start();
    }

    /**
     * Unpairs all remotes from the connected module.
     */
    @FXML
    private void unpairRemotes(ActionEvent event) {

        Task<Void> unpairRemotesTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                displayProgress(unpairRemotesTabLabel, "Unpairing all Remotes...", 0, Status.WORKING);

                try {
                    serialHandler.unpairAllRemotes();
                } catch (IOException e) {
                    displayProgress(unpairRemotesTabLabel, "Error unpairing Remotes -- USB connection lost", 1, Status.ERROR);
                } catch (PortInUseException e) {
                    displayProgress(unpairRemotesTabLabel, "Error unpairing Remotes -- USB port already in use", 1, Status.ERROR);
                } catch (UnsupportedCommOperationException e) {
                    displayProgress(unpairRemotesTabLabel, "Error unpairing Remotes -- check USB dongle compatibility", 1, Status.ERROR);
                }

                displayProgress(unpairRemotesTabLabel, "All Remotes unpaired from Module", 1, Status.SUCCESS);

                return null;
            }
        };

        new Thread(unpairRemotesTask).start();

    }

    /**
     * Places the module into "test mode", allowing the user to verify that their remote is paired.
     */
    @FXML
    private void testPairedRemote(ActionEvent event) {

        Task<Void> testPairedRemoteTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                Platform.runLater(() -> {
                    exitTestModeButton.setDisable(false);
                });

                displayProgress(unpairRemotesTabLabel, "Press any button to test your Remote...", 0, Status.WORKING);

                try {

                    if (!serialHandler.testRemotesFX(unpairRemotesTabLabel)) {
                        displayProgress(unpairRemotesTabLabel, "Error communicating with Module, try again", 1, Status.FAIL);
                    }

                } catch (IOException e) {
                    displayProgress(unpairRemotesTabLabel, "Error pairing Remote -- USB connection lost", 1, Status.ERROR);
                } catch (PortInUseException e) {
                    displayProgress(unpairRemotesTabLabel, "Error pairing Remote -- USB port already in use", 1, Status.ERROR);
                } catch (UnsupportedCommOperationException e) {
                    displayProgress(unpairRemotesTabLabel, "Error pairing Remote -- check USB dongle compatibility", 1, Status.ERROR);
                }

                displayProgress(unpairRemotesTabLabel, "Exited test mode successfully", 1, Status.SUCCESS);

                return null;
            }
        };

        new Thread(testPairedRemoteTask).start();

    }

    /**
     * Exits "test mode" for the given module via SerialComm.
     * This will cause testPairedRemote()'s Task to continue and gracefully exit.
     */
    @FXML
    private void exitRemoteTestingMode(ActionEvent event) {
        serialHandler.exitRemoteTest();
    }

    /**
     * This method reads all of the data captured by the module during a testing period; then, depending on the output
     * type selected (from a defined ToggleGroup of output options), the data is then handled accordingly.
     * @param event
     */
    @FXML
    private void readTestsFromModule(ActionEvent event) {

        // TODO look into auto-connecting before reading tests
        //connectToModule(eraseModuleTabLabel);

        Task<Void> readTask = new Task<Void>() {

            @Override
            protected Void call() {

                try {

                    ArrayList<Integer> testParameters = serialHandler.readTestParams(NUM_TEST_PARAMETERS);

                    // clear all previously read tests for one-module tests
                    if (oneModuleTest) {
                        genericTests.clear();
                    }

                    displayProgress("Reading tests from Module...", 0, Status.WORKING);

                    // ensure test parameters have been read correctly
                    if (testParameters == null) {
                        displayProgress("Error reading test parameters from Module", 1, Status.ERROR);
                        return null;
                    }

                    logger.info("Read test parameters from Module.");

                    int expectedTestNum = testParameters.get(0);

                    // check if there are tests on the module
                    if (expectedTestNum == 0) {
                        displayProgress("No tests found on Module", 1, Status.FAIL);
                        return null;
                    }

                    // Store the test data from the dashboard passing in enough info that the progress bar will be accurately updated
                    HashMap<Integer, ArrayList<Integer>> testData = serialHandler.readTestDataFX(expectedTestNum, progressBar, generalStatusExperimentLabel);

                    if (testData == null) {
                        displayProgress("Error reading tests from Module", 1, Status.ERROR);
                        return null;
                    }

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

                        // create timestamp for CSV/CSVP
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd - HH.mm");
                        String timestamp = sdf.format(new Timestamp(new Date().getTime()));

                        GenericTest test;
                        int moduleNumber = -1;
                        
                        switch (experimentType) {

                            case 0: // Conservation of Momentum
                                double totalMass = massOfCart1 + massOfCart2;
                                test = new ConservationMomentumModule(testParameters, finalData, MPUMinMax, totalMass, comTest);
                                moduleNumber = comTest.addModule(test);
                                break;
                            case 1: // Conservation of Energy
                                test = new ConservationEnergyModule(testParameters, finalData, MPUMinMax, engTest);
                                moduleNumber = engTest.addModule(test);
                                break;
                            case 2: // Inclined Plane
                                test = new InclinedPlaneTest(testParameters, finalData, MPUMinMax, angleFromTop);
                                break;
                            case 3: // Physical Pendulum
                                test = new PhysicalPendulumTest(testParameters, finalData, MPUMinMax, lengthOfPendulum, distanceFromPivot, massOfModuleAndHolder, massOfPendulum);
                                break;
                            case 4: // Spring Test
                                test = new SpringTest(testParameters, finalData, MPUMinMax, springConstant, totalHangingMass, amplitudeSpring, massOfSpring);
                                break;
                            case 5: // Generic Template - One Module
                                test = new GenericTest(testParameters, finalData, MPUMinMax);
                                break;
                            case 6: // Generic Template - Two Module
                                test = new GenericTest(testParameters, finalData, MPUMinMax);
                                break;
                            default:
                                test = new GenericTest(testParameters, finalData, MPUMinMax);
                                break;
                        }

                        // for one module tests, use the order in which tests were read;
                        // if this is a two module test, use the module's number (1/2)
                        int testNum = oneModuleTest ? (i+1) : moduleNumber;

                        String testType = test.getClass().getSimpleName();
                        String testName = testType + " #" + testNum + " " + timestamp;

                        test.setName(testName);

                        logger.info("Created " + testName);

                        // conservation of momentum
                        if (experimentType == 1) {

                            // wait until both tests are read to add to list of GTs
                            if (comTest.isFilled()) {

                                genericTests.add(comTest.getModuleOne());
                                genericTests.add(comTest.getModuleTwo());

                            }

                        // conservation of energy
                        } else if (experimentType == 2) {

                            // wait until both tests are read to add to list of GTs
                            if (engTest.isFilled()) {
                                genericTests.add(engTest.getModuleOne());
                                genericTests.add(engTest.getModuleTwo());
                            }

                        // all other tests
                        } else genericTests.add(test);

                        // write GenericTest to CSV
                        try {
                            CSVHandler.writeCSV(test, testName + ".csv");
                            CSVHandler.writeCSVP(testParameters, testName + ".csvp", MPUMinMax);
                        }
                        catch (Exception e) {
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.setHeaderText("Error saving test data");
                            alert.setContentText("There was a problem saving \"" + testName + "\".");
                            alert.showAndWait();
                        }

                        // update test data progress
                        displayProgress("Read test " + (i+1) + "/" + testData.size(), ((double) (i+1)) / ((double) testData.size()), Status.WORKING);
                    }

                    displayProgress("All tests read from Module", 1, Status.SUCCESS);

                    // automatically launch the appropriate graph for one-module tests
                    if (oneModuleTest) {

                        logger.info("Launching BioForce Graph...");

                        Platform.runLater(() -> {
                            GraphNoSINCController graph = startGraphingNoSINC();
                            graph.setGenericTests(genericTests);
                        });
                        
                        // move to the "Launch Graph" page
                        Platform.runLater(() -> nextTab());

                    }

                } catch (IOException e) {
                    displayProgress("Error reading tests -- USB connection lost", 1, Status.ERROR);
                } catch (PortInUseException e) {
                    displayProgress("Error reading tests -- USB port already in use", 1, Status.ERROR);
                } catch (UnsupportedCommOperationException e) {
                    displayProgress("Error reading tests -- check USB dongle compatibility", 1, Status.ERROR);
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
     * @param progress the status of the progress bar between 0 and 1
     * @param status the {@link Status} of the current task being displayed
     */
    private void displayProgress(String message, double progress, Status status) {
        displayProgress(generalStatusExperimentLabel, message, progress, status);
    }

    /**
     * Overloaded variant of {@link #displayProgress} allowing a custom label.
     * <p>This method does <b>NOT</b> need to be run inside of <code>Platform.runLater()</code>.</p>
     * @param label the status label to update
     * @param message the message to display in the status label
     * @param progress the status of the progress bar between 0 and 1
     * @param status the {@link Status} of the current task being displayed
     */
    private void displayProgress(Label label, String message, double progress, Status status) {

        // print debug messages
        if (message.length() > 0) {

            String finalMsg = message + " (" + progress * 100 + "%)";

            if (status == Status.SUCCESS || status == Status.NEUTRAL || status == Status.WORKING) {
                logger.info(finalMsg);
            } else if (status == Status.FAIL) {
                logger.warn(finalMsg);
            } else if (status == Status.ERROR) {
                logger.error(finalMsg);
            }
            
        }

        // set progress bar and labels
        Platform.runLater(() -> {
            label.setText(message);
            label.setTextFill(getStatusColor(status));
            progressBar.setStyle(getStatusStyle(status));
            progressBar.setProgress(progress);
        });

    }

    /**
     * Gets the color associated with a {@link Status}.
     * @param status the {@link Status} to get the color for
     * @return the color associated with the {@link Status}
     */
    private Color getStatusColor(Status status) {

        if (status == Status.SUCCESS) {
            return Color.GREEN;
        } else if (status == Status.WORKING) {
            return Color.BLUE;
        } else if (status == Status.FAIL || status == Status.ERROR) {
            return Color.RED;
        }
        else if (status == Status.NEUTRAL) {
            return Color.BLACK;
        }
        else {
            return null;
        }

    }

    /**
     * Gets the style associated with a {@link Status}.
     * @param status the {@link Status} to get the style for
     * @return the style associated with the {@link Status}
     */
    private String getStatusStyle(Status status) {

        if (status == Status.SUCCESS) {
            return "-fx-accent: green";
        } else if (status == Status.WORKING) {
            return "-fx-accent: #1f78d1";
        } else if (status == Status.FAIL || status == Status.ERROR) {
            return "-fx-accent: red";
        } else if (status == Status.NEUTRAL) {
            return "-fx-accent: black";
        }
        else {
            return null;
        }

    }

    /**
     * Enum used to indicate what the status of a progress update is.
     * <p><code>SUCCESS</code> indicates a successful task,</p>
     * <p><code>WORKING</code> indicates a task in progress,</p>
     * <p><code>NEUTRAL</code> indicates a task providing an update with no connotation,</p>
     * <p><code>FAIL</code> indicates a task that could not complete for a known user error,</p>
     * <p>and <code>ERROR</code> indicates a task that could not complete for an unknown reason.</p>
     */
    private enum Status {
        SUCCESS,
        WORKING,
        FAIL,
        ERROR,
        NEUTRAL
    }

    /**
     * Button handler for when "Reset Module" is clicked.
     */
    @FXML
    private void bulkErase() {
        eraseTestsFromModule(eraseModuleTabLabel, EraseMode.BULK);
    }

    /**
     * Button handler for when "Yes, Erase the Module" is clicked.
     */
    @FXML
    private void sectorEraseTab() {
        eraseTestsFromModule(eraseModuleTabLabel, EraseMode.SECTOR);
    }

    /**
     * Button handler for erasing the module from the experiment tab.
     */
    @FXML
    private void eraseTestsExperimentTab() {
        eraseTestsFromModule(generalStatusExperimentLabel, EraseMode.SECTOR);
    }

    /**
     * Enum used to differentiate between bulk and sector erases.
     * Used as an argument in {@link #eraseTestsFromModule}.
     */
    private enum EraseMode {
        BULK,
        SECTOR
    }

    /**
     * Bulk erases all tests from a module.
     * Requires the module to be connected prior to running.
     * Returns the thread created for chaining.
     * @param label the status label to modify
     */
    private void eraseTestsFromModule(Label label, EraseMode e) {

        Task<Void> eraseTestsTask = new Task<Void>() {

            @Override
            protected Void call() {

                String message = e == EraseMode.SECTOR ? "Erasing data..." : "Resetting Module...";
                String method = e == EraseMode.SECTOR ? "Erase" : "Reset";
                String error = e == EraseMode.SECTOR ? "erasing" : "resetting";

                // display erase message
                displayProgress(label, message, 0, Status.WORKING);

                try {

                    if (e == EraseMode.SECTOR ? serialHandler.sectorEraseModule() : serialHandler.bulkEraseModule()) {
                        displayProgress(label, method + " successful", 1, Status.SUCCESS);
                    } else {
                        displayProgress(label, method + " failed, try again", 1, Status.ERROR);
                    }

                } catch (IOException e) {
                    displayProgress(label, "Error " + error + " Module -- USB connection lost", 1, Status.ERROR);
                } catch (PortInUseException e) {
                    displayProgress(label, "Error " + error + " Module -- port in use by another application", 1, Status.ERROR);
                }
                catch (UnsupportedCommOperationException e) {
                    displayProgress(label, "Error " + error + " Module -- check USB dongle compatibility", 1, Status.ERROR);
                }
                catch (Exception e) {
                    displayProgress(label, "Error " + error + " Module, try again", 1, Status.ERROR);
                }

                return null;
            }
        };

        // Erasing tests asynchronously
        new Thread(eraseTestsTask).start();

    }

    @FXML
    private void launchDAG(ActionEvent event) {
        
        GraphNoSINCController g = startGraphingNoSINC(); 
        
        File directory = new File(Settings.get("CSVSaveLocation"));

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
            logger.info("No CSV files found");
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
            logger.info("No CSV/CSVP file pair found");
            return;
        }

        String pathToFile = chosenFile.toString();
        
        long start = System.nanoTime(); 
        g.setGenericTestFromCSV(pathToFile);
        long elapsedTime = System.nanoTime() - start;
        
        logger.info("Loaded CSV in " + elapsedTime/1e9d + " seconds");
        
    }

    /**
     * Loads and launches the BioForce Graph.
     */
    public GraphNoSINCController startGraphingNoSINC() {

    	Stage primaryStage = new Stage();
    	FXMLLoader loader = new FXMLLoader((getClass().getResource("fxml/GraphNoSINC.fxml")));
        Parent root;
        
		try {
			
			root = loader.load();
			primaryStage.setTitle("BioForce Graph");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("css/GraphNoSINC.css").toExternalForm());
	        primaryStage.setMinWidth(900);
	        primaryStage.setMinHeight(600);
	        primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.getIcons().add(icon);
	        primaryStage.show();
	        
		} catch (IOException e) {
			logger.error("Error loading BioForce Graph.");
			e.printStackTrace();
		}
        
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
    public void configureModuleForCalibrationHandler() {

        Task<Void> calibrationTask = new Task<Void>() {

            @Override
            protected Void call() throws IOException, PortInUseException, UnsupportedCommOperationException {

                displayProgress(sincCalibrationTabGeneralStatusLabel, "Configuring Module for SINC Calibration...", 0, Status.WORKING);

                // reset timer0 and "delay after start" test parameters
                if(!serialHandler.applyCalibrationOffsets(0, 0)) {
                    displayProgress(sincCalibrationTabGeneralStatusLabel, "Error resetting calibration offsets, try again", 1, Status.ERROR);
                    return null;
                }

                // configure module in SINC calibration module
                if (serialHandler.configForCalibration()) {
                    displayProgress(sincCalibrationTabGeneralStatusLabel, "Successfully configured Module for calibration", 1, Status.SUCCESS);
                } else {
                    displayProgress(sincCalibrationTabGeneralStatusLabel, "Error configuring Module for calibration, try again", 1, Status.ERROR);
                }

                return null;
            }
        };

        calibrationTask.setOnFailed(event -> {

            Throwable ex = calibrationTask.getException();

            logger.error("Configuration failed: " + ex);

            if (ex instanceof PortInUseException) {
                displayProgress(sincCalibrationTabGeneralStatusLabel, "Error configuring Module -- port in use by another application", 1, Status.ERROR);
            }
            else if (ex instanceof UnsupportedCommOperationException) {
                displayProgress(sincCalibrationTabGeneralStatusLabel, "Error configuring Module -- check USB dongle compatibility", 1, Status.ERROR);
            }
            else if (ex instanceof IOException) {
                displayProgress(sincCalibrationTabGeneralStatusLabel, "Lost connection to Module, try again", 1, Status.ERROR);
            }
            else {
                displayProgress(sincCalibrationTabGeneralStatusLabel, "Error configuring Module, try again", 1, Status.ERROR);
                ex.printStackTrace();
            }

        });

        new Thread(calibrationTask).start();
    }




    @FXML
    public void videoFileHandler(ActionEvent event)
    {
        FileChooser chooser;
        chooser = new FileChooser();
        chooser.setInitialDirectory(new java.io.File("."));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Select a video file", "*.mp4","*.avi","*.flac","*.mov"));
        File fileChosen = chooser.showOpenDialog(null);

        // if a file was chosen and the text box filled out, then 
        if (fileChosen != null && videoFilePathTextField.getText() != null) {

            String fileout = fileChosen.toString();

            Platform.runLater(() -> {
                // set the video text field to the file's path
                videoFilePathTextField.setText(fileout);

                // this is a workaround to a bug in JDK 8 where setText() on a TextField doesn't center it;
                // instead, resetting the alignment will cause it to recompute and fix itself
                // TODO remove this when the codebase is migrated to JDK 11 + OpenJFX
                videoFilePathTextField.setAlignment(Pos.TOP_LEFT);
                videoFilePathTextField.setAlignment(Pos.CENTER);
            });

            displayProgress(sincCalibrationTabGeneralStatusLabel, "Successfully loaded video file", 0.5, Status.SUCCESS);

        } else {
            displayProgress(sincCalibrationTabGeneralStatusLabel, "Invalid video file path", 1, Status.FAIL);
        }
    }


    @FXML
    private TextField videoFilePathTextField;

    public static int trueDelayAfterStart;

    @FXML
    public void applySINCHandler() {

        Task<Void> SINCTask = new Task<Void>() {

            @Override
            protected Void call() throws IOException, PortInUseException, UnsupportedCommOperationException {

                displayProgress(sincCalibrationTabGeneralStatusLabel, "Calibrating Module...", 1, Status.WORKING);

                // analyze video to obtain timer0 tick offset and "delay after start" test parameters
                BlackFrameAnalysis bfo = new BlackFrameAnalysis(videoFilePathTextField.getText());
                timerCalibrationOffset = bfo.getTMR0Offset();
                delayAfterStart = bfo.getDelayAfterStart();

                logger.info("Timer0 calibration offset: " + timerCalibrationOffset);
                logger.info("Delay after start: " + delayAfterStart);

                // apply timer0 and delay after start to module
                if (serialHandler.applyCalibrationOffsets(timerCalibrationOffset, delayAfterStart)) {
                    displayProgress(sincCalibrationTabGeneralStatusLabel, "Successfully calibrated Module (camera and Module synced)", 1, Status.SUCCESS);
                } else {
                    displayProgress(sincCalibrationTabGeneralStatusLabel, "Error applying calibration offsets, try again", 1, Status.ERROR);
                }

                return null;
            }
        };

        // failure messages for SINC calibration
        SINCTask.setOnFailed(event -> {

            Throwable ex = SINCTask.getException();

            logger.error("Calibration failed: " + ex);

            if (ex instanceof PortInUseException) {
                displayProgress(sincCalibrationTabGeneralStatusLabel, "Error calibrating Module -- port in use by another application", 1, Status.ERROR);
            }
            else if (ex instanceof UnsupportedCommOperationException) {
                displayProgress(sincCalibrationTabGeneralStatusLabel, "Error calibrating module -- check USB dongle compatibility", 1, Status.ERROR);
            }
            else if (ex instanceof IOException) {
                displayProgress(sincCalibrationTabGeneralStatusLabel, "Lost connection to Module, try again", 1, Status.ERROR);
            }
            else {
                displayProgress(sincCalibrationTabGeneralStatusLabel, "Error calibrating Module, try again", 1, Status.ERROR);
                ex.printStackTrace();
            }

        });

        // run SINC calibration in separate thread
        new Thread(SINCTask).start();

        // TODO is this comment still valid?
        //===================================
        // In Module Firmware 6, applying a negative delay after start will break the dashboard.
        // Therefore, if a negative value is calculated, 0 will be applied.
        // However, this prevents the storage of the true delay after start value in the csvp,
        // preventing adjusting for the offset in the graph. In firmware 7,
        // a negative delay after start will not break the module.
        // It will still delay 0, but it will also write that negtaive value to the csvp.
        // Allowing for correction in this graph. This section of code should be removed at that point.

    }

    /* Module Parameter Settings */

    /**
     * Get the desired tick threshold for the desired sample rate.
     * This effectively sets the sample rate of the module.
     * @param accelGyroSampleRate the sample rate for the sensor
     * @return the tick threshold for the desired sample rate
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
     *  Fills the testTypeHashMap with the module settings associated with each test type.
     */
    public void fillTestTypeHashMap() {

        // NOTE: index 2 (delayAfterStart) will NOT be overwritten through test parameters.

        Integer[] testParamsA = { 0, getTickThreshold(960), 0, 300, 0, 1, 30, 960, 96, 16, 2000, 92, 92 };
        testTypeHashMap.put("Conservation of Momentum (Elastic Collision)", testParamsA);

        Integer[] testParamsB = { 0, getTickThreshold(960), 0, 300, 0, 1, 30, 960, 96, 16, 2000, 92, 92 };
        testTypeHashMap.put("Conservation of Energy", testParamsB);

        Integer[] testParamsC = { 0, getTickThreshold(960), 0, 300, 0, 1, 30, 960, 96, 16, 2000, 92, 92 };
        testTypeHashMap.put("Inclined Plane", testParamsC);

        Integer[] testParamsD = { 0, getTickThreshold(960), 0, 300, 0, 1, 30, 960, 96, 16, 2000, 92, 92 };
        testTypeHashMap.put("Physical Pendulum", testParamsD);

        Integer[] testParamsE = { 0, getTickThreshold(960), 0, 300, 0, 1, 30, 960, 96, 16, 2000, 92, 92 };
        testTypeHashMap.put("Spring Test", testParamsE);

        Integer[] testParamsF = { 0, getTickThreshold(960), 0, 300, 0, 1, 30, 960, 96, 16, 2000, 92, 92 };
        testTypeHashMap.put("Generic Template - One Module", testParamsF);

        Integer[] testParamsG = { 0, getTickThreshold(960), 0, 300, 0, 1, 30, 960, 96, 16, 2000, 92, 92 };
        testTypeHashMap.put("Generic Template - Two Modules", testParamsG);

        // TODO the following test is not being used, but kept in case we decide to bring this lab back
        //Integer[] testParamsH = { 0, getTickThreshold(960), 0, 300, 1, 30, 960, 96, 16, 2000, 92, 92 };
        //testTypeHashMap.put("Conservation of Angular Momentum", testParamsH);
    }

    /**
     * Attempts to connect to a module.
     * Returns the thread created for chaining.
     */
    public Thread connectToModule(Label label) {

        Task<Void> connectTask = new Task<Void>() {
            
            @Override
            protected Void call() throws IOException, PortInUseException, UnsupportedCommOperationException {

                displayProgress(label, "Connecting to Module...", 0, Status.WORKING);

                // disable test parameters box
                Platform.runLater(() -> {
                    testTypeComboBox.setDisable(false);
                });

                // get all ports (null if none are found)
                ArrayList<String> ports = serialHandler.findPorts();
                logger.info("Searching available ports: " + ports);

                // loop through all ports
                for (int i = 0; i < ports.size(); i++) {

                    // get the name of the current COM port
                    String selectedCommID = ports.get(i);

                    // attempt connection to serial port
                    serialHandler.closeSerialPort();
                    serialHandler.openSerialPort(selectedCommID);

                    // attempt to read module info (used to check firmware ID)
                    ArrayList<Integer> moduleIDInfo = serialHandler.getModuleInfo(NUM_ID_INFO_PARAMETERS);

                    // check if port has a module connected; if not, move onto next port
                    if (moduleIDInfo == null) {
                        logger.debug("ModuleIDInfo null on " + selectedCommID + ", trying next port...");
                        continue;
                    }

                    int firmwareID = moduleIDInfo.get(2);
                    logger.debug("Current Firmware ID: " + firmwareID);

                    // make sure that Dashboard's firmware version matches the module's
                    if (firmwareID == CURRENT_FIRMWARE_ID) {

                        displayProgress(label, "Successfully connected to Module", 0.5, Status.SUCCESS);
                        logger.debug("Opened port " + selectedCommID);

                        // re-enable the test parameters selection box
                        Platform.runLater(() -> testTypeComboBox.setDisable(false));

                        return null;

                    }
                    else {
                        String firmwareMsg = "Incompatible firmware version, update Module to " + CURRENT_FIRMWARE_ID + " (currently " + firmwareID + ")";
                        displayProgress(label, firmwareMsg, 1, Status.FAIL);
                    }

                }

                displayProgress(label, "Make sure Module is connected to the computer", 1, Status.FAIL);
                
                return null;
            }
        };

        // failure messages for connection
        connectTask.setOnFailed(event -> {

            Throwable ex = connectTask.getException();

            logger.error("Connection failed: " + ex);

            if (ex instanceof PortInUseException) {
                displayProgress(label, "Error connecting to Module -- port in use by another application", 1, Status.ERROR);
            }
            else if (ex instanceof UnsupportedCommOperationException) {
                displayProgress(label, "Error connecting to Module -- check USB dongle compatibility", 1, Status.ERROR);
            }
            else if (ex instanceof IOException) {
                displayProgress(label, "Lost connection to Module, try again", 1, Status.ERROR);
            }
            else if (ex instanceof NullPointerException) {
                displayProgress(label, "Make sure Module is connected to the computer", 1, Status.FAIL);
            }
            else {
                displayProgress(label, "Error connecting to Module, try again", 1, Status.ERROR);
            }

        });

        // run the connection task 
        Thread t = new Thread(connectTask);
        t.start();

        return t;
    }

}