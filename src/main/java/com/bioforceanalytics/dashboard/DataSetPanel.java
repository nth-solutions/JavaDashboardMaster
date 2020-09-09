package com.bioforceanalytics.dashboard;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

/**
 * Custom JavaFX component for the Data Analysis Graph's panel of checkboxes toggling graphing of data sets.
 * Created so that data set panels can be dynamically created depending on the number of modules in a test.
 */
public class DataSetPanel extends TitledPane {

	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Signifies the axis (by parsing an AxisType) to graph onto the LineChart.
	 * This field is public so that it can be observed by GraphNoSINCController.
	 */
	// TODO implement Observable in AxisType so that this field can directly be an AxisType?
	public IntegerProperty currentAxis;

	/**
	 * The index of the GenericTest this DataSetPanel represents.
	 * This value is read by {@link com.bioforceanalytics.dashboard.GraphNoSINCController#graphAxis graphAxis}
	 * to signify which GenericTest to read data from and display on the line chart.
	 */
	private int GTIndex;

	private GraphNoSINCController controller;

	/**
	 * Creates a DataSetPanel JavaFX component representing a single module/GenericTest.
	 * @param GTIndex the index of the GenericTest this DataSetPanel represents
	 */
	public DataSetPanel(String title, int GTIndex) {

		this.GTIndex = GTIndex;
		this.currentAxis = new SimpleIntegerProperty();

		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/DataSetPanel.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		}
		catch (Exception e) {
			logger.error("Error loading DataSetPanel JavaFX component");
		}
		
		//Adds HamburgerMenu to DataSetPanel TitledPane
		HBox titleBox = new HBox();
		titleBox.setAlignment(Pos.CENTER);	
		titleBox.setPadding(new Insets(0, 25, 0, 0));
		titleBox.minWidthProperty().bind(this.widthProperty());

		HBox region = new HBox();
		region.setAlignment(Pos.CENTER);
		region.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(region, Priority.ALWAYS);

		Image img = new Image(getClass().getResource("images/HamburgerMenu.png").toExternalForm());
		ImageView view = new ImageView(img);
		view.setFitHeight(20);
		view.setPreserveRatio(true);
		
		MenuItem clear = new MenuItem("Clear Data Sets");
		MenuItem edit = new MenuItem("Edit");
		MenuItem remove = new MenuItem("Remove");
		
		MenuButton button = new MenuButton(null, view, clear, edit, remove);

		clear.setOnAction(e -> controller.clearGraph(GTIndex));

		edit.setOnAction(e -> renameDataSet());

		remove.setOnAction(e -> controller.removeGT(GTIndex));

		button.setStyle("-fx-border-color: transparent;");
		button.setStyle("-fx-border-width: 0;");
		button.setStyle("-fx-background-radius: 0;");
		button.setStyle("-background-color: transparent;");
		//button.setGraphic(view);

		Label label = new Label(title);
		label.setId("data-set-title");
		titleBox.getChildren().addAll(label, region, button);
		this.setGraphic(titleBox);

		// needed for "runLater()"
		Node ref = this;

		// ensures that FXML is loaded before code runs
		Platform.runLater(() -> {

			// if user right clicks on the title of the pane, allow them to rename it
			ref.lookup(".title").setOnMouseClicked(e -> {

				if (e.getButton().equals(MouseButton.SECONDARY)) {
					renameDataSet();
				}
	
			});

		});
		
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
	public void setCheckBox(boolean state, AxisType axis) {
		CheckBox c = (CheckBox) lookup("#Toggle" + axis);
		c.setSelected(state);
	}
	
	public int getGTIndex() { return GTIndex; }

	public void setController(GraphNoSINCController controller) {
		this.controller = controller;
	}

	/**
	 * Sets the title of this data set panel.
	 * @param text title of panel
	 */
	public void setTitle(String text) {
		Label l = (Label) this.lookup("#data-set-title");
		l.setText(text);
	}

	/**
	 * Gets the title of this data set panel.
	 * @return title of panel
	 */
	public String getTitle() {
		Label l = (Label) this.lookup("#data-set-title");
		return l.getText();
	}

	/**
	 * Renames this data set.
	 */
	public void renameDataSet() {

		TextInputDialog dialog = new TextInputDialog(getTitle());
		dialog.setTitle("Rename Data Set");
		dialog.setHeaderText("Rename Data Set");
		dialog.setContentText("Enter new data set name:");

		Optional<String> result = dialog.showAndWait();

		// set the new text of this TitledPane
		if (result.isPresent()) setTitle(result.get());

		controller.renameGT(result.get(), GTIndex);
	}
}