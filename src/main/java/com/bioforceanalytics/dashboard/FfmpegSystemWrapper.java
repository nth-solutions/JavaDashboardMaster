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
	 * Return the directory of the FFMPEG binary for the detected system.
	 * Returns null on no match
	 */
	public String getBinRoot() {
		if (OSName.toLowerCase().contains("windows") && Arch.contains("64")) {
			return "ffmpeg\\win64\\";
		}
		else if (OSName.toLowerCase().contains("mac")) {
			return "ffmpeg/mac/";
		}
		return null;
	}
}
