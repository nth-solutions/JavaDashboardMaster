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
	public AxisDataSeries[] getMomentumAxes(){
		AxisDataSeries[] series = new AxisDataSeries[8];
		for(int i = 0; i < 4; i++){
			series[i] = new AxisDataSeries(getModuleOne().getAxis(AxisType.valueOf(i)).getTime(),
			getModuleOne().getAxis(AxisType.valueOf(i)).integrate(((ConservationMomentumModule)getModuleOne()).getMomentumScalar())
			, false, getModuleOne().getSampleRate());
		}
		for(int i = 0; i < 4; i++){
			series[i+4] = new AxisDataSeries(getModuleTwo().getAxis(AxisType.valueOf(i)).getTime(),
			getModuleTwo().getAxis(AxisType.valueOf(i)).integrate(((ConservationMomentumModule)getModuleTwo()).getMomentumScalar()),
			 false, getModuleTwo().getSampleRate());
		}
		return series;
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
