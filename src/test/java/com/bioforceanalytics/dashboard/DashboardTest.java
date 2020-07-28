package com.bioforceanalytics.dashboard;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.testfx.api.FxAssert;
import org.testfx.matcher.base.WindowMatchers;

import javafx.stage.Stage;

/**
 * Tests the Dashboard Selector's functionality.
 */
public class DashboardTest extends BaseTest {
    
    private DashboardSelector dashboard;

    @Override
    public void start(Stage stage) {

        try {
            dashboard = new DashboardSelector();
            dashboard.start(stage);
        }
        catch (Exception e) {
            fail("Error launching Dashboard");
        }
    }

    @Test
    public void should_launch_educator_mode() {
        clickOn("#EducatorButton");
        FxAssert.verifyThat(window("Education Mode"), WindowMatchers.isShowing());
    }

    @Test
    // TODO Maven throws HeadlessException when running this, but doesn't affect test
    // TODO since this isn't JavaFX, no way to verify if window shows up
    public void should_launch_advanced_mode() {
        clickOn("#AdvancedButton");
    }

}