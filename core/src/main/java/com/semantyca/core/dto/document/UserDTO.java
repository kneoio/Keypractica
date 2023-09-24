package com.semantyca.core.dto.document;

import java.util.List;

public record UserDTO(String login, String email, String pwd, String language, String theme, List<String> roles, List<String> modules) {
}
