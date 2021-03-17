package com.bioforceanalytics.dashboard;

import javafx.fxml.FXML;

/**
 * This is a helper class to keep track of the three sets of information regarding equations in one object
 */
public class CustomEquation {
    String name;
    String units;
    String equation;
    /**
     * Creates a new CustomEquation object
     * @param name the name of the equation
     * @param equation the equation
     * @param units the units of the custom axis
     */
    public CustomEquation(String name, String equation, String units){
        this.name = name;
        this.units = units;
        this.equation = equation;
    }
    
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getUnits(){
        return units;
    }
    public void setUnits(String units){
        this.units = units;
    }public String getEquation(){
        return equation;
    }
    public void setEquation(String equation){
        this.equation = equation;
    }
   
}
