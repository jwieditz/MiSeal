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

public class OrientationArgs {

    @Parameter(names = {"-o", "--orientation"}, description = "Choose the orientation estimation method", validateWith = MethodName.class)
    public String method = "default";

    @Parameter(names = {"-oo", "--orientation-output"}, description = "The output file for the image orientation (.csv)")
    public File output;

    @DynamicParameter(names = "-Odefault", description = "Dynamic parameters for the default orientation estimation method")
    public Map<String, String> defaultArgs = new HashMap<>();

    @DynamicParameter(names = "-Ohwj", description = "Dynamic parameters for the HWJ orientation estimation method")
    public Map<String, String> hwjArgs = new HashMap<>();

    public static class MethodName implements IParameterValidator {

        private static final List<String> VALID = Arrays.asList("default", "hwj");

        @Override
        public void validate(String name, String value) throws ParameterException {
            if (!VALID.contains(value)) {
                throw new ParameterException("'" + value + "' is not a valid orientation estimation method. Valid are: " + VALID);
            }
        }
    }
}
