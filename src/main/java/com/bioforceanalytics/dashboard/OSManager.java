package com.bioforceanalytics.dashboard;

/**
 * The dashboard must behave differently across different OSes,
 * particularly with regard to file retrieval and writing as well as using ffmpeg.
 * This class manages various OS related things, but primarily gets the OS upon first launch of the dashboard.
 */
public class OSManager {

    private String OSName;
    private String OSType;
    public OSManager(){
        OSName = System.getProperty("os.name").toLowerCase();
    }
    public String getOSName(){
        return OSName;
    }

    public String getOSType(){

        if (OSName.indexOf("win") >= 0){
            OSType = "Windows";
        }else if(OSName.indexOf("mac")>= 0){
            OSType = "Mac";
        }else{
            System.out.println("Unsupported OS used");
        }

        return OSType;
    }
}
