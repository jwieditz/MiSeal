package de.jwieditz.miseal.display.color;

import javafx.scene.paint.Color;

public class ColoredPalette implements ColorPalette {

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public String getName() {
        return "coloured";
    }

    @Override
    public Color getMinutiaeColor() {
        return Color.BLACK;
    }

    @Override
    public Color getRoiColor() {
        return Color.WHITE;
    }

    @Override
    public Color getOrientationColor() {
        return Color.GHOSTWHITE;
    }

    @Override
    public Color getIntegralLinesColor() {
        return Color.DIMGREY;
    }

    @Override
    public Color getPatchesColor() {
        return Color.BLACK;
    }

    @Override
    public Color getSingularityPatchColor() {
        return Color.WHITE;
    }

    @Override
    public Color getSingularityColor() {
        return Color.BLACK;
    }

    @Override
    public Color interpolateColor(double normalizedValue) {
        if (Double.isNaN(normalizedValue)) {
            return new Color(105 / 255., 0, 0, 1);
        }
        double hue = Color.BLUE.getHue() + (Color.RED.getHue() - Color.BLUE.getHue()) * normalizedValue;
        return Color.hsb(hue, 1.0, 1.0);
    }
}
