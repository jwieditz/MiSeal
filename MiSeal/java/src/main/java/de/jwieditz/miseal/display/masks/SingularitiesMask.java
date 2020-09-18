package de.jwieditz.miseal.display.masks;

import java.awt.Polygon;
import java.util.Arrays;

import de.jwieditz.miseal.FingerprintImage;
import de.jwieditz.miseal.PatchedData;
import de.jwieditz.miseal.Point;
import de.jwieditz.miseal.display.color.ColorPalette;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class SingularitiesMask implements Mask {

    @Override
    public Canvas draw(FingerprintImage image, ColorPalette colorPalette) {
        return new ResizableCanvas() {

            @Override
            protected void draw() {
                super.draw();

                final double scaleX = getWidth() / image.getWidth();
                final double scaleY = getHeight() / image.getHeight();

                // The singularities matrix contains values in {-1, 0, 1, 2}. Here, -1 encodes a delta, 1 and 2 single and double loops, respectively, and 0 regular points.
                double[][] singularities = image.getSingularities();

                // We draw a triangle for a delta and a diamond for single/ double loops.

                GraphicsContext gc = getGraphicsContext2D();
                gc.setStroke(colorPalette.getSingularityColor());
                gc.setFill(colorPalette.getSingularityColor());
                gc.setLineWidth(2.5);

                for (PatchedData.Patch patch : image.getPatches()) {
                    int fromX = patch.getFromX();
                    int fromY = patch.getFromY();
                    int toX = patch.getToX();
                    int toY = patch.getToY();

                    double posX = 0;
                    double posY = 0;
                    int cnt = 0;
                    int type = 0;

                    if (patch.containsSingularity()) {
                        for (int x = fromX; x < toX; x++) {
                            for (int y = fromY; y < toY; y++) {
                                if (!patch.isInRoi(x, y)) {
                                    continue;
                                }
                                if (singularities[x][y] != 0) {
                                    posX += scaleX * x;
                                    posY += scaleY * y;
                                    cnt++;
                                    type = (int) singularities[x][y];
                                }
                            }
                        }
                        posX /= cnt;
                        posY /= cnt;

                        // In case of a delta, draw a triangle.
                        if (type == -1) {
                            Polygon triangle = buildTriangle(posX, posY, 20);
                            gc.strokePolyline(Arrays.stream(triangle.xpoints).asDoubleStream().toArray(), Arrays.stream(triangle.ypoints).asDoubleStream().toArray(), triangle.npoints);
                            gc.fillRect(posX - scaleX, posY - scaleY, 2 * scaleX, 2 * scaleY);
                        } else {
                            // In case of a loop, draw a diamond.
                            Polygon diamond = buildDiamond(posX, posY, 20);
                            gc.strokePolyline(Arrays.stream(diamond.xpoints).asDoubleStream().toArray(), Arrays.stream(diamond.ypoints).asDoubleStream().toArray(), diamond.npoints);
                            gc.fillRect(posX - scaleX, posY - scaleY, 2 * scaleX, 2 * scaleY);
                        }
                    }
                }
            }
        };
    }

    private Polygon buildTriangle(double x, double y, double size) {

        double xOff = size / 2.0;
        double yOff = xOff * Math.sqrt(3);

        double r1 = 1.0 / 3.0;
        double r2 = 2.0 / 3.0;

        Point top = new Point(x, y - (yOff * r2));
        Point left = new Point(x - xOff, y + (yOff * r1));
        Point right = new Point(x + xOff, y + (yOff * r1));

        int[] xCoords = {(int) top.getX(), (int) left.getX(), (int) right.getX(), (int) top.getX()};
        int[] yCoords = {(int) top.getY(), (int) left.getY(), (int) right.getY(), (int) top.getY()};

        return new Polygon(xCoords, yCoords, xCoords.length);
    }

    private Polygon buildDiamond(double x, double y, double size) {

        Point top = new Point(x, (y + size / 2.0));
        Point bottom = new Point(x, (y - size / 2.0));
        Point left = new Point((x - size / 2.0), y);
        Point right = new Point((x + size / 2.0), y);

        int[] xCoords = {(int) top.getX(), (int) left.getX(), (int) bottom.getX(), (int) right.getX(), (int) top.getX()};
        int[] yCoords = {(int) top.getY(), (int) left.getY(), (int) bottom.getY(), (int) right.getY(), (int) top.getY()};

        return new Polygon(xCoords, yCoords, xCoords.length);
    }
}
