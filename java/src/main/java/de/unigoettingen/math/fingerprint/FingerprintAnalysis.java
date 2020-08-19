package de.unigoettingen.math.fingerprint;

import com.beust.jcommander.JCommander;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import de.unigoettingen.math.fingerprint.cli.Args;
import de.unigoettingen.math.fingerprint.cli.DivergenceArgs;
import de.unigoettingen.math.fingerprint.cli.IntensityArgs;
import de.unigoettingen.math.fingerprint.cli.LineDivergenceArgs;
import de.unigoettingen.math.fingerprint.cli.NormalizeArgs;
import de.unigoettingen.math.fingerprint.cli.OrientationArgs;
import de.unigoettingen.math.fingerprint.cli.RidgeFrequencyArgs;
import de.unigoettingen.math.fingerprint.cli.UnknownInterpolationException;
import de.unigoettingen.math.fingerprint.cli.UnknownSmoothingException;
import de.unigoettingen.math.fingerprint.display.old.GradientImageDisplay;
import de.unigoettingen.math.fingerprint.display.old.ImageDisplay;
import de.unigoettingen.math.fingerprint.display.old.OrientationImageDisplay;
import de.unigoettingen.math.fingerprint.divergence.CurvedRegionDivergenceEstimator;
import de.unigoettingen.math.fingerprint.divergence.DefaultDivergenceEstimator;
import de.unigoettingen.math.fingerprint.divergence.DivergenceEstimator;
import de.unigoettingen.math.fingerprint.intensity.DefaultIntensityEstimator;
import de.unigoettingen.math.fingerprint.intensity.IntensityEstimator;
import de.unigoettingen.math.fingerprint.interpolation.BilinearInterpolation;
import de.unigoettingen.math.fingerprint.interpolation.GaussianInterpolation;
import de.unigoettingen.math.fingerprint.interpolation.Interpolation;
import de.unigoettingen.math.fingerprint.interpolation.NearestNeighbourInterpolation;
import de.unigoettingen.math.fingerprint.io.ImageToMatrixReader;
import de.unigoettingen.math.fingerprint.io.MatrixWriter;
import de.unigoettingen.math.fingerprint.io.MinutiaeReader;
import de.unigoettingen.math.fingerprint.io.OrientationReader;
import de.unigoettingen.math.fingerprint.io.ROIReader;
import de.unigoettingen.math.fingerprint.orientation.DefaultOrientationEstimator;
import de.unigoettingen.math.fingerprint.orientation.HWJOrientationEstimator;
import de.unigoettingen.math.fingerprint.orientation.OrientationEstimator;
import de.unigoettingen.math.fingerprint.ridgefrequency.CurvedRegionFrequencyEstimator;
import de.unigoettingen.math.fingerprint.ridgefrequency.DefaultFrequencyEstimator;
import de.unigoettingen.math.fingerprint.ridgefrequency.FrequencyEstimator;
import de.unigoettingen.math.fingerprint.singularPoints.DefaultSingularPoints;
import de.unigoettingen.math.fingerprint.singularPoints.SingularPoints;
import de.unigoettingen.math.fingerprint.smoothing.AverageSmoothing;
import de.unigoettingen.math.fingerprint.smoothing.GaussianSmoothing;
import de.unigoettingen.math.fingerprint.smoothing.GaussianSmoothing2D;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing1D;
import de.unigoettingen.math.fingerprint.smoothing.Smoothing2D;
import de.unigoettingen.math.fingerprint.utilities.MaximumPadding;
import de.unigoettingen.math.fingerprint.utilities.unwrap.UnwrapType;
import de.unigoettingen.math.fingerprint.utilities.unwrap.Unwrapper;

public class FingerprintAnalysis {

    public static final Args ARGS = new Args();

