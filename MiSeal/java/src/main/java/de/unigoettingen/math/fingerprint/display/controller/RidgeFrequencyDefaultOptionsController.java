package de.unigoettingen.math.fingerprint.display.controller;

import java.io.IOException;

import de.unigoettingen.math.fingerprint.display.Constants;
import de.unigoettingen.math.fingerprint.display.Validation;
import de.unigoettingen.math.fingerprint.display.dialog.InterpolationDialogHelper;
import de.unigoettingen.math.fingerprint.display.dialog.SmoothingDialogHelper;
import de.unigoettingen.math.fingerprint.interpolation.BilinearInterpolation;
import de.unigoettingen.math.fingerprint.interpolation.Interpolation;
import de.unigoettingen.math.fingerprint.interpolation.NearestNeighbourInterpolation;
import de.unigoettingen.math.fingerprint.interpolation.GaussianInterpolation;
import de.unigoettingen.math.fingerprint.smoothing.GaussianSmoothing2D;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing2D;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class RidgeFrequencyDefaultOptionsController implements RidgeFrequencyController {

    public TextField windowWidth;
    public TextField orientationWindowWidth;
    public ComboBox<String> interpolation;
    public Button interpolationConfig;
    public ComboBox<String> smoothing;
    public Button smoothingConfig;

    private Interpolation chosenInterpolation = new GaussianInterpolation(0, 9, 7, 16);
    private Smoothing2D chosenSmoothing = new GaussianSmoothing2D(25, 0, 25);

    public void initialize() {
        setDefaults();
    }

    public Interpolation getChosenInterpolation() {
        return chosenInterpolation;
    }

    public void onInterpolationComboChange() {
        switch (interpolation.getSelectionModel().getSelectedIndex()) {
            case 0: // bilinear
                interpolationConfig.setDisable(true);
                chosenInterpolation = new BilinearInterpolation();
                break;

            case 1: // nearest neighbour
                interpolationConfig.setDisable(true);
                chosenInterpolation = new NearestNeighbourInterpolation();
                break;

            case 2: // gaussian
                interpolationConfig.setDisable(false);
                chosenInterpolation = new GaussianInterpolation(0, 9, 7, Integer.parseInt(windowWidth.getText()) - 1);
                break;
        }
    }

    public void onClickConfigureInterpolation() throws IOException {
        chosenInterpolation = InterpolationDialogHelper.showDialogAndGet(InterpolationDialogHelper.TYPE_GAUSSIAN, chosenInterpolation);
    }

    public void onSmoothingComboChange() {
        boolean firstIndexSelected = smoothing.getSelectionModel().getSelectedIndex() == 0;
        smoothingConfig.setDisable(firstIndexSelected);
        if (firstIndexSelected) {
            chosenSmoothing = null;
        }
    }

    public void onClickConfigureSmoothing() throws IOException {
        int type = -1;
        switch (smoothing.getSelectionModel().getSelectedIndex()) {
            case 1:
                type = SmoothingDialogHelper.TYPE_AVERAGE;
                break;

            case 2:
                type = SmoothingDialogHelper.TYPE_GAUSSIAN;
                break;

            default:
                throw new RuntimeException("Unknown smoothing selected");
        }

        if (type >= 0) {
            chosenSmoothing = SmoothingDialogHelper.showDialogAndGet2D(type, chosenSmoothing);
        }
    }

    @Override
    public void setDeactivated(boolean deactivated) {
        windowWidth.setDisable(deactivated);
        orientationWindowWidth.setDisable(deactivated);
        interpolation.setDisable(deactivated);
        interpolationConfig.setDisable(deactivated);
        smoothing.setDisable(deactivated);
        smoothingConfig.setDisable(deactivated);

        if (!deactivated) {
            onInterpolationComboChange();
        }
    }

    @Override
    public void reset() {
        setDefaults();
    }

    @Override
    public void setDefaults() {
        windowWidth.setText("17");
        orientationWindowWidth.setText("33");

        interpolation.setItems(Constants.INTERPOLATIONS);
        interpolation.getSelectionModel().select(2);

        smoothing.setItems(Constants.SMOOTHINGS_2D);
        smoothing.getSelectionModel().select(2);

        onInterpolationComboChange();
    }

    @Override
    public void initializeValidation(Validation validation) {
        validation.positiveOddInteger(windowWidth, "the window width for the default ridge frequency");
        validation.positiveOddInteger(orientationWindowWidth, "the orientation window width for the default ridge frequency");
    }
}
