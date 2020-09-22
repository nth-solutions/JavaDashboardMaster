package com.bioforceanalytics.dashboard;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

// test1 MUST be run before test2 due to file access issues;
// the Settings class locks the settings files/directory,
// so the CSV test (which uses Settings) must come second
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SettingsTest {

    private static final Logger logger = LogManager.getLogger();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void test1_check_if_config_file_regenerates() {

        File settingsDir = Paths.get(System.getProperty("user.home"), ".BioForce Dashboard").toFile();
        File configFile = new File(settingsDir, "DataOrganizer.properties");

        try {

            // IMPORTANT -- do NOT try to copy & restore settings directory as in test2;
            // due to the file access lock from the Settings class, this will cause test1 to fail

            logger.info("Deleting settings directory...");
            FileUtils.deleteDirectory(settingsDir);

            // verify all config files are gone
            assertTrue(!settingsDir.exists());
            assertTrue(!configFile.exists());

            // recreate settings folder
            FileUtils.forceMkdir(settingsDir);

            logger.info("Restoring default config...");
            assertTrue(Settings.restoreDefaultConfig());

            // verify that all config files were generated
            assertTrue(settingsDir.exists());
            assertTrue(configFile.exists());

        } catch (IOException e) {
            e.printStackTrace();
            fail("Error deleting settings directory");
        }

    }

    @Test
    public void test2_check_if_csv_save_location_regenerates() {

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
