package de.unigoettingen.math.fingerprint.io;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.unigoettingen.math.fingerprint.FingerprintImage;

public class ImageToMatrixReader {

    public static FingerprintImage readImageToMatrix(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);

        Raster data = image.getData();
        int width = image.getWidth();
        int height = image.getHeight();

        double[][] matrix = new double[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                matrix[i][j] = data.getSampleDouble(i, j, 0);
            }
        }

        return new FingerprintImage(matrix);
    }
}
