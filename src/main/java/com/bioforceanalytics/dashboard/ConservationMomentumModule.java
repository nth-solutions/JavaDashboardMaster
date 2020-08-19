package com.bioforceanalytics.dashboard;

import java.util.ArrayList;

public class ConservationMomentumModule extends GenericTest{

    private ConservationMomentumTest controller;

    public ConservationMomentumTest getController(){
        return controller;
    }

	public ConservationMomentumModule(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, double mass,ConservationMomentumTest controller) {
        super(testParameters,finalData,MPUMinMax,mass);
		this.controller = controller;
		// TODO Auto-generated constructor stub
	}

}
