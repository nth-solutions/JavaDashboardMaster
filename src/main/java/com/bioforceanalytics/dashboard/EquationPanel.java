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
import javafx.scene.layout.HBox;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Similar to DataSetPanel except that this exclusively holds the checkboxes for CustomAxis
 */
public class EquationPanel extends TitledPane{
    private static final Logger logger = LogManager.getLogger();
    //connects the equations to their checkboxes
    public HashMap equationCheckboxMap = new HashMap<CustomEquation, CheckBox>();
    //connects the checkboxes to the custom axis type that they control
    public HashMap<CheckBox, CustomAxisType> customAxisTypeMap = new HashMap<CheckBox, CustomAxisType>();
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
        // add equation panel
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER);	
        titleBox.setPadding(new Insets(0, 25, 0, 0));
        titleBox.minWidthProperty().bind(this.widthProperty());

        HBox region = new HBox();
        region.setAlignment(Pos.CENTER);
        region.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(region, Priority.ALWAYS);

        // set title of equation panel
        Label label = new Label("Custom Axes");
        label.setId("data-set-title");
        titleBox.getChildren().addAll(label, region);
        this.setGraphic(titleBox);

        Node ref = this;
        Platform.runLater(() -> {
            //addEquation(new CustomEquation("TestName","TestEquation", "TestUnits"));
            for(CustomEquation eq : controller.customEquations){
              //  addEquation(eq);
            }
        });
        
    }
    /**
     * Adds a new equation to the EquationPanel
     * @param eq the equation to add to the panel
     * @param type the CustomAxisType of the equation
     */
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
    /**
     * resets the EquationPanel to no CustomAxes
     */
    public void reset(){
        CustomAxisType.reset();
        equationCheckboxMap.clear();
        checkboxPane.getChildren().clear();
        customAxisTypeMap.clear();
        
    }
    
}
