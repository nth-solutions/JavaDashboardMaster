package dataorganizer;

import java.util.ArrayList;

public class ConservationMomentumTest extends GenericTest {

	private double massRightModule;
	private double massLeftModule;
	private double massRightGlider;
	private double massLeftGlider;
	
	public ConservationMomentumTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, double massRightModule, double massLeftModule, double massRightGlider, double massLeftGlider) {
		super(testParameters, finalData, MPUMinMax);
		this.massRightModule = massRightModule;
		this.massLeftModule = massLeftModule;
		this.massRightGlider = massRightGlider;
		this.massLeftGlider = massLeftGlider;
		// TODO Auto-generated constructor stub
	}

}
