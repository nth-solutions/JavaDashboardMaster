package dataorganizer;

import java.util.ArrayList;

public class ConservationEnergyTest extends GenericTest {

	private double mass;
	private double momentOfInertia;
	private double radiusOfArm;
	private double distanceDropped;
	public ConservationEnergyTest(ArrayList<Integer> testParameters, int[] finalData, int[][] MPUMinMax, double mass, double momentOfInertia, double radiusOfArm, double distanceDropped) {
		super(testParameters, finalData, MPUMinMax);
		// TODO Auto-generated constructor stub
		this.mass = mass;
		this.momentOfInertia = momentOfInertia;
		this.radiusOfArm = radiusOfArm;
		this.distanceDropped = distanceDropped;
		setGraphTitle("Conservation of Energy");
		setDefaultAxes(new AxisType[] {AxisType.AccelX});
	}
	public void setupExperimentPanel(ExperimentPanel panel){
		panel.setExperimentName("Conservation of Energy");
		panel.addParamName("Mass");
		panel.addParamValue(mass +" kg");
		panel.addParamName("Moment of Inertia");
		panel.addParamValue(momentOfInertia +" kg-m²");
		panel.addParamName("Radius of Torque Arm");
		panel.addParamValue(radiusOfArm +" m");
		panel.addParamName("Distance Dropped");
		panel.addParamValue(distanceDropped +" m");
		panel.applyParams();
		panel.setFormulaResult("Total Energy: " + mass * distanceDropped * 9.8 + " J");
	}


}
