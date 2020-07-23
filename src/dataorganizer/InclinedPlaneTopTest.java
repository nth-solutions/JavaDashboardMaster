package dataorganizer;

import java.util.ArrayList;

public class InclinedPlaneTopTest extends GenericTest {

	public InclinedPlaneTopTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax) {
		super(testParameters, finalData, MPUMinMax);
		setGraphTitle("Inclined Plane from Top");
		setDefaultAxes(new AxisType[]{AxisType.AccelX});
		// TODO Auto-generated constructor stub
	}
	public void setupExperimentPanel(ExperimentPanel panel){
		panel.setExperimentName("Inclined Plane from Top");
		panel.applyParams();
	}

}