    public static void main(String[] argv) throws UnknownInterpolationException, UnknownSmoothingException {
        JCommander jCommander =
                JCommander.newBuilder()
                        .addObject(ARGS)
                        .programName("fingerprint")
                        .build();

        jCommander.parse(argv);

        if (ARGS.help) {
            jCommander.usage();
            return;
        }

        FingerprintImage image;

        final MaximumPadding padding = new MaximumPadding();

        try {
            System.out.println("Loading fingerprint image \"" + ARGS.inputFile + "\"");

            image = ImageToMatrixReader.readImageToMatrix(ARGS.inputFile);
        } catch (IOException e) {
            System.out.println("Could not read image at \"" + ARGS.inputFile + "\": " + e.getMessage());
            return;
        }

        Minutia[] minutiae = null;
        if (ARGS.minutiaeFile != null) {
            System.out.println("Found minutiae file \"" + ARGS.minutiaeFile + "\"");
            try {
                minutiae = MinutiaeReader.readMinutiae(ARGS.minutiaeFile);
            } catch (IOException e) {
                System.err.println("Could not read minutiae file at \"" + ARGS.minutiaeFile + "\": " + e.getMessage());
            }
        }

        boolean[][] roi = null;
        if (ARGS.roiFile != null) {
            System.out.println("Found ROI file \"" + ARGS.roiFile + "\"");
            try {
                roi = ROIReader.readROI(ARGS.roiFile);
            } catch (IOException e) {
                System.err.println("Could not read ROI file at \"" + ARGS.roiFile + "\": " + e.getMessage());
            }
        }
        // no inspection ConstantConditions
        if (roi == null || roi.length != image.getWidth() || roi[0].length != image.getHeight()) {
            if (roi == null) {
                System.out.println("ROI is null, therefore setting all to true");
            } else {
                System.out.println("ROI dimensions (" + roi.length + "," + roi[0].length + ") do not match image's (" + image.getWidth() + "," + image.getHeight() + "), therefore setting all to true");
            }
            roi = new boolean[image.getWidth()][image.getHeight()];
            Arrays.stream(roi).forEach(a -> Arrays.fill(a, true));
        }

        image.setROI(roi);

        if (ARGS.orientationFile != null) {
            System.out.println("Found orientation file \"" + ARGS.orientationFile + "\"");
            try {
                double[][] orientation = OrientationReader.readOrientationFromImage(ARGS.orientationFile);

                image.setOrientationMatrix(orientation);

                // since we already have the orientation field, we can skip its estimation later
                ARGS.skipOrientation = true;
            } catch (IOException e) {
                System.err.println("Could not read orientation file at \"" + ARGS.orientationFile + "\": " + e.getMessage());
            }
        }

        if (!ARGS.noGui) {
            new ImageDisplay(image.getImageDataMatrix()).withMinutiae(minutiae).display("");
        }

        OrientationEstimator orientationEstimator = null;
        FrequencyEstimator frequencyEstimator = null;
        DivergenceEstimator divergenceEstimator = null;
        LineDivergenceCalculator lineDivergenceCalculator = null;
        IntensityEstimator intensityEstimator = null;

        if (!ARGS.skipOrientation) {
            orientationEstimator = createOrientationEstimator(ARGS.orientationArgs);
        }

        if (!ARGS.skipRidgeFrequency) {
            frequencyEstimator = createRidgeFrequencyEstimator(ARGS.ridgeFrequencyArgs, padding);
        }

        if (!ARGS.skipDivergence) {
            divergenceEstimator = createDivergenceEstimator(ARGS.divergenceArgs, padding);
        }

        if (!ARGS.skipLineDivergence && !ARGS.skipRidgeFrequency) {
            lineDivergenceCalculator = createLineDivergenceCalculator(ARGS.lineDivergenceArgs, padding);
        }

        if (!ARGS.skipIntensity && !ARGS.skipRidgeFrequency && !ARGS.skipDivergence) {
            intensityEstimator = createIntensityEstimator(ARGS.intensityArgs);
        }

        // --------------------------------------
        // step 1: normalize the image
        // --------------------------------------

        normalize(image, ARGS.normalizeArgs);

        if (!ARGS.noGui) {
            new ImageDisplay(image.getNormalizedImageMatrix()).withMinutiae(minutiae).display("normalized");
        }


        // --------------------------------------
        // step 2: calculate the local orientation
        // --------------------------------------

        estimateOrientation(image, orientationEstimator, ARGS.orientationArgs);
        unwrapImage(image, ARGS.orientationArgs, padding);

        // --------------------------------------
        // step 3: estimate the local ridge frequency
        // --------------------------------------

        estimateRidgeFrequency(image, frequencyEstimator, ARGS.ridgeFrequencyArgs);

        // --------------------------------------
        // step 4: estimate the divergence
        // --------------------------------------

        estimateDivergence(image, divergenceEstimator, ARGS.divergenceArgs);

        // --------------------------------------
        // step 5: calculate the line divergence
        // --------------------------------------

        estimateLineDivergence(image, lineDivergenceCalculator, ARGS.lineDivergenceArgs);

        // --------------------------------------
        // step 6: estimate the intensity
        // --------------------------------------
        estimateIntensity(image, intensityEstimator, ARGS.intensityArgs);

        System.out.println("Finished image \"" + ARGS.inputFile + "\"");

        if (!ARGS.noGui) {
            System.out.println("Close all image windows to stop the program");
        }
    }

