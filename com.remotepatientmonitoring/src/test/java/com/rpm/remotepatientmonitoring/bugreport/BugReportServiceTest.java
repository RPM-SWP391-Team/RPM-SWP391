package com.rpm.remotepatientmonitoring.bugreport;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.BugReport;
import com.rpm.remotepatientmonitoring.repository.BugReportRepository;
import com.rpm.remotepatientmonitoring.service.BugReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BugReportServiceTest {

    @Mock
    private BugReportRepository bugReportRepository;

    @InjectMocks
    private BugReportService bugReportService;

    private Account sampleUserAccount;
    private Account sampleAdminAccount;

    @BeforeEach
    void setUp() {
        bugReportService.init();

        sampleUserAccount = Account.builder()
                .id(1)
                .email("user@example.com")
                .role("PATIENT")
                .build();

        sampleAdminAccount = Account.builder()
                .id(2)
                .email("admin@example.com")
                .role("HOSPITAL_ADMIN")
                .build();
    }

    @Nested
    @DisplayName("Submit Bug Report")
    class SubmitBugReport {

        @Test
        @DisplayName("submitBugReport creates bug report with status NEW")
        void submitBugReport_success() {
            when(bugReportRepository.existsDuplicateRecentReport(eq(1), eq("Lỗi hiển thị biểu đồ"), any()))
                    .thenReturn(false);
            when(bugReportRepository.save(any(BugReport.class))).thenAnswer(i -> i.getArgument(0));

            BugReport report = bugReportService.submitBugReport(
                    "Lỗi hiển thị biểu đồ",
                    "Biểu đồ huyết áp không hiện dữ liệu",
                    "/patient/dashboard",
                    sampleUserAccount
            );

            assertNotNull(report);
            assertEquals("Lỗi hiển thị biểu đồ", report.getTitle());
            assertEquals("Biểu đồ huyết áp không hiện dữ liệu", report.getDescription());
            assertEquals("/patient/dashboard", report.getPageUrl());
            assertEquals("NEW", report.getStatus());
            assertEquals(sampleUserAccount, report.getAccount());
        }

        @Test
        @DisplayName("submitBugReport throws exception if title is blank")
        void submitBugReport_blankTitle_throwsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    bugReportService.submitBugReport("   ", "Mô tả", "/page", sampleUserAccount));

            assertTrue(ex.getMessage().contains("Tiêu đề không được để trống"));
        }

        @Test
        @DisplayName("submitBugReport throws exception if title > 255 chars")
        void submitBugReport_longTitle_throwsException() {
            String longTitle = "a".repeat(256);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    bugReportService.submitBugReport(longTitle, "Mô tả", "/page", sampleUserAccount));

            assertTrue(ex.getMessage().contains("không được vượt quá 255 ký tự"));
        }

        @Test
        @DisplayName("submitBugReport throws exception if description is blank")
        void submitBugReport_blankDescription_throwsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    bugReportService.submitBugReport("Tiêu đề", "   ", "/page", sampleUserAccount));

            assertTrue(ex.getMessage().contains("Mô tả chi tiết không được để trống"));
        }

        @Test
        @DisplayName("submitBugReport throws exception if title or description contains banned words")
        void submitBugReport_bannedWords_throwsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    bugReportService.submitBugReport("Lỗi vcl", "Chó quá", "/page", sampleUserAccount));

            assertTrue(ex.getMessage().contains("từ ngữ không phù hợp"));
        }

        @Test
        @DisplayName("submitBugReport throws exception on duplicate report within 5 minutes")
        void submitBugReport_duplicateRecentReport_throwsException() {
            when(bugReportRepository.existsDuplicateRecentReport(eq(1), eq("Lỗi trùng"), any()))
                    .thenReturn(true);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    bugReportService.submitBugReport("Lỗi trùng", "Mô tả", "/page", sampleUserAccount));

            assertTrue(ex.getMessage().contains("Bạn vừa gửi báo cáo này rồi"));
        }
    }

    @Nested
    @DisplayName("Update Bug Report Status (Admin)")
    class UpdateBugStatus {

        @Test
        @DisplayName("updateBugStatus transitions status to IN_PROGRESS")
        void updateBugStatus_inProgress_success() {
            BugReport existingReport = BugReport.builder()
                    .id(10)
                    .title("Lỗi trang")
                    .status("NEW")
                    .build();

            when(bugReportRepository.findById(10)).thenReturn(Optional.of(existingReport));
            when(bugReportRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            BugReport updated = bugReportService.updateBugStatus(10, "IN_PROGRESS", "Đang xử lý", sampleAdminAccount);

            assertEquals("IN_PROGRESS", updated.getStatus());
            assertEquals("Đang xử lý", updated.getAdminResponse());
            assertNull(updated.getResolvedAt());
        }

        @Test
        @DisplayName("updateBugStatus transitions status to RESOLVED and records timestamp and admin")
        void updateBugStatus_resolved_success() {
            BugReport existingReport = BugReport.builder()
                    .id(10)
                    .title("Lỗi trang")
                    .status("IN_PROGRESS")
                    .build();

            when(bugReportRepository.findById(10)).thenReturn(Optional.of(existingReport));
            when(bugReportRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            BugReport updated = bugReportService.updateBugStatus(10, "RESOLVED", "Đã sửa xong lỗi", sampleAdminAccount);

            assertEquals("RESOLVED", updated.getStatus());
            assertEquals("Đã sửa xong lỗi", updated.getAdminResponse());
            assertEquals(sampleAdminAccount, updated.getResolvedBy());
            assertNotNull(updated.getResolvedAt());
        }

        @Test
        @DisplayName("updateBugStatus throws exception if status RESOLVED is missing admin response note")
        void updateBugStatus_resolvedWithoutNote_throwsException() {
            BugReport existingReport = BugReport.builder().id(10).status("NEW").build();
            when(bugReportRepository.findById(10)).thenReturn(Optional.of(existingReport));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    bugReportService.updateBugStatus(10, "RESOLVED", "   ", sampleAdminAccount));

            assertTrue(ex.getMessage().contains("ghi chú phản hồi"));
        }

        @Test
        @DisplayName("updateBugStatus throws exception for invalid status string")
        void updateBugStatus_invalidStatus_throwsException() {
            BugReport existingReport = BugReport.builder().id(10).status("NEW").build();
            when(bugReportRepository.findById(10)).thenReturn(Optional.of(existingReport));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    bugReportService.updateBugStatus(10, "UNKNOWN_STATUS", "Ghi chú", sampleAdminAccount));

            assertTrue(ex.getMessage().contains("Trạng thái không hợp lệ"));
        }
    }
}
