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

    private static final String TEST_EMAIL = "novakimbi1709@gmail.com";
    private static final String TEST_PASSWORD = "12345678";

    @BeforeAll
    static void launchBrowser() {
        System.out.println("========== [PLAYWRIGHT INIT START] ==========");
        try {
            System.out.println("Step 1: Creating Playwright instance...");
            playwright = Playwright.create();
            System.out.println("-> Playwright instance created successfully.");
        } catch (Throwable t) {
            System.err.println("!!! FAILED to create Playwright instance:");
            t.printStackTrace();
            throw t;
        }

        try {
            System.out.println("Step 2: Launching Chromium with channel='chrome' (headless=false)...");
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(1000)
                    .setChannel("chrome"));
            System.out.println("-> Chromium with channel='chrome' launched successfully.");
            return;
        } catch (Throwable t) {
            System.err.println("!!! Failed to launch with channel 'chrome': " + t.getMessage());
        }

        try {
            System.out.println("Step 3: Launching Chromium with channel='msedge' (headless=false)...");
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(1000)
                    .setChannel("msedge"));
            System.out.println("-> Chromium with channel='msedge' launched successfully.");
            return;
        } catch (Throwable t) {
            System.err.println("!!! Failed to launch with channel 'msedge': " + t.getMessage());
        }

        try {
            System.out.println("Step 4: Launching default bundled Chromium (headless=false, no channel)...");
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(1000));
            System.out.println("-> Bundled Chromium launched successfully.");
            return;
        } catch (Throwable t) {
            System.err.println("!!! Failed to launch bundled Chromium: " + t.getMessage());
        }

        try {
            System.out.println("Step 5: Fallback to headless bundled Chromium...");
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true));
            System.out.println("-> Headless Chromium launched successfully.");
        } catch (Throwable t) {
            System.err.println("!!! Critical: Failed to launch any browser!");
            t.printStackTrace();
            throw new RuntimeException("Cannot launch browser", t);
        }
        System.out.println("========== [PLAYWRIGHT INIT SUCCESS] ==========");
    }

    @AfterAll
    static void closeBrowser() {
        // Đóng trình duyệt sau khi chạy xong tất cả các ca kiểm thử
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUp() {
        System.out.println("========== [SETUP START] ==========");
        try {
            System.out.println("-> Setting up test data...");
            setupTestData();
            System.out.println("-> Test data set up successfully.");
            
            System.out.println("-> Creating BrowserContext...");
            context = browser.newContext();
            System.out.println("-> BrowserContext created.");
            
            System.out.println("-> Creating new Page...");
            page = context.newPage();
            System.out.println("-> Page created successfully.");
        } catch (Throwable t) {
            System.err.println("!!! Critical Error in @BeforeEach setUp:");
            t.printStackTrace();
            throw t;
        }
        System.out.println("========== [SETUP END] ==========");
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
        // Dọn dẹp dữ liệu kiểm thử sau khi hoàn thành
        cleanupTestData();
    }

    private void setupTestData() {
        cleanupTestData();

        transactionTemplate.execute(status -> {
            // Khởi tạo Bệnh viện mẫu
            Hospital hospital = hospitalRepository.findAll().stream().findFirst().orElseGet(() -> {
                Hospital newHospital = Hospital.builder()
                        .hospitalCode("HOSP_BM_TEST_E2E")
                        .fullName("Bệnh viện Bạch Mai E2E")
                        .address("Giải Phóng, Hà Nội")
                        .phone("02438693731")
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                return hospitalRepository.save(newHospital);
            });

            // Khởi tạo Hồ sơ Bệnh lý mẫu (Cao Huyết Áp - ID = 1)
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

            // Tạo tài khoản Account kiểm thử
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
                    .fullName("Nguyễn Văn E2E")
                    .dateOfBirth(LocalDate.of(1985, 10, 15))
                    .gender("MALE")
                    .phone("0987112233")
                    .address("Cầu Giấy, Hà Nội")
                    .emergencyContactName("Người Thân E2E")
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
            accountRepository.findByEmail(TEST_EMAIL).ifPresent(acc -> {
                patientRepository.findByAccountId(acc.getId()).ifPresent(p -> {
                    entityManager.createQuery("DELETE FROM MedicationLog ml WHERE ml.patientMedication.id IN (SELECT pm.id FROM PatientMedication pm WHERE pm.patient.id = :pid)")
                            .setParameter("pid", p.getId())
                            .executeUpdate();
                    entityManager.createQuery("DELETE FROM PatientMedication pm WHERE pm.patient.id = :pid")
                            .setParameter("pid", p.getId())
                            .executeUpdate();
                    entityManager.createQuery("DELETE FROM ExerciseLog el WHERE el.patient.id = :pid")
                            .setParameter("pid", p.getId())
                            .executeUpdate();
                    entityManager.createQuery("DELETE FROM WaterLog wl WHERE wl.patient.id = :pid")
                            .setParameter("pid", p.getId())
                            .executeUpdate();
                    entityManager.createQuery("DELETE FROM Notification n WHERE n.patient.id = :pid")
                            .setParameter("pid", p.getId())
                            .executeUpdate();
                    entityManager.createQuery("DELETE FROM Alert a WHERE a.patient.id = :pid")
                            .setParameter("pid", p.getId())
                            .executeUpdate();
                    entityManager.createQuery("DELETE FROM DailyHealthLog hl WHERE hl.patient.id = :pid")
                            .setParameter("pid", p.getId())
                            .executeUpdate();

                    patientRepository.delete(p);
                });
                accountRepository.delete(acc);
            });

            hospitalRepository.findByHospitalCode("HOSP_BM_TEST_E2E").ifPresent(hospital -> {
                hospitalRepository.delete(hospital);
            });

            entityManager.flush();
            return null;
        });
    }

    @Test
    void testPatientFillsAndSavesDailyHealthLog() {
        System.out.println("========== [TEST START] testPatientFillsAndSavesDailyHealthLog ==========");
        try {
            // Bước 1: Điều hướng tới trang đăng nhập với timeout 15 giây
            String loginUrl = "http://localhost:" + port + "/auth/login";
            System.out.println("Step 1: Navigating to " + loginUrl + " (timeout=15s)...");
            page.navigate(loginUrl, new Page.NavigateOptions().setTimeout(15000));
            System.out.println("-> Navigation to login successful.");

            // Bước 2: Điền thông tin đăng nhập của bệnh nhân
            System.out.println("Step 2: Filling username and password...");
            page.fill("#username", TEST_EMAIL);
            page.fill("#password", TEST_PASSWORD);
            System.out.println("-> Login credentials filled.");

            // Bước 3: Click nút đăng nhập và chờ chuyển hướng
            System.out.println("Step 3: Clicking submit button and waiting for navigation...");
            page.click("form button[type='submit']");
            page.waitForURL("**/patient/**", new Page.WaitForURLOptions().setTimeout(15000));
            System.out.println("-> Redirection successful. Current URL: " + page.url());

            // Bước 4: Điều hướng trực tiếp tới trang ghi nhận chỉ số sức khỏe
            String logUrl = "http://localhost:" + port + "/patient/log";
            System.out.println("Step 4: Navigating to " + logUrl + " (timeout=15s)...");
            page.navigate(logUrl, new Page.NavigateOptions().setTimeout(15000));
            System.out.println("-> Navigation to log successful.");

            // Bước 5: Chọn mốc đo thời điểm "Buổi sáng" trên giao diện wizard
            System.out.println("Step 5: Clicking Morning milestone card...");
            page.click("#panelBpMilestone .ms-card:has-text('Buổi sáng')");

            // Đợi panel nhập chỉ số hiển thị
            System.out.println("-> Waiting for systolicBp input field to be visible...");
            page.locator("#systolicBp").waitFor();

            // Bước 6: Điền các thông số huyết áp, nhịp tim và ghi chú
            System.out.println("Step 6: Filling health indicator values...");
            page.fill("#systolicBp", "125");
            page.fill("#diastolicBp", "85");
            page.fill("#heartRate", "78");
            page.fill("#notesBp", "Đo huyết áp sau khi ngủ dậy, cảm thấy bình thường.");

            // Bước 7: Nhấn nút lưu chỉ số huyết áp
            System.out.println("Step 7: Clicking Save button...");
            page.click("button:has-text('Lưu Huyết áp')");

            // Bước 8: Chờ chuyển hướng thành công
            System.out.println("Step 8: Waiting for saveSuccess URL parameter...");
            page.waitForURL("**/patient/log?saveSuccess=true", new Page.WaitForURLOptions().setTimeout(15000));
            System.out.println("-> Form save redirect successful. URL: " + page.url());

            // Bước 9: Xác nhận thông báo thành công hiển thị trên UI
            System.out.println("Step 9: Verifying success alert on UI...");
            Locator successAlert = page.locator(".alert-success");
            assertTrue(successAlert.isVisible(), "Thông báo thành công trên giao diện phải hiển thị!");
            assertTrue(successAlert.textContent().contains("Chỉ số sức khỏe đã được ghi nhận thành công!"), "Nội dung thông báo thành công không đúng!");
            System.out.println("-> UI assertion passed.");

            // Bước 10: Xác nhận dữ liệu được ghi nhận chính xác trong DB
            System.out.println("Step 10: Verifying data in database...");
            accountRepository.findByEmail(TEST_EMAIL).ifPresent(acc -> {
                patientRepository.findByAccountId(acc.getId()).ifPresent(p -> {
                    List<DailyHealthLog> logs = healthLogRepository.findByPatientIdOrderByLogTimeDesc(p.getId());
                    assertFalse(logs.isEmpty(), "Nhật ký sức khỏe chưa được lưu vào database!");

                    DailyHealthLog latestLog = logs.get(0);
                    assertEquals(125, latestLog.getSystolicBp(), "Chỉ số Huyết áp Tâm thu lưu trong DB không khớp!");
                    assertEquals(85, latestLog.getDiastolicBp(), "Chỉ số Huyết áp Tâm trương lưu trong DB không khớp!");
                    assertEquals(78, latestLog.getHeartRate(), "Chỉ số Nhịp tim lưu trong DB không khớp!");
                    assertEquals("Đo huyết áp sau khi ngủ dậy, cảm thấy bình thường.", latestLog.getPatientNotes(), "Nội dung Ghi chú lưu trong DB không khớp!");
                    assertEquals("MANUAL", latestLog.getInputMethod(), "Phương thức nhập không phải MANUAL!");
                    assertEquals("MORNING", latestLog.getLogType(), "Mốc thời điểm đo lưu trong DB không phải MORNING!");
                });
            });
            System.out.println("========== [TEST SUCCESS] All assertions passed! ==========");
        } catch (Throwable t) {
            System.err.println("========== [TEST FAILED] Exception caught inside test method: ==========");
            t.printStackTrace();
            throw t;
        }
    }
}
