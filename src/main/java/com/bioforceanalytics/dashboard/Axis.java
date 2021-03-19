package com.bioforceanalytics.dashboard;

import java.util.HashMap;

/**
 * Axis is an interface for both AxisType (which is used for data from the module) and CustomAxisType (which is used for newly created CustomAxes)
 * It has a set of common getters and setters that allow the code to treat both axes in the same way
 * 
 * Also imporant to note is that this interface has a static reference to all axes that are currently exist
 */
interface Axis{
    public String getName();
    public String getUnits();
    public boolean isCustomAxis();
    public double getAxisScalar();
    public String getNameAndUnits();
    public int getIndex();
    public int getValue();
    
    public static HashMap<String,Axis> axisNameMap = new HashMap<String,Axis>();
    public static void addAxis(String name, Axis axis){
        axisNameMap.put(name,axis);
    }
    public static Axis getAxis(String name){
        return axisNameMap.get(name);
    }

}