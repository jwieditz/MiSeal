package de.unigoettingen.math.fingerprint.orientation;

import de.unigoettingen.math.fingerprint.FingerprintImage;

public interface OrientationEstimator {

    void calculateOrientation(FingerprintImage image);
}
