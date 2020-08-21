package de.unigoettingen.math.fingerprint.display.masks;

import de.unigoettingen.math.fingerprint.FingerprintImage;
import de.unigoettingen.math.fingerprint.display.color.ColorPalette;
import javafx.scene.canvas.Canvas;

public interface Mask {

    Canvas draw(FingerprintImage image, ColorPalette colorPalette);
}
