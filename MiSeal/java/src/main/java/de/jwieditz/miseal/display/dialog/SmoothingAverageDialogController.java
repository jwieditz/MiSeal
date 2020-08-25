package de.jwieditz.miseal.display.dialog;

import de.jwieditz.miseal.smoothing.AverageSmoothing;
import de.jwieditz.miseal.smoothing.Smoothing2D;
import de.jwieditz.miseal.display.TextFieldUtil;
import javafx.scene.control.TextField;

public class SmoothingAverageDialogController implements Smoothing2DDialogController {

    public TextField size;

    @Override
    public Smoothing2D get() {
        int size = TextFieldUtil.getIntOrDefault(this.size, 1);
        return new AverageSmoothing(size);
    }
}
