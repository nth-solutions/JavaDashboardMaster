package com.bioforceanalytics.dashboard;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.UIManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DashboardSelectorController implements Initializable {

    @FXML
    private EducatorModeControllerFX educator;

    @FXML
    Button EducatorButton;

    private Image icon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        icon = new Image(getClass().getResource("images/bfa.png").toExternalForm());
    }

    private void closeWindow() {
        Stage stage = (Stage) EducatorButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void launchAdvanced() {
        closeWindow();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
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


    public EducatorModeControllerFX startEducator() {

        closeWindow();

        Stage primaryStage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/EducatorModeFXML.fxml"));

        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading Educator Mode");
        }

        Scene scene = new Scene(root, 690,500);

        primaryStage.setTitle("Education Mode");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(icon);
        primaryStage.show();
        primaryStage.setResizable(false);
        return loader.getController();
    }
}