    private static void normalize(FingerprintImage image, NormalizeArgs args) {
        double mean = args.mean;
        double variance = args.variance;

        // no need to normalize if mean and variance are not specified
        if (Double.isNaN(mean) && Double.isNaN(variance)) {
            return;
        }

        if (Double.isNaN(mean)) {
            mean = image.getMean();
        }

        if (Double.isNaN(variance)) {
            variance = image.getVariance();
        }

        Normalizer normalizer = new Normalizer();
        normalizer.normalize(image, mean, variance);
    }

    private static OrientationEstimator createOrientationEstimator(OrientationArgs args) {
        OrientationEstimator estimator;
        switch (args.method) {
            case "hwj":
                boolean useLowPassFilter = Boolean.parseBoolean(args.hwjArgs.getOrDefault("LowPassFilter", "false"));
                estimator = new HWJOrientationEstimator(useLowPassFilter);
                break;

            case "default":
            default:
                double sigmaQ = Double.parseDouble(args.defaultArgs.getOrDefault("SigmaQ", "25"));
                int iterations = Integer.parseInt((args.defaultArgs.getOrDefault("Iter", "1")));
                estimator = new DefaultOrientationEstimator(sigmaQ);
                ((DefaultOrientationEstimator) estimator).setIterations(iterations);
                break;
        }

        return estimator;
    }

    private static void estimateOrientation(FingerprintImage image, OrientationEstimator estimator, OrientationArgs args) {
        if (estimator != null) {
            estimator.calculateOrientation(image);
        }

        try {
            if (args.output != null) {
                double[][] orientation = image.getOrientationMatrix();

                MatrixWriter.writeMatrixToFile(orientation, args.output);
            }
        } catch (IOException e) {
            System.err.println("Could not save orientation output: " + e.getMessage());
        }

        if (!ARGS.noGui) {
            new ImageDisplay(image.getOrientationMatrix()).withMinutiae(image.getMinutiae()).display("orientation");
            new OrientationImageDisplay(image.getNormalizedImageMatrix(), image.getOrientationMatrix()).withMinutiae(image.getMinutiae()).display("orientation");
        }
    }

    private static void unwrapImage(FingerprintImage image, OrientationArgs args, MaximumPadding padding) {
        String unwrapTypeString = args.defaultArgs.getOrDefault("Unwrap", "diamond");
        UnwrapType type = null;
        switch (unwrapTypeString) {
            case "lines":
                type = UnwrapType.LINES;
                break;

            case "spirals":
                type = UnwrapType.SPIRALS;
                break;

            case "diamond":
                type = UnwrapType.DIAMOND;
                break;
        }

        SingularPoints singularPoints = new DefaultSingularPoints();
        singularPoints.computeSingularPoints(image);

        if (ARGS.patchWidth > 0 && ARGS.patchHeight > 0) {
            image.createPatchesForSize(ARGS.patchWidth, ARGS.patchHeight, padding.getPadX(), padding.getPadY());
        } else if (ARGS.patchNumHorizontal > 0 && ARGS.patchNumVertical > 0){
            image.createPatches(ARGS.patchNumHorizontal, ARGS.patchNumVertical, padding.getPadX(), padding.getPadY());
        } else {
            image.createPatchesForSize(16, 16, padding.getPadX(), padding.getPadY());
        }

        Unwrapper unwrapper = new Unwrapper();

        for (PatchedData.Patch patch : image.getPatches()) {
            patch.initOrientationPadding();

            unwrapper.unwrap(patch, type);
        }
    }

