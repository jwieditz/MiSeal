package de.unigoettingen.math.fingerprint.display.old;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import de.unigoettingen.math.fingerprint.display.old.ImageDisplay;

public class OrientationImageDisplay extends ImageDisplay {

    private static final DecimalFormat format = new DecimalFormat("0.00000");

    private double[][] orientationMatrix;

    public OrientationImageDisplay(double[][] data, double[][] orientationMatrix) {
        super(data);
        this.orientationMatrix = orientationMatrix;
    }

    @Override
    protected BufferedImage drawImageToDisplay() {
        int w = data.length;
        int h = data[0].length;
        BufferedImage img = new BufferedImage(scale * w, scale * h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                g.setColor(getNormalizedColor(data[i][j]));
                g.fillRect(scale * i, scale * j, scale, scale);
            }
        }

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (i % 10 == 0 && j % 10 == 0) {
                    g.setColor(Color.RED);
                    g.setStroke(new BasicStroke(2));
                    double angle = orientationMatrix[i][j];
                    if (Double.isNaN(angle)) {
                        continue;
                    }
                    int x1 = (int) (scale * (i + 4 * Math.cos(angle)));
                    int y1 = (int) (scale * (j + 4 * Math.sin(angle)));
                    int x2 = (int) (scale * (i - 4 * Math.cos(angle)));
                    int y2 = (int) (scale * (j - 4 * Math.sin(angle)));
                    g.drawLine(x1, y1, x2, y2);
//                    g.fillOval(scale * i, scale * j, 6, 6);
                }
            }
        }

        return img;
    }

    @Override
    protected String getDisplayInformationAt(int x, int y) {
        return "orientation (rad) = " + format.format(orientationMatrix[x][y]);
    }
}
