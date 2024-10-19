package io.kneo.qtracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.dto.AbstractDTO;
import io.kneo.core.dto.Views;
import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.localization.LanguageCode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class OwnerDTO extends AbstractDTO {
    private String email;
    private String telegramName;
    private String whatsappName;
    private EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);
    private String phone;
    private String country;
    private String currency;
    private List<VehicleDTO> vehicles;

    @JsonView(Views.DetailView.class)
    @NotNull
    private LocalDate birthDate;

    @JsonView(Views.DetailView.class)
    private List<RLSDTO> rls = new ArrayList<>();

    public OwnerDTO(String id) {
        this.id = UUID.fromString(id);
    }
}
