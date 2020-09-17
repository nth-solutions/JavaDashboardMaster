package com.bioforceanalytics.dashboard;
 
import java.util.ArrayList;
import java.util.Collections;
public class CustomAxisType implements Axis{

    private String name;
    private String units;
    private double axisScalar;

    private static ArrayList<String> names;
    private static ArrayList<CustomAxisType> allCustomAxes;
    public CustomAxisType(String name, String units, double axisScalar){
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
        this.units = units;
        allCustomAxes.add(this);
    }
    public String getName(){
        return name;
    }
    public double getAxisScalar(){
        return axisScalar;
    }

    public static CustomAxisType getCustomAxisByIndex(int index){
        if(index < allCustomAxes.size()) return allCustomAxes.get(index);
        return null;
    }
    public static CustomAxisType getCustomAxisType(String name){
        for(CustomAxisType cat : allCustomAxes){
            if(cat.getName() == name) return cat;
        }
        return null;
    }
    public int getIndex(){
        return allCustomAxes.indexOf(this);
    }

    @Override
    public String getUnits() {
        return units;
    }

    @Override
    public boolean isCustomAxis() {
        return true;
    }

    @Override
    public String getNameAndUnits() {
        return getName() +" (" + getUnits() +")";
    }

    public String toString(){
        return name;
    }

    @Override
    public int getValue() {
        return -1;
    }
    
    
}