package de.unigoettingen.math.fingerprint.orientation;

import de.unigoettingen.math.fingerprint.FingerprintImage;
import de.unigoettingen.math.fingerprint.PatchedData;
import de.unigoettingen.math.fingerprint.singularPoints.DefaultSingularPoints;
import de.unigoettingen.math.fingerprint.singularPoints.SingularPoints;
import de.unigoettingen.math.fingerprint.utilities.Gradient;
import de.unigoettingen.math.fingerprint.utilities.unwrap.UnwrapType;
import de.unigoettingen.math.fingerprint.utilities.unwrap.Unwrapper;

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
