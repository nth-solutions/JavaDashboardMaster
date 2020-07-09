package dataorganizer;

import java.util.ArrayList;

public class PhysicalPendulumTest extends GenericTest {

	private double pendulumLength;
	private double pivotDistance;
	private double moduleMass;
	private double pendulumMass;
	
	public PhysicalPendulumTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, double pendulumLength, double pivotDistance, double moduleMass, double pendulumMass) {
		super(testParameters, finalData, MPUMinMax);
		this.pendulumLength = pendulumLength;
		this.pivotDistance = pivotDistance;
		this.moduleMass = moduleMass;
		this.pendulumMass = pendulumMass;
		// TODO Auto-generated constructor stub
	}

}
