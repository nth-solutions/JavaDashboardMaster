module dashboard {

    requires java.sql;

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.media;

    requires org.apache.logging.log4j;

    requires purejavacomm;
    requires json.simple;
    requires jaffree;

    opens com.bioforceanalytics.dashboard;

}
