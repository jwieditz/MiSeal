/**
 * Convolution.java
 * This class implements the discrete convolution operator
 */

package de.jwieditz.miseal.utilities.convolution;

import java.util.Arrays;

import de.jwieditz.miseal.PatchedData;

public class Convolution {

    private int[][] matrix;
    private double[][] matrix_d;

    public static double[][] CONVOLUTION_SOBEL_KERNEL_X = {{-0.125, -0.25, -0.125}, {0.0, 0.0, 0.0}, {0.125, 0.25, 0.125}};
    public static double[][] CONVOLUTION_SOBEL_KERNEL_Y = {{-0.125, 0.0, 0.125}, {-0.25, 0.0, 0.25}, {-0.125, 0, 0.125}};

    public static double[][] CONVOLUTION_SCHARR_KERNEL_X = {{-3.0, -10.0, -3.0}, {0.0, 0.0, 0.0}, {3.0, 10.0, 3.0}};
    public static double[][] CONVOLUTION_SCHARR_KERNEL_Y = {{-3.0, 0.0, 3.0}, {-10.0, 0.0, 10.0}, {-3.0, 0.0, 3.0}};

    // cf. for this filter the paper "Consistent Gradient Operators" from Ando Shigeru (2000)
    public static double[][] CONVOLUTION_OPTIMAL_4_X = {{-0.022116, -0.098381, -0.098381, -0.022116}, {-0.025526, -0.112984, -0.112984, -0.025526}, {0.025526, 0.112984, 0.112984, 0.025526}, {0.022116, 0.098381, 0.098381, 0.022116}};
    public static double[][] CONVOLUTION_OPTIMAL_4_Y = {{-0.022116, -0.025526, 0.025526, 0.022116}, {-0.098381, -0.112984, 0.112984, 0.098381}, {-0.098381, -0.112984, 0.112984, 0.098381}, {-0.022116, -0.025526, 0.025526, 0.022116}};

    public static double[][] CONVOLUTION_OPTIMAL_5_X = {{-0.003776, -0.026786, -0.046548, -0.026786, -0.003776}, {-0.010199, -0.070844, -0.122572, -0.070844, -0.010199}, {0.0, 0.0, 0.0, 0.0, 0.0}, {0.010199, 0.070844, 0.122572, 0.070844, 0.010199}, {0.003776, 0.026786, 0.046548, 0.026786, 0.003776}};
    public static double[][] CONVOLUTION_OPTIMAL_5_Y = {{-0.003776, -0.010199, 0.0, 0.010199, 0.003776}, {-0.026786, -0.070844, 0.0, 0.070844, 0.026786}, {-0.046548, -0.122572, 0.0, 0.122572, 0.046548}, {-0.026786, -0.070844, 0.0, 0.070844, 0.026786}, {-0.003776, -0.010199, 0.0, 0.010199, 0.003776}};

    public Convolution(int[][] matrix) {
        setMatrix(matrix);
    }

    public Convolution(double[][] matrix) {
        setMatrix(matrix);
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public void setMatrix(double[][] matrix) {
        this.matrix_d = matrix;
    }

    public int[][] getMatrix() {
        return this.matrix;
    }

    public double[][] getMatrix_d() {

        return this.matrix_d;
    }


    /**
     * Convolve matrix with the given kernel
     *
     * @param kernel kernel to convolve with the matrix
     * @return Convolution between kernel (*) matrix
     */
    public double[][] convolve(int[][] kernel) {

        int[][] m;
        m = getMatrix();
        int w = m.length;
        int h = m[0].length;
        int kw = kernel.length;
        int kh = kernel[0].length;
        double[][] convolution = new double[w][h];
        int i, j, ki, kj;
        double sum;
        int k_halfw = kw / 2;
        int k_halfh = kh / 2;

        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {

                sum = 0.0;
                outer:
                for (kj = -k_halfh; kj <= k_halfh; kj++) {
                    for (ki = -k_halfw; ki <= k_halfw; ki++) {

                        try {
                            sum += m[i + ki][j + kj] * kernel[ki + k_halfw][kj + k_halfh];
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            sum = Double.NaN;
                            break outer;
                        }
                    }
                }
                convolution[i][j] = sum;
            }
        }

        return convolution;
    }

    public double[][] convolve(double[][] kernel) {

        double[][] m;
        m = getMatrix_d();
        int w = m.length;
        int h = m[0].length;
        int kw = kernel.length;
        int kh = kernel[0].length;
        double[][] convolution = new double[w][h];
        int i, j, ki, kj;
        double sum;
        int k_halfw = kw / 2;
        int k_halfh = kh / 2;

        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {

                sum = 0.0;
                outer:
                for (kj = -k_halfh; kj <= k_halfh; kj++) {
                    for (ki = -k_halfw; ki <= k_halfw; ki++) {

                        try {
                            sum += m[i + ki][j + kj] * kernel[ki + k_halfw][kj + k_halfh];
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            sum = Double.NaN;
                            break outer;
                        }
                    }
                }
                convolution[i][j] = sum;
            }
        }

        return convolution;
    }

    public double[][] convolve_d(int[][] kernel) {
        double[][] m;
        m = getMatrix_d();
        int w = m.length;
        int h = m[0].length;
        int kw = kernel.length;
        int kh = kernel[0].length;
        double[][] convolution = new double[w][h];
        int i, j, ki, kj;
        double sum;
        int k_halfw = kw / 2;
        int k_halfh = kh / 2;

        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {

                sum = 0.0;
                outer:
                for (kj = -k_halfh; kj <= k_halfh; kj++) {
                    for (ki = -k_halfw; ki <= k_halfw; ki++) {

                        try {
                            sum += m[i + ki][j + kj] * kernel[ki + k_halfw][kj + k_halfh];
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            sum = Double.NaN;
                            break outer;
                        }
                    }
                }
                convolution[i][j] = sum;
            }
        }

        return convolution;
    }

    public static double[][] convolve(int[][] matrix, int[][] kernel) {

        return (new Convolution(matrix)).convolve(kernel);
    }

    public static double[][] convolve(double[][] matrix, int[][] kernel) {

        return (new Convolution(matrix)).convolve_d(kernel);
    }

    public static double[][] convolve(double[][] matrix, double[][] kernel) {

        return (new Convolution(matrix)).convolve(kernel);
    }

    public static double[][] convolvePatch(PatchedData.Patch patch, PatchedData.DataType type, double[][] kernel) {
        int padX = patch.getPadX();
        int padY = patch.getPadY();
        int fromX = patch.getFromX() - padX;
        int fromY = patch.getFromY() - padY;
        int toX = patch.getToX() + padX;
        int toY = patch.getToY() + padY;
        int w = toX - fromX;
        int h = toY - fromY;
        int kw = kernel.length;
        int kh = kernel[0].length;
        double[][] convolution = new double[w][h];
        Arrays.stream(convolution).forEach(a -> Arrays.fill(a, Double.NaN));
        int k_halfw = kw / 2;
        int k_halfh = kh / 2;

        for (int j = fromY; j < toY; j++) {
            for (int i = fromX; i < toX; i++) {

                double sum = 0;
                outer:
                for (int kj = -k_halfh; kj <= k_halfh; kj++) {
                    for (int ki = -k_halfw; ki <= k_halfw; ki++) {

                        try {
                            sum += patch.get(type, i + ki, j + kj) * kernel[ki + k_halfw][kj + k_halfh];
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            sum = Double.NaN;
                            break outer;
                        }
                    }
                }
                convolution[i - fromX][j - fromY] = sum;
            }
        }

        return convolution;
    }
}