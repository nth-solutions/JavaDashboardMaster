package com.bioforceanalytics.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.control.CheckBox;

class CustomTest extends GenericTest{

    private HashMap<Axis,AxisDataSeries> axisMap;
    private HashMap<Axis,CheckBox> axisCheckboxMap;
	private List<List<Double>> dataSamples;
	private String graphTitle;
    private Axis[] defaultAxes;

    private static final Logger logger = LogManager.getLogger();

    public CustomTest(){
        axisMap = new HashMap<Axis,AxisDataSeries>();
        axisCheckboxMap = new HashMap<Axis,CheckBox>();
    }
    public void addAxisDataSeries(AxisDataSeries ads, Axis axisType, CheckBox checkbox){
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