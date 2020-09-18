package de.jwieditz.miseal.interpolation;

import de.jwieditz.miseal.PatchedData;

public class NearestNeighbourInterpolation implements Interpolation {

    @Override
    public double interpolate(double x, double y, double[][] data) {
        int xRounded = (int) Math.round(x);
        int yRounded = (int) Math.round(y);
        if (xRounded < 0 || xRounded >= data.length || yRounded < 0 || yRounded >= data[0].length) {
            return Double.NaN;
        }
        return data[xRounded][yRounded];
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

                int xRounded = Math.round(x);
                int yRounded = Math.round(y);

                target[x][y] = patch.get(type, xRounded, yRounded);
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
