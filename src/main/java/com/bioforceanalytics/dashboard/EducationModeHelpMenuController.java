package com.bioforceanalytics.dashboard;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class EducationModeHelpMenuController implements Initializable {

    @FXML
    TabPane EraseModuleHelpTabPane;

    @FXML
    TabPane UnpairRemotesHelpTabPane;

    @FXML
    TabPane ExperimentHelpTabPane;

    @FXML
    TabPane SINCTechnologyHelpTabPane;

    @FXML
    TabPane SINCModuleCalibrationTabPane;

    @FXML
    TabPane EducationHelpMenuTabPane;

    @FXML
    Tab eraseModuleHelpTab;

    @FXML
    Tab unpairRemotesHelpTab;

    @FXML
    Tab experimentHelpTab;

    @FXML
    Tab SINCTechnologyHelpTab;

    @FXML
    Tab SINCCalirbationHelpTab;

    @FXML
    Tab experimentHelpTabTwo;

    @FXML
    Tab experimentHelpTabThree;

    @FXML
    Tab experimentHelpTabFour;

    @FXML
    Tab experimentHelpTabFive;

    @FXML
    Tab blankTab;

    public void selectEraseModuleHelpTabOne(){
        EducationHelpMenuTabPane.getSelectionModel().select(eraseModuleHelpTab);
    }
    public void selectUnpairRemotesHelpTab(){
        EducationHelpMenuTabPane.getSelectionModel().select(unpairRemotesHelpTab);
    }
    public void selectExperimentHelpTabOne(){
        EducationHelpMenuTabPane.getSelectionModel().select(experimentHelpTab);
    }

    public void selectExperimentHelpTabTwo(){
        EducationHelpMenuTabPane.getSelectionModel().select(experimentHelpTabTwo);
    }

    public void selectExperimentHelpTabThree(){
        EducationHelpMenuTabPane.getSelectionModel().select(experimentHelpTabThree);
    }

    public void selectExperimentHelpTabFour(){
        EducationHelpMenuTabPane.getSelectionModel().select(experimentHelpTabFour);
    }

    public void selectExperimentHelpTabFive(){
        EducationHelpMenuTabPane.getSelectionModel().select(experimentHelpTabFive);
    }

    public void selectSINCTechnologyHelpTab(){
        EducationHelpMenuTabPane.getSelectionModel().select(SINCTechnologyHelpTab);
    }
    // FIXME typo in FXML field name
    public void selectSINCModuleCalibrationTab(){
        EducationHelpMenuTabPane.getSelectionModel().select(SINCCalirbationHelpTab);
    }
    public void selectBlankTab(){
        EducationHelpMenuTabPane.getSelectionModel().select(blankTab);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

}
