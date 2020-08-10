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

    // TODO
    // The following tests require a module to be plugged in to apply SINC configuration.
    // This means these wouldn't work in CI for GitHub Actions; I don't have the heart to delete these after writing them,
    // so maybe if we ever figure out how to get these to work, we could rework this? A man can hope
    //====================================================================================================================
    // @Test
    // public void check_sinc_calibration_real_test_gui() {

    //     // navigate to "SINC Module Calibration"
    //     clickOn("#sincCalibrationButton");

    //     // fill in video file path textbox
    //     String videoFile = new File(getClass().getResource("sinc-test-real.mp4").getFile()).getPath();
    //     TextField videoInput = lookup("#videoFilePathTextField").query();
    //     videoInput.setText(videoFile);

    //     // click "Apply SINC Configurations to Module"
    //     clickOn("#importCalibrationDataButton11");

    //     try {
    //         WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
    //             @Override
    //             public Boolean call() {
    //                 Label l = (Label) lookup("#sincCalibrationTabGeneralStatusLabel").query();
    //                 return !l.getText().equals("Calibrating module...");
    //             }
    //         });
    //     }
    //     catch (TimeoutException e) {
    //         fail("SINC Calibration process timed out.");
    //     }

    //     FxAssert.verifyThat("#sincCalibrationTabGeneralStatusLabel", LabeledMatchers.hasText("Successfully calibrated module (camera and module synced)"));

    // }

    // @Test
    // public void check_sinc_calibration_ideal_test_gui() {

    //     // navigate to "SINC Module Calibration"
    //     clickOn("#sincCalibrationButton");

    //     // fill in video file path textbox
    //     String videoFile = new File(getClass().getResource("sinc-test-ideal.mp4").getFile()).getPath();
    //     TextField videoInput = lookup("#videoFilePathTextField").query();
    //     videoInput.setText(videoFile);

    //     // click "Apply SINC Configurations to Module"
    //     clickOn("#importCalibrationDataButton11");

    //     try {
    //         WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
    //             @Override
    //             public Boolean call() {
    //                 Label l = (Label) lookup("#sincCalibrationTabGeneralStatusLabel").query();
    //                 return !l.getText().equals("Calibrating module...");
    //             }
    //         });
    //     }
    //     catch (TimeoutException e) {
    //         fail("SINC Calibration process timed out.");
    //     }

    //     FxAssert.verifyThat("#sincCalibrationTabGeneralStatusLabel", LabeledMatchers.hasText("Successfully calibrated module (camera and module synced)"));

    // }

}