package io.kneo.core.dto.document;

import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.dto.AbstractDTO;
import io.kneo.core.dto.Views;
import io.kneo.core.localization.LanguageCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.EnumMap;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDTO extends AbstractDTO {
    @JsonView(Views.ListView.class)
    LanguageCode code;
    @JsonView(Views.ListView.class)
    EnumMap<LanguageCode, String> localizedNames;
    @JsonView(Views.ListView.class)
    int position;
}
