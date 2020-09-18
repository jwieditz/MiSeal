package de.jwieditz.miseal;

import de.jwieditz.miseal.interpolation.Interpolation;
import de.jwieditz.miseal.interpolation.NearestNeighbourInterpolation;

public class CurvedRegionHelper {

    private int p;
    private int q;
    private Interpolation orientationInterpolation = new NearestNeighbourInterpolation();

    public CurvedRegionHelper(int p, int q) {
        this.p = p;
        this.q = q;
    }

    public void setOrientationInterpolation(Interpolation orientationInterpolation) {
        if (orientationInterpolation != null) {
            this.orientationInterpolation = orientationInterpolation;
        }
    }

    public Interpolation getOrientationInterpolation() {
        return orientationInterpolation;
    }

    public Point[] getCurve(double x, double y, boolean horizontal, double[][] orientationMatrix) {
        try {
            return getCurveWithThreshold(x, y, Double.POSITIVE_INFINITY, horizontal, orientationMatrix);
        } catch (ThresholdReachedException ignored) {
            // this case can't occur because Double.POSITIVE_INFINITY > everything
            return null;
        }
    }

    public Point[] getCurveWithThreshold(double x, double y, double threshold, boolean horizontal, double[][] orientationMatrix) throws ThresholdReachedException {
        int dim = horizontal ? q : p;

        Point[] curve = new Point[2 * dim + 1];
        curve[dim] = new Point(x, y);

        double lastOrientationPos = orientationInterpolation.interpolate(x, y, orientationMatrix);
        double lastOrientationNeg = orientationInterpolation.interpolate(x, y, orientationMatrix);

        for (int i = 1; i <= dim; i++) {
            double lastXPos = curve[dim + (i - 1)].getX();
            double lastYPos = curve[dim + (i - 1)].getY();

            double lastXNeg = curve[dim - (i - 1)].getX();
            double lastYNeg = curve[dim - (i - 1)].getY();

            double orientationPos = orientationInterpolation.interpolate(lastXPos, lastYPos, orientationMatrix);
            double orientationNeg = orientationInterpolation.interpolate(lastXNeg, lastYNeg, orientationMatrix);

            if (lastOrientationPos - orientationPos > Math.PI / 2) {
                orientationPos += Math.PI;
            } else if (orientationPos - lastOrientationPos > Math.PI / 2) {
                orientationPos -= Math.PI;
            }

            if (lastOrientationNeg - orientationNeg > Math.PI / 2) {
                orientationNeg += Math.PI;
            } else if (orientationNeg - lastOrientationNeg > Math.PI / 2) {
                orientationNeg -= Math.PI;
            }

            if (Math.abs(lastOrientationPos - orientationPos) > threshold) {
                throw new ThresholdReachedException("threshold reached: " + Math.abs(lastOrientationPos - orientationPos) + " > " + threshold);
            } else if(Math.abs(lastOrientationNeg - orientationNeg) > threshold) {
                throw new ThresholdReachedException("threshold reached: " + Math.abs(lastOrientationNeg - orientationNeg) + " > " + threshold);
            }

            double xPos, yPos, xNeg, yNeg;

            if (horizontal) {
                xPos = lastXPos + Math.cos(orientationPos);
                yPos = lastYPos + Math.sin(orientationPos);

                xNeg = lastXNeg + Math.cos(orientationNeg + Math.PI);
                yNeg = lastYNeg + Math.sin(orientationNeg + Math.PI);
            } else {
                xPos = lastXPos + Math.cos(orientationPos + Math.PI / 2);
                yPos = lastYPos + Math.sin(orientationPos + Math.PI / 2);

                xNeg = lastXNeg - Math.cos(orientationNeg + Math.PI / 2);
                yNeg = lastYNeg - Math.sin(orientationNeg + Math.PI / 2);
            }

            lastOrientationPos = orientationPos;
            lastOrientationNeg = orientationNeg;

            curve[dim + i] = new Point(xPos, yPos);
            curve[dim - i] = new Point(xNeg, yNeg);
        }

        return curve;
    }
}
