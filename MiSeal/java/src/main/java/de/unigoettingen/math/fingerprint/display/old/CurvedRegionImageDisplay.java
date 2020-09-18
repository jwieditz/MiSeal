package de.unigoettingen.math.fingerprint.display.old;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.unigoettingen.math.fingerprint.Point;

public class CurvedRegionImageDisplay extends ImageDisplay {

    private static final int OFFSET = 20; // pixel

    private Point[][] region;
    private int minX;
    private int minY;

    public CurvedRegionImageDisplay(double[][] data, Point[][] region) {
        super(data);
        this.region = region;
    }

    @Override
    protected BufferedImage drawImageToDisplay() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = 0;
        double maxY = 0;

        for (Point[] pArray : region) {
            for (Point p : pArray) {
                if (p.getX() < minX) {
                    minX = p.getX();
                }
                if (p.getX() > maxX) {
                    maxX = p.getX();
                }
                if (p.getY() < minY) {
                    minY = p.getY();
                }
                if (p.getY() > maxY) {
                    maxY = p.getY();
                }
            }
        }

        this.minX = (int) Math.floor(minX);
        this.minY = (int) Math.floor(minY);

        int w = (int) (Math.ceil(maxX) - Math.floor(minX)) + 2 * OFFSET;
        int h = (int) (Math.ceil(maxY) - Math.floor(minY)) + 2 * OFFSET;
        scale = 1;

        while(w * scale <= 1090 && h * scale <= 1090) {
            scale++;
        }

        BufferedImage img = new BufferedImage(scale * w, scale * h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int x = i + (int) Math.floor(minX) - OFFSET;
                int y = j + (int) Math.floor(minY) - OFFSET;
                if (x < 0 || x >= data.length || y < 0 || y >= data[0].length) {
                    g.setColor(getNormalizedColor(Double.NaN));
                } else {
                    double val = data[x][y];
                    g.setColor(getNormalizedColor(val));
                }
                g.fillRect(scale * i, scale * j, scale, scale);
            }
        }

        for (Point[] pArray : region) {
            for (Point p : pArray) {
                int x = (int) Math.round(scale * (p.getX() - Math.floor(minX) + OFFSET));
                int y = (int) Math.round(scale * (p.getY() - Math.floor(minY) + OFFSET));

                g.setColor(Color.BLUE);
                g.fillRect(x, y, scale / 2, scale / 2);
            }
        }

        int midX = (int) Math.round(scale * (region[(region.length - 1) / 2][(region[0].length - 1) / 2].getX() - Math.floor(minX) + OFFSET));
        int midY = (int) Math.round(scale * (region[(region.length - 1) / 2][(region[0].length - 1) / 2].getY() - Math.floor(minY) + OFFSET));
        g.setColor(Color.RED);
        g.fillRect(midX, midY, scale - 2, scale - 2);

        return img;
    }

    @Override
    protected String getDisplayInformationAt(int x, int y) {
        int scaledX = x + minX - OFFSET;
        int scaledY = y + minY - OFFSET;
        return "scale = " + scale + "  |  scaled x = " + scaledX + "  |  scaled y = " + scaledY + "  |  " + super.getDisplayInformationAt(scaledX, scaledY);
    }
}
