package com.bioforceanalytics.dashboard;

import java.util.ArrayList;

public class SpringTest extends GenericTest {

	private double springConstant;
	private double totalMass;
	private double amplitude;
	private double springMass;
	
	public SpringTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, double springConstant, double totalMass, double amplitude, double springMass) {
		super(testParameters, finalData, MPUMinMax);
		this.springConstant = springConstant;
		this.totalMass = totalMass;
		this.amplitude = amplitude;
		this.springMass = springMass;
		setGraphTitle("Spring Test");
		setDefaultAxes(new AxisType[] {AxisType.AccelX});
		// TODO Auto-generated constructor stub
	}
	public void setupExperimentPanel(ExperimentPanel panel){
		panel.setExperimentName("Spring Test");
		panel.addParamName("Spring Constant");
		panel.addParamValue(springConstant +" N/m");
		panel.addParamName("Total Mass");
		panel.addParamValue(totalMass +" kg");
		panel.addParamName("Spring Amplitude");
		panel.addParamValue(amplitude +" m");
		panel.addParamName("Mass of the Spring");
		panel.addParamValue(springMass +" kg");
		panel.applyParams();
		panel.setFormulaResult("Period: " + 2 * Math.PI * Math.sqrt((totalMass + springMass)/springConstant) + " s");
	}
}
