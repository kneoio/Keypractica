package com.semantyca.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.semantyca.localization.LanguageCode;
import com.semantyca.model.Application;
import com.semantyca.model.DataEntity;
import com.semantyca.model.Language;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Setter
@Getter
@NoArgsConstructor
public class User extends DataEntity<Long> {
    private Long identifier;
    @NotBlank
    private String login;
    @JsonIgnore
    @NotBlank
    private String pwd;
    private String email;
    private List<Application> applications = new ArrayList<>();
    @JsonIgnore
    boolean authorized;
    private List<String> roles = new ArrayList<>();
    private Language defaultLang;
    private TimeZone timeZone;

    public static class Builder {
        private String login;
        private String pwd = "123";
        private String email;
        private TimeZone timeZone = TimeZone.getDefault();
        private List<String> roles;
        private Language defaultLang = new Language.Builder().build();

        private List<Application> applications = Arrays.asList(new Application.Builder().build());

        public Builder setLogin(String login) {
            this.login = login;
            return this;
        }

        public Builder setPwd(String pwd) {
            this.pwd = pwd;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setDefaultLang(String defaultLang) {
            LanguageCode code = LanguageCode.valueOf(defaultLang);
            Language language = new Language();
            language.setName(code.getLang());
            language.setLocalizedNames(Map.of(LanguageCode.ENG, LanguageCode.ENG.getLang()));
            this.defaultLang = language;
            return this;
        }

        public Builder setRoles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder setTimeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public User build() {
            User newUser = new User();
            newUser.setLogin(login);
            newUser.setPwd(pwd);
            newUser.setEmail(email);
            newUser.setRoles(roles);
            newUser.setTimeZone(timeZone);
            newUser.setApplications(applications);
            newUser.setDefaultLang(defaultLang);
            return newUser;
        }
    }

}
