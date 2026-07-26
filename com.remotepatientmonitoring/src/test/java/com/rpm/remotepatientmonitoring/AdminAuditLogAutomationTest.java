package com.rpm.remotepatientmonitoring;

import com.rpm.remotepatientmonitoring.model.AuditTrail;
import com.rpm.remotepatientmonitoring.model.SystemLog;
import com.rpm.remotepatientmonitoring.repository.AuditTrailRepository;
import com.rpm.remotepatientmonitoring.repository.SystemLogRepository;
import com.rpm.remotepatientmonitoring.service.hospital.AuditLogMaskingService;
import com.rpm.remotepatientmonitoring.service.hospital.ExcelExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminAuditLogAutomationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private SystemLogRepository systemLogRepository;

    @Autowired
    private AuditLogMaskingService auditLogMaskingService;

    @Autowired
    private ExcelExportService excelExportService;

    private AuditTrail doctorAudit;
    private AuditTrail patientAudit;
    private AuditTrail adminAudit;
    private SystemLog ocrSystemLog;

    @BeforeEach
    public void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Log Bác sĩ
        doctorAudit = auditTrailRepository.save(AuditTrail.builder()
                .actorType("DOCTOR")
                .actorId(5)
                .action("CREATE_TREATMENT_PLAN")
                .targetTable("treatment_plans")
                .targetRecordId(15)
                .oldValue(null)
                .newValue("{\"systolic_bp\":140, \"glucose_level\":8.5, \"diagnosis\":\"Tăng huyết áp độ 1\"}")
                .ipAddress("192.168.1.50")
                .deviceInfo("Chrome 120 / Windows 11")
                .notes("Bác sĩ Nguyễn Văn A lập phác đồ mới cho bệnh nhân B")
                .createdAt(now.minusMinutes(30))
                .build());

        // 2. Log Bệnh nhân
        patientAudit = auditTrailRepository.save(AuditTrail.builder()
                .actorType("PATIENT")
                .actorId(102)
                .action("LOG_DAILY_VITAL")
                .targetTable("daily_health_logs")
                .targetRecordId(88)
                .oldValue(null)
                .newValue("{\"systolic_bp\":135, \"diastolic_bp\":85, \"glucose_level\":6.2, \"patient_notes\":\"Bình thường\"}")
                .ipAddress("118.69.10.20")
                .deviceInfo("Mobile Safari / iPhone 15")
                .notes("Bệnh nhân Trần Văn B nhập chỉ số đo hằng ngày qua OCR")
                .createdAt(now.minusMinutes(15))
                .build());

        // 3. Log Admin
        adminAudit = auditTrailRepository.save(AuditTrail.builder()
                .actorType("HOSPITAL_ADMIN")
                .actorId(1)
                .action("SET_HOSPITAL_THRESHOLD")
                .targetTable("alert_thresholds")
                .targetRecordId(1)
                .oldValue("{\"glucose_max\":10.0}")
                .newValue("{\"glucose_max\":10.5}")
                .ipAddress("127.0.0.1")
                .deviceInfo("Edge 120 / Windows 11")
                .notes("Cập nhật ngưỡng cảnh báo toàn bệnh viện")
                .createdAt(now.minusMinutes(5))
                .build());

        // 4. System Log lỗi hạ tầng
        ocrSystemLog = systemLogRepository.save(SystemLog.builder()
                .logLevel("ERROR")
                .moduleName("OCR_VISION_SERVICE")
                .eventCode("OCR_BLURRY_IMAGE")
                .message("Google Cloud Vision API không đọc được ảnh do bị mờ góc")
                .relatedPatientId(102)
                .createdAt(now.minusMinutes(10))
                .build());
    }

    @Test
    @DisplayName("Test 1: Lọc Audit Log theo ActorType (DOCTOR, PATIENT, HOSPITAL_ADMIN)")
    public void testAuditLogFilteringByActorType() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Page<AuditTrail> doctorPage = auditTrailRepository.filterAuditLogs("DOCTOR", null, null, start, end, PageRequest.of(0, 10));
        assertTrue(doctorPage.getContent().stream().allMatch(a -> "DOCTOR".equals(a.getActorType())));
        assertTrue(doctorPage.getContent().stream().anyMatch(a -> a.getId().equals(doctorAudit.getId())));

        Page<AuditTrail> patientPage = auditTrailRepository.filterAuditLogs("PATIENT", null, null, start, end, PageRequest.of(0, 10));
        assertTrue(patientPage.getContent().stream().allMatch(a -> "PATIENT".equals(a.getActorType())));
        assertTrue(patientPage.getContent().stream().anyMatch(a -> a.getId().equals(patientAudit.getId())));

        Page<AuditTrail> adminPage = auditTrailRepository.filterAuditLogs("HOSPITAL_ADMIN", null, null, start, end, PageRequest.of(0, 10));
        assertTrue(adminPage.getContent().stream().allMatch(a -> "HOSPITAL_ADMIN".equals(a.getActorType())));
        assertTrue(adminPage.getContent().stream().anyMatch(a -> a.getId().equals(adminAudit.getId())));
    }

    @Test
    @DisplayName("Test 2: Lọc Audit Log theo Action và Từ khóa Tìm kiếm (Keyword Search)")
    public void testAuditLogFilteringByActionAndKeyword() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Page<AuditTrail> actionPage = auditTrailRepository.filterAuditLogs(null, "CREATE_TREATMENT_PLAN", null, start, end, PageRequest.of(0, 10));
        assertEquals(1, actionPage.getContent().size());
        assertEquals("CREATE_TREATMENT_PLAN", actionPage.getContent().get(0).getAction());

        Page<AuditTrail> keywordPage = auditTrailRepository.filterAuditLogs(null, null, "118.69.10.20", start, end, PageRequest.of(0, 10));
        assertEquals(1, keywordPage.getContent().size());
        assertEquals("118.69.10.20", keywordPage.getContent().get(0).getIpAddress());
    }

    @Test
    @DisplayName("Test 3: Bảo mật Che mờ Dữ liệu Y tế Nhạy cảm cho Admin (Mục 1.4 BRD)")
    public void testMedicalDataMaskingForAdmin() {
        AuditTrail maskedDoctorLog = auditLogMaskingService.maskAuditTrailForAdmin(doctorAudit);
        assertNotNull(maskedDoctorLog);
        assertTrue(maskedDoctorLog.getNewValue().contains("***MASKED***"));
        assertFalse(maskedDoctorLog.getNewValue().contains("Tăng huyết áp độ 1"));

        AuditTrail maskedPatientLog = auditLogMaskingService.maskAuditTrailForAdmin(patientAudit);
        assertNotNull(maskedPatientLog);
        assertTrue(maskedPatientLog.getNewValue().contains("***MASKED***"));

        // Thông tin quản trị không bị che mờ
        assertEquals("DOCTOR", maskedDoctorLog.getActorType());
        assertEquals("CREATE_TREATMENT_PLAN", maskedDoctorLog.getAction());
        assertEquals("192.168.1.50", maskedDoctorLog.getIpAddress());
    }

    @Test
    @DisplayName("Test 4: Tab 2 System Logs - Lọc Sự cố Kỹ thuật Hạ tầng theo Cấp độ và Module")
    public void testSystemLogFiltering() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Page<SystemLog> logs = systemLogRepository.filterSystemLogs("ERROR", "OCR_VISION_SERVICE", start, end, PageRequest.of(0, 10));
        assertFalse(logs.isEmpty());
        assertEquals("ERROR", logs.getContent().get(0).getLogLevel());
        assertEquals("OCR_VISION_SERVICE", logs.getContent().get(0).getModuleName());
        assertEquals("OCR_BLURRY_IMAGE", logs.getContent().get(0).getEventCode());
    }

    @Test
    @DisplayName("Test 5: Xuất Báo cáo File Excel (.csv) Kiểm toán UTF-8 BOM")
    public void testExcelCsvExportService() {
        List<AuditTrail> logs = List.of(doctorAudit, patientAudit, adminAudit);
        ByteArrayInputStream in = excelExportService.exportAuditLogsToCsv(logs);
        assertNotNull(in);

        byte[] bytes = in.readAllBytes();
        assertTrue(bytes.length > 0);

        // Kiểm tra UTF-8 BOM header (0xEF, 0xBB, 0xBF)
        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);

        String csvText = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csvText.contains("STT,Thời Gian,Vai Trò"));
        assertTrue(csvText.contains("DOCTOR"));
        assertTrue(csvText.contains("PATIENT"));
        assertTrue(csvText.contains("HOSPITAL_ADMIN"));
    }

    @Test
    @WithMockUser(username = "admin@hospital.com", roles = {"HOSPITAL_ADMIN"})
    @DisplayName("Test 6: Giao diện Controller GET /hospital/audit-logs")
    public void testAdminAuditLogControllerUI() throws Exception {
        mockMvc.perform(get("/hospital/audit-logs")
                        .param("actorType", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(view().name("hospital/audit_logs"))
                .andExpect(model().attributeExists("logs"));
    }

    @Test
    @WithMockUser(username = "admin@hospital.com", roles = {"HOSPITAL_ADMIN"})
    @DisplayName("Test 7: Download File Báo cáo GET /hospital/audit-logs/export")
    public void testAdminAuditLogExportEndpoint() throws Exception {
        mockMvc.perform(get("/hospital/audit-logs/export")
                        .param("actorType", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(header().string(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment; filename=\"NhatKy_KiemToan_")))
                .andExpect(content().contentType("text/csv; charset=UTF-8"));
    }
}
