package dataorganizer;

import java.util.List;
import java.util.ArrayList;

public class GenericTest {
	
	private AxisDataSeries[] axes;
	
	public int[][] MPUMinMax; //first dimension is for axis(1), second is for min(0)/max(1) 
	private int [] mpuOffsets;

	private int delayAfterStart;

	
	
	
	/**
	 * <p>Creates a GenericTest from an already-existing DataOrganizer.</p>
	 * <b>This is the old method of passing data to NewGraph,
	 * use {@link #GenericTest(ArrayList, int[], int[][])}
	 * with data directly from SerialComm instead.</b>
	 * @param d The DataOrganizer object to read data from.
	 */
	public GenericTest(DataOrganizer d) {
		
		/*
		========================
		AxisDataSeries indices:
		========================

		0		Acceleration X
		1		Acceleration Y
		2		Acceleration Z
		3		Acceleration Magnitude
		4		Velocity X
		5		Velocity Y
		6		Velocity Z
		7		Velocity Magnitude
		8		Displacement X
		9		Displacement Y
		10		Displacement Z
		11		Displacement Magnitude
		12		Angular Acceleration X
		13		Angular Acceleration Y
		14		Angular Acceleration Z
		15		Angular Acceleration Magnitude
		16		Angular Velocity X
		17		Angular Velocity Y
		18		Angular Velocity Z
		19		Angular Velocity Magnitude
		20		Angular Displacement X
		21		Angular Displacement Y
		22		Angular Displacement Z
		23		Angular Displacement Magnitude
		24		Magnetometer X
		25		Magnetometer Y
		26		Magnetometer Z
		27		Magnetometer Magnitude
		*/

		axes = new AxisDataSeries[32];
		
		List<Double> timeAxis = new ArrayList<Double>();
		List<Double> magTimeAxis = new ArrayList<Double>();

		// populate time and magnetometer time axes

		/*
		TODO - for some reason, the number of time samples
		is one greater than the number of data samples;
		not knowing whether this is intentional or not,
		I have left the extra sample in and changed all
		appropriate loops to use the data array as the bounds
		*/
		for (int i = 0; i < d.getDataSamples().get(0).size(); i++) {
			
			timeAxis.add(new Double(i) / d.getSampleRate());
			
			// since magnetometer sample rate is 1/10 of the regular sample rate,
			// "magTimeAxis" can still be calculated using "d.getSampleRate()"

			if (i % 10 == 0) magTimeAxis.add(new Double(i) / d.getSampleRate());
			
		}
		
		// loops through axes (X=0, Y=1, Z=2)
		for (int i = 0; i < 3; i++) {
			
			// acceleration (NATIVE ACCELEROMETER MEASUREMENT)
			axes[i] = new AxisDataSeries(timeAxis, d.getDataSamples().get(i+1), AxisType.valueOf(i), d.getMPUOffsets(), d.accelSensitivity, d.getSampleRate());
			
			// velocity
			axes[i+4] = new AxisDataSeries(timeAxis, axes[i].integrate(), AxisType.valueOf(i+4), false, d.getSampleRate());
			
			// displacement
			axes[i+8] = new AxisDataSeries(timeAxis, axes[i+4].integrate(), AxisType.valueOf(i+8), false, d.getSampleRate());
			
			// angular velocity (NATIVE GYRO MEASUREMENT)
			axes[i+16] = new AxisDataSeries(timeAxis, d.getDataSamples().get(i+4), AxisType.valueOf(i+16), d.gyroSensitivity, d.getSampleRate());
			
			// angular acceleration
			axes[i+12] = new AxisDataSeries(timeAxis, axes[i+16].differentiate(), AxisType.valueOf(i+12), false, d.getSampleRate());	
			
			// angular displacement
			axes[i+20] = new AxisDataSeries(timeAxis, axes[i+16].integrate(), AxisType.valueOf(i+20), false, d.getSampleRate());
			
			// magnetometer
			axes[i+24] = new AxisDataSeries(magTimeAxis, d.getDataSamples().get(i+7), AxisType.valueOf(i+24), true, d.getMagSampleRate());

			// TESTING -- linear acceleration
			axes[i+28] = new AxisDataSeries(axes[i]);
	
		}
				
		// TODO potentially rework the magnitude calculations section --
		// seems like an imperfect solution and somewhat verbose

		// acceleration magnitude
		axes[3] = new AxisDataSeries(timeAxis, d.getDataSamples().get(0), AxisType.valueOf(3), false, d.getSampleRate());

		// velocity magnitude
		axes[7] = new AxisDataSeries(timeAxis, d.getDataSamples().get(0), AxisType.valueOf(7), false, d.getSampleRate());

		// displacement magnitude
		axes[11] = new AxisDataSeries(timeAxis, d.getDataSamples().get(0), AxisType.valueOf(11), false, d.getSampleRate());

		// angular acceleration magnitude
		axes[15] = new AxisDataSeries(timeAxis, d.getDataSamples().get(3), AxisType.valueOf(15), false, d.getSampleRate());

		// (GYRO) angular velocity magnitude
		axes[19] = new AxisDataSeries(timeAxis, d.getDataSamples().get(3), AxisType.valueOf(19), false, d.getSampleRate());

		// angular displacement magnitude
		axes[23] = new AxisDataSeries(timeAxis, d.getDataSamples().get(3), AxisType.valueOf(23), false, d.getSampleRate());

		// magnetic field magnitude
		axes[27] = new AxisDataSeries(magTimeAxis, d.getDataSamples().get(7), AxisType.valueOf(27), false, d.getMagSampleRate());
		
		// loop through all data samples
		for (int i = 0; i < d.getDataSamples().get(1).size(); i++) {

			axes[3].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[0].getSmoothedData()[i], 2)+Math.pow(axes[1].getSmoothedData()[i], 2)+Math.pow(axes[2].getSmoothedData()[i], 2)));
			axes[7].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[4].getSmoothedData()[i], 2)+Math.pow(axes[5].getSmoothedData()[i], 2)+Math.pow(axes[6].getSmoothedData()[i], 2)));
			axes[11].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[8].getSmoothedData()[i], 2)+Math.pow(axes[9].getSmoothedData()[i], 2)+Math.pow(axes[10].getSmoothedData()[i], 2)));
			axes[15].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[12].getSmoothedData()[i], 2)+Math.pow(axes[13].getSmoothedData()[i], 2)+Math.pow(axes[14].getSmoothedData()[i], 2)));
			axes[19].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[16].getSmoothedData()[i], 2)+Math.pow(axes[17].getSmoothedData()[i], 2)+Math.pow(axes[18].getSmoothedData()[i], 2)));
			axes[23].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[20].getSmoothedData()[i], 2)+Math.pow(axes[21].getSmoothedData()[i], 2)+Math.pow(axes[22].getSmoothedData()[i], 2)));
			
			// if "i" fits # of magnetometer data samples
			if (i < d.getDataSamples().get(7).size()) {
				axes[27].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[24].getSmoothedData()[i], 2)+Math.pow(axes[25].getSmoothedData()[i], 2)+Math.pow(axes[26].getSmoothedData()[i], 2)));
			} 
		}
	}
	
	/**
	 * This constructor takes data directly from SerialComm and doesn't rely on DataOrganizer.
	 * @param testParameters - array of test parameters
	 * @param finalData - Array of raw data from module
	* @param MPUMinMax - Array of constant MPU offsets specific to the module
	*/
	public GenericTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax) {

		int timer0 = testParameters.get(1);
		int delayAfterStart = testParameters.get(2);
		double lengthOfTest = testParameters.get(6);
		int sampleRate = testParameters.get(7);
		int magSampleRate = testParameters.get(8);
		int accelSensitivity = testParameters.get(9);
		int gyroSensitivity = testParameters.get(10);

		int magSensitivity = 4800;
		double magInterval = sampleRate / magSampleRate;
		
		mpuOffsets = new int[9];
		
		for(int axi = 0; axi < MPUMinMax.length; axi++) {
			mpuOffsets[axi] = (MPUMinMax[axi][0]+MPUMinMax[axi][1])/2;
		}
		
		/*************************Organizing Raw Data Received from SerialComm (finalData)*****************************************
		 * 
		 * How data in array passed from SerialComm (finalData) is organized:
		 * 
		 * a= acceleration, g = gyroscope, m = magnetometer
		 * 1= first byte, 2 = second byte
		 * 
		 * 
		 * Each data sample is composed of two bytes (e.g. ax1,ax2 are the two bytes composing a single acceleration data point w/respect to the x axis).
		 * For each final data point we multiply the sum of its two constituent bytes by 256 (to convert to the units we want).
		 * The bytes are stored chronologically in the finalData array in the following order, with all samples in a given row 
		 * being from the same point in time:
		 * 
		 * 1. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2, mx1, mx2, my1, my2, mz1, mz2
		 * 2. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2
		 * 3. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2 
		 * 4. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2
		 * 5. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2  
		 * 6. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2 
		 * 7. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2 
		 * 8. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2
		 * 9. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2
		 * 10. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2
		 * 11. ax1, ax2, ay1, ay2, az1, az2, gx1, gx2, gy1, gy2, gz1, gz2, mx1, mx2, my1, my2, mz1, mz2
		 * ...
		 * 
		 * Because the magnetometer has 1/10th the sample rate of the accelerometer and gyroscope, it only has data for every 10th
		 * point on the time axis.
		 */
		

		/*
		Creates "dataSamples" 2D List in the following format:
		0 - Time
		1 - Acceleration X
		2 - Acceleration Y
		3 - Acceleration Z
		4 - Gyroscope (Angular Velocity) X
		5 - Gyroscope (Angular Velocity) Y
		6 - Gyroscope (Angular Velocity) Z
		7 - Magnetometer X
		8 - Magnetometer Y
		9 - Magnetometer Z
		*/ 
		List<List<Double>> dataSamples = new ArrayList<List<Double>>();
		
		// populate "dataSamples"'s inner lists
		for (int i = 0; i < 10; i++) {
			List<Double> axis = new ArrayList<Double>();
			dataSamples.add(axis);
		}

		// Populate acceleration x, y, and z
		for(int i = 0; i < finalData.length - 7; i+=7) {
			dataSamples.get(1).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(2).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(3).add((double)(finalData[i]*256)+finalData[i+1]); i+=1;
			
			//Starting at index 5, the counter must be incremented by an extra 6 every 126 bytes to account for the magnetometer data every 10th time index 
			if((i-5)%126==0) i+=6;		
		}

		// Populate gyroscope x, y, and z
		for (int i = 6; i < finalData.length - 7; i+=7) {
			dataSamples.get(4).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(5).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(6).add((double)(finalData[i]*256)+finalData[i+1]); i+=1;
			//Starting at index 11, the counter must be incremented by an extra 6 every 126 bytes to account for the magnetometer data every 10th time index
			if((i-11)%126==0) i+=6;	
		}
		
		// Populate magnetometer x, y, and z
		for (int i = 12; i<finalData.length - 121; i+=121) {
			dataSamples.get(7).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(8).add((double)(finalData[i]*256)+finalData[i+1]); i+=2; 
			dataSamples.get(9).add((double)(finalData[i]*256)+finalData[i+1]); i+=1;		
		}
		
		// Create time axis by mapping each sample to its ordinal position in the sample array divided by the sample rate
		// TODO may try to implement use of timer0 in future
		List<Double> timeAxis = new ArrayList<Double>();
		List<Double> magTimeAxis = new ArrayList<Double>();

		// using number of accelx samples as proxy for total number of samples
		for (int i = 0; i < dataSamples.get(1).size(); i++) {
			//populating dataSamples.get(0) and timeAxis for now, only one is necessary
			//dataSamples.get(0).add((double)i/((double)sampleRate));
			timeAxis.add((double)i/((double)sampleRate));

			if (i%10==0) {
				magTimeAxis.add((double)i/((double)magSampleRate)); 
			}

			// Pad time axis with two entries at the end to prevent misalignments in length with gyro samples (off by 1 or 2) 
			if (i == dataSamples.get(1).size()-1) {
				//dataSamples.get(0).add((double)i+1/((double)sampleRate));
				timeAxis.add((double)i+2/((double)sampleRate));
			}
		}
		
		// initialize axis data series
		axes = new AxisDataSeries[28];
		
		// loops X Y Z
		for (int i = 0; i < 3; i++) {
			
			// acceleration
			axes[i] = new AxisDataSeries(timeAxis, dataSamples.get(i+1), AxisType.valueOf(i), mpuOffsets, accelSensitivity, sampleRate);

			// velocity
			axes[i+4] = new AxisDataSeries(timeAxis, axes[i].integrate(), AxisType.valueOf(i+4), false, sampleRate);

			// displacement
			axes[i+8] = new AxisDataSeries(timeAxis, axes[i+4].integrate(), AxisType.valueOf(i+8), false, sampleRate);

			// (GYRO) angular velocity
			axes[i+16] = new AxisDataSeries(timeAxis, dataSamples.get(i+4), AxisType.valueOf(i+16), gyroSensitivity, sampleRate);

			// angular acceleration
			axes[i+12] = new AxisDataSeries(timeAxis, axes[i+16].differentiate(), AxisType.valueOf(i+12), false, sampleRate);	

			// angular displacement
			axes[i+20] = new AxisDataSeries(timeAxis, axes[i+16].integrate(), AxisType.valueOf(i+20), false, sampleRate);

			// magnetometer
			axes[i+24] = new AxisDataSeries(magTimeAxis, dataSamples.get(i+7), AxisType.valueOf(i+24), true, magSampleRate);

		}
	
		// Accel magnitude
		axes[3] = new AxisDataSeries(timeAxis, dataSamples.get(1), AxisType.valueOf(3), false, sampleRate); 

		// Velocity magnitude
		axes[7] = new AxisDataSeries(timeAxis, dataSamples.get(1), AxisType.valueOf(7), false, sampleRate);

		// Displacement magnitude
		axes[11] = new AxisDataSeries(timeAxis, dataSamples.get(1), AxisType.valueOf(11), false, sampleRate);

		// Angular accel magnitude
		axes[15] = new AxisDataSeries(timeAxis, dataSamples.get(3), AxisType.valueOf(15), false, sampleRate);

		// (GYRO) Angular velocity magnitude
		axes[19] = new AxisDataSeries(timeAxis, dataSamples.get(3), AxisType.valueOf(19), false, sampleRate);

		// Angular displacement magnitude
		axes[23] = new AxisDataSeries(timeAxis, dataSamples.get(3), AxisType.valueOf(23), false, sampleRate);

		// magnetic field magnitude
		axes[27] = new AxisDataSeries(magTimeAxis, dataSamples.get(7), AxisType.valueOf(27), true, magSampleRate);
		
		// Goes until length-1 to account for potential difference in length of 1 b/w accel and gyro series
		for (int i = 0; i < dataSamples.get(1).size()-1; i++) {

			axes[3].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[0].getSmoothedData()[i], 2)+Math.pow(axes[1].getSmoothedData()[i], 2)+Math.pow(axes[2].getSmoothedData()[i], 2)));
			axes[7].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[4].getSmoothedData()[i], 2)+Math.pow(axes[5].getSmoothedData()[i], 2)+Math.pow(axes[6].getSmoothedData()[i], 2)));
			axes[11].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[8].getSmoothedData()[i], 2)+Math.pow(axes[9].getSmoothedData()[i], 2)+Math.pow(axes[10].getSmoothedData()[i], 2)));
			axes[15].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[12].getSmoothedData()[i], 2)+Math.pow(axes[13].getSmoothedData()[i], 2)+Math.pow(axes[14].getSmoothedData()[i], 2)));
			axes[19].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[16].getSmoothedData()[i], 2)+Math.pow(axes[17].getSmoothedData()[i], 2)+Math.pow(axes[18].getSmoothedData()[i], 2)));
			axes[23].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[20].getSmoothedData()[i], 2)+Math.pow(axes[21].getSmoothedData()[i], 2)+Math.pow(axes[22].getSmoothedData()[i], 2)));

			//for adjusted magnetometer time scale
			if (i < dataSamples.get(7).size()) {
				axes[27].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[24].getSmoothedData()[i], 2)+Math.pow(axes[25].getSmoothedData()[i], 2)+Math.pow(axes[26].getSmoothedData()[i], 2)));
			} 
		
		}
				
	}
			
	public AxisDataSeries getAxis(AxisType axis) {
		return axes[axis.getValue()];
	}
				
}
