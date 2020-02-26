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

		for (int i = 1; i < 10; i++) {
			axes.add(new AxisDataSeries(d.getTimeAxis(), d.getDataSamples().get(i), AxisType.valueOf(i), d.getMPUOffsets(), d.accelSensitivity, d.gyroSensitivity));
		}
		
	}
	
	public ObservableList<XYChart.Series<Number,Number>> getAcc(AxisType axis) {
		return axes.get(axis.getValue()).createSeries();
	}
	
	public void getVel(AxisType axis) {}
	public void getPos(AxisType axis) {}
	public void getMagnitudeAccel() {}
	public void getMagnitudeGyro() {}
	
	public void getAngularAccel(AxisType axis) {}
	public void getAngularVel(AxisType axis) {}
	public void getAngularPos(AxisType axis) {}
	
	public void getMag(AxisType axis) {}
	

}
