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
import java.util.concurrent.FutureTask;

/*
GOALS:
1. Fill-in Template Paths
2. Multi-test saving
3. Motion Visualization UI Opening
4. Motion Visualization Fixing
 */


public class EducatorModeControllerFX implements Initializable {

    //Test Parameter Variables and Constants
    public static final int NUM_TEST_PARAMETERS = 13;
    public static final int NUM_ID_INFO_PARAMETERS = 3;
    public static final int CURRENT_FIRMWARE_ID = 26;
    private static SerialComm serialHandler = new SerialComm();
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
    TextField momentOfInertiaSpringTextField;
    @FXML
    TextField radiusOfTorqueArmSpringTextField;

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
    //Colors
    private Color DarkGreen = Color.rgb(51, 204, 51);
    //Dashboard Background Functionality
    private int experimentTabIndex = 0;
    private int selectedIndex = 0;
    private HashMap<String, ArrayList<Integer>> testTypeHashMap = new HashMap<>();
    private ToggleGroup outputTypeToggleGroup = new ToggleGroup();
    public static String testType;


    private Boolean moduleConnected;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        testTypeComboBox.getItems().addAll("Conservation of Momentum (Elastic Collision)", "Conservation of Energy", "Inclined Plane", "Physical Pendulum", "Spring Test - Simple Harmonics");
        backButton.setVisible(false);
        initializeToggleGroup();
        fillTestTypeHashMap();

