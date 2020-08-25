package de.jwieditz.miseal;

import de.jwieditz.miseal.smoothing.Smoothing2D;
import de.jwieditz.miseal.utilities.Gradient;
import de.jwieditz.miseal.utilities.Utils;

public class LineDivergenceCalculator {

    private Smoothing2D lineDivergenceSmoothing;
    private boolean transformLog10 = false;

    public void setLineDivergenceSmoothing(Smoothing2D lineDivergenceSmoothing) {
        this.lineDivergenceSmoothing = lineDivergenceSmoothing;
    }

    public Smoothing2D getLineDivergenceSmoothing() {
        return lineDivergenceSmoothing;
    }

    public void calculateLineDivergence(FingerprintImage image) {
        double[][] lineDivergence = Utils.getNaNMatrix(image.getWidth(), image.getHeight());
        image.setLineDivergenceMatrix(lineDivergence);

        for (PatchedData.Patch patch : image.getPatches()) {
            patch.initLineDivergence();

            calculateLineDivergencePatch(patch);
        }

        // transform to log10 scale if divergence is not too close to zero,
        if (lineDivergenceSmoothing != null) {
            double[][] smoothedLineDivergence = new double[image.getWidth()][image.getHeight()];

            for (PatchedData.Patch patch : image.getPatches()) {
                lineDivergenceSmoothing.smooth(patch, PatchedData.DataType.LINE_DIVERGENCE, smoothedLineDivergence);
            }

            if (transformLog10) {
                smoothedLineDivergence = Utils.log10(smoothedLineDivergence);
            }

            image.setLineDivergenceMatrix(smoothedLineDivergence);
        } else {
            if (transformLog10) {
                lineDivergence = Utils.log10(lineDivergence);
                image.setLineDivergenceMatrix(lineDivergence);
            }
        }
    }

    private void calculateLineDivergencePatch(PatchedData.Patch patch) {
        int padX = patch.getPadX();
        int padY = patch.getPadY();
        int fromX = patch.getFromX() - padX;
        int fromY = patch.getFromY() - padY;
        int toX = patch.getToX() + padX;
        int toY = patch.getToY() + padY;

        Gradient gradient = new Gradient(patch);
        double[][] ridgeFrequencyGradientX = gradient.getGradientX();
        double[][] ridgeFrequencyGradientY = gradient.getGradientY();

        for (int x = fromX; x < toX; x++) {
            for (int y = fromY; y < toY; y++) {
                if (!patch.isInRoi(x, y)) {
                    patch.set(PatchedData.DataType.LINE_DIVERGENCE, x, y, Double.NaN);
                    continue;
                }

                // Continuity adjustment. Apparently shifts the problem into regions where discontinuities just become less apparent.
                double val = ridgeFrequencyGradientX[x - fromX][y - fromY] * Math.cos(patch.get(PatchedData.DataType.ORIENTATION, x, y)) + ridgeFrequencyGradientY[x - fromX][y - fromY] * Math.sin(patch.get(PatchedData.DataType.ORIENTATION, x, y));
                patch.set(PatchedData.DataType.LINE_DIVERGENCE, x, y, val);
            }
        }
    }
}
