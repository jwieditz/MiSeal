package de.jwieditz.miseal.display.masks;

import de.jwieditz.miseal.FingerprintImage;
import de.jwieditz.miseal.display.color.ColorPalette;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class IntegralLinesMask implements Mask {

    @Override
    public Canvas draw(FingerprintImage image, ColorPalette colorPalette) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        return new ResizableCanvas() {

            @Override
            protected void draw() {
                super.draw();

                double width = getWidth();
                double height = getHeight();

                final double scaleX = width / image.getWidth();
                final double scaleY = height / image.getHeight();

                double[][] orientation = image.getOrientationMatrix();
                boolean[][] roi = image.getROI();

                if (orientation != null) {

                    GraphicsContext gc = getGraphicsContext2D();
                    gc.setLineWidth(2);
                    gc.setStroke(colorPalette.getIntegralLinesColor());

                    // for each 5th row pixel go to the left until you find the first pixel containing valid orientation
                    // then do the following procedure: 1) go one unit width (scaled pixel) in direction of the orientation in the current pixel
                    // 2) set the resulting pixel of step 1) as the current pixel and iterate 1) [width of image] times or until you leave the image

                    for (int y = 0; y < imageHeight; y += 5) {

                        int i = (int) (imageWidth / 2.0);
                        int j = y;

                        double currentX = i;
                        double currentY = j;
                        double previousOrientation = Double.NaN;

                        for (int x = 0; x < 2 * imageWidth; x++) {

                            if (i < 0 || i >= imageWidth || j < 0 || j >= imageHeight) {
                                break;
                            }
                            double or = orientation[i][j];

                            if (Math.abs(or - previousOrientation) > Math.PI - 10.0 / 180.0 * Math.PI) {
                                or += Math.PI;
                            }
                            if (Double.isNaN(or) || !roi[i][j]) {
                                i += 2;
                                currentX = i;
                                continue;
                            }

                            gc.setStroke(colorPalette.getIntegralLinesColor());
                            // normalise the step width to max norm such that the grids in the image and the matrix entries
                            // of the orientation field coincide
                            double maxNorm = Math.max(Math.abs(Math.cos(or)), Math.abs(Math.sin(or)));
                            double nextX = currentX + Math.cos(or) / maxNorm;
                            double nextY = currentY + Math.sin(or) / maxNorm;

                            i = (int) nextX;
                            j = (int) nextY;
                            gc.strokeLine(scaleX * currentX, scaleY * currentY, scaleX * nextX, scaleY * nextY);
                            currentX = nextX;
                            currentY = nextY;
                            previousOrientation = or;
                        }

                        i = (int) (imageWidth / 2.0);
                        j = y;

                        currentX = i;
                        currentY = j;
                        previousOrientation = Double.NaN;

                        for (int x = 0; x < 2 * imageWidth; x++) {

                            if (i < 0 || i >= imageWidth || j < 0 || j >= imageHeight) {
                                break;
                            }
                            double or = orientation[i][j];

                            if (Math.abs(or - previousOrientation) > Math.PI - 10.0 / 180.0 * Math.PI) {
                                or += Math.PI;
                            }
                            if (Double.isNaN(or) || !roi[i][j]) {
                                i -= 2;
                                currentX = i;
                                continue;
                            }

                            gc.setStroke(colorPalette.getIntegralLinesColor());
                            // normalise the step width to max norm such that the grids in the image and the matrix entries
                            // of the orientation field coincide
                            double maxNorm = Math.max(Math.abs(Math.cos(or)), Math.abs(Math.sin(or)));
                            double nextX = currentX - Math.cos(or) / maxNorm;
                            double nextY = currentY - Math.sin(or) / maxNorm;

                            i = (int) nextX;
                            j = (int) nextY;
                            gc.strokeLine(scaleX * currentX, scaleY * currentY, scaleX * nextX, scaleY * nextY);
                            currentX = nextX;
                            currentY = nextY;
                            previousOrientation = or;
                        }
                    }
                }
            }
        };

    }
}
