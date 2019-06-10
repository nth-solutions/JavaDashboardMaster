package dataorganizer;

import com.sun.org.apache.xml.internal.security.Init;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class ColorPaletteController implements Initializable {

    @FXML
    private ColorPicker xAccelColorPicker;
    @FXML
    private ColorPicker yAccelColorPicker;
    @FXML
    private ColorPicker zAccelColorPicker;
    @FXML
    private ColorPicker xGyroColorPicker;
    @FXML
    private ColorPicker yGyroColorPicker;
    @FXML
    private ColorPicker zGyroColorPicker;
    @FXML
    private ColorPicker xMagColorPicker;
    @FXML
    private ColorPicker yMagColorPicker;
    @FXML
    private ColorPicker zMagColorPicker;
    @FXML
    private ColorPicker accelMagColorPicker;

    private Color[] lineColors = new Color[10];

    private GraphController Window;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * ActionEvent that resets all of the ColorPickers within the window to their default color (Color.WHITE)
     * @param event
     */
    @FXML
    private void resetColorPickers(ActionEvent event) {
        xAccelColorPicker.setValue(Color.WHITE);
        yAccelColorPicker.setValue(Color.WHITE);
        zAccelColorPicker.setValue(Color.WHITE);

        xGyroColorPicker.setValue(Color.WHITE);
        yGyroColorPicker.setValue(Color.WHITE);
        zGyroColorPicker.setValue(Color.WHITE);

        xMagColorPicker.setValue(Color.WHITE);
        yMagColorPicker.setValue(Color.WHITE);
        zMagColorPicker.setValue(Color.WHITE);

        accelMagColorPicker.setValue(Color.WHITE);

    }

    /**
     * ActionEvent that appends the values held by each ColorPicker object to the lineColors Array
     * @param event
     */
    @FXML
    private void saveColorPickerColors(ActionEvent event) {
        lineColors[0] = xAccelColorPicker.getValue();
        lineColors[1] = yAccelColorPicker.getValue();
        lineColors[2] = zAccelColorPicker.getValue();

        lineColors[3] = xGyroColorPicker.getValue();
        lineColors[4] = yGyroColorPicker.getValue();
        lineColors[5] = zGyroColorPicker.getValue();

        lineColors[6] = xMagColorPicker.getValue();
        lineColors[7] = yMagColorPicker.getValue();
        lineColors[8] = zMagColorPicker.getValue();

        lineColors[9] = accelMagColorPicker.getValue();

        Window.setLineColors(lineColors);
    }

    public void setGraphControllerObject(GraphController window){
        Window = window;
    }

    /**
     * Gets the array containing Color objects which are used to change line colors in the graph interface
     * @return
     */
    public void setLineColors(GraphController window) {
    }








}
