package com.rpm.remotepatientmonitoring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityAccessDeniedTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "patient@test.com", roles = {"PATIENT"})
    public void patientAccessesDoctorRoute_ShouldRedirectToPatientDashboard() throws Exception {
        mockMvc.perform(get("/doctor/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patient/dashboard"));
    }

    @Test
    @WithMockUser(username = "doctor@test.com", roles = {"DOCTOR"})
    public void doctorAccessesHospitalAdminRoute_ShouldRedirectToDoctorDashboard() throws Exception {
        mockMvc.perform(get("/hospital/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/dashboard"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"HOSPITAL_ADMIN"})
    public void adminAccessesPatientRoute_ShouldRedirectToHospitalAdminDashboard() throws Exception {
        mockMvc.perform(get("/patient/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/hospital/dashboard"));
    }

    @Test
    public void anonymousUserAccessesProtectedRoute_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/patient/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/auth/login"));
    }
}
