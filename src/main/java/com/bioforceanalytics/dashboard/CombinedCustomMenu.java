package com.bioforceanalytics.dashboard;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;

public class CombinedCustomMenu implements Initializable{
    private GraphNoSINCController parent;
    @FXML
    CustomAxisMenu equationPageController;

    @FXML
    VariableMenu variablesPageController;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }

    public void setParent(GraphNoSINCController controller){
        parent = controller;
        equationPageController.setParent(parent);
        variablesPageController.setParent(parent);
    }
    
    public GraphNoSINCController getParent(){
        return parent;
    }
}
