package com.bioforceanalytics.dashboard;
 
import java.util.ArrayList;
import java.util.Collections;
public class CustomAxisType{

    private String name;
    private double axisScalar;

    private static ArrayList<String> names;
    private static ArrayList<CustomAxisType> allCustomAxes;
    public CustomAxisType(String name, double axisScalar){
        if(names == null){
            names = new ArrayList<String>();
        }
        if(allCustomAxes == null){
            allCustomAxes = new ArrayList<CustomAxisType>();
        }
        this.axisScalar = axisScalar;
        if(names.contains(name)){
            this.name = name + "(" + Collections.frequency(names,name) +")";
        }else{
            this.name = name;
        }
        names.add(name);
        allCustomAxes.add(this);
    }
    public String getName(){
        return name;
    }
    public double getAxisScalar(){
        return axisScalar;
    }

    public static CustomAxisType getCustomAxisType(String name){
        for(CustomAxisType cat : allCustomAxes){
            if(cat.getName() == name) return cat;
        }
        return null;
    }
    
    
}