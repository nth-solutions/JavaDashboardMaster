package dataorganizer;

import java.math.BigDecimal;
import java.math.MathContext;

import javafx.beans.NamedArg;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Custom LineChart created solely to shade in sections of area under a curve.
 * Should not be manually instantiated, instead reference this component in FXML;
 * the fields for NumberAxes will be populated from the FXMLLoader that reads the file.
 */
public class BFALineChart<X,Y> extends LineChart<X,Y> {

    /**
     * JavaFX component containing area label.
     */
    private Pane areaPane;

    public BFALineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }

    /**
     * Shades area under a section of a curve by drawing trapezoids between adjacent points and the x-axis.
     * Takes XYChart-based objects as arguments for ease of use in the Data Analysis Graph.
     * @param p1 the left bound of the area
     * @param p2 the right bound of the area
     * @param data the data set that should be shaded in
     * @param area the value of the definite integral from the two bounds
     * @param SIG_FIGS the number of significant figures used to round the area value
     */
    public void graphArea(XYChart.Data<Double, Double> p1, XYChart.Data<Double, Double> p2, ObservableList<XYChart.Data<Number,Number>> data, double area, final int SIG_FIGS) {

        int start = -1;
        int end = -1;

        // get index of x1 and x2 in samples
        for (int i = 0; i < data.size(); i++) {

            if (data.get(i).getXValue().equals(p1.getXValue()) && data.get(i).getYValue().equals(p1.getYValue())) {
                start = i;
            }

            if (data.get(i).getXValue().equals(p2.getXValue()) && data.get(i).getYValue().equals(p2.getYValue())) {
                end = i;
            }

        }  

        // remove area shading and label
        clearArea();

        // create area label
        double roundedArea = new BigDecimal(area).round(new MathContext(SIG_FIGS)).doubleValue();
        areaPane.getChildren().addAll(createAreaLabel(roundedArea));

        // allows mouse events to pass through polygon
        // makes selecting data points easier
        areaPane.setPickOnBounds(false);

        // set (x,y) position of the area label to halfway between the x-bounds of the area
        areaPane.setLayoutX(data.get((start + end) / 2).getNode().getLayoutX() - 50);
        areaPane.setLayoutY(data.get((start + end) / 2).getNode().getLayoutY() - 100);

        // add area label to LineChart
        getPlotChildren().add(areaPane);

        // cast axes to NumberAxes so that certain methods can be called on them
        NumberAxis xAxis = (NumberAxis) getXAxis();
        NumberAxis yAxis = (NumberAxis) getYAxis();

        // pixel position of y=0 on LineChart component
        double y0 = yAxis.getDisplayPosition(0);

        // loop through all points in [p1, p2]
        for (int i = start; i < end; i++) {      

            // pixel positions of x=data[i] and x=data[i+1] on LineChart component
            double x1 = xAxis.getDisplayPosition(data.get(i).getXValue());
            double x2 = xAxis.getDisplayPosition(data.get(i+1).getXValue());

            // pixel positions of y=data[i] and y=data[i+1] on LineChart component
            double y1 = yAxis.getDisplayPosition(data.get(i).getYValue());
            double y2 = yAxis.getDisplayPosition(data.get(i+1).getYValue());

            // this is a single trapezoidal approximation of area under the curve
            Polygon poly = new Polygon();

            // add points to polygon
            poly.getPoints().addAll(new Double[] {
                x1, y0,
                x1, y1,
                x2, y2,
                x2, y0
            });

            // TODO change this to shade the same color as the graph?
            // not important at all, just a cosmetic feature that would be nice
            poly.setFill(Color.RED);

            // add polygon to LineChart
            getPlotChildren().add(poly);

        }

        // ensure that the area label is on top of the shaded area
        areaPane.toFront();

        // display full floating-point number on click
        areaPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event) {
                areaPane.getChildren().addAll(createAreaLabel(area));
            }

        });
    
    }

    /**
     * Clears the area label and shading of the graph.
     */
    public void clearArea() {

        // get all children nodes of LineChart
        ObservableList<Node> nodes = getPlotChildren();

        // loops backwards to avoid ConcurrentModificationException
        for (int i = nodes.size() - 1; i >= 0; i--) {

            // if node is a polygon for graphing area, remove it
            if (nodes.get(i) instanceof Polygon) {
                getPlotChildren().remove(nodes.get(i));
            }

        }

        // reset area label and remove from LineChart
        getPlotChildren().remove(areaPane);
        areaPane = new Pane();

    }

    /**
     * Internal method used to generate the JavaFX label displaying area.
     * @param a the value of the definite integral
     * @return the JavaFX Label object
     */
    private Label createAreaLabel(double a) {

		Label label = new Label("Area: " + a);

		// add styling to label
		label.getStyleClass().addAll("hover-label");

		// place the label above the data point
		label.translateYProperty().bind(label.heightProperty());

        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

		return label;

	} 

    /*
    ==========================================================================
    TODO WIP method for real-time area shading. Does not work as of right now.
    ==========================================================================

    private XYChart.Data<Double, Double> p1;
    private XYChart.Data<Double, Double> p2;

    private ObservableList<XYChart.Data<Number, Number>> data;

    private double area;

    public void graphArea(XYChart.Data<Double, Double> p1, XYChart.Data<Double, Double> p2, ObservableList<XYChart.Data<Number,Number>> data, double area) {

        System.out.println("Graphing area...");

        this.p1 = p1;
        this.p2 = p2;
        this.data = data;
        this.area = area;

        redrawArea();

    }

    public void redrawArea() {

        //System.out.println("Redrawing area...");

        if (p1 == null && p2 == null && data == null) return;

        clearArea();

        int start = -1;
        int end = -1;

        // get index of x1 and x2 in samples
        for (int i = 0; i < data.size(); i++) {

            if (data.get(i).getXValue().equals(p1.getXValue()) && data.get(i).getYValue().equals(p1.getYValue())) {
                start = i;
            }

            if (data.get(i).getXValue().equals(p2.getXValue()) && data.get(i).getYValue().equals(p2.getYValue())) {
                end = i;
            }

        }

        NumberAxis xAxis = (NumberAxis) getXAxis();
        NumberAxis yAxis = (NumberAxis) getYAxis();

        getPlotChildren().remove(areaPane);

        areaPane = new StackPane();
        
        areaPane.getChildren().addAll(createAreaLabel(this.area));
        areaPane.toFront();
        areaPane.setPickOnBounds(false);

        getPlotChildren().add(areaPane);

        System.out.println((start + end) / 2);

        data.get((start + end) / 2).setNode(areaPane);

        // pixel position of y=0 on LineChart component
        double y0 = yAxis.getDisplayPosition(0);

        // loop through all points in [p1, p2]
        for (int i = start; i < end; i++) {      

            // pixel positions of x=data[i] and x=data[i+1] on LineChart component
            double x1 = xAxis.getDisplayPosition(data.get(i).getXValue());
            double x2 = xAxis.getDisplayPosition(data.get(i+1).getXValue());

            // pixel positions of y=data[i] and y=data[i+1] on LineChart component
            double y1 = yAxis.getDisplayPosition(data.get(i).getYValue());
            double y2 = yAxis.getDisplayPosition(data.get(i+1).getYValue());

            Polygon poly = new Polygon();

            poly.getPoints().addAll(new Double[] {
                x1, y0,
                x1, y1,
                x2, y2,
                x2, y0
            });

            LinearGradient linearGrad = new LinearGradient(0, 0, 0, 1,
                true, // proportional
                CycleMethod.NO_CYCLE, // cycle colors
                new Stop(0.1f, Color.rgb(255, 0, 0, .3)));

            poly.setFill(linearGrad);
            poly.toBack();

            // allows mouse events to pass through polygon
            // makes selecting data points easier
            poly.setPickOnBounds(false);

            getPlotChildren().add(poly);

        }

    }
    */

}