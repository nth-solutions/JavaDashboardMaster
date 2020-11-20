package com.bioforceanalytics.dashboard;

import java.util.ArrayList;

public class InclinedPlaneTest extends GenericTest {

	double angle;

	public InclinedPlaneTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, double angleFromTop) {
		super(testParameters, finalData, MPUMinMax);
		setGraphTitle("Inclined Plane from Top");
		setDefaultAxes(AxisType.AccelX);
		angle = angleFromTop;
	}

	public void setupExperimentPanel(ExperimentPanel panel) {
		panel.setExperimentName("Inclined Plane from Top");
		panel.addParamName("Angle of Inclination");
		panel.addParamValue(angle + "°");
		panel.applyParams();
		panel.setFormulaResult("Acceleration: " + 9.8 * Math.sin((angle/180) * Math.PI) + " m/s²");
	}

}
