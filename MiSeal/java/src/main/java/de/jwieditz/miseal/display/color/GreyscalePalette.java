package de.jwieditz.miseal.display.color;

import javafx.scene.paint.Color;

public class GreyscalePalette implements ColorPalette {

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return "greyscale";
    }

    @Override
    public Color getMinutiaeColor() {
        return new Color(0, 1, 0, 1);
    }

    @Override
    public Color getRoiColor() {
        return Color.CYAN;
    }

    @Override
    public Color getOrientationColor() {
        return Color.RED;
    }

    @Override
    public Color getIntegralLinesColor() {
        return new Color(1, 0.5, 0, 0.4);
    }

    @Override
    public Color getPatchesColor() {
        return new Color(0.22, 0.44, 0.66, 1);
    }

    @Override
    public Color getSingularityPatchColor() {
        return new Color(1, 0.1, 0.1, 0.6);
    }

    @Override
    public Color getSingularityColor() {
        return new Color(1, 0, 1, 1);
    }

    @Override
    public Color interpolateColor(double normalizedValue) {
        if (Double.isNaN(normalizedValue)) {
            return new Color(105 / 255., 0, 0, 1);
        }
        return new Color(normalizedValue, normalizedValue, normalizedValue, 1);
    }
}
