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
    public static String convertToMP4(String videoFilePath) throws RuntimeException {

        // fetch location of FFmpeg binary and input file
        FfmpegSystemWrapper wrapper = new FfmpegSystemWrapper();
        Path BIN = Paths.get(wrapper.getBinRoot());
        
        // define input and output files for FFmpeg
        Path INPUT = Paths.get(videoFilePath);
        Path OUTPUT = Paths.get(convertFileExt(videoFilePath));
        
        // retrieve file extension of video to convert
        String fileExt = getFileExt(videoFilePath);

        // initialize FFmpeg command chain
        FFmpeg cmd = FFmpeg.atPath(BIN);

        // apply fix to FFmpeg if video is .avi
        //
        // FIXME Jaffree doesn't put arguments before inputs;
        // find a workaround to make sure AVI conversions work
        if (fileExt.equals("avi")) {
            cmd.addArguments("-fflags", "+genpts");
        }

        cmd
            // set input to "videoFilePath"
            .addInput(UrlInput.fromPath(INPUT))
            // set output to "videoFilePath" with .mp4 extension
            .addOutput(UrlOutput.toPath(OUTPUT)
                // don't re-encode the video (wastes time)
                //
                // FIXME this command shouldn't be run for H.265 videos;
                // these should actually be re-encoded in H.264.
                // To do so, check the codec in GraphNoSINCController.
                // (May need a quick FFprobe to check this [MediaConverter.getCodec()])
                .copyAllCodecs()
            )
            .execute();

        // return file path of converted video for UI purposes
        return OUTPUT.toString();
    }

    /**
     * Gets the file extension associated with a file path.
     * @param filePath the file path
     * @return the extension of the file
     */
    public static String getFileExt(String filePath) {

        // split file extension from name
        String[] arr = filePath.split("\\.");

        // return last element (file extension)
        return arr[arr.length-1].toLowerCase();

    }

    /**
     * Converts a given file path's extension to ".mp4".
     * @param filePath the file path to convert
     * @return the converted file path
     */
    public static String convertFileExt(String filePath) {

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
