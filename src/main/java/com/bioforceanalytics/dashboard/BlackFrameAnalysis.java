package com.bioforceanalytics.dashboard;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.github.kokorin.jaffree.ffmpeg.Filter;
import com.github.kokorin.jaffree.ffmpeg.FilterChain;
import com.github.kokorin.jaffree.ffmpeg.FilterGraph;
import com.github.kokorin.jaffree.ffmpeg.NullOutput;
import com.github.kokorin.jaffree.ffmpeg.OutputListener;
import com.github.kokorin.jaffree.ffmpeg.ProgressListener;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Calculates SINC calibration values such as timer0 offset and delay after
 * start.
 * <p>
 * Based on a 2 minute test where the module lights up at <code>00:02</code>,
 * then turns off at <code>02:00</code>. Scans from <code>00:00-00:03</code> for
 * the first non-black frame, then from <code>01:55-END</code> for the first
 * black frame.
 * </p>
 */
public class BlackFrameAnalysis {

	/*
	 * TODO implement features listed below: Read module sample rate, video sample
	 * rate, and the video file Returns the offset for TMR0
	 */
	private final int videoFPS = 30;
	private final int moduleSPS = 960;
	private final int lengthOfTest = 120;
	private final double T_INTERVAL = (1.0 / (double) videoFPS);

	private int preLitBFNum = 0;
	private int postLitBFNum = 0;

	private static final Logger logger = LogManager.getLogger();

	public BlackFrameAnalysis(String videoFilePath) throws IOException {

		// fetch location of FFmpeg binary and input file
		FfmpegSystemWrapper wrapper = new FfmpegSystemWrapper();
		Path BIN = Paths.get(wrapper.getBinRoot());
		Path VIDEO_MP4 = Paths.get(videoFilePath);

		// settings for black frame analysis filter
		// (https://ffmpeg.org/ffmpeg-filters.html#blackframe)
		Filter blackframe = Filter.withName("blackframe").addArgument("amount", "80");

		// find last black frame before test starts
		FFmpeg.atPath(BIN)
			// set input to the first 5 seconds of the video file
			.addInput(UrlInput.fromPath(VIDEO_MP4).setDuration(5, TimeUnit.SECONDS))
			// use black frame analysis filter
			.setFilter(StreamType.VIDEO, FilterGraph.of(FilterChain.of(blackframe)))
			// don't create an output video file
			.addOutput(new NullOutput(false))
			// read output messages
			.setOutputListener(line -> {

				// check if a black frame is detected
				if (line.contains("[Parsed_blackframe")) {
					// save the last black frame number before test starts
					preLitBFNum = Integer.parseInt(line.split(" ")[3].split(":")[1]);
				}

				// indicates no errors occurred
				return true;

			})
			.execute();

		// find first black frame after test ends
		FFmpeg.atPath(BIN)
			// set input to 1:55-2:05 of the video file
			.addInput(UrlInput.fromPath(VIDEO_MP4)
						.setPosition(115, TimeUnit.SECONDS)
						.setDuration(10, TimeUnit.SECONDS))
			// use black frame analysis filter
			.setFilter(StreamType.VIDEO, FilterGraph.of(FilterChain.of(blackframe)))
			// don't create an output video file
			.addOutput(new NullOutput(false))
			// read output messages
			.setOutputListener(new OutputListener() {

				boolean postLitFound = false;

				@Override
				public boolean onOutput(String line) {

					// check if the first black frame is detected
					if (!postLitFound && line.contains("[Parsed_blackframe")) {

						// save the first black frame number after test ends
						postLitBFNum = Integer.parseInt(line.split(" ")[3].split(":")[1]);
						postLitBFNum += (115 * videoFPS);

						// update now that black frame has been found
						postLitFound = true;
					}

					// indicates no errors occurred
					return true;

				}
			})
			.execute();

		logger.debug("First non-black frame: " + preLitBFNum);
		logger.debug("Last non-black frame: " + postLitBFNum);

	}

	/**
	 * <p>
	 * Calculates, in milliseconds, how long the module should wait before recording data.
	 * This should only be used if the value is greater than 0.
	 * </p>
	 * 
	 * For a test with no timing errors where the light turns on at exactly 2 seconds,
	 * this method will return 2000. For a non-ideal test, the returned value will be
	 * the actual time the light turned on at subtracted from the expected time (2 seconds).
	 * 
	 * @return how long the module should wait before recording data in milliseconds
	 */
	public int getDelayAfterStart() {

		// Time at which light turns on = (1 / frame rate) * frame number
		double lightOnTime = T_INTERVAL * preLitBFNum;

		// Delay after start = 2000 ms - "light-on time" in milliseconds
		return (int) (2000 - (lightOnTime * 1000));
	}

	/**
	 * Calculates the timer0 tick offset used to mitigate error between actual and expected sample rate.
	 * @return the timer0 tick offset
	 */
	public int getTMR0Offset() {
		
		// Total number of frames = frame rate * duration
		int totalNumFrames = videoFPS * lengthOfTest;

		// Error in seconds = (Actual - Expected) * period
		double timeError = (double) (totalNumFrames - postLitBFNum) * T_INTERVAL;

		// Total number of samples = Sample rate * duration
		int totalNumSamples = moduleSPS * lengthOfTest;

		// Error over each sample = error / total number of samples;
		// multiplied by 1 billion to convert from seconds to nanoseconds
		double sampleDrift = (timeError / totalNumSamples) * 1E9;

		// convert sample drift to clock cycles (each bit of TMR0 offset is 250 nanoseconds)
		double tmr0Adj = sampleDrift / 250;

		// round fraction to an integer
		return (int) Math.round(tmr0Adj);

	}
	
}