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
	private final double T_INTERVAL = 4.16667;
	private int lastBlackFrame = 0;                                                                                              //sets integer for the last black frame at 0
	

	/*
	 * Reads module sample rate, video sample rate, and the video file. 
	 * Returns the offset for TMR0
	 */
	public int getLatencyOffset(String videoFilePath) throws IOException{
		Process process = Runtime.getRuntime().exec(cmdWrapper(videoFilePath));                                                               //get runtime variable to execute command line
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));                  //initializes BufferedReader to read the error stream of the CMD
		ArrayList<Integer> blackFrames = new ArrayList<>();                                                                  //black frames stores the black frames of the video
		String lineText;                                                                                                       //will store the command line outputs   
		String blackFrame = "0";
		while ((lineText = stdError.readLine()) != null) { 
			if(Integer.parseInt(blackFrame) > lastBlackFrame + 1) {
				break;
			}
			//If line contains the string "[P"
			if(lineText.substring(0,2).equals("[P")){
				blackFrames.add(Integer.parseInt(blackFrame));   	//adds the frame number to the blackFrames list
				if (blackFrames.size() > 1)
					lastBlackFrame = blackFrames.get(blackFrames.size() - 1);
				else
					lastBlackFrame = 0;
				blackFrame = lineText.split(" ")[3].split(":")[1];                                                  //parses the number of the frames from the line
			}



			



			/*Check if the line contains the string ' fps,' it should be in the metadata
			if(lineText.toLowerCase().contains(" fps,") && videoFPS == 0) {
				//Read the FPS as an integer, it will be a floating point number(add 1 because the string is truncated through the conversion), 5-6 characters, and suffix of 'fps'.
				if(lineText.substring(lineText.indexOf(" fps,") - 6, lineText.indexOf(" fps,") - 5).equals(" ")) {
					videoFPS = (int) Float.parseFloat(lineText.substring(lineText.indexOf(" fps,")-5, lineText.indexOf(" fps,"))) + 1;
				}
				else if(lineText.substring(lineText.indexOf(" fps,") - 7, lineText.indexOf(" fps,") - 6).equals(" ")) {
					videoFPS = (int) Float.parseFloat(lineText.substring(lineText.indexOf(" fps,")-6, lineText.indexOf(" fps,"))) + 1;
				}
			} 

			*/
		}



		int lastblackframe = blackFrames.size();
		return ((int)((double)(1.0/videoFPS) * (videoFPS * DELAY_IN_SECONDS_BEFORE_LIGHT - lastblackframe) * 1000));
	}

	
	public String getLastBlackFrame() {
		return Integer.toString(lastBlackFrame);
	}
	
	
	
	/*
	 * Returns a String to be run as a command with the proper directory prefix, determined by os.name property and os.arch properties. 
	 */
	public String cmdWrapper(String videoName) {
		String CMD = "ffmpeg -i " + videoName + " -vf blackframe -f rawvideo -y NUL";
		String CMD1 = "ffmpeg -i " + videoName + " -to 00:00:03 -vf blackframe -f rawvideo -y NUL";                   //Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified
		String CMD2 = "ffmpeg -ss 00:02:00 -i " + videoName + " -to 00:00:03 -vf blackframe -f rawvideo -y NUL";                   //Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified

		FfmpegSystemWrapper SysWrap = new FfmpegSystemWrapper();
		SysWrap.setSystemInfo();

		return SysWrap.getBinRoot()+CMD;
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

