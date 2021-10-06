package com.bioforceanalytics.dashboard;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.event.ChangeEvent;

import org.apache.logging.log4j.Logger;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import javafx.util.Duration;
import java.util.ArrayList; 

/**
 * Custom LineChart created to shade in area under a curve and to play overlayed
 * video for SINC Technology. This is also the "base chart" used by
 * {@link com.bioforceanalytics.dashboard.MultiAxisLineChart
 * MultiAxisLineChart}.
 */
public class BFALineChart<X, Y> extends LineChart<X, Y> {

    // reference to the MultiAxisLineChart that controls this chart
    private MultiAxisLineChart parentChart;

    // JavaFX component containing area label
    private Pane areaPane;

    // fields used for shading in area
    private XYChart.Data<Double, Double> p1;
    private XYChart.Data<Double, Double> p2;
    private GraphData data;
    private int SIG_FIGS;
    private double area;

    private ArrayList<Integer> startIndex = new ArrayList<Integer>();
    private ArrayList<Integer> endIndex = new ArrayList<Integer>();

    private double areaBoundX1; 
    private double areaBoundX2;  

    // used to play SINC videos
    private MediaPlayer mediaPlayer;
    private AnimationTimer timer;

    // the amount of time to jump forward/backward with arrow keys
    public final int JUMP_AMOUNT = 1;

    // the frame rate of the video
    private final int FPS = 30;

    // the amount of time between frames (1/FPS)
    private final double DELTA_TIME = ((double) 1) / ((double) FPS);

    // the number of frames to offset all SINC calculations
    private final int SINC_FRAME_ERROR = 12;

    /**
     * The amount of time to offset all SINC calculations.
     * <hr>
     * For some reason, SINC calibration is off by 12 frames when
     * <code>delayAfterStart > 0</code>; this is likely caused by a firmware or
     * remote issue, but since this is a consistent problem, this value will be
     * applied on top of <code>delayAfterStart</code> from the calibration process
     * when graphing SINC trials in the DAG.
     */
    public final double SINC_TIME_ERROR = ((double) SINC_FRAME_ERROR) / ((double) FPS);

    // JavaFX SINC components
    private MediaView mediaView;
    private Rectangle scrubber;

    // indicates whether line chart is playing a SINC video
    private boolean hasSINC;

    private static final Logger logger = LogController.start();

