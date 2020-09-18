package de.jwieditz.miseal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class PatchedData implements Iterable<PatchedData.Patch> {

    private final FingerprintImage image;
    private final int padX;
    private final int padY;
    private boolean[] singularity;
    private int patchWidth;
    private int patchHeight;
    private int patchNumHorizontal;
    private int patchNumVertical;
    private List<Patch> patches;

    private PatchedData(FingerprintImage image, int padX, int padY) {
        if (image == null) {
            throw new NullPointerException("image == null");
        }

        this.image = image;
        this.padX = padX;
        this.padY = padY;
    }

    public static PatchedData create(FingerprintImage image, int patchNumHorizontal, int patchNumVertical, int padX, int padY) {
        PatchedData patchedData = new PatchedData(image, padX, padY);
        patchedData.patchWidth = (int) Math.ceil((float) image.getWidth() / patchNumHorizontal);
        patchedData.patchHeight = (int) Math.ceil((float) image.getHeight() / patchNumVertical);

        patchedData.createPatchesForSize(image.getSingularities());

        return patchedData;
    }

    public static PatchedData createForSize(FingerprintImage image, int patchWidth, int patchHeight, int padX, int padY) {
        PatchedData patchedData = new PatchedData(image, padX, padY);
        patchedData.patchWidth = patchWidth;
        patchedData.patchHeight = patchHeight;

        patchedData.createPatchesForSize(image.getSingularities());

        return patchedData;
    }

    @Override
    public Iterator<Patch> iterator() {
        return patches.iterator();
    }

    @Override
    public void forEach(Consumer<? super Patch> action) {
        patches.forEach(action);
    }

    @Override
    public Spliterator<Patch> spliterator() {
        return patches.spliterator();
    }

    private void createPatchesForSize(double[][] singularities) {
        if (patchWidth <= 0 || patchHeight <= 0) {
            throw new IllegalArgumentException("patch dimensions must be strictly positive");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (patchWidth > width) {
            throw new IllegalArgumentException("patchWidth is greater than data width");
        }

        if (patchHeight > height) {
            throw new IllegalArgumentException("patchHeight is greater than data height");
        }

        patches = new ArrayList<>();

        this.patchNumHorizontal = (int) Math.ceil((float) width / patchWidth);
        this.patchNumVertical = (int) Math.ceil((float) height / patchHeight);

        singularity = new boolean[patchNumHorizontal * patchNumVertical];

        int patchIndex = 0;

        for (int x = 0; x < width; x += patchWidth) {
            for (int y = 0; y < height; y += patchHeight) {
                int toX = Math.min(width, x + patchWidth);
                int toY = Math.min(height, y + patchHeight);

                boolean containsSingularity = false;

                outer:
                for (int i = x; i < toX; i++) {
                    for (int j = y; j < toY; j++) {
                        if (!Double.isNaN(singularities[i][j]) && singularities[i][j] != 0) {
                            containsSingularity = true;
                            break outer;
                        }
                    }
                }

                if (containsSingularity) {
                    singularity[patchIndex] = true;
                }

                Patch patch = new Patch(x, y, toX, toY, patchIndex);
                patches.add(patch);

                patchIndex++;
            }
        }
    }

    private int getPatchIndex(int x, int y) {
        return (x / patchWidth) * patchNumVertical + (y / patchHeight);
    }

    public class Patch {

        private final int fromX;
        private final int fromY;
        private final int toX;
        private final int toY;
        private final int index;
        private double[][] orientation;
        private double[][] ridgeFrequency;
        private double[][] divergence;
        private double[][] lineDivergence;

        public Patch(int fromX, int fromY, int toX, int toY, int index) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            this.index = index;
        }

        public int getPadX() {
            return padX;
        }

        public int getPadY() {
            return padY;
        }

        public int getFromX() {
            return fromX;
        }

        public int getFromY() {
            return fromY;
        }

        public int getToX() {
            return toX;
        }

        public int getToY() {
            return toY;
        }

        public void initOrientationPadding() {
            orientation = initPaddingMatrix(image.getOrientationMatrix());
        }

        public void initRidgeFrequency() {
            ridgeFrequency = initPaddingMatrix(image.getRidgeFrequencyMatrix());
        }

        public void initDivergencePadding() {
            divergence = initPaddingMatrix(image.getDivergenceMatrix());
        }

        public void initLineDivergence() {
            lineDivergence = initPaddingMatrix(image.getLineDivergenceMatrix());
        }

        public double get(DataType type, int x, int y) {
            if (type == null) {
                return Double.NaN;
            }

            switch (type) {
                case ORIENTATION:
                    return checkAndGet(x, y, orientation);

                case RIDGE_FREQUENCY:
                    return checkAndGet(x, y, ridgeFrequency);

                case DIVERGENCE:
                    return checkAndGet(x, y, divergence);

                case LINE_DIVERGENCE:
                    return checkAndGet(x, y, lineDivergence);
            }
            return Double.NaN;
        }

        public double getIntensity(int x, int y) {
            if (!isInImage(x, y) || singularity[getPatchIndex(x, y)]) {
                return Double.NaN;
            }
            return image.getIntensityMatrix()[x][y];
        }

        public double getNecessaryMinutiae(int x, int y) {
            if (!isInImage(x, y) || singularity[getPatchIndex(x, y)]) {
                return Double.NaN;
            }
            return image.getNecessaryMinutiae()[x][y];
        }

        public double getNormalized(int x, int y) {
            if (!isInImage(x, y) || singularity[getPatchIndex(x, y)]) {
                return Double.NaN;
            }
            return image.getNormalizedImageMatrix()[x][y];
        }

        public void set(DataType type, int x, int y, double value) {
            if (type == null) {
                return;
            }

            switch (type) {
                case ORIENTATION:
                    checkAndSet(x, y, value, image.getOrientationMatrix(), orientation);
                    break;

                case RIDGE_FREQUENCY:
                    checkAndSet(x, y, value, image.getRidgeFrequencyMatrix(), ridgeFrequency);
                    break;

                case DIVERGENCE:
                    checkAndSet(x, y, value, image.getDivergenceMatrix(), divergence);
                    break;

                case LINE_DIVERGENCE:
                    checkAndSet(x, y, value, image.getLineDivergenceMatrix(), lineDivergence);
                    break;
            }
        }

        public void setIntensity(int x, int y, double value) {
            if (isInPatch(x, y)) {
                image.getIntensityMatrix()[x][y] = value;
            }
        }

        public void setNecessaryMinutiae(int x, int y, double value) {
            if (isInPatch(x, y)) {
                image.getNecessaryMinutiae()[x][y] = value;
            }
        }

        public boolean isInRoi(int x, int y) {
            return isInImage(x, y) && image.getROI()[x][y];
        }

        public boolean containsSingularity() {
            return singularity[index];
        }

        public boolean isInPatch(int x, int y) {
            return x >= fromX &&
                    x < toX &&
                    y >= fromY &&
                    y < toY;
        }

        public boolean isInPatchWithPadding(int x, int y) {
            return x >= fromX - padX &&
                    x < toX + padX &&
                    y >= fromY - padY &&
                    y < toY + padY;
        }

        public int getIndex() {
            return index;
        }

        public boolean isInImage(int x, int y) {
            return x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight();
        }

        private double[][] initPaddingMatrix(double[][] source) {
            double[][] target = new double[toX - fromX + 2 * padX][toY - fromY + 2 * padY];
            for (int x = 0; x < toX - fromX + 2 * padX; x++) {
                for (int y = 0; y < toY - fromY + 2 * padY; y++) {
                    target[x][y] = getFromMatrix(source, fromX + x - padX, fromY + y - padY);
                }
            }
            return target;
        }

        private double getFromMatrix(double[][] matrix, int x, int y) {
            if (!isInImage(x, y) || singularity[getPatchIndex(x, y)]) {
                return Double.NaN;
            }
            return matrix[x][y];
        }

        private double checkAndGet(int x, int y, double[][] matrix) {
            int idx = getPatchIndex(x, y);
            if (!isInPatchWithPadding(x, y) || containsSingularity() || (idx >= 0 && idx < patches.size() && singularity[idx])) {
                return Double.NaN;
            }
            return matrix[x - fromX + padX][y - fromY + padY];
        }

        private void checkAndSet(int x, int y, double value, double[][] matrix, double[][] paddedMatrix) {
            if (isInPatchWithPadding(x, y)) {
                paddedMatrix[x - fromX + padX][y - fromY + padY] = value;

                if (isInPatch(x, y)) {
                    matrix[x][y] = value;
                }
            }
        }
    }

    public enum DataType {
        ORIENTATION,
        RIDGE_FREQUENCY,
        DIVERGENCE,
        LINE_DIVERGENCE,
    }
}
