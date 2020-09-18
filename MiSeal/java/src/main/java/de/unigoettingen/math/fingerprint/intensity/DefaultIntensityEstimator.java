package de.unigoettingen.math.fingerprint.intensity;

import de.unigoettingen.math.fingerprint.FingerprintImage;
import de.unigoettingen.math.fingerprint.PatchedData;
import de.unigoettingen.math.fingerprint.utilities.Utils;

import static de.unigoettingen.math.fingerprint.PatchedData.DataType.DIVERGENCE;
import static de.unigoettingen.math.fingerprint.PatchedData.DataType.LINE_DIVERGENCE;
import static de.unigoettingen.math.fingerprint.PatchedData.DataType.RIDGE_FREQUENCY;

public class DefaultIntensityEstimator implements IntensityEstimator {

    @Override
    public void estimateIntensity(FingerprintImage image) {
        double[][] intensity = Utils.getNaNMatrix(image.getWidth(), image.getHeight());
        image.setIntensityMatrix(intensity);

        for (PatchedData.Patch patch : image.getPatches()) {
            estimateIntensityPatch(patch);
        }
    }

    public void estimateNecessaryMinutiae(FingerprintImage image) {
        double[][] necessaryMinutiae = Utils.getNaNMatrix(image.getWidth(), image.getHeight());
        image.setNecessaryMinutiae(necessaryMinutiae);

        for (PatchedData.Patch patch : image.getPatches()) {
            estimateNecessaryMinutiaePatch(patch);
        }
    }

    private void estimateIntensityPatch(PatchedData.Patch patch) {
        int fromX = patch.getFromX();
        int fromY = patch.getFromY();
        int toX = patch.getToX();
        int toY = patch.getToY();

        for (int x = fromX; x < toX; x++) {
            for (int y = fromY; y < toY; y++) {
                if (!patch.isInRoi(x, y)) {
                    patch.setIntensity(x, y, Double.NaN);
                    continue;
                }

                patch.setIntensity(x, y, Math.abs(patch.get(RIDGE_FREQUENCY, x, y) * patch.get(DIVERGENCE, x, y) + patch.get(LINE_DIVERGENCE, x, y)));
            }
        }
    }

    private void estimateNecessaryMinutiaePatch(PatchedData.Patch patch) {
        int fromX = patch.getFromX();
        int fromY = patch.getFromY();
        int toX = patch.getToX();
        int toY = patch.getToY();

        boolean patchIsNaN = true;
        double necessaryMinutiae = 0;

        for (int x = fromX; x < toX; x++) {
            for (int y = fromY; y < toY; y++) {
                if (!patch.isInRoi(x, y) || Double.isNaN(patch.get(RIDGE_FREQUENCY, x, y)) || Double.isNaN(patch.get(DIVERGENCE, x, y)) || Double.isNaN(patch.get(LINE_DIVERGENCE, x, y))) {
                    continue;
                }
                patchIsNaN = false;
                necessaryMinutiae += patch.get(RIDGE_FREQUENCY, x, y) * patch.get(DIVERGENCE, x, y) + patch.get(LINE_DIVERGENCE, x, y);
            }
        }

        for (int x = fromX; x < toX; x++) {
            for (int y = fromY; y < toY; y++) {
                if (!patchIsNaN) {
                    patch.setNecessaryMinutiae(x, y, Math.abs(necessaryMinutiae));
                } else {
                    patch.setNecessaryMinutiae(x, y, Double.NaN);
                }
            }
        }
    }
}
