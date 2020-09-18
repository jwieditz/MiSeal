package de.jwieditz.miseal.intensity;

import de.jwieditz.miseal.FingerprintImage;

public interface IntensityEstimator {

    void estimateIntensity(FingerprintImage image);

    void estimateNecessaryMinutiae(FingerprintImage image);

}
