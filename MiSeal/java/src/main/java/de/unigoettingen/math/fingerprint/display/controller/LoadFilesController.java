package de.unigoettingen.math.fingerprint.display.controller;

import java.io.File;

import de.unigoettingen.math.fingerprint.display.MainGUI;
import de.unigoettingen.math.fingerprint.display.Validation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class LoadFilesController implements IController {

    private static final FileChooser.ExtensionFilter FILTER_PNG = new FileChooser.ExtensionFilter("PNG", "*.png");
    private static final FileChooser.ExtensionFilter FILTER_CSV = new FileChooser.ExtensionFilter("CSV", "*.csv");
    private static final FileChooser.ExtensionFilter FILTER_TXT = new FileChooser.ExtensionFilter("TXT", "*.txt");

    public TextField imageFileField;
    public TextField roiFileField;
    public TextField minutiaeFileField;
    public TextField orientationFileField;
    @FXML
    private Label patchDim1Name;
    @FXML
    private Label patchDim2Name;
    @FXML
    private TextField patchDim1Field;
    @FXML
    private TextField patchDim2Field;

    /* default */ File imageFile;
    /* default */ File roiFile;
    /* default */ File minutiaeFile;
    /* default */ File orientationFile;

    private boolean widthHeight;
    private int patchWidth = 16;
    private int patchHeight = 16;
    private int patchNumHorizontal = 10;
    private int patchNumVertical = 10;

    public void initialize() {
        setDefaults();
    }

    public void onClickLoadImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open a fingerprint image");
        chooser.getExtensionFilters().add(FILTER_PNG);
        File file = chooser.showOpenDialog(MainGUI.stage);
        if (file == null) {
            return;
        }
        imageFileField.setText(file.getAbsolutePath());
        imageFile = file;
    }

    public void onClickLoadRoi() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open a region of interest (ROI) file");
        chooser.getExtensionFilters().addAll(FILTER_CSV, FILTER_TXT);
        File file = chooser.showOpenDialog(MainGUI.stage);
        if (file == null) {
            return;
        }
        roiFileField.setText(file.getAbsolutePath());
        roiFile = file;
    }

    public void onClickLoadMinutiae() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open a minutiae file");
        chooser.getExtensionFilters().add(FILTER_TXT);
        File file = chooser.showOpenDialog(MainGUI.stage);
        if (file == null) {
            return;
        }
        minutiaeFileField.setText(file.getAbsolutePath());
        minutiaeFile = file;
    }

    public void onClickLoadOrientation() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open an orientation file");
        chooser.getExtensionFilters().add(FILTER_TXT);
        File file = chooser.showOpenDialog(MainGUI.stage);
        if (file == null) {
            return;
        }
        orientationFileField.setText(file.getAbsolutePath());
        orientationFile = file;
    }

    public void onSwitchPatchDim() {
        widthHeight = !widthHeight;
        if (widthHeight) {
            patchDim1Field.setText(String.valueOf(patchWidth));
            patchDim2Field.setText(String.valueOf(patchHeight));
            patchDim1Name.setText("Width:");
            patchDim2Name.setText("Height:");
        } else {
            patchDim1Field.setText(String.valueOf(patchNumHorizontal));
            patchDim2Field.setText(String.valueOf(patchNumVertical));
            patchDim1Name.setText("#Horizontal:");
            patchDim2Name.setText("#Vertical:");
        }
    }

    public boolean isWidthHeightSelected() {
        return widthHeight;
    }

    public int getPatchWidth() {
        return patchWidth;
    }

    public int getPatchHeight() {
        return patchHeight;
    }

    public int getPatchNumHorizontal() {
        return patchNumHorizontal;
    }

    public int getPatchNumVertical() {
        return patchNumVertical;
    }

    @Override
    public void reset() {
        setDefaults();

        imageFile = null;
        roiFile = null;
        minutiaeFile = null;
        orientationFile = null;
    }

    @Override
    public void setDefaults() {
        widthHeight = false;
        imageFileField.setText(null);
        roiFileField.setText(null);
        minutiaeFileField.setText(null);
        orientationFileField.setText("[optional]");
        patchDim1Field.setText("10");
        patchDim2Field.setText("10");
        patchDim1Name.setText("#Horizontal:");
        patchDim2Name.setText("#Vertical:");

        patchDim1Field.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (widthHeight) {
                    patchWidth = Integer.parseInt(newValue);
                } else {
                    patchNumHorizontal = Integer.parseInt(newValue);
                }
            } catch (NumberFormatException e) {
                patchDim1Field.textProperty().setValue(oldValue);
            }
        });

        patchDim2Field.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (widthHeight) {
                    patchHeight = Integer.parseInt(newValue);
                } else {
                    patchNumVertical = Integer.parseInt(newValue);
                }
            } catch (NumberFormatException e) {
                patchDim2Field.textProperty().setValue(oldValue);
            }
        });
    }

    @Override
    public void initializeValidation(Validation validation) {
        validation.notEmpty(imageFileField, "the image file");
        validation.positiveInteger(patchDim1Field, "the patch width/number of horizontal patches");
        validation.positiveInteger(patchDim2Field, "the patch height/number of vertical patches");
    }
}
