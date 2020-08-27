package com.bioforceanalytics.dashboard;

import java.util.ArrayList;

public class ConservationEnergyTest{

	private double mass;
	private double momentOfInertia;
	private double radiusOfArm;
	private double distanceDropped;
	private GenericTest moduleOne;
	private GenericTest moduleTwo;

	public boolean isFilled(){
		return (moduleOne != null && moduleTwo != null);
	}

	public GenericTest getModuleOne(){
		return moduleOne;
	}
	public GenericTest getModuleTwo(){
		return moduleTwo;
	}

	public void addModule(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax) {

		if (moduleOne == null) {
			moduleOne = new ConservationEnergyModule(testParameters,finalData, MPUMinMax,(mass),this);
			moduleOne.setGraphTitle("Conservation of Energy");
			moduleOne.setDefaultAxes(AxisType.DispY);
		} else if (moduleTwo == null) {
			moduleTwo = new ConservationEnergyModule(testParameters, finalData, MPUMinMax, (radiusOfArm * momentOfInertia), this);
			moduleTwo.setGraphTitle("Conservation of Energy");
			moduleTwo.setDefaultAxes(AxisType.AngVelZ);
		} else {
			System.out.println("Conservation of Energy Error");
		}

	}
	public ConservationEnergyTest(double mass, double momentOfInertia, double radiusOfArm, double distanceDropped) {

		this.mass = mass;
		this.momentOfInertia = momentOfInertia;
		this.radiusOfArm = radiusOfArm;
		this.distanceDropped = distanceDropped;

	}

	public void setupExperimentPanel(ExperimentPanel panel){
		panel.setExperimentName("Conservation of Energy");
		panel.addParamName("Mass");
		panel.addParamValue(mass +" kg");
		panel.addParamName("Moment of Inertia");
		panel.addParamValue(momentOfInertia +" kg-m²");
		panel.addParamName("Radius of Torque Arm");
		panel.addParamValue(radiusOfArm +" m");
		panel.addParamName("Distance Dropped");
		panel.addParamValue(distanceDropped +" m");
		panel.applyParams();
		panel.setFormulaResult("Total Energy: " + mass * distanceDropped * 9.8 + " J");
	}


}
