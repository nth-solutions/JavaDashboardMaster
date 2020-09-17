package com.bioforceanalytics.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class MultipleAxesLineChart extends StackPane {

    private final BFALineChart<Number, Number> baseChart;
    private final ObservableList<BFALineChart<Number, Number>> backgroundCharts = FXCollections.observableArrayList();

    /**
     * Tracks the axis units and their respective line charts holding the y-axes.
     */
    private final Map<String, BFALineChart<Number, Number>> axisUnitMap = new HashMap<>();

    /**
     * Tracks the custom axis types and their respective line charts holding the y-axes
     */
    //private final Map<CustomAxisType, BFALineChart<Number,Number>> customAxisTypeMap = new HashMap<>();

    /**
     * Tracks the currently drawn data sets and their respective line charts.
     */
    public final Map<GraphData, BFALineChart<Number, Number>> axisChartMap = new HashMap<>();
    
    // keeps track of currently graphed data sets (GTIndex/AxisType/data)
    private ArrayList<GraphData> dataSets;

    private BFANumberAxis xAxis;
    private BFANumberAxis yAxis;

    private final double yAxisWidth = 60;
    private final double yAxisSeparation = 20;
    private final double xAxisHeight = 30;
    private static final Logger logger = LogManager.getLogger();

    public MultipleAxesLineChart() {
        this(null);
    }

    /**
     * Returns the amount that the data set's graph should be scaled by.
     * This is used for angular and magnetometer data sets.
     * @param axis the AxisType representing the data set
     * @return the amount that the data set's graph should be scaled by
     */
    public double getAxisScalar(Axis axis) {
        return axis.getAxisScalar();

    }

    public MultipleAxesLineChart(BFALineChart<Number, Number> baseChart) {
        
        super();
        setPickOnBounds(false);
        dataSets = new ArrayList<GraphData>();

        if (baseChart == null) {

            xAxis = new BFANumberAxis();
            yAxis = new BFANumberAxis();
            xAxis.setTickUnit(1);
            yAxis.setTickUnit(1);
            
            baseChart = new BFALineChart<Number, Number>(xAxis, yAxis);
            baseChart.getXAxis().setLabel("Time (s)");
            baseChart.getYAxis().setLabel("Y Axis");

        }

        this.baseChart = baseChart;

        styleBaseChart(baseChart);
        setFixedAxisWidth(baseChart);

        setAlignment(Pos.CENTER_LEFT);

        backgroundCharts.addListener((Observable observable) -> rebuildChart());

        rebuildChart();
    }

    public BFALineChart<Number, Number> getBaseChart() {
        return baseChart;
    }

    public boolean isBaseEmpty() {
        return (baseChart.getData().size() == 0);
    }

    private void styleBaseChart(BFALineChart<Number, Number> baseChart) {
        baseChart.setCreateSymbols(false);
        baseChart.setLegendVisible(true);
        baseChart.getXAxis().setAutoRanging(false);
        baseChart.getYAxis().setAutoRanging(false);
        baseChart.getXAxis().setAnimated(false);
        baseChart.getYAxis().setAnimated(false);
        baseChart.getYAxis().setOpacity(0.0);

    }

    private void setFixedAxisWidth(BFALineChart<Number, Number> chart) {
        chart.getYAxis().setPrefWidth(yAxisWidth);
        chart.getYAxis().setMaxWidth(yAxisWidth);
    }

    public void setXBounds(double lowerBound, double upperBound) {
        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);
    }

    public void setYBounds(double lowerBound, double upperBound) {
        yAxis.setLowerBound(lowerBound);
        yAxis.setUpperBound(upperBound);
    }

    private void rebuildChart() {

        // resize the base chart and add it the multi axis chart if necessary
        if (getChildren().contains(baseChart)) {
            resizeBaseChart(baseChart);
        } else {
            resizeBaseChart(baseChart);
            getChildren().add(baseChart);
        }

        // loop through each background chart
        for (BFALineChart<Number, Number> lineChart : backgroundCharts) {

            // resize each background chart and add it to the multi axis chart if necessary
            if (!getChildren().contains(lineChart)) {
                resizeBackgroundChart(lineChart);
                getChildren().add(lineChart);
            } else {
                resizeBackgroundChart(lineChart);
            }
        }

        ArrayList<Node> emptyChildren = new ArrayList<Node>();

        for (Node child : getChildren()) {
            if (child != baseChart) {
                if (!backgroundCharts.contains((BFALineChart) child)) {
                    emptyChildren.add(child);
                }
            }
        }

        for (Node child : emptyChildren) {
            getChildren().remove(child);
        }

    }

    private void resizeBaseChart(BFALineChart<Number, Number> lineChart) {

        // calculate the width of the current line chart: if there is already a background chart,
        // subtract the number of background charts from the current width; otherwise, subtract nothing
        DoubleBinding binding = widthProperty()
            .subtract((yAxisWidth + yAxisSeparation) * (backgroundCharts.size() > 0 ? backgroundCharts.size() - 1 : 0));

        // apply widths to current line chart
        lineChart.prefWidthProperty().bind(binding);
        lineChart.minWidthProperty().bind(binding);
        lineChart.maxWidthProperty().bind(binding);

    }

    private void resizeBackgroundChart(BFALineChart<Number, Number> lineChart) {

        // calculate the width of the current line chart by
        // subtracting the number of background charts from the current width
        DoubleBinding wBinding = widthProperty()
            .subtract((yAxisWidth + yAxisSeparation) * (backgroundCharts.size() - 1));

        DoubleBinding hBinding = heightProperty().subtract(xAxisHeight);

        // apply widths to current line chart
        lineChart.prefWidthProperty().bind(wBinding);
        lineChart.minWidthProperty().bind(wBinding);
        lineChart.maxWidthProperty().bind(wBinding);
        lineChart.prefHeightProperty().bind(hBinding);
        lineChart.minHeightProperty().bind(hBinding);
        lineChart.maxHeightProperty().bind(hBinding);
        
        // if this is the first background chart, place it on the left;
        // otherwise, place it to the right of the current line chart
        if (backgroundCharts.indexOf(lineChart) != 0) {
            lineChart.translateXProperty().bind(baseChart.getYAxis().widthProperty());
            //lineChart.translateYProperty().bind(baseChart.translateYProperty());
            lineChart.getYAxis().setSide(Side.RIGHT);
            lineChart.getYAxis().setTranslateX((yAxisWidth + yAxisSeparation) * (backgroundCharts.indexOf(lineChart) - 1));
            lineChart.getYAxis().setTranslateY(-xAxisHeight/2);
            
        } else {
            lineChart.translateXProperty().unbind();
            lineChart.translateXProperty().setValue(0.0);
            lineChart.getYAxis().setSide(Side.LEFT);
            lineChart.getYAxis().setTranslateY(-xAxisHeight/2);
            
        }

    }

    /**
     * Adds a data set to the graph. Creates a y-axis class if necessary.
     * @param d the GraphData object representing the AxisType, GTIndex, and XYChart.Series
     * @param lineColor the color of the data set's graph
     */
    public void addSeries(GraphData d) {

        // clear area shading
        baseChart.clearArea();

        BFANumberAxis yAxisAdd = new BFANumberAxis();
        BFANumberAxis xAxisAdd = new BFANumberAxis();
        
        double axisScale;

        BFALineChart<Number, Number> lineChart;

        
            
        // set the label of the new y-axis
        yAxisAdd.setLabel(d.axis.getNameAndUnits());
        axisScale = d.axis.getAxisScalar();

        // if axis class is not graphed, create it
        if (!isUnitGraphed(d)) {
            
            
            // style x-axis
            xAxisAdd.setTickUnit(axisScale);
            xAxisAdd.setAutoRanging(false);

            xAxisAdd.setOpacity(0.0); // somehow the upper setVisible does not work
            xAxisAdd.lowerBoundProperty().bind(((BFANumberAxis) baseChart.getXAxis()).lowerBoundProperty());
            xAxisAdd.upperBoundProperty().bind(((BFANumberAxis) baseChart.getXAxis()).upperBoundProperty());
            
            // xAxis.tickUnitProperty().bind(((BFANumberAxis)
            // baseChart.getXAxis()).tickUnitProperty());

            // style y-axis
            yAxisAdd.setTickUnit(axisScale);
            yAxisAdd.setAutoRanging(false);
            if(backgroundCharts.size() == 0){
                yAxisAdd.setSide(Side.LEFT);
            }else{
                yAxisAdd.setSide(Side.RIGHT);
            }
           
            yAxisAdd.lowerBoundProperty()
                    .bind(((BFANumberAxis) baseChart.getYAxis()).lowerBoundProperty().multiply(axisScale));
            yAxisAdd.upperBoundProperty()
                    .bind(((BFANumberAxis) baseChart.getYAxis()).upperBoundProperty().multiply(axisScale));

            // create chart
            lineChart = new BFALineChart<Number, Number>(xAxisAdd, yAxisAdd);
            lineChart.setMouseTransparent(true);
            axisUnitMap.put(d.axis.getUnits(), lineChart);
            backgroundCharts.add(lineChart);
            styleBackgroundChart(lineChart);
            setFixedAxisWidth(lineChart);
            
           
            
        } else {

            lineChart = axisUnitMap.get(d.axis.getUnits());
        }

        axisChartMap.put(d, lineChart);

        baseChart.getData().add(d.data);
        dataSets.add(d);

        // set color/style of line
        styleChartLine(d);

        // set legend symbol colors
        styleLegend();

    }


    /**
     * Removes a data set from the graph.
     * Also removes a y-axis class if necessary.
     * @param axis the AxisType identifying the data set
     * @param GTIndex the GenericTest to read data from
     */
    public void removeAxis(Axis axis, int GTIndex) {
        removeAxis(findGraphData(GTIndex, axis));
    }

    public void removeAxis(GraphData d){

        // remove GraphData from list
        dataSets.remove(d);
        
        // remove XYChart.Series from its LineChart
        d.data.getData().clear();
        axisChartMap.get(d).getData().remove(d.data);
        axisChartMap.remove(d);
        baseChart.getData().remove(d.data);

        // remove axis class if necessary (only for Non-custom axis)

            if (!isUnitGraphed(d)) {
                
                logger.info("Removing " + d.axis.getName()+ "'s axis class: " + d.axis.getNameAndUnits());

                backgroundCharts.remove(axisUnitMap.get(d.axis.getUnits()));
                axisUnitMap.remove(d.axis.getUnits());
            }
        
        // set legend symbol colors
        styleLegend();

    }

    /**
     * Returns the label for a given axis class.
     * @param axis the AxisType representing the data set
     * @return the label for a given axis class
     */
    private String getAxisLabel(AxisType axis) {

        // an axis class is the sensor type of the data set
        // since AxisType is formatted "X,Y,Z,Magnitude", dividing by 4 works here
        switch (axis.getValue() / 4) {
            case 0: return "Acceleration (m/s²)";
            case 1: return "Velocity (m/s)";
            case 2: return "Displacement (m)";
            case 3: return "Angular Acceleration (°/s²)";
            case 4: return "Angular Velocity (°/s)";
            case 5: return "Angular Displacement (°)";
            case 6: return "Magnetic Field (µT)";
            case 7: return "Momentum (kg-m/s)";
            default: return "Y-Axis";
        }

    }

    private void styleBackgroundChart(BFALineChart<Number, Number> lineChart) {

        Node contentBackground = lineChart.lookup(".chart-content").lookup(".chart-plot-background");
        contentBackground.setStyle("-fx-background-color: transparent;");
        contentBackground.setMouseTransparent(true);
        lineChart.setVerticalZeroLineVisible(false);
        lineChart.setHorizontalZeroLineVisible(false);
        lineChart.setVerticalGridLinesVisible(false);
        lineChart.setHorizontalGridLinesVisible(false);
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);
        lineChart.setLegendVisible(false);
        lineChart.setPickOnBounds(false);
    
    }

    private void styleChartLine(GraphData d) {

        // fetch the line object from the graph
        Node line = d.data.getNode().lookup(".chart-series-line");

        // set the color of the line and symbols from BFAColorMenu
        String colorStyle = "-fx-stroke: " + BFAColorMenu.getHexString(d.axis) + ";";

        // if GT number (not index) is even, render a dashed line
        String dashedStyle = d.GTIndex % 2 == 1 ? "-fx-stroke-dash-array: 5 5 5 5;" : "";

        line.setStyle(colorStyle + dashedStyle);

    }

    /**
     * Updates legend symbols to match line colors.
     */
    public void styleLegend() {
        
        // loop through each child of the line chart
        for (Node n : baseChart.getChildrenUnmodifiable()) {

            // ensure node is the legend
            if (n instanceof Legend) {

                // tracks the AxisTypes of legend items to check for duplicates
                ArrayList<Axis> legendAxes = new ArrayList<Axis>();
                ObservableList<LegendItem> legendItems = ((Legend) n).getItems();

                // TODO this code will NOT work if the codebase is updated to JDK 9 or later;
                // more info: https://stackoverflow.com/questions/57412846/javafx-missing-legend-class
                //
                // loop through each legend item
                for (int i = 0; i < legendItems.size(); i++) {

                    LegendItem legendItem = legendItems.get(i);

                    String style = "";

                    // if this is a slope line, set the color to black
                    if (legendItem.getText().contains("Slope")) {
                        style = "black";
                    }
                    else {
                        
                        // get AxisType of this legend item
                        Axis a = null;
                        try{
                             a = AxisType.valueOf(legendItem.getText());
                            
                        }catch(Exception e){}
                        if(a == null){
                            a = CustomAxisType.getCustomAxisType(legendItem.getText());
                            
                        }
                        if(a != null){
                            // if this legend item is not a duplicate AxisType
                            if (!legendAxes.contains(a)) {

                                // get the corresponding color
                                style = BFAColorMenu.getHexString(a);

                                // track this legend's AxisType
                                legendAxes.add(a);

                            }
                            // if this legend item is a duplicate, remove it
                            else legendItems.remove(i);
                        }
                    }

                    // set legend symbol color
                    legendItem.getSymbol().setStyle("-fx-background-color: " + style + ", white;");
                    
                }
            }
        }

    }

	/**
	 * Updates the colors of currently graphed lines based on BFAColorMenu.
	 */
    public void updateGraphColors() {

        for (GraphData d : dataSets) {
            styleChartLine(d);
        }

    }

    /**
	 * Finds a GraphData object given its fields.
     * Returns <code>null</code> if none is found.
	 * @param GTIndex the GenericTest associated with the GraphData
	 * @param axis the AxisType associated with the GraphData
	 */
    private GraphData findGraphData(int GTIndex, Axis axis) {
        
        for (GraphData d : dataSets) {
            if (d.GTIndex == GTIndex && d.axis == axis) return d;
        }

        return null;

    }


    /**
	 * Determines whether an axis class (meaning a sensor type) is graphed.
     * <p><i>e.g. if given "AccelX", it will check if any "Accel" AxisType is graphed.</i></p>
	 * @param GTIndex the GenericTest associated with the GraphData
	 * @param axis the AxisType associated with the GraphData
	 */
    private boolean isUnitGraphed(GraphData g) {

            for (GraphData d : dataSets) {
                if (d.axis.getUnits().equals(g.axis.getUnits())) return true;
            }

            return false;

    }

}
