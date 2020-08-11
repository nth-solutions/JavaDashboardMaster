package com.bioforceanalytics.dashboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Calculates SINC calibration values such as timer0 offset and delay after start.
 * <p>
 * Based on a 2 minute test where the module lights up at <code>00:02</code>, then turns off at <code>02:00</code>.
 * Scans from <code>00:00-00:03</code> for the first non-black frame, then from <code>01:55-END</code> for the first black frame.
 * </p>
 */
public class BlackFrameAnalysis {

	private final int videoFPS = 30;
	private final int moduleSPS = 960;
	private final int lengthOfTest = 120;
	private final double T_INTERVAL = (1.0/(double)videoFPS);
	private int preLitBFNum = 0;		//sets integer for the last black frame at 0
	private int postLitBFNum = 0;
	
	private static final Logger logger = LogManager.getLogger();

	/*
	 * TODO implement features listed below
	 * Reads module sample rate, video sample rate, and the video file.
	 * Returns the offset for TMR0
	 */	

	public BlackFrameAnalysis(String videoFilePath) throws IOException {

		// TODO rework this method, a for loop should not be necessary
		//
		// when i = 1, FFmpeg checks for the last black frame before light turns on,
		// when i = 2, FFmpeg checks for the first black frame after light turns off
		for (int i = 1; i < 3; i++) {

			// start FFmpeg process to search for black frames
			Process ffmpeg = Runtime.getRuntime().exec(cmdWrapper(videoFilePath, i));

			// save the output stream of the FFmpeg command
			BufferedReader stdError = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));

			String lineText;
			// keep reading messages from the output stream until there's nothing left
			while ((lineText = stdError.readLine()) != null) {

				// check if a black frame is detected
				if (lineText.contains("[Parsed_blackframe")) {

					if (i == 1) {
						// save the last black frame number before test starts
						preLitBFNum = Integer.parseInt(lineText.split(" ")[3].split(":")[1]);
					} else {
						// save the first black frame number after test ends 
						postLitBFNum = Integer.parseInt(lineText.split(" ")[3].split(":")[1]); 
						postLitBFNum += (115 * videoFPS);
						
						// end the loop
						i = 3;
						break;
					}	
				}
			}
		}
		
	}

	public int getDelayAfterStart() {
		logger.debug("Last non-black frame: " + postLitBFNum);
		if((int)(2000-(T_INTERVAL * (preLitBFNum) * 1000)) >= 0){
			return (int)(2000-(T_INTERVAL * (preLitBFNum) * 1000)); //Milliseconds the module started before camera; formula = (2SecondsFrames - MeasuredFrames) * (periodOfFrame) * 1000; Error times period to find offset in second, times 1E3 to convert to milliseconds
		}
		else{
			return (int)(2000-(T_INTERVAL * (preLitBFNum) * 1000));
			//return 0;
		}
	}

	public int getTMR0Offset() {
		logger.debug("First non-black frame: " + preLitBFNum);
		double timeError =  (double)((lengthOfTest * videoFPS) - postLitBFNum) *  T_INTERVAL;  //Error in seconds; formula = (Actual - Expected) * (period); Amount of frames off times period equals error in seconds
		//System.out.println(timeError);
		double sampleDrift = (timeError /(moduleSPS * lengthOfTest)) * 1000000000 ;		//Error over each sample in nano seconds; formula = (Error / TotalNumSample) * 1 billion; Total error divided evenly over every individual sample times 1E9 to convert to nano seconds
		double tmr0Adj = sampleDrift / 250;		//Each bit of TMR0 offset is 250 nano seconds; converts SampleDrift to clock cycles
		return (int) Math.round(tmr0Adj);		//Rounds fraction to an integer
	}

	public int getTMR0Offset(int a, int b) {
		double timeError =  (double)((a - b) - (lengthOfTest * videoFPS)) *  T_INTERVAL;  //Error in seconds; formula = (Actual - Expected) * (period); Amount of frames off times period equals error in seconds
		//System.out.println(timeError);
		double sampleDrift = (timeError /(moduleSPS * lengthOfTest)) * 1000000000 ;		//Error over each sample in nano seconds; formula = (Error / TotalNumSample) * 1 billion; Total error divided evenly over every individual sample times 1E9 to convert to nano seconds
		double tmr0Adj = sampleDrift / 250;		//Each bit of TMR0 offset is 250 nano seconds; converts SampleDrift to clock cycles
		return (int) Math.round(tmr0Adj);		//Rounds fraction to an integer
	}
	
	/*
	 * Returns a String to be run as a command with the proper directory prefix, determined by os.name property and os.arch properties. 
	 */
	public String cmdWrapper(String videoName, int commandNum) {

		// analyzes full video (shouldn't be used in normal function)
		String CMD = "ffmpeg -i \"" + videoName + "\" -vf blackframe -f rawvideo -y NUL";

		// analyzes the start of the video for black frames
		String CMD1 = "ffmpeg -i \"" + videoName + "\" -to 00:00:04 -vf blackframe -f rawvideo -y NUL";

		// analyzes the end of the video for black frames
		String CMD2 = "ffmpeg -ss 00:01:55 -i \"" + videoName + "\" -to 00:00:20 -vf blackframe -f rawvideo -y NUL";

		FfmpegSystemWrapper SysWrap = new FfmpegSystemWrapper();

		//Set internal private variable (detects system binary for OS + Architecture)
		switch(commandNum) {

			case 1: return SysWrap.getBinRoot()+CMD1;		//First 3 seconds of video
			case 2: return SysWrap.getBinRoot()+CMD2;		//Skips 115  seconds in; analyzes next ten seconds
			case 0: default: return SysWrap.getBinRoot()+CMD; //Analyzes full video
		}

	}
}

