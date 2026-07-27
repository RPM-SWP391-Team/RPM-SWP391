package com.rpm.remotepatientmonitoring.bugreport;

import com.rpm.remotepatientmonitoring.config.MockBeansConfig;
import com.rpm.remotepatientmonitoring.controller.BugReportRestController;
import com.rpm.remotepatientmonitoring.controller.hospital.HospitalBugReportController;
import com.rpm.remotepatientmonitoring.model.BugReport;
import com.rpm.remotepatientmonitoring.security.WithMockCustomUser;
import com.rpm.remotepatientmonitoring.service.BugReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {BugReportRestController.class, HospitalBugReportController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(MockBeansConfig.class)
class BugReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BugReportService bugReportService;

    @Autowired
    private HospitalBugReportController hospitalBugReportController;

    private MockMvc standaloneMockMvc;

    @BeforeEach
    void setUpStandalone() {
        reset(bugReportService);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");

        standaloneMockMvc = MockMvcBuilders.standaloneSetup(hospitalBugReportController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Nested
    @DisplayName("REST API - Submit Bug Report")
    class RestApiSubmit {

        @Test
        @WithMockCustomUser(username = "patient@example.com", role = "PATIENT")
        @DisplayName("POST /api/bug-reports returns 200 OK on valid report submission")
        void submitBugReport_success() throws Exception {
            String jsonRequest = "{\"title\":\"Lỗi nút lưu\",\"description\":\"Nút lưu không phản hồi khi bấm\",\"pageUrl\":\"/patient/log\"}";

            mockMvc.perform(post("/api/bug-reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Báo cáo lỗi của bạn đã được gửi thành công. Cảm ơn sự đóng góp của bạn!"));

            verify(bugReportService).submitBugReport(eq("Lỗi nút lưu"), eq("Nút lưu không phản hồi khi bấm"), eq("/patient/log"), any());
        }

        @Test
        @WithMockCustomUser(username = "patient@example.com", role = "PATIENT")
        @DisplayName("POST /api/bug-reports returns 400 Bad Request on validation or duplicate error")
        void submitBugReport_validationFailure_returnsBadRequest() throws Exception {
            doThrow(new IllegalArgumentException("Tiêu đề không được để trống."))
                    .when(bugReportService).submitBugReport(any(), any(), any(), any());

            String jsonRequest = "{\"title\":\"\",\"description\":\"Mô tả\",\"pageUrl\":\"/patient/log\"}";

            mockMvc.perform(post("/api/bug-reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Tiêu đề không được để trống."));
        }
    }

    @Nested
    @DisplayName("Admin Controller - Bug Report Management")
    class AdminBugReportManagement {

        @Test
        @DisplayName("GET /hospital/bug-reports renders list view with pagination and filters")
        void listBugReports_rendersView() throws Exception {
            when(bugReportService.getBugReports(any(), any())).thenReturn(new PageImpl<>(List.of(new BugReport())));
            when(bugReportService.getBannedWords()).thenReturn(List.of("đm", "vcl"));

            standaloneMockMvc.perform(get("/hospital/bug-reports"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("hospital/bug-reports"))
                    .andExpect(model().attributeExists("bugReports"))
                    .andExpect(model().attributeExists("bannedWords"));
        }

        @Test
        @DisplayName("POST /hospital/bug-reports/update/{id} updates bug status and redirects")
        void updateBugReport_success_redirects() throws Exception {
            standaloneMockMvc.perform(post("/hospital/bug-reports/update/10")
                            .param("status", "RESOLVED")
                            .param("adminResponse", "Đã xử lý xong lỗi này.")
                            .param("page", "0")
                            .param("statusFilter", "ALL"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/hospital/bug-reports?page=0&status=ALL"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(bugReportService).updateBugStatus(eq(10), eq("RESOLVED"), eq("Đã xử lý xong lỗi này."), any());
        }
    }
}
