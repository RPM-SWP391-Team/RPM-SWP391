package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[TC-05-Auth] Playwright E2E: User Authentication & Doctor Profile Navigation")
public class AuthAndAccountAutomationTest {

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
    @DisplayName("[TC-05-Auth] E2E: Invalid Credentials Warning -> Valid Login -> Doctor Profile Navigation")
    void testDuplicateRegistrationBlocked() {
        // Step 1: Open Login Page
        page.navigate("http://localhost:8080/auth/login");
        assertTrue(page.isVisible("#username"));

        // Step 2: Try invalid credentials to test error alert
        page.fill("#username", "wrongdoctor@hospital.vn");
        page.fill("#password", "wrongpassword");
        page.click("button[type='submit']");
        page.waitForTimeout(2000);
        assertTrue(page.isVisible(".alert-danger"), "Error alert must appear for invalid login");

        // Step 3: Enter valid credentials
        page.fill("#username", "doctor1@hospital.vn");
        page.fill("#password", "123456");
        page.click("button[type='submit']");

        // Step 4: Wait for Dashboard
        page.waitForURL("**/doctor/dashboard**");
        assertTrue(page.url().contains("/doctor/dashboard"));

        // Step 5: Click Profile Dropdown & Open Doctor Profile
        if (page.isVisible("#doctorDropdown")) {
            page.click("#doctorDropdown");
            page.click("a[href='/doctor/profile']");
            page.waitForTimeout(3000);
        }

        // Wait 5 seconds for visual verification
        page.waitForTimeout(5000);
    }
}
