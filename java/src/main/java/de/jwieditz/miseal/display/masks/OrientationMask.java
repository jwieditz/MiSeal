package de.jwieditz.miseal.display.masks;

import de.jwieditz.miseal.FingerprintImage;
import de.jwieditz.miseal.display.color.ColorPalette;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class OrientationMask implements Mask {

    @Override
    public Canvas draw(FingerprintImage image, ColorPalette colorPalette) {
        return new ResizableCanvas() {

            @Override
            protected void draw() {
                super.draw();

                if (image.getOrientationMatrix() != null) {
                    final int length = 4;
                    final double scaleX = getWidth() / image.getWidth();
                    final double scaleY = getHeight() / image.getHeight();

                    GraphicsContext gc = getGraphicsContext2D();
                    gc.setStroke(colorPalette.getOrientationColor());
                    gc.setLineWidth(2);

                    for (int x = 0; x < image.getWidth(); x += 10) {
                        for (int y = 0; y < image.getHeight(); y += 10) {
                            double or = image.getOrientationMatrix()[x][y];

                            if (Double.isNaN(or)) {
                                continue;
                            }

                            double x1 = (int) (scaleX * (x + length * Math.cos(or)));
                            double y1 = (int) (scaleY * (y + length * Math.sin(or)));
                            double x2 = (int) (scaleX * (x - length * Math.cos(or)));
                            double y2 = (int) (scaleY * (y - length * Math.sin(or)));
                            gc.strokeLine(x1, y1, x2, y2);
                        }
                    }
                }
            }
        };
    }
}
