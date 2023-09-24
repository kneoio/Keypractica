package com.semantyca.core.model.user;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.SimpleReferenceEntity;
import com.semantyca.core.model.cnst.RoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class Role extends SimpleReferenceEntity {

    private RoleType roleType;

    @Getter
    private Map<LanguageCode, String> localizedDescr;

    public void setLocalizedDescr(Map<LanguageCode, String> localizedDescr) {
        this.localizedDescr = localizedDescr;
    }

    public static class Builder {
        protected UUID id;
        protected String identifier;
        private Map<LanguageCode, String> localizedDescr = new HashMap<>();
        private final Map<LanguageCode, String> localizedName = new HashMap<>();
        private long author;
        private ZonedDateTime regDate;
        private ZonedDateTime lastModifiedDate;
        private long lastModifier;
        private final RoleType roleType = RoleType.CUSTOM;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Role.Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Role.Builder setLocalizedName(Map<LanguageCode, String> locName) {
            this.localizedDescr = locName;
            return this;
        }

        public Builder setLocalizedDescr(Map<LanguageCode, String> localizedDescr) {
            this.localizedDescr = localizedDescr;
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
            role.setLocalizedDescr(localizedDescr);
            role.setAuthor(author);
            role.setRegDate(regDate);
            role.setRoleType(roleType);
            role.setLastModifier(lastModifier);
            role.setLastModifiedDate(lastModifiedDate);
            return role;
        }
    }
}
