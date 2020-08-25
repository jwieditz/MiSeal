package de.jwieditz.miseal.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ROIReader {

    public static boolean[][] readROI(File file) throws IOException {
        BufferedReader reader = Files.newBufferedReader(file.toPath());
        String line;
        List<boolean[]> lines = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            String[] lineSplit = line.split(",");
            boolean[] booleans = new boolean[lineSplit.length];
            for (int i = 0; i < lineSplit.length; i++) {
                if (lineSplit[i] != null && (lineSplit[i].equalsIgnoreCase("true") || lineSplit[i].equalsIgnoreCase("0"))) {
                    booleans[i] = true;
                }
            }
            lines.add(booleans);
        }
        boolean[][] roi = lines.toArray(new boolean[0][]);
        boolean[][] transposedRoi = new boolean[roi[0].length][roi.length];
        for (int i = 0; i < roi.length; i++) {
            for (int j = 0; j < roi[0].length; j++) {
                transposedRoi[j][i] = roi[i][j];
            }
        }
        return transposedRoi;
    }
}
