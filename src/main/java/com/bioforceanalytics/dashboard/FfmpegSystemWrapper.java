package com.bioforceanalytics.dashboard;

public class FfmpegSystemWrapper {
	private String OSName;
	private String Arch;
	
	//INIT
	public FfmpegSystemWrapper() {
		Arch 	 = System.getProperty("os.arch");
		OSName   = System.getProperty("os.name");
	}
	
	/**
	 * Populates the OSName and Arch variables.
	 */
	public void setSystemInfo(){
		Arch 	 = System.getProperty("os.arch");
		OSName   = System.getProperty("os.name");
	}
	
	/**
	 * Print class privates
	 */
	public void PrintAll() {
		System.out.println(OSName);
		System.out.println(Arch);
	}
	
	/**
	 * Return the directory of the FFMPEG binary for the detected system.
	 * Returns null on no match
	 */
	public String getBinRoot() {
		if(OSName.toLowerCase().contains("windows") && Arch.contains("64")) {
			return "ffmpeg\\ffmpeg-win64-static\\bin\\";
		}
		else if (OSName.toLowerCase().contains("mac")){
			return "ffmpeg/ffmpeg-mac/";
		}
		return null;
	}
}
