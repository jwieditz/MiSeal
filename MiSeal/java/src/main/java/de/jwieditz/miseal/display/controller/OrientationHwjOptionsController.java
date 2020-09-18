package de.jwieditz.miseal.display.controller;

import de.jwieditz.miseal.display.Validation;
import javafx.scene.control.CheckBox;

public class OrientationHwjOptionsController implements OrientationController {

    public CheckBox useLowPassFilter;

    public void initialize() {
        setDefaults();
    }

    @Override
    public void reset() {
        setDefaults();
    }

    @Override
    public void setDefaults() {
        useLowPassFilter.setSelected(false);
    }

    @Override
    public void initializeValidation(Validation validation) {
        // no validation required
    }
}
