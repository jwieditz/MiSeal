package de.jwieditz.miseal;

import de.jwieditz.miseal.utilities.convolution.Convolution;

public class ErrorBound {

    private double[][] ErrorX;
    private double[][] ErrorY;

    public double[][] getErrorX() {
        return ErrorX;
    }

    public void setErrorX(double[][] errorX) {
        ErrorX = errorX;
    }

    public double[][] getErrorY() {
        return ErrorY;
    }

    public void setErrorY(double[][] errorY) {
        ErrorY = errorY;
    }

    public ErrorBound(){

    }

    public void calculateBound(FingerprintImage image) {

        int w = image.getWidth();
        int h = image.getHeight();

        boolean[][] roi = image.getROI();
        double[][] ridgeFrequency = image.getRidgeFrequencyMatrix();
        double[][] orientation = image.getOrientationMatrix();

        double[][] gradientPhiX = Convolution.convolve(ridgeFrequency, Convolution.CONVOLUTION_OPTIMAL_5_X);
        double[][] gradientPhiY = Convolution.convolve(ridgeFrequency, Convolution.CONVOLUTION_OPTIMAL_5_Y);

        double[][] jacobiDxFx = new double[w][h];
        double[][] jacobiDyFx = new double[w][h];
        double[][] jacobiDxFy = new double[w][h];
        double[][] jacobiDyFy = new double[w][h];

        double[][] errorX = new double[w][h];
        double[][] errorY = new double[w][h];

        for (int x = 0; x < w; x++){
            for (int y = 0; y < h; y++){
                if( !roi[x][y] || Double.isNaN(orientation[x][y])){
                    continue;
                }
                jacobiDxFx[x][y] = (Math.cos(orientation[x+1][y]) - Math.cos(orientation[x-1][y])) / 2.0;
                jacobiDxFy[x][y] = (Math.sin(orientation[x+1][y]) - Math.sin(orientation[x-1][y])) / 2.0;
                jacobiDyFx[x][y] = (Math.cos(orientation[x][y+1]) - Math.cos(orientation[x][y-1])) / 2.0;
                jacobiDyFy[x][y] = (Math.sin(orientation[x][y+1]) - Math.sin(orientation[x][y-1])) / 2.0;
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (!roi[x][y] || Double.isNaN(ridgeFrequency[x][y])) {
                    continue;
                }

                errorX[x][y] = -gradientPhiX[x][y] * jacobiDyFy[x][y] + gradientPhiY[x][y] * jacobiDxFy[x][y];
                errorY[x][y] = gradientPhiX[x][y] * jacobiDyFx[x][y] - gradientPhiY[x][y] * jacobiDxFx[x][y];
            }
        }
        setErrorX(errorX);
        setErrorY(errorY);
    }
}
