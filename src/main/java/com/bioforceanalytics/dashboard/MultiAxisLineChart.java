package com.bioforceanalytics.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.bioforceanalytics.dashboard.GraphNoSINCController.GraphMode;
import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;

import org.apache.logging.log4j.Logger;

import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class MultiAxisLineChart extends StackPane {

    private final BFALineChart<Number, Number> baseChart;
    private final ObservableList<LineChart<Number, Number>> backgroundCharts = FXCollections.observableArrayList();

    /**
     * Tracks the axis classes and their respective line charts holding the y-axes.
     */
    private final Map<String, LineChart<Number, Number>> axisTypeMap = new HashMap<>();

    /**
     * Tracks the currently drawn data sets and their respective line charts.
     */
    public final Map<GraphData, LineChart<Number, Number>> axisChartMap = new HashMap<>();

    // keeps track of currently graphed data sets (GTIndex/AxisType/data)
    private ArrayList<GraphData> dataSets;

    private BFANumberAxis xAxis;
    private BFANumberAxis yAxis;

    private final double yAxisWidth = 60;
    private final double yAxisSeparation = 20;
    private final double xAxisHeight = 30;

    // the interval at which samples are drawn to the screen
    // if value is 20 (default), every 20th sample will be rendered
    // TODO make this an advanced user setting
    private int resolution;

    // mouse coordinates
    private double mouseX;
    private double mouseY;

    // scalars to convert pixel space into graph space
    private double zoomviewScalarX;
    private double zoomviewScalarY;

    // indicates the amount to scroll based on the relative position
    // of the mouse to the center of the viewport
    private double leftScrollPercentage;
    private double topScrollPercentage;

    // coordinates of the center of the viewport
    private double zoomviewX;
    private double zoomviewY;

    // size of the viewport
    private double zoomviewW;
    private double zoomviewH;

    // coordinates of the center of the resetted viewport
    private double resetZoomviewX;
    private double resetZoomviewY;

    // size of the resetted viewport
    private double resetZoomviewH;
    private double resetZoomviewW;

    // coordinates of the last location of the mouse
    private double lastMouseX;
    private double lastMouseY;

    private double scrollCenterX;
    private double scrollCenterY;

    // holds the values scaling each axis class on the graph
    private double[] axisScalars;

    private GraphNoSINCController controller;

    private static final Logger logger = LogController.start();

    public MultiAxisLineChart() {

        setPickOnBounds(false);
        dataSets = new ArrayList<GraphData>();

        // initialize axis scalars temporarily
        axisScalars = new double[] { 10, 10, 10, 500, 100, 100, 100, 10 };

        xAxis = new BFANumberAxis();
        yAxis = new BFANumberAxis();
        xAxis.setTickUnit(1);
        yAxis.setTickUnit(1);

        baseChart = new BFALineChart<Number, Number>(xAxis, yAxis);
        baseChart.getXAxis().setLabel("Time (s)");
        baseChart.getYAxis().setLabel("Y Axis");
        baseChart.setParentChart(this);

        styleBaseChart(baseChart);
        setFixedAxisWidth(baseChart);

        setAlignment(Pos.CENTER_LEFT);

        backgroundCharts.addListener((Observable observable) -> rebuildChart());
        rebuildChart();

        // zoom/viewport settings
        resolution = 20;
        zoomviewScalarX = 1;
        zoomviewScalarY = 1;
        resetZoomviewX = 0;
        resetZoomviewY = 0;
        resetZoomviewW = 10;
        resetZoomviewH = 5;
        zoomviewX = 5;
        zoomviewY = 0;
        zoomviewW = 10;
        zoomviewH = 5;

        // listener that runs every tick the mouse scrolls, calculates zooming
        this.setOnScroll(event -> {

            // saves the mouse location of the scroll event to x and y variables
            scrollCenterX = event.getX();
            scrollCenterY = event.getY();

            /**
             * calculates the percentage of scroll either on the left or top of the screen
             * e.g. if the mouse is at the middle of the screen, leftScrollPercentage is
             * 0.5, if it is three quarters to the right, it is 0.75
             */
            leftScrollPercentage = (scrollCenterX - 48) / (baseChart.getWidth() - 63);
            topScrollPercentage = (scrollCenterY - 17) / (baseChart.getHeight() - 88);

            // vertically scale the graph
            if (!event.isAltDown()) {
                zoomviewW -= zoomviewW * event.getDeltaY() / 300;
                zoomviewW = Math.max(baseChart.getWidth() * .00005, zoomviewW);
                zoomviewX += zoomviewW * event.getDeltaY() * (leftScrollPercentage - .5) / 300;
            }

            // horizontally scale the graph
            if (!event.isControlDown()) {
                // decreases the zoomview width and height by an amount relative to the scroll
                // and the current size of the zoomview (slows down zooming at high levels of
                // zoom)
                zoomviewH -= zoomviewH * event.getDeltaY() / 300;

                zoomviewH = Math.max(baseChart.getHeight() * .00005, zoomviewH);
                // moves the center of the zoomview to accomodate for the zoom, accounts for the
                // position of the mouse to try an keep it in the same spot
                zoomviewY -= zoomviewH * event.getDeltaY() * (topScrollPercentage - .5) / 300;
            }

            redrawGraph();

        });

        // listener that runs every tick the mouse is dragged, calculates panning
        this.setOnMouseDragged(event -> {

            if (controller.getGraphMode() == GraphNoSINCController.GraphMode.NONE) {

                // get the mouse x and y position relative to the line chart
                mouseX = event.getX();
                mouseY = event.getY();

                // calculate a scalar to convert pixel space into graph space (mouse data in
                // pixels, zoomview in whatever units the graph is in)
                zoomviewScalarX = (xAxis.getUpperBound() - xAxis.getLowerBound())
                        / (baseChart.getWidth() - yAxis.getWidth());
                zoomviewScalarY = (yAxis.getUpperBound() - yAxis.getLowerBound())
                        / (baseChart.getHeight() - xAxis.getHeight());

                // adds the change in mouse position this tick to the zoom view, converted into
                // graph space
                zoomviewX -= (mouseX - lastMouseX) * zoomviewScalarX;
                zoomviewY += (mouseY - lastMouseY) * zoomviewScalarY;

                redrawGraph();

                // sets last tick's mouse data as this tick's
                lastMouseX = mouseX;
                lastMouseY = mouseY;

            }

        });

        // listener that runs when the mouse is clicked, only runs once per click, helps
        // to differentiate between drags
        this.setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        //clear area when clicked anywhere on scene
        // this.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
        //     if((controller.getGraphMode() != GraphNoSINCController.GraphMode.SLOPE) && (controller.getGraphMode() != GraphNoSINCController.GraphMode.AREA) && (controller.getGraphMode() != GraphNoSINCController.GraphMode.NORM) && (controller.getGraphMode() != GraphNoSINCController.GraphMode.LINEUP) && (controller.getGraphMode() != GraphNoSINCController.GraphMode.LINEUP_SINC))
        //         baseChart.clearArea();
        // }); 

    }

    /**
     * Sets a reference to the parent DAG controller class.
     * 
     * @param controller the GraphNoSINCController parenting this graph
     */
    public void setController(GraphNoSINCController controller) {
        this.controller = controller;
    }

    public boolean isBaseEmpty() {
        return (baseChart.getData().size() == 0);
    }

    private void styleBaseChart(LineChart<Number, Number> baseChart) {
        baseChart.setAnimated(false);
        baseChart.setCreateSymbols(false);
        baseChart.setLegendVisible(true);
        baseChart.getXAxis().setAutoRanging(false);
        baseChart.getYAxis().setAutoRanging(false);
        baseChart.getXAxis().setAnimated(false);
        baseChart.getYAxis().setAnimated(false);
        baseChart.getYAxis().setOpacity(0.0);

    }

    private void setFixedAxisWidth(LineChart<Number, Number> chart) {
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

    public BFALineChart<Number, Number> getBaseChart() {
        return baseChart;
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
        for (LineChart<Number, Number> lineChart : backgroundCharts) {

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
                if (!backgroundCharts.contains((LineChart) child)) {
                    emptyChildren.add(child);
                }
            }
        }

        for (Node child : emptyChildren) {
            getChildren().remove(child);
        }

    }

    private void resizeBaseChart(LineChart<Number, Number> lineChart) {

        // calculate the width of the current line chart: if there is already a
        // background chart,
        // subtract the number of background charts from the current width; otherwise,
        // subtract nothing
        DoubleBinding binding = widthProperty().subtract(
                (yAxisWidth + yAxisSeparation) * (backgroundCharts.size() > 0 ? backgroundCharts.size() - 1 : 0));

        // apply widths to current line chart
        lineChart.prefWidthProperty().bind(binding);
        lineChart.minWidthProperty().bind(binding);
        lineChart.maxWidthProperty().bind(binding);

    }

    private void resizeBackgroundChart(LineChart<Number, Number> lineChart) {

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
            // lineChart.translateYProperty().bind(baseChart.translateYProperty());
            lineChart.getYAxis().setSide(Side.RIGHT);
            lineChart.getYAxis()
                    .setTranslateX((yAxisWidth + yAxisSeparation) * (backgroundCharts.indexOf(lineChart) - 1));
            lineChart.getYAxis().setTranslateY(-xAxisHeight / 2);

        } else {
            lineChart.translateXProperty().unbind();
            lineChart.translateXProperty().setValue(0.0);
            lineChart.getYAxis().setSide(Side.LEFT);
            lineChart.getYAxis().setTranslateY(-xAxisHeight / 2);

        }

    }

    /**
     * Adds a data set to the graph. Creates a y-axis class if necessary.
     * 
     * @param d         the GraphData object representing the AxisType, GTIndex, and
     *                  XYChart.Series
     * @param lineColor the color of the data set's graph
     */
    public void addSeries(GraphData d) {

        // clear area shading
        baseChart.clearArea();

        BFANumberAxis yAxisAdd = new BFANumberAxis();
        BFANumberAxis xAxisAdd = new BFANumberAxis();
        LineChart<Number, Number> lineChart;

        // set the label of the new y-axis
        yAxisAdd.setLabel(getAxisLabel(d.axis));

        // if axis class is not graphed, create it
        if (!isAxisClassGraphed(d.axis)) {

            double axisScale = getAxisScalar(d.axis);

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
            if (backgroundCharts.size() == 0) {
                yAxisAdd.setSide(Side.LEFT);
            } else {
                yAxisAdd.setSide(Side.RIGHT);
            }

            yAxisAdd.lowerBoundProperty()
                    .bind(((BFANumberAxis) baseChart.getYAxis()).lowerBoundProperty().multiply(axisScale));
            yAxisAdd.upperBoundProperty()
                    .bind(((BFANumberAxis) baseChart.getYAxis()).upperBoundProperty().multiply(axisScale));

            // create chart
            lineChart = new LineChart<Number, Number>(xAxisAdd, yAxisAdd);
            lineChart.setMouseTransparent(true);

            axisTypeMap.put(d.axis.getUnits(), lineChart);
            backgroundCharts.add(lineChart);
            styleBackgroundChart(lineChart);
            setFixedAxisWidth(lineChart);

            lineChart.getXAxis().setLabel("#" + d.axis.getValue());

        } else {
            lineChart = axisTypeMap.get(d.axis.getUnits());
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
     * Removes a data set from the graph. Also removes a y-axis class if necessary.
     * 
     * @param axis    the AxisType identifying the data set
     * @param GTIndex the GenericTest to read data from
     */
    public void removeAxis(Axis axis, int GTIndex) {

        // clear area shading
        baseChart.clearArea();

        GraphData d = findGraphData(GTIndex, axis);

        // remove GraphData from list
        dataSets.remove(d);

        // remove XYChart.Series from its LineChart
        d.data.getData().clear();
        axisChartMap.get(d).getData().remove(d.data);
        axisChartMap.remove(d);
        baseChart.getData().remove(d.data);

        // remove axis class if necessary
        if (!isAxisClassGraphed(axis)) {

            logger.info("Removing " + axis.getName() + "'s axis class: " + getAxisLabel(axis));

            backgroundCharts.remove(axisTypeMap.get(axis.getUnits()));
            axisTypeMap.remove(axis.getUnits());
        }

        // set legend symbol colors
        styleLegend();

    }

    /**
     * Handles zooming/panning of the graph.
     */
    public void redrawGraph() {

        this.setXBounds(zoomviewX - zoomviewW / 2, zoomviewX + zoomviewW / 2);
        this.setYBounds(zoomviewY - zoomviewH / 2, zoomviewY + zoomviewH / 2);

        yAxis.setLowerBound(zoomviewY - zoomviewH / 2);
        yAxis.setUpperBound(zoomviewY + zoomviewH / 2);

        xAxis.setTickUnit(Math.pow(2, Math.floor(Math.log(zoomviewW) / Math.log(2)) - 3));
        yAxis.setTickUnit(Math.pow(2, Math.floor(Math.log(zoomviewH) / Math.log(2)) - 2));

        // update tick spacing based on zoom level
        for (GraphData d : this.axisChartMap.keySet()) {

            ((BFANumberAxis) (this.axisChartMap.get(d).getYAxis())).setTickUnit(
                    Math.pow(2, Math.floor(Math.log(zoomviewH) / Math.log(2)) - 2) * this.getAxisScalar(d.axis));
            ((BFANumberAxis) (this.axisChartMap.get(d).getXAxis()))
                    .setTickUnit(Math.pow(2, Math.floor(Math.log(zoomviewW) / Math.log(2)) - 3));
        }

        baseChart.clearArea();
        controller.clearSlope();
        //make sure area and slope is not shown after lineup/normalization
        controller.setSlopeFlag(false); 
        controller.setAreaFlag(false);

    }

    /**
     * Resets the viewport of the graph to the specified bounds.
     * 
     * @param x      the x-value of the point to center on when resetting
     * @param y      the y-value of the point to center on when resetting
     * @param width  the width of the viewport when resetting
     * @param height the height of the viewport when resetting
     */
    public void resetViewport(double x, double y, double width, double height) {

        resetZoomviewX = x;
        resetZoomviewY = y;
        resetZoomviewW = width;
        resetZoomviewH = height;

        resetViewport();

    }

    /**
     * Resets the viewport of the graph.
     */
    public void resetViewport() {

        zoomviewX = resetZoomviewX;
        zoomviewY = resetZoomviewY;
        zoomviewW = resetZoomviewW;
        zoomviewH = resetZoomviewH;

        redrawGraph();

    }

    /**
     * Returns the label for a given axis class.
     * 
     * @param axis the AxisType representing the data set
     * @return the label for a given axis class
     */
    private String getAxisLabel(Axis axis) {

        // an axis class is the sensor type of the data set
        // since AxisType is formatted "X,Y,Z,Magnitude", dividing by 4 works here

        return axis.getNameAndUnits();
        /*
         * switch (axis.getValue() / 4) { case 0: return "Acceleration (m/s²)"; case 1:
         * return "Velocity (m/s)"; case 2: return "Displacement (m)"; case 3: return
         * "Angular Acceleration (°/s²)"; case 4: return "Angular Velocity (°/s)"; case
         * 5: return "Angular Displacement (°)"; case 6: return "Magnetic Field (µT)";
         * case 7: return "Momentum (kg-m/s)"; default: return "Y-Axis"; }
         */

    }

    private void styleBackgroundChart(LineChart<Number, Number> lineChart) {

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

        Axis a;
        // get AxisType of this legend item
        try {
            a = AxisType.valueOf(d.axis.getName());

        } catch (Exception e) {
            a = Axis.getAxis(d.axis.getExactName());
        }

        String colorStyle = "-fx-stroke: " + BFAColorMenu.getHexString(a) + ";";

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
                // more info:
                // https://stackoverflow.com/questions/57412846/javafx-missing-legend-class
                //
                // loop through each legend item
                for (int i = 0; i < legendItems.size(); i++) {

                    LegendItem legendItem = legendItems.get(i);

                    String style = "";

                    // if this is a slope line, set the color to black
                    if (legendItem.getText().contains("Slope")) {
                        style = "black";
                    } else {
                        Axis a;
                        // get AxisType of this legend item
                        try {
                            a = AxisType.valueOf(legendItem.getText());

                        } catch (Exception e) {
                            a = Axis.getAxis(legendItem.getText());
                        }

                        // if this legend item is not a duplicate AxisType
                        if (!legendAxes.contains(a)) {

                            // get the corresponding color
                            style = BFAColorMenu.getHexString(a);

                            // track this legend's AxisType
                            legendAxes.add(a);

                        }
                        // if this legend item is a duplicate, remove it
                        else
                            legendItems.remove(i);

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
        styleLegend();

    }

    /**
     * Finds a GraphData object given its fields. Returns <code>null</code> if none
     * is found.
     * 
     * @param GTIndex the GenericTest associated with the GraphData
     * @param axis    the AxisType associated with the GraphData
     */
    private GraphData findGraphData(int GTIndex, Axis axis) {

        for (GraphData d : dataSets) {
            if (d.GTIndex == GTIndex && d.axis == axis)
                return d;
        }

        return null;

    }

    /**
     * Determines whether an axis class (meaning a sensor type) is graphed.
     * <p>
     * <i>e.g. if given "AccelX", it will check if any "Accel" AxisType is
     * graphed.</i>
     * </p>
     * 
     * @param GTIndex the GenericTest associated with the GraphData
     * @param axis    the AxisType associated with the GraphData
     */
    private boolean isAxisClassGraphed(Axis axis) {

        for (GraphData d : dataSets) {
            if (d.axis.getUnits() == axis.getUnits())
                return true;
        }

        return false;

    }

    /**
     * <p>
     * Rounds up the bound to the nearest multiple of the power of 10 multiple. Can
     * be thought of as converting the number to scientific notation, then rounding
     * up the coefficient.
     * </p>
     * eg. 17 -> 20; 17 = 1.7*10^1, round up 1.7 to 2, 2*10^1 = 20.
     * 
     * @param val the bound to round up
     */
    public static double roundBound(double val) {

        // get the order of magnitude (largest power of 10 in the bound)
        // eg. 17 = 1.7*10^1, therefore order = 1
        int order = (int) Math.log10(val);

        // get the magnitude of the power of 10
        // eg. 17 = 1.7*10^1, floor 1.7 to 1, therefore mag = 1
        int mag = (int) (val / (Math.pow(10, order)));

        // calculate the rounded bound
        // eg. mag = 1, mag+1 = 2, order = 1, 2*10^1 = 20
        return (mag + 1) * Math.pow(10, order);

    }

    /**
     * Calculates axis scalars from the min/max values of axis classes.
     * 
     * @param axisClassRanges array holding min/max values for each axis class
     */
    public void setAxisScalars(Double[][] axisClassRanges) {

        for (int i = 0; i < axisClassRanges.length; i++) {

            // TODO remove min/max from "axisClassRanges";
            // just input the bound with the highest magnitude,
            // since we just want our graph bounds to be symmetrical

            // get the bound with the larger magnitude
            double bound = Math.max(axisClassRanges[i][0], axisClassRanges[i][1]);

            // convert bound to axis scalar
            // (since scalar value of 1 yields bounds of [-5,5])
            axisScalars[i] = roundBound(bound) / 5;

            String axisClassName = AxisType.valueOf(i * 4).toString();

            logger.debug("Bounds for {}: [{},{}] | Scalar: {}", axisClassName.substring(0, axisClassName.length() - 1),
                    axisClassRanges[i][0], axisClassRanges[i][1], axisScalars[i]);

        }

    }

    /**
     * Returns the amount that the data set's graph should be scaled by.
     * 
     * @param axis the AxisType representing the data set
     * @return the amount that the data set's graph should be scaled by
     */
    public double getAxisScalar(Axis axis) {
        if (axis.isCustomAxis())
            return axis.getAxisScalar();
        return axisScalars[axis.getValue() / 4];
    }

    /**
     * Calculates the graphing resolution used when plotting data to the screen.
     * This should be used instead of directly accessing the "resolution" field.
     * 
     * @param axis the AxisType to get the resolution for
     * @return the graphing resolution of the given axis
     */
    public int getResolution(Axis axis) {
        // if this is a magnetometer data set, divide resolution by 10 to match 960 sps
        // data sets
        if (axis.getValue() / 4 == 6) {

            // ensure that the resolution is never smaller than 1
            return resolution >= 10 ? resolution / 10 : 1;
        } else {
            return resolution;
        }
    }

    /**
     * Sets the graphing resolution used when plotting data to the screen.
     * 
     * @param resolution the graphing resolution to use
     */
    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    /**
     * Returns a reference to the BioForce Graph controller.
     */
    public GraphNoSINCController getController() {
        return controller;
    }

}
