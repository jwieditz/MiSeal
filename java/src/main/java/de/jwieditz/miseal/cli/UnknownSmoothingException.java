package de.jwieditz.miseal.cli;

public class UnknownSmoothingException extends Exception {

    public UnknownSmoothingException(String type) {
        super("Unknown smoothing type " + type);
    }
}
