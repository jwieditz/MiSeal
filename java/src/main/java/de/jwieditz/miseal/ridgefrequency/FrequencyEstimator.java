package de.jwieditz.miseal.ridgefrequency;

import de.jwieditz.miseal.FingerprintImage;

public interface FrequencyEstimator {

    void estimateFrequency(FingerprintImage image);

}
