package com.semantyca.core.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.DataEntity;
import com.semantyca.core.model.Module;
import com.semantyca.core.server.EnvConst;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

@Setter
@Getter
@NoArgsConstructor
public class User extends DataEntity<Long> implements IUser{
    private Long id;
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
    private Integer pageSize = EnvConst.DEFAULT_PAGE_SIZE;
    private Integer defaultLang;
    private TimeZone timeZone;

    @Override
    public Long getUserId() {
        return id;
    }
    @Override
    public String getUserName() {
        return login;
    }

    public static class Builder {
        private Long id;
        private String login;
        private String pwd = "123";
        private String email;
        private TimeZone timeZone = TimeZone.getDefault();
        private List<String> roles;
        private Integer defaultLang = LanguageCode.ENG.getCode();
        private final List<Module> modules = Collections.singletonList(new Module.Builder().build());


        public Builder setId(long id) {
            this.id = id;
            return this;
        }

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
            newUser.setId(id);
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
