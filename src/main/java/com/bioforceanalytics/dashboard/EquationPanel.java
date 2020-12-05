package com.bioforceanalytics.dashboard;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EquationPanel extends TitledPane{
    private static final Logger logger = LogManager.getLogger();

    public HashMap equationCheckboxMap = new HashMap<CustomEquation, CheckBox>();
    public HashMap customAxisTypeMap = new HashMap<CheckBox, CustomAxisType>();
    GraphNoSINCController controller;

    @FXML
    GridPane checkboxPane;
    public EquationPanel(GraphNoSINCController controller){
        this.controller = controller;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/EquationPanel.fxml"));
		loader.setRoot(this);
        loader.setController(this);
        
        try {
			loader.load();
		}
		catch (Exception e) {
            logger.error("Error loading Equation Panel JavaFX component");
            e.printStackTrace();
        }
        Node ref = this;
        Platform.runLater(() -> {
            //addEquation(new CustomEquation("TestName","TestEquation", "TestUnits"));
            for(CustomEquation eq : controller.customEquations){
              //  addEquation(eq);
            }
        });
        
    }
    public void addEquation(CustomEquation eq, CustomAxisType type){
        CheckBox checkbox = new CheckBox(eq.getName()); 
        checkbox.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				controller.graphAxis((CustomAxisType)customAxisTypeMap.get(checkbox),-1);
				
			} 
            
        });

        checkboxPane.add(checkbox, 0, equationCheckboxMap.size());
        equationCheckboxMap.put(eq, checkbox);
        customAxisTypeMap.put(checkbox, type);
        
    }

    public void reset(){
        equationCheckboxMap.clear();
        checkboxPane.getChildren().clear();
        customAxisTypeMap.clear();
        
    }
    
}
