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
		setGraphTitle("Conservation of Energy");
		setDefaultAxes(new AxisType[] {AxisType.AccelX});
	}
	public void setupExperimentPanel(ExperimentPanel panel){
		panel.setExperimentName("Conservation of Energy");
		panel.addParamName("Mass");
		panel.addParamValue(mass +" kg");
		panel.addParamName("Moment of Inertia");
		panel.addParamValue(momentOfInertia +" kg-m²");

		panel.applyParams();
	}

}
