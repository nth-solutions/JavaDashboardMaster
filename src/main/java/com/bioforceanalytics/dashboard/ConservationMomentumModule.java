package com.bioforceanalytics.dashboard;

import java.util.ArrayList;

public class ConservationMomentumModule extends GenericTest{

    private ConservationMomentumTest controller;
    private double momentumScalar;
    public ConservationMomentumTest getController(){
        return controller;
    }
    public double getMomentumScalar(){
        return momentumScalar;
    }

	public ConservationMomentumModule(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, double mass,ConservationMomentumTest controller) {
        super(testParameters,finalData,MPUMinMax);
        this.controller = controller;
        momentumScalar = mass;
        this.createAxisDataSeries();
		// TODO Auto-generated constructor stub
	}

}
