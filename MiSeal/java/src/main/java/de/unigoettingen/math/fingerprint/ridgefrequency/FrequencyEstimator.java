package de.unigoettingen.math.fingerprint.ridgefrequency;

import de.unigoettingen.math.fingerprint.FingerprintImage;

public interface FrequencyEstimator {

    void estimateFrequency(FingerprintImage image);

}
