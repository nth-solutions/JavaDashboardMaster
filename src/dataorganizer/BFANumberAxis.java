
package dataorganizer;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.javafx.charts.ChartLayoutAnimator;
import com.sun.javafx.css.converters.SizeConverter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.chart.ValueAxis;
import javafx.util.StringConverter;

/**
 * A axis class that plots a range of numbers with major tick marks every "tickUnit". You can use any Number type with
 * this axis, Long, Double, BigDecimal etc.
 */
public final class BFANumberAxis extends ValueAxis<Number> {

    private Object currentAnimationID;
    private final ChartLayoutAnimator animator = new ChartLayoutAnimator(this);
    private final StringProperty currentFormatterProperty = new SimpleStringProperty(this, "currentFormatter", "");
    private final DefaultFormatter defaultFormatter = new DefaultFormatter(this);

    // -------------- PUBLIC PROPERTIES --------------------------------------------------------------------------------

    /** When true zero is always included in the visible range. This only has effect if auto-ranging is on. */
    private BooleanProperty forceZeroInRange = new BooleanPropertyBase(true) {
        @Override protected void invalidated() {
            // This will effect layout if we are auto ranging
            if(isAutoRanging()) {
                requestAxisLayout();
                invalidateRange();
            }
        }

        @Override
        public Object getBean() {
            return BFANumberAxis.this;
        }

        @Override
        public String getName() {
            return "forceZeroInRange";
        }
    };
    public final boolean isForceZeroInRange() { return forceZeroInRange.getValue(); }
    public final void setForceZeroInRange(boolean value) { forceZeroInRange.setValue(value); }
    public final BooleanProperty forceZeroInRangeProperty() { return forceZeroInRange; }

