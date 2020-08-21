package de.unigoettingen.math.fingerprint.display.masks;

import de.unigoettingen.math.fingerprint.FingerprintImage;
import de.unigoettingen.math.fingerprint.PatchedData;
import de.unigoettingen.math.fingerprint.display.color.ColorPalette;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class PatchesMask implements Mask {

    @Override
    public Canvas draw(FingerprintImage image, ColorPalette colorPalette) {
        return new ResizableCanvas() {

            @Override
            protected void draw() {
                super.draw();

                final double scaleX = getWidth() / image.getWidth();
                final double scaleY = getHeight() / image.getHeight();

                GraphicsContext gc = getGraphicsContext2D();
                gc.setStroke(colorPalette.getPatchesColor());
                gc.setFill(colorPalette.getSingularityPatchColor());

                for (PatchedData.Patch patch : image.getPatches()) {
                    double fromX = scaleX * patch.getFromX();
                    double fromY = scaleY * patch.getFromY();
                    double toX = scaleX * patch.getToX();
                    double toY = scaleY * patch.getToY();

                    gc.strokeRect(fromX, fromY, toX - fromX, toY - fromY);

                    if (patch.containsSingularity()) {
                        gc.fillRect(fromX, fromY, toX - fromX, toY - fromY);
                    }
                }
            }
        };
    }
}
