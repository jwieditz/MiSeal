package de.jwieditz.miseal.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.validators.PositiveInteger;

import java.io.File;

public class Args {

    @Parameter(description = "input", converter = FileConverter.class, required = true)
    public File inputFile;

    @Parameter(names = {"-m", "--minutiae"}, description = "The input file for the minutiae")
    public File minutiaeFile;

    @Parameter(names = {"-roi", "--region-of-interest"}, description = "The input file for the region of interest")
    public File roiFile;

    @Parameter(names = {"-oi", "--orientation-input"}, description = "The input file for the orientation field")
    public File orientationFile;

    @Parameter(names = {"-h", "--help"}, description = "Display this help and exit", help = true)
    public boolean help;

    @Parameter(names = "--nogui", description = "Disable plotting the images")
    public boolean noGui;

    @Parameter(names = "--skip-orientation", description = "Skip the estimation of the orientation field")
    public boolean skipOrientation;

    @Parameter(names = "--skip-ridge-frequency", description = "Skip the estimation of the ridge frequency")
    public boolean skipRidgeFrequency;

    @Parameter(names = "--skip-divergence", description = "Skip the estimation of the divergence")
    public boolean skipDivergence;

    @Parameter(names = "--skip-intensity", description = "Skip the estimation of the intensity")
    public boolean skipIntensity;

    @Parameter(names = "--skip-line-divergence", description = "Skip the calculation of the line divergence")
    public boolean skipLineDivergence;

    @Parameter(names = {"-pw", "--patch-width"}, description = "The patch width", validateWith = PositiveInteger.class)
    public int patchWidth;

    @Parameter(names = {"-ph", "--patch-height"}, description = "The patch height", validateWith = PositiveInteger.class)
    public int patchHeight;

    @Parameter(names = {"-pnh", "--patches-horizontal"}, description = "The number of horizontal patches", validateWith = PositiveInteger.class)
    public int patchNumHorizontal;

    @Parameter(names = {"-pnv", "--patches-vertical"}, description = "The number of vertical patches", validateWith = PositiveInteger.class)
    public int patchNumVertical;

    @ParametersDelegate
    public NormalizeArgs normalizeArgs = new NormalizeArgs();

    @ParametersDelegate
    public OrientationArgs orientationArgs = new OrientationArgs();

    @ParametersDelegate
    public RidgeFrequencyArgs ridgeFrequencyArgs = new RidgeFrequencyArgs();

    @ParametersDelegate
    public DivergenceArgs divergenceArgs = new DivergenceArgs();

    @ParametersDelegate
    public IntensityArgs intensityArgs = new IntensityArgs();

    @ParametersDelegate
    public LineDivergenceArgs lineDivergenceArgs = new LineDivergenceArgs();

    class OddInteger implements IParameterValidator {

        @Override
        public void validate(String name, String value) throws ParameterException {
            Integer val = Integer.parseInt(value);
            if (val % 2 == 0) {
                throw new ParameterException("The value must be odd.");
            }
        }
    }
}
