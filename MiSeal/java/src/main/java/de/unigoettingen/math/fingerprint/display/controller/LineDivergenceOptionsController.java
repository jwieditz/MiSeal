package de.unigoettingen.math.fingerprint.display.controller;

import java.io.IOException;

import de.unigoettingen.math.fingerprint.display.Constants;
import de.unigoettingen.math.fingerprint.display.Deactivatable;
import de.unigoettingen.math.fingerprint.display.Validation;
import de.unigoettingen.math.fingerprint.display.dialog.SmoothingDialogHelper;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing2D;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class LineDivergenceOptionsController implements IController, Deactivatable {

    public ComboBox<String> smoothing;
    public Button smoothingConfig;

    private Smoothing2D chosenSmoothing;

    public void initialize() {
        setDefaults();
    }

    public Smoothing2D getChosenSmoothing() {
        return chosenSmoothing;
    }

    public void onSmoothingComboChange() {
        boolean firstIndexSelected = smoothing.getSelectionModel().getSelectedIndex() == 0;
        smoothingConfig.setDisable(firstIndexSelected);
        if (firstIndexSelected) {
            chosenSmoothing = null;
        }
    }

    public void onClickConfigureSmoothing() throws IOException {
        int type;
        switch (smoothing.getSelectionModel().getSelectedIndex()) {
            case 1:
                type = SmoothingDialogHelper.TYPE_AVERAGE;
                break;

            case 2:
                type = SmoothingDialogHelper.TYPE_GAUSSIAN;
                break;

            default:
                throw new RuntimeException("unknown smoothing selected");
        }
        chosenSmoothing = SmoothingDialogHelper.showDialogAndGet2D(type, chosenSmoothing);
    }

    @Override
    public void reset() {
        setDefaults();
    }

    @Override
    public void setDefaults() {
        smoothing.setItems(Constants.SMOOTHINGS_2D);
        smoothing.getSelectionModel().select(0);

        onSmoothingComboChange();
    }

    @Override
    public void initializeValidation(Validation validation) {
        // no validation required
    }

    @Override
    public void setDeactivated(boolean deactivated) {
        smoothing.setDisable(deactivated);
        smoothingConfig.setDisable(deactivated);
    }
}
