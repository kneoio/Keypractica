package io.kneo.core.dto.document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class UserDTO {
    @NotNull
    String identifier;
    @NotNull
    String name;
    @NotNull
    String login;
    @NotNull @Email
    String email;
    @NotNull
    String language;
    @NotNull
    String theme;
    List<String> roles = Collections.emptyList();
    List<String> modules = Collections.emptyList();
}
