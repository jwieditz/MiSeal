package de.unigoettingen.math.fingerprint.smoothing;

import java.util.function.BiFunction;

import de.unigoettingen.math.fingerprint.PatchedData;

public class AverageSmoothing implements Smoothing2D {

    private final int size;

    public AverageSmoothing(int size) {
        if (size % 2 == 0) {
            throw new IllegalArgumentException("size must be odd");
        }

        this.size = size;
    }

    @Override
    public double smooth(int x, int y, double[][] data) {
        double average = 0;

        if (x < size / 2 || x >= data.length - size / 2 || y < size / 2 || y >= data[0].length - size / 2 || Double.isNaN(data[x][y])) {
            return Double.NaN;
        }

        int numberOfValidPixels = 0;

        for (int i = -size / 2; i <= size / 2; i++) {
            for (int j = -size / 2; j <= size / 2; j++) {
                if (!Double.isNaN(data[x + i][y + j])) {
                    average += data[x + i][y + j];
                    numberOfValidPixels++;
                }
            }
        }

        return average / numberOfValidPixels;
    }

    @Override
    public int getSizeX() {
        return size / 2;
    }

    @Override
    public int getSizeY() {
        return size / 2;
    }

    @Override
    public void smooth(PatchedData.Patch patch, PatchedData.DataType type, double[][] target) {
        if (patch == null) {
            return;
        }

        int fromX = patch.getFromX();
        int fromY = patch.getFromY();
        int toX = patch.getToX();
        int toY = patch.getToY();

        for (int x = fromX; x < toX; x++) {
            for (int y = fromY; y < toY; y++) {

                if (!patch.isInRoi(x, y) || Double.isNaN(patch.get(type, x, y))) {
                    target[x][y] = Double.NaN;
                    continue;
                }

                int computedSize = 0;
                double average = 0;
                for (int i = -size / 2; i <= size / 2; i++) {
                    for (int j = -size / 2; j <= size / 2; j++) {
                        int xAdj = x + i;
                        int yAdj = y + j;
                        double val = patch.get(type, xAdj, yAdj);
                        if (Double.isNaN(val) || !patch.isInRoi(xAdj, yAdj)) {
                            continue;
                        }
                        average += val;
                        computedSize++;
                    }
                }
                average /= computedSize;

                target[x][y] = average;
            }
        }
    }

}
