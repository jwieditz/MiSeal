package de.unigoettingen.math.fingerprint.io;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

public class OrientationReader {

    public static double[][] readOrientationFromImage(File file) throws IOException {

        if( !file.exists() ) { throw new IOException("File does not exist!"); }
        if( !file.canRead() ) { throw new IOException("Cannot read file!"); }

        BufferedImage orientationImage = ImageIO.read(file);
        int w = orientationImage.getWidth();
        int h = orientationImage.getHeight();
        double[][] orientation = new double[w][h];

        Raster raster = orientationImage.getRaster();

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int sample = raster.getSample(i, j, 0);
                if (sample != 255) {
                    orientation[i][j] = sample * Math.PI / 180;
                    if (sample > 90) {
                        orientation[i][j] -= Math.PI;
                    }
                } else {
                    orientation[i][j] = Double.NaN;
                }
            }
        }
        return orientation;
    }
}
