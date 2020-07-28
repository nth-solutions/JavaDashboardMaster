package com.bioforceanalytics.dashboard;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.testfx.api.FxAssert;
import org.testfx.matcher.base.WindowMatchers;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Tests the Educator Dashboard's functionality.
 */
public class EducatorModeTest extends BaseTest {
    
    private EducatorModeControllerFX emfx;

    @Override
    public void start(Stage stage) {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/EducatorModeFXML.fxml"));

        Parent root = null;

        try {
            root = loader.load();   
        }
        catch (Exception e) {
            fail("Error launching Educator Mode");
        }

        emfx = loader.getController();
        stage.setTitle("Education Mode");
        Scene scene = new Scene(root, 700, 500);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("css/EducatorModeCSS.css").toExternalForm());
        stage.show();
        stage.setResizable(true);
    }

    @Test
    public void should_launch_data_analysis_graph() {
        Platform.runLater(() -> {
            emfx.startGraphingNoSINC();
            FxAssert.verifyThat(window("BioForce Data Analysis Graph"), WindowMatchers.isShowing());
        });
    }

    @Test
    public void should_launch_sinc_graph() {
        Platform.runLater(() -> {
            emfx.startGraphing();
            FxAssert.verifyThat(window("BioForce SINC Technology Graph"), WindowMatchers.isShowing());
        });
    }

}