package com.bioforceanalytics.dashboard;

import java.util.ResourceBundle;

import javafx.application.Platform;
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
    Button guideToggle;

    boolean showGuide = false;

    @FXML
    HBox hBox;

    @FXML
    VBox helpPane;

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            closeHelpMenu();
        });
    }

    public void setParent(GraphNoSINCController controller) {
        parent = controller;
        equationPageController.setParent(parent);
        variablesPageController.setParent(parent);
    }

    public GraphNoSINCController getParent() {
        return parent;

    }

    public void toggleGuide() {
        if (showGuide) {
            showGuide = false;
            closeHelpMenu();
            guideToggle.setText("Open Guide ");
        } else {
            showGuide = true;
            openHelpMenu();
            guideToggle.setText("Close Guide");
        }
    }

    public void closeHelpMenu() {
        logger.info("Closing help menu");
        hBox.getChildren().remove(helpPane);
        masterPane.setPrefWidth(700);
        masterPane.setMaxWidth(700);
        masterPane.setMinWidth(700);
        masterPane.getScene().getWindow().setWidth(715);

    }

    public void openHelpMenu() {
        logger.info("Opening help menu");
        hBox.getChildren().add(helpPane);
        masterPane.setPrefWidth(1000);
        masterPane.setMaxWidth(1000);
        masterPane.setMinWidth(1000);
        masterPane.getScene().getWindow().setWidth(1015);

    }
}
