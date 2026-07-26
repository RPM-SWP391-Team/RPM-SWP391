package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Appointment02Test {

    @LocalServerPort
    private int port;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

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

    private static final String TEST_PATIENT_EMAIL = "patient_apt01@example.com";
    private static final String TEST_PASSWORD = "Password123";

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        try {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setSlowMo(100)
                    .setChannel("chrome"));
        } catch (Exception e) {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setSlowMo(100)
                    .setChannel("msedge"));
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUpAndLogin() {
        setupTestData();

        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1920, 1080));
        page = context.newPage();

        // Step 1 & 2: Open browser and navigate to Login page
        page.navigate("http://localhost:" + port + "/auth/login");

        // Step 3 & 4: Enter Email and Password
        page.fill("#username", TEST_PATIENT_EMAIL);
        page.fill("#password", TEST_PASSWORD);

        // Step 5: Click Login
        page.click("button[type='submit']");

        // Step 6: Verify Dashboard is displayed (redirects to patient dashboard or appointments)
        // Wait for successful login routing
        page.waitForURL("**/patient/**");
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
        cleanupTestData();
    }

    private void setupTestData() {
        cleanupTestData();

        transactionTemplate.execute(status -> {
            // Setup Hospital
            Hospital hospital = hospitalRepository.findAll().stream().findFirst().orElseGet(() -> {
                Account hospitalAccount = Account.builder()
                        .email("hosp_apt01@example.com")
                        .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                        .role("HOSPITAL_ADMIN")
                        .isEmailVerified(true)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                hospitalAccount = accountRepository.save(hospitalAccount);

                Hospital newHospital = Hospital.builder()
                        .hospitalCode("HOSP_APT01")
                        .fullName("Hospital APT 01")
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                return hospitalRepository.save(newHospital);
            });

            // Setup Doctor
            Account doctorAccount = Account.builder()
                    .email("doctor_apt01@example.com")
                    .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                    .role("DOCTOR")
                    .isEmailVerified(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            doctorAccount = accountRepository.save(doctorAccount);

            Doctor doctor = Doctor.builder()
                    .account(doctorAccount)
                    .hospital(hospital)
                    .doctorCode("DOC_APT01")
                    .fullName("Doctor APT 01")
                    .phone("0987654321")
                    .capacityLimit(50)
                    .currentPatientCount(0)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            doctorRepository.save(doctor);

            // Setup Patient
            Account patientAccount = Account.builder()
                    .email(TEST_PATIENT_EMAIL)
                    .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                    .role("PATIENT")
                    .isEmailVerified(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            patientAccount = accountRepository.save(patientAccount);

            Patient patient = Patient.builder()
                    .account(patientAccount)
                    .hospital(hospital)
                    .fullName("Patient APT 01")
                    .phone("0123456789")
                    .status("TREATING")
                    .registrationSource("ONLINE")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            patientRepository.save(patient);
            return null;
        });
    }

    private void cleanupTestData() {
        transactionTemplate.execute(status -> {
            accountRepository.findByEmail(TEST_PATIENT_EMAIL).ifPresent(account -> {
                patientRepository.findByAccountId(account.getId()).ifPresent(patient -> {
                    entityManager.createQuery("DELETE FROM Notification n WHERE n.patient.id = :pid").setParameter("pid", patient.getId()).executeUpdate();
                    entityManager.createQuery("DELETE FROM Appointment a WHERE a.patient.id = :pid")
                            .setParameter("pid", patient.getId())
                            .executeUpdate();
                    patientRepository.delete(patient);
                });
                accountRepository.delete(account);
            });

            accountRepository.findByEmail("doctor_apt01@example.com").ifPresent(account -> {
                doctorRepository.findByAccountId(account.getId()).ifPresent(doctor -> {
                    doctorRepository.delete(doctor);
                });
                accountRepository.delete(account);
            });

            hospitalRepository.findByHospitalCode("HOSP_APT01").ifPresent(hospital -> {
                hospitalRepository.delete(hospital);
            });
            accountRepository.findByEmail("hosp_apt01@example.com").ifPresent(account -> {
                accountRepository.delete(account);
            });
            return null;
        });
    }

    @Test
    void testExecute() {
        // Step 7: Navigate to Appointments page
        page.navigate("http://localhost:" + port + "/patient/appointments");

        // Step 8: Click on "Book Appointment" button
        // Since it's a CSS modal, clicking the anchor that opens it, or just showing the modal if it's hidden by CSS
        page.locator("a[href='/patient/appointments/book']").click();
        
        // Wait for modal fields to be visible
        page.locator("#doctorId").waitFor();

        // Step 9: Select a Doctor
        // We select the first available valid option
        // Left doctor empty

        // Step 10: Select Appointment Time (Future date)
        // Format: yyyy-MM-ddTHH:mm
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        String formattedTime = tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        page.evalOnSelector("#appointmentTime", "el => el.value = '" + formattedTime + "'");

        // Step 11: Select Appointment Type
        page.selectOption("#appointmentType", "INITIAL");

        // Step 12: Enter Request Reason
        page.fill("#patientRequestReason", "Routine checkup for APT 01");

        // Step 13: Click Save button
        page.click("#bookAppointmentForm button[type='submit']");

        // Verify: Success message and new appointment is displayed in the list
        page.waitForURL("**/patient/appointments**");
        
        // Check for success alert (assuming standard ID or class for alerts)
        // Check if there is an alert div containing success message
        boolean hasAlert = page.locator(".alert-success").isVisible() || page.url().contains("success");
        assertTrue(hasAlert, "Success message or URL parameter should be present");

        // Verify database state
        accountRepository.findByEmail(TEST_PATIENT_EMAIL).ifPresent(account -> {
            patientRepository.findByAccountId(account.getId()).ifPresent(patient -> {
                List<Appointment> appointments = appointmentRepository.findAll();
                boolean found = appointments.stream().anyMatch(a -> 
                    a.getPatient().getId().equals(patient.getId()) &&
                    a.getPatientRequestReason().equals("Routine checkup for APT 01") &&
                    a.getStatus().equals("PENDING")
                );
                assertTrue(found, "Appointment should be saved in DB with PENDING status");
            });
        });
    }
}

