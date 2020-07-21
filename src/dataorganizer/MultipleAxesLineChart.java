package dataorganizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class MultipleAxesLineChart extends StackPane {

    private final BFALineChart<Number, Number> baseChart;
    private final ObservableList<BFALineChart<Number, Number>> backgroundCharts = FXCollections.observableArrayList();
    private final Map<BFALineChart<Number, Number>, Color> chartColorMap = new HashMap<>();

    /**
     * Tracks the axis classes and their respective line charts holding the y-axes.
     */
    private final Map<Integer, BFALineChart<Number, Number>> axisTypeMap = new HashMap<>();

    /**
     * Tracks the currently drawn data sets and their respective line charts.
     */
    public  final Map<AxisType, BFALineChart<Number, Number>> axisChartMap = new HashMap<>();
    
    // keeps track of currently graphed data sets (GTIndex/AxisType/data)
    private ArrayList<GraphData> dataSets;

    private BFANumberAxis xAxis;
    private BFANumberAxis yAxis;

    private final double yAxisWidth = 60;
    private final double yAxisSeparation = 20;
    private double strokeWidth = 0.3;

    public MultipleAxesLineChart(BFALineChart<Number, Number> baseChart, Color lineColor) {
        this(baseChart, lineColor, null);
    }

    public MultipleAxesLineChart() {
        this(null, Color.RED, null);

    }
   
    /**
     * Returns the amount that the data set's graph should be scaled by.
     * This is used for angular and magnetometer data sets.
     * @param axis the AxisType representing the data set
     * @return the amount that the data set's graph should be scaled by
     */
    public double getAxisScalar(AxisType axis) {

        // if AxisType is Accel, Vel, or Disp
        if (axis.getValue()/4 < 3) return 10;

        // all other data sets
        else return 100;

    }

    public ObservableList<BFALineChart<Number, Number>> getBackgroundCharts(){
        return backgroundCharts;
    }

    public MultipleAxesLineChart(BFALineChart<Number, Number> baseChart, Color lineColor, Double strokeWidth) {
        
        super();
        setPickOnBounds(false);
        dataSets = new ArrayList<GraphData>();

        if (baseChart == null) {

            xAxis = new BFANumberAxis();
            yAxis = new BFANumberAxis();
            xAxis.setTickUnit(1);
            yAxis.setTickUnit(1);
            
            baseChart = new BFALineChart<Number, Number>(xAxis, yAxis);
            baseChart.getXAxis().setLabel("X Axis");
            baseChart.getYAxis().setLabel("Y Axis");

        }

        if (strokeWidth != null) {
            this.strokeWidth = strokeWidth;
        } else {
            this.strokeWidth = 2;
        }
        this.baseChart = baseChart;

        chartColorMap.put(baseChart, lineColor);

        styleBaseChart(baseChart);
        styleChartLine(baseChart, lineColor);
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
        baseChart.setLegendVisible(false);
        baseChart.getXAxis().setAutoRanging(false);
        baseChart.getYAxis().setAutoRanging(false);
        baseChart.getXAxis().setAnimated(false);
        baseChart.getYAxis().setAnimated(false);

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
        getChildren().clear();
        for (BFALineChart<Number, Number> lineChart : backgroundCharts) {
            getChildren().add(resizeBackgroundChart(lineChart));
        }
        getChildren().add(resizeBaseChart(baseChart));
        
    }

    private Node resizeBaseChart(BFALineChart<Number, Number> lineChart) {
        HBox hBox = new HBox(lineChart);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.prefHeightProperty().bind(heightProperty());
        hBox.prefWidthProperty().bind(widthProperty());

        lineChart.minWidthProperty()
                .bind(widthProperty().subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size()));
        lineChart.prefWidthProperty()
                .bind(widthProperty().subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size()));
        lineChart.maxWidthProperty()
                .bind(widthProperty().subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size()));

        return lineChart;
    }

    private Node resizeBackgroundChart(BFALineChart<Number, Number> lineChart) {
        HBox hBox = new HBox(lineChart);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.prefHeightProperty().bind(heightProperty());
        hBox.prefWidthProperty().bind(widthProperty());

        lineChart.minWidthProperty()
                .bind(widthProperty().subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size()));
        lineChart.prefWidthProperty()
                .bind(widthProperty().subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size()));
        lineChart.maxWidthProperty()
                .bind(widthProperty().subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size()));

        lineChart.translateXProperty().bind(baseChart.getYAxis().widthProperty());
        lineChart.getYAxis().setTranslateX((yAxisWidth + yAxisSeparation) * backgroundCharts.indexOf(lineChart));

        return hBox;
    }

    /**
     * Adds a data set to the graph.
     * Creates a y-axis class if necessary.
     * @param d the GraphData object representing the AxisType, GTIndex, and XYChart.Series
     * @param lineColor the color of the data set's graph
     */
    public void addSeries(GraphData d, Color lineColor) {

        BFANumberAxis yAxisAdd = new BFANumberAxis();
        BFANumberAxis xAxisAdd = new BFANumberAxis();
        BFALineChart<Number, Number> lineChart;

        int axisTypeInt = d.axis.getValue() / 4;

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
            yAxisAdd.setSide(Side.RIGHT);
            yAxisAdd.lowerBoundProperty()
                    .bind(((BFANumberAxis) baseChart.getYAxis()).lowerBoundProperty().multiply(axisScale));
            yAxisAdd.upperBoundProperty()
                    .bind(((BFANumberAxis) baseChart.getYAxis()).upperBoundProperty().multiply(axisScale));

            // create chart
            lineChart = new BFALineChart<Number, Number>(xAxisAdd, yAxisAdd);
            lineChart.setMouseTransparent(true);
            
            lineChart.setAnimated(false);
            lineChart.setLegendVisible(false);
            lineChart.setCreateSymbols(false);
            lineChart.setPickOnBounds(false);
            axisTypeMap.put(axisTypeInt,lineChart);
            backgroundCharts.add(lineChart);
            styleBackgroundChart(lineChart, lineColor);
            setFixedAxisWidth(lineChart);
            
            lineChart.getXAxis().setLabel("#" + d.axis.getValue());
            
        } else {
            lineChart = axisTypeMap.get(axisTypeInt);
        }

        axisChartMap.put(d.axis, lineChart);
        baseChart.getData().add(d.data);
        dataSets.add(d);
        
        for (Node n : lineChart.lookupAll(".chart-line-symbol")) {
            if (!n.getStyleClass().contains(".chart-legend-item-symbol")) {
                n.setStyle("-fx-background-color: transparent;");
            }
        }

    }

    /**
     * Removes a data set from the graph.
     * Also removes a y-axis class if necessary.
     * @param axis the AxisType identifying the data set
     * @param GTIndex the GenericTest to read data from
     */
    public void removeAxis(AxisType axis, int GTIndex) {

        GraphData d = findGraphData(GTIndex, axis);

        // remove GraphData from list
        dataSets.remove(d);
        
        // remove XYChart.Series from its LineChart
        d.data.getData().clear();
        axisChartMap.get(axis).getData().remove(d.data);
        axisChartMap.remove(axis);

        // remove axis class if necessary
        if (!isAxisClassGraphed(axis)) {
            System.out.println("Removing " + axis.name() + "'s axis class: " + getAxisLabel(axis));
            backgroundCharts.remove(axisTypeMap.get(axis.getValue()/4));
            axisTypeMap.remove(axis.getValue()/4);
        }
       
        rebuildChart();
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
            default: return "Y-Axis";
        }

    }

    private void styleBackgroundChart(BFALineChart<Number, Number> lineChart, Color lineColor) {
        // styleChartLine(lineChart, lineColor);

        Node contentBackground = lineChart.lookup(".chart-content").lookup(".chart-plot-background");
        contentBackground.setStyle("-fx-background-color: transparent;");
        contentBackground.setMouseTransparent(true);
        lineChart.setVerticalZeroLineVisible(false);
        lineChart.setHorizontalZeroLineVisible(false);
        lineChart.setVerticalGridLinesVisible(false);
        lineChart.setHorizontalGridLinesVisible(false);
        lineChart.setCreateSymbols(false);
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void styleChartLine(BFALineChart<Number, Number> chart, Color lineColor) {
        chart.getYAxis().lookup(".axis-label")
                .setStyle("-fx-text-fill: " + toRGBCode(lineColor) + "; -fx-font-weight: bold;");
        Node seriesLine = chart.lookup(".chart-series-line");
        if (seriesLine != null)
            seriesLine.setStyle("-fx-stroke: " + toRGBCode(lineColor) + "; -fx-stroke-width: " + strokeWidth + ";");

    }

    /**
	 * Finds a GraphData object given its fields.
     * Returns <code>null</code> if none is found.
	 * @param GTIndex the GenericTest associated with the GraphData
	 * @param axis the AxisType associated with the GraphData
	 */
    private GraphData findGraphData(int GTIndex, AxisType axis) {
        
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
    private boolean isAxisClassGraphed(AxisType axis) {

        int axisClass = axis.getValue() / 4;

        for (GraphData d : dataSets) {
            if (d.axis.getValue() / 4 == axisClass) return true;
        }

        return false;

    }

}
