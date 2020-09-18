module de.jwieditz.miseal {
    requires javafx.controls;
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.swing;
    requires jcommander;
    requires org.controlsfx.controls;

    exports de.jwieditz.miseal;
    exports de.jwieditz.miseal.display;
    exports de.jwieditz.miseal.display.controller;
    exports de.jwieditz.miseal.display.dialog;
    exports de.jwieditz.miseal.cli;

    opens de.jwieditz.miseal.display to javafx.fxml;
    opens de.jwieditz.miseal.display.controller to javafx.fxml;
    opens de.jwieditz.miseal.display.dialog to javafx.fxml;
}