    private static FrequencyEstimator createRidgeFrequencyEstimator(RidgeFrequencyArgs args, MaximumPadding padding) throws UnknownInterpolationException, UnknownSmoothingException {
        FrequencyEstimator estimator;
        switch (args.method) {
            case "cr":
                int p = Integer.parseInt(args.curvedRegionArgs.getOrDefault("P", "16"));
                int q = Integer.parseInt(args.curvedRegionArgs.getOrDefault("Q", "32"));
                estimator = new CurvedRegionFrequencyEstimator(p, q);

                String greyValueInterpolationType = args.curvedRegionArgs.get("GreyInterp");
                if (isStringOk(greyValueInterpolationType)) {
                    Interpolation greyValueInterpolation = parseInterpolation(args.curvedRegionArgs, greyValueInterpolationType, "GreyInterp");
                    ((CurvedRegionFrequencyEstimator) estimator).setGreyValueInterpolation(greyValueInterpolation);
                }

                String orientationInterpolationType = args.curvedRegionArgs.get("OrientationInterp");
                if (isStringOk(orientationInterpolationType)) {
                    Interpolation orientationInterpolation = parseInterpolation(args.curvedRegionArgs, orientationInterpolationType, "OrientationInterp");
                    ((CurvedRegionFrequencyEstimator) estimator).setOrientationInterpolation(orientationInterpolation);
                }

                String finalSmoothingType = args.curvedRegionArgs.get("FinalSmooth");
                if (isStringOk(finalSmoothingType)) {
                    Smoothing2D finalSmoothing = parseSmoothing2D(args.curvedRegionArgs, finalSmoothingType, "FinalSmooth");
                    ((CurvedRegionFrequencyEstimator) estimator).setFinalImageSmoothing(finalSmoothing);
                }

                String profileSmoothType = args.curvedRegionArgs.get("ProfileSmooth");
                if (isStringOk(profileSmoothType)) {
                    Smoothing1D profileSmoothing = parseSmoothing1D(args.curvedRegionArgs, profileSmoothType, "ProfileSmooth");
                    int maxIterations = Integer.parseInt(args.curvedRegionArgs.getOrDefault("ProfileSmoothIterations", "3"));
                    ((CurvedRegionFrequencyEstimator) estimator).setProfileSmoothing(profileSmoothing, maxIterations);
                }

                padding.update(((CurvedRegionFrequencyEstimator) estimator).getOrientationInterpolation());
                padding.update(((CurvedRegionFrequencyEstimator) estimator).getGreyValueInterpolation());
                padding.update(((CurvedRegionFrequencyEstimator) estimator).getFinalImageSmoothing());
                break;

            case "default":
            default:
                int windowWidth = Integer.parseInt(args.defaultArgs.getOrDefault("WindowWidth", "17"));
                int orientedWindowWidth = Integer.parseInt(args.defaultArgs.getOrDefault("OrientedWindowWidth", "33"));
                estimator = new DefaultFrequencyEstimator(windowWidth, orientedWindowWidth);

                String frequencyInterpolationType = args.defaultArgs.get("Interpolation");
                if (isStringOk(frequencyInterpolationType)) {
                    Interpolation frequencyInterpolation = parseInterpolation(args.defaultArgs, frequencyInterpolationType, "Interpolation");
                    ((DefaultFrequencyEstimator) estimator).setFrequencyInterpolation(frequencyInterpolation);
                }

                String smoothingType = args.defaultArgs.get("Smoothing");
                if (isStringOk(smoothingType)) {
                    Smoothing2D smoothing = parseSmoothing2D(args.defaultArgs, smoothingType, "Smoothing");
                    ((DefaultFrequencyEstimator) estimator).setFinalImageSmoothing(smoothing);
                }

                padding.update(((DefaultFrequencyEstimator) estimator).getFrequencyInterpolation());
                padding.update(((DefaultFrequencyEstimator) estimator).getFinalImageSmoothing());
                break;
        }

        return estimator;
    }

