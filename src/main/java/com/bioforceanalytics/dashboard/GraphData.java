package com.bioforceanalytics.dashboard;

import javafx.scene.chart.XYChart;

/**
 * Data type used to store information about currently graphed data sets.
 * Used by the BioForce Graph in {@link com.bioforceanalytics.dashboard.GraphNoSINCController GraphNoSINCController}
 * and {@link com.bioforceanalytics.dashboard.MultiAxisLineChart MultiAxisLineChart}.
 */
public class GraphData {

    public int GTIndex;
    public Axis axis;
    public XYChart.Series<Number,Number> data;

    public GraphData(int GTIndex, Axis axis, XYChart.Series<Number, Number> data) {
        this.GTIndex = GTIndex;
        this.axis = axis;
        this.data = data;
    }
    public GraphData(CustomAxisType customAxisType, XYChart.Series<Number,Number> data){
        this.axis = customAxisType;
        this.data = data;
    }
    public GraphData(int GTIndex, Axis axis){
        this.axis = axis;
        this.GTIndex = GTIndex;
    }

    @Override
    public String toString() {
        return "Test #" + (GTIndex + 1) + ", " + axis; 
    }

}