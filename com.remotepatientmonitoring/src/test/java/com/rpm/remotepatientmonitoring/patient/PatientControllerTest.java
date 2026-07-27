package com.rpm.remotepatientmonitoring.patient;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.config.MockBeansConfig;
import com.rpm.remotepatientmonitoring.controller.patient.PatientController;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.service.patient.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MockBeansConfig.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Account sampleAccount;
    private Patient samplePatient;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        sampleAccount = Account.builder()
                .id(1)
                .email("patient@example.com")
                .passwordHash("hashedOldPassword")
                .role("PATIENT")
                .isEmailVerified(true)
                .isActive(true)
                .build();

        samplePatient = Patient.builder()
                .id(10)
                .account(sampleAccount)
                .fullName("Nguyen Van A")
                .phone("0912345678")
                .build();

        userDetails = new CustomUserDetails(sampleAccount);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(patientService.findByAccountId(1)).thenReturn(Optional.of(samplePatient));
    }

    @Nested
    @DisplayName("Patient Profile Update & Password Change")
    class ProfilePasswordManagement {

        @Test
        @DisplayName("POST /patient/progress/update fails when changing password without entering current password")
        void updateProfile_passwordChangeWithoutCurrentPassword_returnsError() throws Exception {
            mockMvc.perform(post("/patient/progress/update")
                            .param("phone", "0912345678")
                            .param("address", "123 Main St")
                            .param("password", "NewPassword123")
                            .param("currentPassword", ""))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/patient/progress"))
                    .andExpect(flash().attribute("error", "Vui lòng nhập mật khẩu hiện tại để xác nhận đổi mật khẩu mới!"));

            verify(patientService, never()).updateProfile(any(), any(), any(), any(), any(), any());
        }
    }
}
