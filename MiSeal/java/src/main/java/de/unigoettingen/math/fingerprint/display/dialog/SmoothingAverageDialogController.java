package de.unigoettingen.math.fingerprint.display.dialog;

import de.unigoettingen.math.fingerprint.display.TextFieldUtil;
import de.unigoettingen.math.fingerprint.smoothing.AverageSmoothing;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing2D;
import javafx.scene.control.TextField;

public class SmoothingAverageDialogController implements Smoothing2DDialogController {

    public TextField size;

    @Override
    public Smoothing2D get() {
        int size = TextFieldUtil.getIntOrDefault(this.size, 1);
        return new AverageSmoothing(size);
    }
}
