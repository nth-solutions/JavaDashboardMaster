package com.bioforceanalytics.dashboard;

import com.bioforceanalytics.dashboard.OSManager.OS;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

/**
 * Initializes logging system for the Dashboard.
 * Instead of accessing log4j directly, classes should call {@link #start()}
 * to get a Logger instance to use when outputting information.
 */
public class LogController {
    
    /**
     * Initializes all custom log4j configurations and returns a
     * {@link org.apache.logging.log4j.Logger Logger} instance for logging messages.
     * @return a Logger instance for logging messages
     */
    public static Logger start() {

        // set log file location based on operating system
        if (OSManager.getOS() == OS.WINDOWS) {
            System.setProperty("LOG_PATH", System.getenv("APPDATA") + "/EduForce Dashboard/logs");
        }
        else if (OSManager.getOS() == OS.MAC) {
            System.setProperty("LOG_PATH", System.getenv("HOME") + "/Library/Logs/EduForce Dashboard");
        }

        // redirect stdout and stderr to Log4J: this adds more detailed info & captures ALL console output in logs
        System.setErr(IoBuilder.forLogger(LogManager.getRootLogger()).setLevel(Level.ERROR).buildPrintStream());
        System.setOut(IoBuilder.forLogger(LogManager.getRootLogger()).setLevel(Level.INFO).buildPrintStream());

        return LogManager.getRootLogger();

    }

    /**
     * Returns the log file location for the current operating system.
     * <hr>
     * <p>On Windows, this should be <code>%APPDATA%\EduForce Dashboard\logs</code>.</p>
     * <p>On macOS, this should be <code>~/Library/Logs/EduForce Dashboard</code>.</p>
     * @return the log file location for the current operating system
     */
    public String getLogPath() {

        if (OSManager.getOS() == OS.WINDOWS) {
            return System.getenv("APPDATA") + "/EduForce Dashboard/logs";
        }
        else if (OSManager.getOS() == OS.MAC) {
            return System.getenv("HOME") + "/Library/Logs/EduForce Dashboard";
        }
        else {
            return "logs";
        }

    }

}
