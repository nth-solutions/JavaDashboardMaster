package com.bioforceanalytics.dashboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import javax.swing.text.html.parser.TagElement;

import java.util.ArrayList;

import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.Map;

public class CustomAxisMenu implements Initializable {

    // left column labelled "Axis Name" with axis type names
    @FXML
    private TableColumn<CustomAxisCell, String> axisNameCol;

    // middle column labelled "Axis Equation" with equation field
    @FXML
    private TableColumn<CustomAxisCell, String> equationCol;

    // right column labelled "Axis Units" with units field
    @FXML
    private TableColumn<CustomAxisCell, String> unitsCol;
    // Node representing the entire table
    @FXML
    private TableView<CustomAxisCell> tableView;

    private static final Logger logger = LogManager.getLogger();

    private GraphNoSINCController controller;

    /**
     * This method runs when the CustomAxisMenu is created
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO Auto-generated method stub
        // don't allow the user to click and select rows
        // tableView.setSelectionModel(null);
        tableView.setEditable(true);
        // center both table columns
        axisNameCol.setStyle("-fx-alignment: CENTER");
        equationCol.setStyle("-fx-alignment: CENTER");
        unitsCol.setStyle("-fx-alignment: CENTER");

        // set the left column to the name of each AxisType
        axisNameCol.setCellValueFactory(new PropertyValueFactory<CustomAxisCell, String>("name"));
        axisNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        axisNameCol.setOnEditCommit(new EventHandler<CellEditEvent<CustomAxisCell, String>>() {
            @Override
            public void handle(CellEditEvent<CustomAxisCell, String> t) {

                ((CustomAxisCell) t.getTableView().getItems().get(t.getTablePosition().getRow()))
                        .setName(t.getNewValue());

            }
        });
        //sets the middle column to be the equation of the custom axis
        equationCol.setCellValueFactory(new PropertyValueFactory<CustomAxisCell, String>("equation"));
        equationCol.setCellFactory(TextFieldTableCell.forTableColumn());
        equationCol.setOnEditCommit(
            new EventHandler<CellEditEvent<CustomAxisCell, String>>() {
                @Override
                public void handle(CellEditEvent<CustomAxisCell, String> t) {
                    
                    ((CustomAxisCell) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setEquation(t.getNewValue());
                    
                }
            }
        );
        //sets the last column to be the units of the custom axis
        unitsCol.setCellValueFactory(new PropertyValueFactory<CustomAxisCell, String>("units"));
        unitsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        unitsCol.setOnEditCommit(
            new EventHandler<CellEditEvent<CustomAxisCell, String>>() {
                @Override
                public void handle(CellEditEvent<CustomAxisCell, String> t) {
                    
                    ((CustomAxisCell) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setUnits(t.getNewValue());
                   
                }
            }
        );
        //
       
        
    }
    /**
     * Allows communication between the equation menu and the DAG's controller.
     * @param controller the DAG controller to pair this equation menu to
     */
    public void setParent(GraphNoSINCController controller) {
        this.controller = controller;
        //we reload the table here because it relies on the controller to send equations to the GraphNoSINCController
        reloadTable();
    }

    /**
     * acts as a reset function for the custom axis menu, allows the table to be reset according to the current equations
     */
    private void reloadTable(){
        if(controller != null){
            //clears the table
            tableView.getItems().clear();
            
            for(CustomAxisCell cell : loadDefaultEquations()){
                //re-adds each cell
                tableView.getItems().add(cell);
            }

        }
        
    }
    /**
     * this method accesses a .json file (savedEquations.json) that contains the equations of all of the default axes
     * @return returns a list of CustomAxisCells that contain the default axes
     */
     private ArrayList<CustomAxisCell> loadDefaultEquations() {
         //first checks if equations have already been loaded, and if so just gets the equations from the controller
        ArrayList<CustomAxisCell> cells = new ArrayList<CustomAxisCell>();
        if(controller.customEquations.size() != 0){
            for(CustomEquation e : controller.customEquations){
                cells.add(new CustomAxisCell(e.getName(), e.getEquation(), e.getUnits()));
            }
        }
        else{
            try {

                // load "saveEquations.json" as an object
                InputStream stream = CustomAxisMenu.class.getResourceAsStream("savedEquations.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                JSONObject obj = (JSONObject) new JSONParser().parse(reader);
                JSONArray arr = (JSONArray) obj.get("Equations");

                Iterator itr = arr.iterator(); 
                while (itr.hasNext())  
                { 
                    JSONObject e = (JSONObject)itr.next();
                    //adds a new equation cell from each unit in the .json file
                    cells.add(new CustomAxisCell((String)e.get("name"),(String)e.get("equation"),(String)e.get("units")));
                }
                
            } catch (Exception e) {

                logger.error("Could not load default equations.");
                e.printStackTrace();

            }
        }
        return cells;
    }
    /**
     * this serves as a way to submit the changes and axes written in this instance of the menu to the graphing window
     */
    @FXML
    private void loadAxes(){
        controller.customEquations.clear();
        for(CustomAxisCell cell : tableView.getItems()){
            controller.customEquations.add(new CustomEquation(cell.getName(), cell.getEquation(), cell.getUnits()));
        }
        controller.loadEquations();
    }
    /**
     * Allows the UI to create a new empty axis cell
     */
    @FXML
    private void addNewAxis(){
        tableView.getItems().add(new CustomAxisCell("Axis Name Here","Equation Here","Units Here"));
        
    }
    /**
     * Resets the table to the default state
     */
    @FXML 
    private void resetAxes(){
        controller.customEquations.clear();
        reloadTable();
           
    }
    /**
     * A helper class that controls the table cells. See the color panel for a more in-depth explanation
     */
    public static class CustomAxisCell{
        private final SimpleStringProperty name;
        private final SimpleStringProperty equation;
        private final SimpleStringProperty units;

        private CustomAxisCell(String name, String equation, String units) {
            this.name = new SimpleStringProperty(name);
            this.equation = new SimpleStringProperty(equation);
            this.units = new SimpleStringProperty(units);
        }
        
        public String getName() {
            return name.get();
        }
 
        public void setName(String name) {
            this.name.set(name);
        }
 
        public String getEquation() {
            return equation.get();
        }
 
        public void setEquation(String equation) {
            this.equation.set(equation);
        }
        public String getUnits() {
            return units.get();
        }
 
        public void setUnits(String units) {
            this.units.set(units);
        }
    }
}

