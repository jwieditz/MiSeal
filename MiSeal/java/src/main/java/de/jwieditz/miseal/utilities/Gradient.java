package de.jwieditz.miseal.utilities;

import java.util.Arrays;

import de.jwieditz.miseal.PatchedData;
import de.jwieditz.miseal.smoothing.GaussianSmoothing2D;
import de.jwieditz.miseal.utilities.convolution.Convolution;

import static de.jwieditz.miseal.PatchedData.DataType.RIDGE_FREQUENCY;

public class Gradient {

    private final int width;
    private final int height;
    private double[][] gradientX;
    private double[][] gradientY;
    private double[][] orientation;
    private double[][] magnitude;
    private boolean[][] roi;

    /**
     * Copy constructor.
     */
    public Gradient(Gradient gradient) {
        this.width = gradient.width;
        this.height = gradient.height;
        this.roi = gradient.roi;
        this.orientation = gradient.orientation;
        this.magnitude = gradient.magnitude;
        this.gradientX = gradient.gradientX;
        this.gradientY = gradient.gradientY;
    }

    public Gradient(int width, int height, double[][] data, boolean[][] roi) {
        this.width = width;
        this.height = height;
        this.roi = roi;

        double[][] gradientX = new double[width][height];
        double[][] gradientY = new double[width][height];
        double[][] orientation = new double[width][height];
        double[][] magnitude = new double[width][height];

        // TODO: necessary?
        Arrays.stream(gradientX).forEach(a -> Arrays.fill(a, Double.NaN));
        Arrays.stream(gradientY).forEach(a -> Arrays.fill(a, Double.NaN));

        // Apply edge detection kernel to determine initial gradient field in x and y direction
        double[][] Gx = Convolution.convolve(data, Convolution.CONVOLUTION_OPTIMAL_5_X);
        double[][] Gy = Convolution.convolve(data, Convolution.CONVOLUTION_OPTIMAL_5_Y);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (!roi[i][j] || Double.isNaN(data[i][j])) {
                    gradientX[i][j] = Double.NaN;
                    gradientY[i][j] = Double.NaN;
                    orientation[i][j] = Double.NaN;
                    magnitude[i][j] = Double.NaN;
                    continue;
                }

                gradientX[i][j] = Gx[i][j];
                gradientY[i][j] = Gy[i][j];
                orientation[i][j] = Math.atan2(Gy[i][j], Gx[i][j]);
                magnitude[i][j] = Math.sqrt(Math.pow(Gx[i][j], 2) + Math.pow(Gy[i][j], 2));
            }
        }

        this.gradientX = gradientX;
        this.gradientY = gradientY;
        this.orientation = orientation;
        this.magnitude = magnitude;
    }

    public Gradient(PatchedData.Patch patch) {
        int padX = patch.getPadX();
        int padY = patch.getPadY();
        int fromX = patch.getFromX() - padX;
        int fromY = patch.getFromY() - padY;
        int toX = patch.getToX() + padX;
        int toY = patch.getToY() + padY;
        this.width = toX - fromX;
        this.height = toY - fromY;

        double[][] gradientX = new double[width][height];
        double[][] gradientY = new double[width][height];
        double[][] orientation = new double[width][height];
        double[][] magnitude = new double[width][height];

        // TODO: necessary?
        Arrays.stream(gradientX).forEach(a -> Arrays.fill(a, Double.NaN));
        Arrays.stream(gradientY).forEach(a -> Arrays.fill(a, Double.NaN));

        // Apply edge detection kernel to determine initial gradient field in x and y direction
        double[][] Gx = Convolution.convolvePatch(patch, RIDGE_FREQUENCY, Convolution.CONVOLUTION_OPTIMAL_5_X);
        double[][] Gy = Convolution.convolvePatch(patch, RIDGE_FREQUENCY, Convolution.CONVOLUTION_OPTIMAL_5_Y);

        for (int i = fromX; i < toX; i++) {
            for (int j = fromY; j < toY; j++) {
                if (!patch.isInRoi(i, j) || Double.isNaN(patch.get(RIDGE_FREQUENCY, i, j))) {
                    gradientX[i - fromX][j - fromY] = Double.NaN;
                    gradientY[i - fromX][j - fromY] = Double.NaN;
                    orientation[i - fromX][j - fromY] = Double.NaN;
                    magnitude[i - fromX][j - fromY] = Double.NaN;
                    continue;
                }

                gradientX[i - fromX][j - fromY] = Gx[i - fromX][j - fromY];
                gradientY[i - fromX][j - fromY] = Gy[i - fromX][j - fromY];
                orientation[i - fromX][j - fromY] = Math.atan2(Gy[i - fromX][j - fromY], Gx[i - fromX][j - fromY]);
                magnitude[i - fromX][j - fromY] = Math.sqrt(Math.pow(Gx[i - fromX][j - fromY], 2) + Math.pow(Gy[i - fromX][j - fromY], 2));
            }
        }

        this.gradientX = gradientX;
        this.gradientY = gradientY;
        this.orientation = orientation;
        this.magnitude = magnitude;
    }

    /**
     * Generate gradient from two 2D arrays containing the cartesian coordinates of the gradient.
     */
    public Gradient(int width, int height, double[][] Gx, double[][] Gy, boolean[][] roi) {
        this.width = width;
        this.height = height;
        this.roi = roi;
        this.gradientX = Gx;
        this.gradientY = Gy;
        this.orientation = new double[width][height];
        this.magnitude = new double[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (Double.isNaN(Gx[i][j]) || Double.isNaN(Gy[i][j])) {
                    this.orientation[i][j] = Double.NaN;
                    this.magnitude[i][j] = Double.NaN;
                    continue;
                }
                this.orientation[i][j] = Math.atan2(Gy[i][j], Gx[i][j]);
                this.magnitude[i][j] = Math.sqrt(Math.pow(Gx[i][j], 2) + Math.pow(Gy[i][j], 2));
            }
        }
    }

    private Gradient(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public double[][] getGradientX() {
        return gradientX;
    }

    public double[][] getGradientY() { return gradientY; }

    public double[][] getMagnitude() { return magnitude; }

    public double[][] getOrientation() { return orientation; }

    public void smoothAngles(double smoothingVariance) {
        // initialise temporary arrays for smoothed gradient
        double[][] orientation_tmp = new double[width][height];
        double[][] magnitude_tmp = new double[width][height];
        double[][] Gx_tmp = new double[width][height];
        double[][] Gy_tmp = new double[width][height];

        // declare gradient matrices
        double[][] Gx = getGradientX();
        double[][] Gy = getGradientY();
        double[][] Gxx = new double[width][height];
        double[][] Gxy = new double[width][height];
        double[][] Gyy = new double[width][height];

        // Numeric approach - estimating orientation field and smoothing afterwards (Bazen Gerez)
        // define size of smoothing window/ bandwidth of Gaussian kernel
        int winSize = 10;

        // Implementation of one smoothing step, smoothing for a window of size winSize with a Gaussian weighting function
        // Using the approach from Bazen & Gerez 2002
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (!roi[i][j] || Double.isNaN(Gx[i][j]) || Double.isNaN(Gy[i][j]) || i < winSize + 1 || i >= width - winSize - 1 || j < winSize + 1 || j >= height - winSize - 1) {
                    orientation_tmp[i][j] = Double.NaN;
                    magnitude_tmp[i][j] = Double.NaN;
                    continue;
                }

                for (int l = -winSize; l <= winSize; l++) {
                    for (int k = -winSize; k <= winSize; k++) {

                        // Sum over all elements of the window, smoothing with a Gaussian Window
                        if (!Double.isNaN(Gx[i + l][j + k]) && !Double.isNaN(Gy[i + l][j + k])) {
                            // averaging the doubled vectors
                            double weight = Math.exp(-(Math.pow(l, 2) + Math.pow(k, 2)) / (2 * smoothingVariance));
                            Gxx[i][j] += Math.pow(Gx[i + l][j + k], 2) * weight;
                            Gyy[i][j] += Math.pow(Gy[i + l][j + k], 2) * weight;
                            Gxy[i][j] += (Gx[i + l][j + k] * Gy[i + l][j + k]) * weight;
                        }
                    }
                }

                // rescaling
                Gxx[i][j] /= (2.0 * Math.PI * smoothingVariance);
                Gyy[i][j] /= (2.0 * Math.PI * smoothingVariance);
                Gxy[i][j] /= (2.0 * Math.PI * smoothingVariance);

                // calculation of the "halfed" vectors
                double phi = 0.5 * Math.atan2(2 * Gxy[i][j], (Gxx[i][j] - Gyy[i][j]));
                double r = Math.pow(Math.pow(Gxx[i][j] - Gyy[i][j], 2) + Math.pow(2.0 * Gxy[i][j], 2), 0.25);

                orientation_tmp[i][j] = phi;
                magnitude_tmp[i][j] = r;
                Gx_tmp[i][j] = r * Math.cos(phi);
                Gy_tmp[i][j] = r * Math.sin(phi);
            }
        }

        this.orientation = orientation_tmp;
        this.magnitude = magnitude_tmp;
        this.gradientX = Gx_tmp;
        this.gradientY = Gy_tmp;
    }

    public void smoothGradients(double smoothingVariance) {
        // initialise temporary arrays for smoothed gradient
        double[][] orientation_tmp = new double[width][height];
        double[][] magnitude_tmp = new double[width][height];
        double[][] Gx_tmp = new double[width][height];
        double[][] Gy_tmp = new double[width][height];

        // get gradients to smooth
        double[][] Gx = getGradientX();
        double[][] Gy = getGradientY();

        // define size of smoothing window
        int winSize = 21;

        GaussianSmoothing2D kernel = new GaussianSmoothing2D(winSize, 0, smoothingVariance);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                if (!roi[i][j]) {
                    Gx_tmp[i][j] = Double.NaN;
                    Gy_tmp[i][j] = Double.NaN;
                    orientation_tmp[i][j] = Double.NaN;
                    magnitude_tmp[i][j] = Double.NaN;
                    continue;
                }

                double x = kernel.smooth(i, j, Gx);
                double y = kernel.smooth(i, j, Gy);
                Gx_tmp[i][j] = x;
                Gy_tmp[i][j] = y;
                orientation_tmp[i][j] = Math.atan2(y, x);
                magnitude_tmp[i][j] = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
            }
        }

        this.gradientX = Gx_tmp;
        this.gradientY = Gy_tmp;
        this.orientation = orientation_tmp;
        this.magnitude = magnitude_tmp;
    }

    public Gradient squaredGradient() {
        Gradient squaredGradient = new Gradient(this);

        // initialise temporary arrays for smoothed gradient
        double[][] orientation_tmp = new double[width][height];
        double[][] magnitude_tmp = new double[width][height];
        double[][] Gx_tmp = new double[width][height];
        double[][] Gy_tmp = new double[width][height];

        // declare gradient matrices
        double[][] Gx = this.getGradientX();
        double[][] Gy = this.getGradientY();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (!roi[i][j] || Double.isNaN(Gx[i][j]) || Double.isNaN(Gy[i][j])) {
                    Gx_tmp[i][j] = Double.NaN;
                    Gy_tmp[i][j] = Double.NaN;
                    orientation_tmp[i][j] = Double.NaN;
                    magnitude_tmp[i][j] = Double.NaN;
                    continue;
                }
                Gx_tmp[i][j] = Math.pow(Gx[i][j], 2) + Math.pow(Gy[i][j], 2);
                Gy_tmp[i][j] = 2 * Gx[i][j] * Gy[i][j];
                orientation_tmp[i][j] = Math.atan2(Gy_tmp[i][j], Gx_tmp[i][j]);
                magnitude_tmp[i][j] = Math.sqrt(Math.pow(Gx_tmp[i][j], 2) + Math.pow(Gy_tmp[i][j], 2));
            }
        }

        squaredGradient.orientation = orientation_tmp;
        squaredGradient.magnitude = magnitude_tmp;
        squaredGradient.gradientX = Gx_tmp;
        squaredGradient.gradientY = Gy_tmp;
        return squaredGradient;
    }

    /**
     * Generate gradient from two 2D arrays containing the cartesian coordinates of the gradient
     */
    public static Gradient gradientFromOrientation(int width, int height, double[][] orientation, boolean[][] roi) {
        Gradient gradient = new Gradient(width, height);

        double[][] gradientX_tmp = new double[width][height];
        double[][] gradientY_tmp = new double[width][height];
        double[][] orientation_tmp = new double[width][height];
        double[][] magnitude_tmp = new double[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (Double.isNaN(orientation[i][j]) || !roi[i][j]) {
                    orientation_tmp[i][j] = Double.NaN;
                    gradientX_tmp[i][j] = Double.NaN;
                    gradientY_tmp[i][j] = Double.NaN;
                    magnitude_tmp[i][j] = Double.NaN;
                    continue;
                }
                orientation_tmp[i][j] = orientation[i][j];
                gradientX_tmp[i][j] = Math.cos(orientation[i][j]);
                gradientY_tmp[i][j] = Math.sin(orientation[i][j]);
                magnitude_tmp[i][j] = 1;
            }
        }
        gradient.orientation = orientation_tmp;
        gradient.gradientX = gradientX_tmp;
        gradient.gradientY = gradientY_tmp;
        gradient.magnitude = magnitude_tmp;
        gradient.roi = roi;
        return gradient;
    }
}