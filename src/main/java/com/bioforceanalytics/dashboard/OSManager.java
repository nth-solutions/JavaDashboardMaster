package com.bioforceanalytics.dashboard;

/**
 * The dashboard must behave differently across different OSes,
 * particularly with regard to file retrieval and writing as well as using ffmpeg.
 * This class manages various OS related things, but primarily gets the OS upon first launch of the dashboard.
 */
public class OSManager {

    public enum OS {
        WINDOWS,
        MAC,
        UNSUPPORTED
    }

    /**
     * Retrieves the operating system this application is running on.
     * @return an {@link com.bioforceanalytics.dashboard.OSManager.OS OS} enum representing the current operating system
     */
    public static OS getOS() {

        String name = System.getProperty("os.name").toLowerCase();

        if (name.contains("win")) {
            return OS.WINDOWS;
        } else if (name.contains("mac")) {
            return OS.MAC;
        } else {
            return OS.UNSUPPORTED;
        }

    }
}
