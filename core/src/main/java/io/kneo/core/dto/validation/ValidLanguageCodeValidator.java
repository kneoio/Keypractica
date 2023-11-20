package io.kneo.core.dto.validation;

import io.kneo.core.localization.LanguageCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidLanguageCodeValidator implements ConstraintValidator<ValidLanguageCode, LanguageCode> {
    private List<String> acceptedValues;
    @Override
    public void initialize(ValidLanguageCode annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
    @Override
    public boolean isValid(LanguageCode value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return acceptedValues.contains(value.toString());
    }
}
