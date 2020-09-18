package de.unigoettingen.math.fingerprint.display;

import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.scene.control.TextField;

public class Validation {

    private static final String M_POSITIVE_INTEGER = "The value of %s must be a strictly positive integer (> 0).";
    private static final String M_POSITIVE_DOUBLE = "The value of %s must be a strictly positive double (> 0).";
    private static final String M_POSITIVE_OR_NEGATIVE_DOUBLE = "The value of %s must be a double.";
    private static final String M_NOT_EMPTY = "The value of %s must not be empty.";
    private static final String M_ODD_INTEGER = "The value of %s must be an odd integer.";

    private static final Pattern POSITIVE_INTEGER = Pattern.compile("[1-9]\\d*");
    private static final Pattern POSITIVE_DOUBLE = Pattern.compile("\\+?\\d*\\.?\\d+");
    private static final Pattern POSITIVE_OR_NEGATIVE_DOUBLE = Pattern.compile("[+-]?\\d*\\.?\\d+");

    private final ValidationSupport validationSupport = new ValidationSupport();

    public boolean isInvalid() {
        return validationSupport.isInvalid();
    }

    public String getAllErrors() {
        return validationSupport.getValidationResult().getMessages().stream()
                .map(ValidationMessage::getText)
                .collect(Collectors.joining("\n"));
    }

    public void positiveInteger(TextField textField, String location) {
        validationSupport.registerValidator(textField, Validator.createRegexValidator(String.format(M_POSITIVE_INTEGER, location), POSITIVE_INTEGER, Severity.ERROR));
    }

    public void positiveOddInteger(TextField textField, String location) {
        validationSupport.registerValidator(textField, Validator.combine(
                Validator.createRegexValidator(String.format(M_POSITIVE_INTEGER, location), POSITIVE_INTEGER, Severity.ERROR),
                Validator.createPredicateValidator(s -> Integer.parseInt(s) % 2 == 1, String.format(M_ODD_INTEGER, location), Severity.ERROR)
        ));
    }

    public void positiveOfNegativeDouble(TextField textField, String location) {
        validationSupport.registerValidator(textField, Validator.createRegexValidator(String.format(M_POSITIVE_OR_NEGATIVE_DOUBLE, location), POSITIVE_OR_NEGATIVE_DOUBLE, Severity.ERROR));
    }

    public void positiveDouble(TextField textField, String location) {
        validationSupport.registerValidator(textField, Validator.createRegexValidator(String.format(M_POSITIVE_DOUBLE, location), POSITIVE_DOUBLE, Severity.ERROR));
    }

    public void notEmpty(TextField textField, String location) {
        validationSupport.registerValidator(textField, Validator.createEmptyValidator(String.format(M_NOT_EMPTY, location)));
    }

}
