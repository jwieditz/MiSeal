package de.jwieditz.miseal.display.dialog;

import de.jwieditz.miseal.smoothing.GaussianSmoothing2D;
import de.jwieditz.miseal.smoothing.Smoothing2D;
import de.jwieditz.miseal.display.TextFieldUtil;
import javafx.scene.control.TextField;

public class SmoothingGaussian2DDialogController implements Smoothing2DDialogController {

    public TextField size;
    public TextField mean;
    public TextField variance;

    @Override
    public Smoothing2D get() {
        int size = TextFieldUtil.getIntOrDefault(this.size, 1);
        double mean = TextFieldUtil.getDoubleOrDefault(this.mean, 0);
        double variance = TextFieldUtil.getDoubleOrDefault(this.variance, 1);
        return new GaussianSmoothing2D(size, mean, variance);
    }
}
