package com.bioforceanalytics.dashboard;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class EducationModeHelpMenuController implements Initializable {

    @FXML
    TabPane educationHelpMenuTabPane;

    @FXML
    Tab eraseModuleHelpTab;

    @FXML
    Tab unpairRemotesHelpTab;

    @FXML
    Tab experimentHelpTab;

    @FXML
    Tab SINCTechnologyHelpTab;

    @FXML
    Tab SINCCalibrationHelpTab;

    @FXML
    Tab experimentHelpTabTwo;

    @FXML
    Tab experimentHelpTabThree;

    @FXML
    Tab experimentHelpTabFour;

    @FXML
    Tab blankTab;

    @FXML
    Label debugInfo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        debugInfo.setText("Version: " + Settings.getVersion() + " | Build Date: " + Settings.getBuildDate());
    }

    @FXML
    public void openLogsFolder() {
        try {
            Desktop d = Desktop.getDesktop();
            d.open(new File(LogController.getLogPath()));
        } catch (IOException e) {
            Alert a = new Alert(AlertType.ERROR); 
            a.setContentText("Error opening logs folder.");
            a.showAndWait();
        }
    }

    public void selectEraseModuleHelpTabOne() {
        educationHelpMenuTabPane.getSelectionModel().select(eraseModuleHelpTab);
    }
    public void selectUnpairRemotesHelpTab() {
        educationHelpMenuTabPane.getSelectionModel().select(unpairRemotesHelpTab);
    }
    public void selectExperimentHelpTab(int index) {
        educationHelpMenuTabPane.getSelectionModel().select(index);
    }
    public void selectSINCTechnologyHelpTab() {
        educationHelpMenuTabPane.getSelectionModel().select(SINCTechnologyHelpTab);
    }
    public void selectSINCModuleCalibrationTab() {
        educationHelpMenuTabPane.getSelectionModel().select(SINCCalibrationHelpTab);
    }
    public void selectBlankTab() {
        educationHelpMenuTabPane.getSelectionModel().select(blankTab);
    }

}
