package com.bioforceanalytics.dashboard;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;

public class CombinedCustomMenu implements Initializable {
    private GraphNoSINCController parent;
    @FXML
    CustomAxisMenu equationPageController;

    @FXML
    VariableMenu variablesPageController;

    @FXML
    Button closeHelpMenu;

    @FXML
    AnchorPane masterPane;

    @FXML
    HBox hBox;

    @FXML
    VBox helpPane;

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setParent(GraphNoSINCController controller) {
        parent = controller;
        equationPageController.setParent(parent);
        variablesPageController.setParent(parent);
    }

    public GraphNoSINCController getParent() {
        return parent;

    }

    @FXML
    public void closeHelpMenu() {
        logger.info("Closing help menu");
        hBox.getChildren().remove(helpPane);
        masterPane.setPrefWidth(700);
        masterPane.setMaxWidth(700);
        masterPane.setMinWidth(700);
        masterPane.getScene().getWindow().setWidth(715);

    }
}
