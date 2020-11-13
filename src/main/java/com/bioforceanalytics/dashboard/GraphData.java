package com.bioforceanalytics.dashboard;

import javafx.scene.chart.XYChart;

/**
 * Data type used to store information about currently graphed data sets.
 * Used by the BioForce Graph in {@link com.bioforceanalytics.dashboard.GraphNoSINCController GraphNoSINCController}
 * and {@link com.bioforceanalytics.dashboard.MultiAxisLineChart MultiAxisLineChart}.
 */
public class GraphData {

    public int GTIndex;
    public AxisType axis;
    public XYChart.Series<Number,Number> data;

    public GraphData(int GTIndex, AxisType axis, XYChart.Series<Number, Number> data) {
        this.GTIndex = GTIndex;
        this.axis = axis;
        this.data = data;
    }

}