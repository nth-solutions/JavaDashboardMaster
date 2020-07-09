package dataorganizer;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class MultipleAxesLineChart extends StackPane {

    private final BFALineChart<Number, Number> baseChart;
    private final ObservableList<BFALineChart<Number, Number>> backgroundCharts = FXCollections.observableArrayList();
    private final Map<BFALineChart<Number, Number>, Color> chartColorMap = new HashMap<>();
    private final Map<Integer, BFALineChart<Number, Number>> axisTypeMap = new HashMap<>();
    private final Map<AxisType, XYChart.Series<Number, Number>> axisSeriesMap = new HashMap<>();

    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private AxisType baseAxis;

    private boolean[] axisTypeGraphed;

    private final double yAxisWidth = 60;
    private final double yAxisSeparation = 20;
    private double strokeWidth = 0.3;

    public MultipleAxesLineChart(BFALineChart<Number, Number> baseChart, Color lineColor) {
        this(baseChart, lineColor, null);
    }

    public MultipleAxesLineChart() {
        this(null, Color.RED, null);

    }

    private double getAxisScalar(AxisType axis) {
        int axisValue = axis.getValue();
        if (axisValue < 4) {
            return 1;
        } // accel
        else if (axisValue >= 4 && axisValue < 8) {
            return 1;
        } else if (axisValue >= 8) {
            return 90;
        } // velocity
        return 0;
    }

    public MultipleAxesLineChart(BFALineChart<Number, Number> baseChart, Color lineColor, Double strokeWidth) {
        super();
        axisTypeGraphed = new boolean[7];
        if (baseChart == null) {
            xAxis = new NumberAxis();
            yAxis = new NumberAxis();
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

        getChildren().add(resizeBaseChart(baseChart));
        for (BFALineChart<Number, Number> lineChart : backgroundCharts) {
            getChildren().add(resizeBackgroundChart(lineChart));
        }
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
        hBox.setMouseTransparent(true);

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

    public void addSeries(XYChart.Series<Number,Number> series, Color lineColor, AxisType axis) {

        NumberAxis yAxisAdd = new NumberAxis();
        NumberAxis xAxisAdd = new NumberAxis();
        BFALineChart<Number, Number> lineChart;

        int axisTypeInt = axis.getValue() / 4;

        if (!axisTypeGraphed[axisTypeInt]) {
            switch (axisTypeInt) {
            case 0: // accel
                yAxisAdd.setLabel("m/s²");
                break;
            case 1: // vel
                yAxisAdd.setLabel("m/s");
                break;
            case 2: // disp
                yAxisAdd.setLabel("m");
                break;
            case 3: // angAcc
                yAxisAdd.setLabel("°/s²");
                break;
            case 4: // angVel
                yAxisAdd.setLabel("°/s");
                break;
            case 5: // angDisp
                yAxisAdd.setLabel("° (Degrees)");
                break;
            case 6: // mag
                yAxisAdd.setLabel("µT");
                break;
            default:
                yAxisAdd.setLabel("Y-Axis");
                break;
            }
            double axisScale = getAxisScalar(axis);
            // style x-axis
            xAxisAdd.setTickUnit(axisScale);
            xAxisAdd.setAutoRanging(false);

            xAxisAdd.setOpacity(0.0); // somehow the upper setVisible does not work
            xAxisAdd.lowerBoundProperty().bind(((NumberAxis) baseChart.getXAxis()).lowerBoundProperty());
            xAxisAdd.upperBoundProperty().bind(((NumberAxis) baseChart.getXAxis()).upperBoundProperty());
            // xAxis.tickUnitProperty().bind(((NumberAxis)
            // baseChart.getXAxis()).tickUnitProperty());

            // style y-axis
            yAxisAdd.setTickUnit(axisScale);
            yAxisAdd.setAutoRanging(false);
            yAxisAdd.setSide(Side.RIGHT);
            yAxisAdd.lowerBoundProperty()
                    .bind(((NumberAxis) baseChart.getYAxis()).lowerBoundProperty().multiply(axisScale));
            yAxisAdd.upperBoundProperty()
                    .bind(((NumberAxis) baseChart.getYAxis()).upperBoundProperty().multiply(axisScale));

            // create chart
            lineChart = new BFALineChart<Number, Number>(xAxisAdd, yAxisAdd);
            axisTypeMap.put(axisTypeInt, lineChart);
            lineChart.setAnimated(false);
            lineChart.setLegendVisible(false);
            lineChart.setCreateSymbols(false);
            backgroundCharts.add(lineChart);
            chartColorMap.put(lineChart, lineColor);
            styleBackgroundChart(lineChart, lineColor);
            setFixedAxisWidth(lineChart);
            
            lineChart.getXAxis().setLabel("#" + axis.getValue());
            
        } else {
            lineChart = axisTypeMap.get(axisTypeInt);
        }

        axisTypeGraphed[axisTypeInt] = true;
        
        lineChart.getData().add(series);
        axisSeriesMap.put(axis, series);
        
        for (Node n : lineChart.lookupAll(".chart-line-symbol")) {
            if (!n.getStyleClass().contains(".chart-legend-item-symbol")) {
                n.setStyle("-fx-background-color: transparent;");
            }
        }

    }

    public void removeAxis(AxisType axis) {
        axisSeriesMap.get(axis).getData().clear();
        axisSeriesMap.remove(axis);
        boolean isEmpty = true;
        for(XYChart.Series<Number,Number> series : axisTypeMap.get(axis.getValue()/4).getData()){
            if(!series.getData().isEmpty()) isEmpty = false;
        }
        if(isEmpty){
            backgroundCharts.remove(axisTypeMap.get(axis.getValue()/4));
            axisTypeMap.remove(axis.getValue()/4);
            axisTypeGraphed[axis.getValue()/4] = false;
        }
       
        rebuildChart();
    }

    private void styleBackgroundChart(BFALineChart<Number, Number> lineChart, Color lineColor) {
        // styleChartLine(lineChart, lineColor);

        Node contentBackground = lineChart.lookup(".chart-content").lookup(".chart-plot-background");
        contentBackground.setStyle("-fx-background-color: transparent;");

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

    public boolean isAxisGraphed(AxisType axis) {
        return axisSeriesMap.containsKey(axis);
    }
}
