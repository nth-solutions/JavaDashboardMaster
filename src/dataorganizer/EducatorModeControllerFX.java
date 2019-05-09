package dataorganizer;


import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class EducatorModeControllerFX implements Initializable {



    /*
    GOALS:
    1. Multi-Test Saving Capabilities
    2. Reprogram module without reloading
    3. UI Bug Testing

     */

    //Test Parameter Variables and Constants
    public static final int NUM_TEST_PARAMETERS = 13;
    public static final int NUM_ID_INFO_PARAMETERS = 3;
    public static final int CURRENT_FIRMWARE_ID = 26;
    private static SerialComm serialHandler;
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
    Button readTestButton;
    @FXML
    ProgressBar progressBar;
    @FXML
    Button eraseButton;
    //Extra Test Parameter TextFields
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
    TextField momentOfIntertiaSpringTextField;
    @FXML
    TextField radiusOfTorqueArmSpringTextField;
    //Color Palette
    Color DeepBlue = Color.rgb(31, 120, 209);
    Color LightBlue = Color.rgb(76, 165, 255);
    Color LightOrange = Color.rgb(255, 105, 40);
    Color DarkGreen = Color.rgb(51, 204, 51);
    int selectedIndex;
    ToggleGroup outputType = new ToggleGroup();
    //Extra Module Parameters - CoM
    double massOfRightModule;
    double massOfRightGlider;
    double massOfLeftModule;
    double massOfLeftGlider;
    //Extra Module Parameters - CoE
    double totalDropDistance;
    double massOfModuleAndHolder;
    double momentOfInertiaCOE;
    double radiusOfTorqueArmCOE;
    //Extra Module Parameters - Pendulum
    double lengthOfPendulum;
    double distanceFromPivot;
    double massOfModule;
    double massOfHolder;
    //Extra Module Parameters - Spring
    double springConstant;
    double totalHangingMass;
    double momentOfIntertiaSpring;
    double radiusOfTorqueArmSpring;
    private DataOrganizer dataOrgo;
    //Dashboard Background Functionality
    private int experimentTabIndex = 0;
    private HashMap<String, ArrayList<Integer>> testTypeHashMap = new HashMap<String, ArrayList<Integer>>();
    private String testType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        testTypeComboBox.getItems().addAll("Conservation of Momentum (Elastic Collision)", "Conservation of Energy", "Inclined Plane", "Physical Pendulum", "Spring Test - Simple Harmonics");
        backButton.setVisible(false);
        serialHandler = new SerialComm();


        //Prevents more than one output type from being selected
        spreadsheetRadioButton.setToggleGroup(outputType);
        graphRadioButton.setToggleGroup(outputType);
        graphAndSpreadsheetRadioButton.setToggleGroup(outputType);
        sincTechnologyRadioButton.setToggleGroup(outputType);

        //TODO: CHECK IMPLEMENTATION OF "fillTestTypeHashMap()" ->> NOT SURE IF ACTUALLY USED EVER
        //TODO: TESTING PROCESS: APPLY CONFIGS AND THEN PULL PARAMS IN ADVANCED MODE TO SEE IF THEY HAVE BEEN SET

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
        generalStatusExperimentLabel.setTextFill(Color.BLACK);

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
        generalStatusExperimentLabel.setTextFill(Color.BLACK);

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
        selectedIndex = testTypeComboBox.getSelectionModel().getSelectedIndex();
        testParametersTabPane.getSelectionModel().select(selectedIndex);
    }

    /* Begin Experiment Tab Methods */

    /**
     * ActionEvent that writes the selected parameters and module configurations to the module for testing
     *
     * @param event
     */
    @FXML
    private void applyConfigurations(ActionEvent event) {
        writeButtonHandler();
        getExtraParameters(selectedIndex);
        readExtraTestParamsForTemplate();
    }


    //TODO: Fix UI Lock-up (Look into Tasks)
    //TODO: Fix inability to program the module multiple times

    /**
     * A handler method called within the applyConfigurations() ActionEvent that writes pre-defined optimal parameters
     * to the module's firmware for use in one of several experiments
     */
    private void writeButtonHandler() {

        Task writeParametersTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                //Disable write config button while the sendParameters() method is running
                applyConfigurationsButton.setDisable(true);
                nextButton.setDisable(true);
                if (findModuleCommPort()) {
                    generalStatusExperimentLabel.setTextFill(DarkGreen);
                    generalStatusExperimentLabel.setText("Initial connection to module successful");
                }
                try {
                    if (!serialHandler.sendTestParams(testTypeHashMap.get(testTypeComboBox.getSelectionModel().getSelectedItem().toString()))) {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        generalStatusExperimentLabel.setText("Module Not Responding, parameter write failed.");
                    } else {
                        generalStatusExperimentLabel.setTextFill(DarkGreen);
                        generalStatusExperimentLabel.setText("Module Configuration Successful, Parameters Have Been Updated");
                    }
                } catch (NumberFormatException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Please Fill out Every Field");
                } catch (IOException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Error Communicating With Serial Dongle");
                } catch (PortInUseException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Serial Port Already In Use");
                } catch (UnsupportedCommOperationException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Check Dongle Compatability");
                }

                //Re-enable the write config button when the routine has completed
                applyConfigurationsButton.setDisable(false);
                nextButton.setDisable(false);
                return null;

            }
        };

        Platform.runLater(() -> {
            generalStatusExperimentLabel.textProperty().bind(writeParametersTask.messageProperty());
        });

        new Thread(writeParametersTask).start();
    }


    /**
     * Fills extra test parameter class fields according to what textfields are shown
     *
     * @param comboBoxIndex Selected ComboBox Index that defines what parameter TextFields are shown for the UI
     */
    @FXML
    private void getExtraParameters(int comboBoxIndex) {
        generalStatusExperimentLabel.setText("");
        switch (comboBoxIndex) {
            case 0:
                //CoM
                try {
                    massOfRightModule = Double.parseDouble(massOfRightModuleTextField.getText());
                    massOfRightGlider = Double.parseDouble(massOfRightGliderTextField.getText());
                    massOfLeftModule = Double.parseDouble(massOfLeftModuleTextField.getText());
                    massOfLeftGlider = Double.parseDouble(massOfLeftGliderTextField.getText());
                    testType = "Conservation of Momentum (Elastic Collision)";
                } catch (NumberFormatException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Invalid or Missing Data");
                }

                break;
            case 1:
                //CoE
                try {
                    totalDropDistance = Double.parseDouble(totalDropDistanceTextField.getText());
                    massOfModuleAndHolder = Double.parseDouble(massOfModuleAndHolderTextField.getText());
                    momentOfInertiaCOE = Double.parseDouble(momentOfInertiaCOETextField.getText());
                    radiusOfTorqueArmCOE = Double.parseDouble(radiusOfTorqueArmCOETextField.getText());
                    testType = "Conservation of Energy";
                } catch (NumberFormatException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Invalid or Missing Data");
                }

                break;
            case 2:
                //IP
                testType = "Inclined Plane";
                break;
            case 3:
                //Pendulum
                try {
                    lengthOfPendulum = Double.parseDouble(lengthOfPendulumTextField.getText());
                    distanceFromPivot = Double.parseDouble(distanceFromPivotTextField.getText());
                    massOfModule = Double.parseDouble(massOfModuleTextField.getText());
                    massOfHolder = Double.parseDouble(massOfHolderTextField.getText());
                    testType = "Physical Pendulum";

                } catch (NumberFormatException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Invalid or Missing Data");
                }

                break;
            case 4:
                //Spring
                try {
                    springConstant = Double.parseDouble(springConstantTextField.getText());
                    totalHangingMass = Double.parseDouble(totalHangingMassTextField.getText());
                    momentOfIntertiaSpring = Double.parseDouble(momentOfIntertiaSpringTextField.getText());
                    radiusOfTorqueArmSpring = Double.parseDouble(radiusOfTorqueArmSpringTextField.getText());
                    testType = "Spring Test - Simple Harmonics";

                } catch (NumberFormatException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Invalid or Missing Data");
                }

                break;
            default:
                break;
        }

    }

    //TODO: Find a way to dynamically change the progress bar's color

    /**
     * ActionEvent that configures the module using functionality from the SerialComm Class for pairing with an RF remote
     * control
     *
     * @param event
     */
    @FXML
    private void pairNewRemote(ActionEvent event) {

        Task<Void> pairRemoteTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                pairNewRemoteButton.setDisable(true);
                unpairAllRemotesButton.setDisable(true);
                testRemotesButton.setDisable(true);

                generalStatusExperimentLabel.setTextFill(Color.BLACK);
                generalStatusExperimentLabel.setText("Module Listening for New Remote, Hold 'A' or 'B' Button to Pair");
                progressBar.setProgress(0);
                //progressBar.setForeground(Color.RED);

                try {
                    if (serialHandler.pairNewRemote()) {
                        generalStatusExperimentLabel.setTextFill(DarkGreen);
                        generalStatusExperimentLabel.setText("New Remote Successfully Paired");
                        progressBar.setProgress(100);
                        //progressBar.setForeground(new Color(51, 204, 51));
                    } else {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        generalStatusExperimentLabel.setText("Pair Unsuccessful, Receiver Timed Out");
                        progressBar.setProgress(100);
                        //progressBar.setForeground(Color.RED);
                    }


                } catch (IOException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Error Communicating With Serial Dongle");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(Color.RED);
                } catch (PortInUseException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Serial Port Already In Use");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(Color.RED);
                } catch (UnsupportedCommOperationException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Check Dongle Compatability");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(Color.RED);
                }

                //Enable buttons that can now be used since the bulk erase completed
                pairNewRemoteButton.setDisable(false);
                unpairAllRemotesButton.setDisable(false);
                testRemotesButton.setDisable(false);
                return null;
            }
        };

        Platform.runLater(() -> generalStatusExperimentLabel.textProperty().bind(pairRemoteTask.messageProperty()));

        new Thread(pairRemoteTask).start();
    }

    /**
     * ActionEvent that unpairs all known RF remote controls from the module using functionality from the SerialComm
     * class
     *
     * @param event
     */
    @FXML
    private void unpairRemotes(ActionEvent event) {
        Task<Void> unpairRemoteTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                //Disable buttons that should not be used in the middle of a sequence
                pairNewRemoteButton.setDisable(true);
                unpairAllRemotesButton.setDisable(true);
                testRemotesButton.setDisable(true);

                generalStatusExperimentLabel.setTextFill(Color.BLACK);
                generalStatusExperimentLabel.setText("Unpairing all Remotes...");
                progressBar.setProgress(0);
                //progressBar.setForeground(new Color(51, 204, 51));

                try {
                    serialHandler.unpairAllRemotes();
                } catch (IOException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Error Communicating With Serial Dongle");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                } catch (PortInUseException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Serial Port Already In Use");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                } catch (UnsupportedCommOperationException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Check Dongle Compatability");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                }


                pairNewRemoteButton.setDisable(false);
                unpairAllRemotesButton.setDisable(false);
                testRemotesButton.setDisable(false);

                generalStatusExperimentLabel.setTextFill(Color.BLACK);
                generalStatusExperimentLabel.setText("All Remotes Unpaired, There are 0 Remotes Paired to this Module");
                progressBar.setProgress(0);
                //progressBar.setForeground(new Color(51, 204, 51));
                return null;
            }
        };

        Platform.runLater(() -> {
            generalStatusExperimentLabel.textProperty().bind(unpairRemoteTask.messageProperty());
        });

        new Thread(unpairRemoteTask).start();

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
                //Disable buttons that should not be used in the middle of a sequence
                pairNewRemoteButton.setDisable(true);
                unpairAllRemotesButton.setDisable(true);
                testRemotesButton.setDisable(true);
                backButton.setDisable(true);
                nextButton.setDisable(true);
                exitTestModeButton.setDisable(false);


                //Notify the user that the bulk erase sequence has began
                generalStatusExperimentLabel.setTextFill(Color.BLACK);
                generalStatusExperimentLabel.setText("Press a Button on a Remote to Test if it is Paired");
                progressBar.setProgress(0);
                //progressBar.setForeground(new Color(51, 204, 51));

                try {
                    if (!serialHandler.testRemotesFX(generalStatusExperimentLabel)) {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        generalStatusExperimentLabel.setText("Error Communicating with Module");
                        progressBar.setProgress(100);
                        //progressBar.setForeground(new Color(255, 0, 0));
                    }
                } catch (IOException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Error Communicating With Serial Dongle");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                } catch (PortInUseException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Serial Port Already In Use");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                } catch (UnsupportedCommOperationException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Check Dongle Compatability");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                }

                //Enable button
                pairNewRemoteButton.setDisable(false);
                unpairAllRemotesButton.setDisable(false);
                testRemotesButton.setDisable(false);
                exitTestModeButton.setDisable(true);
                backButton.setDisable(false);
                nextButton.setDisable(false);

                //Notify the user that the sequence has completed
                generalStatusExperimentLabel.setTextFill(DarkGreen);
                generalStatusExperimentLabel.setText("Test Mode Successfully Exited");
                progressBar.setProgress(100);
                //progressBar.setForeground(DarkGreen);
                return null;
            }
        };

        Platform.runLater(() -> generalStatusExperimentLabel.textProperty().bind(testPairedRemoteTask.messageProperty()));

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
        serialHandler.exitRemoteTest();
    }

    @FXML
    private void readTestsFromModule(ActionEvent event) {
        String path = chooseSpreadsheetOutputPath(generalStatusExperimentLabel);
        PendulumSpreadsheetController pendulumSpreadsheetController = new PendulumSpreadsheetController();


        Task<Void> readTestsFromModuleTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                generalStatusExperimentLabel.setTextFill(Color.BLACK);
                ParameterSpreadsheetController parameterSpreadsheetController = new ParameterSpreadsheetController();
                //Disable read button while read is in progress
                backButton.setDisable(true);
                nextButton.setDisable(true);
                readTestButton.setDisable(true);

                try {
                    generalStatusExperimentLabel.setTextFill(Color.BLACK);
                    generalStatusExperimentLabel.setText("Reading Data from Module...");

                    //Read test parameters from module and store it in testParameters
                    ArrayList<Integer> testParameters = serialHandler.readTestParams(NUM_TEST_PARAMETERS);

                    //Executes if the reading of the test parameters was successful
                    if (testParameters != null) {

                        int expectedTestNum = testParameters.get(0);

                        //Assign local variables to their newly received values from the module
                        int timedTestFlag = testParameters.get(4);
                        //Trigger on release is 8
                        int testLength = testParameters.get(6);
                        int accelGyroSampleRate = testParameters.get(7);
                        int magSampleRate = testParameters.get(8);
                        int accelSensitivity = testParameters.get(9);
                        int gyroSensitivity = testParameters.get(10);
                        int accelFilter = testParameters.get(11);
                        int gyroFilter = testParameters.get(12);


                        double bytesPerSample = 18;
                        if (accelGyroSampleRate / magSampleRate == 10) {
                            bytesPerSample = 12.6;
                        }


                        String nameOfFile = "";

                        //Executes if there are tests on the module
                        if (expectedTestNum > 0) {

                            //Get date for file name
                            Date date = new Date();

                            //Assign file name
                            nameOfFile += (" " + accelGyroSampleRate + "-" + magSampleRate + " " + accelSensitivity + "G-" + accelFilter + " " + gyroSensitivity + "dps-" + gyroFilter + " MAG-N " + date.getDate() + getMonth(date.getMonth()) + (date.getYear() - 100) + ".csv");

                            HashMap<Integer, ArrayList<Integer>> testData;

                            //Store the test data from the dashboard passing in enough info that the progress bar will be accurately updated
                            //TODO: Update method
                            testData = serialHandler.readTestDataFX(expectedTestNum, progressBar, generalStatusExperimentLabel);

                            generalStatusExperimentLabel.setTextFill(DarkGreen);
                            generalStatusExperimentLabel.setText("All Data Received from Module");

                            //Executes if the data was received properly (null = fail)
                            if (testData != null) {
                                for (int testIndex = 0; testIndex < testData.size(); testIndex++) {

                                    int[] finalData = new int[testData.get(testIndex).size()];

                                    for (int byteIndex = 0; byteIndex < testData.get(testIndex).size(); byteIndex++) {
                                        if (testData.get(testIndex).get(byteIndex) != -1) {
                                            finalData[byteIndex] = testData.get(testIndex).get(byteIndex);
                                        } else {
                                            finalData[byteIndex] = -1;
                                            break;
                                        }
                                    }
                                    String tempName = "(#" + (testIndex + 1) + ") " + nameOfFile;
                                    dataOrgo = new DataOrganizer(testParameters, tempName);
                                    //Define operation that can be run in separate thread
                                    Runnable organizerOperation = new Runnable() {
                                        public void run() {

                                            //Organize data into .CSV
                                            dataOrgo.createDataSmpsRawData(finalData);


                                            if (spreadsheetRadioButton.isSelected()) {

                                                List<List<Double>> dataSamples = dataOrgo.getRawDataSamples();



                                                generalStatusExperimentLabel.setTextFill(Color.BLACK);
                                                generalStatusExperimentLabel.setText("Writing data to spreadsheet");

                                                if (testType == "Conservation of Momentum (Elastic Collision)"){
                                                    //TODO: CHANGE ARGS ->>>>> parameterSpreadsheetController.loadConservationofMomentumParameters(gliderOneMassDouble, gliderTwoMassDouble);
                                                    parameterSpreadsheetController.fillTemplateWithData(2, dataSamples);
                                                    parameterSpreadsheetController.saveWorkbook(path);
                                                }
                                                else if(testType == "Conservation of Energy"){

                                                    parameterSpreadsheetController.loadConservationofEnergyParameters();
                                                    parameterSpreadsheetController.fillTemplateWithData(2, dataSamples);
                                                    parameterSpreadsheetController.saveWorkbook(path);

                                                }
                                                else if(testType == "Inclined Plane") {
                                                    //TODO: ADD METHODS FOR INCLINED PLANE TESTS
                                                }
                                                else if(testType == "Physical Pendulum"){

                                                    //TODO: CHANGE ARGS ->>>>> parameterSpreadsheetController.loadPendulumParameters(pendulumLengthDouble, pendulumMassDouble, pendulumModuleMassDouble, pendulumModulePositionDouble);
                                                    parameterSpreadsheetController.fillTemplateWithData(2, dataSamples);
                                                    parameterSpreadsheetController.saveWorkbook(path);

                                                }else if(testType == "Spring Test - Simple Haromincs"){
                                                    //TODO: CHANGE ARGS ->>>>> parameterSpreadsheetController.loadSpringTestParameters(springConstantDouble, totalMassDouble, amplitudeDouble, massOfSpringDouble);
                                                    parameterSpreadsheetController.fillTemplateWithData(2, dataSamples);
                                                    parameterSpreadsheetController.saveWorkbook(path);

                                                }

                                                try {
                                                    Thread.sleep(10000);

                                                } catch (Exception exceptionalexception) {
                                                    System.out.println("If you got this error, something went seriously wrong");
                                                }


                                                generalStatusExperimentLabel.setTextFill(DarkGreen);
                                                generalStatusExperimentLabel.setText("Data Successfully Written");

                                                //Re-enable read button upon read completion
                                                backButton.setDisable(false);
                                                nextButton.setDisable(false);
                                                readTestButton.setDisable(false);

                                            }
                                            dataOrgo.getSignedData();
                                            //dataOrgo.createCSVP();
                                            //dataOrgo.createCSV(true, true); //Create CSV file, do label (column labels) the data (includes time axis), and sign the data

                                            //CSVBuilder.sortData(finalData, tempName, (accelGyroSampleRate / magSampleRate), settings.getKeyVal("CSVSaveLocation"), (getSelectedButtonText(group) == "Data (Excel)"), (timedTestFlag==1), testParameters)
                                        }
                                    };

                                    //Set thread to execute previously defined operation
                                    Thread organizerThread = new Thread(organizerOperation);
                                    //Start thread
                                    organizerThread.start();

                                }
                            } else {
                                generalStatusExperimentLabel.setTextFill(Color.RED);
                                generalStatusExperimentLabel.setText("Error Reading From Module, Try Again");
                                progressBar.setProgress(100);
                                //progressBar.setForeground(new Color(255, 0, 0));
                            }
                        } else {
                            generalStatusExperimentLabel.setTextFill(Color.RED);
                            generalStatusExperimentLabel.setText("No Tests Found on Module");
                            progressBar.setProgress(100);
                            //progressBar.setForeground(new Color(255, 0, 0));
                        }
                    } else {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        generalStatusExperimentLabel.setText("Error Reading From Module, Try Again");
                        progressBar.setProgress(100);
                        //progressBar.setForeground(new Color(255, 0, 0));
                    }

                } catch (IOException e) {

                    //Re-enable read button upon read completion
                    backButton.setDisable(false);
                    nextButton.setDisable(false);
                    readTestButton.setDisable(false);
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Error Communicating With Serial Dongle");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                } catch (PortInUseException e) {

                    //Re-enable read button upon read completion
                    backButton.setDisable(false);
                    nextButton.setDisable(false);
                    readTestButton.setDisable(false);
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Serial Port Already In Use");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                } catch (UnsupportedCommOperationException e) {

                    //Re-enable read button upon read completion
                    backButton.setDisable(false);
                    nextButton.setDisable(false);
                    readTestButton.setDisable(false);
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Check Dongle Compatability");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                }
                return null;
            }
        };

        Platform.runLater(() -> {
            generalStatusExperimentLabel.textProperty().bind(readTestsFromModuleTask.messageProperty());
        });

        new Thread(readTestsFromModuleTask).start();
    }

    @FXML
    private void launchMotionVisualization(ActionEvent event) {
        //TODO: Implement @ a Later Data
    }

    @FXML
    private void eraseTestsFromModule(ActionEvent event) {

        //Notify the user that the bulk erase sequence has began
        generalStatusExperimentLabel.setTextFill(Color.BLACK);
        generalStatusExperimentLabel.setText("Bulk Erasing...");
        Task eraseTestsFromModuleTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                //Disable buttons that should not be used in the middle of a sequence
                eraseButton.setDisable(true);

                //Notify the user that the bulk erase sequence has began
                generalStatusExperimentLabel.setTextFill(Color.BLACK);
                generalStatusExperimentLabel.setText("Bulk Erasing...");
                progressBar.setProgress(0);
                //progressBar.setForeground(new Color(51, 204, 51));

                try {
                    if (serialHandler.bulkEraseModule()) {
                        //Notify the user that the sequence has completed
                        generalStatusExperimentLabel.setTextFill(DarkGreen);
                        generalStatusExperimentLabel.setText("Bulk Erase Complete");
                        progressBar.setProgress(100);
                        //progressBar.setForeground(new Color(51, 204, 51));
                    } else {

                        //Notify the user that the sequence has failed
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        generalStatusExperimentLabel.setText("Bulk Erase Failed");
                        progressBar.setProgress(100);
                        //progressBar.setForeground(new Color(255, 0, 0));
                    }
                    //Enable buttons that can now be used since the sector erase completed
                    eraseButton.setDisable(false);
                } catch (IOException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Error Communicating With Serial Dongle");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                } catch (PortInUseException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Serial Port Already In Use");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                } catch (UnsupportedCommOperationException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Check Dongle Compatability");
                    progressBar.setProgress(100);
                    //progressBar.setForeground(new Color(255, 0, 0));
                }


                return null;
            }

        };
        Platform.runLater(() -> {
            generalStatusExperimentLabel.textProperty().bind(eraseTestsFromModuleTask.messageProperty());
        });
        new Thread(eraseTestsFromModuleTask).start();
    }

    /*End Experiment Tab Methods*/





    /*Begin Motion Visualization Tab Methods*/

    //Under Development... Will have methods soon

    /*End Motion Visualization Tab Methods*/




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


    public boolean findModuleCommPort() {
        class threadHack {
            private boolean status = false;

            public boolean getStatus() {
                return status;
            }

            public void setStatus(boolean x) {
                status = x;
            }
        }
        final threadHack th = new threadHack();


        Platform.runLater(() -> {
            try {
                ArrayList<String> commPortIDList = serialHandler.findPorts();
                boolean moduleFound = false;
                int commPortIndex = 0;
                while (!moduleFound && commPortIndex < commPortIDList.size()) {

                    //Get the string identifier (name) of the current port
                    String selectedCommID = commPortIDList.toArray()[commPortIndex].toString();

                    //Open the serial port with the selected name, initialize input and output streams, set necessary flags so the whole program know that everything is initialized
                    if (serialHandler.openSerialPort(selectedCommID)) {

                        int attemptCounter = 0;
                        while (attemptCounter < 3 && !moduleFound) {
                            try {
                                ArrayList<Integer> moduleIDInfo = serialHandler.getModuleInfo(NUM_ID_INFO_PARAMETERS);

                                if (moduleIDInfo != null) {
                                    moduleFound = true;

                                    if (moduleIDInfo.get(2) != CURRENT_FIRMWARE_ID) {
                                        generalStatusExperimentLabel.setTextFill(Color.RED);
                                        generalStatusExperimentLabel.setText("Incompatable Firmware Version: " + moduleIDInfo.get(2) + ", Program Module with Version " + CURRENT_FIRMWARE_ID);
                                    } else {
                                        generalStatusExperimentLabel.setTextFill(DarkGreen);
                                        generalStatusExperimentLabel.setText("Successfully Connected to Module");
                                    }
                                } else {
                                    attemptCounter++;
                                }
                            } catch (IOException e) {
                                attemptCounter++;
                            } catch (PortInUseException e) {
                                attemptCounter++;
                            } catch (UnsupportedCommOperationException e) {
                                attemptCounter++;
                            }
                        }

                    }
                    commPortIndex++;
                }
                if (!moduleFound) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
                    th.setStatus(false);
                }

            } catch (IOException e) {
                generalStatusExperimentLabel.setTextFill(Color.RED);
                generalStatusExperimentLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
                th.setStatus(false);
            } catch (PortInUseException e) {
                generalStatusExperimentLabel.setTextFill(Color.RED);
                generalStatusExperimentLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
                th.setStatus(false);
            }
        });

