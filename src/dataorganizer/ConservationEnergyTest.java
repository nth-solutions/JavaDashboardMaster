package dataorganizer;

import java.util.ArrayList;

public class ConservationEnergyTest extends GenericTest {

	private double mass;
	private double momentOfInertia;
	
	public ConservationEnergyTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, double mass, double momentOfInertia) {
		super(testParameters, finalData, MPUMinMax);
		// TODO Auto-generated constructor stub
		this.mass = mass;
		this.momentOfInertia = momentOfInertia;
	}

}
