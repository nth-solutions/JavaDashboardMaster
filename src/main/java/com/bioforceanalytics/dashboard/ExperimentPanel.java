package com.bioforceanalytics.dashboard;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.CheckBox;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleIntegerProperty;
/**
 * Custom JavaFX component for the BioForce Graph's accordion view,
 * provides a place for the user to view the details of the specific experiment type.
 */
public class ExperimentPanel extends TitledPane {

	private static final Logger logger = LogController.start();

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

	@FXML
	private CheckBox toggleAxis1;

	@FXML
	private CheckBox toggleAxis2;

	@FXML
	private CheckBox toggleAxis3;

	@FXML
	private CheckBox toggleAxis4;

	

	public IntegerProperty currentAxis;

	

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
		this.currentAxis = new SimpleIntegerProperty();
		paramNames = new ArrayList<String>();
		paramValues = new ArrayList<String>();

        try {
			loader.load();
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Error loading ExperimentPanel JavaFX component");
		}
		params = new Label[]{param1,param2,param3,param4};
		values = new TextField[]{value1,value2,value3,value4};

    }
	@FXML private void chooseGraphAxis(ActionEvent event){
		CheckBox c = (CheckBox)event.getSource();
		String axisID = (String) c.getId().replace("toggleAxis", "");
		Axis a = CustomAxisType.getCustomAxisByIndex(Integer.parseInt(axisID));
		//logger.info("event on " + a);
		currentAxis.set(-1);

		// convert AxisType to int
		currentAxis.set(a.getIndex());
	}
	/**
	 * Sets the state of a checkbox in the experiment panel.
	 * @param state whether the checkbox should be ticked
	 */
	public void setCheckBox(boolean state, Axis axis) {
		CheckBox c = (CheckBox) lookup("#toggleAxis" + axis.getIndex());
		c.setSelected(state);
	}
}