package dataorganizer;

import java.util.ArrayList;

public class InclinedPlaneBottomTest extends GenericTest {

	public InclinedPlaneBottomTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax) {
		super(testParameters, finalData, MPUMinMax);
<<<<<<< HEAD
		setGraphTitle("Inclined Plane from");
		setDefaultAxes(new AxisType[]{AxisType.AccelX});
		// TODO Auto-generated constructor stub
=======
>>>>>>> cb2fd1b1379deea8fe74b68ea2ad97ad5b9c4431
	}
	public void setupExperimentPanel(ExperimentPanel panel){
		panel.setExperimentName("Inclined Plane from Bottom");
		panel.applyParams();
	}
}
