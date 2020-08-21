package de.unigoettingen.math.fingerprint.display.masks;

import de.unigoettingen.math.fingerprint.FingerprintImage;
import de.unigoettingen.math.fingerprint.display.color.ColorPalette;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RoiMask implements Mask {

    @Override
    public Canvas draw(FingerprintImage image, ColorPalette colorPalette) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        return new ResizableCanvas() {

            @Override
            protected void draw() {
                super.draw();

                final double scaleX = getWidth() / image.getWidth();
                final double scaleY = getHeight() / image.getHeight();

                GraphicsContext gc = getGraphicsContext2D();
                gc.clearRect(0, 0, getWidth(), getHeight());

                for (int x = 0; x < imageWidth; x++) {
                    for (int y = 0; y < imageHeight; y++) {

                        double posX = x * scaleX;
                        double posY = y * scaleY;

                        if (!image.getROI()[x][y]) {
                            gc.setFill(colorPalette.getRoiColor());
                        } else {
                            gc.setFill(Color.TRANSPARENT);
                        }
                        gc.fillRect(posX - 0.5, posY - 0.5, scaleX + 1, scaleY + 1);
                    }
                }
            }
        };
    }
}
