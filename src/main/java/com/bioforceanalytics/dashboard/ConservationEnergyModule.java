package com.bioforceanalytics.dashboard;

import java.util.ArrayList;

public class ConservationEnergyModule extends GenericTest {

    private ConservationEnergyTest controller;

    public ConservationEnergyTest getController() {
        return controller;
    }


	public ConservationEnergyModule(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, ConservationEnergyTest controller) {
        
        super(testParameters, finalData, MPUMinMax);
		this.controller = controller;
        
        this.setGraphTitle("Conservation of Energy");

	}

}
