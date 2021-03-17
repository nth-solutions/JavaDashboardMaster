package com.bioforceanalytics.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.control.CheckBox;

/**
 * CustomTest extends GenericTest and acts as a way for the GraphNoSINCController code to read custom axes as if they were on the traditional layout of the GenericTest system
 */
class CustomTest extends GenericTest{

    private HashMap<Axis,AxisDataSeries> axisMap;
    // keeps track of which checkbox controls which axis
    private HashMap<Axis,CheckBox> axisCheckboxMap; 
    public ArrayList<AxisDataSeries> customAxes;

    /**
     * Creates a new empty CustomTest object
     */
    public CustomTest(){
        super();
        axisMap = new HashMap<Axis,AxisDataSeries>();
        axisCheckboxMap = new HashMap<Axis,CheckBox>();
        customAxes = new ArrayList<AxisDataSeries>();
    }
    /**
     * Adds an AxisDataSeries object to the CustomTest, which allows it to be graphed
     * @param ads the AxisDataSeries object
     * @param axisType the Axis type
     * @param checkbox the Checkbox that controls that axis
     */
    public void addAxisDataSeries(AxisDataSeries ads, Axis axisType, CheckBox checkbox){
        customAxes.add(ads);
        axisMap.put(axisType,ads);
        axisCheckboxMap.put(axisType,checkbox);
    }
    public AxisDataSeries getAxis(Axis a){
        return axisMap.get(a);
    }
    public CheckBox getCheckBox(Axis axis){
        return axisCheckboxMap.get(axis);
    }

}