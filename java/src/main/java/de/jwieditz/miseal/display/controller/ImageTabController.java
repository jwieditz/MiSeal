package de.jwieditz.miseal.display.controller;

import org.controlsfx.dialog.ExceptionDialog;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import de.jwieditz.miseal.divergence.DivergenceEstimator;
import de.jwieditz.miseal.intensity.IntensityEstimator;
import de.jwieditz.miseal.interpolation.Interpolation;
import de.jwieditz.miseal.orientation.HWJOrientationEstimator;
import de.jwieditz.miseal.orientation.OrientationEstimator;
import de.jwieditz.miseal.ridgefrequency.FrequencyEstimator;
import de.jwieditz.miseal.singularPoints.SingularPoints;
import de.jwieditz.miseal.smoothing.Smoothing1D;
import de.jwieditz.miseal.smoothing.Smoothing2D;
import de.jwieditz.miseal.utilities.Gradient;
import de.jwieditz.miseal.utilities.unwrap.UnwrapType;
import de.jwieditz.miseal.FingerprintImage;
import de.jwieditz.miseal.LineDivergenceCalculator;
import de.jwieditz.miseal.Minutia;
import de.jwieditz.miseal.Normalizer;
import de.jwieditz.miseal.PatchedData;
import de.jwieditz.miseal.display.DrawableHolder;
import de.jwieditz.miseal.display.MainGUI;
import de.jwieditz.miseal.display.Validation;
import de.jwieditz.miseal.display.color.ColorPalette;
import de.jwieditz.miseal.display.masks.IntegralLinesMask;
import de.jwieditz.miseal.display.masks.Mask;
import de.jwieditz.miseal.display.masks.MinutiaeMask;
import de.jwieditz.miseal.display.masks.OrientationMask;
import de.jwieditz.miseal.display.masks.PatchesMask;
import de.jwieditz.miseal.display.masks.RoiMask;
import de.jwieditz.miseal.display.masks.SingularitiesMask;
import de.jwieditz.miseal.divergence.CurvedRegionDivergenceEstimator;
import de.jwieditz.miseal.divergence.DefaultDivergenceEstimator;
import de.jwieditz.miseal.intensity.DefaultIntensityEstimator;
import de.jwieditz.miseal.io.ImageToMatrixReader;
import de.jwieditz.miseal.io.MatrixWriter;
import de.jwieditz.miseal.io.MinutiaeReader;
import de.jwieditz.miseal.io.OrientationReader;
import de.jwieditz.miseal.io.ROIReader;
import de.jwieditz.miseal.orientation.DefaultOrientationEstimator;
import de.jwieditz.miseal.ridgefrequency.CurvedRegionFrequencyEstimator;
import de.jwieditz.miseal.ridgefrequency.DefaultFrequencyEstimator;
import de.jwieditz.miseal.singularPoints.DefaultSingularPoints;
import de.jwieditz.miseal.utilities.MaximumPadding;
import de.jwieditz.miseal.utilities.Utils;
import de.jwieditz.miseal.utilities.unwrap.Unwrapper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Transform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class ImageTabController implements IController {

    private static final int N_TICKS = 10;

    public LoadFilesController loadFilesController;
    public NormalizationController normalizationController;
    public LineDivergenceOptionsController lineDivergenceController;

    public Accordion accordion;
    public TitledPane loadFilesPane;
    public ToggleButton toggleMinutiaeButton;
    public ToggleButton toggleRoiButton;
    public ToggleButton toggleOrientationButton;
    public ToggleButton toggleIntegralLinesButton;
    public ToggleButton togglePatchesButton;
    public ToggleButton toggleSingularitiesButton;
    public HBox imagesGrid;
    public Pane imageContainer;
    public Pane originalImageContainer;
    public Pane orientationOptions;
    public Pane ridgeFrequencyOptions;
    public Pane divergenceOptions;
    public ComboBox<String> comboOrientation;
    public ComboBox<String> unwrapType;
    public ComboBox<String> comboRidgeFrequency;
    public ComboBox<String> comboDivergence;
    public ComboBox<String> comboColour;
    public ToggleButton buttonNormalized;
    public ToggleButton buttonOrientation;
    public ToggleButton buttonRidgeFrequency;
    public ToggleButton buttonDivergence;
    public ToggleButton buttonLineDivergence;
    public ToggleButton buttonIntensity;
    public ToggleButton buttonNecessaryMinutiae;
    public Pane scale;
    public CheckBox useLog10;
    public Button buttonClearAllImages;
    public CheckBox skipNormalization;
    public CheckBox skipRidgeFrequency;
    public CheckBox skipDivergence;
    public CheckBox skipLineDivergence;
    public CheckBox skipIntensity;
    public Label xPosText;
    public Label yPosText;
    public Label valueText;
    public Button buttonAnalyze;
    public Slider imageScale;

    private FingerprintImage fingerprintImage;

    private DrawableHolder originalImage;
    private DrawableHolder normalizedImage;
    private DrawableHolder orientationImage;
    private DrawableHolder ridgeFrequencyImage;
    private DrawableHolder divergenceImage;
    private DrawableHolder lineDivergenceImage;
    private DrawableHolder intensityImage;
    private DrawableHolder necessaryMinutiaeImage;

    private ToggleGroup imageToggleButtons;

    private Mask roiMask = new RoiMask();
    private Mask minutiaeMask = new MinutiaeMask();
    private Mask orientationMask = new OrientationMask();
    private Mask integralLinesMask = new IntegralLinesMask();
    private Mask patchesMask = new PatchesMask();
    private Mask singularitiesMask = new SingularitiesMask();

    private ImageView leftRootImage;
    private ImageView rightRootImage;

    private DrawableHolder currentlyShown;
    private ColorPalette currentColor = ColorPalette.values()[0];

    private OrientationController orientationController;
    private RidgeFrequencyController ridgeFrequencyController;
    private DivergenceController divergenceController;

    private Tab tab;

    private boolean hasRoi;
    private boolean hasMinutiae;
    private boolean hasOrientation;
    private int selectedButton;

    private final Validation validation = new Validation();

    public void initialize() throws IOException {
        setDefaults();

        initializeValidation(validation);

        imageScale.valueProperty().addListener((obs, oldVal, newVal) -> {
            int newValInt = newVal.intValue();

            originalImageContainer.setVisible(newValInt > 0);
            imageContainer.setVisible(newValInt < 100);

            imageScale.setValue(newVal.intValue());
        });

        originalImageContainer.prefWidthProperty().bind(imageScale.valueProperty().divide(100).multiply(imagesGrid.prefWidthProperty()));
        imageContainer.prefWidthProperty().bind(imageScale.valueProperty().subtract(100).negate().divide(100).multiply(imagesGrid.prefWidthProperty()));
    }

    public void setTab(Tab tab) {
        this.tab = tab;
        imagesGrid.prefWidthProperty().bind(tab.getTabPane().widthProperty().subtract(512));
    }

    public void onToggleMinutiae() {
        drawWithOverlays(currentlyShown);
        if (toggleMinutiaeButton.isSelected()) {
            toggleMinutiaeButton.setText("Hide minutiae");
        } else {
            toggleMinutiaeButton.setText("Show minutiae");
        }
    }

    public void onToggleRoi() {
        drawWithOverlays(currentlyShown);
        if (toggleRoiButton.isSelected()) {
            toggleRoiButton.setText("Show all");
        } else {
            toggleRoiButton.setText("Show ROI");
        }
    }

    public void onToggleOrientation() {
        drawWithOverlays(currentlyShown);
        if (toggleOrientationButton.isSelected()) {
            toggleOrientationButton.setText("Hide orientation");
        } else {
            toggleOrientationButton.setText("Show orientation");
        }
    }

    public void onToggleIntegralLines() {
        drawWithOverlays(currentlyShown);
        if (toggleIntegralLinesButton.isSelected()) {
            toggleIntegralLinesButton.setText("Hide integral lines");
        } else {
            toggleIntegralLinesButton.setText("Show integral lines");
        }
    }

    public void onTogglePatches() {
        drawWithOverlays(currentlyShown);
        if (togglePatchesButton.isSelected()) {
            togglePatchesButton.setText("Hide patches");
        } else {
            togglePatchesButton.setText("Show patches");
        }
    }

    public void onToggleSingularities() {
        drawWithOverlays(currentlyShown);
        if (toggleSingularitiesButton.isSelected()) {
            toggleSingularitiesButton.setText("Hide singularities");
        } else {
            toggleSingularitiesButton.setText("Show singularities");
        }
    }

    public void onClickShowNormalizedImage() {
        useLog10.setSelected(false);
        useLog10.setDisable(true);
        selectedButton = 1;
        drawNormalizedImage();
    }

    public void onClickShowOrientationImage() {
        useLog10.setSelected(false);
        useLog10.setDisable(true);
        selectedButton = 2;
        drawOrientationImage();
    }

    public void onClickShowRidgeFrequencyImage() {
        useLog10.setDisable(false);
        selectedButton = 3;
        drawRidgeFrequencyImage();
    }

    public void onClickShowDivergenceImage() {
        useLog10.setDisable(false);
        selectedButton = 4;
        drawDivergenceImage();
    }

    public void onClickShowLineDivergenceImage() {
        useLog10.setDisable(false);
        selectedButton = 5;
        drawLineDivergenceImage();
    }

    public void onClickShowIntensityImage() {
        useLog10.setDisable(false);
        selectedButton = 6;
        drawIntensityImage();
    }

    public void onClickShowNecessaryMinutiaeImage() {
        useLog10.setSelected(false);
        useLog10.setDisable(true);
        selectedButton = 7;
        drawNecessaryMinutiaeImage();
    }

    public void comboOrientationChange() throws IOException {
        FXMLLoader loader;
        switch (comboOrientation.getSelectionModel().getSelectedIndex()) {
            case 0:
                loader = new FXMLLoader(getClass().getResource("/fxml/orientation_default_options.fxml"));
                break;

            case 1:
                loader = new FXMLLoader(getClass().getResource("/fxml/orientation_hwj_options.fxml"));
                break;

            default:
                System.err.println("Wrong index selected: " + comboOrientation.getSelectionModel().getSelectedIndex());
                return;
        }
        orientationOptions.getChildren().clear();
        orientationOptions.getChildren().add(loader.load());
        orientationController = loader.getController();

        orientationController.initializeValidation(validation);
    }

    public void onRidgeFrequencyComboChange() throws IOException {
        FXMLLoader loader;
        switch (comboRidgeFrequency.getSelectionModel().getSelectedIndex()) {
            case 0:
                loader = new FXMLLoader(getClass().getResource("/fxml/ridge_frequency_default_options.fxml"));
                break;

            case 1:
                loader = new FXMLLoader(getClass().getResource("/fxml/ridge_frequency_curved_region_options.fxml"));
                break;

            default:
                System.err.println("Wrong index selected: " + comboRidgeFrequency.getSelectionModel().getSelectedIndex());
                return;
        }
        ridgeFrequencyOptions.getChildren().clear();
        ridgeFrequencyOptions.getChildren().add(loader.load());
        ridgeFrequencyController = loader.getController();

        ridgeFrequencyController.initializeValidation(validation);
    }

    public void onDivergenceComboChange() throws IOException {
        FXMLLoader loader;
        switch (comboDivergence.getSelectionModel().getSelectedIndex()) {
            case 0:
                loader = new FXMLLoader(getClass().getResource("/fxml/divergence_default_options.fxml"));
                break;

            case 1:
                loader = new FXMLLoader(getClass().getResource("/fxml/divergence_curved_region_options.fxml"));
                break;

            default:
                System.err.println("Wrong index selected: " + comboDivergence.getSelectionModel().getSelectedIndex());
                return;
        }
        divergenceOptions.getChildren().clear();
        divergenceOptions.getChildren().add(loader.load());
        divergenceController = loader.getController();

        divergenceController.initializeValidation(validation);
    }

    public void onComboColourChange() {
        currentColor = ColorPalette.values()[comboColour.getSelectionModel().getSelectedIndex()];
        drawWithOverlays(currentlyShown);
    }

    public void onClickAnalyze() {
        if (validation.isInvalid()) {
            String errors = validation.getAllErrors();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setHeaderText(null);
            alert.setContentText(errors);
            alert.showAndWait();
            return;
        }

        originalImageContainer.getChildren().clear();
        imageContainer.getChildren().clear();

        ProgressIndicator leftProgressIndicator = new ProgressIndicator();
        leftProgressIndicator.setLayoutX(imageContainer.getWidth() / 2);
        leftProgressIndicator.setLayoutY(imageContainer.getHeight() / 2);
        imageContainer.getChildren().add(leftProgressIndicator);

        ProgressIndicator rightProgressIndicator = new ProgressIndicator();
        rightProgressIndicator.setLayoutX(originalImageContainer.getWidth() / 2);
        rightProgressIndicator.setLayoutY(originalImageContainer.getHeight() / 2);
        originalImageContainer.getChildren().add(rightProgressIndicator);

        originalImage = null;
        normalizedImage = null;
        orientationImage = null;
        ridgeFrequencyImage = null;
        divergenceImage = null;
        lineDivergenceImage = null;
        intensityImage = null;
        necessaryMinutiaeImage = null;

        currentlyShown = null;

        scale.getChildren().clear();

        fingerprintImage = null;

        disableControls();

        buttonAnalyze.setDisable(true);

        Thread calculationThread = new Thread(this::calculate, "calculate");
        calculationThread.setUncaughtExceptionHandler((thread, throwable) -> Platform.runLater(() -> {
            buttonAnalyze.setDisable(false);
            originalImageContainer.getChildren().clear();
            imageContainer.getChildren().clear();
            new ExceptionDialog(throwable).showAndWait();
        }));
        calculationThread.start();
    }

    public void onSelectUseLog10() {
        drawWithOverlays(currentlyShown);
    }

    public void onClickClearAllImages() throws IOException {
        buttonClearAllImages.setDisable(true);

        reset();
        loadFilesController.reset();
        normalizationController.reset();
    }

    @Override
    public void reset() throws IOException {
        imageContainer.getChildren().clear();
        originalImageContainer.getChildren().clear();

        originalImage = null;
        normalizedImage = null;
        orientationImage = null;
        ridgeFrequencyImage = null;
        divergenceImage = null;
        lineDivergenceImage = null;
        intensityImage = null;
        necessaryMinutiaeImage = null;

        currentlyShown = null;

        scale.getChildren().clear();

        setDefaults();
    }

    @Override
    public void setDefaults() throws IOException {
        FXMLLoader orientationLoader = new FXMLLoader(getClass().getResource("/fxml/orientation_default_options.fxml"));
        orientationOptions.getChildren().clear();
        orientationOptions.getChildren().add(orientationLoader.load());
        orientationController = orientationLoader.getController();

        unwrapType.setItems(FXCollections.observableArrayList("none", "lines", "spiral", "diamond"));
        unwrapType.getSelectionModel().select(3);

        FXMLLoader ridgeFrequencyLoader = new FXMLLoader(getClass().getResource("/fxml/ridge_frequency_default_options.fxml"));
        ridgeFrequencyOptions.getChildren().clear();
        ridgeFrequencyOptions.getChildren().add(ridgeFrequencyLoader.load());
        ridgeFrequencyController = ridgeFrequencyLoader.getController();

        FXMLLoader divergenceLoader = new FXMLLoader(getClass().getResource("/fxml/divergence_default_options.fxml"));
        divergenceOptions.getChildren().clear();
        divergenceOptions.getChildren().add(divergenceLoader.load());
        divergenceController = divergenceLoader.getController();

        comboOrientation.getSelectionModel().selectFirst();
        comboRidgeFrequency.getSelectionModel().selectFirst();
        comboDivergence.getSelectionModel().selectFirst();

        accordion.setExpandedPane(loadFilesPane);

        ObservableList<String> colorPaletteNames = Arrays.stream(ColorPalette.values())
                .map(ColorPalette::getName)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        comboColour.setItems(colorPaletteNames);
        comboColour.getSelectionModel().select(0);

        disableControls();
    }

    @Override
    public void initializeValidation(Validation validation) {
        loadFilesController.initializeValidation(validation);
        normalizationController.initializeValidation(validation);
        orientationController.initializeValidation(validation);
        ridgeFrequencyController.initializeValidation(validation);
        divergenceController.initializeValidation(validation);
        lineDivergenceController.initializeValidation(validation);
    }

    public void saveCurrent() {
        if (fingerprintImage == null) {
            return;
        }
        FileChooser saveDialog = new FileChooser();
        saveDialog.setTitle("Save current image");
        saveDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files", "*.png", "*.PNG"));
        String imageType;
        switch (selectedButton) {
            case 0:
                imageType = "original";
                break;

            case 1:
                imageType = "normalized";
                break;

            case 2:
                imageType = "orientation";
                break;

            case 3:
                imageType = "ridge_frequency";
                break;

            case 4:
                imageType = "divergence";
                break;

            case 5:
                imageType = "line_divergence";
                break;

            case 6:
                imageType = "intensity";
                break;

            case 7:
                imageType = "necessary_minutiae";
                break;

            default:
                return;
        }
        saveDialog.setInitialFileName("image-" + extractName(fingerprintImage) + "-" + imageType + ".png");
        File file = saveDialog.showSaveDialog(MainGUI.stage);
        if (file != null) {
            try {
                Group group = (Group) imageContainer.getChildren().get(0);
                double scaleX = fingerprintImage.getWidth() / rightRootImage.getFitWidth();
                double scaleY = fingerprintImage.getHeight() / rightRootImage.getFitHeight();
                SnapshotParameters sp = new SnapshotParameters();
                sp.setTransform(Transform.scale(scaleX, scaleY));
                WritableImage size = new WritableImage(fingerprintImage.getWidth(), fingerprintImage.getHeight());
                WritableImage image = group.snapshot(sp, size);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void exportCurrent() {
        if (fingerprintImage == null) {
            return;
        }
        String imageType;
        double[][] data;
        switch (selectedButton) {
            case 0:
                imageType = "original";
                data = fingerprintImage.getImageDataMatrix();
                break;

            case 1:
                imageType = "normalized";
                data = fingerprintImage.getNormalizedImageMatrix();
                break;

            case 2:
                imageType = "orientation";
                data = fingerprintImage.getOrientationMatrix();
                break;

            case 3:
                imageType = "ridge_frequency";
                data = fingerprintImage.getRidgeFrequencyMatrix();
                break;

            case 4:
                imageType = "divergence";
                data = fingerprintImage.getDivergenceMatrix();
                break;

            case 5:
                imageType = "line_divergence";
                data = fingerprintImage.getLineDivergenceMatrix();
                break;

            case 6:
                imageType = "intensity";
                data = fingerprintImage.getIntensityMatrix();
                break;

            case 7:
                imageType = "necessary_minutiae";
                data = fingerprintImage.getNecessaryMinutiae();
                break;

            default:
                return;
        }
        FileChooser exportDialog = new FileChooser();
        exportDialog.setTitle("Export current image data");
        exportDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv", "*.CSV"));
        exportDialog.setInitialFileName("image-" + extractName(fingerprintImage) + "-" + imageType + ".csv");
        File file = exportDialog.showSaveDialog(MainGUI.stage);
        if (file != null && data != null) {
            try {
                MatrixWriter.writeMatrixToFile(data, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void exportAll() {
        if (fingerprintImage == null) {
            return;
        }
        DirectoryChooser exportAllDialog = new DirectoryChooser();
        exportAllDialog.setTitle("Export all image data");
        File directory = exportAllDialog.showDialog(MainGUI.stage);
        if (directory != null) {
            try {
                double[][] data = fingerprintImage.getImageDataMatrix();
                File file = new File(directory, "image-" + extractName(fingerprintImage) + "-original.csv");
                MatrixWriter.writeMatrixToFile(data, file);

                data = fingerprintImage.getNormalizedImageMatrix();
                file = new File(directory, "image-" + extractName(fingerprintImage) + "-normalized.csv");
                MatrixWriter.writeMatrixToFile(data, file);

                data = fingerprintImage.getOrientationMatrix();
                file = new File(directory, "image-" + extractName(fingerprintImage) + "-orientation.csv");
                MatrixWriter.writeMatrixToFile(data, file);

                data = fingerprintImage.getRidgeFrequencyMatrix();
                file = new File(directory, "image-" + extractName(fingerprintImage) + "-ridge_frequency.csv");
                MatrixWriter.writeMatrixToFile(data, file);

                data = fingerprintImage.getDivergenceMatrix();
                file = new File(directory, "image-" + extractName(fingerprintImage) + "-divergence.csv");
                MatrixWriter.writeMatrixToFile(data, file);

                data = fingerprintImage.getLineDivergenceMatrix();
                file = new File(directory, "image-" + extractName(fingerprintImage) + "-line_divergence.csv");
                MatrixWriter.writeMatrixToFile(data, file);

                data = fingerprintImage.getIntensityMatrix();
                file = new File(directory, "image-" + extractName(fingerprintImage) + "-intensity.csv");
                MatrixWriter.writeMatrixToFile(data, file);

                data = fingerprintImage.getNecessaryMinutiae();
                file = new File(directory, "image-" + extractName(fingerprintImage) + "-necessary_minutiae.csv");
                MatrixWriter.writeMatrixToFile(data, file);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onSelectSkipNormalization() {
        normalizationController.setDeactivated(skipNormalization.isSelected());
    }

    public void onSelectSkipRidgeFrequency() {
        boolean doSkipRidgeFrequency = skipRidgeFrequency.isSelected();
        boolean doSkipDivergence = skipDivergence.isSelected();
        boolean doSkipLineDivergence = skipLineDivergence.isSelected();

        ridgeFrequencyController.setDeactivated(doSkipRidgeFrequency);
        comboRidgeFrequency.setDisable(doSkipRidgeFrequency);

        lineDivergenceController.setDeactivated(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);
        skipLineDivergence.setSelected(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);
        skipLineDivergence.setDisable(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);

        skipIntensity.setSelected(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);
        skipIntensity.setDisable(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);
    }

    public void onSelectSkipDivergence() {
        boolean doSkipRidgeFrequency = skipRidgeFrequency.isSelected();
        boolean doSkipDivergence = skipDivergence.isSelected();
        boolean doSkipLineDivergence = skipLineDivergence.isSelected();

        divergenceController.setDeactivated(doSkipDivergence);
        comboDivergence.setDisable(doSkipDivergence);

        lineDivergenceController.setDeactivated(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);
        skipLineDivergence.setSelected(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);
        skipLineDivergence.setDisable(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);

        skipIntensity.setSelected(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);
        skipIntensity.setDisable(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);
    }

    public void onSelectSkipLineDivergence() {
        boolean doSkipRidgeFrequency = skipRidgeFrequency.isSelected();
        boolean doSkipDivergence = skipDivergence.isSelected();
        boolean doSkipLineDivergence = skipLineDivergence.isSelected();

        lineDivergenceController.setDeactivated(doSkipLineDivergence);

        skipIntensity.setSelected(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);
        skipIntensity.setDisable(doSkipRidgeFrequency || doSkipDivergence || doSkipLineDivergence);
    }

    public void onClickImageScale(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if (mouseEvent.getClickCount() == 2) {
                imageScale.setValue(50);
            }
        }
    }

    private void drawOriginalImage() {
        if (originalImage == null) {
            originalImage = createDrawableHolder(fingerprintImage.getImageDataMatrix());
        }

        Image leftImage = originalImage.getImage(currentColor);

        double imageWidth = leftImage.getWidth();
        double imageHeight = leftImage.getHeight();

        DoubleProperty imageWidthProperty = new SimpleDoubleProperty(imageWidth);
        DoubleProperty imageHeightProperty = new SimpleDoubleProperty(imageHeight);

        NumberBinding min = Bindings.min(
                originalImageContainer.widthProperty().divide(imageWidthProperty),
                originalImageContainer.heightProperty().divide(imageHeightProperty)
        );

        leftRootImage = new ImageView(leftImage);
        leftRootImage.setSmooth(false);

        leftRootImage.fitWidthProperty().bind(imageWidthProperty.multiply(min));
        leftRootImage.fitHeightProperty().bind(imageHeightProperty.multiply(min));

        leftRootImage.xProperty().bind(originalImageContainer.widthProperty().subtract(leftRootImage.fitWidthProperty()).divide(2));
        leftRootImage.yProperty().bind(originalImageContainer.heightProperty().subtract(leftRootImage.fitHeightProperty()).divide(2));

        originalImageContainer.getChildren().clear();
        originalImageContainer.getChildren().add(leftRootImage);
        originalImageContainer.addEventFilter(MouseEvent.MOUSE_MOVED, e -> onMouseMoved(e, true));
    }

    private void drawNormalizedImage() {
        if (normalizedImage == null || imageContainer.getHeight() != normalizedImage.getScaleHeight()) {
            normalizedImage = createDrawableHolder(fingerprintImage.getNormalizedImageMatrix());
        }

        drawWithOverlays(normalizedImage);
    }

    private void drawOrientationImage() {
        if (orientationImage == null || imageContainer.getHeight() != orientationImage.getScaleHeight()) {
            orientationImage = createDrawableHolder(fingerprintImage.getOrientationMatrix());
        }

        drawWithOverlays(orientationImage);
    }

    private void drawRidgeFrequencyImage() {
        if (ridgeFrequencyImage == null || imageContainer.getHeight() != ridgeFrequencyImage.getScaleHeight()) {
            ridgeFrequencyImage = createDrawableHolder(fingerprintImage.getRidgeFrequencyMatrix());
        }

        drawWithOverlays(ridgeFrequencyImage);
    }

    private void drawDivergenceImage() {
        if (divergenceImage == null || imageContainer.getHeight() != divergenceImage.getScaleHeight()) {
            divergenceImage = createDrawableHolder(fingerprintImage.getDivergenceMatrix());
        }

        drawWithOverlays(divergenceImage);
    }

    private void drawLineDivergenceImage() {
        if (lineDivergenceImage == null || imageContainer.getHeight() != lineDivergenceImage.getScaleHeight()) {
            lineDivergenceImage = createDrawableHolder(fingerprintImage.getLineDivergenceMatrix());
        }

        drawWithOverlays(lineDivergenceImage);
    }

    private void drawIntensityImage() {
        if (intensityImage == null || imageContainer.getHeight() != intensityImage.getScaleHeight()) {
            intensityImage = createDrawableHolder(fingerprintImage.getIntensityMatrix());
        }

        drawWithOverlays(intensityImage);
    }

    private void drawNecessaryMinutiaeImage() {
        if (necessaryMinutiaeImage == null || imageContainer.getHeight() != intensityImage.getScaleHeight()) {
            necessaryMinutiaeImage = createDrawableHolder(fingerprintImage.getNecessaryMinutiae());
        }

        drawWithOverlays(necessaryMinutiaeImage);
    }

    private DrawableHolder createDrawableHolder(double[][] data) {
        int width = data.length;
        int height = data[0].length;

        DoubleSummaryStatistics stats = Arrays.stream(data)
                .flatMapToDouble(Arrays::stream)
                .filter(Double::isFinite)
                .summaryStatistics();

        DoubleSummaryStatistics log10Stats = Arrays.stream(data)
                .flatMapToDouble(Arrays::stream)
                .filter(Double::isFinite)
                .map(Utils::log10WithCheck)
                .filter(d -> !Double.isNaN(d))
                .summaryStatistics();

        double min = stats.getMin();
        double max = stats.getMax();

        double log10Min = log10Stats.getMin();
        double log10Max = log10Stats.getMax();

        Function<Double, Double> normalize = x -> (x - min) / (max - min);
        Function<Double, Double> log10Normalize = x -> (x - log10Min) / (log10Max - log10Min);

        DrawableHolder holder = new DrawableHolder(imageContainer.getHeight());

        for (ColorPalette colorPalette : ColorPalette.values()) {
            WritableImage img = new WritableImage(width, height);
            WritableImage log10Img = new WritableImage(width, height);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double val = normalize.apply(data[x][y]);
                    img.getPixelWriter().setColor(x, y, colorPalette.interpolateColor(val));

                    val = log10Normalize.apply(Utils.log10WithCheck(data[x][y]));
                    log10Img.getPixelWriter().setColor(x, y, colorPalette.interpolateColor(val));
                }
            }

            double[] tickValues = new double[N_TICKS];
            double[] log10TickValues = new double[N_TICKS];

            tickValues[0] = max;
            log10TickValues[0] = log10Max;

            for (int i = 1; i < N_TICKS - 1; i++) {
                tickValues[i] = max - (max - min) * i / (double) N_TICKS;
                log10TickValues[i] = log10Max - (log10Max - log10Min) * i / (double) N_TICKS;
            }

            tickValues[N_TICKS - 1] = min;
            log10TickValues[N_TICKS - 1] = log10Min;

            Canvas scale = createScale(this.scale.getWidth(), 0.55 * MainGUI.stage.getMinHeight(), tickValues, colorPalette);
            Canvas log10Scale = createScale(this.scale.getWidth(), 0.55 * MainGUI.stage.getMinHeight(), log10TickValues, colorPalette);

            holder.add(colorPalette, img, log10Img, scale, log10Scale);
        }

        return holder;
    }

    private Canvas createScale(double width, double height, double[] tickValues, ColorPalette colorPalette) {
        Canvas canvas = new Canvas(width, height);
        int fontSize = 12;
        Font font = new Font(Font.getDefault().getName(), fontSize);
        int maxTextWidth = 48;

        int yStart = fontSize / 2;
        int yEnd = (int) (height - fontSize / 2);

        int barWidth = (int) (width - maxTextWidth - 3 - 2);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (int y = yStart; y < yEnd; y++) {
            double value = 1 - (double) y / height;
            Color color = colorPalette.interpolateColor(value);
            gc.setStroke(color);
            gc.strokeLine(0, y, barWidth, y);
        }
        gc.setStroke(Color.BLACK);
        gc.setFont(font);
        gc.strokeLine(barWidth + 2, yStart, barWidth + 2, yEnd - 1);

        int nTicks = tickValues.length;

        NumberFormat format = new DecimalFormat("#0.0##");

        for (int i = 0; i < nTicks; i++) {
            double y = (yEnd - yStart - 1) * i / (double) (nTicks - 1) + yStart;
            gc.strokeLine(barWidth + 3, y, barWidth + 5, y);
            gc.fillText(format.format(tickValues[i]), barWidth + 6, y + 4);
        }

        return canvas;
    }

    private Canvas createMask(Mask mask, ImageView sizeReference) {
        Canvas canvas = mask.draw(fingerprintImage, currentColor);

        canvas.setBlendMode(BlendMode.SRC_OVER);

        canvas.layoutXProperty().bind(sizeReference.xProperty());
        canvas.layoutYProperty().bind(sizeReference.yProperty());
        canvas.widthProperty().bind(sizeReference.fitWidthProperty());
        canvas.heightProperty().bind(sizeReference.fitHeightProperty());

        return canvas;
    }

    private void drawWithOverlays(DrawableHolder holder) {

        Image rightImage;
        if (useLog10.isSelected()) {
            rightImage = holder.getLog10Image(currentColor);
        } else {
            rightImage = holder.getImage(currentColor);
        }

        double imageWidth = rightImage.getWidth();
        double imageHeight = rightImage.getHeight();

        DoubleProperty imageWidthProperty = new SimpleDoubleProperty(imageWidth);
        DoubleProperty imageHeightProperty = new SimpleDoubleProperty(imageHeight);

        NumberBinding min = Bindings.min(
                imageContainer.widthProperty().divide(imageWidthProperty),
                imageContainer.heightProperty().divide(imageHeightProperty)
        );

        rightRootImage = new ImageView(rightImage);
        rightRootImage.setSmooth(false);

        rightRootImage.fitWidthProperty().bind(imageWidthProperty.multiply(min));
        rightRootImage.fitHeightProperty().bind(imageHeightProperty.multiply(min));

        rightRootImage.xProperty().bind(imageContainer.widthProperty().subtract(rightRootImage.fitWidthProperty()).divide(2));
        rightRootImage.yProperty().bind(imageContainer.heightProperty().subtract(rightRootImage.fitHeightProperty()).divide(2));

        Group group = new Group(rightRootImage);

        // TODO: reuse masks
        if (toggleRoiButton.isSelected()) {
            group = new Group(rightRootImage, createMask(roiMask, rightRootImage));
        }

        if (toggleMinutiaeButton.isSelected()) {
            group = new Group(group, createMask(minutiaeMask, rightRootImage));
        }

        if (toggleOrientationButton.isSelected()) {
            group = new Group(group, createMask(orientationMask, rightRootImage));
        }

        if (toggleIntegralLinesButton.isSelected()) {
            group = new Group(group, createMask(integralLinesMask, rightRootImage));
        }

        if (togglePatchesButton.isSelected()) {
            group = new Group(group, createMask(patchesMask, rightRootImage));
        }

        if (toggleSingularitiesButton.isSelected()) {
            group = new Group(group, createMask(singularitiesMask, rightRootImage));
        }

        imageContainer.getChildren().clear();
        imageContainer.getChildren().add(group);
        imageContainer.addEventFilter(MouseEvent.MOUSE_MOVED, e -> onMouseMoved(e, false));

        Canvas scale;
        if (useLog10.isSelected()) {
            scale = holder.getLog10Scale(currentColor);
        } else {
            scale = holder.getScale(currentColor);
        }
        this.scale.getChildren().clear();
        this.scale.getChildren().add(scale);

        currentlyShown = holder;
    }

    private void onMouseMoved(MouseEvent e, boolean originalImage) {
        ImageView reference = originalImage ? leftRootImage : rightRootImage;
        int x = (int) (fingerprintImage.getWidth() / reference.getFitWidth() * (e.getX() - reference.getX()));
        int y = (int) (fingerprintImage.getHeight() / reference.getFitHeight() * (e.getY() - reference.getY()));

        if (x < 0 || x >= fingerprintImage.getWidth() || y < 0 || y >= fingerprintImage.getHeight()) {
            return;
        }

        double value = Double.NaN;

        if (originalImage) {
            value = fingerprintImage.getImageDataMatrix()[x][y];
        } else {
            switch (selectedButton) {
                case 1:
                    value = fingerprintImage.getNormalizedImageMatrix()[x][y];
                    break;

                case 2:
                    value = fingerprintImage.getOrientationMatrix()[x][y];
                    break;

                case 3:
                    value = fingerprintImage.getRidgeFrequencyMatrix()[x][y];
                    break;

                case 4:
                    value = fingerprintImage.getDivergenceMatrix()[x][y];
                    break;

                case 5:
                    value = fingerprintImage.getLineDivergenceMatrix()[x][y];
                    break;

                case 6:
                    value = fingerprintImage.getIntensityMatrix()[x][y];
                    break;

                case 7:
                    value = fingerprintImage.getNecessaryMinutiae()[x][y];
                    break;
            }
        }

        xPosText.setText(String.format("%3d", x));
        yPosText.setText(String.format("%3d", y));
        valueText.setText(String.format("%.5f", value));

        // System.out.println("Patch " + fingerprintImage.getPatches().getPatchIndex(x, y));
    }

    private void calculate() {
        // load files
        try {
            fingerprintImage = ImageToMatrixReader.readImageToMatrix(loadFilesController.imageFile);
            fingerprintImage.setName(loadFilesController.imageFile.getName());

            Platform.runLater(this::setName);

            if (loadFilesController.roiFile != null) {
                boolean[][] roi = ROIReader.readROI(loadFilesController.roiFile);
                fingerprintImage.setROI(roi);
                hasRoi = true;
            } else {
                boolean[][] roi = new boolean[fingerprintImage.getWidth()][fingerprintImage.getHeight()];
                Arrays.stream(roi).forEach(a -> Arrays.fill(a, true));
                fingerprintImage.setROI(roi);
            }

            if (loadFilesController.minutiaeFile != null) {
                Minutia[] minutiae = MinutiaeReader.readMinutiae(loadFilesController.minutiaeFile);
                fingerprintImage.setMinutiae(minutiae);
                hasMinutiae = true;
            }

            if (loadFilesController.orientationFile != null) {
                double[][] orientation = OrientationReader.readOrientationFromImage(loadFilesController.orientationFile);
                fingerprintImage.setOrientationMatrix(orientation);
                hasOrientation = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // the maximum padding in each direction is required when creating the patches
        final MaximumPadding padding = new MaximumPadding();

        // normalization
        double normalizeMean = parseDoubleWithDefault(normalizationController.mean.getText(), 1);
        double normalizeVariance = parseDoubleWithDefault(normalizationController.variance.getText(), 1);

        Normalizer normalizer = new Normalizer();

        // orientation
        OrientationEstimator orientationEstimator;
        int selectedUnwrapType = unwrapType.getSelectionModel().getSelectedIndex();
        UnwrapType unwrapType = null;
        if (selectedUnwrapType > 0) {
            unwrapType = UnwrapType.values()[selectedUnwrapType - 1];
        }

        if (orientationController instanceof OrientationDefaultOptionsController) {
            String sigmaQString = ((OrientationDefaultOptionsController) orientationController).sigmaQ.getText();
            String iterationsString = ((OrientationDefaultOptionsController) orientationController).iterations.getText();

            double sigmaQ = parseDoubleWithDefault(sigmaQString, 25);
            int iterations = parseIntWithDefault(iterationsString, 1);

            orientationEstimator = new DefaultOrientationEstimator(sigmaQ);
            ((DefaultOrientationEstimator) orientationEstimator).setIterations(iterations);
        } else if (orientationController instanceof OrientationHwjOptionsController) {
            boolean useLowPassFilter = ((OrientationHwjOptionsController) orientationController).useLowPassFilter.isSelected();
            orientationEstimator = new HWJOrientationEstimator(useLowPassFilter);
        } else {
            throw new RuntimeException("Orientation controller is not supported");
        }

        // ridge frequency
        FrequencyEstimator frequencyEstimator;

        if (ridgeFrequencyController instanceof RidgeFrequencyDefaultOptionsController) {
            String windowWidthString = ((RidgeFrequencyDefaultOptionsController) ridgeFrequencyController).windowWidth.getText();
            String orientationWindowWidthString = ((RidgeFrequencyDefaultOptionsController) ridgeFrequencyController).orientationWindowWidth.getText();

            int windowWidth = parseIntWithDefault(windowWidthString, 17);
            int orientationWindowWidth = parseIntWithDefault(orientationWindowWidthString, 33);

            frequencyEstimator = new DefaultFrequencyEstimator(windowWidth, orientationWindowWidth);
            Interpolation frequencyInterpolation = ((RidgeFrequencyDefaultOptionsController) ridgeFrequencyController).getChosenInterpolation();
            ((DefaultFrequencyEstimator) frequencyEstimator).setFrequencyInterpolation(frequencyInterpolation);

            padding.update(((DefaultFrequencyEstimator) frequencyEstimator).getFrequencyInterpolation());
            padding.update(((DefaultFrequencyEstimator) frequencyEstimator).getFinalImageSmoothing());
        } else if (ridgeFrequencyController instanceof RidgeFrequencyCurvedRegionOptionsController) {
            String pString = ((RidgeFrequencyCurvedRegionOptionsController) ridgeFrequencyController).p.getText();
            String qString = ((RidgeFrequencyCurvedRegionOptionsController) ridgeFrequencyController).q.getText();
            String profileSmoothingIterationsString = ((RidgeFrequencyCurvedRegionOptionsController) ridgeFrequencyController).profileSmoothingIterations.getText();

            int p = parseIntWithDefault(pString, 17);
            int q = parseIntWithDefault(qString, 33);
            int profileSmoothingIterations = parseIntWithDefault(profileSmoothingIterationsString, 3);

            frequencyEstimator = new CurvedRegionFrequencyEstimator(p, q);

            Interpolation orientationInterpolation = ((RidgeFrequencyCurvedRegionOptionsController) ridgeFrequencyController).getChosenOrientationInterpolation();
            Interpolation greyValueInterpolation = ((RidgeFrequencyCurvedRegionOptionsController) ridgeFrequencyController).getChosenGreyValueInterpolation();
            Smoothing1D profileSmoothing = ((RidgeFrequencyCurvedRegionOptionsController) ridgeFrequencyController).getChosenProfileSmoothing();
            Smoothing2D finalSmoothing = ((RidgeFrequencyCurvedRegionOptionsController) ridgeFrequencyController).getChosenFinalSmoothing();

            ((CurvedRegionFrequencyEstimator) frequencyEstimator).setOrientationInterpolation(orientationInterpolation);
            ((CurvedRegionFrequencyEstimator) frequencyEstimator).setGreyValueInterpolation(greyValueInterpolation);
            ((CurvedRegionFrequencyEstimator) frequencyEstimator).setProfileSmoothing(profileSmoothing, profileSmoothingIterations);
            ((CurvedRegionFrequencyEstimator) frequencyEstimator).setFinalImageSmoothing(finalSmoothing);

            padding.update(((CurvedRegionFrequencyEstimator) frequencyEstimator).getOrientationInterpolation());
            padding.update(((CurvedRegionFrequencyEstimator) frequencyEstimator).getGreyValueInterpolation());
            padding.update(((CurvedRegionFrequencyEstimator) frequencyEstimator).getFinalImageSmoothing());
        } else {
            throw new RuntimeException("Ridge frequency controller is not supported");
        }

        // divergence
        DivergenceEstimator divergenceEstimator;

        if (divergenceController instanceof DivergenceDefaultOptionsController) {
            divergenceEstimator = new DefaultDivergenceEstimator();

            Smoothing2D smoothing = ((DivergenceDefaultOptionsController) divergenceController).getChosenSmoothing();

            ((DefaultDivergenceEstimator) divergenceEstimator).setSmoothing(smoothing);

            padding.update(((DefaultDivergenceEstimator) divergenceEstimator).getDivergenceSmoothing());
        } else if (divergenceController instanceof DivergenceCurvedRegionOptionsController) {
            String pString = ((DivergenceCurvedRegionOptionsController) divergenceController).p.getText();
            String qString = ((DivergenceCurvedRegionOptionsController) divergenceController).q.getText();
            boolean useRealDistance = ((DivergenceCurvedRegionOptionsController) divergenceController).useRealDistance.isSelected();
            boolean removeLineDivergence = ((DivergenceCurvedRegionOptionsController) divergenceController).removeLineDivergence.isSelected();

            int p = parseIntWithDefault(pString, 17);
            int q = parseIntWithDefault(qString, 33);

            divergenceEstimator = new CurvedRegionDivergenceEstimator(p, q);

            Interpolation orientationInterpolation = ((DivergenceCurvedRegionOptionsController) divergenceController).getChosenOrientationInterpolation();
            Smoothing2D smoothing = ((DivergenceCurvedRegionOptionsController) divergenceController).getChosenSmoothing();

            ((CurvedRegionDivergenceEstimator) divergenceEstimator).setUseRealDistance(useRealDistance);
            ((CurvedRegionDivergenceEstimator) divergenceEstimator).setRemoveLineDivergence(removeLineDivergence);
            ((CurvedRegionDivergenceEstimator) divergenceEstimator).setOrientationInterpolation(orientationInterpolation);
            ((CurvedRegionDivergenceEstimator) divergenceEstimator).setDivergenceSmoothing(smoothing);

            padding.update(((CurvedRegionDivergenceEstimator) divergenceEstimator).getOrientationInterpolation());
            padding.update(((CurvedRegionDivergenceEstimator) divergenceEstimator).getDivergenceSmoothing());
        } else {
            throw new RuntimeException("Divergence controller is not supported");
        }

        // line divergence
        LineDivergenceCalculator lineDivergenceCalculator = new LineDivergenceCalculator();

        Smoothing2D lineDivergenceSmoothing = lineDivergenceController.getChosenSmoothing();

        padding.update(lineDivergenceCalculator.getLineDivergenceSmoothing());

        lineDivergenceCalculator.setLineDivergenceSmoothing(lineDivergenceSmoothing);

        // intensity
        IntensityEstimator intensityEstimator = new DefaultIntensityEstimator();

        // compute normalised image
        if (skipNormalization.isSelected()) {
            fingerprintImage.setNormalizedImageMatrix(fingerprintImage.getImageDataMatrix());
        } else {
            normalizer.normalize(fingerprintImage, normalizeMean, normalizeVariance);
        }

        // compute orientation field if no orientation field was passed as input
        if (!hasOrientation) {
            orientationEstimator.calculateOrientation(fingerprintImage);
        } else {
            Gradient gradient = Gradient.gradientFromOrientation(fingerprintImage.getWidth(), fingerprintImage.getHeight(), fingerprintImage.getOrientationMatrix(), fingerprintImage.getROI());
            gradient.smoothAngles(25);
            gradient.smoothAngles(25);
            fingerprintImage.setOrientationMatrix(gradient.getOrientation());
        }

        // unwrap the orientation
        SingularPoints singularPoints = new DefaultSingularPoints();
        singularPoints.computeSingularPoints(fingerprintImage);

        if (loadFilesController.isWidthHeightSelected()) {
            int patchWidth = loadFilesController.getPatchWidth();
            int patchHeight = loadFilesController.getPatchHeight();

            fingerprintImage.createPatchesForSize(patchWidth, patchHeight, padding.getPadX(), padding.getPadY());
        } else {
            int patchNumHorizontal = loadFilesController.getPatchNumHorizontal();
            int patchNumVertical = loadFilesController.getPatchNumVertical();

            fingerprintImage.createPatches(patchNumHorizontal, patchNumVertical, padding.getPadX(), padding.getPadY());
        }

        Unwrapper unwrapper = new Unwrapper();

        for (PatchedData.Patch patch : fingerprintImage.getPatches()) {
            patch.initOrientationPadding();

            unwrapper.unwrap(patch, unwrapType);
        }

        // compute ridge frequency
        if (!skipRidgeFrequency.isSelected()) {
            frequencyEstimator.estimateFrequency(fingerprintImage);
        }

        // compute divergence
        if (!skipDivergence.isSelected()) {
            divergenceEstimator.calculateDivergence(fingerprintImage);
        }

        if (!skipLineDivergence.isSelected()) {
            lineDivergenceCalculator.calculateLineDivergence(fingerprintImage);
        }

        if (!skipIntensity.isSelected() && !skipRidgeFrequency.isSelected() && !skipDivergence.isSelected() && !skipLineDivergence.isSelected()) {
            intensityEstimator.estimateIntensity(fingerprintImage);
            intensityEstimator.estimateNecessaryMinutiae(fingerprintImage);
        }

        Platform.runLater(this::drawAllImages);
    }

    private void setName() {
        String name = extractName(fingerprintImage);
        tab.setText(name);
    }

    private void drawAllImages() {
        originalImageContainer.getChildren().clear();
        imageContainer.getChildren().clear();

        // Add listener to observe changes in colour scale.
        comboColour.setDisable(false);

        // Draw original image (on the left) and the normalised image (on the right).
        drawOriginalImage();
        drawNormalizedImage();

        // Enable all buttons.
        buttonNormalized.setDisable(fingerprintImage.getNormalizedImageMatrix() == null || skipNormalization.isSelected());
        buttonOrientation.setDisable(fingerprintImage.getOrientationMatrix() == null);
        buttonRidgeFrequency.setDisable(fingerprintImage.getRidgeFrequencyMatrix() == null);
        buttonDivergence.setDisable(fingerprintImage.getDivergenceMatrix() == null);
        buttonLineDivergence.setDisable(fingerprintImage.getLineDivergenceMatrix() == null);
        buttonIntensity.setDisable(fingerprintImage.getIntensityMatrix() == null);
        buttonNecessaryMinutiae.setDisable(fingerprintImage.getNecessaryMinutiae() == null);

        selectedButton = 1;

        buttonClearAllImages.setDisable(false);

        buttonAnalyze.setDisable(false);

        imageScale.setDisable(false);

        if (hasRoi) {
            toggleRoiButton.setDisable(false);
        }
        if (hasMinutiae) {
            toggleMinutiaeButton.setDisable(false);
        }
        if (fingerprintImage.getOrientationMatrix() != null) {
            toggleOrientationButton.setDisable(false);
            toggleIntegralLinesButton.setDisable(false);
            togglePatchesButton.setDisable(false);
            toggleSingularitiesButton.setDisable(false);
        }
    }

    private void disableControls() {

        // Disable image buttons
        buttonNormalized.setDisable(true);
        buttonOrientation.setDisable(true);
        buttonRidgeFrequency.setDisable(true);
        buttonDivergence.setDisable(true);
        buttonLineDivergence.setDisable(true);
        buttonIntensity.setDisable(true);
        buttonNecessaryMinutiae.setDisable(true);

        // Assign image buttons to toggle group
        imageToggleButtons = new ToggleGroup();
        buttonNormalized.setToggleGroup(imageToggleButtons);
        buttonOrientation.setToggleGroup(imageToggleButtons);
        buttonRidgeFrequency.setToggleGroup(imageToggleButtons);
        buttonDivergence.setToggleGroup(imageToggleButtons);
        buttonLineDivergence.setToggleGroup(imageToggleButtons);
        buttonIntensity.setToggleGroup(imageToggleButtons);
        buttonNecessaryMinutiae.setToggleGroup(imageToggleButtons);
        imageToggleButtons.selectToggle(buttonNormalized);
        imageToggleButtons.selectedToggleProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == null) {
                oldValue.setSelected(true);
            }
        }));

        // Disable masks buttons
        toggleMinutiaeButton.setDisable(true);
        toggleRoiButton.setDisable(true);
        toggleOrientationButton.setDisable(true);
        toggleIntegralLinesButton.setDisable(true);
        togglePatchesButton.setDisable(true);
        toggleSingularitiesButton.setDisable(true);

        buttonClearAllImages.setDisable(true);

        // Disable scale buttons
        useLog10.setSelected(false);
        useLog10.setDisable(true);
        comboColour.setDisable(true);

        // disable image scaling slider
        imageScale.setDisable(true);

        // reset selection state
        selectedButton = -1;
    }

    private double parseDoubleWithDefault(String val, double def) {
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private int parseIntWithDefault(String val, int def) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private String extractName(FingerprintImage image) {
        String name = image.getName();
        name = name.substring(name.lastIndexOf("/") + 1);
        name = name.substring(0, name.lastIndexOf(".png"));
        return name;
    }
}
