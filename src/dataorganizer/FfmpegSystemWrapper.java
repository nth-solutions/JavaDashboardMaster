package dataorganizer;

public class FfmpegSystemWrapper {
	private String OSName;
	private String Arch;
	
	
	public void FfmpegSystemWrapper() {
	}
	
	public void setSystemInfo(){
		Arch = System.getProperty("os.arch");
		OSName   = System.getProperty("os.name");
	}
	
	public void PrintAll() {
		System.out.println(OSName);
		System.out.println(Arch);
	}
	
	public String getBinRoot() {
		if(OSName.toLowerCase().contains("windows") && Arch.contains("64")) {
			return "ffmpeg-win64-static\\bin\\";
		}
		return null;
	}
}
