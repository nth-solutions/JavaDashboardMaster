package com.bioforceanalytics.dashboard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Ensures versions match across configuration files.
 */
public class VersionTest {
    
    @Test
    public void nsis_should_match_maven() {

        // NSIS install script
        File nsis = new File("tools/install.nsi");

        // version line in NSIS install script
        String versionLine = "!define PRODUCT_VERSION";

        // passed in from Maven's pom.xml
        // TODO does not work through VSCode's "Java Test Runner"
        String mavenVersion = "\"" + System.getProperty("projectVersion") + "\"";

        try (Stream<String> stream = Files.lines(nsis.toPath(), Charset.defaultCharset())) {

            stream.forEach(line -> {
                if (line.contains(versionLine)) {
                    assertThat(line, endsWith(mavenVersion));
                }
            });

        }
        catch (IOException e) {
            fail("NSIS install script not found.");
        }

    }

}