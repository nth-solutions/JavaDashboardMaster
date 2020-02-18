package dataorganizer;

import java.util.Dictionary;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class GenericTest {
	
	private DataOrganizer dataOrg;
	//private Dictionary<Integer, String> axesDict;
	private List<AxisDataSeries> axes;
	
	public GenericTest(DataOrganizer d) {
		
		this.dataOrg = d;
		
		/*
		axesDict.put(0, "Time");
		axesDict.put(1, "Accel X");
		axesDict.put(2, "Accel Y");
		axesDict.put(3, "Accel Z");
		axesDict.put(4, "Gyro X");
		axesDict.put(5, "Gyro Y");
		axesDict.put(6, "Gyro Z");
		axesDict.put(7, "Mag X");
		axesDict.put(8, "Mag Y");
		axesDict.put(9, "Mag Z");
		*/

		for (int i = 1; i < 10; i++) {
			axes.add(new AxisDataSeries(d.getTimeAxis(), d.getDataSamples().get(i), Axis.valueOf(i)));
		}
		
	}
	
	// TODO replace void with return type of vector data
	
	public ObservableList<XYChart.Series<Number,Number>> getAccel(Axis axis) {
		return axes.get(axis.getValue()).createSeries();
	}
	public void getVel(String axis) {}
	public void getPos(String axis) {}
	public void getMagnitudeAccel() {}
	public void getMagnitudeGyro() {}
	
	public void getAngularAccel(String axis) {}
	public void getAngularVel(String axis) {}
	public void getAngularPos(String axis) {}
	
	public void getMag(String axis) {}
	
}
