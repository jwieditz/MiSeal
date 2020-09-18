package de.unigoettingen.math.fingerprint.ridgefrequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.unigoettingen.math.fingerprint.CurvedRegionHelper;
import de.unigoettingen.math.fingerprint.FingerprintAnalysis;
import de.unigoettingen.math.fingerprint.FingerprintImage;
import de.unigoettingen.math.fingerprint.Point;
import de.unigoettingen.math.fingerprint.display.old.CurvedRegionImageDisplay;
import de.unigoettingen.math.fingerprint.interpolation.Interpolation;
import de.unigoettingen.math.fingerprint.interpolation.NearestNeighbourInterpolation;
import de.unigoettingen.math.fingerprint.smoothing.AverageSmoothing;
import de.unigoettingen.math.fingerprint.smoothing.GaussianSmoothing;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing1D;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing2D;
import de.unigoettingen.math.fingerprint.utilities.Utils;

public class CurvedRegionFrequencyEstimator implements FrequencyEstimator {

    // amount of curves
    private final int p;

    // amount of points per curve
    private final int q;

    private Interpolation greyValueInterpolation = new NearestNeighbourInterpolation();

    private Smoothing2D finalImageSmoothing = new AverageSmoothing(49);
    private Smoothing1D profileSmoothing = new GaussianSmoothing(7, 0, 1);

    private CurvedRegionHelper helper;

    private int maxProfileSmoothingIterations = 3;
    private boolean transformLog10 = false;

    public CurvedRegionFrequencyEstimator(int p, int q) {
        this.p = p;
        this.q = q;

        helper = new CurvedRegionHelper(p, q);
    }

    public void setOrientationInterpolation(Interpolation orientationInterpolation) {
        helper.setOrientationInterpolation(orientationInterpolation);
    }

    public Interpolation getOrientationInterpolation(){
        return helper.getOrientationInterpolation();
    }

    public void setGreyValueInterpolation(Interpolation interpolation) {
        if (interpolation != null) {
            greyValueInterpolation = interpolation;
        }
    }

    public Interpolation getGreyValueInterpolation() {
        return greyValueInterpolation;
    }

    public void setFinalImageSmoothing(Smoothing2D smoothing) {
        finalImageSmoothing = smoothing;
    }

    public Smoothing2D getFinalImageSmoothing() {
        return finalImageSmoothing;
    }

    public void setProfileSmoothing(Smoothing1D smoothing, int maxIterations) {
        if (smoothing != null) {
            profileSmoothing = smoothing;
        }
        if (maxIterations >= 0) {
            maxProfileSmoothingIterations = maxIterations;
        }
    }

    public void setTransformLog10(boolean transformLog10) {
        this.transformLog10 = transformLog10;
    }

