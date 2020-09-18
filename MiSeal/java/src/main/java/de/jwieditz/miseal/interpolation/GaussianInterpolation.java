package de.jwieditz.miseal.interpolation;

import java.util.function.Function;

import de.jwieditz.miseal.PatchedData;

public class GaussianInterpolation implements Interpolation {

    private final double mean;
    private final double variance;

    private final int size;
    private final int width;

    private double[][] kernel;

    private final Function<Double, Double> mu = x -> Math.max(0, x);
    private final Function<Double, Integer> delta = x -> x <= 0 ? 0 : 1;

    public GaussianInterpolation(double mean, double variance, int size, int width) {
        this.mean = mean;
        this.variance = variance;

        if (size % 2 == 0) {
            throw new IllegalArgumentException("size must be odd");
        }
        this.size = size;
        this.width = width;

        initializeKernel();
    }

    @Override
    public double interpolate(double x, double y, double[][] data) {
        int xRounded = (int) Math.round(x);
        int yRounded = (int) Math.round(y);
        if (xRounded - size / 2 * width < 0
                || xRounded + size / 2* width >= data.length
                || yRounded - size / 2 * width < 0
                || yRounded + size / 2 * width >= data[0].length) {
            return Double.NaN;
        }

        double numerator = 0;
        double denominator = 0;
        int winSize = size / 2;

        for (int u = -winSize; u <= winSize; u++) {
            for (int v = -winSize; v <= winSize; v++) {
                double freq = data[xRounded - u * width][yRounded - v * width];
                double wg = kernel[u + winSize][v + winSize];
                numerator += (wg * mu.apply(freq));
                denominator += (wg * delta.apply(freq + 1));
            }
        }

        if (numerator == 0 && denominator == 0) {
            return 0;
        } else {
            return numerator / denominator;
        }
    }

    @Override
    public void interpolate(PatchedData.Patch patch, PatchedData.DataType type, double[][] target, boolean onlyValidValues) {
        if (patch == null) {
            return;
        }

        int fromX = patch.getFromX();
        int fromY = patch.getFromY();
        int toX = patch.getToX();
        int toY = patch.getToY();

        for (int x = fromX; x < toX; x++) {
            for (int y = fromY; y < toY; y++) {
                if (!patch.isInRoi(x, y)) {
                    target[x][y] = Double.NaN;
                    continue;
                }

                if (onlyValidValues && patch.get(type, x, y) != -1 && !Double.isNaN(patch.get(type, x, y))) {
                    continue;
                }

                int xRounded = Math.round(x);
                int yRounded = Math.round(y);

                double numerator = 0;
                double denominator = 0;
                int winSize = size / 2;

                for (int u = -winSize; u <= winSize; u++) {
                    for (int v = -winSize; v <= winSize; v++) {
                        double freq = patch.get(type, xRounded - u * width, yRounded - v * width);
                        double wg = kernel[u + winSize][v + winSize];
                        numerator += (wg * mu.apply(freq));
                        denominator += (wg * delta.apply(freq + 1));
                    }
                }

                if (numerator == 0 && denominator == 0) {
                    target[x][y] = 0;
                } else {
                    target[x][y] = numerator / denominator;
                }
            }
        }
    }

    @Override
    public int getSizeX() {
        return size / 2 * width;
    }

    @Override
    public int getSizeY() {
        return size / 2 * width;
    }

    private void initializeKernel() {
        kernel = new double[size][size];

        double sum = 0;

        for (int x = -size / 2; x <= size / 2; x++) {
            for (int y = -size / 2; y <= size / 2; y++) {
                kernel[x + size / 2][y + size / 2] = twoDimGaussian(x, y);
                sum += kernel[x + size / 2][y + size / 2];
            }
        }

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                kernel[x][y] /= sum;
            }
        }
    }

    private double twoDimGaussian(int x, int y) {
        double d1 = x - mean;
        double d2 = y - mean;
        return Math.exp(-((d1 * d1) + (d2 * d2)) / (2 * variance));
    }
}
