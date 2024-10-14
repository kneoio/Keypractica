package io.kneo.qtracker.model;

import io.kneo.core.model.SecureDataEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Consuming extends SecureDataEntity<UUID> {
    private UUID vehicleId;
    private int status;
    private int totalKm;
    private int lastLiters;
    private int lastCost;
}
