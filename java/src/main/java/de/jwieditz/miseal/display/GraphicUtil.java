package de.jwieditz.miseal.display;

import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class GraphicUtil {

    // Bresenham's midpoint circle algorithm
    public static void drawCircle(int centerX, int centerY, int r, Color color, PixelWriter pw) {
        int x = 0, y = r;
        double d = 1.25 - r;
        do {
            pw.setColor(centerX + x, centerY + y, color);
            pw.setColor(centerX + x, centerY - y, color);
            pw.setColor(centerX - x, centerY + y, color);
            pw.setColor(centerX - x, centerY - y, color);
            pw.setColor(centerX + y, centerY + x, color);
            pw.setColor(centerX + y, centerY - x, color);
            pw.setColor(centerX - y, centerY + x, color);
            pw.setColor(centerX - y, centerY - x, color);
            if (d < 0) {
                d += 2 * x + 3;
            } else {
                d += 2 * (x - y) + 5;
                y--;
            }
            x++;
        } while (x <= y);
    }

    // Bresenham's line algorithm
    public static void drawLine(int fromX, int fromY, int toX, int toY, Color color, PixelWriter pw) {
        if (Math.abs(toY - fromY) < Math.abs(toX - fromX)) {
            if (fromX > toX) {
                drawLineLow(toX, toY, fromX, fromY, color, pw);
            } else {
                drawLineLow(fromX, fromY, toX, toY, color, pw);
            }
        } else {
            if (fromY > toY) {
                drawLineHigh(toX, toY, fromX, fromY, color, pw);
            } else {
                drawLineHigh(fromX, fromY, toX, toY, color, pw);
            }
        }
    }

    private static void drawLineLow(int fromX, int fromY, int toX, int toY, Color color, PixelWriter pw) {
        int dx = toX - fromX;
        int dy = toY - fromY;
        int yi = 1;
        if (dy < 0) {
            yi = -1;
            dy = -dy;
        }
        int d = 2 * dy - dx;
        int y = fromY;
        for (int x = fromX; x <= toX; x++) {
            pw.setColor(x, y, color);
            if (d > 0) {
                y += yi;
                d -= 2 * dx;
            }
            d += 2 * dy;
        }
    }

    private static void drawLineHigh(int fromX, int fromY, int toX, int toY, Color color, PixelWriter pw) {
        int dx = toX - fromX;
        int dy = toY - fromY;
        int xi = 1;
        if (dy < 0) {
            xi = -1;
            dx = -dx;
        }
        int d = 2 * dx - dy;
        int x = fromX;
        for (int y = fromY; y <= toY; y++) {
            pw.setColor(x, y, color);
            if (d > 0) {
                x += xi;
                d -= 2 * dy;
            }
            d += 2 * dx;
        }
    }
}
