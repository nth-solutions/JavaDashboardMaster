package dataorganizer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class EducationModeHelpMenuController implements Initializable {

    @FXML
    TabPane EraseModuleHelpTabPane;

    @FXML
    TabPane UnpairRemotesHelpTabPane;

    @FXML
    TabPane ExperientHelpTabPane;

    @FXML
    TabPane SINCTechnologyHelpTabPane;

    @FXML
    TabPane SINCModuleCalibrationTabPane;

    @FXML
    Tab EraseModuleHelpTabOne;

    @FXML
    Tab UnpairRemotesHelpTabOne;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void selectEraseModuelHelpTabOne(){
        EraseModuleHelpTabPane.getSelectionModel().select(0);
    }

}
