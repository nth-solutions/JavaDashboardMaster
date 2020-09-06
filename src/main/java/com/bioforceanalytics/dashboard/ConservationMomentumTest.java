package com.bioforceanalytics.dashboard;

public class ConservationMomentumTest extends TwoModuleTest {

	private double massRightModule;
	private double massLeftModule;
	private double massRightGlider;
	private double massLeftGlider;

	public ConservationMomentumTest(double massRightModule, double massLeftModule, double massRightGlider, double massLeftGlider) {
		this.massRightModule = massRightModule;
		this.massLeftModule = massLeftModule;
		this.massRightGlider = massRightGlider;
		this.massLeftGlider = massLeftGlider;
	}

	public void setupExperimentPanel(ExperimentPanel panel) {
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
		panel.setFormulaResult("m1v1 = m2v2");
	}

}
