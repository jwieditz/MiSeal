package de.jwieditz.miseal.display.controller;

import de.jwieditz.miseal.display.Validation;
import javafx.scene.control.TextField;

public class OrientationDefaultOptionsController implements OrientationController {

    public TextField sigmaQ;
    public TextField iterations;

    public void initialize() {
        setDefaults();
    }

    @Override
    public void reset() {
        setDefaults();
    }

    @Override
    public void setDefaults() {
        sigmaQ.setText("25");
        iterations.setText("1");
    }

    @Override
    public void initializeValidation(Validation validation) {
        validation.positiveDouble(sigmaQ, "sigmaQ for the default orientation");
        validation.positiveInteger(iterations, "the number of iterations for the default orientation");
    }

}
