package com.bioforceanalytics.dashboard;

public class Variable {
    
    private String name;
    private double value;

    public Variable (String name, double value){
        this.name = name;
        this.value = value;
    }

    public String getName(){
        return name;
    }
    public double getValue(){
        return value;
    }
}
