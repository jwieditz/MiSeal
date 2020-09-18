package de.jwieditz.miseal.display.controller;

import de.jwieditz.miseal.display.Deactivatable;
import de.jwieditz.miseal.display.Validation;
import javafx.scene.control.TextField;

public class NormalizationController implements IController, Deactivatable {

    public TextField mean;
    public TextField variance;

    @Override
    public void reset() {
        setDefaults();
    }

    @Override
    public void setDefaults() {
        mean.setText("0");
        variance.setText("1");
    }

    @Override
    public void setDeactivated(boolean deactivated) {
        mean.setDisable(deactivated);
        variance.setDisable(deactivated);
    }

    @Override
    public void initializeValidation(Validation validation) {
        validation.positiveOfNegativeDouble(mean, "the normalization mean");
        validation.positiveDouble(variance, "the normalization variance");
    }
}
