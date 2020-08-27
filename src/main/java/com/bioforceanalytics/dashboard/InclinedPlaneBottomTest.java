package com.bioforceanalytics.dashboard;

import java.util.ArrayList;

public class InclinedPlaneBottomTest extends GenericTest {

	double angle;

	public InclinedPlaneBottomTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, double angleFromBottom) {
		super(testParameters, finalData, MPUMinMax);
		setGraphTitle("Inclined Plane from Bottom");
		setDefaultAxes(AxisType.AccelX);
		angle = angleFromBottom;
	}

	public void setupExperimentPanel(ExperimentPanel panel) {
		panel.setExperimentName("Inclined Plane from Bottom");
		panel.addParamName("Angle of Inclination");
		panel.addParamValue(angle + "°");
		panel.applyParams();
		panel.setFormulaResult("Acceleration: " + 9.8 * Math.sin((angle/180) * Math.PI) + " m/s²");
	}
}
