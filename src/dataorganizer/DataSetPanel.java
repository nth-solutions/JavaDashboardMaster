package dataorganizer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;

/**
 * Custom JavaFX component for the Data Analysis Graph's panel of checkboxes toggling graphing of data sets.
 * Created so that data set panels can be dynamically created depending on the number of modules in a test.
 */
public class DataSetPanel extends TitledPane {
	
	/**
	 * Signifies the axis (by parsing an AxisType) to graph onto the LineChart.
	 * This field is public so that it can be observed by GraphNoSINCController.
	 */
	// TODO implement Observable in AxisType so that this field can directly be an AxisType?
	public IntegerProperty currentAxis;

	/**
	 * The index of the GenericTest this DataSetPanel represents.
	 * This value is read by {@link dataorganizer.GraphNoSINCController#graphAxis graphAxis}
	 * to signify which GenericTest to read data from and display on the line chart.
	 */
	private int GTIndex;

	/**
	 * No argument constructor called when parsing the FXML associated with the Data Analysis Graph.
	 * Under normal circumstances, this constructor will not be called, as DataSetPanels are instantiated in GraphNoSINCController.
	 */
	public DataSetPanel() {
		this(0);
	}

	/**
	 * Creates a DataSetPanel JavaFX component representing a single module/GenericTest.
	 * @param GTIndex the index of the GenericTest this DataSetPanel represents
	 */
	public DataSetPanel(int GTIndex) {

		this.GTIndex = GTIndex;
		this.currentAxis = new SimpleIntegerProperty();

		FXMLLoader loader = new FXMLLoader(getClass().getResource("DataSetPanel.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error loading DataSetPanel JavaFX component");
		}

	}

	@FXML
	public void chooseGraphAxis(ActionEvent event) {

		// get AxisType from checkbox
		CheckBox c = (CheckBox) event.getSource();
		String axis = (String) c.getId().replace("Toggle", "");
		AxisType a = AxisType.valueOf(axis);

		// TODO currently using a hack by switching to -1, then to the original value
		// this ensures that a change event is fired and data is passed to parent
		// maybe this can be cleaned up later?
		currentAxis.set(-1);

		// convert AxisType to int
		currentAxis.set(a.getValue());

	}

	/**
	 * Sets the state of a checkbox in the data set panel.
	 * @param state whether the checkbox should be ticked
	 */
	public void setCheckBox(boolean state) {
		CheckBox c = (CheckBox) lookup("#Toggle" + AxisType.valueOf(currentAxis.get()));
		c.setSelected(state);
	}
	
	public int getGTIndex() { return GTIndex; }

}