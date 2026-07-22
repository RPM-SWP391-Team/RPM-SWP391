package com.rpm.remotepatientmonitoring.config;

import com.rpm.remotepatientmonitoring.model.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final Account account;

    public CustomUserDetails(Account account) {
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getRole()));
    }

    @Override
    public String getPassword() {
        return account.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return account.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return account.getIsActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            return false;
        }
        if ("DOCTOR".equals(account.getRole()) || "HOSPITAL_ADMIN".equals(account.getRole())) {
            return true;
        }
        return Boolean.TRUE.equals(account.getIsEmailVerified());
    }

    public Account getAccount() {
        return account;
    }
}
