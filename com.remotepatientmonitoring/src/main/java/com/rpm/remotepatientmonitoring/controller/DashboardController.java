package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String role = userDetails.getAccount().getRole().toString();
        switch (role) {
            case "HOSPITAL_ADMIN": return "redirect:/hospital/dashboard";
            case "DOCTOR": return "redirect:/doctor/dashboard";
            case "PATIENT": return "redirect:/patient/dashboard";
            default: return "redirect:/auth/login";
        }
    }
}