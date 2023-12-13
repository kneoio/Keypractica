package io.kneo.core.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.SimpleReferenceEntity;
import io.kneo.core.model.cnst.SystemRoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class Role extends SimpleReferenceEntity implements IRole {
    private SystemRoleType roleType;
    private EnumMap<LanguageCode, String> localizedDescription = new EnumMap<>(LanguageCode.class);

    @Override
    public String getName() {
        return identifier;
    }

    public static class Builder {
        private UUID id;
        private String identifier;
        private EnumMap<LanguageCode, String> localizedDescription = new EnumMap<>(LanguageCode.class);
        private final EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);
        private long author;
        private ZonedDateTime regDate;
        private ZonedDateTime lastModifiedDate;
        private long lastModifier;
        private final SystemRoleType roleType = SystemRoleType.UNKNOWN;


        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Role.Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Role.Builder setLocalizedName(EnumMap<LanguageCode, String> locName) {
            this.localizedDescription = locName;
            return this;
        }

        public Builder setLocalizedDescription(EnumMap<LanguageCode, String> localizedDescription) {
            this.localizedDescription = localizedDescription;
            return this;
        }

        public Role.Builder setAuthor(long author) {
            this.author = author;
            return this;
        }

        public Role.Builder setRegDate(ZonedDateTime regDate) {
            this.regDate = regDate;
            return this;
        }

        public Role.Builder setLastModifiedDate(ZonedDateTime lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
            return this;
        }

        public Role.Builder setLastModifier(long lastModifier) {
            this.lastModifier = lastModifier;
            return this;
        }

        public Role build() {
            Role role = new Role();
            role.setId(id);
            role.setIdentifier(identifier);
            role.setLocalizedName(localizedName);
            role.setLocalizedDescription(localizedDescription);
            role.setAuthor(author);
            role.setRegDate(regDate);
            role.setRoleType(roleType);
            role.setLastModifier(lastModifier);
            role.setLastModifiedDate(lastModifiedDate);
            return role;
        }
    }
}
