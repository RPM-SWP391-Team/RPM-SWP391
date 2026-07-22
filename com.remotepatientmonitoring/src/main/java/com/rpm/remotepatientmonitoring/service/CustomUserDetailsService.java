package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cleanUsername = username != null ? username.trim() : "";
        Account account = accountRepository.findByEmail(cleanUsername)
                .or(() -> {
                    if (!cleanUsername.contains("@")) {
                        return accountRepository.findByEmail(cleanUsername + "@gmail.com");
                    }
                    return java.util.Optional.empty();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Account not found with email: " + cleanUsername));
        return new CustomUserDetails(account);
    }
}