        moduleConnected = findModuleCommPort();
    }

    /**
     * This method is utilized solely to clean up method implementation within the initialize() method. Essentially, this
     * method adds several key UI radioButtons to the outputTypeToggleGroup ToggleGroup, and then assigns each button a userData
     * object which is used to identify which toggle is being selected in the readTestsFromModule() ActionEvent called below
     */
    private void initializeToggleGroup() {
        //Prevents more than one output type from being selected
        spreadsheetRadioButton.setToggleGroup(outputTypeToggleGroup);  //Adds a RadioButton to the outputTypeToggleGroup Toggle Group
        spreadsheetRadioButton.setUserData("spreadSheetRadioButton");   //Assigns the RadioButton a userData object
        graphRadioButton.setToggleGroup(outputTypeToggleGroup);
        graphRadioButton.setUserData("graphRadioButton");
        graphAndSpreadsheetRadioButton.setToggleGroup(outputTypeToggleGroup);
        graphAndSpreadsheetRadioButton.setUserData("graphAndSpreadsheetRadioButton");
        sincTechnologyRadioButton.setToggleGroup(outputTypeToggleGroup);
        sincTechnologyRadioButton.setUserData("sincTechnologyRadioButton");
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
        int numberOfTabs = 4; //Begins at 0. Notates the total number of tabs within the experiment procedure tab pane
        generalStatusExperimentLabel.setText("");   // Resets the status text to blank for each new page
        generalStatusExperimentLabel.setTextFill(Color.BLACK);
        progressBar.setProgress(0);
        progressBar.setStyle("-fx-accent: #1f78d1;");

        if (experimentTabIndex == numberOfTabs) {  // If the Index is 4, the maximum tab index has been reached and the index is reset to origin
            experimentTabIndex = -1;
            nextButton.setDisable(true);    //Disables the nextButton until new parameters have been written
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
        progressBar.setProgress(0);
        progressBar.setStyle("-fx-accent: #1f78d1;");

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
        selectedIndex = testTypeComboBox.getSelectionModel().getSelectedIndex() + 1; //Gets the index of the test type selected by user within the combobox
        testParametersTabPane.getSelectionModel().select(selectedIndex ); //Since the number of tabs matches the length of the combobox selection model, the user's
                                                                         //selected index is used to select the matching tab pane index to display
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
    }

    //TODO: Untested Task Implementation
    /**
     * A handler method called within the applyConfigurations() ActionEvent that writes pre-defined optimal parameters
     * to the module's firmware for use in one of several experiments
     */
    private void writeButtonHandler() {
        Platform.runLater(() -> {
            //Disable write config button while the sendParameters() method is running
            applyConfigurationsButton.setDisable(true);
            nextButton.setDisable(true);
            if (moduleConnected) {  //If the moduleConnected Boolean is true, the UI will update to inform the user the module has been connected
                generalStatusExperimentLabel.setTextFill(DarkGreen);
                generalStatusExperimentLabel.setText("Initial connection to module successful");
            }
            try {
                if (!serialHandler.sendTestParams(testTypeHashMap.get(testTypeComboBox.getSelectionModel().getSelectedItem()))) {
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
        });

    }


    /**
     * Fills extra test parameter class fields according to what textfields are shown
     *
     * @param comboBoxIndex Selected ComboBox Index that defines what parameter TextFields are shown for the UI
     */
    @FXML
    private void getExtraParameters(int comboBoxIndex) {
        generalStatusExperimentLabel.setText("");
        switch (comboBoxIndex-1) {
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
                    momentOfIntertiaSpring = Double.parseDouble(momentOfInertiaSpringTextField.getText());
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

    /**
     * ActionEvent that configures the module using functionality from the SerialComm Class for pairing with an RF remote
     * control
     *
     * @param event
     */
    @FXML
    private void pairNewRemote(ActionEvent event) {

        Task<Void> pairNewRemoteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int maxProgress = 100;
                updateMessage("Module Listening for New Remote, Hold 'A' or 'B' Button to Pair");
                updateProgress(0, maxProgress);

                Platform.runLater(() -> {
                    generalStatusExperimentLabel.setTextFill(Color.BLACK);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

                try {
                    if (serialHandler.pairNewRemote()) {
                        updateMessage("New Remote Successfully Paired");
                        updateProgress(100, maxProgress);

                        Platform.runLater(() -> {
                            generalStatusExperimentLabel.setTextFill(Color.GREEN);
                            progressBar.setStyle("-fx-accent: #1f78d1;");
                        });

                    } else {
                        updateMessage("Pair Unsuccessful, Receiver Timed Out");
                        updateProgress(100, maxProgress);

                        Platform.runLater(() -> {
                            generalStatusExperimentLabel.setTextFill(Color.RED);
                            progressBar.setStyle("-fx-accent: red;");
                        });
                    }

                } catch (IOException e) {
                    updateMessage("Error Communicating With Serial Dongle");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (PortInUseException e) {
                    updateMessage("Serial Port Already In Use");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (UnsupportedCommOperationException e) {
                    updateMessage("Check Dongle Compatability");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                }
                return null;
            }
        };

        pairNewRemoteButton.disableProperty().bind(pairNewRemoteTask.runningProperty());
        unpairAllRemotesButton.disableProperty().bind(pairNewRemoteTask.runningProperty());
        testRemotesButton.disableProperty().bind(pairNewRemoteTask.runningProperty());
        nextButton.disableProperty().bind(pairNewRemoteTask.runningProperty());
        backButton.disableProperty().bind(pairNewRemoteTask.runningProperty());
        generalStatusExperimentLabel.textProperty().bind(pairNewRemoteTask.messageProperty());
        progressBar.progressProperty().bind(pairNewRemoteTask.progressProperty());

        pairNewRemoteTask.setOnSucceeded(e -> {
            pairNewRemoteButton.disableProperty().unbind();
            unpairAllRemotesButton.disableProperty().unbind();
            testRemotesButton.disableProperty().unbind();
            nextButton.disableProperty().unbind();
            backButton.disableProperty().unbind();
            generalStatusExperimentLabel.textProperty().unbind();
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
                    generalStatusExperimentLabel.setTextFill(Color.BLACK);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

                try {
                    serialHandler.unpairAllRemotes();
                } catch (IOException e) {

                    updateMessage("Error Communicating With Serial Dongle");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (PortInUseException e) {

                    updateMessage("Serial Port Already In Use");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (UnsupportedCommOperationException e) {

                    updateMessage("Check Dongle Compatability");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                }

                updateMessage("All Remotes Unpaired, There are 0 Remotes Paired to this Module");
                updateProgress(0, maxProgress);

                Platform.runLater(() -> {
                    generalStatusExperimentLabel.setTextFill(Color.BLACK);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

                generalStatusExperimentLabel.setTextFill(Color.BLACK);
                generalStatusExperimentLabel.setText("All Remotes Unpaired, There are 0 Remotes Paired to this Module");
                progressBar.setProgress(0);
                //progressBar.setForeground(new Color(51, 204, 51));

                return null;
            }
        };

        pairNewRemoteButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        unpairAllRemotesButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        testRemotesButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        nextButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        backButton.disableProperty().bind(unpairRemotesTask.runningProperty());
        generalStatusExperimentLabel.textProperty().bind(unpairRemotesTask.messageProperty());
        progressBar.progressProperty().bind(unpairRemotesTask.progressProperty());

        unpairRemotesTask.setOnSucceeded(e -> {
            pairNewRemoteButton.disableProperty().unbind();
            unpairAllRemotesButton.disableProperty().unbind();
            testRemotesButton.disableProperty().unbind();
            nextButton.disableProperty().unbind();
            backButton.disableProperty().unbind();
            generalStatusExperimentLabel.textProperty().unbind();
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
                    generalStatusExperimentLabel.setTextFill(Color.BLACK);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

                try {
                    generalStatusExperimentLabel.textProperty().unbind(); //Unbinds the Label from the testPairedRemoteTask so that the new task created in testRemotesFX can take control over it

                    if (!serialHandler.testRemotesFX(generalStatusExperimentLabel)) {

                        generalStatusExperimentLabel.textProperty().bind(messageProperty()); //Rebinds the Label to the testPairedRemotesTask

                        updateMessage("Error Communicating with Module");
                        updateProgress(100, maxProgress);

                        Platform.runLater(() -> {
                            generalStatusExperimentLabel.setTextFill(Color.RED);
                            progressBar.setStyle("-fx-accent: red;");
                        });

                    }
                } catch (IOException e) {

                    updateMessage("Error Communicating With Serial Dongle");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (PortInUseException e) {

                    updateMessage("Serial Port Already In Use");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });

                } catch (UnsupportedCommOperationException e) {

                    updateMessage("Check Dongle Compatability");
                    updateProgress(100, maxProgress);

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                    });
                }

                updateMessage("Test Mode Successfully Exited");
                updateProgress(100, maxProgress);

                Platform.runLater(() -> {
                    generalStatusExperimentLabel.setTextFill(DarkGreen);
                    progressBar.setStyle("-fx-accent: #1f78d1;");
                });

                return null;
            }
        };


        pairNewRemoteButton.disableProperty().bind(testPairedRemoteTask.runningProperty());
        unpairAllRemotesButton.disableProperty().bind(testPairedRemoteTask.runningProperty());
        testRemotesButton.disableProperty().bind(testPairedRemoteTask.runningProperty());
        nextButton.disableProperty().bind(testPairedRemoteTask.runningProperty());
        backButton.disableProperty().bind(testPairedRemoteTask.runningProperty());
        generalStatusExperimentLabel.textProperty().bind(testPairedRemoteTask.messageProperty());
        progressBar.progressProperty().bind(testPairedRemoteTask.progressProperty());


        testPairedRemoteTask.setOnSucceeded(e -> {
            pairNewRemoteButton.disableProperty().unbind();
            unpairAllRemotesButton.disableProperty().unbind();
            testRemotesButton.disableProperty().unbind();
            nextButton.disableProperty().unbind();
            backButton.disableProperty().unbind();
            generalStatusExperimentLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();

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
        serialHandler.exitRemoteTest();
        generalStatusExperimentLabel.setTextFill(Color.BLACK);
        generalStatusExperimentLabel.setText("");

    }

    /**
     * This method gets the selected toggle and its assigned userData from the outputTypeToggleGroup ToggleGroup and returns it as
     * a string.
     * @return String that details what output type has been selected from the outputTypeToggleGroup ToggleGroup
     */
    private String getOutputTypeToggle() {
        String outputSelected = outputTypeToggleGroup.getSelectedToggle().getUserData().toString();
        return outputSelected;
    }

    /**
     * This method reads all of the data captured by the module during a testing period; then, depending on the output
     * type selected (from a defined ToggleGroup of output options), the data is then handled accordingly.
     * @param event
     */
    @FXML
    private void readTestsFromModule(ActionEvent event) {

        String outputSelected = getOutputTypeToggle();

        HashMap<Integer, ArrayList<Integer>>[] testDataArray = new HashMap[1];

        FutureTask<HashMap<Integer, ArrayList<Integer>>[]> readTestsFromModuleTask = new FutureTask<HashMap<Integer, ArrayList<Integer>>[]>(new Runnable() {
            @Override
            public void run() {
                String path = chooseSpreadsheetOutputPath(generalStatusExperimentLabel);
                ParameterSpreadsheetController parameterSpreadsheetController = new ParameterSpreadsheetController();

                try {
                    ArrayList<Integer> testParameters = serialHandler.readTestParams(NUM_TEST_PARAMETERS);


                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setText("Reading Data from Module...");
                        generalStatusExperimentLabel.setTextFill(Color.BLACK);
                    });

                    //Read test parameters from module and store it in testParameters


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
                            testData = serialHandler.readTestDataFX(expectedTestNum, progressBar, generalStatusExperimentLabel);

                            Platform.runLater(() -> {
                                generalStatusExperimentLabel.setText("All Data Received from Module");
                                generalStatusExperimentLabel.setTextFill(DarkGreen);
                            });

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
                                    //TODO: This will probably throw an error
                                    Runnable organizerOperation = new Runnable() {
                                        public void run() {

                                            //Organize data into .CSV
                                            dataOrgo.createDataSmpsRawData(finalData);

                                            //TODO: This will throw Errors because its handling UI components
                                            if (spreadsheetRadioButton.isSelected()) {

                                                List<List<Double>> dataSamples = dataOrgo.getRawDataSamples();

                                                Platform.runLater(() -> {
                                                    generalStatusExperimentLabel.setText("Writing data to spreadsheet");
                                                    generalStatusExperimentLabel.setTextFill(Color.BLACK);
                                                });

                                                if (testType == "Conservation of Momentum (Elastic Collision)") {
                                                    parameterSpreadsheetController.loadConservationofMomentumParameters(massOfLeftGlider, massOfRightGlider);
                                                    parameterSpreadsheetController.fillTemplateWithData(2, dataSamples);
                                                    parameterSpreadsheetController.saveWorkbook(path);
                                                } else if (testType == "Conservation of Energy") {
                                                    parameterSpreadsheetController.loadConservationofEnergyParameters();
                                                    parameterSpreadsheetController.fillTemplateWithData(2, dataSamples);
                                                    parameterSpreadsheetController.saveWorkbook(path);
                                                } else if (testType == "Inclined Plane") {
                                                    parameterSpreadsheetController.fillTemplateWithData(2, dataSamples);
                                                    parameterSpreadsheetController.saveWorkbook(path);
                                                } else if (testType == "Physical Pendulum") {

                                                    parameterSpreadsheetController.loadPendulumParameters(lengthOfPendulum, massOfHolder, massOfModule, distanceFromPivot);
                                                    parameterSpreadsheetController.fillTemplateWithData(2, dataSamples);
                                                    parameterSpreadsheetController.saveWorkbook(path);
                                                    System.out.println("debug");
                                                } else if (testType == "Spring Test - Simple Harmonics") {
                                                    parameterSpreadsheetController.loadSpringTestParameters(springConstant, totalHangingMass, momentOfIntertiaSpring, radiusOfTorqueArmSpring);
                                                    parameterSpreadsheetController.fillTemplateWithData(2, dataSamples);
                                                    parameterSpreadsheetController.saveWorkbook(path);
                                                }

                                                try {
                                                    Thread.sleep(10000);

                                                } catch (Exception exceptionalexception) {
                                                    System.out.println("If you got this error, something went seriously wrong");
                                                }


                                                Platform.runLater(() -> {
                                                    generalStatusExperimentLabel.setText("Data Successfully Written");
                                                    generalStatusExperimentLabel.setTextFill(DarkGreen);
                                                });

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

                                Platform.runLater(() -> {
                                    generalStatusExperimentLabel.setText("Error Reading From Module, Try Again");
                                    generalStatusExperimentLabel.setTextFill(Color.RED);
                                    progressBar.setStyle("-fx-accent: red;");
                                    progressBar.setProgress(100);
                                });

                            }
                        } else {

                            Platform.runLater(() -> {
                                generalStatusExperimentLabel.setText("No Tests Found on Module");
                                generalStatusExperimentLabel.setTextFill(Color.RED);
                                progressBar.setStyle("-fx-accent: red;");
                                progressBar.setProgress(100);
                            });
                        }
                    } else {

                        Platform.runLater(() -> {
                            generalStatusExperimentLabel.setText("Error Reading From Module, Try Again");
                            generalStatusExperimentLabel.setTextFill(Color.RED);
                            progressBar.setStyle("-fx-accent: red;");
                            progressBar.setProgress(100);
                        });
                    }
                } catch (IOException e) {

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setText("Error Communicating With Serial Dongle");
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                        progressBar.setProgress(100);
                    });

                } catch (PortInUseException e) {

                    Platform.runLater(() -> {
                        generalStatusExperimentLabel.setText("Serial Port Already In Use");

                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                        progressBar.setProgress(100);
                    });

                } catch (UnsupportedCommOperationException e) {

                    Platform.runLater(() -> {
                       generalStatusExperimentLabel.setText("Check Dongle Compatability");
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        progressBar.setStyle("-fx-accent: red;");
                        progressBar.setProgress(100);
                    });

                }


            }

        },testDataArray);

        readTestsFromModuleTask.run();

    }

    @FXML
    private void launchMotionVisualization(ActionEvent event) {
        //TODO: Implement @ a Later Data
    }


    /**
     * ActionEvent that handles bulk erasing the module's tests. Implements the Task Class, which essentially creates
     * an FX Safe Thread. All relavant UI parameters are bound to specific ReadOnly Properties defined in the Worker
     * Interface. After a successful Task run, all of the properties are unbound so as to be released back to the main
     * UI Thread.
     *
     * @param event
     */
    @FXML
    private void eraseTestsFromModule(ActionEvent event) {

        Task<Void> eraseTestsTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int maxProgress = 100;  //Defines local variable for maximum Progress Bar progress
                updateMessage("Bulk Erasing...");   //Updates the Task's readable message property in order to update generalStatusText
                updateProgress(0, maxProgress); //Updates the Task's readable progress property in order to update the Progress Bar

                Platform.runLater(() -> {   // Platform.runLater() uses a runnable (defined as a lambda expression) to control UI coloring
                    progressBar.setStyle("-fx-accent: #1f78d1;");   //Updates the progress bar's color style with a CSS call, setting its color back to its origin
                    generalStatusExperimentLabel.setTextFill(Color.BLACK);  //Updates the generalStatusExperimentLabel's text fill (coloring) back to black
                });

                try {

                    if (serialHandler.bulkEraseModule()) {  // Checks if the module is curently being bulk erased
                        //Notify the user that the sequence has completed
                        updateMessage("Bulk Erase Complete");
                        Platform.runLater(() -> {
                            progressBar.setStyle("-fx-accent: #1f78d1;");
                            generalStatusExperimentLabel.setTextFill(DarkGreen);
                        });
                        updateProgress(100, maxProgress);
                    } else {
                        updateMessage("Bulk Erase Failed");
                        updateProgress(100, maxProgress);
                        Platform.runLater(() -> {
                            progressBar.setStyle("-fx-accent: red;");
                            generalStatusExperimentLabel.setTextFill(Color.RED);
                        });
                    }
                } catch (IOException e) {
                    updateMessage("Error Communicating With Serial Dongle");
                    updateProgress(100, maxProgress);
                    Platform.runLater(() -> {
                        progressBar.setStyle("-fx-accent: red;");
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                    });
                } catch (PortInUseException e) {
                    updateMessage("Serial Port Already In Use");
                    updateProgress(100, maxProgress);
                    Platform.runLater(() -> {
                        progressBar.setStyle("-fx-accent: red;");
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                    });
                } catch (UnsupportedCommOperationException e) {
                    updateMessage("Check Dongle Compatibility");
                    updateProgress(100, maxProgress);
                    Platform.runLater(() -> {
                        progressBar.setStyle("-fx-accent: red;");
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                    });
                }

                return null;
            }
        };

        // Binds UI properties on the pairing tab to read only properties of the Task so that the UI may be edited in a thread different from the main UI thread
        generalStatusExperimentLabel.textProperty().bind(eraseTestsTask.messageProperty());
        nextButton.disableProperty().bind(eraseTestsTask.runningProperty());
        backButton.disableProperty().bind(eraseTestsTask.runningProperty());
        eraseButton.disableProperty().bind(eraseTestsTask.runningProperty());
        progressBar.progressProperty().bind(eraseTestsTask.progressProperty());

        eraseTestsTask.setOnSucceeded(e -> {    // If the task successfully completes its routine, the UI components are unbound, releasing their control back to the main UI thread
            generalStatusExperimentLabel.textProperty().unbind();
            nextButton.disableProperty().unbind();
            backButton.disableProperty().unbind();
            eraseButton.disableProperty().unbind();
            progressBar.progressProperty().unbind();

        });

        new Thread(eraseTestsTask).start(); //Starts an anonymous thread, passing it the Task defined above

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

    //TODO: Untested implementation
    /***
     *  Fills the testTypeHashMap with the module settings associated with each test type
     *
     */
    public void fillTestTypeHashMap() {
        ArrayList<Integer> testParams = new ArrayList<Integer>();

        //0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
        testParams.add(0);
        //1 Timer0 Tick Threshold
        testParams.add(getTickThreshold(960));
        //2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
        testParams.add(0);
        //3 Battery timeout flag
        testParams.add(300);
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


