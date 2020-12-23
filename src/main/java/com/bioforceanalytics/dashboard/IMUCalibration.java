package com.bioforceanalytics.dashboard;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;

/**
 * Calculates offsets for the IMU calibration process.
 */
public class IMUCalibration {

    private static final Logger logger = LogController.start();

    /**
     * Calculates acceleration offsets for the IMU.
     * @param csvPath the path to the CSV calibration data
     * @param blockSize the sampling interval used for calibration 
     * @param maxSD the maximum standard deviation allowed for a block
     * @param dataThreshold the maximum range from ±2048 (1G) in raw sensor format
     * @param sensitivity the sensitivity of the accelerometer (2/4/8/16G)
     * @param sampleRate the sample rate recorded by the accelerometer (60/120/240/480/500/960 sps)
     * @return a 2D array containing each axis and its min/max in signed 16-bit int format
     * @throws NumberFormatException
     * @throws IOException
     */
    public static int[][] getOffsets(String csvPath, int blockSize, double maxSD, int dataThreshold, int sensitivity, int sampleRate) throws NumberFormatException, IOException {

        // create offsets
        int[][] offsets = new int[9][2];

        // read CSV data into 2D list (see "Data Samples" in README)
        List<List<Double>> data = CSVHandler.readCSV(csvPath);

        // 2D lists that stores min/max average values over "static blocks";
        //
        // "static blocks" = sections of data where the module is not moving,
        // but where the given axis is aligned with the direction of gravity (±1G)
        List<List<Double>> minMeans = new ArrayList<List<Double>>();
        List<List<Double>> maxMeans = new ArrayList<List<Double>>();

        // loop through each acceleration axis (X/Y/Z)
        for (int i = 1; i < 4; i++) {

            // initialize inner lists for given axis
            minMeans.add(new ArrayList<Double>());
            maxMeans.add(new ArrayList<Double>());

            // loop through each block (j = start index of block)
            for (int j = 0; j < data.get(1).size(); j+=blockSize) {

                // block indices
                int start = j;
                int end = j+blockSize;

                // if this is the last block, reduce block size & fix indices
                if (end > data.get(1).size()) {
                    end = data.get(1).size();
                    blockSize = end - start;
                }

                double mean = 0, SD = 0;

                // sign data for this block (needed to calculate mean)
                for (int k = start; k < end; k++) {

                    // convert double to int
                    int sample = (int) data.get(i).get(k).doubleValue();

                    // convert all unsigned negative values
                    if (sample > 32768) {
                        data.get(i).set(k, (double) (sample - 65535));
                    }

                }

                // calculate sum for mean
                for (int k = start; k < end; k++) {
                    mean += data.get(i).get(k);
                }

                // calculate mean
                mean /= blockSize;

                // calculate sum for standard deviation
                for (int k = start; k < end; k++) {
                    SD += Math.pow(data.get(i).get(k) - mean, 2);
                }

                // calculate standard deviation
                SD = Math.sqrt(SD / blockSize);

                // check that mean and SD are within acceptable bounds before saving mean
                if ((Math.abs(mean) <= (32768/sensitivity) + dataThreshold) && SD <= maxSD) {

                    // figure out which list (min offsets vs max offsets) to add mean to
                    List<List<Double>> list = mean < 0 ? minMeans : maxMeans;

                    // add the mean to its respective list and axis;
                    // (i-1) is needed because i starts at 1 instead of 0
                    list.get(i-1).add(mean);

                }

                // save information about standard deviations
                DecimalFormat df = new DecimalFormat("#.##");

                double startTime = (double) start / sampleRate;
                double endTime = (double) end / sampleRate;

                String interval = "[" + df.format(startTime) + ", " + df.format(endTime) + "]";
                logger.trace("SD for " + interval + " of " + AxisType.valueOf(i-1) + ": " + (int) SD);

            }

            // set min and max offset in final array
            offsets[i-1][0] = (int) calcAverage(minMeans.get(i-1));
            offsets[i-1][1] = (int) calcAverage(maxMeans.get(i-1));

        }

        // display all acceleration offsets
        for (int i = 0; i < 3; i++) {
            logger.info(AxisType.valueOf(i) + " offsets: " + Arrays.toString(offsets[i]));
        }

        // return final array of offsets
        return offsets;

    }

    /**
     * Internal method to calculate average value of a list.
     * @param list the list of doubles to calculate from
     * @return the average value of the list
     */
    private static double calcAverage(List<Double> list) {

        double mean = 0;

        for (double d: list) {
            mean += d;
        }

        mean /= list.size();
        return mean;

    }

}
