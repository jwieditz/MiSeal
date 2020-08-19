package de.unigoettingen.math.fingerprint.display.controller;

import java.io.IOException;

import de.unigoettingen.math.fingerprint.display.Validation;

public interface IController {
    void reset() throws IOException;

    void setDefaults() throws IOException;

    void initializeValidation(Validation validation);
}
