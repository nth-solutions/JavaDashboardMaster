package dataorganizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardSelector extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("DashboardSelector.fxml"));
        primaryStage.setTitle("Dashboard Selector");
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        //scene.getStylesheets().add(getClass().getResource("EducatorModeCSS.css").toExternalForm());
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    public static void main(String[] args){
        launch(args);
    }

}

