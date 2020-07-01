package dataorganizer;

import javafx.beans.NamedArg;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;

/**
 * Custom LineChart created solely to shade in sections of area under a curve.
 * Should not be manually instantiated, instead reference this component in FXML;
 * the fields for NumberAxes will be populated from the FXMLLoader that reads the file.
 */
public class BFALineChart<X,Y> extends LineChart<X,Y> {

    private XYChart.Data<Double, Double> p1;
    private XYChart.Data<Double, Double> p2;

    private ObservableList<XYChart.Data<Number, Number>> data;
    
    private double area;

    public BFALineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }

    public void clearArea() {

        ObservableList<Node> nodes = getPlotChildren();

        // loops backwards to avoid ConcurrentModificationException
        for (int i = nodes.size() - 1; i >= 0; i--) {

            // if node is a polygon for graphing area, remove it
            if (nodes.get(i) instanceof Polygon) {
                getPlotChildren().remove(nodes.get(i));
            }

        }

    }

    public void redrawArea() {

        //System.out.println("Redrawing area...");

        if (p1 == null && p2 == null && data == null) return;

        clearArea();

        int start = -1;
        int end = -1;

        // TODO data.indexOf() didn't seem to work with XYChart.Data:
        // maybe due to being a new object and not the same reference?
        //
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

        getPlotChildren().add(createAreaLabel(this.area));

        // loop through all points in [x1, x2]
        for (int i = start; i < end; i++) {         

            // pixel positions of x=data[i] and x=data[i+1] on LineChart component
            double x1 = xAxis.getDisplayPosition(data.get(i).getXValue());
            double x2 = xAxis.getDisplayPosition(data.get(i+1).getXValue());
        
            // pixel position of y=0 on LineChart component
            double y0 = yAxis.getDisplayPosition(0);

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

            poly.toFront();
            poly.setFill(linearGrad);
            getPlotChildren().add(poly);

        }

    }

    public Label createAreaLabel(double a) {

		Label label = new Label("Area: " + (Math.round(a * 100.0) / 100.0));

		// add styling to label
		label.getStyleClass().addAll("hover-label");

		// place the label above the data point
		label.translateYProperty().bind(label.heightProperty());

		label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

		return label;

	}

    public void graphArea(XYChart.Data<Double, Double> p1, XYChart.Data<Double, Double> p2, ObservableList<XYChart.Data<Number,Number>> data, double area) {

        System.out.println("Graphing area...");

        this.p1 = p1;
        this.p2 = p2;
        this.data = data;
        this.area = area;

        redrawArea();

    }

}