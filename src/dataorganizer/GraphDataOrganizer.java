package dataorganizer;

import java.util.ArrayList;
import java.util.List;

public class GraphDataOrganizer {

	public String[] seriesName;
	public String origin;
	private List<Double> timeAxis;
	public List<List<Double>> samples;
	private int sampleRate;
	private int magSampleRate;
	private int magInterval;
	public int accelSensitivity;
	public int gyroSensitivity;
	private int magSensitivity;
	private double lengthOfTest;
	public int yMin;
	public int yMax;
	
	public void setOrigin(String newOrigin) {
		origin = newOrigin;
	}
	
	public void setSeriesName(String[] name) {
		seriesName = name;
	}
	
	public String getSeriesName(int index) {
		return origin+seriesName[index];
	}
	
	public List<Double> createTimeAxis(double startTime){
		timeAxis = new ArrayList<Double>();
		timeAxis.add(0, 0.0);
		for(int i = 1; i < samples.get(0).size(); i++) {
			timeAxis.add(i, (double)i/sampleRate + startTime*sampleRate);
		}
		return timeAxis;
	}
	
	public void setMinMaxYAxis() {
		getMin();
		getMax();
	}
	
	public int getMin() {
		for(int i = 0; i < samples.size(); i++) {
			for(int j = 0; j < samples.get(i).size(); j++) {
				if(samples.get(i).get(j) < yMin)
					yMin = samples.get(i).get(j).intValue() - 3;
			}
		}
		return yMin;
	}
	
	public int getMax() {
		for(int i = 0; i < samples.size(); i++) {
			for(int j = 0; j < samples.get(i).size(); j++) {
				if(samples.get(i).get(j) > yMax)
					yMax = samples.get(i).get(j).intValue() + 3;
			}
		}
		return yMax;
	}
	
	public List<List<Double>> updateZoom(double start, double end,  int dofNum){
		int numSamples = (int) Math.round((end - start) * sampleRate);

		double rate = 7000.0 / (double) numSamples;
		double newSps = (sampleRate * rate);
		double modifier = sampleRate / newSps;

		List<List<Double>> dofData = new ArrayList<List<Double>>();

		ArrayList<Double> dofTime = new ArrayList<Double>();
		List<Double> dofAxis = new ArrayList<Double>();

		List<Double> tempDofTime = createTimeAxis(start);
		
		if (modifier < 1)
			modifier = 1;
		for (int sample = 0; sample < 7000 && ((start * sampleRate) + sample*modifier) < (tempDofTime.size() - 1); sample++) {
			dofTime.add(sample, tempDofTime.get((int) ((start * sampleRate) + (int) (sample * modifier))));
		}

		dofData.add(0, dofTime);

		for (int sample = 0; sample < 7000 && (start * sampleRate) + (int) (sample * modifier) < (samples.get(dofNum).size() - 1); sample++) {
			dofAxis.add(sample, samples.get(dofNum).get((int) ((start * sampleRate) + (int) (sample * modifier))));
		}

		dofData.add(1, dofAxis);

		return dofData;
	}
	
	public List<List<Double>> rollingBlock(int rollRange, int dof) {
		List<List<Double>> modifiedDataSmps = new ArrayList<List<Double>>();
		modifiedDataSmps = samples;
		
		for(int j = 0; j < modifiedDataSmps.get(dof).size(); j++) { //iterate smps
			double avg = 0.0;
			for(int i = 0; i < rollRange && i+j < modifiedDataSmps.get(dof).size(); i++) { //Sum 10 smps, do not exceed size of dof data size
				avg += modifiedDataSmps.get(dof).get(j+i);
			}
			avg = avg/rollRange; //divide for average
			modifiedDataSmps.get(dof).set(j, avg);
		}

		return modifiedDataSmps;
	}
	
	public List<List<Double>> getZoomedSeries(double start, int dofNum){
		int numSamples = (int) Math.round((lengthOfTest - start) * sampleRate);

		double rate = 7000.0 / (double) numSamples;
		double newSps = (sampleRate * rate);
		double modifier = sampleRate / newSps;
		
		List<List<Double>> dofData = new ArrayList<List<Double>>();

		timeAxis = new ArrayList<Double>();
		List<Double> dofAxis = new ArrayList<Double>();

		if (modifier < 1)
			modifier = 1;
		timeAxis = createTimeAxis(start); 
		
		dofData.add(0, timeAxis);

		dofData.add(1, samples.get(dofNum));
		
		return dofData;
	}
	
	public void printSeries(List<List<Double>> seriesData){
		for(int i = 0; i < seriesData.get(0).size(); i++) {
			System.out.println(seriesData.get(0).get(i) +" : " + seriesData.get(1).get(i));
		}
	}
	
	public double getLengthOfTest() {
		return lengthOfTest;
	}
	
	public void setSamples(List<List<Double>> newSamples) {
		samples = newSamples;
	}
	
	public void setTestParams(List<Integer> testParameters) {
		sampleRate = testParameters.get(7);
		lengthOfTest = testParameters.get(6);
		magSampleRate = testParameters.get(8);
		accelSensitivity = testParameters.get(9);
		gyroSensitivity = testParameters.get(10);
		magSensitivity = 4800;
		magInterval = sampleRate / magSampleRate;
	}
	
	public void addNulls(int xOffsetCounter) {
		if(xOffsetCounter > 0) {
			for(int i = 0; i < xOffsetCounter; i++) {
				samples.get(0).add(i, null);
				samples.get(1).add(i, null);
				samples.get(2).add(i, null); 
			}
		}
		else {
			for(int i = 0; i < xOffsetCounter; i++) {
				samples.get(0).remove(0);
				samples.get(1).remove(0);
				samples.get(2).remove(0);
			}
		}
	}
}
