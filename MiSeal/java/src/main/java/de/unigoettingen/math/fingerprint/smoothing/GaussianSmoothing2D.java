package de.unigoettingen.math.fingerprint.smoothing;

import de.unigoettingen.math.fingerprint.PatchedData;

public class GaussianSmoothing2D implements Smoothing2D {

    private final int size;
    private final double mean;
    private final double variance;

    private double[][] kernel;

    public GaussianSmoothing2D(int size, double mean, double variance) {
        this.size = size;
        this.mean = mean;
        this.variance = variance;

        if (size % 2 == 0) {
            throw new IllegalArgumentException("size must be odd");
        }

        initializeKernel();
    }

    @Override
    public double smooth(int x, int y, double[][] data) {
        if (x < 0 || x >= data.length || y < 0 || y >= data[0].length || Double.isNaN(data[x][y])) {
            return Double.NaN;
        }

        // The substitution of NaNs with zero introduces an error. We adjust this by diving the result with the actual weight of the kernel on the !NaN region.
        // Note, that if the kernel's support is completely !NaN, then the adjustmentFactor = 1 (i.e. no adjustment).
        double adjustmentFactor = 0;

        double sum = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (x + i - size / 2 >= 0 && x + i - size / 2 < data.length && y + j - size / 2 >= 0 && y + j - size / 2 < data[0].length && !Double.isNaN(data[x + i - size / 2][y + j - size / 2])) {
                    sum += data[x + i - size / 2][y + j - size / 2] * kernel[i][j];
                    adjustmentFactor += kernel[i][j];
                }
            }
        }
        return sum / adjustmentFactor;
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

                // The substitution of NaNs with zero introduces an error. We adjust this by diving the result with the actual weight of the kernel on the !NaN region.
                // Note, that if the kernel's support is completely !NaN, then the adjustmentFactor = 1 (i.e. no adjustment).
                double adjustmentFactor = 0;

                double sum = 0;
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        int xAdj = x + i - size / 2;
                        int yAdj = y + j - size / 2;
                        double val = patch.get(type, xAdj, yAdj);
                        if (Double.isNaN(val) || !patch.isInRoi(xAdj, yAdj)) {
                            continue;
                        }
                        sum += val * kernel[i][j];
                        adjustmentFactor += kernel[i][j];
                    }
                }

                if (adjustmentFactor == 0) {
                    target[x][y] = Double.NaN;
                } else {
                    target[x][y] = sum / adjustmentFactor;
                }
            }
        }
    }

    @Override
    public int getSizeX() {
        return size / 2;
    }

    @Override
    public int getSizeY() {
        return size / 2;
    }

    private void initializeKernel() {
        kernel = new double[size][size];

        double sum = 0;

        for (int x = -size / 2; x <= size / 2; x++) {
            for (int y = -size / 2; y <= size / 2; y++) {
                double val = gaussian(x) * gaussian(y);
                kernel[x + size / 2][y + size / 2] = val;
                sum += val;
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                kernel[i][j] /= sum;
            }
        }
    }

    private double gaussian(int x) {
        return Math.exp(-(x - mean) * (x - mean) / (2 * variance)) / (2 * Math.PI * variance);
    }

}
