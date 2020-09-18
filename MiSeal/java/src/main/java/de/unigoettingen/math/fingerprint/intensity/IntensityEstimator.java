package de.unigoettingen.math.fingerprint.intensity;

import de.unigoettingen.math.fingerprint.FingerprintImage;

public interface IntensityEstimator {

    void estimateIntensity(FingerprintImage image);

    void estimateNecessaryMinutiae(FingerprintImage image);

}
