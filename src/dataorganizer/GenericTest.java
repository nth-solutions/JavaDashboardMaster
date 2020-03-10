package dataorganizer;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class GenericTest {
	
	private DataOrganizer dataOrg;
	private AxisDataSeries[] axes;
	
	public GenericTest(DataOrganizer d) {
		
		this.dataOrg = d;
/************************************************create axis data series for all axes********************************************************************************************/
		axes = new AxisDataSeries[28];
		
		// scale time axis for magnetometer by 10
		List<Double> magTimeAxis = d.getTimeAxis().stream().map(n -> n * 10).collect(Collectors.toList());
		
		// loops X Y Z
		for (int i = 0; i < 3; i++) {
			
			// acceleration
			axes[i] = new AxisDataSeries(d.getTimeAxis(), d.getDataSamples().get(i), AxisType.valueOf(i), d.getMPUOffsets(), d.accelSensitivity, d.getSampleRate());
			
			// velocity
			axes[i+4] = new AxisDataSeries(d.getTimeAxis(), axes[i].integrate(), AxisType.valueOf(i+4), false, d.getSampleRate());
			
			// displacement
			axes[i+8] = new AxisDataSeries(d.getTimeAxis(), axes[i+4].integrate(), AxisType.valueOf(i+8), false, d.getSampleRate());
			
			// (GYRO) angular velocity
			axes[i+16] = new AxisDataSeries(d.getTimeAxis(), d.getDataSamples().get(i+3), AxisType.valueOf(i+16), d.gyroSensitivity, d.getSampleRate());
			
			// angular acceleration
			axes[i+12] = new AxisDataSeries(d.getTimeAxis(), axes[i+16].differentiate(), AxisType.valueOf(i+12), false, d.getSampleRate());	
			
			// (angular displacement
			axes[i+20] = new AxisDataSeries(d.getTimeAxis(), axes[i+16].integrate(), AxisType.valueOf(i+20), false, d.getSampleRate());
			
			// magnetometer
			axes[i+24] = new AxisDataSeries(magTimeAxis, d.getDataSamples().get(i+6), AxisType.valueOf(i+24), true, d.getMagSampleRate());
	
		}
		/************************************************magnitude series********************************************************************************************/		
		//accel magnitude
		axes[3] = new AxisDataSeries(d.getTimeAxis(), d.getDataSamples().get(0), AxisType.valueOf(3), d.getMPUOffsets(), d.accelSensitivity, d.getSampleRate()); 
		// velocity magnitude
		axes[7] = new AxisDataSeries(d.getTimeAxis(), d.getDataSamples().get(0), AxisType.valueOf(7), d.getMPUOffsets(), d.accelSensitivity, d.getSampleRate());
		//displacement magnitude
		axes[11] = new AxisDataSeries(d.getTimeAxis(), d.getDataSamples().get(0), AxisType.valueOf(11), d.getMPUOffsets(), d.accelSensitivity, d.getSampleRate());
		// Angular accel magnitude
		axes[15] = new AxisDataSeries(d.getTimeAxis(), d.getDataSamples().get(3), AxisType.valueOf(15), d.gyroSensitivity, d.getSampleRate());
		//(GYRO) Angular velocity magnitude
		axes[19] = new AxisDataSeries(d.getTimeAxis(), d.getDataSamples().get(3), AxisType.valueOf(19), d.gyroSensitivity, d.getSampleRate());
		// Angular displacement magnitude
		axes[23] = new AxisDataSeries(d.getTimeAxis(), d.getDataSamples().get(3), AxisType.valueOf(23), d.gyroSensitivity, d.getSampleRate());
		// magnetic field magnitude
		axes[27] = new AxisDataSeries(magTimeAxis, d.getDataSamples().get(6), AxisType.valueOf(27), true, d.getMagSampleRate());
		
		for (int i = 0; i < d.getDataSamples().size(); i++) {
			
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
	
	public ObservableList<XYChart.Series<Number,Number>> getSeries(AxisType axis) {
		return axes[axis.getValue()].createSeries();
	}

}

