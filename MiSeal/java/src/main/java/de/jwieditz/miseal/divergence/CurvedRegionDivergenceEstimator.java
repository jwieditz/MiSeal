package de.jwieditz.miseal.divergence;

import de.jwieditz.miseal.CurvedRegionHelper;
import de.jwieditz.miseal.ThresholdReachedException;
import de.jwieditz.miseal.interpolation.Interpolation;
import de.jwieditz.miseal.smoothing.AverageSmoothing;
import de.jwieditz.miseal.smoothing.Smoothing2D;
import de.jwieditz.miseal.FingerprintImage;
import de.jwieditz.miseal.Point;
import de.jwieditz.miseal.utilities.Utils;

//import GaussianSmoothing2D;

public class CurvedRegionDivergenceEstimator implements DivergenceEstimator {

    private static final double THRESHOLD_ANGLE = 10.0 * Math.PI / 180;
    private static final double THRESHOLD_MAXDIST = 8.0;

    private final int p;
    private final int q;

    private Smoothing2D divergenceSmoothing = new AverageSmoothing(1);

    private CurvedRegionHelper helper;

    private boolean useRealDistance = true;
    private boolean removeLineDivergence = true;
    private boolean transformLog10 = false;

    public CurvedRegionDivergenceEstimator(int p, int q) {
        this.p = p;
        this.q = q;

        helper = new CurvedRegionHelper(p, q);
    }

    public void setOrientationInterpolation(Interpolation orientationInterpolation) {
        helper.setOrientationInterpolation(orientationInterpolation);
    }

    public Interpolation getOrientationInterpolation() {
        return helper.getOrientationInterpolation();
    }

    public void setDivergenceSmoothing(Smoothing2D divergenceSmoothing) {
        this.divergenceSmoothing = divergenceSmoothing;
    }

    public Smoothing2D getDivergenceSmoothing() {
        return divergenceSmoothing;
    }

    public void setUseRealDistance(boolean useRealDistance) {
        this.useRealDistance = useRealDistance;
    }

    public void setRemoveLineDivergence(boolean removeLineDivergence) {
        this.removeLineDivergence = removeLineDivergence;
    }

    public void setTransformLog10(boolean transformLog10) {
        this.transformLog10 = transformLog10;
    }

