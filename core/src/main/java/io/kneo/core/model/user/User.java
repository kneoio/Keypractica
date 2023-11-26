package io.kneo.core.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.kneo.core.dto.cnst.UserRegStatus;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.DataEntity;
import io.kneo.core.model.Module;
import io.kneo.core.server.EnvConst;
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
public class User extends DataEntity<Long> implements IUser {
    @NotBlank
    private String login;
    @JsonIgnore
    @NotBlank
    private String email;
    private boolean isSupervisor;
    private List<Module> modules = new ArrayList<>();
    private List<Role> roles = new ArrayList<>();
    private Integer pageSize = EnvConst.DEFAULT_PAGE_SIZE;
    private Integer defaultLang;
    private TimeZone timeZone;
    private UserRegStatus regStatus;
    private int confirmationCode;
    @Override
    public String getUserName() {
        return login;
    }
    @Override
    public Integer getPageSize() {
        return IUser.super.getPageSize();
    }
    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public List<IRole> getActivatedRoles() {
        return new ArrayList<>(roles);
    }

    public static class Builder {
        private String login;
        private String email;
        private boolean isSupervisor;
        private TimeZone timeZone = TimeZone.getDefault();
        private List<Role> roles;
        private Integer defaultLang = LanguageCode.ENG.getCode();
        private List<Module> modules = Collections.singletonList(new Module.Builder().build());
        private UserRegStatus regStatus;
        private int confirmationCode;

        public Builder setLogin(String login) {
            this.login = login;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setSupervisor(boolean supervisor) {
            isSupervisor = supervisor;
            return this;
        }

        public Builder setModules(List<Module> modules) {
            this.modules = modules;
            return this;
        }

        public Builder setDefaultLang(Integer defaultLang) {
            this.defaultLang = defaultLang;
            return this;
        }

        public Builder setRoles(List<Role> roles) {
            this.roles = roles;
            return this;
        }

        public Builder setTimeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public Builder setRegStatus(UserRegStatus regStatus) {
            this.regStatus = regStatus;
            return this;
        }

        public Builder setConfirmationCode(int confirmationCode) {
            this.confirmationCode = confirmationCode;
            return this;
        }


        public User build() {
            User doc = new User();
            doc.setLogin(login);
            doc.setEmail(email);
            doc.setSupervisor(isSupervisor);
            doc.setRoles(roles);
            doc.setTimeZone(timeZone);
            doc.setModules(modules);
            doc.setDefaultLang(defaultLang);
            doc.setRegStatus(regStatus);
            doc.setConfirmationCode(confirmationCode);
            doc.setSupervisor(isSupervisor);
            return doc;
        }



    }

}
