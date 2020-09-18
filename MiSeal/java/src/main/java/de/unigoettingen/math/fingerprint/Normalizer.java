package de.unigoettingen.math.fingerprint;

import java.util.Arrays;

public class Normalizer {

    public void normalize(FingerprintImage image, double mean, double variance) {
        double[][] imageData = image.getImageDataMatrix();
        double originalMean = image.getMean();
        double originalVariance = image.getVariance();

        double[][] newImageData = Arrays.stream(imageData).map(double[]::clone).toArray(double[][]::new);

        double factor = Math.sqrt(variance / originalVariance);

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                double pixelValue = imageData[i][j];
                if (pixelValue > originalMean) {
                    newImageData[i][j] = mean + factor * Math.abs(imageData[i][j] - originalMean);
                } else {
                    newImageData[i][j] = mean - factor * Math.abs(imageData[i][j] - originalMean);
                }
            }
        }

        image.setNormalizedImageMatrix(newImageData);
    }

}
