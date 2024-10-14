package io.kneo.qtracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.dto.AbstractDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class ConsumingDTO extends AbstractDTO {
    private UUID vehicleId; // Reference to Vehicle
    private int totalKm;
    private int lastLiters;
    private int lastCost;

    public ConsumingDTO(String id) {
        this.id = UUID.fromString(id);
    }
}
