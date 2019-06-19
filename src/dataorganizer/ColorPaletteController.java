package dataorganizer;

import com.sun.corba.se.impl.orbutil.graph.Graph;
import com.sun.org.apache.xml.internal.security.Init;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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



    public Color[] lineColors = new Color[10];

    private GraphController Window;

    public static String xAccelColor;
    public static String yAccelColor;
    public static String zAccelColor;

    public static String xGyroColor;
    public static String yGyroColor;
    public static String zGyroColor;

    public static String xMagColor;
    public static String yMagColor;
    public static String zMagColor;

    public static String accelMagColor;

    /**
     * Sets default values for the colorPicker.
     * @param location
     * @param resources
     */

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xAccelColorPicker.setValue(Color.RED);
        yAccelColorPicker.setValue(Color.DODGERBLUE);
        zAccelColorPicker.setValue(Color.FORESTGREEN);

        xGyroColorPicker.setValue(Color.GOLD);
        yGyroColorPicker.setValue(Color.CORAL);
        zGyroColorPicker.setValue(Color.MEDIUMBLUE);

        xMagColorPicker.setValue(Color.DARKVIOLET);
        yMagColorPicker.setValue(Color.DARKSLATEGRAY);
        zMagColorPicker.setValue(Color.SADDLEBROWN);

        accelMagColorPicker.setValue(Color.BLACK);
    }

    /**
     * Closes the Window
     */
    private void closeWindow(){
       Stage stage = (Stage) xAccelColorPicker.getScene().getWindow();
       stage.close();
    }

    /**
     * ActionEvent that resets all of the ColorPickers within the window to their default color.
     * @param event
     */
    @FXML
    private void resetColorPickers(ActionEvent event) {
        xAccelColorPicker.setValue(Color.RED);
        yAccelColorPicker.setValue(Color.DODGERBLUE);
        zAccelColorPicker.setValue(Color.FORESTGREEN);

        xGyroColorPicker.setValue(Color.GOLD);
        yGyroColorPicker.setValue(Color.CORAL);
        zGyroColorPicker.setValue(Color.MEDIUMBLUE);

        xMagColorPicker.setValue(Color.DARKVIOLET);
        yMagColorPicker.setValue(Color.DARKSLATEGRAY);
        zMagColorPicker.setValue(Color.SADDLEBROWN);

        accelMagColorPicker.setValue(Color.BLACK);

    }

    /**
     * After the User clicks the save Button, the values of each colorPicker are saved to variables that are used to actually set the colors in the graph.
     * @param event
     */
    @FXML
    public void saveColorPickerColors(ActionEvent event) {
//        lineColors[0] = xAccelColorPicker.getValue();
//        lineColors[1] = yAccelColorPicker.getValue();
//        lineColors[2] = zAccelColorPicker.getValue();
//
//        lineColors[3] = xGyroColorPicker.getValue();
//        lineColors[4] = yGyroColorPicker.getValue();
//        lineColors[5] = zGyroColorPicker.getValue();
//
//        lineColors[6] = xMagColorPicker.getValue();
//        lineColors[7] = yMagColorPicker.getValue();
//        lineColors[8] = zMagColorPicker.getValue();
//
//        lineColors[9] = accelMagColorPicker.getValue();

        xAccelColor = xAccelColorPicker.getValue().toString();
        yAccelColor = yAccelColorPicker.getValue().toString();
        zAccelColor = zAccelColorPicker.getValue().toString();

        xGyroColor = xGyroColorPicker.getValue().toString();
        yGyroColor = yGyroColorPicker.getValue().toString();
        zGyroColor = zGyroColorPicker.getValue().toString();

        xMagColor = xMagColorPicker.getValue().toString();
        yMagColor = yMagColorPicker.getValue().toString();
        zMagColor = zMagColorPicker.getValue().toString();

        accelMagColor = accelMagColorPicker.getValue().toString();

        //Window.restyleSeries();
        closeWindow();
      //  Window.setLineColors(lineColors);

    }

    public void setGraphControllerObject(GraphController window){
        Window = window;
    }

    /**
     * Gets the array containing Color objects which are used to change line colors in the graph interface
     * @return
     */
}
