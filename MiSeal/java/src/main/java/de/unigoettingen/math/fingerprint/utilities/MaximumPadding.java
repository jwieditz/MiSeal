package de.unigoettingen.math.fingerprint.utilities;

import de.unigoettingen.math.fingerprint.interpolation.Interpolation;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing2D;

public class MaximumPadding {

    private int padX;
    private int padY;

    public int getPadX() {
        return padX;
    }

    public int getPadY() {
        return padY;
    }

    public void update(Smoothing2D smoothing) {
        if (smoothing == null) {
            return;
        }
        padX = Math.max(padX, smoothing.getSizeX());
        padY = Math.max(padY, smoothing.getSizeY());
    }

    public void update(Interpolation interpolation) {
        if (interpolation == null) {
            return;
        }
        padX = Math.max(padX, interpolation.getSizeX());
        padY = Math.max(padY, interpolation.getSizeY());
    }
}
