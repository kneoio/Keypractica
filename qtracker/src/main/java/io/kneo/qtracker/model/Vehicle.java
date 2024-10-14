package io.kneo.qtracker.model;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.SecureDataEntity;
import io.kneo.core.server.Environment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.EnumMap;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class Vehicle extends SecureDataEntity<UUID> {
    private UUID ownerId;
    private String vin;
    private int vehicleType;
    private String brand;
    private String model;
    private int fuelType;
    private int status;
    private EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);

    public EnumMap<LanguageCode, String> getLocalizedName() {
        for (LanguageCode code : Environment.AVAILABLE_LANGUAGES) {
            if (!localizedName.containsKey(code)) {
                localizedName.put(code, "");
            }
        }

        return localizedName;
    }
}
