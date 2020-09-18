package de.jwieditz.miseal.orientation;

import de.jwieditz.miseal.FingerprintImage;

public interface OrientationEstimator {

    void calculateOrientation(FingerprintImage image);
}
