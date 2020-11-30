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
        return name;
    }
    public void setUnits(String name){
        this.name = name;
    }public String getEquation(){
        return name;
    }
    public void setEquation(String name){
        this.name = name;
    }
   
}
