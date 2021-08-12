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
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.Map; 
public class VariableMenu implements Initializable{

    // left column labelled "Axis Name" with axis type names
    @FXML private TableColumn<VariableCell,String> variableNameCol;

    // middle column labelled "Axis Equation" with equation field
    @FXML private TableColumn<VariableCell,String> valueCol;
    // Node representing the entire table
    @FXML private TableView<VariableCell> tableView;

    private static final Logger logger = LogManager.getLogger();

    private GraphNoSINCController controller;

    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO Auto-generated method stub
        // don't allow the user to click and select rows
        //tableView.setSelectionModel(null);
        tableView.setEditable(true);
        Callback<TableColumn<VariableCell, String>, TableCell<VariableCell, String>> cellFactory =
        new Callback<TableColumn<VariableCell, String>, TableCell<VariableCell, String>>() {
            public TableCell call(TableColumn p) {
               return EditCell.createStringEditCell(); 
            }
        };
        // center both table columns
        variableNameCol.setStyle("-fx-alignment: CENTER");
        
        valueCol.setStyle("-fx-alignment: CENTER");

        // set the left column to the name of each AxisType
        variableNameCol.setCellValueFactory(new PropertyValueFactory<VariableCell, String>("name"));
        variableNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        variableNameCol.setCellFactory(cellFactory);
        variableNameCol.setOnEditCommit(
            new EventHandler<CellEditEvent<VariableCell, String>>() {
                @Override
                public void handle(CellEditEvent<VariableCell, String> t) {
                    
                    ((VariableCell) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setName(t.getNewValue());
                    
                }
            }
        );
      
        valueCol.setCellValueFactory(new PropertyValueFactory<VariableCell, String>("value"));
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setCellFactory(cellFactory);
        valueCol.setOnEditCommit(
            new EventHandler<CellEditEvent<VariableCell, String>>() {
                @Override
                public void handle(CellEditEvent<VariableCell, String> t) {
                    
                    ((VariableCell) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setValue(t.getNewValue());
                    
                }
            }
        );
        
        if(controller != null){
            reloadTable();
        }
       
        
    }
    /**
     * Allows communication between the equation menu and the DAG's controller.
     * @param controller the DAG controller to pair this equation menu to
     */
    public void setParent(GraphNoSINCController controller) {
        this.controller = controller;
        reloadTable();
        //we reload the table here because it relies on the controller to send equations to the GraphNoSINCController
    }

    private void reloadTable(){
        for(Variable v : controller.variables){
            tableView.getItems().add(new VariableCell(v.getName(), Double.toString(v.getValue())));
        }
    }

    @FXML
    private void saveChanges(){
        controller.variables.clear();
        for(VariableCell vc : tableView.getItems()){
            controller.variables.add(new Variable(vc.getName(), Double.parseDouble(vc.getValue())));
        }
    }

    @FXML
    private void addVariable(){
        tableView.getItems().add(new VariableCell("New Variable","0"));
        
    }

    @FXML 
    private void resetVariables(){
       controller.variables.clear();
       tableView.getItems().clear();
    }

    public static class VariableCell{
        private final SimpleStringProperty name;
        private final SimpleStringProperty value;

        private VariableCell(String name, String value) {
            this.name = new SimpleStringProperty(name);
            this.value = new SimpleStringProperty(value);
            
        }
        
        public String getName() {
            return name.get();
        }
 
        public void setName(String name) {
            this.name.set(name);
        }
 
        public String getValue() {
            return value.get();
        }
 
        public void setValue(String value) {
            this.value.set(value);
        }
    }
}

