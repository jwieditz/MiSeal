package de.unigoettingen.math.fingerprint.singularPoints;

import java.util.Arrays;

import de.unigoettingen.math.fingerprint.FingerprintImage;
import de.unigoettingen.math.fingerprint.utilities.Utils;

public class DefaultSingularPoints implements SingularPoints {

    final double eps = 1E-2;

    @Override
    public void computeSingularPoints(FingerprintImage image) {
        boolean[][] roi = image.getROI();
        int w = image.getWidth();
        int h = image.getHeight();

        double[][] poincareIndex = Utils.getNaNMatrix(w, h);
        double[][] singularities = Utils.getNaNMatrix(w, h);

        double[][] orientation = image.getOrientationMatrix();

        int k = 1;
        int l = 1;
        int cutoff = Math.max(k, l) + 1;

        // Determine the singular points using the Poincaré index as in Bazen & Gerez (2002)

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (!roi[x][y] || x < cutoff || x >= w - cutoff || y < cutoff || y >= h - cutoff) {
                    poincareIndex[x][y] = Double.NaN;
                    singularities[x][y] = Double.NaN;
                    continue;
                }

                // Compute the Poincaré index pixelwise using Green's theorem via the rotation.
                double theta1 = mod2Pi(2 * orientation[x + k][y + l] - 2 * orientation[x + k][y - l]);
                double theta2 = mod2Pi(2 * orientation[x - k][y + k] - 2 * orientation[x - k][y - l]);
                double theta3 = mod2Pi(2 * orientation[x + k][y + l] - 2 * orientation[x - k][y - l]);
                double theta4 = mod2Pi(2 * orientation[x + k][y - l] - 2 * orientation[x - k][y - l]);

                poincareIndex[x][y] = (theta1 - theta2) / (2.0 * k) - (theta3 - theta4) / (2.0 * l);

                // A Poincaré index of 2 * Pi indicates a double loop...
                if (poincareIndex[x][y] > 2 * Math.PI - eps) {
                    singularities[x][y] = 2;
                    continue;
                }

                // ... of Pi indicates a single loop ...
                if (poincareIndex[x][y] > Math.PI - eps) {
                    singularities[x][y] = 1;
                    continue;
                }

                // ... of -Pi indicates a delta ...
                if (poincareIndex[x][y] < -Math.PI + eps) {
                    singularities[x][y] = -1;
                    continue;
                }

                // ... and of (about) zero a regular point of the fingerprint.
                singularities[x][y] = 0;
            }
        }

        image.setSingularities(singularities);
    }

    double mod2Pi(double theta) {
        double normalized = theta % (2 * Math.PI);
        normalized = (normalized + 2 * Math.PI) % (2 * Math.PI);
        return normalized <= Math.PI ? normalized : normalized - (2 * Math.PI);
    }
}



