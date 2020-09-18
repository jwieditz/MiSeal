package de.jwieditz.miseal.display.masks;

import de.jwieditz.miseal.FingerprintImage;
import de.jwieditz.miseal.Minutia;
import de.jwieditz.miseal.display.color.ColorPalette;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class MinutiaeMask implements Mask {

    @Override
    public Canvas draw(FingerprintImage image, ColorPalette colorPalette) {
        return new ResizableCanvas() {

            @Override
            protected void draw() {
                super.draw();

                final double scaleX = getWidth() / image.getWidth();
                final double scaleY = getHeight() / image.getHeight();

                if (image.getMinutiae() != null) {
                    final int radius = 7;
                    final double length = 2.5;

                    GraphicsContext gc = getGraphicsContext2D();
                    gc.setStroke(colorPalette.getMinutiaeColor());
                    gc.setLineWidth(2);

                    for (Minutia minutia : image.getMinutiae()) {
                        double x = scaleX * minutia.getX();
                        double y = scaleY * minutia.getY();
                        double or = -minutia.getOrientation();

                        gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);
                        gc.strokeLine(x + radius * Math.cos(or),
                                y + radius * Math.sin(or),
                                x + length * radius * Math.cos(or),
                                y + length * radius * Math.sin(or));
                    }
                }
            }
        };
    }
}
