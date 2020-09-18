package de.unigoettingen.math.fingerprint.interpolation;

import de.unigoettingen.math.fingerprint.PatchedData;

public class BilinearInterpolation implements Interpolation{

    @Override
    public double interpolate(double x, double y, double[][] data) {
        int x1 = (int) Math.floor(x);
        int y1 = (int) Math.floor(y);
        int x2 = (int) Math.ceil(x);
        int y2 = (int) Math.ceil(y);
        if (x1 < 0 || x1 >= data.length || x2 < 0 || x2 >= data.length || y1 < 0 || y1 >= data[0].length || y2 < 0 || y2 >= data[0].length) {
            return Double.NaN;
        }
        double fR1 = (x2 - x) / (x2 - x1) * data[x1][y1] + (x - x1) / (x2 - x1) * data[x2][y1];
        double fR2 = (x2 - x) / (x2 - x1) * data[x1][y2] + (x - x1) / (x2 - x1) * data[x2][y2];
        return (y2 - y) / (y2 - y1) * fR1 + (y - y1) / (y2 - y1) * fR2;
    }

    @Override
    public void interpolate(PatchedData.Patch patch, PatchedData.DataType type, double[][] target, boolean onlyValidValues) {
        if (patch == null) {
            return;
        }

        int fromX = patch.getFromX();
        int fromY = patch.getFromY();
        int toX = patch.getToX();
        int toY = patch.getToY();

        for (int x = fromX; x < toX; x++) {
            for (int y = fromY; y < toY; y++) {
                if (!patch.isInRoi(x, y)) {
                    target[x][y] = Double.NaN;
                    continue;
                }

                if (onlyValidValues && patch.get(type, x, y) != -1 && !Double.isNaN(patch.get(type, x, y))) {
                    continue;
                }

                int x1 = (int) Math.floor(x);
                int y1 = (int) Math.floor(y);
                int x2 = (int) Math.ceil(x);
                int y2 = (int) Math.ceil(y);

                double fR1 = (x2 - x) / (x2 - x1) * patch.get(type, x1, y1) + (x - x1) / (x2 - x1) * patch.get(type, x2, y1);
                double fR2 = (x2 - x) / (x2 - x1) * patch.get(type, x1, y2) + (x - x1) / (x2 - x1) * patch.get(type, x2, y2);
                target[x][y] = (y2 - y) / (y2 - y1) * fR1 + (y - y1) / (y2 - y1) * fR2;
            }
        }
    }

    @Override
    public int getSizeX() {
        return 1;
    }

    @Override
    public int getSizeY() {
        return 1;
    }

}
