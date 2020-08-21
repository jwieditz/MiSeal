package de.unigoettingen.math.fingerprint.display.old;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.awt.geom.AffineTransform;

import de.unigoettingen.math.fingerprint.display.old.ImageDisplay;

public class GradientImageDisplay extends ImageDisplay {

    private static final DecimalFormat format = new DecimalFormat("0.00000");

    private double[][] gradientX;
    private double[][] gradientY;

    public GradientImageDisplay(double[][] data, double[][] gradientX, double[][] gradientY) {
        super(data);
        this.gradientX = gradientX;
        this.gradientY = gradientY;
    }

    private final int ARR_SIZE = 4;

    void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.drawLine(0, 0, len, 0);
        g.fillPolygon(new int[] {len, len - ARR_SIZE, len - ARR_SIZE, len}, new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
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

                    double dx = gradientX[i][j];
                    double dy = gradientY[i][j];

                    if (Double.isNaN(dx) || Double.isNaN(dx)) {
                        continue;
                    }
                    int x2 = (int) (scale * (i + 10000 * dx));
                    int y2 = (int) (scale * (j + 10000 * dy));

                    g.setColor(Color.RED);
                    g.setStroke(new BasicStroke(2));
//                    g.fillOval(scale * i, scale * j, 22, 22);
                    drawArrow(g, scale * i, scale * j, x2 , y2 );

                }
            }
        }


        return img;
    }

    @Override
    protected String getDisplayInformationAt(int x, int y) {
        return "orientation (rad) = " + format.format(Math.atan2(gradientY[x][y], gradientX[x][y])) + " magnitude = " + format.format(Math.sqrt( Math.pow(gradientX[x][y], 2) + Math.pow(gradientY[x][y], 2)));
    }
}
