package dataorganizer;

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
	}

}
