package io.kneo.officeframe.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Deprecated
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class PlainUserDTO {
    private Long id;
    private String name;
}
