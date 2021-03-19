package com.bioforceanalytics.dashboard;
 
import java.util.ArrayList;
import java.util.Collections;

/**
 * An alternative to AxisType for CustomAxes, as there can be an unknown number of these objects
 */
public class CustomAxisType implements Axis{

    private String name;
    private String units;
    private double axisScalar;

    private static ArrayList<String> names;
    //a static refernce to all existing custom axes
    private static ArrayList<CustomAxisType> allCustomAxes;
    /**
     * A construction for a new custom axis type
     * @param name the name of the custom axis (e.g. momentum)
     * @param units the units of the custom axis (e.g. kg-m/s)
     * @param axisScalar the scale of the axis (e.g. 10)
     */
    public CustomAxisType(String name, String units, double axisScalar){
        if(names == null){
            names = new ArrayList<String>();
        }
        if(allCustomAxes == null){
            allCustomAxes = new ArrayList<CustomAxisType>();
        }
        this.axisScalar = axisScalar;
        if(names.contains(name)){
            //this checks if there is any other axis with the same name, and if so adds an identifying number to the end of the axis for clarity
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
   /**
     * Allows you to reference any custom axis by name
     * @param index the index of the custom axis
     * @return the custom axis type
     */
    public static CustomAxisType getCustomAxisByIndex(int index){
        if(index < allCustomAxes.size()) return allCustomAxes.get(index);
        return null;
    }
    /**
     * Allows you to reference any custom axis by name
     * @param name the name of the custom axis
     * @return the custom axis type
     */
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
    public static void reset(){
        names = new ArrayList<String>();
        allCustomAxes = new ArrayList<CustomAxisType>();
    }
    
}