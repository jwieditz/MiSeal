package de.unigoettingen.math.fingerprint.divergence;

import de.unigoettingen.math.fingerprint.FingerprintImage;

public interface DivergenceEstimator {

    void calculateDivergence(FingerprintImage image);
}
