package com.rpm.remotepatientmonitoring.security;

import com.rpm.remotepatientmonitoring.config.MockBeansConfig;
import com.rpm.remotepatientmonitoring.config.SecurityConfig;
import com.rpm.remotepatientmonitoring.controller.AuthController;
import com.rpm.remotepatientmonitoring.controller.DashboardController;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.service.AuthService;
import com.rpm.remotepatientmonitoring.service.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, DashboardController.class})
@Import({SecurityConfig.class, MockBeansConfig.class})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private AuthService authService;

    @Nested
    @DisplayName("Public URLs PermitAll Verification")
    class PublicUrls {

        @Test
        @DisplayName("Public auth endpoints are accessible without authentication")
        void publicAuthUrls_allowUnauthenticated() throws Exception {
            mockMvc.perform(get("/auth/login"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/auth/register"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/auth/verify-otp").param("email", "novakimbi1709@gmail.com"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/auth/forgot-password"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Static asset paths are accessible without authentication")
        void staticAssetUrls_allowUnauthenticated() throws Exception {
            mockMvc.perform(get("/css/style.css"))
                    .andExpect(status().isNotFound()); // Bypasses security filter to MVC (404)

            mockMvc.perform(get("/js/app.js"))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/images/logo.png"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Unauthenticated Access Protection")
    class UnauthenticatedProtection {

        @Test
        @DisplayName("Unauthenticated request to /dashboard redirects to /auth/login")
        void dashboard_unauthenticated_redirectsToLogin() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("http://localhost/auth/login"));
        }

        @Test
        @DisplayName("Unauthenticated request to /hospital/dashboard redirects to /auth/login")
        void hospital_unauthenticated_redirectsToLogin() throws Exception {
            mockMvc.perform(get("/hospital/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("http://localhost/auth/login"));
        }

        @Test
        @DisplayName("Unauthenticated request to /doctor/dashboard redirects to /auth/login")
        void doctor_unauthenticated_redirectsToLogin() throws Exception {
            mockMvc.perform(get("/doctor/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("http://localhost/auth/login"));
        }

        @Test
        @DisplayName("Unauthenticated request to /patient/dashboard redirects to /auth/login")
        void patient_unauthenticated_redirectsToLogin() throws Exception {
            mockMvc.perform(get("/patient/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("http://localhost/auth/login"));
        }
    }

    @Nested
    @DisplayName("Role-Based Authorization (RBAC) Isolation")
    class RoleBasedAccessControl {

        @Test
        @WithMockCustomUser(username = "admin890@gmail.com", role = "HOSPITAL_ADMIN")
        @DisplayName("HOSPITAL_ADMIN (admin890@gmail.com) can access /dashboard and is redirected to /hospital/dashboard by CustomAccessDeniedHandler on unauthorized pages")
        void hospitalAdmin_rbac_permissions() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/hospital/dashboard"));

            // Unauthorized page access for HOSPITAL_ADMIN redirects to /hospital/dashboard via CustomAccessDeniedHandler
            mockMvc.perform(get("/doctor/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/hospital/dashboard"));

            mockMvc.perform(get("/patient/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/hospital/dashboard"));

            // REST API endpoint returns 403 Forbidden for unauthorized role
            mockMvc.perform(get("/api/doctor/data"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockCustomUser(username = "bacsi890@rpm.com", role = "DOCTOR")
        @DisplayName("DOCTOR (bacsi890@rpm.com) can access /dashboard and is redirected to /doctor/dashboard by CustomAccessDeniedHandler on unauthorized pages")
        void doctor_rbac_permissions() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/doctor/dashboard"));

            mockMvc.perform(get("/hospital/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/doctor/dashboard"));

            mockMvc.perform(get("/patient/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/doctor/dashboard"));

            mockMvc.perform(get("/api/patient/data"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockCustomUser(username = "novakimbi1709@gmail.com", role = "PATIENT")
        @DisplayName("PATIENT (novakimbi1709@gmail.com) can access /dashboard and is redirected to /patient/appointments by CustomAccessDeniedHandler on unauthorized pages")
        void patient_rbac_permissions() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/patient/appointments"));

            mockMvc.perform(get("/hospital/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/patient/dashboard"));

            mockMvc.perform(get("/doctor/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/patient/dashboard"));

            mockMvc.perform(get("/api/hospital/data"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Authentication Handlers & Logout")
    class AuthHandlersAndLogout {

        @Test
        @DisplayName("Form login failure with wrong credentials redirects with error=true")
        void loginFailure_badCredentials_redirectsWithError() throws Exception {
            when(customUserDetailsService.loadUserByUsername(anyString()))
                    .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

            mockMvc.perform(formLogin("/auth/login")
                            .user("username", "novakimbi1709@gmail.com")
                            .password("password", "wrongpass"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/login?error=true&username=novakimbi1709%40gmail.com"));
        }

        @Test
        @DisplayName("Form login failure with unverified email (disabled account) redirects with disabled=true")
        void loginFailure_disabledAccount_redirectsWithDisabled() throws Exception {
            Account disabledAccount = Account.builder()
                    .id(2)
                    .email("unverified@example.com")
                    .passwordHash("hashedPass")
                    .role("PATIENT")
                    .isEmailVerified(false)
                    .isActive(true)
                    .build();

            CustomUserDetails disabledUserDetails = new CustomUserDetails(disabledAccount);

            when(customUserDetailsService.loadUserByUsername("unverified@example.com"))
                    .thenReturn(disabledUserDetails);

            mockMvc.perform(formLogin("/auth/login")
                            .user("username", "unverified@example.com")
                            .password("password", "hashedPass"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/login?disabled=true&username=unverified%40example.com"));
        }

        @Test
        @DisplayName("Form login with NEW password after reset succeeds and redirects to /dashboard")
        void loginSuccess_withNewPasswordAfterReset_redirectsToDashboard() throws Exception {
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

            Account resetAccount = Account.builder()
                    .id(1)
                    .email("resetuser@example.com")
                    .passwordHash(encoder.encode("NewPassword123"))
                    .role("PATIENT")
                    .isEmailVerified(true)
                    .isActive(true)
                    .build();

            CustomUserDetails userDetails = new CustomUserDetails(resetAccount);

            when(customUserDetailsService.loadUserByUsername("resetuser@example.com"))
                    .thenReturn(userDetails);

            mockMvc.perform(formLogin("/auth/login")
                            .user("username", "resetuser@example.com")
                            .password("password", "NewPassword123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }

        @Test
        @DisplayName("Form login with OLD password after reset fails and redirects to login with error")
        void loginFailure_withOldPasswordAfterReset_redirectsWithError() throws Exception {
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

            Account resetAccount = Account.builder()
                    .id(1)
                    .email("resetuser@example.com")
                    .passwordHash(encoder.encode("NewPassword123"))
                    .role("PATIENT")
                    .isEmailVerified(true)
                    .isActive(true)
                    .build();

            CustomUserDetails userDetails = new CustomUserDetails(resetAccount);

            when(customUserDetailsService.loadUserByUsername("resetuser@example.com"))
                    .thenReturn(userDetails);

            mockMvc.perform(formLogin("/auth/login")
                            .user("username", "resetuser@example.com")
                            .password("password", "OldPassword123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/login?error=true&username=resetuser%40example.com"));
        }

        @Test
        @DisplayName("Logout request invalidates session and redirects to /auth/login?logout=true")
        void logout_success_redirectsToLoginWithLogoutParam() throws Exception {
            mockMvc.perform(logout("/auth/logout"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/login?logout=true"));
        }
    }
}
