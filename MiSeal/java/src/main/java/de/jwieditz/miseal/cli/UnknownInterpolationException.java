package de.jwieditz.miseal.cli;

public class UnknownInterpolationException extends Exception {

    public UnknownInterpolationException(String type) {
        super("Unknown interpolation type " + type);
    }
}
