package com.bioforceanalytics.dashboard;

import org.testfx.framework.junit.ApplicationTest;

/**
 * Initializes headless instances. Ensure all GUI test classes inherit from this class.
 */
public class GUITest extends ApplicationTest {
    static {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("java.awt.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
    }
}