package com.bioforceanalytics.dashboard;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SettingsTest {

    private static final Logger logger = LogManager.getLogger();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Ignore("Problem with deleting settings file when used by Java process")
    @Test
    public void check_if_config_file_regenerates() {

        File settingsDir = Paths.get(System.getProperty("user.home"), ".BioForce Dashboard").toFile();
        File configFile = new File(settingsDir, "DataOrganizer.prop");

        try {

            logger.info("Saving a copy of current settings directory to \"{}\"...", tempFolder.getRoot());
            FileUtils.copyDirectory(settingsDir, tempFolder.getRoot());

            logger.info("Deleting settings directory...");
            FileUtils.deleteDirectory(settingsDir);

            // verify all config files are gone
            assertTrue(!settingsDir.exists());
            assertTrue(!configFile.exists());

            logger.info("Restoring default config...");
            assertTrue(Settings.restoreDefaultConfig());

            // verify that all config files were generated
            assertTrue(settingsDir.exists());
            assertTrue(configFile.exists());

            logger.info("Regenerating original settings directory...");
            FileUtils.copyDirectory(tempFolder.getRoot(), settingsDir);

        } catch (IOException e) {
            e.printStackTrace();
            fail("Error deleting settings directory");
        }

    }

    @Test
    public void check_if_csv_save_location_regenerates() {

        File saveDir = new File(Settings.get("CSVSaveLocation"));

        try {

            logger.info("Saving a copy of current CSV save directory to \"{}\"...", tempFolder.getRoot());
            FileUtils.copyDirectory(saveDir, tempFolder.getRoot());

            logger.info("Deleting CSV save directory \"{}\"...", saveDir);
            FileUtils.deleteDirectory(saveDir);

            // verify that CSV save directory was deleted
            assertTrue(!saveDir.exists());

            logger.info("Regenerating CSV save directory...");
            Settings.get("CSVSaveLocation");

            // verify that CSV save directory was regenerated
            assertTrue(saveDir.exists());

            // clean up test by copying CSVs back
            FileUtils.copyDirectory(tempFolder.getRoot(), saveDir);

        } catch (IOException e) {
            fail("Error copying/creating CSV save directory");
        }

    }

}
