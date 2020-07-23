package dataorganizer;

import java.util.ArrayList;

public class InclinedPlaneTopTest extends GenericTest {

	double angle;

	public InclinedPlaneTopTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax,double angleFromTop) {
		super(testParameters, finalData, MPUMinMax);
		setGraphTitle("Inclined Plane from Top");
		setDefaultAxes(new AxisType[]{AxisType.AccelX});
		angle = angleFromTop;
		// TODO Auto-generated constructor stub
	}
	public void setupExperimentPanel(ExperimentPanel panel){
		panel.setExperimentName("Inclined Plane from Top");
		panel.addParamName("Angle of Inclination");
		panel.addParamValue(angle + "°");
		panel.applyParams();
		panel.setFormulaResult("Acceleration: " + 9.8 * Math.sin((angle/180) * Math.PI) + " m/s²");
	}

}
