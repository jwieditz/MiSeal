package de.jwieditz.miseal.display.masks;

import de.jwieditz.miseal.FingerprintImage;
import de.jwieditz.miseal.display.color.ColorPalette;
import javafx.scene.canvas.Canvas;

public interface Mask {

    Canvas draw(FingerprintImage image, ColorPalette colorPalette);
}