    @Override
    public void calculateDivergence(FingerprintImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        boolean[][] roi = image.getROI();

        double[][] orientation = image.getOrientationMatrix();

        double[][] divergence = new double[w][h];

        int offset = Math.max(p, q) + 15;

        for (int x = 0; x < w; x++) {
            outerloop:
            for (int y = 0; y < h; y++) {
                if (!roi[x][y] || x < offset || x >= w - offset || y < offset || y >= h - offset) {
                    divergence[x][y] = Double.NaN;
                    continue;
                }

                Point[] vertical;
                Point[] leftCurve = new Point[2 * p + 1];
                Point[] rightCurve = new Point[2 * p + 1];
                Point[][] region = new Point[2 * p + 1][2 * q + 1];

                region[p] = helper.getCurve(x, y, true, orientation);

                try {
                    vertical = helper.getCurveWithThreshold(x, y, THRESHOLD_ANGLE, false, orientation);
                } catch (ThresholdReachedException e) {
                    divergence[x][y] = Double.NaN;
                    continue;
                }

                // if the vertical curve bends too much we assume a core or delta and skip the divergence estimation for this point because we cannot get any meaningful value there
                // TODO: think about interpolating/smoothing it away in the future

                try {
                    for (int i = 1; i <= p; i++) {
                        region[p + i] = helper.getCurveWithThreshold(vertical[p + i].getX(), vertical[p + i].getY(), THRESHOLD_ANGLE, true, orientation);
                        region[p - i] = helper.getCurveWithThreshold(vertical[p - i].getX(), vertical[p - i].getY(), THRESHOLD_ANGLE, true, orientation);
                    }
                } catch (ThresholdReachedException e) {
                    divergence[x][y] = Double.NaN;
                    continue;
                }

                // Compute the area of the curved region. To this end, count the number of pixels visited by the curves.
                boolean[][] visited = new boolean[w][h];
                int area = 0;

                for (int i = 0; i < 2 * p + 1; i++) {
                    for (int j = 0; j < 2 * q + 1; j++) {
                        int positionX = (int) region[i][j].getX();
                        int positionY = (int) region[i][j].getY();

                        if (!visited[positionX][positionY]) {
                            visited[positionX][positionY] = true;
                            area++;
                        }
                    }
                }


                // Extract the left and right border line from the curved region and check whether two consecutive points on either of the two lines drift too far apart from each other.
                // Notice that the border points might switch from right to left (or vice versa) as the curves in the curved region are not all oriented in the same way.

                Point left = region[0][0];
                Point right = region[0][2 * q];

                Point lastLeft;
                Point lastRight;

                double leftDist = 0;
                double rightDist = 0;

                leftCurve[0] = left;
                rightCurve[0] = right;

                for (int i = 1; i <= 2 * p; i++) {

                    // left border line
                    if (Math.min(dist(left, region[i][0]), dist(left, region[i][2 * q])) > THRESHOLD_MAXDIST) {
                        divergence[x][y] = Double.NaN;
                        continue outerloop;
                    } else {
                        lastLeft = left;
                        if (dist(left, region[i][0]) <= dist(left, region[i][2 * q])) {
                            left = region[i][0];
                        } else {
                            left = region[i][2 * q];
                        }
                        if (useRealDistance) {
                            leftDist += dist(lastLeft, left);
                        }
                        leftCurve[i] = left;
                    }

                    // right border line
                    if (Math.min(dist(right, region[i][0]), dist(right, region[i][2 * q])) > THRESHOLD_MAXDIST) {
                        divergence[x][y] = Double.NaN;
                        continue outerloop;
                    } else {
                        lastRight = right;
                        if (dist(right, region[i][2 * q]) <= dist(right, region[i][0])) {
                            right = region[i][2 * q];
                        } else {
                            right = region[i][0];
                        }
                        if (useRealDistance) {
                            rightDist += dist(lastRight, right);
                        }
                        rightCurve[i] = right;
                    }
                }

                if (!useRealDistance) {
                    leftDist = dist(leftCurve[0], leftCurve[2 * p]);
                    rightDist = dist(rightCurve[0], rightCurve[2 * p]);
                }

                if (removeLineDivergence) {
                    double[][] ridgeFrequency = image.getRidgeFrequencyMatrix();

                    if (ridgeFrequency == null) {
                        throw new UnsupportedOperationException("Ridge frequency must be computed first!");
                    }

                    double leftRidgeFrequencyIntegral = 0;
                    double rightRidgeFrequencyIntegral = 0;

                    for (int i = 0; i < 2 * p; i++) {
                        Point curLeft = leftCurve[i];
                        Point curRight = rightCurve[i];

                        if (removeLineDivergence) {
                            leftRidgeFrequencyIntegral += ridgeFrequency[(int) curLeft.getX()][(int) curLeft.getY()];
                            rightRidgeFrequencyIntegral += ridgeFrequency[(int) curRight.getX()][(int) curRight.getY()];
                        }
                    }

                    if (leftRidgeFrequencyIntegral == 0 || rightRidgeFrequencyIntegral == 0) {
                        divergence[x][y] = Double.NaN;
                        continue;
                    }

                    divergence[x][y] = (rightDist - leftDist) / area;
                } else {
                    divergence[x][y] = (rightDist - leftDist) / area;
                }
            }
        }

        if (divergenceSmoothing != null) {
            double[][] smoothedDivergence = new double[image.getWidth()][image.getHeight()];

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if (!roi[x][y]) {
                        smoothedDivergence[x][y] = Double.NaN;
                        continue;
                    }
                    smoothedDivergence[x][y] = divergenceSmoothing.smooth(x, y, divergence);
                }
            }

            if (transformLog10) {
                smoothedDivergence = Utils.log10(smoothedDivergence);
            }

            image.setDivergenceMatrix(smoothedDivergence);
        } else {
            if (transformLog10) {
                divergence = Utils.log10(divergence);
            }

            image.setDivergenceMatrix(divergence);
        }
    }

    private double dist(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }

}
