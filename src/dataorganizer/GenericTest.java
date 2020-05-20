package dataorganizer;

import java.util.List;
import java.util.ArrayList;

public class GenericTest {
	
	private AxisDataSeries[] axes;
	
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

		axes = new AxisDataSeries[28];
		
		List<Double> timeAxis = new ArrayList<Double>();
		List<Double> magTimeAxis = new ArrayList<Double>();

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
	
		}
				
		//accel magnitude
		axes[3] = new AxisDataSeries(timeAxis, d.getDataSamples().get(0), AxisType.valueOf(3), false, d.getSampleRate()); 
		// velocity magnitude
		axes[7] = new AxisDataSeries(timeAxis, d.getDataSamples().get(0), AxisType.valueOf(7), false, d.getSampleRate());
		//displacement magnitude
		axes[11] = new AxisDataSeries(timeAxis, d.getDataSamples().get(0), AxisType.valueOf(11), false, d.getSampleRate());
		// Angular accel magnitude
		axes[15] = new AxisDataSeries(timeAxis, d.getDataSamples().get(3), AxisType.valueOf(15), false, d.getSampleRate());
		//(GYRO) Angular velocity magnitude
		axes[19] = new AxisDataSeries(timeAxis, d.getDataSamples().get(3), AxisType.valueOf(19), false, d.getSampleRate());
		// Angular displacement magnitude
		axes[23] = new AxisDataSeries(timeAxis, d.getDataSamples().get(3), AxisType.valueOf(23), false, d.getSampleRate());
		// magnetic field magnitude
		axes[27] = new AxisDataSeries(magTimeAxis, d.getDataSamples().get(7), AxisType.valueOf(27), false, d.getMagSampleRate());
		
		for (int i = 0; i < d.getDataSamples().size(); i++) {
			
			// accel magnitude
			axes[3].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[0].getSmoothedData()[i], 2)+Math.pow(axes[1].getSmoothedData()[i], 2)+Math.pow(axes[2].getSmoothedData()[i], 2)));
			axes[7].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[4].getSmoothedData()[i], 2)+Math.pow(axes[5].getSmoothedData()[i], 2)+Math.pow(axes[6].getSmoothedData()[i], 2)));
			axes[11].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[8].getSmoothedData()[i], 2)+Math.pow(axes[9].getSmoothedData()[i], 2)+Math.pow(axes[10].getSmoothedData()[i], 2)));
			axes[15].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[12].getSmoothedData()[i], 2)+Math.pow(axes[13].getSmoothedData()[i], 2)+Math.pow(axes[14].getSmoothedData()[i], 2)));
			axes[19].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[16].getSmoothedData()[i], 2)+Math.pow(axes[17].getSmoothedData()[i], 2)+Math.pow(axes[18].getSmoothedData()[i], 2)));
			axes[23].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[20].getSmoothedData()[i], 2)+Math.pow(axes[21].getSmoothedData()[i], 2)+Math.pow(axes[22].getSmoothedData()[i], 2)));
			
			//for adjusted magnetometer time scale // TODO NEEDS FIXED
			if (i % 10 == 0) {
				axes[27].setOriginalDataPoint(i, Math.sqrt(Math.pow(axes[24].getSmoothedData()[i], 2)+Math.pow(axes[25].getSmoothedData()[i], 2)+Math.pow(axes[26].getSmoothedData()[i], 2)));
			} 
		}
	}
	
	public AxisDataSeries getAxis(AxisType axis) {
		return axes[axis.getValue()];
	}

}

