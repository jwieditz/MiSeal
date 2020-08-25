package de.jwieditz.miseal.display.controller;

import java.io.IOException;

import de.jwieditz.miseal.display.Validation;

public interface IController {
    void reset() throws IOException;

    void setDefaults() throws IOException;

    void initializeValidation(Validation validation);
}
