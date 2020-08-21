package de.unigoettingen.math.fingerprint.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class IntensityArgs {

    @Parameter(names = {"-i", "--intensity"}, description = "Choose the intensity estimation method", validateWith = MethodName.class)
    public String method = "default";

    @Parameter(names = {"-io", "--intensity-output"}, description = "The output file for the image intensity (.csv)")
    public File output;

    public static class MethodName implements IParameterValidator {

        private static final List<String> VALID = Arrays.asList("default");

        @Override
        public void validate(String name, String value) throws ParameterException {
            if (!VALID.contains(value)) {
                throw new ParameterException("'" + value + "' is not a valid orientation estimation method. Valid are: " + VALID);
            }
        }
    }
}
