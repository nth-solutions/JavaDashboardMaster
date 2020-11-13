package com.bioforceanalytics.dashboard;

public class ConservationMomentumTest extends TwoModuleTest {

	private double massOfCart1;
	private double massOfCart2;

	public ConservationMomentumTest(double massOfCart1, double massOfCart2) {
		this.massOfCart1 = massOfCart1;
		this.massOfCart2 = massOfCart2;
	}

	public void setupExperimentPanel(ExperimentPanel panel) {
		panel.setExperimentName("Conservation of Momentum");
		panel.addParamName("Mass of Cart 1");
		panel.addParamValue(massOfCart1 +" kg");
		panel.addParamName("Mass of Cart 2");
		panel.addParamValue(massOfCart2 +" kg");
		panel.applyParams();
		panel.setFormulaResult("m1v1 = m2v2");
	}

}