    public BFALineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);

        this.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (p1 != null && p2 != null)
                    Platform.runLater(() -> redrawArea());

            }
        });
        this.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (p1 != null && p2 != null)
                    Platform.runLater(() -> redrawArea());

            }
        });
    }

    public double getAreaBoundX2() {
        return areaBoundX2;
    }

    public void setAreaBoundX2(double areaBoundX2) {
        this.areaBoundX2 = areaBoundX2;
    }

    public double getAreaBoundX1() {
        return areaBoundX1;
    }

    public void setAreaBoundX1(double areaBoundX1) {
        this.areaBoundX1 = areaBoundX1;
    }

    /**
     * Passes a reference of MultiAxisLineChart to BFALineChart.
     * 
     * @param parentChart the MultiAxisLineChart to link this chart to
     */
    public void setParentChart(MultiAxisLineChart parentChart) {
        this.parentChart = parentChart;
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
     * 
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

    /**
     * Shades area under a section of a curve by drawing trapezoids between adjacent
     * points and the x-axis. Takes XYChart-based objects as arguments for ease of
     * use in the BioForce Graph.
     * 
     * @param p1       the left bound of the area
     * @param p2       the right bound of the area
     * @param data     the data set that should be shaded in
     * @param area     the value of the definite integral from the two bounds
     * @param SIG_FIGS the number of significant figures used to round the area
     *                 value
     */
    public void graphArea(XYChart.Data<Double, Double> p1, XYChart.Data<Double, Double> p2, GraphData data, double area,
            final int SIG_FIGS) {

        this.p1 = p1;
        this.p2 = p2;
        this.data = data;
        this.area = area;
        this.SIG_FIGS = SIG_FIGS;
        redrawArea();

    }

    public void redrawArea() {
        redrawArea(0, 0, 0);
    }

    /**
     * Re-renders the polygon used for shading area under a curve. Currently not
     * being called multiple times, only once. TODO at some point we may look into
     * real-time shading with this method.
     */
    public void redrawArea(double xOffset, double yOffset, double areaNorm) {

        ObservableList<XYChart.Data<Number, Number>> series = data.data.getData();

        if (p1 == null || p2 == null || data == null)
            return;
        int start = -1;
        int end = -1;
        
        // redraw area with line-up/normalization shift 
        if((parentChart.getController().getGraphMode() == GraphNoSINCController.GraphMode.LINEUP) || (parentChart.getController().getGraphMode() == GraphNoSINCController.GraphMode.LINEUP_SINC) || (parentChart.getController().getGraphMode() == GraphNoSINCController.GraphMode.NORM)){
            if (startIndex.size() >= 1 && endIndex.size() >= 1){
                int indexS = startIndex.size() - 1; 
                int indexE = startIndex.size() - 1; 

                int startIndexVal = startIndex.get(indexS); 
                int endIndexVal = endIndex.get(indexE); 

                double newX1 = series.get(startIndexVal).getXValue().doubleValue(); 
                p1.setXValue(newX1);
                double newX2 = series.get(endIndexVal).getXValue().doubleValue(); 
                p2.setXValue(newX2);

                double newY1 = series.get(startIndexVal).getYValue().doubleValue(); 
                p1.setYValue(newY1);
                double newY2 = series.get(endIndexVal).getYValue().doubleValue(); 
                p2.setYValue(newY2);
            }
        }

        // get index of x1 and x2 in samples
            for (int i = 0; i < series.size(); i++) {

                if (series.get(i).getXValue().equals(p1.getXValue()) && series.get(i).getYValue().equals(p1.getYValue())) {
                    start = i;
                    startIndex.add(i); 
                    setAreaBoundX1(p1.getXValue()); 
                }
                if (series.get(i).getXValue().equals(p2.getXValue()) &&  series.get(i).getYValue().equals(p2.getYValue())) {
                    end = i;
                    endIndex.add(i); 
                    setAreaBoundX2(p2.getXValue());
                }
            }

        // remove area shading and label
        clearArea();

        if(parentChart.getController().getGraphMode() == GraphNoSINCController.GraphMode.NORM){
            area = areaNorm; 
        }

        // create area label
        double roundedArea = new BigDecimal(area).round(new MathContext(SIG_FIGS)).doubleValue();
        areaPane.getChildren().addAll(createAreaLabel(roundedArea));

        // allows mouse events to pass through polygon
        // makes selecting data points easier
        areaPane.setPickOnBounds(false);

        // set (x,y) position of the area label to halfway between the x-bounds of the
        // area

        //removed positioning due to overlap
        // areaPane.setLayoutX(series.get((start + end) / 2).getNode().getLayoutX() - 50);
        // areaPane.setLayoutY(series.get((start + end) / 2).getNode().getLayoutY() - 100);

        // add area label to LineChart
        getPlotChildren().add(areaPane);

        // cast axes to NumberAxes so that certain methods can be called on them
        BFANumberAxis xAxis = (BFANumberAxis) getXAxis();
        BFANumberAxis yAxis = (BFANumberAxis) getYAxis();

        // pixel position of y=0 on LineChart component
        double y0 = yAxis.getDisplayPosition(0);

        Polygon poly = new Polygon();

        // loop through all points in [p1, p2]
        for (int i = start; i <= end; i++) {

            // pixel positions of x=series[i] and x=seriesi+1] on LineChart component
            double x1 = xAxis.getDisplayPosition(series.get(i).getXValue());

            // pixel positions of y=series[i] and y=series[i+1] on LineChart component
            double y1 = yAxis.getDisplayPosition(series.get(i).getYValue());

            // add points to polygon
            poly.getPoints().addAll(new Double[] { x1, y1 });

        }

        // populate points along the x-axis
        for (int i = end; i >= start; i--) {
            double x1 = xAxis.getDisplayPosition(series.get(i).getXValue());
            poly.getPoints().addAll(new Double[] { x1, y0 });
        }

        // set of the shaded area to the axis's color with a lower opacity
        Color c = BFAColorMenu.getColor(data.axis);
        poly.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.5));

        // add polygon to LineChart
        getPlotChildren().add(poly);
        // areaPane.getChildren().addAll(createAreaLabel(roundedArea));

        // ensure that the area label is on top of the shaded area
        areaPane.toFront(); 

        // Label label = createAreaLabel(roundedArea); 
        // getPlotChildren().add(label); 
        // label.toFront();

        // display full floating-point number on click
        areaPane.setOnMouseClicked(event -> areaPane.getChildren().addAll(createAreaLabel(area)));

    }

    /**
     * Passes references to SINC components from GraphNoSINCController to this
     * class.
     * 
     * @param mediaView the JavaFX component that displays video
     * @param scrubber  the JavaFX component that displays the video scrubber
     */
    public void initSINC(MediaView mediaView, Rectangle scrubber) {

        this.mediaView = mediaView;
        this.scrubber = scrubber;

        // enable scrubber dragging
        this.scrubber.setOnMouseDragged(event -> {

            // get mouse coordinates in the scope of the entire window
            Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());

            // get x-coordinate of the mouse in the scope of the graph
            double displayX = getXAxis().sceneToLocal(mouseSceneCoords).getX();

            // convert display x-coordinate to graph's x-coordinate (aka time-value)
            Number time = ((BFANumberAxis) getXAxis()).getValueForDisplay(displayX);

            // jump to the given time in the video
            mediaPlayer.seek(Duration.seconds(time.doubleValue()));

            logger.info("Dragged scrubber to {}s", mediaPlayer.getCurrentTime().toSeconds());

        });

        // enable scrubber context menu
        this.scrubber.setOnMouseClicked(e -> {

            // ensure this is a right click
            if (e.getButton() == MouseButton.SECONDARY) {

                MenuItem lineUpTrial = new MenuItem("Line Up Trial");
                lineUpTrial.setOnAction(
                        e2 -> parentChart.getController().setGraphMode(GraphNoSINCController.GraphMode.LINEUP_SINC));

                MenuItem jumpToStart = new MenuItem("Jump to Start");
                jumpToStart.setOnAction(e2 -> mediaPlayer.seek(mediaPlayer.getStartTime()));

                MenuItem jumpToEnd = new MenuItem("Jump to End");
                jumpToEnd.setOnAction(e2 -> mediaPlayer.seek(mediaPlayer.getStopTime()));

                MenuItem[] menuItems = { lineUpTrial, jumpToStart, jumpToEnd, };

                // display context menu at the mouse's current position
                Window owner = ((Node) e.getTarget()).getScene().getWindow();
                ContextMenu contextMenu = new ContextMenu(menuItems);
                contextMenu.show(owner, e.getScreenX(), e.getScreenY());

            }

        });

    }

    /**
     * Indicates whether or not the graph is currently displaying a SINC trial.
     * 
     * @return whether or not the graph is currently displaying a SINC trial
     */
    public boolean hasSINC() {
        return hasSINC;
    }

    /**
     * Disables all SINC features.
     */
    public void exitSINC() {

        // indicate that SINC is disabled
        hasSINC = false;

        // remove media player + media from memory
        mediaPlayer.dispose();

        // stop scrubber animation
        timer.stop();

        // hide media player pane + scrubber
        mediaView.setVisible(false);
        scrubber.setVisible(false);

    }

    /**
     * Plays a SINC video overlayed on this chart.
     * 
     * @param videoFile the File object for this video
     */
    public void playVideo(File videoFile) {

        // stop any previous videos/timers
        if (mediaPlayer != null)
            mediaPlayer.dispose();
        if (timer != null)
            timer.stop();

        // load and play video
        mediaPlayer = new MediaPlayer(new Media(videoFile.toURI().toString()));
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.setVisible(true);
        mediaPlayer.play();

        // get "toggle playback" button and reset icon to "PAUSE"
        Button b = (Button) this.getScene().lookup("#togglePlayback");
        b.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.PAUSE));

        // initialize scrubber
        scrubber.setVisible(true);
        scrubber.setX(0);

        // start scrubber animation
        timer = new ScrubberAnimation((BFALineChart<Number, Number>) this, mediaPlayer);
        timer.start();

        // indicate that SINC is enabled
        hasSINC = true;

    }

    /**
     * Plays/pauses the current video.
     */
    public void togglePlayback() {

        // cancel if no video is playing
        if (mediaPlayer == null)
            return;

        // get "toggle playback" button
        Button b = (Button) this.getScene().lookup("#togglePlayback");

        if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            logger.info("Started video from {}s", mediaPlayer.getCurrentTime().toSeconds());
            mediaPlayer.play();

            b.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.PAUSE));
        } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            logger.info("Paused video at {}s", mediaPlayer.getCurrentTime().toSeconds());
            mediaPlayer.pause();

            b.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.PLAY));
        }
    }

    /**
     * Sets the playback speed of the video.
     * 
     * @param speed the new speed of the video
     */
    public void setPlaybackSpeed(double speed) {

        // cancel if no video is playing
        if (mediaPlayer == null)
            return;

        mediaPlayer.setRate(speed);

    }

    /**
     * Jumps back in the video by a specified time amount.
     */
    public void jumpBack() {
        double seconds = relativeSeek(-JUMP_AMOUNT);
        logger.info("Jumped back to {}s", seconds);
    }

    /**
     * Jumps forward in the video by a specified time amount.
     */
    public void jumpForward() {
        double seconds = relativeSeek(JUMP_AMOUNT);
        logger.info("Jumped forward to {}s", seconds);
    }

    /**
     * Jumps one frame back in the video.
     */
    public void lastFrame() {
        double seconds = relativeSeek(-DELTA_TIME);
        logger.info("Jumped back one frame to {}s", seconds);
    }

    /**
     * Jumps one frame forward in the video.
     */
    public void nextFrame() {
        double seconds = relativeSeek(DELTA_TIME);
        logger.info("Jumped forward one frame to {}s", seconds);
    }

    /**
     * Resets scrubber to 0 seconds.
     */
    public void resetVideo() {

        // if video is playing, reset video time to 0 seconds
        if (mediaPlayer != null)
            mediaPlayer.seek(Duration.seconds(0));

    }

    /**
     * Returns the current time of the scrubber in the video.
     */
    public double getCurrentTime() {
        return mediaPlayer.getCurrentTime().toSeconds();
    }

    /**
     * Internal method to move the scrubber relative to the current time.
     * 
     * @param delta the amount of time (in seconds) to move the scrubber
     * @return the current time (in seconds) of the scrubber
     */
    private double relativeSeek(double delta) {

        // cancel if no video is playing
        if (mediaPlayer == null)
            return -1;

        double seconds = mediaPlayer.getCurrentTime().toSeconds();
        Duration time = Duration.seconds(seconds + delta);
        mediaPlayer.seek(time);

        return time.toSeconds();

    }

    /**
     * Controls the scrubber's position on the graph.
     */
    class ScrubberAnimation extends AnimationTimer {

        private BFALineChart<Number, Number> lineChart;
        private MediaPlayer mediaPlayer;

        /**
         * Constructs a scrubber animation.
         * 
         * @param lineChart   the LineChart that this scrubber is overlaid on
         * @param mediaPlayer the media player dictating this scrubber's position
         */
        public ScrubberAnimation(BFALineChart<Number, Number> lineChart, MediaPlayer mediaPlayer) {
            this.lineChart = lineChart;
            this.mediaPlayer = mediaPlayer;
        }

        @Override
        // runs every frame
        public void handle(long now) {

            BFANumberAxis xAxis = (BFANumberAxis) lineChart.getXAxis();
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();

            // if the current time's position is not visible in the viewport
            if (!xAxis.isValueOnAxis(currentTime)) {

                // clamps the current time value to the closest bound visible on the x-axis
                //
                // (if the time is to the right of the upper bound, clamp to the upper bound;
                // if not, then it must be less than the lower bound, so clamp to that)
                currentTime = currentTime >= xAxis.getUpperBound() ? xAxis.getUpperBound() : xAxis.getLowerBound();

            }

            // set scrubber's position to the time's position on the x-axis
            scrubber.setX(lineChart.getXAxis().getDisplayPosition(currentTime));

        }

    }

}