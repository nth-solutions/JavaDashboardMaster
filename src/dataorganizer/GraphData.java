package dataorganizer;

import javafx.scene.chart.XYChart;

/**
 * Data type used to store data about currently graphed data sets.
 * Used by the Data Analysis Graph in {@link dataorganizer.GraphNoSINCController GraphNoSINCController}
 * and {@link dataorganizer.MultipleAxesLineChart MultipleAxesLineChart}.
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