package com.bioforceanalytics.dashboard;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javafx.stage.Stage;

public class DashboardTest extends ApplicationTest {
    
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
    }

    @Test
    public void should_launch_advanced_mode() {
        clickOn("#AdvancedButton");
    }

}