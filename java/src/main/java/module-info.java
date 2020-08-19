module de.unigoettingen.math.fingerprint {
    requires javafx.controls;
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.swing;
    requires jcommander;
    requires org.controlsfx.controls;

    exports de.unigoettingen.math.fingerprint;
    exports de.unigoettingen.math.fingerprint.display;
    exports de.unigoettingen.math.fingerprint.display.controller;
    exports de.unigoettingen.math.fingerprint.display.dialog;
    exports de.unigoettingen.math.fingerprint.cli;

    opens de.unigoettingen.math.fingerprint.display to javafx.fxml;
    opens de.unigoettingen.math.fingerprint.display.controller to javafx.fxml;
    opens de.unigoettingen.math.fingerprint.display.dialog to javafx.fxml;
}