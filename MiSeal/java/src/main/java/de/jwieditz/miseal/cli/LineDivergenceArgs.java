package de.jwieditz.miseal.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LineDivergenceArgs {
    @Parameter(names = {"-lo", "--line-divergence-output"}, description = "The output file for the line divergence (.csv)")
    public File output;

    @DynamicParameter(names = "-L", description = "Dynamic parameters for the line divergence calculation")
    public Map<String, String> dynamicArgs = new HashMap<>();
}
