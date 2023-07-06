package com.semantyca.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.semantyca.localization.LanguageCode;
import com.semantyca.model.DataEntity;
import com.semantyca.model.Module;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@Setter
@Getter
@NoArgsConstructor
public class User extends DataEntity<Long> {
    @NotBlank
    private String login;
    @JsonIgnore
    @NotBlank
    private String pwd;
    private String email;
    private List<Module> modules = new ArrayList<>();
    @JsonIgnore
    boolean authorized;
    private List<String> roles = new ArrayList<>();
    private Integer defaultLang;
    private TimeZone timeZone;

    public static class Builder {
        private String login;
        private String pwd = "123";
        private String email;
        private TimeZone timeZone = TimeZone.getDefault();
        private List<String> roles;
        private Integer defaultLang = LanguageCode.ENG.getCode();

        private List<Module> modules = Arrays.asList(new Module.Builder().build());

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

        public Builder setDefaultLang(Integer defaultLang) {
            this.defaultLang = defaultLang;
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
            newUser.setModules(modules);
            newUser.setDefaultLang(defaultLang);
            return newUser;
        }
    }

}
