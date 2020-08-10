package com.bioforceanalytics.dashboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

public class SINCTest extends GUITest {
    
    @Test
    public void check_if_ffmpeg_exists() {
        
        // check that wrapper class has correct path
        FfmpegSystemWrapper ffmpeg = new FfmpegSystemWrapper();
        assertNotNull(ffmpeg.getBinRoot());

        try {

            Process proc = Runtime.getRuntime().exec(ffmpeg.getBinRoot() + "ffmpeg.exe -version");
            proc.waitFor();
            
            // print any errors to stdout
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			String lineText;
			while ((lineText = stdError.readLine()) != null) {
				System.out.println(lineText);
			}

            // check that ffmpeg runs version command successfully (error code 0)
            assertEquals("FFmpeg exit status", 0, proc.waitFor());
        }
        catch (InterruptedException e) {
            fail("FFmpeg process interrupted during execution");
        }
        catch (IOException e) {
            fail("IOException when running FFmpeg");
        }
    }

    @Test
    public void check_sinc_calibration_real_test() {

        final int timer0Offset = -43;
        final int delayAfterStart = 2000;

        try {

            String videoFile = new File(getClass().getResource("sinc-test-real.mp4").getFile()).getPath();
            BlackFrameAnalysis bfa = new BlackFrameAnalysis(videoFile);

            assertEquals("Timer0 offset", timer0Offset, bfa.getTMR0Offset());
            assertEquals("Delay after start", delayAfterStart, bfa.getDelayAfterStart());

        } catch (IOException e) {
            fail("IOException loading video file");
        }

    }

    @Test
    public void check_sinc_calibration_ideal_test() {

        final int timer0Offset = 0;
        final int delayAfterStart = -466;

        try {

            String videoFile = new File(getClass().getResource("sinc-test-ideal.mp4").getFile()).getPath();
            BlackFrameAnalysis bfa = new BlackFrameAnalysis(videoFile);

            assertEquals("Timer0 offset", timer0Offset, bfa.getTMR0Offset());
            assertEquals("Delay after start", delayAfterStart, bfa.getDelayAfterStart());

        } catch (IOException e) {
            fail("IOException loading video file");
        }

    }

}