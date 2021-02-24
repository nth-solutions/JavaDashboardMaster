package com.bioforceanalytics.dashboard;

public class ConservationEnergyTest extends TwoModuleTest {

	private double mass;
	private double momentOfInertia;
	private double distanceDropped;

	public ConservationEnergyTest(double mass, double momentOfInertia, double distanceDropped) {

		this.mass = mass;
		this.momentOfInertia = momentOfInertia;
		this.distanceDropped = distanceDropped;

	}

	@Override
	public int addModule(GenericTest test) {

		int moduleNumber = super.addModule(test);

		test.setGraphTitle("Conservation of Energy");

		if (moduleNumber == 1) {
			test.setDefaultAxes(AxisType.DispY);
		}
		else if (moduleNumber == 2) {
			test.setDefaultAxes(AxisType.AngVelZ);
		}

		return moduleNumber;

	}

	public void setupExperimentPanel(ExperimentPanel panel) {

		panel.setExperimentName("Conservation of Energy");

		panel.addParamName("Mass");
		panel.addParamValue(mass + " kg");

		panel.addParamName("Moment of Inertia");
		panel.addParamValue(momentOfInertia + " kg-m²");

		panel.addParamName("Distance Dropped");
		panel.addParamValue(distanceDropped + " m");

		panel.applyParams();
		panel.setFormulaResult("Total Energy: " + mass * distanceDropped * 9.8 + " J");

	}


}
