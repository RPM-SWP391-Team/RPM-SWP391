package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalHeaderAdvice {

    @Autowired
    private AccountRepository accountRepository;

    @ModelAttribute
    public void addGlobalUserAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Optional<Account> opt = accountRepository.findById(userDetails.getAccount().getId());
                if (opt.isPresent()) {
                    Account account = opt.get();
                    model.addAttribute("currentAccount", account);
                }
            }
        }
    }
}
