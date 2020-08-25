package de.jwieditz.miseal;

import de.jwieditz.miseal.utilities.Utilities;

public class FingerprintImage implements Cloneable {

    private final int width;
    private final int height;

    private String name;

    private Minutia[] minutiae;
    private boolean[][] roi;
    private double[][] ridgeFrequencyGradientX;
    private double[][] ridgeFrequencyGradientY;
    private double[][] singularities;

    private PatchedData patchedData;

    private final double[][] imageDataMatrix;
    private double[][] normalizedImageMatrix;
    private double[][] orientationMatrix;
    private double[][] ridgeFrequencyMatrix;
    private double[][] divergenceMatrix;
    private double[][] intensityMatrix;
    private double[][] lineDivergenceMatrix;
    private double[][] necessaryMinutiae;

    public FingerprintImage(double[][] imageDataMatrix) {
        this.imageDataMatrix = imageDataMatrix;
        this.normalizedImageMatrix = imageDataMatrix;
        width = imageDataMatrix.length;
        height = imageDataMatrix[0].length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean[][] getROI() {
        return roi;
    }

    public void setROI(boolean[][] roi) {
        this.roi = roi;
    }

    public double[][] getImageDataMatrix() {
        return imageDataMatrix;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getMean() {
        double mean = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                mean += imageDataMatrix[i][j];
            }
        }
        return mean / (width * height);
    }

    public double getVariance() {
        double mean = getMean();
        double variance = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                variance += ((imageDataMatrix[i][j] - mean) * (imageDataMatrix[i][j] - mean));
            }
        }
        return variance / ((long) (width * height) * (width * height));
    }

    public Minutia[] getMinutiae() {
        return minutiae;
    }

    public void setMinutiae(Minutia[] minutiae) {
        this.minutiae = minutiae;
    }

    public double[][] getNormalizedImageMatrix() {
        return normalizedImageMatrix;
    }

    public void setNormalizedImageMatrix(double[][] image) {
        checkDimensions(image);
        this.normalizedImageMatrix = image;
    }

    public double[][] getOrientationMatrix() {
        return orientationMatrix;
    }

    public void setOrientationMatrix(double[][] orientationMatrix) {
        checkDimensions(orientationMatrix);
        this.orientationMatrix = orientationMatrix;
    }

    public double[][] getRidgeFrequencyMatrix() {
        return ridgeFrequencyMatrix;
    }

    public double[][] getInterridgeDistanceMatrix() {
        return Utilities.elementwiseInverse(ridgeFrequencyMatrix);
    }

    public void setRidgeFrequencyMatrix(double[][] ridgeFrequencyMatrix) {
        checkDimensions(ridgeFrequencyMatrix);
        this.ridgeFrequencyMatrix = ridgeFrequencyMatrix;
    }

    public double[][] getDivergenceMatrix() {
        return divergenceMatrix;
    }

    public void setDivergenceMatrix(double[][] divergenceMatrix) {
        checkDimensions(divergenceMatrix);
        this.divergenceMatrix = divergenceMatrix;
    }

    public double[][] getIntensityMatrix() {
        return intensityMatrix;
    }

    public void setIntensityMatrix(double[][] intensityMatrix) {
        checkDimensions(intensityMatrix);
        this.intensityMatrix = intensityMatrix;
    }

    public double[][] getLineDivergenceMatrix() {
        return lineDivergenceMatrix;
    }

    public void setLineDivergenceMatrix(double[][] lineDivergenceMatrix) {
        checkDimensions(lineDivergenceMatrix);
        this.lineDivergenceMatrix = lineDivergenceMatrix;
    }

    public double[][] getRidgeFrequencyGradientX() {
        return ridgeFrequencyGradientX;
    }

    public void setRidgeFrequencyGradientX(double[][] ridgeFrequencyGradientX) {
        checkDimensions(ridgeFrequencyGradientX);
        this.ridgeFrequencyGradientX = ridgeFrequencyGradientX;
    }

    public double[][] getRidgeFrequencyGradientY() {
        return ridgeFrequencyGradientY;
    }

    public void setRidgeFrequencyGradientY(double[][] ridgeFrequencyGradientY) {
        checkDimensions(ridgeFrequencyMatrix);
        this.ridgeFrequencyGradientY = ridgeFrequencyGradientY;
    }

    public double[][] getSingularities() {
        return singularities;
    }

    public void setSingularities(double[][] singularities) {
        checkDimensions(singularities);
        this.singularities = singularities;
    }

    public double[][] getNecessaryMinutiae() {
        return necessaryMinutiae;
    }

    public void setNecessaryMinutiae(double[][] necessaryMinutiae) {
        this.necessaryMinutiae = necessaryMinutiae;
    }

    public PatchedData getPatches() {
        return patchedData;
    }

    public void createPatches(int patchNumHorizontal, int patchNumVertical, int padX, int padY) {
        patchedData = PatchedData.create(this, patchNumHorizontal, patchNumVertical, padX, padY);
    }

    public void createPatchesForSize(int patchWidth, int patchHeight, int padX, int padY) {
        patchedData = PatchedData.createForSize(this, patchWidth, patchHeight, padX, padY);
    }

    private void checkDimensions(double[][] matrix) {
        if (matrix == null || matrix.length != width || matrix[0].length != height) {
            throw new IllegalArgumentException("dimension must be the same as of imageData");
        }
    }

}
