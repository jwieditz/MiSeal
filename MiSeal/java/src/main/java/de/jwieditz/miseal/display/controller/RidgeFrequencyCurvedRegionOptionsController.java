package de.jwieditz.miseal.display.controller;

import java.io.IOException;

import de.jwieditz.miseal.interpolation.Interpolation;
import de.jwieditz.miseal.interpolation.NearestNeighbourInterpolation;
import de.jwieditz.miseal.smoothing.AverageSmoothing;
import de.jwieditz.miseal.smoothing.GaussianSmoothing;
import de.jwieditz.miseal.smoothing.Smoothing1D;
import de.jwieditz.miseal.smoothing.Smoothing2D;
import de.jwieditz.miseal.display.Constants;
import de.jwieditz.miseal.display.Validation;
import de.jwieditz.miseal.display.dialog.InterpolationDialogHelper;
import de.jwieditz.miseal.display.dialog.SmoothingDialogHelper;
import de.jwieditz.miseal.interpolation.BilinearInterpolation;
import de.jwieditz.miseal.interpolation.GaussianInterpolation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class RidgeFrequencyCurvedRegionOptionsController implements RidgeFrequencyController {

    public TextField p;
    public TextField q;
    public ComboBox<String> orientationInterpolation;
    public ComboBox<String> profileSmoothing;
    public TextField profileSmoothingIterations;
    public ComboBox<String> greyValueInterpolation;
    public ComboBox<String> finalSmoothing;
    public Button orientationInterpolationConfig;
    public Button profileSmoothingConfig;
    public Button greyValueInterpolationConfig;
    public Button finalSmoothingConfig;

    private Interpolation chosenOrientationInterpolation = new NearestNeighbourInterpolation();
    private Smoothing1D chosenProfileSmoothing = new GaussianSmoothing(7, 0, 1);
    private Interpolation chosenGreyValueInterpolation = new NearestNeighbourInterpolation();
    private Smoothing2D chosenFinalSmoothing = new AverageSmoothing(49);

    public void initialize() {
        setDefaults();
    }

    public Interpolation getChosenOrientationInterpolation() {
        return chosenOrientationInterpolation;
    }

    public Smoothing1D getChosenProfileSmoothing() {
        return chosenProfileSmoothing;
    }

    public Interpolation getChosenGreyValueInterpolation() {
        return chosenGreyValueInterpolation;
    }

    public Smoothing2D getChosenFinalSmoothing() {
        return chosenFinalSmoothing;
    }

    public void onClickConfigureOrientationInterpolation() throws IOException {
        chosenOrientationInterpolation = InterpolationDialogHelper.showDialogAndGet(InterpolationDialogHelper.TYPE_GAUSSIAN, chosenOrientationInterpolation);
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

    public void onClickConfigureProfileSmoothing() throws IOException {
        int type = -1;
        switch (profileSmoothing.getSelectionModel().getSelectedIndex()) {
            case 0:
                chosenProfileSmoothing = null;
                break;

            case 1:
                type = SmoothingDialogHelper.TYPE_GAUSSIAN;
                break;

            default:
                throw new RuntimeException("unknown smoothing selected");
        }
        if (type > 0) {
            chosenProfileSmoothing = SmoothingDialogHelper.showDialogAndGet1D(type, chosenProfileSmoothing);
        }
    }

    public void onProfileSmoothingComboChange() {
        boolean firstIndexSelected = profileSmoothing.getSelectionModel().getSelectedIndex() == 0;
        profileSmoothingConfig.setDisable(firstIndexSelected);
        if (firstIndexSelected) {
            chosenProfileSmoothing = null;
        }
    }

    public void onClickConfigureGreyValueInterpolation() throws IOException {
        chosenGreyValueInterpolation = InterpolationDialogHelper.showDialogAndGet(InterpolationDialogHelper.TYPE_GAUSSIAN, chosenOrientationInterpolation);
    }

    public void onGreyValueInterpolationComboChange() {
        switch (greyValueInterpolation.getSelectionModel().getSelectedIndex()) {
            case 0: // bilinear
                greyValueInterpolationConfig.setDisable(true);
                chosenGreyValueInterpolation = new BilinearInterpolation();
                break;

            case 1: // nearest neighbour
                greyValueInterpolationConfig.setDisable(true);
                chosenGreyValueInterpolation = new NearestNeighbourInterpolation();
                break;

            case 2: // gaussian
                greyValueInterpolationConfig.setDisable(false);
                chosenGreyValueInterpolation = new GaussianInterpolation(0, 1, 1, 1);
                break;
        }
    }

    public void onClickConfigureFinalSmoothing() throws IOException {
        int type = -1;
        switch (finalSmoothing.getSelectionModel().getSelectedIndex()) {
            case 1:
                type = SmoothingDialogHelper.TYPE_AVERAGE;
                break;

            case 2:
                type = SmoothingDialogHelper.TYPE_GAUSSIAN;
                break;

            default:
                throw new RuntimeException("unknown smoothing selected");
        }
        if (type > 0) {
            chosenFinalSmoothing = SmoothingDialogHelper.showDialogAndGet2D(type, chosenFinalSmoothing);
        }
    }

    public void onFinalSmoothingComboChange() {
        boolean firstIndexSelected = finalSmoothing.getSelectionModel().getSelectedIndex() == 0;
        finalSmoothingConfig.setDisable(firstIndexSelected);
        if (firstIndexSelected) {
            chosenFinalSmoothing = null;
        }
    }

    @Override
    public void setDeactivated(boolean deactivated) {
        p.setDisable(deactivated);
        q.setDisable(deactivated);
        orientationInterpolation.setDisable(deactivated);
        profileSmoothing.setDisable(deactivated);
        profileSmoothingIterations.setDisable(deactivated);
        greyValueInterpolation.setDisable(deactivated);
        finalSmoothing.setDisable(deactivated);
        orientationInterpolationConfig.setDisable(deactivated);
        profileSmoothingConfig.setDisable(deactivated);
        greyValueInterpolationConfig.setDisable(deactivated);
        finalSmoothingConfig.setDisable(deactivated);

        if (!deactivated) {
            onOrientationInterpolationComboChange();
            onGreyValueInterpolationComboChange();
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
        profileSmoothingIterations.setText("3");

        orientationInterpolation.setItems(Constants.INTERPOLATIONS);
        orientationInterpolation.getSelectionModel().select(1);

        profileSmoothing.setItems(Constants.SMOOTHINGS_1D);
        profileSmoothing.getSelectionModel().select(1);

        greyValueInterpolation.setItems(Constants.INTERPOLATIONS);
        greyValueInterpolation.getSelectionModel().select(1);

        finalSmoothing.setItems(Constants.SMOOTHINGS_2D);
        finalSmoothing.getSelectionModel().select(1);

        onOrientationInterpolationComboChange();
        onProfileSmoothingComboChange();
        onGreyValueInterpolationComboChange();
        onFinalSmoothingComboChange();
    }

    @Override
    public void initializeValidation(Validation validation) {
        validation.positiveInteger(p, "p in the curved region ridge frequency");
        validation.positiveInteger(q, "q in the curved region ridge frequency");
        validation.positiveInteger(profileSmoothingIterations, "the number of profile smoothing iterations in the curved region ridge frequency");
    }
}
