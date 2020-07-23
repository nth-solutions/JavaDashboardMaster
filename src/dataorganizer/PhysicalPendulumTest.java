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
		setGraphTitle("Physical Pendulum");
		setDefaultAxes(new AxisType[]{AxisType.AngDispX});
		// TODO Auto-generated constructor stub
	}

	public void setupExperimentPanel(ExperimentPanel panel){
		panel.setExperimentName("Physical Pendulum");
		panel.addParamName("Pendulum Length");
		panel.addParamValue(pendulumLength +" m");
		panel.addParamName("Pivot Distance");
		panel.addParamValue(pivotDistance +" m");
		panel.addParamName("Mass of the Module");
		panel.addParamValue(moduleMass +" kg");
		panel.addParamName("Mass of the Pendulum");
		panel.addParamValue(pendulumMass +" kg");
		panel.applyParams();
	}

}
