package de.unigoettingen.math.fingerprint.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class NormalizeArgs {

    @Parameter(names = {"-nm", "--normalize-mean"}, description = "Normalize the image such that it has the given mean", validateWith = PositiveDouble.class)
    public double mean = Double.NaN;

    @Parameter(names = {"-nv", "--normalize-variance"}, description = "Normalize the image such that it has the given variance", validateWith = PositiveDouble.class)
    public double variance = Double.NaN;

    public static class PositiveDouble implements IParameterValidator {

        @Override
        public void validate(String name, String value) throws ParameterException {
            Double val = Double.parseDouble(value);
            if (val <= 0) {
                throw new ParameterException("The value must be strictly greater than 0");
            }
        }
    }
}
