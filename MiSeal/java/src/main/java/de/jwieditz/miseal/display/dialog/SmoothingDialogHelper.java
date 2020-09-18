package de.jwieditz.miseal.display.dialog;

import java.io.IOException;

import de.jwieditz.miseal.smoothing.Smoothing1D;
import de.jwieditz.miseal.smoothing.Smoothing2D;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class SmoothingDialogHelper {

    public static final int TYPE_AVERAGE = 1;
    public static final int TYPE_GAUSSIAN = 2;

    private SmoothingDialogHelper() {}

    public static Smoothing2D showDialogAndGet2D(int type, Smoothing2D fallback) throws IOException {
        Dialog<Smoothing2D> dialog = new Dialog<>();
        FXMLLoader loader;
        String title;
        switch (type) {
            case TYPE_AVERAGE:
                title = "Parameters for Average Smoothing";
                loader = new FXMLLoader(SmoothingDialogHelper.class.getResource("/fxml/smoothing_average.fxml"));
                break;

            case TYPE_GAUSSIAN:
                title = "Parameters for Gaussian Smoothing";
                loader = new FXMLLoader(SmoothingDialogHelper.class.getResource("/fxml/smoothing_gaussian_2d.fxml"));
                break;

            default:
                throw new RuntimeException("unknown smoothing type " + type);
        }

        DialogPane dialogPane = loader.load();
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setTitle(title);
        dialog.setDialogPane(dialogPane);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return loader.<Smoothing2DDialogController>getController().get();
            }

            return fallback;
        });
        return dialog.showAndWait().orElse(fallback);
    }

    public static Smoothing1D showDialogAndGet1D(int type, Smoothing1D fallback) throws IOException {
        Dialog<Smoothing1D> dialog = new Dialog<>();
        FXMLLoader loader;
        String title;
        switch (type) {
            case TYPE_GAUSSIAN:
                title = "Parameters for Gaussian Smoothing";
                loader = new FXMLLoader(SmoothingDialogHelper.class.getResource("/fxml/smoothing_gaussian_1d.fxml"));
                break;

            default:
                throw new RuntimeException("unknown smoothing type " + type);
        }

        DialogPane dialogPane = loader.load();
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setTitle(title);
        dialog.setDialogPane(dialogPane);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return loader.<Smoothing1DDialogController>getController().get();
            }

            return fallback;
        });
        return dialog.showAndWait().orElse(fallback);
    }

}
