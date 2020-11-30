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
import java.util.Map; 
public class CustomAxisMenu implements Initializable{

    // left column labelled "Axis Name" with axis type names
    @FXML private TableColumn<CustomAxisCell,String> axisNameCol;

    // middle column labelled "Axis Equation" with equation field
    @FXML private TableColumn<CustomAxisCell,String> equationCol;

    // right column labelled "Axis Units" with units field
    @FXML private TableColumn<CustomAxisCell,String> unitsCol;
    // Node representing the entire table
    @FXML private TableView<CustomAxisCell> tableView;

    private static final Logger logger = LogManager.getLogger();

    private GraphNoSINCController controller;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO Auto-generated method stub
        // don't allow the user to click and select rows
        //tableView.setSelectionModel(null);
        tableView.setEditable(true);
        // center both table columns
        axisNameCol.setStyle("-fx-alignment: CENTER");
        equationCol.setStyle("-fx-alignment: CENTER");
        unitsCol.setStyle("-fx-alignment: CENTER");

        // set the left column to the name of each AxisType
        axisNameCol.setCellValueFactory(new PropertyValueFactory<CustomAxisCell, String>("name"));
        axisNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        axisNameCol.setOnEditCommit(
            new EventHandler<CellEditEvent<CustomAxisCell, String>>() {
                @Override
                public void handle(CellEditEvent<CustomAxisCell, String> t) {
                    ((CustomAxisCell) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setName(t.getNewValue());
                }
            }
        );
       
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
       
        reloadTable();
    }
    /**
     * Allows communication between the color menu and the DAG's controller.
     * @param controller the DAG controller to pair this color menu to
     */
    public void setParent(GraphNoSINCController controller) {
        this.controller = controller;
        //we reload the table here because it relies on the controller to send equations to the GraphNoSINCController
        reloadTable();
    }

    private void reloadTable(){
        if(controller != null){
            tableView.getItems().clear();
            controller.customEquations.clear();
            for(CustomAxisCell cell : loadDefaultEquations()){
                tableView.getItems().add(cell);
                controller.customEquations.add(new CustomEquation(cell.getName(), cell.getEquation(), cell.getUnits()));
            }
            logger.info(controller.customEquations);
        }
        
    }
     private static ArrayList<CustomAxisCell> loadDefaultEquations() {
        ArrayList<CustomAxisCell> cells = new ArrayList<CustomAxisCell>();
        try {

            // load "defaultGraphColors.json" as an object
            InputStream stream = CustomAxisMenu.class.getResourceAsStream("savedEquations.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            JSONObject obj = (JSONObject) new JSONParser().parse(reader);
            JSONArray arr = (JSONArray) obj.get("Equations");

            Iterator itr = arr.iterator(); 
            while (itr.hasNext())  
            { 
                JSONObject e = (JSONObject)itr.next();
                cells.add(new CustomAxisCell((String)e.get("name"),(String)e.get("equation"),(String)e.get("units")));
            }
            
        } catch (Exception e) {

            logger.error("Could not load default equations.");
            e.printStackTrace();

        }
        return cells;
    }
    @FXML
    private void saveAxes(){
        try {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray(); 
        File output = new File(this.getClass().getResource("savedEquations.json").getPath());
        PrintWriter writer = new PrintWriter(output);
        for(CustomAxisCell cell : tableView.getItems()){
            Map m = new LinkedHashMap(3); 
            m.put("name", cell.getName()); 
            m.put("equation", cell.getEquation()); 
            m.put("units",cell.getUnits());
            ja.add(m);
        }
        jo.put("Equations",ja);
        writer.write(jo.toJSONString());
        
        writer.flush();
        writer.close();
        }
        catch (Exception e){
            logger.error("Could not save axes.");
            e.printStackTrace();
        }
    }

    @FXML
    private void addNewAxis(){
        tableView.getItems().add(new CustomAxisCell("New Custom Axis","",""));
    }

    @FXML 
    private void resetAxes(){
        try{
            File input = new File(this.getClass().getResource("defaultEquations.json").getPath());
            File output = new File(this.getClass().getResource("savedEquations.json").getPath());
            PrintWriter writer = new PrintWriter(output);
            
            InputStream stream = CustomAxisMenu.class.getResourceAsStream("defaultEquations.json");
            FileChannel src = new FileInputStream(input).getChannel();
            FileChannel dest = new FileOutputStream(output).getChannel();
            dest.transferFrom(src, 0, src.size());
           reloadTable();
        }
        catch(Exception e){
            logger.error("Could not revert axes.");
            e.printStackTrace();
        }
    }

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

