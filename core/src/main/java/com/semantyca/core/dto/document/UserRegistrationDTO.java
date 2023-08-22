package com.semantyca.core.dto.document;

import com.semantyca.core.localization.LanguageCode;
import jakarta.validation.constraints.*;

import java.util.TimeZone;

public record UserRegistrationDTO(
        @NotNull @Email String email,
        @NotNull @Size(min = 2, max = 50) String userName,
        @NotNull LanguageCode lang,
        @NotNull TimeZone timeZone,
        @Min(value = 100000, message = "Confirmation code must be at least 100000")
        @Max(value = 999999, message = "Confirmation code must be at most 999999") int confirmationCode) {

}
