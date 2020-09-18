package de.jwieditz.miseal.orientation;

import de.jwieditz.miseal.FingerprintImage;
import de.jwieditz.miseal.utilities.Gradient;

public class DefaultOrientationEstimator implements OrientationEstimator {

    private final double smoothingVariance;
    private int iterations;

    public DefaultOrientationEstimator(double smoothingVariance) {
        this.smoothingVariance = smoothingVariance;
    }

    public void setIterations(int iterations) {
        if (iterations < 1) {
            throw new IllegalArgumentException("iterations < 1");
        }
        this.iterations = iterations;
    }

    public void calculateOrientation(FingerprintImage image) {

        Gradient gradient = new Gradient(image.getWidth(), image.getHeight(), image.getNormalizedImageMatrix(), image.getROI());
        gradient.smoothAngles(smoothingVariance);
        gradient.smoothAngles(smoothingVariance);

        double[][] orientation = gradient.getOrientation();

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                orientation[i][j] += (Math.PI / 2);
                if (orientation[i][j] > Math.PI / 2) {
                    orientation[i][j] -= Math.PI;
                }
            }
        }
        image.setOrientationMatrix(orientation);
    }
}
