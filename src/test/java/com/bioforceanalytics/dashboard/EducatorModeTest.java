package com.bioforceanalytics.dashboard;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Ignore;
import org.junit.Test;
import org.testfx.api.FxAssert;
import org.testfx.matcher.base.WindowMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Tests the Educator Dashboard's functionality.
 */
public class EducatorModeTest extends GUITest {
    
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
        stage.setTitle("BioForce Education Mode");
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

    // TODO
    // The following tests require a module to be plugged in to apply SINC configuration.
    // This has problems since other tests establish connections to the module as well.
    // In addition, these wouldn't work in CI for GitHub Actions; I don't have the heart to delete these after writing them,
    // so maybe if we ever figure out how to get these to work, we could rework this? A man can hope
    //====================================================================================================================

    @Ignore("Requires a module to be plugged in, and other tests use the serial port")
    @Test
    public void check_sinc_calibration_real_test_gui() {

        // wait for connection process to complete
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    Label l = (Label) lookup("#generalStatusExperimentLabel").query();
                    return !l.getText().equals("Connecting to module...");
                }
            });
        }
        catch (TimeoutException e) {
            fail("Connection process timed out.");
        }

        FxAssert.verifyThat("#generalStatusExperimentLabel", LabeledMatchers.hasText("Successfully connected to module"));

        // navigate to "SINC Module Calibration"
        clickOn("#sincCalibrationButton");

        // fill in video file path textbox
        String videoFile = new File(getClass().getResource("sinc-test-real.mp4").getFile()).getPath();
        TextField videoInput = lookup("#videoFilePathTextField").query();
        videoInput.setText(videoFile);

        // click "Apply SINC Configurations to Module"
        clickOn("#importCalibrationDataButton11");

        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    Label l = (Label) lookup("#sincCalibrationTabGeneralStatusLabel").query();
                    return !l.getText().equals("Calibrating module...");
                }
            });
        }
        catch (TimeoutException e) {
            fail("SINC Calibration process timed out.");
        }

        FxAssert.verifyThat("#sincCalibrationTabGeneralStatusLabel", LabeledMatchers.hasText("Successfully calibrated module (camera and module synced)"));

    }

    @Ignore("Requires a module to be plugged in, and other tests use the serial port")
    @Test
    public void check_sinc_calibration_ideal_test_gui() {

        // wait for connection process to complete
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    Label l = (Label) lookup("#generalStatusExperimentLabel").query();
                    return !l.getText().equals("Connecting to module...");
                }
            });
        }
        catch (TimeoutException e) {
            fail("Connection process timed out.");
        }

        FxAssert.verifyThat("#generalStatusExperimentLabel", LabeledMatchers.hasText("Successfully connected to module"));

        // navigate to "SINC Module Calibration"
        clickOn("#sincCalibrationButton");

        // fill in video file path textbox
        String videoFile = new File(getClass().getResource("sinc-test-ideal.mp4").getFile()).getPath();
        TextField videoInput = lookup("#videoFilePathTextField").query();
        videoInput.setText(videoFile);

        // click "Apply SINC Configurations to Module"
        clickOn("#importCalibrationDataButton11");

        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    Label l = (Label) lookup("#sincCalibrationTabGeneralStatusLabel").query();
                    return !l.getText().equals("Calibrating module...");
                }
            });
        }
        catch (TimeoutException e) {
            fail("SINC Calibration process timed out.");
        }

        FxAssert.verifyThat("#sincCalibrationTabGeneralStatusLabel", LabeledMatchers.hasText("Successfully calibrated module (camera and module synced)"));

    }

}