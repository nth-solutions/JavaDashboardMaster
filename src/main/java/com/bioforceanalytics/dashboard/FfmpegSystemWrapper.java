package com.bioforceanalytics.dashboard;

/**
 * Keeps track of FFmpeg binaries and retrieves appropriate file paths.
 */
public class FfmpegSystemWrapper {

	private String OSName;
	private String Arch;
	
	public FfmpegSystemWrapper() {
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
