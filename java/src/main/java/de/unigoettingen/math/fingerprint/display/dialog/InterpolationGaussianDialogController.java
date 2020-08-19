package de.unigoettingen.math.fingerprint.display.dialog;

import de.unigoettingen.math.fingerprint.display.TextFieldUtil;
import de.unigoettingen.math.fingerprint.interpolation.GaussianInterpolation;
import javafx.scene.control.TextField;

public class InterpolationGaussianDialogController implements InterpolationDialogController {

    public TextField mean;
    public TextField variance;
    public TextField size;
    public TextField width;

    public GaussianInterpolation get() {
        double mean = TextFieldUtil.getDoubleOrDefault(this.mean, 0);
        double variance = TextFieldUtil.getDoubleOrDefault(this.variance, 1);
        int size = TextFieldUtil.getIntOrDefault(this.size, 1);
        int width = TextFieldUtil.getIntOrDefault(this.width, 1);
        return new GaussianInterpolation(mean, variance, size, width);
    }
}
