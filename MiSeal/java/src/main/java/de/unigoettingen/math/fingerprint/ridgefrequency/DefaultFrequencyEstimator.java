package de.unigoettingen.math.fingerprint.ridgefrequency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import de.unigoettingen.math.fingerprint.FingerprintImage;
import de.unigoettingen.math.fingerprint.PatchedData;
import de.unigoettingen.math.fingerprint.interpolation.GaussianInterpolation;
import de.unigoettingen.math.fingerprint.interpolation.Interpolation;
import de.unigoettingen.math.fingerprint.smoothing.GaussianSmoothing2D;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing2D;
import de.unigoettingen.math.fingerprint.utilities.Utils;

import static de.unigoettingen.math.fingerprint.PatchedData.DataType.ORIENTATION;
import static de.unigoettingen.math.fingerprint.PatchedData.DataType.RIDGE_FREQUENCY;

public class DefaultFrequencyEstimator implements FrequencyEstimator {

    // just some value for now - not proven whether it is good or bad, but at the moment it affects
    // only the nearest neighbour interpolation which doesn't change the values
    private static final int MAX_INTERPOLATION_ITERATIONS = 50;

    private final int windowWidth;
    private final int orientedWindowWidth;

    private Smoothing2D finalImageSmoothing = new GaussianSmoothing2D(25, 0, 25);
    private Interpolation frequencyInterpolation;

    public DefaultFrequencyEstimator(int windowWidth, int orientedWindowWidth) {
        if (windowWidth % 2 == 0) {
            throw new IllegalArgumentException("windowWidth must be odd");
        }
        if (orientedWindowWidth % 2 == 0) {
            throw new IllegalArgumentException("orientedWindowWidth must be odd");
        }

        this.windowWidth = windowWidth;
        this.orientedWindowWidth = orientedWindowWidth;

        frequencyInterpolation = new GaussianInterpolation(0, 9, 7, windowWidth - 1);
    }

    public void setFrequencyInterpolation(Interpolation interpolation) {
        if (interpolation != null) {
            frequencyInterpolation = interpolation;
        }
    }

    public Interpolation getFrequencyInterpolation() {
        return frequencyInterpolation;
    }

    public void setFinalImageSmoothing(Smoothing2D finalImageSmoothing) {
        this.finalImageSmoothing = finalImageSmoothing;
    }

    public Smoothing2D getFinalImageSmoothing() {
        return finalImageSmoothing;
    }

    @Override
    public void estimateFrequency(FingerprintImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        double[][] frequencies = Utils.getNaNMatrix(width, height);
        image.setRidgeFrequencyMatrix(frequencies);

        boolean hasWrongFrequency = false;

        for (PatchedData.Patch patch : image.getPatches()) {
            patch.initRidgeFrequency();

            boolean patchHasWrongFrequency = estimateFrequencyPatch(patch);
            if (patchHasWrongFrequency) {
                hasWrongFrequency = true;
            }
        }

        // interpolate wrong frequencies

        double[][] frequenciesInterpolated;

        if (hasWrongFrequency) {
            frequenciesInterpolated = Arrays.stream(frequencies).map(double[]::clone).toArray(double[][]::new);

            int iteration = 0;

            while (hasWrongFrequency && iteration < MAX_INTERPOLATION_ITERATIONS) {
                hasWrongFrequency = false;

                for (PatchedData.Patch patch : image.getPatches()) {
                    patch.initRidgeFrequency();

                    frequencyInterpolation.interpolate(patch, RIDGE_FREQUENCY, frequenciesInterpolated, true);

                    for (int x = patch.getFromX(); x < patch.getToX(); x++) {
                        for (int y = patch.getFromY(); y < patch.getToY(); y++) {
                            double frequency = frequenciesInterpolated[x][y];
                            if (frequency == -1) {
                                hasWrongFrequency = true;
                                break;
                            }
                        }
                    }
                }

                double[][] newFrequencies = Arrays.stream(frequenciesInterpolated).map(double[]::clone).toArray(double[][]::new);
                image.setRidgeFrequencyMatrix(newFrequencies);

                iteration++;
            }

            if (iteration == MAX_INTERPOLATION_ITERATIONS) {
                System.err.println("Reached maximum iterations (" + MAX_INTERPOLATION_ITERATIONS + ") while interpolating ridge frequencies in DefaultFrequencyEstimator.");
            }
        } else {
            frequenciesInterpolated = image.getRidgeFrequencyMatrix();
        }

        // optional (low-pass) filtering
        if (finalImageSmoothing != null) {
            // smooth the frequency image
            double[][] smoothedFrequencies = new double[width][height];

            for (PatchedData.Patch patch : image.getPatches()) {
                patch.initRidgeFrequency();

                finalImageSmoothing.smooth(patch, RIDGE_FREQUENCY, smoothedFrequencies);
            }

            image.setRidgeFrequencyMatrix(smoothedFrequencies);
        } else {
            image.setRidgeFrequencyMatrix(frequenciesInterpolated);
        }

        for (PatchedData.Patch patch : image.getPatches()) {
            patch.initRidgeFrequency();
        }
    }

