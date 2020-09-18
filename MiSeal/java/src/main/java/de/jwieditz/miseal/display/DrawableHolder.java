package de.jwieditz.miseal.display;

import java.util.HashMap;
import java.util.Map;

import de.jwieditz.miseal.display.color.ColorPalette;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public class DrawableHolder {

    private final Map<Integer, Image> image = new HashMap<>();
    private final Map<Integer, Image> log10Image = new HashMap<>();
    private final Map<Integer, Canvas> scale = new HashMap<>();
    private final Map<Integer, Canvas> log10Scale = new HashMap<>();
    private final double scaleHeight;

    public DrawableHolder(double scaleHeight) {
        this.scaleHeight = scaleHeight;
    }

    public void add(ColorPalette palette, Image image, Image log10Image, Canvas scale, Canvas log10Scale) {
        this.image.put(palette.getId(), image);
        this.log10Image.put(palette.getId(), log10Image);
        this.scale.put(palette.getId(), scale);
        this.log10Scale.put(palette.getId(), log10Scale);
    }

    public Image getImage(ColorPalette colorPalette) {
        return image.get(colorPalette.getId());
    }

    public Image getLog10Image(ColorPalette colorPalette) {
        return log10Image.get(colorPalette.getId());
    }

    public Canvas getScale(ColorPalette colorPalette) {
        return scale.get(colorPalette.getId());
    }

    public Canvas getLog10Scale(ColorPalette colorPalette) {
        return log10Scale.get(colorPalette.getId());
    }

    public double getScaleHeight() {
        return scaleHeight;
    }

}
