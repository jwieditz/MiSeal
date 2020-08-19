package de.unigoettingen.math.fingerprint.orientation;

import de.unigoettingen.math.fingerprint.FingerprintImage;
import de.unigoettingen.math.fingerprint.utilities.convolution.Convolution;

/**
 * An {@link OrientationEstimator} which implements the method described by Lin Hong, Yifei Wan
 * and Anil Jain in "Fingerprint Image Enhancement: Algorithm and Performance Evaluation".
 */
public class HWJOrientationEstimator implements OrientationEstimator {

    private boolean useLowPassFilter;

    public HWJOrientationEstimator(boolean useLowPassFilter) {
        this.useLowPassFilter = useLowPassFilter;
    }

    @Override
    public void calculateOrientation(FingerprintImage image) {
        int w = 17;

//        double[][] gradX = Convolution.convolve(image.getNormalizedImage(), Convolution.CONVOLUTION_SOBEL_KERNEL_X);
//        double[][] gradY = Convolution.convolve(image.getNormalizedImage(), Convolution.CONVOLUTION_SOBEL_KERNEL_Y);
        double[][] gradX = Convolution.convolve(image.getNormalizedImageMatrix(), Convolution.CONVOLUTION_OPTIMAL_4_X);
        double[][] gradY = Convolution.convolve(image.getNormalizedImageMatrix(), Convolution.CONVOLUTION_OPTIMAL_4_Y);

        double[][] orientationMatrix = new double[image.getWidth()][image.getHeight()];

        boolean[][] roi = image.getROI();

        double[][] phiX = new double[image.getWidth()][image.getHeight()];
        double[][] phiY = new double[image.getWidth()][image.getHeight()];

        for (int i = w / 2; i < image.getWidth() - w / 2; i++) {
            for (int j = w / 2; j < image.getHeight() - w / 2; j++) {
                if (!roi[i][j]) {
                    orientationMatrix[i][j] = Double.NaN;
                    continue;
                }

                double vx = 0;
                double vy = 0;

                for (int u = i - w / 2; u < i + w / 2; u++) {
                    for (int v = j - w / 2; v < j + w / 2; v++) {
                        vx += (2 * gradX[u][v] * gradY[u][v]);
                        vy += (gradX[u][v] * gradX[u][v] + gradY[u][v] * gradY[u][v]);
                    }
                }

                orientationMatrix[i][j] = Math.atan(vy / vx) / 2;

                phiX[i][j] = 2 * Math.cos(orientationMatrix[i][j]);
                phiY[i][j] = 2 * Math.sin(orientationMatrix[i][j]);
            }
        }

        // low pass filtering (optional)
        if (useLowPassFilter) {
            // Gaussian 5x5 kernel with sigma = 9
            double[][] kernel = {
                    {0.031827, 0.037541, 0.039665, 0.037541, 0.031827},
                    {0.037541, 0.044281, 0.046787, 0.044281, 0.037541},
                    {0.039665, 0.046787, 0.049434, 0.046787, 0.039665},
                    {0.037541, 0.044281, 0.046787, 0.044281, 0.037541},
                    {0.031827, 0.037541, 0.039665, 0.037541, 0.031827}
            };
            int wOmega = 5;
            for (int i = wOmega / 2 * (w - 1); i < image.getWidth() - wOmega / 2 * (w - 1); i++) {
                for (int j = wOmega / 2 * (w - 1); j < image.getHeight() - wOmega / 2 * (w - 1); j++) {
                    if (!roi[i][j]) {
                        orientationMatrix[i][j] = Double.NaN;
                        continue;
                    }

                    double phiXPrime = 0;
                    double phiYPrime = 0;

                    for (int u = -wOmega / 2; u < wOmega / 2; u++) {
                        for (int v = -wOmega / 2; v < wOmega / 2; v++) {
                            phiXPrime += (kernel[u + wOmega / 2][v + wOmega / 2] * phiX[i - u * (w - 1)][j - v * (w - 1)]);
                            phiYPrime += (kernel[u + wOmega / 2][v + wOmega / 2] * phiY[i - u * (w - 1)][j - v * (w - 1)]);
                        }
                    }

                    orientationMatrix[i][j] = Math.atan(phiYPrime / phiXPrime) / 2;

                }
            }
        }

        image.setOrientationMatrix(orientationMatrix);
    }
}
