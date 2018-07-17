package dataorganizer;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author Mobile2
 */
public class BlackFrameAnalysis {
	private int videoFPS = 240;
	private final int DELAY_IN_SECONDS_BEFORE_LIGHT = 2;
	private int NumNonBlack;
	private final int moduleSPS = 960;
	private final int lengthOfTest = 120;
	private final double T_INTERVAL = (1/240);
	private int lastBlackFrame = 0;		//sets integer for the last black frame at 0
	private int blackFrame = 0;		
	
	
	Process process;

	/*
	 * Reads module sample rate, video sample rate, and the video file. 
	 * Returns the offset for TMR0
	 */
	public void getBlackFrameAnalysis(String videoFilePath) throws IOException{
		//for (int i = 0; i < 2; i++) {	
		//	if (i == 0) {
				process = Runtime.getRuntime().exec(cmdWrapper1(videoFilePath));                                                               //get runtime variable to execute command line
		//	}else {
		//		process = Runtime.getRuntime().exec(cmdWrapper2(videoFilePath)); 
		//	}
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));                  //initializes BufferedReader to read the error stream of the CMD
			//ArrayList<Integer> blackFrames = new ArrayList<>();                                                                  //black frames stores the black frames of the video
			String lineText;                                                                                                       //will store the command line outputs   
			
		
			while ((lineText = stdError.readLine()) != null && blackFrame - lastBlackFrame < 200) { 
	
				//If line contains the string "[P"
				if(lineText.substring(0,2).equals("[P")){
					if (blackFrame >= 1)
						lastBlackFrame = blackFrame;
					else
						lastBlackFrame = 0;
					
					blackFrame = Integer.parseInt(lineText.split(" ")[3].split(":")[1]);   			//parses the number of the frames from the line
					//blackFrames.add(Integer.parseInt(blackFrame));   	//adds the frame number to the blackFrames list
					//System.out.println(blackFrame);
				}
			}
			System.out.println(lastBlackFrame);
			System.out.println(blackFrame);
		//}
		

	}
	
	public int getDelayAfterStart() {
		int delayAfterStart = ((int)((double)(1.0/videoFPS) * (videoFPS * DELAY_IN_SECONDS_BEFORE_LIGHT - lastBlackFrame) * 1000));
		if (delayAfterStart < 0)
			return 0;
		else
			return delayAfterStart;
	    
	}
	
	public int getTMR0Offset(int a, int b) {
		double timeError =  ((double)((a - b) - (lengthOfTest * videoFPS))) * ((double) (1.0/240.0));  //seconds off
		//System.out.println((a - b) - (lengthOfTest * videoFPS));
		//System.out.println(timeError);
		double sampleDrift = (timeError /(moduleSPS * lengthOfTest)) * 1000000000 ;		//nano second adjustment per sample
		//System.out.println(sampleDrift);
		double tmr0Adj = sampleDrift / 250;		//tmr0 bit adjustment
		//System.out.println(sampleDrift);
		//System.out.println((int) Math.round(tmr0Adj));
		return (int) Math.round(tmr0Adj);
	}
	
	public String getLastBlackFrame() {
		return Integer.toString(lastBlackFrame);
	}
	
	
	
	/*
	 * Returns a String to be run as a command with the proper directory prefix, determined by os.name property and os.arch properties. 
	 */
	public String cmdWrapper1(String videoName) {
		String CMD = "ffmpeg -i " + videoName + " -vf blackframe -f rawvideo -y NUL";
		String CMD1 = "ffmpeg -i " + videoName + " -to 00:00:03 -vf blackframe -f rawvideo -y NUL";                   //Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified
		String CMD2 = "ffmpeg -ss 00:02:00 -i " + videoName + " -to 00:00:10 -vf blackframe -f rawvideo -y NUL";                   //Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified

		FfmpegSystemWrapper SysWrap = new FfmpegSystemWrapper();
		SysWrap.setSystemInfo();
		return SysWrap.getBinRoot()+CMD;
	}
	
	
	public String cmdWrapper2(String videoName) {
		String CMD = "ffmpeg -i " + videoName + " -vf blackframe -f rawvideo -y NUL";
		String CMD1 = "ffmpeg -i " + videoName + " -to 00:00:03 -vf blackframe -f rawvideo -y NUL";                   //Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified
		String CMD2 = "ffmpeg -ss 00:02:00 -i " + videoName + " -to 00:00:10 -vf blackframe -f rawvideo -y NUL";                   //Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified

		FfmpegSystemWrapper SysWrap = new FfmpegSystemWrapper();
		SysWrap.setSystemInfo();
		return SysWrap.getBinRoot()+CMD2;
	}	



	//Dan's uncommented mess
	public static void writeOutput(PrintWriter writer, List<Integer> nb, List<Integer> b)
	{
		int rows = 0;
		if(Integer.compare(b.size(), nb.size())>0){
			rows = nb.size();
			int lastRow = 0;
			for(int i = 0; i<rows; i++)
			{
				writer.println(Integer.toString(b.get(i))+","+Integer.toString(nb.get(i)));
				lastRow ++;
			}
			for(int j = lastRow; j<b.size();j++)
			{
				writer.println(Integer.toString(b.get(j))+", ");
			}
		}
		else
		{
			rows = b.size();
			int lastRow = 0;
			for(int i = 0; i<rows; i++)
			{
				writer.println(Integer.toString(b.get(i))+","+Integer.toString(nb.get(i)));
				lastRow ++;
			}
			for(int j = lastRow; j<nb.size();j++)
			{
				writer.println(" ,"+Integer.toString(nb.get(j)));
			}
		}

	}
}

