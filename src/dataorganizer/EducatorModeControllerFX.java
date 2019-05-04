package dataorganizer;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;


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
    
    

    //Test Parameter Variables and Constants
    public static final int NUM_TEST_PARAMETERS = 13;
    public static final int NUM_ID_INFO_PARAMETERS = 3;
    public static final int CURRENT_FIRMWARE_ID = 26;
    private DataOrganizer dataOrgo;

    //Color Palette
    Color DeepBlue = Color.rgb(31, 120, 209);
    Color LightBlue = Color.rgb(76, 165, 255);
    Color LightOrange = Color.rgb(255, 105, 40);
    Color DarkGreen = Color.rgb(51, 204, 51);


    //Dashboard Background Functionality
    private int experimentTabIndex = 0;
    int selectedIndex; 
    private static SerialComm serialHandler;
    private HashMap<String, ArrayList<Integer>> testTypeHashMap = new HashMap<String, ArrayList<Integer>>();
    
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
        selectedIndex = testTypeComboBox.getSelectionModel().getSelectedIndex();
        testParametersTabPane.getSelectionModel().select(selectedIndex);
    }


    /* Begin Experiment Tab Methods */

    /**
     * ActionEvent that writes the selected parameters and module configurations to the module for testing
     * @param event
     */
    @FXML
    private void applyConfigurations(ActionEvent event) {
        getExtraParameters(selectedIndex);
        //writeButtonHandler();		//TODO: Fix Threading Issue
        //TODO: Import readExtraTestParamsForTemplate()
    }

    private void writeButtonHandler() {
        //Define no operation that can be run in a thread
        Runnable sendParamOperation = new Runnable() {
            public void run() {
                //Disable write config button while the sendParameters() method is running
                applyConfigurationsButton.setDisable(true);;
                nextButton.setDisable(true);
                if(findModuleCommPort()) {
                    generalStatusExperimentLabel.setTextFill(DarkGreen);
                    generalStatusExperimentLabel.setText("Initial connection to module successful");
                }
                try {
                    if(!serialHandler.sendTestParams(testTypeHashMap.get(testTypeComboBox.getSelectionModel().getSelectedItem().toString()))) {
                        generalStatusExperimentLabel.setTextFill(Color.RED);
                        generalStatusExperimentLabel.setText("Module Not Responding, parameter write failed.");
                    }
                    else {
                        generalStatusExperimentLabel.setTextFill(DarkGreen);
                        generalStatusExperimentLabel.setText("Module Configuration Successful, Parameters Have Been Updated");
                    }
                }
                catch (NumberFormatException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Please Fill out Every Field");
                }
                catch (IOException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Error Communicating With Serial Dongle");
                }
                catch (PortInUseException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Serial Port Already In Use");
                }
                catch (UnsupportedCommOperationException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Check Dongle Compatability");
                }

                //Re-enable the write config button when the routine has completed
                applyConfigurationsButton.setDisable(false);
                nextButton.setDisable(false);
            }
        };


        //Assign new operation to a thread so that it can be run in the background
        Thread paramThread = new Thread(sendParamOperation);
        //Start the new thread
        paramThread.start();
    }

    
    /**
     * Fills extra test parameter class fields according to what textfields are shown
     * @param comboBoxIndex Selected ComboBox Index that defines what parameter TextFields are shown for the UI
     */
    @FXML
    private void getExtraParameters(int comboBoxIndex) {
    	generalStatusExperimentLabel.setText("");
        switch (comboBoxIndex){
            case 0:
                //CoM
        		try {
        		    massOfRightModule = Double.parseDouble(massOfRightModuleTextField.getText());
        		    massOfRightGlider = Double.parseDouble(massOfRightGliderTextField.getText());
        		    massOfLeftModule = Double.parseDouble(massOfLeftModuleTextField.getText());
        		    massOfLeftGlider = Double.parseDouble(massOfLeftGliderTextField.getText());
        			
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
        			
        		} catch (NumberFormatException e) {
        			generalStatusExperimentLabel.setTextFill(Color.RED);
        			generalStatusExperimentLabel.setText("Invalid or Missing Data");
        		}
                
                break;
            case 2:
            	//IP
            	// No Extra Parameters Needed
                break;
            case 3:
            	//Pendulum
            	try {
            	    lengthOfPendulum = Double.parseDouble(lengthOfPendulumTextField.getText());
            	    distanceFromPivot = Double.parseDouble(distanceFromPivotTextField.getText());
            	    massOfModule = Double.parseDouble(massOfModuleTextField.getText());
            	    massOfHolder = Double.parseDouble(massOfHolderTextField.getText());

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


        		} catch (NumberFormatException e) {
        			generalStatusExperimentLabel.setTextFill(Color.RED);
        			generalStatusExperimentLabel.setText("Invalid or Missing Data");
        		}
                
                break;
            default:
                break;
        }
    }
    
    
    //TODO: Implement Methods
    @FXML
    private void pairNewRemote(ActionEvent event) {
    	
    }
    
    @FXML
    private void unpairRemotes(ActionEvent event) {
    	
    }
    
    @FXML
    private void testPairedRemote(ActionEvent event) {
    	
    }
    
    @FXML
    private void exitRemoteTestingMode(ActionEvent event) {
    	
    }
    
    @FXML
    private void readTestsFromModule(ActionEvent event) {
    	
    }
    
    @FXML
    private void launchMotionVisualization(ActionEvent event) {
    	//TODO: Implement @ a Later Data
    }
    
    @FXML
    private void eraseTestsFromModule(ActionEvent event) {
    	
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


    public boolean findModuleCommPort() {
        class threadHack{
            private boolean status = false;

            public boolean getStatus(){
                return status;
            }
            public void setStatus(boolean x) {
                status = x;
            }
        }
        final threadHack th = new threadHack();

        Runnable findModuleOperation = new Runnable() {
            public void run() {
                try {
                    ArrayList<String> commPortIDList = serialHandler.findPorts();
                    boolean moduleFound = false;
                    int commPortIndex = 0;
                    while (!moduleFound && commPortIndex < commPortIDList.size()) {

                        //Get the string identifier (name) of the current port
                        String selectedCommID = commPortIDList.toArray()[commPortIndex].toString();

                        //Open the serial port with the selected name, initialize input and output streams, set necessary flags so the whole program know that everything is initialized
                        if(serialHandler.openSerialPort(selectedCommID)){

                            int attemptCounter = 0;
                            while (attemptCounter < 3 && !moduleFound) {
                                try {
                                    ArrayList<Integer> moduleIDInfo = serialHandler.getModuleInfo(NUM_ID_INFO_PARAMETERS);

                                    if (moduleIDInfo != null) {
                                        moduleFound = true;

                                        if (moduleIDInfo.get(2) != CURRENT_FIRMWARE_ID) {
                                            generalStatusExperimentLabel.setTextFill(Color.RED);
                                            generalStatusExperimentLabel.setText("Incompatable Firmware Version: " + moduleIDInfo.get(2) + ", Program Module with Version " + CURRENT_FIRMWARE_ID);
                                        }
                                        else {
                                            generalStatusExperimentLabel.setTextFill(DarkGreen);
                                            generalStatusExperimentLabel.setText("Successfully Connected to Module");
                                        }
                                    }
                                    else {
                                        attemptCounter++;
                                    }
                                }
                                catch (IOException e) {
                                    attemptCounter++;
                                }
                                catch (PortInUseException e) {
                                    attemptCounter++;
                                }
                                catch (UnsupportedCommOperationException e) {
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

                }
                catch (IOException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
                    th.setStatus(false);
                }
                catch (PortInUseException e) {
                    generalStatusExperimentLabel.setTextFill(Color.RED);
                    generalStatusExperimentLabel.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
                    th.setStatus(false);
                }
            }
        };
        Thread findModuleThread = new Thread(findModuleOperation);
        findModuleThread.run();
        return th.getStatus();
    }

}


