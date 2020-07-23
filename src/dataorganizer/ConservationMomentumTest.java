package dataorganizer;

import java.util.ArrayList;

public class ConservationMomentumTest extends GenericTest {

	private double massRightModule;
	private double massLeftModule;
	private double massRightGlider;
	private double massLeftGlider;
	
	public ConservationMomentumTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, double massRightModule, double massLeftModule, double massRightGlider, double massLeftGlider) {
		super(testParameters, finalData, MPUMinMax);
		this.massRightModule = massRightModule;
		this.massLeftModule = massLeftModule;
		this.massRightGlider = massRightGlider;
		this.massLeftGlider = massLeftGlider;
		setGraphTitle("Conservation of Momentum");
		setDefaultAxes(new AxisType[] {AxisType.VelX});
		// TODO Auto-generated constructor stub
	}
	public void setupExperimentPanel(ExperimentPanel panel){
		panel.setExperimentName("Conservation of Momentum");
		panel.addParamName("Mass of Right Module");
		panel.addParamValue(massRightModule +" kg");
		panel.addParamName("Mass of Left Module");
		panel.addParamValue(massLeftModule +" kg");
		panel.addParamName("Mass of the Right Glider");
		panel.addParamValue(massRightGlider +" kg");
		panel.addParamName("Mass of the Left Glider");
		panel.addParamValue(massLeftGlider +" kg");
		panel.applyParams();
		panel.setFormulaResult("Conservation of Momentum experiment is still a WIP");
	}
}
