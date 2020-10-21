package com.bioforceanalytics.dashboard;

import java.util.HashMap;

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