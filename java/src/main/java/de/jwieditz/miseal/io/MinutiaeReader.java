package de.jwieditz.miseal.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import de.jwieditz.miseal.Minutia;

public class MinutiaeReader {

    public static Minutia[] readMinutiae(File file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            // we can skip the first three lines
            reader.readLine();
            reader.readLine();
            reader.readLine();

            int nMinutiae = Integer.parseInt(reader.readLine());

            Minutia[] minutiae = new Minutia[nMinutiae];

            for (int i = 0; i < nMinutiae; i++) {
                String line = reader.readLine();
                String[] split = line.split(" ");
                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);
                double orientation = Double.parseDouble(split[2]);
                Minutia minutia = new Minutia(x, y, orientation);
                minutiae[i] = minutia;
            }

            return minutiae;
        }
    }
}
