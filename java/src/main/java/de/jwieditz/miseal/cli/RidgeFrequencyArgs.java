package de.jwieditz.miseal.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RidgeFrequencyArgs {

    @Parameter(names = {"-r", "--ridgefrequency"}, description = "Choose the ridge frequency estimation method", validateWith = MethodName.class)
    public String method = "default";

    @Parameter(names = {"-ro", "--ridgefrequency-output"}, description = "The output file for the ridge frequency (.csv)")
    public File output;

    @DynamicParameter(names = "-Rdefault", description = "Dynamic parameters for the default ridge frequency estimation method")
    public Map<String, String> defaultArgs = new HashMap<>();

    @DynamicParameter(names = "-Rcr", description = "Dynamic parameters for the curved region ridge frequency estimation method")
    public Map<String, String> curvedRegionArgs = new HashMap<>();

    public static class MethodName implements IParameterValidator {

        private static final List<String> VALID = Arrays.asList("default", "cr");

        @Override
        public void validate(String name, String value) throws ParameterException {
            if (!VALID.contains(value)) {
                throw new ParameterException("'" + value + "' is not a valid frequency estimation method. Valid are: " + VALID);
            }
        }
    }
}
