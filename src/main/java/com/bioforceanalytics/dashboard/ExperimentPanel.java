package com.bioforceanalytics.dashboard;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
/**
 * Custom JavaFX component for the Data Analysis Graph's accordion view, provies a 
 * place for the user to view the details of the specific experiment
 */
public class ExperimentPanel extends TitledPane {


	@FXML
	private Label experimentName;

	@FXML
	private Label param1;

	@FXML
	private Label param2;

	@FXML
	private Label param3;

	@FXML
	private Label param4;

	@FXML
	private TextField value1;

	@FXML
	private TextField value2;

	@FXML
	private TextField value3;

	@FXML
	private TextField value4;

	@FXML
	private Label formulaResult;

	private Label[] params;

	private TextField[] values;

	private ArrayList<String> paramNames;

	private ArrayList<String> paramValues;

	public void setExperimentName(String name){
		experimentName.setText(name);
	}
	public void addParamName(String name){
		paramNames.add(name);
	}
	public void addParamValue(String value){
		paramValues.add(value);
	}
	public void setFormulaResult(String result){
		formulaResult.setText(result);
	}

	public void applyParams(){
		for(int i = 0; i < paramNames.size(); i++){
			params[i].setText(paramNames.get(i));
			values[i].setText(paramValues.get(i));
		}
		for(int i = params.length - 1; i >= paramNames.size(); i--){
			params[i].setVisible(false);
			values[i].setVisible(false);
		}
	}

    public ExperimentPanel(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/ExperimentPanel.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		paramNames = new ArrayList<String>();
		paramValues = new ArrayList<String>();

        try {
			loader.load();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error loading ExperimentPanel JavaFX component");
		}
		params = new Label[]{param1,param2,param3,param4};
		values = new TextField[]{value1,value2,value3,value4};

    }

}