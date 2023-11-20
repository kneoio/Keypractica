package io.kneo.core.dto.document;

import io.kneo.core.dto.validation.ValidLanguageCode;
import io.kneo.core.localization.LanguageCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.TimeZone;

public record UserRegistrationDTO(
        @NotNull @Email
        String email,
        @NotNull @Size(min = 2, max = 50)
        String login,
        @NotNull
        @ValidLanguageCode(enumClass = LanguageCode.class)
        LanguageCode lang,
        @NotNull
        TimeZone timeZone,
        @Min(value = 0, message = "Confirmation code must be at least 1000")
        @Max(value = 9999, message = "Confirmation code must be at most 9999") int confirmationCode) {

}
