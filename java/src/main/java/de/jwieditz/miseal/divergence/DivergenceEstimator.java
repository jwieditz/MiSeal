package de.jwieditz.miseal.divergence;

import de.jwieditz.miseal.FingerprintImage;

public interface DivergenceEstimator {

    void calculateDivergence(FingerprintImage image);
}
