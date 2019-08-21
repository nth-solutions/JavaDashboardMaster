package dataorganizer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.swing.*;
import java.io.IOException;
import java.util.ResourceBundle;
import java.net.URL;
import javafx.scene.control.*;

public class DashboardSelectorController implements Initializable {

    @FXML
    private EducatorModeControllerFX educator;

    @FXML
    Button EducatorButton;

    @Override
    public void initialize(URL location, ResourceBundle resources){
    }

    private void closeWindow(){
        Stage stage = (Stage) EducatorButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void launchAdvanced(){

        closeWindow();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e) {
            System.out.println("Error Setting Look and Feel: " + e);
        }
        Runnable frameRunner = new Runnable() {
            public void run() {
                try {
                    AdvancedMode frame = new AdvancedMode();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread frameThread = new Thread(frameRunner);
        frameThread.run();

    }

    @FXML
    public void launchEducator(){
        educator = startEducator();

    }


    public EducatorModeControllerFX startEducator(){
        closeWindow();

        Stage primaryStage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EducatorModeFXML.fxml"));
        try {
            root = loader.load();
            //root.getStylesheets().add(getClass().getResource("EducatorModeCSS.css").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root, 690,500);

        primaryStage.setTitle("Educator Mode");
        primaryStage.setScene(scene);
        scene.getStylesheets().clear();
        System.out.println(this.getClass().getResource("EducatorModeCSS.css").toExternalForm());
        System.out.println(this.getClass().getResource("EducatorModeCSS.css").toExternalForm());
        scene.getStylesheets().add(this.getClass().getResource("EducatorModeCSS.css").toString());

        primaryStage.show();
        primaryStage.setResizable(false);
        return loader.getController();
    }
}
