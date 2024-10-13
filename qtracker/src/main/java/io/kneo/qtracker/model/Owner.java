package io.kneo.qtracker.model;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.SecureDataEntity;
import io.kneo.core.server.Environment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class Owner extends SecureDataEntity<UUID> {
    private long userId;
    private int status;
    private String email;
    private String telegramName;
    private EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);
    private String phone;
    private String country;
    private String currency;
    private LocalDate birthDate;

    public EnumMap<LanguageCode, String> getLocalizedName() {
        for (LanguageCode code : Environment.AVAILABLE_LANGUAGES) {
            if (!localizedName.containsKey(code)) {
                localizedName.put(code, "");
            }
        }

        return localizedName;
    }
}