    private static void estimateRidgeFrequency(FingerprintImage image, FrequencyEstimator estimator, RidgeFrequencyArgs args) {
        if (estimator != null) {
            estimator.estimateFrequency(image);
        }

        try {
            if (args.output != null) {
                double[][] ridgeFrequency = image.getRidgeFrequencyMatrix();

                MatrixWriter.writeMatrixToFile(ridgeFrequency, args.output);
            }
        } catch (IOException e) {
            System.err.println("Could not save ridge frequency output: " + e.getMessage());
        }

        if (!ARGS.noGui) {
            new ImageDisplay(image.getRidgeFrequencyMatrix()).withMinutiae(image.getMinutiae()).display("ridge frequency");
        }
    }

    private static DivergenceEstimator createDivergenceEstimator(DivergenceArgs args, MaximumPadding padding) throws UnknownSmoothingException, UnknownInterpolationException {
        DivergenceEstimator estimator;

        switch (args.method) {
            case "cr":
                int p = Integer.parseInt(args.curvedRegionArgs.getOrDefault("P", "16"));
                int q = Integer.parseInt(args.curvedRegionArgs.getOrDefault("Q", "32"));
                estimator = new CurvedRegionDivergenceEstimator(p, q);

                String orientationInterpolationType = args.curvedRegionArgs.get("OrientationInterp");
                if (isStringOk(orientationInterpolationType)) {
                    Interpolation interpolation = parseInterpolation(args.curvedRegionArgs, orientationInterpolationType, "OrientationInterp");
                    ((CurvedRegionDivergenceEstimator) estimator).setOrientationInterpolation(interpolation);
                }

                String smoothingType = args.curvedRegionArgs.get("Smoothing");
                if (isStringOk(smoothingType)) {
                    Smoothing2D smoothing = parseSmoothing2D(args.curvedRegionArgs, smoothingType, "Smoothing");
                    ((CurvedRegionDivergenceEstimator) estimator).setDivergenceSmoothing(smoothing);
                }

                boolean useRealDistance = Boolean.parseBoolean(args.curvedRegionArgs.getOrDefault("UseRealDistance", "true"));
                ((CurvedRegionDivergenceEstimator) estimator).setUseRealDistance(useRealDistance);

                boolean removeLineDivergence = Boolean.parseBoolean(args.curvedRegionArgs.getOrDefault("RemoveLineDivergence", "true"));
                ((CurvedRegionDivergenceEstimator) estimator).setRemoveLineDivergence(removeLineDivergence);

                padding.update(((CurvedRegionDivergenceEstimator) estimator).getOrientationInterpolation());
                padding.update(((CurvedRegionDivergenceEstimator) estimator).getDivergenceSmoothing());
                break;

            case "default":
            default:
                estimator = new DefaultDivergenceEstimator();

                String smoothingTypeDefault = args.defaultArgs.get("Smoothing");
                if (isStringOk(smoothingTypeDefault)) {
                    Smoothing2D smoothing = parseSmoothing2D(args.defaultArgs, smoothingTypeDefault, "Smoothing");
                    ((DefaultDivergenceEstimator) estimator).setSmoothing(smoothing);
                }

                padding.update(((DefaultDivergenceEstimator) estimator).getDivergenceSmoothing());
                break;
        }

        return estimator;


    }

    private static void estimateDivergence(FingerprintImage image, DivergenceEstimator estimator, DivergenceArgs args) {
        if (estimator != null) {
            estimator.calculateDivergence(image);
        }

        try {
            if (args.output != null) {
                double[][] divergence = image.getDivergenceMatrix();

                MatrixWriter.writeMatrixToFile(divergence, args.output);
            }
        } catch (IOException e) {
            System.err.println("Could not save divergence output: " + e.getMessage());
        }

        if (!ARGS.noGui) {
            new ImageDisplay(image.getDivergenceMatrix()).withMinutiae(image.getMinutiae()).display("divergence");
        }
    }

    private static LineDivergenceCalculator createLineDivergenceCalculator(LineDivergenceArgs args, MaximumPadding padding) throws UnknownSmoothingException {
        LineDivergenceCalculator calculator = new LineDivergenceCalculator();

        String smoothingType = args.dynamicArgs.get("Smoothing");
        if (isStringOk(smoothingType)) {
            Smoothing2D smoothing = parseSmoothing2D(args.dynamicArgs, smoothingType, "Smoothing");
            calculator.setLineDivergenceSmoothing(smoothing);
        }

        padding.update(calculator.getLineDivergenceSmoothing());

        return calculator;
    }