    /**  The value between each major tick mark in data units. This is automatically set if we are auto-ranging. */
    private DoubleProperty tickUnit = new StyleableDoubleProperty(5) {
        @Override protected void invalidated() {
            if(!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public CssMetaData<BFANumberAxis,Number> getCssMetaData() {
            return StyleableProperties.TICK_UNIT;
        }

        @Override
        public Object getBean() {
            return BFANumberAxis.this;
        }

        @Override
        public String getName() {
            return "tickUnit";
        }
    };
    public final double getTickUnit() { return tickUnit.get(); }
    public final void setTickUnit(double value) { tickUnit.set(value); }
    public final DoubleProperty tickUnitProperty() { return tickUnit; }

    // -------------- CONSTRUCTORS -------------------------------------------------------------------------------------

    /**
     * Create a auto-ranging BFANumberAxis
     */
    public BFANumberAxis() {}

    /**
     * Create a non-auto-ranging BFANumberAxis with the given upper bound, lower bound and tick unit
     *
     * @param lowerBound The lower bound for this axis, ie min plottable value
     * @param upperBound The upper bound for this axis, ie max plottable value
     * @param tickUnit The tick unit, ie space between tickmarks
     */
    public BFANumberAxis(double lowerBound, double upperBound, double tickUnit) {
        super(lowerBound, upperBound);
        setTickUnit(tickUnit);
    }

    /**
     * Create a non-auto-ranging BFANumberAxis with the given upper bound, lower bound and tick unit
     *
     * @param axisLabel The name to display for this axis
     * @param lowerBound The lower bound for this axis, ie min plottable value
     * @param upperBound The upper bound for this axis, ie max plottable value
     * @param tickUnit The tick unit, ie space between tickmarks
     */
    public BFANumberAxis(String axisLabel, double lowerBound, double upperBound, double tickUnit) {
        super(lowerBound, upperBound);
        setTickUnit(tickUnit);
        setLabel(axisLabel);
    }

    // -------------- PROTECTED METHODS --------------------------------------------------------------------------------

    /**
     * Get the string label name for a tick mark with the given value
     *
     * @param value The value to format into a tick label string
     * @return A formatted string for the given value
     */
    @Override protected String getTickMarkLabel(Number value) {
        StringConverter<Number> formatter = getTickLabelFormatter();
        if (formatter == null) formatter = defaultFormatter;
        return formatter.toString(value);
    }

    /**
     * Called to get the current axis range.
     *
     * @return A range object that can be passed to setRange() and calculateTickValues()
     */
    @Override protected Object getRange() {
        return new Object[]{
            getLowerBound(),
            getUpperBound(),
            getTickUnit(),
            getScale(),
            currentFormatterProperty.get()
        };
    }

    /**
     * Called to set the current axis range to the given range. If isAnimating() is true then this method should
     * animate the range to the new range.
     *
     * @param range A range object returned from autoRange()
     * @param animate If true animate the change in range
     */
    @Override protected void setRange(Object range, boolean animate) {
        final Object[] rangeProps = (Object[]) range;
        final double lowerBound = (Double)rangeProps[0];
        final double upperBound = (Double)rangeProps[1];
        final double tickUnit = (Double)rangeProps[2];
        final double scale = (Double)rangeProps[3];
        final String formatter = (String)rangeProps[4];
        currentFormatterProperty.set(formatter);
        final double oldLowerBound = getLowerBound();
        setLowerBound(lowerBound);
        setUpperBound(upperBound);
        setTickUnit(tickUnit);

            currentLowerBound.set(lowerBound);
            setScale(scale);
        
    }

    /**
     * Calculate a list of all the data values for each tick mark in range
     *
     * @param length The length of the axis in display units
     * @param range A range object returned from autoRange()
     * @return A list of tick marks that fit along the axis if it was the given length
     */
    @Override protected List<Number> calculateTickValues(double length, Object range) {
        final Object[] rangeProps = (Object[]) range;
        final double lowerBound = (Double)rangeProps[0];
        final double upperBound = (Double)rangeProps[1];
        final double tickUnit = (Double)rangeProps[2];
        List<Number> tickValues = new ArrayList<>();
        if (lowerBound == upperBound) {
            tickValues.add(lowerBound);
        } else if (tickUnit <= 0) {
            tickValues.add(lowerBound);
            tickValues.add(upperBound);
        } else if (tickUnit > 0) {
            tickValues.add(lowerBound);
            if (((upperBound - lowerBound) / tickUnit) > 2000) {
                // This is a ridiculous amount of major tick marks, something has probably gone wrong
                System.err.println("Warning we tried to create more than 2000 major tick marks on a BFANumberAxis. " +
                        "Lower Bound=" + lowerBound + ", Upper Bound=" + upperBound + ", Tick Unit=" + tickUnit);
            } else {
                if (lowerBound + tickUnit < upperBound) {
                    // If tickUnit is integer, start with the nearest integer
                    for(int i = 0; i < upperBound/tickUnit; i++){
                        tickValues.add(i * tickUnit);
                    }
                    for(int i = 0; i > lowerBound/tickUnit; i--){
                        tickValues.add(i * tickUnit);
                    }
                    /*
                    double major = Math.rint(tickUnit) == tickUnit ? Math.ceil(lowerBound) : lowerBound + tickUnit;
                    int count = (int)Math.ceil((upperBound - major)/tickUnit);
                    for (int i = 0; major < upperBound && i < count; major += tickUnit, i++) {
                        if (!tickValues.contains(major)) {
                            tickValues.add(major);
                        }
                    }
                    */
                }
            }
            tickValues.add(upperBound);
        }
        return tickValues;
    }

    /**
     * Calculate a list of the data values for every minor tick mark
     *
     * @return List of data values where to draw minor tick marks
     */
    protected List<Number> calculateMinorTickMarks() {
        final List<Number> minorTickMarks = new ArrayList<>();
        final double lowerBound = getLowerBound();
        final double upperBound = getUpperBound();
        final double tickUnit = getTickUnit();
        final double minorUnit = tickUnit/Math.max(1, getMinorTickCount());
        if (tickUnit > 0) {
            if(((upperBound - lowerBound) / minorUnit) > 10000) {
                // This is a ridiculous amount of major tick marks, something has probably gone wrong
                System.err.println("Warning we tried to create more than 10000 minor tick marks on a BFANumberAxis. " +
                        "Lower Bound=" + getLowerBound() + ", Upper Bound=" + getUpperBound() + ", Tick Unit=" + tickUnit);
                return minorTickMarks;
            }
            for(int i = 0; i < upperBound/(tickUnit/5); i++){
                minorTickMarks.add(i * tickUnit / 5);
            }
            for(int i = 0; i > lowerBound/(tickUnit/5); i--){
                minorTickMarks.add(i * tickUnit / 5);
            }
            /*
            final boolean tickUnitIsInteger = Math.rint(tickUnit) == tickUnit;
            if (tickUnitIsInteger) {
                double minor = Math.floor(lowerBound) + minorUnit;
                int count = (int)Math.ceil((Math.ceil(lowerBound) - minor)/minorUnit);
                for (int i = 0; minor < Math.ceil(lowerBound) && i < count; minor += minorUnit, i++) {
                    if (minor > lowerBound) {
                        minorTickMarks.add(minor);
                    }
                }
            }
            double major = tickUnitIsInteger ? Math.ceil(lowerBound) : lowerBound;
            int count = (int)Math.ceil((upperBound - major)/tickUnit);
            for (int i = 0; major < upperBound && i < count; major += tickUnit, i++)  {
                final double next = Math.min(major + tickUnit, upperBound);
                double minor = major + minorUnit;
                int minorCount = (int)Math.ceil((next - minor)/minorUnit);
                for (int j = 0; minor < next && j < minorCount; minor += minorUnit, j++) {
                    minorTickMarks.add(minor);
                }
            }
            */
        }
        return minorTickMarks;
    }

    /**
     * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
     *
     * @param value tick mark value
     * @param range range to use during calculations
     * @return size of tick mark label for given value
     */
    @Override protected Dimension2D measureTickMarkSize(Number value, Object range) {
        final Object[] rangeProps = (Object[]) range;
        final String formatter = (String)rangeProps[4];
        return measureTickMarkSize(value, getTickLabelRotation(), formatter);
    }

    /**
     * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
     *
     * @param value     tick mark value
     * @param rotation  The text rotation
     * @param numFormatter The number formatter
     * @return size of tick mark label for given value
     */
    private Dimension2D measureTickMarkSize(Number value, double rotation, String numFormatter) {
        String labelText;
        StringConverter<Number> formatter = getTickLabelFormatter();
        if (formatter == null) formatter = defaultFormatter;
        if(formatter instanceof DefaultFormatter) {
            labelText = ((DefaultFormatter)formatter).toString(value, numFormatter);
        } else {
            labelText = formatter.toString(value);
        }
        return measureTickMarkLabelSize(labelText, rotation);
    }

    /**
     * Called to set the upper and lower bound and anything else that needs to be auto-ranged
     *
     * @param minValue The min data value that needs to be plotted on this axis
     * @param maxValue The max data value that needs to be plotted on this axis
     * @param length The length of the axis in display coordinates
     * @param labelSize The approximate average size a label takes along the axis
     * @return The calculated range
     */
    @Override protected Object autoRange(double minValue, double maxValue, double length, double labelSize) {
        return null;
    }

    // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------

     /** @treatAsPrivate implementation detail */
    private static class StyleableProperties {
        private static final CssMetaData<BFANumberAxis,Number> TICK_UNIT =
            new CssMetaData<BFANumberAxis,Number>("-fx-tick-unit",
                SizeConverter.getInstance(), 5.0) {

            @Override
            public boolean isSettable(BFANumberAxis n) {
                return n.tickUnit == null || !n.tickUnit.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(BFANumberAxis n) {
                return (StyleableProperty<Number>)(WritableValue<Number>)n.tickUnitProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
           final List<CssMetaData<? extends Styleable, ?>> styleables =
               new ArrayList<CssMetaData<? extends Styleable, ?>>(ValueAxis.getClassCssMetaData());
           styleables.add(TICK_UNIT);
           STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     * @since JavaFX 8.0
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    // -------------- INNER CLASSES ------------------------------------------------------------------------------------

    /**
     * Default number formatter for BFANumberAxis, this stays in sync with auto-ranging and formats values appropriately.
     * You can wrap this formatter to add prefixes or suffixes;
     * @since JavaFX 2.0
     */
    public static class DefaultFormatter extends StringConverter<Number> {
        private DecimalFormat formatter;
        private String prefix = null;
        private String suffix = null;

        /**
         * Construct a DefaultFormatter for the given BFANumberAxis
         *
         * @param axis The axis to format tick marks for
         */
        public DefaultFormatter(final BFANumberAxis axis) {
            formatter = axis.isAutoRanging()? new DecimalFormat(axis.currentFormatterProperty.get()) : new DecimalFormat();
            final ChangeListener<Object> axisListener = (observable, oldValue, newValue) -> {
                formatter = axis.isAutoRanging()? new DecimalFormat(axis.currentFormatterProperty.get()) : new DecimalFormat();
            };
            axis.currentFormatterProperty.addListener(axisListener);
            axis.autoRangingProperty().addListener(axisListener);
        }

        /**
         * Construct a DefaultFormatter for the given BFANumberAxis with a prefix and/or suffix.
         *
         * @param axis The axis to format tick marks for
         * @param prefix The prefix to append to the start of formatted number, can be null if not needed
         * @param suffix The suffix to append to the end of formatted number, can be null if not needed
         */
        public DefaultFormatter(BFANumberAxis axis, String prefix, String suffix) {
            this(axis);
            this.prefix = prefix;
            this.suffix = suffix;
        }

        /**
        * Converts the object provided into its string form.
        * Format of the returned string is defined by this converter.
        * @return a string representation of the object passed in.
        * @see StringConverter#toString
        */
        @Override public String toString(Number object) {
            return toString(object, formatter);
        }

        private String toString(Number object, String numFormatter) {
            if (numFormatter == null || numFormatter.isEmpty()) {
                return toString(object, formatter);
            } else {
                return toString(object, new DecimalFormat(numFormatter));
            }
        }

        private String toString(Number object, DecimalFormat formatter) {
            if (prefix != null && suffix != null) {
                return prefix + formatter.format(object) + suffix;
            } else if (prefix != null) {
                return prefix + formatter.format(object);
            } else if (suffix != null) {
                return formatter.format(object) + suffix;
            } else {
                return formatter.format(object);
            }
        }

        /**
        * Converts the string provided into a Number defined by the this converter.
        * Format of the string and type of the resulting object is defined by this converter.
        * @return a Number representation of the string passed in.
        * @see StringConverter#toString
        */
        @Override public Number fromString(String string) {
            try {
                int prefixLength = (prefix == null)? 0: prefix.length();
                int suffixLength = (suffix == null)? 0: suffix.length();
                return formatter.parse(string.substring(prefixLength, string.length() - suffixLength));
            } catch (ParseException e) {
                return null;
            }
        }
    }

}

/*
  // Code to generate tick unit defaults

  public static void main(String[] args) {
        List<BigDecimal> values = new ArrayList<BigDecimal>();
        List<String> formats = new ArrayList<String>();
        for(int power=-10; power <= 12; power ++) {
            BigDecimal val = new BigDecimal(10);
            val = val.pow(power, MathContext.DECIMAL32);
            BigDecimal val2 = val.multiply(new BigDecimal(2.5d));
            BigDecimal val5 = val.multiply(new BigDecimal(5d));
            values.add(val);
            values.add(val2);
            values.add(val5);
            System.out.print("["+power+"]  ");
            System.out.print(
                    val.doubleValue() + "d, " +
                            val2.doubleValue() + "d, " +
                            val5.doubleValue() + "d, "
            );
            DecimalFormat df = null;
            DecimalFormat dfTwoHalf = null;
            if (power < 0) {
                String nf = "0.";
                for (int i=0; i<Math.abs(power); i++) nf = nf+"0";
                System.out.print("    ---   nf = " + nf);
                String nf2 = "0.";
                for (int i=0; i<=Math.abs(power); i++) nf2 = nf2+"0";
                System.out.print("    ---   nf2 = " + nf2);
                df = new DecimalFormat(nf);
                dfTwoHalf = new DecimalFormat(nf2);
                formats.add(nf);
                formats.add(nf2);
                formats.add(nf);
            } else if (power == 0) {
                df = new DecimalFormat("0");
                dfTwoHalf = new DecimalFormat("0.0");
                formats.add("0");
                formats.add("0.0");
                formats.add("0");
            } else {
                String nf = "0";
                for (int i=0; i<Math.abs(power); i++) {
                    if((i % 3) == 2) {
                        nf = "#," + nf;
                    } else {
                        nf = "#" + nf;
                    }
                }
                System.out.print("    ---   nf = " + nf);
                formats.add(nf);
                formats.add(nf);
                formats.add(nf);
                dfTwoHalf = df = new DecimalFormat(nf);
            }
            System.out.println("        ---      "+
                    df.format(val.doubleValue())+", "+
                    dfTwoHalf.format(val2.doubleValue())+", "+
                    df.format(val5.doubleValue())+", "
            );
        }
        System.out.print("    private static final double[] TICK_UNIT_DEFAULTS = { ");
        for(BigDecimal val: values) System.out.print(val.doubleValue()+", ");
        System.out.println(" };");
        System.out.print("    private static final String[] TICK_UNIT_FORMATTER_DEFAULTS = { ");
        for(String format: formats) System.out.print("\""+format+"\", ");
        System.out.println(" };");
    }
*/
