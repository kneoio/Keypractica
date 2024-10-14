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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class VehicleDTO extends AbstractDTO {
    @NotNull
    private UUID ownerId;
    private String vin;
    private int vehicleType;
    private String brand;
    private String model;
    private int fuelType;
    private int status;

    private EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);

    @JsonView(Views.DetailView.class)
    @NotNull
    private List<RLSDTO> rls = new ArrayList<>();

    public VehicleDTO(String id) {
        this.id = UUID.fromString(id);
    }
}
