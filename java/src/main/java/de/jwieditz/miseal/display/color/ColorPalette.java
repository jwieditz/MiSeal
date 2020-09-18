package de.jwieditz.miseal.display.color;

import javafx.scene.paint.Color;

public interface ColorPalette {

    int getId();

    String getName();

    Color getMinutiaeColor();

    Color getRoiColor();

    Color getOrientationColor();

    Color getIntegralLinesColor();

    Color getPatchesColor();

    Color getSingularityPatchColor();

    Color getSingularityColor();

    Color interpolateColor(double normalizedValue);

    static ColorPalette[] values() {
        return new ColorPalette[]{new GreyscalePalette(), new ColoredPalette()};
    }
}