    @Override
    public void estimateFrequency(FingerprintImage image) {
        double[][] imageData = image.getNormalizedImageMatrix();
        double[][] orientationMatrix = image.getOrientationMatrix();

        double[][] frequencies = new double[image.getWidth()][image.getHeight()];

        boolean[][] roi = image.getROI();

        // TODO: adjust the offset; can probably be smaller
        int offset = Math.max(p, q) + 15;

        // TODO: stop when a core point is detected, i.e. the difference between two consecutive
        // TODO: curve points is larger than a threshold

        // start outside the offset so that we can set the values outside to NaN
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (!roi[x][y] || x < offset || x >= image.getWidth() - offset || y < offset || y >= image.getHeight() - offset) {
                    frequencies[x][y] = Double.NaN;
                    continue;
                }

                // first compute the curved region at (x, y)

                Point[][] region = new Point[2 * p + 1][2 * q + 1];

                Point[] vertical = helper.getCurve(x, y, false, orientationMatrix);

                region[p] = helper.getCurve(x, y, true, orientationMatrix);

                for (int i = 1; i <= p; i++) {
                    region[p + i] = helper.getCurve(vertical[p + i].getX(), vertical[p + i].getY(), true, orientationMatrix);
                    region[p - i] = helper.getCurve(vertical[p - i].getX(), vertical[p - i].getY(), true, orientationMatrix);
                }

                // this does not belong to the algorithm, it is just here to display and give an
                // idea of the curved region at the centre of the image
                //                int watchX = image.getWidth() / 2;
                //                int watchY = image.getHeight() / 2;

                int watchX = 120;
                int watchY = 362;

                if (x == watchX && y == watchY && !FingerprintAnalysis.ARGS.noGui) {
                     new CurvedRegionImageDisplay(imageData, region).display("curved region at (" + watchX + ", " + watchY + ")");
                }

                // TODO: the paper introduces another optional step here, curvature estimation.
                // TODO: since it is not necessary for the ridge frequency estimation, it is not
                // TODO: implemented yet

                // compute the grey-level profile

                double[] profile = new double[2 * p + 1];

                double frequency;

                boolean recalculateProfile;

                int profileSmoothingIterations = 0;

                do {
                    // assume first, that the recalculation of the profile is not necessary
                    recalculateProfile = false;

                    for (int i = -p; i < p; i++) {
                        Point[] curve = region[p + i];
                        for (Point point : curve) {
                            profile[p + i] += greyValueInterpolation.interpolate(point.getX(), point.getY(), imageData);
                        }
                        profile[p + 1] /= (2 * p + 1);
                    }

                    List<Integer> distances = new ArrayList<>();

                    int lastMax = -1;
                    int lastMin = -1;

                    int maxs = 0;
                    int mins = 0;

                    int direction = 0;
                    for (int i = 1; i < profile.length; i++) {
                        int newDirection = 0;
                        if (profile[i] > profile[i - 1]) {
                            newDirection = 1;
                        } else if (profile[i] < profile[i - 1]) {
                            newDirection = -1;
                        }
                        if (newDirection == -1 && direction != -1) {
                            if (lastMax != -1) {
                                distances.add(i - lastMax);
                            }
                            lastMax = i;
                            maxs++;
                        } else if (newDirection == 1 && direction != 1) {
                            if (lastMin != -1) {
                                distances.add(i - lastMin);
                            }
                            lastMin = i;
                            mins++;
                        }
                        direction = newDirection;
                    }

                    Collections.sort(distances);

                    double medianDistance;

                    if (distances.isEmpty()) {
                        medianDistance = 0;
                    } else if (distances.size() == 1) {
                        medianDistance = distances.get(0);
                    } else {
                        int middle = distances.size() / 2; // integer division here

                        if (distances.size() % 2 == 0) {
                            medianDistance = (double) (distances.get(middle - 1) + distances.get(middle)) / 2;
                        } else {
                            medianDistance = distances.get(middle);
                        }
                    }

                    frequency = 1.0 / medianDistance;

                    if (frequency > 1.0 / 3 || frequency < 1.0 / 25 || mins < 2 || maxs < 2) {
                        frequency = Double.NaN;
                    } else if (profileSmoothing != null) {
                        // distances list is already sorted
                        int largestDistance = distances.get(distances.size() - 1);
                        int smallestDistance = distances.get(0);

                        double pMaxMin = (double) largestDistance / (double) smallestDistance;

                        if (pMaxMin > 1.5) {
                            frequency = 0;

                            double[] newProfile = new double[profile.length];

                            for (int i = 0; i < profile.length; i++) {
                                newProfile[i] = profileSmoothing.smooth(i, profile);
                            }

                            profile = newProfile;

                            recalculateProfile = profileSmoothingIterations < maxProfileSmoothingIterations;

                            profileSmoothingIterations++;
                        }
                    }
                } while (recalculateProfile);

                frequencies[x][y] = frequency;
            }
        }

        if (finalImageSmoothing != null) {
            // smooth the frequency image
            double[][] smoothedFrequencies = new double[image.getWidth()][image.getHeight()];

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if (!roi[x][y]) {
                        smoothedFrequencies[x][y] = Double.NaN;
                        continue;
                    }
                    smoothedFrequencies[x][y] = finalImageSmoothing.smooth(x, y, frequencies);
                }
            }

            if (transformLog10) {
                smoothedFrequencies = Utils.log10(smoothedFrequencies);
            }

            image.setRidgeFrequencyMatrix(smoothedFrequencies);
        } else {
            if (transformLog10) {
                frequencies = Utils.log10(frequencies);
            }

            image.setRidgeFrequencyMatrix(frequencies);
        }
    }
}