    private static void estimateLineDivergence(FingerprintImage image, LineDivergenceCalculator estimator, LineDivergenceArgs args) {
        if (estimator != null) {
            estimator.calculateLineDivergence(image);
        }

        try {
            if (args.output != null) {
                double[][] lineDivergence = image.getLineDivergenceMatrix();

                MatrixWriter.writeMatrixToFile(lineDivergence, args.output);
            }
        } catch (IOException e) {
            System.err.println("Could not save line divergence output: " + e.getMessage());
        }

        if (!ARGS.noGui) {
            new ImageDisplay(image.getLineDivergenceMatrix()).withMinutiae(image.getMinutiae()).display("line divergence");
        }
    }

    private static IntensityEstimator createIntensityEstimator(IntensityArgs args) {
        return new DefaultIntensityEstimator();
    }

    private static void estimateIntensity(FingerprintImage image, IntensityEstimator estimator, IntensityArgs args) {
        if (estimator != null) {
            estimator.estimateIntensity(image);
        }

        try {
            if (args.output != null) {
                double[][] intensity = image.getIntensityMatrix();

                MatrixWriter.writeMatrixToFile(intensity, args.output);
            }
        } catch (IOException e) {
            System.err.println("Could not save intensity output: " + e.getMessage());
        }

        if (!ARGS.noGui) {
            new ImageDisplay(image.getIntensityMatrix()).withMinutiae(image.getMinutiae()).display("intensity");
            new ImageDisplay(image.getNecessaryMinutiae()).withMinutiae(image.getMinutiae()).display("necessary minutiae");
        }
    }

    private static Interpolation parseInterpolation(Map<String, String> args, String type, String prefix) throws UnknownInterpolationException {
        Interpolation interpolation;

        switch (type) {
            case "bilinear":
                interpolation = new BilinearInterpolation();
                break;

            case "nn":
                interpolation = new NearestNeighbourInterpolation();
                break;

            case "gauss":
                double mean = Double.parseDouble(args.getOrDefault(prefix + "Mean", "0"));
                double variance = Double.parseDouble(args.getOrDefault(prefix + "Variance", "1"));
                int size = Integer.parseInt(args.getOrDefault(prefix + "Size", "1"));
                int width = Integer.parseInt(args.getOrDefault(prefix + "Width", "1"));
                interpolation = new GaussianInterpolation(mean, variance, size, width);
                break;

            default:
                throw new UnknownInterpolationException(type);
        }

        return interpolation;
    }

    private static Smoothing2D parseSmoothing2D(Map<String, String> args, String type, String prefix) throws UnknownSmoothingException {
        Smoothing2D smoothing;

        switch (type) {
            case "avg":
                int size = Integer.parseInt(args.getOrDefault(prefix + "Size", "1"));
                smoothing = new AverageSmoothing(size);
                break;

            case "gauss":
                int gaussSize = Integer.parseInt(args.getOrDefault(prefix + "Size", "1"));
                double mean = Double.parseDouble(args.getOrDefault(prefix + "Mean", "0"));
                double variance = Double.parseDouble(args.getOrDefault(prefix + "Variance", "1"));
                smoothing = new GaussianSmoothing2D(gaussSize, mean, variance);
                break;

            default:
                throw new UnknownSmoothingException(type);
        }

        return smoothing;
    }

    private static Smoothing1D parseSmoothing1D(Map<String, String> args, String type, String prefix) throws UnknownSmoothingException {
        Smoothing1D smoothing;

        switch (type) {
            case "gauss":
                int size = Integer.parseInt(args.getOrDefault(prefix + "Size", "1"));
                double mean = Double.parseDouble(args.getOrDefault(prefix + "Mean", "0"));
                double variance = Double.parseDouble(args.getOrDefault(prefix + "Variance", "1"));
                smoothing = new GaussianSmoothing(size, mean, variance);
                break;

            default:
                throw new UnknownSmoothingException(type);
        }

        return smoothing;
    }

    private static boolean isStringOk(String s) {
        return s != null && !s.isEmpty();
    }

}
