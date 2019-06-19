package dataorganizer;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 *
 * @author Mobile2
 */
public class BlackFrameAnalysis {
	private int videoFPS = 240;
	private final int moduleSPS = 960;
	private final int lengthOfTest = 120;
	private final double T_INTERVAL = (1.0/(double)videoFPS);
	private int preLitBFNum = 0;		//sets integer for the last black frame at 0
	private int postLitBFNum = 0;		

	/*
	 * Reads module sample rate, video sample rate, and the video file. 
	 * Returns the offset for TMR0
	 */
	public BlackFrameAnalysis(){
		
	}
	
	public BlackFrameAnalysis(String videoFilePath) throws IOException{
		for(int i = 1; i < 3; i++) {			//Loop sets i == 1; then 1 == 2, while i == 1 FFMPEG checks for the last black frame before light turns on; while 1 == 2 ffMPEG checks for first black frame after light turns off
			Process ffmpeg = Runtime.getRuntime().exec(cmdWrapper(videoFilePath, i));                                                               //get runtime variable to execute command line
			BufferedReader stdError = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));                  //initializes BufferedReader to read the error stream of the CMD
			String lineText;                                                                                                       //will store the command line outputs   

			while ((lineText = stdError.readLine()) != null) { 		//Read until end of time length
				//If line contains the string "[P"
				if(lineText.substring(0,2).equals("[P")){
					if (i == 1) {
						preLitBFNum = Integer.parseInt(lineText.split(" ")[3].split(":")[1]);   			//parses the number of the frames from the line
					}else {
						i = 3;
						postLitBFNum = Integer.parseInt(lineText.split(" ")[3].split(":")[1]); 
						postLitBFNum += (115 * videoFPS); 
						break;
					}	
				}
			}
		}
		
	}



	public int getDelayAfterStart() {
		return (int)(T_INTERVAL * (preLitBFNum) * 1000);	//Milliseconds the module started before camera; formula = (2SecondsFrames - MeasuredFrames) * (periodOfFrame) * 1000; Error times period to find offset in second, times 1E3 to convert to milliseconds
	}

	public int getTMR0Offset() {
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
		String CMD = "ffmpeg -i \"" + videoName + "\" -vf blackframe -f rawvideo -y NUL";//Analyzes full video
		//String CMD1 = "ffmpeg -i " + videoName + " -to 00:00:03 -vf blackframe -f rawvideo -y NUL";                   //First 3 seconds of video; analyzes next ten seconds; Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified
		//String CMD2 = "ffmpeg -ss 00:01:55 -i " + videoName + " -to 00:00:20 -vf blackframe -f rawvideo -y NUL";                   //SKips 115 seconds in and reads next 20 seconds; Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified
		String CMD1 = "ffmpeg -i \"" + videoName + "\" -to 00:00:04 -vf blackframe -f rawvideo -y NUL";                   //First 3 seconds of video; analyzes next ten seconds; Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified
		String CMD2 = "ffmpeg -ss 00:01:55 -i \"" + videoName + "\" -to 00:00:20 -vf blackframe -f rawvideo -y NUL"; 
		

		FfmpegSystemWrapper SysWrap = new FfmpegSystemWrapper();
		//Create instance of wrapper class
		SysWrap.setSystemInfo();
		//Set internal private variable (detects system binary for OS + Architecture)
		switch(commandNum) {

			case 1: return SysWrap.getBinRoot()+CMD1;		//First 3 seconds of video
			case 2: return SysWrap.getBinRoot()+CMD2;		//Skips 115  seconds in; analyzes next ten seconds
			case 0: default: return SysWrap.getBinRoot()+CMD; //Analyzes full video

		}

	}
}

