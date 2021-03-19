package com.bioforceanalytics.dashboard;

public class ConservationMomentumTest extends TwoModuleTest {

	private double massOfCart1;
	private double massOfCart2;

	public ConservationMomentumTest(double massOfCart1, double massOfCart2) {
		this.massOfCart1 = massOfCart1;
		this.massOfCart2 = massOfCart2;
	}
	public AxisDataSeries[] getMomentumAxes(){
		AxisDataSeries[] series = new AxisDataSeries[8];
		for(int i = 0; i < 4; i++){
			//series[i] = getModuleOne().getAxis(AxisType.valueOf(i)).add(getModuleOne().getAxis(AxisType.valueOf(i)));
			/*
			series[i] = new AxisDataSeries(getModuleOne().getAxis(AxisType.valueOf(i)).getTime(),
			getModuleOne().getAxis(AxisType.valueOf(i)).integrate(((ConservationMomentumModule)getModuleOne()).getMomentumScalar())
			, false, getModuleOne().getSampleRate());
			*/
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
		panel.addParamName("Mass of Cart 1");
		panel.addParamValue(massOfCart1 +" kg");
		panel.addParamName("Mass of Cart 2");
		panel.addParamValue(massOfCart2 +" kg");
		panel.applyParams();
		panel.setFormulaResult("m1v1 = m2v2");
	}

}
