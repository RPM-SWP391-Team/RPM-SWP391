package com.rpm.remotepatientmonitoring.config;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        
        // For REST API endpoints, return standard 403 Forbidden instead of redirecting
        if (request.getRequestURI() != null && request.getRequestURI().startsWith("/api/")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            String role = "";
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                if (userDetails.getAccount() != null && userDetails.getAccount().getRole() != null) {
                    role = userDetails.getAccount().getRole().toString();
                }
            }
            if (role.isEmpty()) {
                role = auth.getAuthorities().stream()
                        .map(r -> r.getAuthority())
                        .findFirst()
                        .orElse("");
                if (role.startsWith("ROLE_")) {
                    role = role.substring(5);
                }
            }

            if ("PATIENT".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/patient/dashboard");
                return;
            } else if ("DOCTOR".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/doctor/dashboard");
                return;
            } else if ("HOSPITAL_ADMIN".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/hospital/dashboard");
                return;
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/auth/login");
    }
}
