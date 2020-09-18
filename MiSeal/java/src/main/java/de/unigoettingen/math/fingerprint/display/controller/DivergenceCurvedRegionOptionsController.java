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
import de.unigoettingen.math.fingerprint.smoothing.AverageSmoothing;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class DivergenceCurvedRegionOptionsController implements DivergenceController {

    public TextField p;
    public TextField q;
    public CheckBox useRealDistance;
    public CheckBox removeLineDivergence;
    public ComboBox<String> orientationInterpolation;
    public ComboBox<String> smoothing;
    public Button orientationInterpolationConfig;
    public Button smoothingConfig;

    private Interpolation chosenOrientationInterpolation = new NearestNeighbourInterpolation();
    private Smoothing2D chosenSmoothing = new AverageSmoothing(49);

    public void initialize() {
        setDefaults();
    }

    public Interpolation getChosenOrientationInterpolation() {
        return chosenOrientationInterpolation;
    }

    public Smoothing2D getChosenSmoothing() {
        return chosenSmoothing;
    }

    public void onClickConfigureOrientationInterpolation() throws IOException {
        chosenOrientationInterpolation = InterpolationDialogHelper.showDialogAndGet(InterpolationDialogHelper.TYPE_GAUSSIAN, chosenOrientationInterpolation);
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
                throw new RuntimeException("unknown smoothing selected");
        }
        if (type >= 0) {
            chosenSmoothing = SmoothingDialogHelper.showDialogAndGet2D(type, chosenSmoothing);
        }
    }

    public void onOrientationInterpolationComboChange() {
        switch (orientationInterpolation.getSelectionModel().getSelectedIndex()) {
            case 0: // bilinear
                orientationInterpolationConfig.setDisable(true);
                chosenOrientationInterpolation = new BilinearInterpolation();
                break;

            case 1: // nearest neighbour
                orientationInterpolationConfig.setDisable(true);
                chosenOrientationInterpolation = new NearestNeighbourInterpolation();
                break;

            case 2: // gaussian
                orientationInterpolationConfig.setDisable(false);
                chosenOrientationInterpolation = new GaussianInterpolation(0, 1, 1, 1);
                break;
        }
    }

    public void onSmoothingComboChange() {
        boolean firstIndexSelected = smoothing.getSelectionModel().getSelectedIndex() == 0;
        smoothingConfig.setDisable(firstIndexSelected);
        if (firstIndexSelected) {
            chosenSmoothing = null;
        }
    }

    @Override
    public void setDeactivated(boolean deactivated) {
        p.setDisable(deactivated);
        q.setDisable(deactivated);
        useRealDistance.setDisable(deactivated);
        removeLineDivergence.setDisable(deactivated);
        orientationInterpolation.setDisable(deactivated);
        smoothing.setDisable(deactivated);
        orientationInterpolationConfig.setDisable(deactivated);
        smoothingConfig.setDisable(deactivated);

        if (!deactivated) {
            onOrientationInterpolationComboChange();
        }
    }

    @Override
    public void reset() {
        setDefaults();
    }

    @Override
    public void setDefaults() {
        p.setText("16");
        q.setText("32");
        useRealDistance.setSelected(true);
        removeLineDivergence.setSelected(true);

        orientationInterpolation.setItems(Constants.INTERPOLATIONS);
        orientationInterpolation.getSelectionModel().select(1);

        smoothing.setItems(Constants.SMOOTHINGS_2D);
        smoothing.getSelectionModel().select(1);

        onOrientationInterpolationComboChange();
        onSmoothingComboChange();
    }

    @Override
    public void initializeValidation(Validation validation) {
        validation.positiveInteger(p, "p in the curved region divergence options");
        validation.positiveInteger(q, "q in the curved region divergence options");
    }
}
