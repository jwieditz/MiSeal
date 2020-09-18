package de.jwieditz.miseal.smoothing;

public class GaussianSmoothing implements Smoothing1D {

    private int size;
    private double mean;
    private double variance;

    private double[] kernel;

    public GaussianSmoothing(int size, double mean, double variance) {
        this.size = size;
        this.mean = mean;
        this.variance = variance;

        if (size % 2 == 0) {
            throw new IllegalArgumentException("size must be odd");
        }

        initializeKernel();
    }

    @Override
    public double smooth(int x, double[] data) {
        if (x < 0 || x >= data.length || Double.isNaN(data[x])) {
            return Double.NaN;
        }
        double sum = 0;
        for (int i = 0; i < size; i++) {
            if (x + i - size / 2 >= 0 && x + i - size / 2 < data.length && !Double.isNaN(data[x + i - size / 2])) {
                sum += data[x + i - size / 2] * kernel[i];
            }
        }
        return sum;
    }

    private void initializeKernel() {
        kernel = new double[size];

        double sum = 0;

        for (int x = -size / 2; x <= size / 2; x++) {
            kernel[x + size / 2] = gaussian(x);
            sum += kernel[x + size / 2];
        }

        for  (int i = 0; i < size; i++) {
            kernel[i] /= sum;
        }
    }

    private double gaussian(int x) {
        return Math.exp(-(x - mean) * (x - mean) / (2 * variance));
    }
}
