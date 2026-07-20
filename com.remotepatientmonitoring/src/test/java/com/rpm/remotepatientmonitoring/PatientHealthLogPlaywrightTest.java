package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PatientHealthLogPlaywrightTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DiseaseProfileRepository diseaseProfileRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    private static final String TEST_EMAIL = "novakimbi179@gmail.com";
    private static final String TEST_PASSWORD = "12345678";

    @BeforeAll
    static void launchBrowser() {
        // Khởi tạo Playwright và mở trình duyệt ở chế độ có giao diện (headless = false) để người dùng xem trực quan
        playwright = Playwright.create();
        try {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(1000) // Trễ 1 giây giữa các thao tác để dễ theo dõi
                    .setChannel("chrome"));
        } catch (Exception e) {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(1000)
                    .setChannel("msedge"));
        }
    }

    @AfterAll
    static void closeBrowser() {
        // Giải phóng tài nguyên trình duyệt sau khi hoàn thành toàn bộ test class
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUpAndLogin() {
        // Thiết lập dữ liệu kiểm thử trong database trước mỗi ca test
        setupTestData();

        // Tạo context và trang mới cho phiên test
        context = browser.newContext();
        page = context.newPage();

        // Bước 1: Điều hướng tới trang đăng nhập
        page.navigate("http://localhost:" + port + "/auth/login");

        // Bước 2: Điền thông tin đăng nhập của bệnh nhân hợp lệ
        page.fill("#username", TEST_EMAIL);
        page.fill("#password", TEST_PASSWORD);

        // Bước 3: Click nút đăng nhập
        page.click("button[type='submit']");

        // Đợi hệ thống xác thực thành công và chuyển hướng tới trang hẹn gặp
        page.waitForURL("**/patient/appointments");
    }

    @AfterEach
    void tearDown() {
        // Đóng phiên duyệt web hiện tại
        if (context != null) {
            context.close();
        }
        // Dọn dẹp sạch dữ liệu kiểm thử đã tạo để không ảnh hưởng database
        cleanupTestData();
    }

    private void setupTestData() {
        cleanupTestData();

        transactionTemplate.execute(status -> {
            // Lấy hoặc tạo Hospital mẫu để gán cho bệnh nhân
            Hospital hospital = hospitalRepository.findAll().stream().findFirst().orElseGet(() -> {
                Account hospitalAccount = Account.builder()
                        .email("hospital_admin_test@example.com")
                        .passwordHash(passwordEncoder.encode("Password123"))
                        .role("HOSPITAL_ADMIN")
                        .isEmailVerified(true)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                hospitalAccount = accountRepository.save(hospitalAccount);

                Hospital newHospital = Hospital.builder()
                        .account(hospitalAccount)
                        .hospitalCode("HOSP_BM_TEST")
                        .fullName("Bệnh viện Bạch Mai")
                        .address("Giải Phóng, Hà Nội")
                        .phone("02438693731")
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                return hospitalRepository.save(newHospital);
            });

            // Lấy hoặc tạo DiseaseProfile Hypertension (id = 1) để bệnh nhân có hồ sơ cao huyết áp
            DiseaseProfile diseaseProfile = diseaseProfileRepository.findById(1).orElseGet(() -> {
                DiseaseProfile dp = DiseaseProfile.builder()
                        .profileCode("HYPERTENSION")
                        .profileName("Cao Huyết Áp")
                        .requiresBpInput(true)
                        .requiresGlucoseInput(false)
                        .isActive(true)
                        .build();
                return diseaseProfileRepository.save(dp);
            });

            // Tạo tài khoản Account cho Bệnh nhân test
            Account account = Account.builder()
                    .email(TEST_EMAIL)
                    .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                    .role("PATIENT")
                    .isEmailVerified(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            account = accountRepository.save(account);

            // Tạo thông tin Bệnh nhân (trạng thái TREATING để được quyền nhập chỉ số sức khỏe)
            Patient patient = Patient.builder()
                    .account(account)
                    .hospital(hospital)
                    .diseaseProfile(diseaseProfile)
                    .fullName("Nguyễn Văn Kiểm Thử")
                    .dateOfBirth(LocalDate.of(1985, 10, 15))
                    .gender("MALE")
                    .phone("0987112233")
                    .address("Cầu Giấy, Hà Nội")
                    .emergencyContactName("Người Thân Kiểm Thử")
                    .emergencyContactPhone("0987445566")
                    .status("TREATING")
                    .isActive(true)
                    .onboardedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            patientRepository.save(patient);
            return null;
        });
    }

    private void cleanupTestData() {
        transactionTemplate.execute(status -> {
            accountRepository.findByEmail(TEST_EMAIL).ifPresent(account -> {
                patientRepository.findByAccountId(account.getId()).ifPresent(patient -> {
                    // Xóa các record trong medication_logs liên quan đến patient
                    entityManager.createQuery("DELETE FROM MedicationLog ml WHERE ml.patientMedication.id IN (SELECT pm.id FROM PatientMedication pm WHERE pm.patient.id = :pid)")
                            .setParameter("pid", patient.getId())
                            .executeUpdate();

                    // Xóa các record trong patient_medications liên quan đến patient
                    entityManager.createQuery("DELETE FROM PatientMedication pm WHERE pm.patient.id = :pid")
                            .setParameter("pid", patient.getId())
                            .executeUpdate();

                    // Xóa các record trong exercise_logs liên quan đến patient
                    entityManager.createQuery("DELETE FROM ExerciseLog el WHERE el.patient.id = :pid")
                            .setParameter("pid", patient.getId())
                            .executeUpdate();

                    // Xóa các record trong water_logs liên quan đến patient
                    entityManager.createQuery("DELETE FROM WaterLog wl WHERE wl.patient.id = :pid")
                            .setParameter("pid", patient.getId())
                            .executeUpdate();

                    // Xóa các record trong notifications liên quan đến patient
                    entityManager.createQuery("DELETE FROM Notification n WHERE n.patient.id = :pid")
                            .setParameter("pid", patient.getId())
                            .executeUpdate();

                    // Xóa các record trong alerts liên quan đến patient
                    entityManager.createQuery("DELETE FROM Alert a WHERE a.patient.id = :pid")
                            .setParameter("pid", patient.getId())
                            .executeUpdate();

                    // Xóa sạch các chỉ số sức khỏe liên quan trước để tránh lỗi ràng buộc khóa ngoại
                    entityManager.createQuery("DELETE FROM DailyHealthLog hl WHERE hl.patient.id = :pid")
                            .setParameter("pid", patient.getId())
                            .executeUpdate();

                    patientRepository.delete(patient);
                });
                accountRepository.delete(account);
            });

            // Xóa hospital test và account liên quan
            hospitalRepository.findByHospitalCode("HOSP_BM_TEST").ifPresent(hospital -> {
                hospitalRepository.delete(hospital);
            });
            accountRepository.findByEmail("hospital_admin_test@example.com").ifPresent(account -> {
                accountRepository.delete(account);
            });
            return null;
        });
    }

    @Test
    void testPatientFillsAndSavesDailyHealthLog() {
        // Bước 4: Bệnh nhân di chuyển trực tiếp tới trang ghi nhận chỉ số sức khỏe
        page.navigate("http://localhost:" + port + "/patient/log");

        // Bước 5: Click chọn mốc đo thời điểm "Buổi sáng" (giới hạn trong panelBpMilestone để tránh trùng với panel đường huyết)
        page.locator("#panelBpMilestone .ms-card:has-text('Buổi sáng')").click();

        // Đợi panel nhập chỉ số huyết áp của mốc đo hiển thị
        page.locator("#systolicBp").waitFor();

        // Bước 6: Điền đầy đủ các chỉ số sức khỏe yêu cầu (Huyết áp tâm thu, Tâm trương, Nhịp tim, Ghi chú)
        page.fill("#systolicBp", "125");
        page.fill("#diastolicBp", "85");
        page.fill("#heartRate", "78");
        page.fill("#notesBp", "Đo huyết áp sau khi ngủ dậy, cảm thấy bình thường.");

        // Bước 7: Nhấn nút lưu chỉ số huyết áp
        page.click("#panelBpInput button.btn-next");

        // Bước 8: Chờ hệ thống lưu thành công và chuyển hướng về dashboard kèm param thành công
        page.waitForURL("**/patient/dashboard?logSuccess=true");

        // Bước 9: Xác nhận thông báo thành công hiển thị trên giao diện UI của trang dashboard
        Locator successAlert = page.locator("#successAlert");
        assertTrue(successAlert.isVisible(), "Thông báo thành công trên giao diện phải hiển thị!");
        assertTrue(successAlert.textContent().contains("Chỉ số sức khỏe của bạn đã được ghi nhận vào nhật ký."), "Nội dung thông báo thành công không đúng!");

        // Bước 10: Xác nhận dữ liệu được lưu chính xác trong Cơ sở dữ liệu (DB) thông qua Repository
        accountRepository.findByEmail(TEST_EMAIL).ifPresent(account -> {
            patientRepository.findByAccountId(account.getId()).ifPresent(patient -> {
                List<DailyHealthLog> logs = healthLogRepository.findByPatientIdOrderByLogTimeDesc(patient.getId());
                assertFalse(logs.isEmpty(), "Dữ liệu nhật ký sức khỏe không được lưu vào DB!");
                
                DailyHealthLog latestLog = logs.get(0);
                assertEquals(125, latestLog.getSystolicBp(), "Chỉ số Huyết áp Tâm thu lưu trong DB bị sai!");
                assertEquals(85, latestLog.getDiastolicBp(), "Chỉ số Huyết áp Tâm trương lưu trong DB bị sai!");
                assertEquals(78, latestLog.getHeartRate(), "Chỉ số Nhịp tim lưu trong DB bị sai!");
                assertEquals("Đo huyết áp sau khi ngủ dậy, cảm thấy bình thường.", latestLog.getPatientNotes(), "Nội dung Ghi chú lưu trong DB bị sai!");
                assertEquals("MANUAL", latestLog.getInputMethod(), "Phương thức nhập không phải MANUAL!");
                assertEquals("MORNING", latestLog.getLogType(), "Mốc thời điểm đo lưu trong DB không phải MORNING!");
            });
        });
    }
}
