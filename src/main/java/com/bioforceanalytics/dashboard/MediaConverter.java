package com.bioforceanalytics.dashboard;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;

/**
 * Handles converting various file formats to MP4 for use with JavaFX Media.
 * <hr>
 * TODO -- this process is necessary due to a limitation with the JavaFX API;
 * this could be solved by switching to a 3rd party video player such as
 * <code>vlcj-javafx</code>, a JavaFX-compatible component that wraps VLC Media
 * Player's functionality.
 * 
 * However, the base requirement for <code>vlcj-javafx</code> is JavaFX 14 and
 * JDK 11, which the Dashboard's codebase does not support (on JDK 8 currently).
 * 
 * This migration could be done later down the line, but would require
 * substantial rework of various sections of the codebase, specifically all
 * video interactions in the SINC+DAG rework of SINC Technology.
 */
public class MediaConverter {

    /**
     * Converts a given file to an .mp4 and saves the converted version alongside the original.
     * @param videoFilePath the file path of the video to convert
     * @return the file path of the converted video
     */
    public static String toMP4(String videoFilePath) {

        // fetch location of FFmpeg binary and input file
        FfmpegSystemWrapper wrapper = new FfmpegSystemWrapper();
        Path BIN = Paths.get(wrapper.getBinRoot());
        
        // define input and output files for FFmpeg
        Path INPUT = Paths.get(videoFilePath);
        Path OUTPUT = Paths.get(convertFileExt(videoFilePath));
        
        // convert video to .mp4
        // TODO add "setProgressListener()"
        FFmpeg.atPath(BIN)
            // set input to "videoFilePath"
            .addInput(UrlInput.fromPath(INPUT))
            // set output to "videoFilePath" with .mp4 extension
            .addOutput(UrlOutput.toPath(OUTPUT)
                // don't re-encode the video (wastes time)
                .copyAllCodecs()
            )
            .execute();

        // return file path of converted video for UI purposes
        return OUTPUT.toString();
    }

    /**
     * Internal method used to convert a given file path's extension into ".mp4".
     * @param filePath the file path to convert
     * @return the converted file path
     */
    private static String convertFileExt(String filePath) {

        // split file extension from name
        // (will also split at all periods in file name, this is accounted for)
        String[] arr = filePath.split("\\.");

        // remove the last entry (file extension) from array
        arr = Arrays.copyOf(arr, arr.length-1);

        String newFilePath = "";

        // recreate file path (add back any periods)
        for (String s : arr) {
            newFilePath += s + ".";
        }

        // append new file extension
        // (period is already there from the` for` loop)
        newFilePath += "mp4";

        return newFilePath;

    }

}
