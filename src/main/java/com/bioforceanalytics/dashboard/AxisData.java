package com.bioforceanalytics.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.aspose.cells.Axis;
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


    public static AxisData getAxisData(String name){
        for(AxisData ad : allAxisData){
            if(ad.name == name){
                return ad;
            }
        }
        return null;
    }
    public AxisData (List<Double> data, String name){
        this.data = new Double[data.size()];
        for (int i = 0; i < data.size(); i++) {
                this.data[i] = data.get(i); 
        }
        this.name = name;
        allAxisData.add(this);
    }
    public AxisData (Double[] data, String name){
        this.data = data;
        this.name = name;
        allAxisData.add(this);
    }
    public String toString(){
        String result = "";
        for(Double a : data){
            result += (a+", ");
        }
        return result;
    }
    private int minLength(AxisData a, AxisData b){
        return a.getData().length < b.getData().length ? a.getData().length : b.getData().length;
    }
    public Double[] getData(){
        return data;
    }
    public Double getIndex(int i){
        return data[i];
    }
    public AxisData add(AxisData b, String newName){
        AxisData a = this;
        int length = minLength(a, b);
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) + b.getIndex(i);
        }
        return new AxisData(res,newName);
    }
    public AxisData add(double c, String newName){
        AxisData a = this;
        int length = a.getData().length;
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) + c;
            
        }
        return new AxisData(res,newName);
    }
    public AxisData subtract(AxisData b, String newName){
        AxisData a = this;
        int length = minLength(a, b);
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) - b.getIndex(i);
        }
        return new AxisData(res,newName);
    }
    public AxisData subtract(double c, String newName){
        AxisData a = this;
        int length = a.getData().length;
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) - c;
        }
        return new AxisData(res,newName);
    }
    public AxisData multiply(AxisData b, String newName){
        AxisData a = this;
        int length = minLength(a, b);
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) * b.getIndex(i);
        }
        return new AxisData(res,newName);
    }
    public AxisData multiply(double c, String newName){
        AxisData a = this;
        int length = a.getData().length;
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) * c;
        }
        return new AxisData(res,newName);
    }
    public AxisData divide(AxisData b, String newName){
        AxisData a = this;
        int length = minLength(a, b);
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) / b.getIndex(i);
        }
        return new AxisData(res,newName);
    }
    public AxisData divide(double c, String newName){
        AxisData a = this;
        int length = a.getData().length;
        Double[] res = new Double[length];
        for(int i = 0; i < length; i++){
            res[i] = a.getIndex(i) / c;
        }
        return new AxisData(res,newName);
    }
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