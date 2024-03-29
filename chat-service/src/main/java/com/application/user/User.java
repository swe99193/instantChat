package com.application.user;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
// lombok: https://projectlombok.org/features/GetterSetter

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
//import javax.persistence.*;
// Note: javax deprecated, use jakarta instead
// see: https://stackoverflow.com/questions/73350585/upgrade-from-spring-boot-2-7-2-to-spring-boot-3-0-0-snapshot-java-package-java
import java.util.Collection;

// TODO: remove this file, import from authentication service instead

@Getter
@Setter
@NoArgsConstructor
// Note: UserDetails is a magic behind spring security
public class User implements UserDetails {

    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}