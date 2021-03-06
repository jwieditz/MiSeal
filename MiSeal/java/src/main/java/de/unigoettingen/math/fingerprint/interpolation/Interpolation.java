package de.unigoettingen.math.fingerprint.interpolation;

import de.unigoettingen.math.fingerprint.PatchedData;

public interface Interpolation {

    double interpolate(double x, double y, double[][] data);

    void interpolate(PatchedData.Patch patch, PatchedData.DataType type, double[][] target, boolean onlyValidValues);

    int getSizeX();

    int getSizeY();

}
