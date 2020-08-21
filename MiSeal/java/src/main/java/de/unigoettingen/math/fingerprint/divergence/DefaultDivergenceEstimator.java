/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.unigoettingen.math.fingerprint.divergence;


import de.unigoettingen.math.fingerprint.FingerprintImage;
import de.unigoettingen.math.fingerprint.PatchedData;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing2D;
import de.unigoettingen.math.fingerprint.utilities.Utils;

import static de.unigoettingen.math.fingerprint.PatchedData.DataType.DIVERGENCE;
import static de.unigoettingen.math.fingerprint.PatchedData.DataType.ORIENTATION;

/**
 * @author jwieditz
 */
public class DefaultDivergenceEstimator implements DivergenceEstimator {

    private Smoothing2D divergenceSmoothing;

    public void setSmoothing(Smoothing2D smoothing) {
        divergenceSmoothing = smoothing;
    }

    public Smoothing2D getDivergenceSmoothing() {
        return divergenceSmoothing;
    }

    public void calculateDivergence(FingerprintImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        double[][] divergence = Utils.getNaNMatrix(w, h);
        image.setDivergenceMatrix(divergence);

        for (PatchedData.Patch patch : image.getPatches()) {
            patch.initDivergencePadding();

            calculateDivergencePatch(patch);
        }

        if (divergenceSmoothing != null) {
            double[][] smoothedDivergence = new double[w][h];
            for (PatchedData.Patch patch : image.getPatches()) {
                divergenceSmoothing.smooth(patch, DIVERGENCE, smoothedDivergence);
            }
            image.setDivergenceMatrix(smoothedDivergence);

            for (PatchedData.Patch patch : image.getPatches()) {
                patch.initDivergencePadding();
            }
        }
    }

    private void calculateDivergencePatch(PatchedData.Patch patch) {
        int padX = patch.getPadX();
        int padY = patch.getPadY();
        int fromX = patch.getFromX() - padX;
        int fromY = patch.getFromY() - padY;
        int toX = patch.getToX() + padX;
        int toY = patch.getToY() + padY;

        final int k = 1;
        final int l = 1;

        // calculation of divergence via finite differences
        // here, actually some continuity adjustment has to be made;

        for (int x = fromX; x < toX; x++) {
            for (int y = fromY; y < toY; y++) {
                if (!patch.isInRoi(x, y)) {
                    patch.set(DIVERGENCE, x, y, Double.NaN);
                    continue;
                }

                double theta1 = patch.get(ORIENTATION, x + k, y);
                double theta2 = patch.get(ORIENTATION, x - k, y);
                double theta3 = patch.get(ORIENTATION, x, y + l);
                double theta4 = patch.get(ORIENTATION, x, y - l);

                double divergence = (Math.cos(theta1) - Math.cos(theta2)) / (2.0 * k) + (Math.sin(theta3) - Math.sin(theta4)) / (2.0 * l);
                patch.set(DIVERGENCE, x, y, divergence);
            }
        }
    }
}
