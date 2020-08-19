package de.unigoettingen.math.fingerprint.display.dialog;

import de.unigoettingen.math.fingerprint.display.TextFieldUtil;
import de.unigoettingen.math.fingerprint.smoothing.GaussianSmoothing;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing1D;
import javafx.scene.control.TextField;

public class SmoothingGaussian1DDialogController implements Smoothing1DDialogController {

    public TextField size;
    public TextField mean;
    public TextField variance;

    @Override
    public Smoothing1D get() {
        int size = TextFieldUtil.getIntOrDefault(this.size, 1);
        double mean = TextFieldUtil.getDoubleOrDefault(this.mean, 0);
        double variance = TextFieldUtil.getDoubleOrDefault(this.variance, 1);
        return new GaussianSmoothing(size, mean, variance);
    }
}
