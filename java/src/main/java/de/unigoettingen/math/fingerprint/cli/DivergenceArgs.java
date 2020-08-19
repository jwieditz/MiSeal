package de.unigoettingen.math.fingerprint.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DivergenceArgs {

    @Parameter(names = {"-d", "--divergence"}, description = "Choose the divergence estimation method", validateWith = DivergenceValidator.class)
    public String method = "default";

    @Parameter(names = {"-do", "--divergence-output"}, description = "The output file for the divergence (.csv)")
    public File output;

    @DynamicParameter(names = "-Ddefault", description = "Dynamic parameters for the default divergence estimation method")
    public Map<String, String> defaultArgs = new HashMap<>();

    @DynamicParameter(names = "-Dcr", description = "Dynamic parameters for the curved region divergence estimation method")
    public Map<String, String> curvedRegionArgs = new HashMap<>();

    public static class DivergenceValidator implements IParameterValidator {

        private static final List<String> VALID = Arrays.asList("default", "cr");

        @Override
        public void validate(String name, String value) throws ParameterException {
            if (!VALID.contains(value)) {
                throw new ParameterException("'" + value + "' is not a valid divergence estimation method. Valid are: " + VALID);
            }
        }
    }
}
