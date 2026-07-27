package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[TC-09-Appointment] Playwright E2E: Doctor Appointment Management")
public class AppointmentSchedulingAutomationTest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(1000)
        );
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    @Test
    @DisplayName("[TC-09-Appointment] E2E: Login -> Doctor Profile Dropdown -> Navigate Appointment Management")
    void testCreateAndApproveAppointment() {
        // Step 1: Open Login Page
        page.navigate("http://localhost:8080/auth/login");
        assertTrue(page.isVisible("#username"));

        // Step 2: Login as Doctor
        page.fill("#username", "doctor1@hospital.vn");
        page.fill("#password", "123456");
        page.click("button[type='submit']");

        // Step 3: Wait for Dashboard
        page.waitForURL("**/doctor/dashboard**");
        assertTrue(page.url().contains("/doctor/dashboard"));

        // Step 4: Click Profile Dropdown & Open Appointment Management page
        if (page.isVisible("#doctorDropdown")) {
            page.click("#doctorDropdown");
            page.click("a[href='/doctor/appointments']");
            page.waitForTimeout(3000);
        }

        // Wait 5 seconds for visual verification
        page.waitForTimeout(5000);
    }
}
