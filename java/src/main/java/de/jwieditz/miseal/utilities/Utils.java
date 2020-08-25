package de.jwieditz.miseal.utilities;

import java.util.Arrays;

public class Utils {

    private Utils() {}

    public static double[][] log10(double[][] data) {
        if (data == null) {
            return data;
        }

        return Arrays.stream(data)
                .map(a -> Arrays.stream(a).map(Utils::log10WithCheck).toArray())
                .toArray(double[][]::new);
    }

    public static double log10WithCheck(double d) {
        return Math.abs(d) > 1e-16 ? Math.log10(Math.abs(d)) : Double.NaN;
    }

    public static double[][] getNaNMatrix(int width, int height) {
        double[][] matrix = new double[width][height];
        Arrays.stream(matrix).forEach(a -> Arrays.fill(a, Double.NaN));
        return matrix;
    }
}
