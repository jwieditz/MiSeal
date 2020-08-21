package de.unigoettingen.math.fingerprint.display;

import javafx.scene.control.TextField;

public class TextFieldUtil {

    private TextFieldUtil() {}

    public static int getIntOrDefault(TextField source, int def) {
        if (source == null) {
            throw new NullPointerException("source == null");
        }
        String text = source.getText();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static double getDoubleOrDefault(TextField source, double def) {
        if (source == null) {
            throw new NullPointerException("source == null");
        }
        String text = source.getText();
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