    private boolean estimateFrequencyPatch(PatchedData.Patch patch) {
        int padX = patch.getPadX();
        int padY = patch.getPadY();
        int fromX = patch.getFromX() - padX;
        int fromY = patch.getFromY() - padY;
        int toX = patch.getToX() + padX;
        int toY = patch.getToY() + padY;

        boolean hasWrongFrequency = false;

        int halfWindowSize = (windowWidth - 1) / 2;
        int halfOrientatedWindowSize = (orientedWindowWidth - 1) / 2;

        // for every pixel (i, j)...
        for (int x = fromX; x < toX; x++) {
            for (int y = fromY; y < toY; y++) {
                if (!patch.isInRoi(x, y) || Double.isNaN(patch.get(ORIENTATION, x, y))) {
                    patch.set(RIDGE_FREQUENCY, x, y, Double.NaN);
                    continue;
                }

                // calculate the x-signature
                double[] xSignature = new double[orientedWindowWidth]; // for FFT the length needs to be a power of 2
                boolean hasNaNs = false;

                double orientation = patch.get(ORIENTATION, x, y);
                double sin = Math.sin(orientation);
                double cos = Math.cos(orientation);

                double s1 = x - halfWindowSize * cos - halfOrientatedWindowSize * sin;
                double s2 = y - halfWindowSize * sin + halfOrientatedWindowSize * cos;

                orientedWindowLoop:
                for (int k = 0; k < orientedWindowWidth; k++) {
                    double s3 = s1 + k * sin;
                    double s4 = s2 - k * cos;
                    for (int d = 0; d < windowWidth; d++) {
                        // a little unclear from the paper, what is meant: they seem to assume that
                        // u, v are always integers, but sometimes they become negative which leads
                        // to NaNs
                        int u = (int) Math.round(s3 + d * cos);
                        int v = (int) Math.round(s4 + d * sin);

                        double val = patch.getNormalized(u, v);
                        if (Double.isNaN(val)) {
                            hasNaNs = true;
                            break orientedWindowLoop;
                        }

                        xSignature[k] += val;
                    }
                    xSignature[k] /= windowWidth;
                }

                // check if the x-signature forms a discrete sinusoidal-shape wave
                double frequency = -1;

                if (!hasNaNs) {
                    List<Integer> peaks = new ArrayList<>();
                    int direction = 0;
                    for (int k = 1; k < xSignature.length; k++) {
                        int newDirection = 0;
                        if (xSignature[k] > xSignature[k - 1]) {
                            newDirection = 1;
                        } else if (xSignature[k] < xSignature[k - 1]) {
                            newDirection = -1;
                        }
                        if (newDirection == -1 && direction != -1) {
                            peaks.add(k);
                        }
                        direction = newDirection;
                    }
                    double meanDistance = IntStream.range(0, peaks.size() - 1).map(k -> peaks.get(k + 1) - peaks.get(k)).average().orElse(-1);
                    frequency = 1.0 / meanDistance;
                }

                // check if the frequency is in a given range which depends on the resolution of the image
                // for 500dpi the range is [1/25, 1/3]
                // TODO: make this variable
                if (frequency < 1.0 / 25 || frequency > 1.0 / 3) {
                    frequency = -1;
                }

                patch.set(RIDGE_FREQUENCY, x, y, frequency);

                if (frequency == -1) {
                    hasWrongFrequency = true;
                }
            }
        }

        return hasWrongFrequency;
    }
}
