package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.BugReport;
import com.rpm.remotepatientmonitoring.repository.BugReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BugReportServiceTest {

    @InjectMocks
    private BugReportService bugReportService;

    @Mock
    private BugReportRepository bugReportRepository;

    private Account reporterAccount;
    private Account adminAccount;

    @BeforeEach
    void setUp() {
        bugReportService.init();

        reporterAccount = new Account();
        reporterAccount.setId(1);
        reporterAccount.setEmail("reporter@rpm.vn");

        adminAccount = new Account();
        adminAccount.setId(2);
        adminAccount.setEmail("admin@rpm.vn");
    }

    @Test
    void testSubmitBugReport_Success() {
        String title = "Lỗi kết nối Bluetooth";
        String description = "Không thể kết nối với máy đo huyết áp Omron qua Bluetooth.";
        String pageUrl = "http://localhost:8080/patient/log";

        BugReport mockReport = BugReport.builder()
                .id(101)
                .account(reporterAccount)
                .title(title)
                .description(description)
                .pageUrl(pageUrl)
                .status("NEW")
                .build();

        when(bugReportRepository.save(any(BugReport.class))).thenReturn(mockReport);

        BugReport result = bugReportService.submitBugReport(title, description, pageUrl, reporterAccount);

        assertNotNull(result);
        assertEquals(101, result.getId());
        assertEquals("NEW", result.getStatus());
        assertEquals(title, result.getTitle());
        verify(bugReportRepository, times(1)).save(any(BugReport.class));
    }

    @Test
    void testSubmitBugReport_EmptyTitleOrDescription_ThrowsException() {
        // Empty title
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bugReportService.submitBugReport("   ", "Mô tả hợp lệ", "http://page.com", reporterAccount);
        });
        assertEquals("Tiêu đề không được để trống.", exception.getMessage());

        // Null title
        exception = assertThrows(IllegalArgumentException.class, () -> {
            bugReportService.submitBugReport(null, "Mô tả hợp lệ", "http://page.com", reporterAccount);
        });
        assertEquals("Tiêu đề không được để trống.", exception.getMessage());

        // Empty description
        exception = assertThrows(IllegalArgumentException.class, () -> {
            bugReportService.submitBugReport("Tiêu đề hợp lệ", "   ", "http://page.com", reporterAccount);
        });
        assertEquals("Mô tả chi tiết không được để trống.", exception.getMessage());

        // Title too long (> 255 chars)
        StringBuilder longTitle = new StringBuilder();
        for (int i = 0; i < 260; i++) {
            longTitle.append("a");
        }
        exception = assertThrows(IllegalArgumentException.class, () -> {
            bugReportService.submitBugReport(longTitle.toString(), "Mô tả hợp lệ", "http://page.com", reporterAccount);
        });
        assertEquals("Tiêu đề không được vượt quá 255 ký tự.", exception.getMessage());
    }

    @Test
    void testSubmitBugReport_BannedWords_ThrowsException() {
        // Title contains banned word "vcl"
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bugReportService.submitBugReport("Lỗi vcl luôn", "Mô tả hợp lệ", "http://page.com", reporterAccount);
        });
        assertEquals("Nội dung chứa từ ngữ không phù hợp. Vui lòng chỉnh sửa lại.", exception.getMessage());

        // Description contains banned word "đm"
        exception = assertThrows(IllegalArgumentException.class, () -> {
            bugReportService.submitBugReport("Lỗi hiển thị", "Mô tả lỗi này đm admin", "http://page.com", reporterAccount);
        });
        assertEquals("Nội dung chứa từ ngữ không phù hợp. Vui lòng chỉnh sửa lại.", exception.getMessage());
    }

    @Test
    void testUpdateBugStatus_ToResolved_SetsResolvedAtAndResolvedBy() {
        BugReport report = BugReport.builder()
                .id(1)
                .account(reporterAccount)
                .title("Lỗi giao diện")
                .description("Lỗi nút lệch")
                .status("NEW")
                .build();

        when(bugReportRepository.findById(1)).thenReturn(Optional.of(report));
        when(bugReportRepository.save(any(BugReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BugReport updated = bugReportService.updateBugStatus(1, "RESOLVED", "Đã fix", adminAccount);

        assertEquals("RESOLVED", updated.getStatus());
        assertEquals("Đã fix", updated.getAdminResponse());
        assertNotNull(updated.getResolvedAt());
        assertEquals(adminAccount, updated.getResolvedBy());
        verify(bugReportRepository, times(1)).save(report);
    }

    @Test
    void testUpdateBugStatus_ToOtherThanResolved_ClearsResolvedInfo() {
        BugReport report = BugReport.builder()
                .id(1)
                .account(reporterAccount)
                .title("Lỗi giao diện")
                .description("Lỗi nút lệch")
                .status("RESOLVED")
                .resolvedBy(adminAccount)
                .resolvedAt(java.time.LocalDateTime.now())
                .build();

        when(bugReportRepository.findById(1)).thenReturn(Optional.of(report));
        when(bugReportRepository.save(any(BugReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BugReport updated = bugReportService.updateBugStatus(1, "IN_PROGRESS", "Đang nghiên cứu", adminAccount);

        assertEquals("IN_PROGRESS", updated.getStatus());
        assertEquals("Đang nghiên cứu", updated.getAdminResponse());
        assertNull(updated.getResolvedAt());
        assertNull(updated.getResolvedBy());
        verify(bugReportRepository, times(1)).save(report);
    }

    @Test
    void testHasDuplicateRecentReport_CaseDuplicateWithin5Minutes_ReturnsTrue() {
        String title = "Lỗi kết nối Bluetooth";
        when(bugReportRepository.existsDuplicateRecentReport(eq(reporterAccount.getId()), eq(title.trim()), any(java.time.LocalDateTime.class)))
                .thenReturn(true);

        boolean result = bugReportService.hasDuplicateRecentReport(reporterAccount.getId(), title);
        assertTrue(result);
    }

    @Test
    void testHasDuplicateRecentReport_CaseDuplicateAfter5Minutes_ReturnsFalse() {
        String title = "Lỗi kết nối Bluetooth";
        when(bugReportRepository.existsDuplicateRecentReport(eq(reporterAccount.getId()), eq(title.trim()), any(java.time.LocalDateTime.class)))
                .thenReturn(false);

        boolean result = bugReportService.hasDuplicateRecentReport(reporterAccount.getId(), title);
        assertFalse(result);
    }

    @Test
    void testHasDuplicateRecentReport_CaseDifferentTitle_ReturnsFalse() {
        String title = "Lỗi hiển thị";
        when(bugReportRepository.existsDuplicateRecentReport(eq(reporterAccount.getId()), eq(title.trim()), any(java.time.LocalDateTime.class)))
                .thenReturn(false);

        boolean result = bugReportService.hasDuplicateRecentReport(reporterAccount.getId(), title);
        assertFalse(result);
    }

    @Test
    void testSubmitBugReport_SpamSameTitleWithin5Minutes_ThrowsException() {
        String title = "Lỗi giao diện";
        String description = "Mô tả hợp lệ";

        // Mock recent duplicate exists
        when(bugReportRepository.existsDuplicateRecentReport(eq(reporterAccount.getId()), eq(title.trim()), any(java.time.LocalDateTime.class)))
                .thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            bugReportService.submitBugReport(title, description, "http://page.com", reporterAccount);
        });

        assertEquals("Bạn vừa gửi báo cáo này rồi. Vui lòng đợi ít phút trước khi gửi lại, hoặc chỉnh sửa tiêu đề nếu đây là lỗi khác.", exception.getMessage());
    }

    @Test
    void testSubmitBugReport_SameTitleAfter5Minutes_Success() {
        String title = "Lỗi giao diện";
        String description = "Mô tả hợp lệ";

        // Mock no recent duplicate
        when(bugReportRepository.existsDuplicateRecentReport(eq(reporterAccount.getId()), eq(title.trim()), any(java.time.LocalDateTime.class)))
                .thenReturn(false);

        BugReport mockReport = BugReport.builder()
                .id(102)
                .account(reporterAccount)
                .title(title)
                .description(description)
                .status("NEW")
                .build();
        when(bugReportRepository.save(any(BugReport.class))).thenReturn(mockReport);

        BugReport result = bugReportService.submitBugReport(title, description, "http://page.com", reporterAccount);

        assertNotNull(result);
        assertEquals(102, result.getId());
    }

    @Test
    void testUpdateBugStatus_ToResolvedWithEmptyResponse_ThrowsException() {
        BugReport report = BugReport.builder()
                .id(1)
                .account(reporterAccount)
                .title("Lỗi giao diện")
                .description("Lỗi nút lệch")
                .status("NEW")
                .build();

        when(bugReportRepository.findById(1)).thenReturn(Optional.of(report));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bugReportService.updateBugStatus(1, "RESOLVED", "   ", adminAccount);
        });

        assertEquals("Vui lòng nhập ghi chú phản hồi giải trình lý do khi giải quyết hoặc từ chối báo cáo lỗi.", exception.getMessage());
    }

    @Test
    void testUpdateBugStatus_ToRejectedWithEmptyResponse_ThrowsException() {
        BugReport report = BugReport.builder()
                .id(1)
                .account(reporterAccount)
                .title("Lỗi giao diện")
                .description("Lỗi nút lệch")
                .status("NEW")
                .build();

        when(bugReportRepository.findById(1)).thenReturn(Optional.of(report));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bugReportService.updateBugStatus(1, "REJECTED", "", adminAccount);
        });

        assertEquals("Vui lòng nhập ghi chú phản hồi giải trình lý do khi giải quyết hoặc từ chối báo cáo lỗi.", exception.getMessage());
    }

    @Test
    void testUpdateBugStatus_ResponseTooLong_ThrowsException() {
        BugReport report = BugReport.builder()
                .id(1)
                .account(reporterAccount)
                .title("Lỗi giao diện")
                .description("Lỗi nút lệch")
                .status("NEW")
                .build();

        when(bugReportRepository.findById(1)).thenReturn(Optional.of(report));

        StringBuilder longResponse = new StringBuilder();
        for (int i = 0; i < 1010; i++) {
            longResponse.append("a");
        }

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bugReportService.updateBugStatus(1, "IN_PROGRESS", longResponse.toString(), adminAccount);
        });

        assertEquals("Ghi chú phản hồi của Admin không được vượt quá 1000 ký tự.", exception.getMessage());
    }

    @Test
    void testUpdateBugStatus_ToInProgressWithEmptyResponse_Success() {
        BugReport report = BugReport.builder()
                .id(1)
                .account(reporterAccount)
                .title("Lỗi giao diện")
                .description("Lỗi nút lệch")
                .status("NEW")
                .build();

        when(bugReportRepository.findById(1)).thenReturn(Optional.of(report));
        when(bugReportRepository.save(any(BugReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BugReport updated = bugReportService.updateBugStatus(1, "IN_PROGRESS", "   ", adminAccount);

        assertEquals("IN_PROGRESS", updated.getStatus());
        assertNull(updated.getAdminResponse());
    }

    @Test
    void testUpdateBugStatus_ToResolvedWithBannedWords_Success() {
        BugReport report = BugReport.builder()
                .id(1)
                .account(reporterAccount)
                .title("Lỗi giao diện")
                .description("Lỗi nút lệch")
                .status("NEW")
                .build();

        when(bugReportRepository.findById(1)).thenReturn(Optional.of(report));
        when(bugReportRepository.save(any(BugReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BugReport updated = bugReportService.updateBugStatus(1, "RESOLVED", "Đã fix vcl", adminAccount);

        assertEquals("RESOLVED", updated.getStatus());
        assertEquals("Đã fix vcl", updated.getAdminResponse());
    }
}
