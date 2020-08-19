package de.unigoettingen.math.fingerprint.display.dialog;

import java.io.IOException;

import de.unigoettingen.math.fingerprint.interpolation.Interpolation;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class InterpolationDialogHelper {

    public static final int TYPE_GAUSSIAN = 0;

    private InterpolationDialogHelper() {}

    public static Interpolation showDialogAndGet(int type, Interpolation fallback) throws IOException {
        Dialog<Interpolation> dialog = new Dialog<>();
        FXMLLoader loader;
        String title;
        switch (type) {
            case TYPE_GAUSSIAN:
                loader = new FXMLLoader(InterpolationDialogHelper.class.getResource("/fxml/interpolation_gaussian.fxml"));
                title = "Parameters for Gaussian Interpolation";
                break;

            default:
                throw new RuntimeException("unknown interpolation type " + type);
        }

        DialogPane dialogPane = loader.load();
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setTitle(title);
        dialog.setDialogPane(dialogPane);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return loader.<InterpolationGaussianDialogController>getController().get();
            }

            return fallback;
        });
        return dialog.showAndWait().orElse(fallback);
    }
}
