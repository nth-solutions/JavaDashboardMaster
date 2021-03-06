package com.bioforceanalytics.dashboard;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.ArrayList;

import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class BFAColorMenu implements Initializable {

    // keeps track of AxisTypes and their matching colors.
    private static HashMap<Axis, Color> colorMap = new HashMap<Axis, Color>();

    // reference to parent GraphNoSINCController
    private GraphNoSINCController controller;

    private static ArrayList<Color> customAxisColors = new ArrayList<Color>();

    // left column labelled "Axis Type" with axis type names
    @FXML
    private TableColumn<Axis, String> axisTypeCol;

    // right column labelled "Color" with color pickers
    @FXML
    private TableColumn<Axis, ColorPicker> colorCol;

    // Node representing the entire table
    @FXML
    private TableView<Axis> tableView;

    private static final Logger logger = LogController.start();

    static {
        BFAColorMenu.loadColorsFromJSON();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // don't allow the user to click and select rows
        tableView.setSelectionModel(null);

        // center both table columns
        axisTypeCol.setStyle("-fx-alignment: CENTER");
        colorCol.setStyle("-fx-alignment: CENTER");

        // set the left column to the name of each AxisType
        axisTypeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().toString()));

        // set the right column to the custom color picker cell
        colorCol.setCellFactory(column -> new ColorPickerCell());

        // colorCol.setCellValueFactory(data -> new ColorPicker());

        // populate table with AxisType entries
        for (AxisType a : AxisType.values()) {
            tableView.getItems().add(a);
        }
        for (int i = 0; i < 10; i++) {
            if (CustomAxisType.getCustomAxisByIndex(i) != null) {
                tableView.getItems().add(CustomAxisType.getCustomAxisByIndex(i));
            }
        }

    }

    /**
     * Custom table cell that contains a color picker.
     */
    private class ColorPickerCell extends TableCell<Axis, ColorPicker> {

        final ColorPicker picker = new ColorPicker();

        @Override
        // this method handles rendering color pickers
        protected void updateItem(ColorPicker item, boolean empty) {

            // necessary for this method
            super.updateItem(item, empty);

            // if cell is not empty, add color picker
            if (!empty) {

                // get index and AxisType of this color picker
                int rowIndex = getTableRow().getIndex();

                final Axis a;
                Axis temp = null;
                for (Axis axis : colorMap.keySet()) {
                    if (axis != null) {
                        if ((axis.isCustomAxis() && axis.getIndex() + AxisType.values().length == rowIndex)
                                || (!axis.isCustomAxis() && axis.getIndex() == rowIndex)) {
                            temp = axis;
                            if (axis.isCustomAxis()) {
                                logger.info("Custom axis" + axis + ", " + rowIndex + ", " + axis.getIndex());
                            }
                        }
                    }
                }
                a = temp;

                // set color picker's value based on color map

                picker.setValue(colorMap.get(a));

                // when color picker changes
                picker.setOnAction(e -> {

                    // update color map with the newly selected color
                    colorMap.replace(a, picker.getValue());

                    logger.info("Updated " + a + "'s color to " + picker.getValue());
                });

                // render color picker
                setGraphic(picker);
            }
            // if empty, don't render anything
            else
                setGraphic(null);

        }

    }

    private static void addCustomAxisColor(CustomAxisType axis) {
        if (customAxisColors.size() > 0 && CustomAxisType.allCustomAxes.size() <= 10) { // 10 is max number of default

            colorMap.put(axis, customAxisColors.get(CustomAxisType.allCustomAxes.indexOf(axis)));
        } else {
            Random rand = new Random();
            colorMap.put(axis, Color.rgb((int) rand.nextDouble() * 255, (int) rand.nextDouble() * 255,
                    (int) rand.nextDouble() * 255));
        }

    }

    /**
     * Returns the color associated with an AxisType.
     * 
     * @param axis the enum representing the data set
     * @return the color associated with an AxisType
     */
    public static Color getColor(Axis axis) {
        if (!colorMap.containsKey(axis)) {
            addCustomAxisColor((CustomAxisType) axis);
        }
        return colorMap.get(axis);

    }

    /**
     * Returns the color associated with an AxisType, formatted as a hexadecimal
     * color code.
     * 
     * @param axis the enum representing the data set
     * @return the color associated with an AxisType
     */
    public static String getHexString(Axis axis) {
        Color c;

        c = getColor(axis);

        return String.format("#%02X%02X%02X", (int) (c.getRed() * 255), (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));

    }

    /**
     * Allows communication between the color menu and the DAG's controller.
     * 
     * @param controller the DAG controller to pair this color menu to
     */
    public void setParent(GraphNoSINCController controller) {
        this.controller = controller;
    }

    // loads default colors from "defaultGraphColors.json"
    private static void loadColorsFromJSON() {

        try {

            // load "defaultGraphColors.json" as an object
            InputStream stream = BFAColorMenu.class.getResourceAsStream("defaultGraphColors.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            JSONObject obj = (JSONObject) new JSONParser().parse(reader);

            // loop through AxisTypes to populate color map
            for (AxisType a : AxisType.values()) {

                // read color hexcode from JSON
                String colorString = (String) obj.get(a.toString());

                // convert hexcode to Color object
                Color c = Color.web(colorString);

                // add color to map
                colorMap.put(a, c);
            }
            for (int i = 0; i < 10; i++) {
                String colorString = (String) obj.get("CustomColor" + i);

                Color c = Color.web(colorString);

                customAxisColors.add(c);
            }
            logger.info("Loaded 'defaultGraphColors.json' into color map.");

        } catch (Exception e) {

            logger.error("Could not load default graph color configuration.");
            e.printStackTrace();

            // fill color map with black colors so graph still functions
            for (AxisType a : AxisType.values()) {
                colorMap.put(a, Color.BLACK);
            }
        }
    }

    @FXML
    // TODO make this save "customGraphColors.json" to disk
    private void updateGraphColors() {

        logger.info("Updating graph colors...");
        controller.updateGraphColors();

        // close the color menu
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.close();

    }

    @FXML
    private void resetGraphColors() {

        logger.info("Resetting graph colors...");

        // reload default color scheme
        BFAColorMenu.loadColorsFromJSON();

        // re-render table with new colors
        tableView.refresh();

    }

}