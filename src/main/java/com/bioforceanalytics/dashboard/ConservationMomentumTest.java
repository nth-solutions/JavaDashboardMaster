package com.bioforceanalytics.dashboard;

import java.util.ArrayList;

public class ConservationMomentumTest {

	private double massRightModule;
	private double massLeftModule;
	private double massRightGlider;
	private double massLeftGlider;
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

	public void addModule(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax){
		if(moduleOne == null){
			moduleOne = new ConservationMomentumModule(testParameters,finalData, MPUMinMax,(massLeftGlider + massLeftModule),this);
			moduleOne.setGraphTitle("Conservation of Momentum");
			moduleOne.setDefaultAxes(new AxisType[] {AxisType.MomentumX});
		}else if (moduleTwo == null){
			moduleTwo = new ConservationMomentumModule(testParameters,finalData, MPUMinMax,(massRightGlider + massRightModule),this);
			moduleTwo.setGraphTitle("Conservation of Momentum");
			moduleTwo.setDefaultAxes(new AxisType[] {AxisType.MomentumX});
		}else{
			System.out.println("Conservation of Momentum Error");
		}

	}

	public ConservationMomentumTest(double massRightModule, double massLeftModule, double massRightGlider, double massLeftGlider) {
		this.massRightModule = massRightModule;
		this.massLeftModule = massLeftModule;
		this.massRightGlider = massRightGlider;
		this.massLeftGlider = massLeftGlider;

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
