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
public class Role extends SimpleReferenceEntity implements IRole {

    private RoleType roleType;

    private Map<LanguageCode, String> localizedDescr;
    public Map<LanguageCode, String> getLocalizedDescr() {
        return localizedDescr;
    }
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
            Role newNode = new Role();
            newNode.setId(id);
            newNode.setIdentifier(identifier);
            newNode.setLocName(localizedName);
            newNode.setLocalizedDescr(localizedDescr);
            newNode.setAuthor(author);
            newNode.setRegDate(regDate);
            newNode.setRoleType(roleType);
            newNode.setLastModifier(lastModifier);
            newNode.setLastModifiedDate(lastModifiedDate);
            return newNode;
        }
    }
}
