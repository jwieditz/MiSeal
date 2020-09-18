package de.unigoettingen.math.fingerprint.cli;

public class UnknownSmoothingException extends Exception {

    public UnknownSmoothingException(String type) {
        super("Unknown smoothing type " + type);
    }
}
