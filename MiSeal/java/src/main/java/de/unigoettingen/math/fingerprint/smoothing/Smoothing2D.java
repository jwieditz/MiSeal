package de.unigoettingen.math.fingerprint.smoothing;

import de.unigoettingen.math.fingerprint.PatchedData;

public interface Smoothing2D {

    double smooth(int x, int y, double[][] data);

    void smooth(PatchedData.Patch patch, PatchedData.DataType type, double[][] target);

    int getSizeX();

    int getSizeY();
}
