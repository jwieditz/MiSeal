package de.unigoettingen.math.fingerprint.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class MatrixWriter {

    public static void writeMatrixToFile(double[][] matrix, File file) throws IOException {
        if (file.exists() && file.isDirectory()) {
            throw new IOException("Expected a file, got a directory: " + file);
        }
        try (PrintWriter writer = new PrintWriter(file)) {
            for (double[] row : matrix) {
                StringBuilder sb = new StringBuilder();
                Arrays.stream(row).forEach(d -> sb.append(d).append(','));
                sb.deleteCharAt(sb.length() - 1);
                writer.append(sb.toString()).append('\n');
            }
            writer.flush();
        }
    }
}
