package com.bioforceanalytics.dashboard;

interface Axis{
    public String getName();
    public String getUnits();
    public boolean isCustomAxis();
    public double getAxisScalar();
    public String getNameAndUnits();
    public int getIndex();
    public int getValue();
}