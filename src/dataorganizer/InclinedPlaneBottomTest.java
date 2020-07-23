package dataorganizer;

import java.util.ArrayList;

public class InclinedPlaneBottomTest extends GenericTest {

	public InclinedPlaneBottomTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax) {
		super(testParameters, finalData, MPUMinMax);
		setGraphTitle("Inclined Plane from");
		setDefaultAxes(new AxisType[]{AxisType.AccelX});
		// TODO Auto-generated constructor stub
	}
	public void setupExperimentPanel(ExperimentPanel panel){
		panel.setExperimentName("Inclined Plane from Bottom");
		panel.applyParams();
	}
}
