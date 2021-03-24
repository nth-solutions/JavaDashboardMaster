package com.bioforceanalytics.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used by the CustomAxisCreation feature to temporarily organize AxisDataSeries
 */
public class AxisData {

    private Double[] data;
    private String name;
    private int sampleRate;
    private Axis axis;
    public static List<AxisData> allAxisData = new ArrayList<AxisData>();
    public static Map<String,AxisData> nameAxisDataMap = new HashMap<String,AxisData>();

    /**
     * Finds a specific axis-data object based on it's name
     * @param name the name of the axisdata
     * @return the specified axisdata object
     */
    public static AxisData getAxisData(String name){
        for(AxisData ad : allAxisData){
            if(ad.name == name){
                return ad;
            }
        }
        return null;
    }
    /**
     * Creates a new AxisData object with the given data and name
     * @param data the Double List of raw values that the AxisData object will keep track of
     * @param name the identifier of this specific AxisData object. Must be unique
     */
    public AxisData (List<Double> data, String name){
        this.data = new Double[data.size()];
        for (int i = 0; i < data.size(); i++) {
                this.data[i] = data.get(i); 
        }
        this.name = name;
        allAxisData.add(this);
        
    }
    /**
     * Creates a new AxisData object with the given data and name
     * @param data the Double array of raw values that the AxisData object will keep track of
     * @param name the identifier of this specific AxisData object. Must be unique
     */
    public AxisData (Double[] data, String name){
        this.data = data;
        this.name = name;
        allAxisData.add(this);
    }
    /**
     * retuns all of the data in a comma'd list
     */
    public String toString(){
        String result = "";
        for(Double a : data){
            result += (a+", ");
        }
        return result;
    }
    /**
     * returns the small of the two lengths between two AxisData objects
     * @param a instance A of AxisData
     * @param b instance B of AxisData
     * @return smallest length
     */
    private int minLength(AxisData a, AxisData b){
        return a.getData().length < b.getData().length ? a.getData().length : b.getData().length;
    }
    public Double[] getData(){
        return data;
    }
    /**
     * get the value of the data at index i
     * @param i the index
     * @return the value of index i
     */
    public Double getIndex(int i){
        return data[i];
    }
    /**
     * Combines this AxisData with another AxisData through the addition operation
     * @param b the AxisData to be added
     * @param newName the identifier of the result
     * @return the result of the addition (a new AxisData object)
     */
    public AxisData add(AxisData b, String newName){
        AxisData a = this;
        int length = minLength(a, b);
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) + b.getIndex(i);
        }
        return new AxisData(res,newName);
    }
    /**
     * Combines this AxisData with a constant through the addition operation
     * @param c the constant
     * @param newName the identifier of the result
     * @return the result of the addition (a new AxisData object)
     */
    public AxisData add(double c, String newName){
        AxisData a = this;
        int length = a.getData().length;
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) + c;
            
        }
        return new AxisData(res,newName);
    }
      /**
     * Combines this AxisData with another AxisData through the subtraction operation
     * @param b the AxisData to be subtracted
     * @param newName the identifier of the result
     * @return the result of the addition (a new AxisData object)
     */
    public AxisData subtract(AxisData b, String newName){
        AxisData a = this;
        int length = minLength(a, b);
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) - b.getIndex(i);
        }
        return new AxisData(res,newName);
    }
    /**
     * Combines this AxisData with a constant through the subtraction operation
     * @param c the constant
     * @param newName the identifier of the result
     * @return the result of the subtraction (a new AxisData object)
     */
    public AxisData subtract(double c, String newName){
        AxisData a = this;
        int length = a.getData().length;
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) - c;
        }
        return new AxisData(res,newName);
    }
     /**
     * Combines this AxisData with another AxisData through the multiplication operation
     * @param b the AxisData to be multiplied by
     * @param newName the identifier of the result
     * @return the result of the multiplication (a new AxisData object)
     */
    public AxisData multiply(AxisData b, String newName){
        AxisData a = this;
        int length = minLength(a, b);
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) * b.getIndex(i);
        }
        return new AxisData(res,newName);
    }
    /**
     * Combines this AxisData with a constant through the multiplication operation
     * @param c the constant
     * @param newName the identifier of the result
     * @return the result of the multiplication (a new AxisData object)
     */
    public AxisData multiply(double c, String newName){
        AxisData a = this;
        int length = a.getData().length;
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) * c;
        }
        return new AxisData(res,newName);
    }
    /**
    * Combines this AxisData with another AxisData through the division operation
     * @param b the AxisData to be divided by (this is the QUOTIENT)
     * @param newName the identifier of the result
     * @return the result of the division (a new AxisData object)
     */
    public AxisData divide(AxisData b, String newName){
        AxisData a = this;
        int length = minLength(a, b);
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) / b.getIndex(i);
        }
        return new AxisData(res,newName);
    }
    /**
     * Combines this AxisData with a constant through the division operation
     * @param c the constant (this is the QUOTIENT)
     * @param newName the identifier of the result
     * @return the result of the division (a new AxisData object)
     */
    public AxisData divide(double c, String newName){
        AxisData a = this;
        int length = a.getData().length;
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) / c;
        }
        return new AxisData(res,newName);
    }
    /**
     * Combines this AxisData with a constant through the exponentiation operation
     * @param c the constant
     * @param newName the identifier of the result
     * @return the result of the exponentiation (a new AxisData object)
     */
    public AxisData exp(double c, String newName){
        AxisData a = this;
        int length = a.getData().length;
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = Math.pow(a.getIndex(i),c);
        }
        return new AxisData(res,newName);
    }

}