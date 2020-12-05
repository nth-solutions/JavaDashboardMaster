package com.bioforceanalytics.dashboard;

import javafx.fxml.FXML;

public class CustomEquation {
    String name;
    String units;
    String equation;

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