//        Runnable findModuleOperation = new Runnable() {
//            public void run() {
//                try {
//                    ArrayList<String> commPortIDList = serialHandler.findPorts();
//                    boolean moduleFound = false;
//                    int commPortIndex = 0;
//                    while (!moduleFound && commPortIndex < commPortIDList.size()) {
//
//                        //Get the string identifier (name) of the current port
//                        String selectedCommID = commPortIDList.toArray()[commPortIndex].toString();
//
//                        //Open the serial port with the selected name, initialize input and output streams, set necessary flags so the whole program know that everything is initialized
//                        if(serialHandler.openSerialPort(selectedCommID)){
//
//                            int attemptCounter = 0;
//                            while (attemptCounter < 3 && !moduleFound) {
//                                try {
//                                    ArrayList<Integer> moduleIDInfo = serialHandler.getModuleInfo(NUM_ID_INFO_PARAMETERS);
//
//                                    if (moduleIDInfo != null) {
//                                        moduleFound = true;
//
//                                        if (moduleIDInfo.get(2) != CURRENT_FIRMWARE_ID) {
//                                            generalStatusExperimentLabel.setTextFill(Color.RED);
//                                            generalStatusExperimentLabel.setText("Incompatable Firmware Version: " + moduleIDInfo.get(2) + ", Program Module with Version " + CURRENT_FIRMWARE_ID);
//                                        }
//                                        else {
//                                            generalStatusExperimentLabel.setTextFill(DarkGreen);
//                                            generalStatusExperimentLabel.setText("Successfully Connected to Module");
//                                        }
//                                    }
//                                    else {
//                                        attemptCounter++;
//                                    }
//                                }
//                                catch (IOException e) {
//                                    attemptCounter++;
//                                }
//                                catch (PortInUseException e) {
//                                    attemptCounter++;
//                                }
//                                catch (UnsupportedCommOperationException e) {
//                                    attemptCounter++;
//                                }
//                            }
//
//                        }
//                        commPortIndex++;
//                    }
//                    if (!moduleFound) {
//                        generalStatusExperimentLabel.setTextFill(Color.RED);
//                        generalStatusExperimentLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
//                        th.setStatus(false);
//                    }
//
//                }
//                catch (IOException e) {
//                    generalStatusExperimentLabel.setTextFill(Color.RED);
//                    generalStatusExperimentLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
//                    th.setStatus(false);
//                }
//                catch (PortInUseException e) {
//                    generalStatusExperimentLabel.setTextFill(Color.RED);
//                    generalStatusExperimentLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
//                    th.setStatus(false);
//                }
//            }
//        };
//        Thread findModuleThread = new Thread(findModuleOperation);
//        findModuleThread.run();
        return th.getStatus();
    }

    public String chooseSpreadsheetOutputPath(Label label) {
        generalStatusExperimentLabel.setTextFill(Color.BLACK);
        generalStatusExperimentLabel.setText("Copying File Template...");

        FileChooser chooser;
        chooser = new FileChooser();
        chooser.setInitialDirectory(new java.io.File("."));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Name Output File (*.xlsx)", "*.xlsx"));
        File file = chooser.showSaveDialog(null);
        if (file != null) {

            String fileout = file.toString();

            generalStatusExperimentLabel.setTextFill(DarkGreen);
            generalStatusExperimentLabel.setText("File Copy finished!");

            if (!fileout.endsWith(".xlsx")) {
                return fileout + ".xlsx";
            } else {
                return fileout;
            }

        } else {
            generalStatusExperimentLabel.setTextFill(Color.RED);
            generalStatusExperimentLabel.setText("Invalid File Path Entered");
            return "Invalid File Path";
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

    //Not Sure why this method is needed but the program will not work without it for some reason
    public void readExtraTestParamsForTemplate() {
        //params.put(testType).put(variable) = x,y,content
        class CellData {
            public int X;
            public int Y;
            public String content;
        }

        HashMap<String, CellData> param = new HashMap<>();
        CellData cell = new CellData();


        switch (testType) {
            case "Conservation of Momentum (Elastic Collision)":

                //Write param to hashmap and location of template to write in
                cell.X = 3;
                cell.Y = 3;
                cell.content = testTypeHashMap.get(testType).get(7).toString();    //Sample Rate
                param.put("SampleRate", cell);

                //Write param to hashmap and location of template to write in
                cell.X = 3;
                cell.Y = 4;
                cell.content = testTypeHashMap.get(testType).get(9).toString();    //Accel Sensitivity
                param.put("AccelSensitivity", cell);


                //Write param to hashmap and location of template to write in
                cell.X = 3;
                cell.Y = 5;
                cell.content = testTypeHashMap.get(testType).get(10).toString();    //Gyro Rate
                param.put("GyroSensitivity", cell);

                cell.X = 3;
                cell.Y = 8;
                cell.content = massOfLeftGliderTextField.getText();
                param.put("firstGliderAndModuleMassTextField", cell);

                cell.X = 3;
                cell.Y = 9;
                cell.content = massOfRightGliderTextField.getText();
                param.put("secondGliderAndModuleMassTextField", cell);
                break;


            case "Physical Spring":
                cell.X = 3;
                cell.Y = 3;
                cell.content = testTypeHashMap.get(testType).get(7).toString();    //Sample Rate

                param.put("sampleRate", cell);

                cell.X = 3;
                cell.Y = 4;
                cell.content = testTypeHashMap.get(testType).get(9).toString();    //Accel Sensitivity
                param.put("AccelSensitivity", cell);

                cell.X = 3;
                cell.Y = 5;
                cell.content = testTypeHashMap.get(testType).get(10).toString();//Gyro Sensitivity
                param.put("GyroSensitivity", cell);
                break;
            case "Spinny Stool":

        }
    }

}


